package com.example.animalwiki.ui.viewmodel

import android.app.Application
import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.animalwiki.data.model.Animal
import com.example.animalwiki.data.model.Favorite
import com.example.animalwiki.data.model.History
import com.example.animalwiki.data.model.INatTaxon
import com.example.animalwiki.data.network.RetrofitClient
import com.example.animalwiki.data.repository.AnimalRepository
import com.example.animalwiki.data.repository.INatRecognitionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// 识别结果封装
data class RecognitionResult(
    val taxon: INatTaxon,
    val matchedAnimal: Animal?,      // 本地数据库匹配到的动物，可能为 null
    val confidence: Float            // vision_score 转换后的百分比
)

class AnimalViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = AnimalRepository.getInstance(application)

    // ✅ 新增：识别仓库（单例或工厂模式）
    private val recognitionRepository = INatRecognitionRepository(RetrofitClient.apiService)

    // ==================== 原有状态（完全不变） ====================
    private val _animals = MutableStateFlow<List<Animal>>(emptyList())
    val animals: StateFlow<List<Animal>> = _animals.asStateFlow()

    private val _currentAnimal = MutableStateFlow<Animal?>(null)
    val currentAnimal: StateFlow<Animal?> = _currentAnimal.asStateFlow()

    private val _searchResults = MutableStateFlow<List<Animal>>(emptyList())
    val searchResults: StateFlow<List<Animal>> = _searchResults.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _historyList = MutableStateFlow<List<History>>(emptyList())
    val historyList: StateFlow<List<History>> = _historyList.asStateFlow()

    private val _favoriteList = MutableStateFlow<List<Favorite>>(emptyList())
    val favoriteList: StateFlow<List<Favorite>> = _favoriteList.asStateFlow()

    private val _isFavorite = MutableStateFlow(false)
    val isFavorite: StateFlow<Boolean> = _isFavorite.asStateFlow()

    private val _dailyRecommendation = MutableStateFlow<Animal?>(null)
    val dailyRecommendation: StateFlow<Animal?> = _dailyRecommendation.asStateFlow()

    // ==================== 新增：相机识别状态 ====================
    private val _recognitionResults = MutableStateFlow<List<RecognitionResult>>(emptyList())
    val recognitionResults: StateFlow<List<RecognitionResult>> = _recognitionResults.asStateFlow()

    private val _isRecognizing = MutableStateFlow(false)
    val isRecognizing: StateFlow<Boolean> = _isRecognizing.asStateFlow()

    private val _recognitionError = MutableStateFlow<String?>(null)
    val recognitionError: StateFlow<String?> = _recognitionError.asStateFlow()

    // ==================== 初始化 ====================
    init {
        viewModelScope.launch {
            _isLoading.value = true
            repository.initializeDatabase()
            loadAllAnimals()
            _isLoading.value = false
        }

        viewModelScope.launch {
            repository.getAllHistory().collect { historyList ->
                _historyList.value = historyList
            }
        }

        viewModelScope.launch {
            repository.getAllFavorites().collect {
                _favoriteList.value = it
            }
        }
    }

    // ==================== 原有方法（保持不变） ====================
    fun loadAllAnimals() {
        viewModelScope.launch {
            _animals.value = repository.getAllAnimals()
        }
    }

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
        if (query.isBlank()) {
            _searchResults.value = emptyList()
        } else {
            searchAnimals(query)
        }
    }

    fun searchAnimals(keyword: String) {
        viewModelScope.launch {
            _searchResults.value = repository.searchAnimals(keyword)
        }
    }

    fun getAnimalById(id: String) {
        viewModelScope.launch {
            _currentAnimal.value = repository.getAnimalById(id)
        }
    }

    fun getAnimalByName(name: String) {
        viewModelScope.launch {
            _currentAnimal.value = repository.searchByName(name)
        }
    }

    fun getAnimalImages(animal: Animal): List<Int> {
        return repository.getImageResIds(animal.latinName, maxImages = 5)
    }

    fun getAnimalImage(animal: Animal, index: Int = 1): Int {
        return repository.getImageResId(animal.latinName, index)
    }

    // ==================== 历史记录操作 ====================
    fun insertHistory(history: History) {
        viewModelScope.launch {
            repository.insertHistory(history)
        }
    }

    fun deleteHistory(history: History) {
        viewModelScope.launch {
            repository.deleteHistory(history)
        }
    }

    fun clearAllHistory() {
        viewModelScope.launch {
            repository.clearAllHistory()
        }
    }

    // ==================== 收藏记录相关操作方法 ====================
    suspend fun toggleFavorite(animal: Animal): Boolean {
        val isNowFavorite = repository.toggleFavorite(animal)
        _isFavorite.value = isNowFavorite
        return isNowFavorite
    }

    fun checkIsFavorite(animalId: String) {
        viewModelScope.launch {
            _isFavorite.value = repository.getFavoriteByAnimalId(animalId) != null
        }
    }

    fun deleteFavorite(favorite: Favorite) {
        viewModelScope.launch {
            repository.deleteFavorite(favorite)
        }
    }

    fun clearAllFavorites() {
        viewModelScope.launch {
            repository.clearAllFavorites()
        }
    }

    // ==================== 新增：相机识别核心方法 ====================

    /**
     * 识别图片并匹配本地数据库
     * @param bitmap 拍照得到的 Bitmap
     */
    fun recognizeImage(bitmap: Bitmap) {
        viewModelScope.launch {
            _isRecognizing.value = true
            _recognitionError.value = null
            _recognitionResults.value = emptyList()

            try {
                // 1. 调用 iNaturalist API 识别
                val taxons = recognitionRepository.recognizeAnimal(bitmap)

                if (taxons.isEmpty()) {
                    _recognitionError.value = "未能识别出动物，请尝试重新拍摄"
                    return@launch
                }

                // 2. 获取本地动物列表用于匹配
                val localAnimals = _animals.value.ifEmpty {
                    repository.getAllAnimals().also { _animals.value = it }
                }

                // 3. 逐个匹配并封装结果
                val results = taxons.mapNotNull { taxon ->
                    val visionScore = taxon.rank?.let { 0.85f } ?: 0.5f  // 简化处理，实际应从 API 返回解析
                    val matched = recognitionRepository.matchWithLocalData(taxon, localAnimals)

                    RecognitionResult(
                        taxon = taxon,
                        matchedAnimal = matched,
                        confidence = visionScore
                    )
                }.sortedByDescending { it.confidence }

                _recognitionResults.value = results

                // 4. 自动记录历史（如果匹配到本地动物）
                results.firstOrNull()?.matchedAnimal?.let { animal ->
                    insertHistory(
                        History(
                            animalId = animal.id,
                            name = animal.cnname.firstOrNull() ?: "未知动物",
                            // 从分类信息构造category字段
                            category = "${animal.classification.className} ${animal.classification.order}",
                            viewTime = System.currentTimeMillis()
                        )
                    )
                }

            } catch (e: Exception) {
                _recognitionError.value = "识别失败：${e.message}"
            } finally {
                _isRecognizing.value = false
            }
        }
    }

    /**
     * 清除识别状态（返回相机界面时调用）
     */
    fun clearRecognitionState() {
        _recognitionResults.value = emptyList()
        _recognitionError.value = null
        _isRecognizing.value = false
    }

    /**
     * Uri 转 Bitmap 的辅助方法（在 Screen 层调用）
     */
    // 注意：Bitmap 处理应该在 Screen 层用 Context 完成，避免内存泄漏
}