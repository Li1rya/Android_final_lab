package com.example.animalwiki.data.repository

import com.example.animalwiki.BuildConfig
import android.graphics.Bitmap
import android.util.Base64
import android.util.Log
import android.content.Context
import com.example.animalwiki.data.model.Animal
import com.example.animalwiki.data.model.BaiduAnimalResult
import com.example.animalwiki.data.model.INatIdentifyResponse
import com.example.animalwiki.data.model.INatTaxon
import com.example.animalwiki.data.network.AnimalApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.ByteArrayOutputStream
import kotlin.collections.find


class BaiduRecognitionRepository(
    private val apiService: AnimalApiService,
    private val context: Context
) {
    private val TAG = "BaiduRecognition"

    // ================== 请替换为你自己的百度应用密钥 ==================
    private val API_KEY = BuildConfig.BAIDU_API_KEY
    private val SECRET_KEY = BuildConfig.BAIDU_SECRET_KEY
    // ================================================================

    // Token 内存缓存（简单实现，如需持久化可改用 DataStore）
    private var cachedToken: String? = null
    private var tokenExpireAt: Long = 0

    /** 获取/刷新 access_token */
    private suspend fun getAccessToken(): String? {
        // 缓存未过期直接返回
        if (cachedToken != null && System.currentTimeMillis() < tokenExpireAt) {
            return cachedToken
        }
        return try {
            val resp = apiService.getBaiduAccessToken(
                clientId = API_KEY,
                clientSecret = SECRET_KEY
            )
            if (resp.accessToken != null) {
                cachedToken = resp.accessToken
                // 提前 5 分钟视为过期
                val expires = (resp.expiresIn ?: 2592000) - 300
                tokenExpireAt = System.currentTimeMillis() + expires * 1000
                Log.d(TAG, "Token 获取成功，有效期 ${resp.expiresIn} 秒")
                cachedToken
            } else {
                Log.e(TAG, "Token 获取失败: ${resp.errorDescription}")
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Token 请求异常", e)
            null
        }
    }

    /** Bitmap → Base64（控制大小不超过 4MB） */
    private fun bitmapToBase64(bitmap: Bitmap): String {
        val baos = ByteArrayOutputStream()
        val quality = if (bitmap.byteCount > 3_000_000) 75 else 90
        bitmap.compress(Bitmap.CompressFormat.JPEG, quality, baos)
        val bytes = baos.toByteArray()
        baos.close()
        val base64 = Base64.encodeToString(bytes, Base64.NO_WRAP)

        // ✅ 关键日志 1：看图片有没有内容
        Log.d(TAG, "Bitmap尺寸: ${bitmap.width}x${bitmap.height}, 字节数: ${bitmap.byteCount}")
        Log.d(TAG, "压缩后JPEG字节: ${bytes.size}, Base64长度: ${base64.length}")

        return base64
    }

    /** 识别图片，返回百度原始结果 */
    suspend fun recognizeAnimal(bitmap: Bitmap): List<BaiduAnimalResult> {
        val token = getAccessToken()

        // ✅ 关键日志 2：Token 拿到了吗？
        Log.d(TAG, "Token=${token?.take(10)}... 是否为空=${token.isNullOrBlank()}")

        if (token.isNullOrBlank()) {
            Log.e(TAG, "Token获取失败，无法请求百度API")
            return emptyList()
        }

        return withContext(Dispatchers.IO) {
            try {
                val base64Str = bitmapToBase64(bitmap)

                // ✅ 关键日志 3：看 HTTP 请求前的参数
                Log.d(TAG, "开始请求百度API，token前10位=${token.take(10)}...")

                val response = apiService.baiduAnimalDetect(
                    accessToken = token,
                    imageBase64 = base64Str,
                    topNum = 6,
                    baikeNum = 1
                )

                // ✅ 关键日志 4：看原始返回
                Log.d(TAG, "百度API返回: result=${response.result}, log_id=${response.logId}")
                Log.d(TAG, "返回结果数量: ${response.result?.size ?: 0}")

                response.result ?: emptyList()
            } catch (e: Exception) {
                // ✅ 关键日志 5：异常详情
                Log.e(TAG, "请求异常: ${e.javaClass.simpleName}: ${e.message}", e)
                emptyList()
            }
        }
    }

    /** 用百度返回的「中文名」去匹配本地 Room 数据库的动物 */
    fun matchWithLocalData(
        baiduResult: BaiduAnimalResult,
        localAnimals: List<Animal>
    ): Animal? {
        val baiduName = baiduResult.name ?: return null

        // 1. 完全相等
        localAnimals.find { it.cnname.any { cn -> cn == baiduName } }?.let { return it }

        // 2. 本地名包含百度名（如本地"东北虎"包含百度"虎"）
        localAnimals.find { it.cnname.any { cn -> cn.contains(baiduName) } }?.let { return it }

        // 3. 百度名包含本地名（如百度"东北虎"包含本地"虎"）
        localAnimals.find { it.cnname.any { cn -> baiduName.contains(cn) } }?.let { return it }

        return null
    }
}