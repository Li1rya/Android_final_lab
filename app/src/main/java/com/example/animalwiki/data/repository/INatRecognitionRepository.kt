package com.example.animalwiki.data.repository

import android.R.attr.bitmap
import android.graphics.Bitmap
import android.util.Log
import com.example.animalwiki.data.model.INatIdentifyResponse
import com.example.animalwiki.data.model.INatTaxon
import com.example.animalwiki.data.network.AnimalApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.ByteArrayOutputStream

class INatRecognitionRepository(
    private val apiService: AnimalApiService
) {
    private val TAG = "INatRecognition"

    /**
     * 识别图片，返回候选物种列表
     */
    suspend fun recognizeAnimal(bitmap: Bitmap): List<INatTaxon> {
        return withContext(Dispatchers.IO) {
            try {
                // Bitmap 转 JPEG 字节数组，控制大小避免 OOM
                val stream = ByteArrayOutputStream()
                // 如果图片太大，先缩放
                val scaledBitmap = scaleBitmapIfNeeded(bitmap)
                scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 85, stream)
                val imageBytes = stream.toByteArray()
                stream.close()

                Log.d(TAG, "上传图片大小: ${imageBytes.size / 1024} KB")

                // 构建 Multipart
                val requestBody = imageBytes.toRequestBody("image/jpeg".toMediaTypeOrNull())
                val multipart = MultipartBody.Part.createFormData(
                    "image", "photo.jpg", requestBody
                )

                // 调用识别 API
                val response = apiService.identifyImage(multipart)
                parseIdentificationResults(response)

            } catch (e: Exception) {
                Log.e(TAG, "识别失败: ${e.message}", e)
                emptyList()
            }
        }
    }

    /**
     * 缩放过大的图片（iNaturalist 建议不超过 2MB）
     */
    private fun scaleBitmapIfNeeded(bitmap: Bitmap): Bitmap {
        val maxDimension = 1024
        val width = bitmap.width
        val height = bitmap.height

        if (width <= maxDimension && height <= maxDimension) return bitmap

        val ratio = width.toFloat() / height.toFloat()
        val newWidth: Int
        val newHeight: Int

        if (width > height) {
            newWidth = maxDimension
            newHeight = (maxDimension / ratio).toInt()
        } else {
            newHeight = maxDimension
            newWidth = (maxDimension * ratio).toInt()
        }

        return Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)
    }

    /**
     * 解析识别结果
     * 注意：iNaturalist 返回的 vision_score 可能在不同层级
     */
    private fun parseIdentificationResults(response: INatIdentifyResponse): List<INatTaxon> {
        val results = response.results ?: return emptyList()

        return results.mapNotNull { identification ->
            identification.taxon?.let { taxon ->
                // 保留 vision_score 到 taxon 的扩展属性（如果需要）
                // 这里简化处理：只要有结果就返回，置信度过滤交给 ViewModel
                taxon
            }
        }.distinctBy { it.id }
    }

    /**
     * 本地数据库匹配
     */
    fun matchWithLocalData(
        taxon: INatTaxon,
        localAnimals: List<com.example.animalwiki.data.model.Animal>
    ): com.example.animalwiki.data.model.Animal? {
        // 1. 优先用拉丁学名精确匹配
        val latinName = taxon.name ?: return null
        localAnimals.find {
            it.latinName.equals(latinName, ignoreCase = true)
        }?.let { return it }

        // 2. 用中文俗名匹配
        val commonName = taxon.preferredCommonName
        if (!commonName.isNullOrBlank()) {
            localAnimals.find { animal ->
                animal.cnname.any { it.contains(commonName, ignoreCase = true) }
            }?.let { return it }
        }

        // 3. 用属名模糊匹配
        val genus = latinName.split(" ").firstOrNull()
        if (genus != null) {
            localAnimals.find {
                it.latinName.startsWith(genus, ignoreCase = true)
            }?.let { return it }
        }

        return null
    }
}