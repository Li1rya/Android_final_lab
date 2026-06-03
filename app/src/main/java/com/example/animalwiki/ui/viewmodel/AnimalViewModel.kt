package com.example.animalwiki.ui.viewmodel

import android.app.Application
import android.graphics.Bitmap
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.animalwiki.data.model.Animal
import com.example.animalwiki.data.model.BaiduBaikeInfo
import com.example.animalwiki.data.model.Favorite
import com.example.animalwiki.data.model.Folder
import com.example.animalwiki.data.model.History
import com.example.animalwiki.data.model.SearchMode
import com.example.animalwiki.data.network.RetrofitClient
import com.example.animalwiki.data.repository.AnimalRepository
import com.example.animalwiki.data.repository.BaiduRecognitionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf


data class RecognitionResult(
    val name: String,
    val matchedAnimal: Animal?,
    val confidence: Float,
    val baikeInfo: BaiduBaikeInfo? = null
)

class AnimalViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = AnimalRepository.getInstance(application)

    private val recognitionRepository = BaiduRecognitionRepository(
        RetrofitClient.apiService,
        application
    )

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

    private val _searchMode = MutableStateFlow(SearchMode.ALL)
    val searchMode: StateFlow<SearchMode> = _searchMode.asStateFlow()

    private val _historyList = MutableStateFlow<List<History>>(emptyList())
    val historyList: StateFlow<List<History>> = _historyList.asStateFlow()

    private val _favoriteList = MutableStateFlow<List<Favorite>>(emptyList())
    val favoriteList: StateFlow<List<Favorite>> = _favoriteList.asStateFlow()

    private val _folderList = MutableStateFlow<List<Folder>>(emptyList())
    val folderList: StateFlow<List<Folder>> = _folderList.asStateFlow()

    // ✅ 新增：当前选中的收藏夹ID
    private val _currentFolderId = MutableStateFlow(0L) // 默认显示默认收藏夹
    val currentFolderId: StateFlow<Long> = _currentFolderId.asStateFlow()

    private val _isFavorite = MutableStateFlow(false)
    val isFavorite: StateFlow<Boolean> = _isFavorite.asStateFlow()

    private val _dailyRecommendation = MutableStateFlow<Animal?>(null)
    val dailyRecommendation: StateFlow<Animal?> = _dailyRecommendation.asStateFlow()

    private val _recognitionResults = MutableStateFlow<List<RecognitionResult>>(emptyList())
    val recognitionResults: StateFlow<List<RecognitionResult>> = _recognitionResults.asStateFlow()

    private val _isRecognizing = MutableStateFlow(false)
    val isRecognizing: StateFlow<Boolean> = _isRecognizing.asStateFlow()

    private val _recognitionError = MutableStateFlow<String?>(null)
    val recognitionError: StateFlow<String?> = _recognitionError.asStateFlow()

    init {
        viewModelScope.launch {
            _isLoading.value = true
            repository.initializeDatabase()
            loadAllAnimals()
            _isLoading.value = false
        }

        viewModelScope.launch {
            repository.getAllHistory().collect { _historyList.value = it }
        }

        // ✅ 正确写法：用flatMapLatest自动切换收藏列表
        viewModelScope.launch {
            _currentFolderId
                .flatMapLatest { folderId ->
                    repository.getFavoritesByFolder(folderId)
                }
                .collect { favorites ->
                    _favoriteList.value = favorites
                }
        }

        // ✅ 修复收藏夹列表监听，自动切换到第一个收藏夹
        viewModelScope.launch {
            repository.getAllFolders().collect { folders ->
                _folderList.value = folders
                // 自动设置当前收藏夹为第一个（解决id不匹配问题）
                if (folders.isNotEmpty() && _currentFolderId.value == 0L) {
                    _currentFolderId.value = folders.first().id
                }
            }
        }
    }

    fun loadAllAnimals() {
        viewModelScope.launch { _animals.value = repository.getAllAnimals() }
    }

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
        if (query.isBlank()) {
            _searchResults.value = emptyList()
        } else {
            searchAnimals(query)
        }
    }

    fun onSearchModeChange(mode: SearchMode) {
        _searchMode.value = mode
        if (_searchQuery.value.isNotBlank()) {
            searchAnimals(_searchQuery.value)
        }
    }

    fun searchAnimals(keyword: String) {
        viewModelScope.launch {
            _searchResults.value = repository.searchAnimals(keyword, _searchMode.value)
        }
    }

    fun getAnimalById(id: String) {
        viewModelScope.launch { _currentAnimal.value = repository.getAnimalById(id) }
    }

    fun getAnimalByName(name: String) {
        viewModelScope.launch { _currentAnimal.value = repository.searchByName(name) }
    }

    fun getAnimalImages(animal: Animal): List<Int> {
        return repository.getImageResIds(animal.latinName, maxImages = 5)
    }

    fun getAnimalImage(animal: Animal, index: Int = 1): Int {
        return repository.getImageResId(animal.latinName, index)
    }

    fun insertHistory(history: History) {
        viewModelScope.launch { repository.insertHistory(history) }
    }

    fun deleteHistory(history: History) {
        viewModelScope.launch { repository.deleteHistory(history) }
    }

    fun clearAllHistory() {
        viewModelScope.launch { repository.clearAllHistory() }
    }

    // ✅ 修改后的 toggleFavorite：只处理取消收藏
    suspend fun toggleFavorite(animal: Animal): Boolean {
        val isNowFavorite = repository.toggleFavorite(animal)
        _isFavorite.value = isNowFavorite
        return isNowFavorite
    }

    // ✅ 新增：添加到指定收藏夹（解决报错的核心方法）
    suspend fun addToFavorite(animal: Animal, folderId: Long): Boolean {
        val isAdded = repository.addToFavorite(animal, folderId)
        if (isAdded) {
            _isFavorite.value = true
        }
        return isAdded
    }

    fun checkIsFavorite(animalId: String) {
        viewModelScope.launch {
            _isFavorite.value = repository.getFavoriteByAnimalId(animalId) != null
        }
    }

    fun deleteFavorite(favorite: Favorite) {
        viewModelScope.launch { repository.deleteFavorite(favorite) }
    }

    fun clearAllFavorites() {
        viewModelScope.launch { repository.clearAllFavorites() }
    }

    // ==================== 新增：收藏夹操作方法 ====================
    fun insertFolder(name: String) {
        viewModelScope.launch {
            repository.insertFolder(name)
        }
    }

    // AnimalViewModel.kt
// ✅ 修改为返回Boolean，表示是否删除成功
    suspend fun deleteFolder(folder: Folder): Boolean {
        if (folder.isDefault) {
            return false
        }
        return repository.deleteFolder(folder)
    }

    fun switchFolder(folderId: Long) {
        _currentFolderId.value = folderId
    }

    // AnimalViewModel.kt
// 新增：获取指定收藏夹的收藏列表
    fun getFavoritesByFolder(folderId: Long): Flow<List<Favorite>> {
        return repository.getFavoritesByFolder(folderId)
    }

    // ==================== 以下所有原有方法完全不变 ====================
    fun recognizeImage(bitmap: Bitmap) {
        viewModelScope.launch {
            _isRecognizing.value = true
            _recognitionError.value = null
            _recognitionResults.value = emptyList()

            try {
                Log.d("ViewModel", "开始识别，bitmap=${bitmap.width}x${bitmap.height}")
                val baiduResults = recognitionRepository.recognizeAnimal(bitmap)

                Log.d("ViewModel", "百度返回原始结果数: ${baiduResults.size}")
                baiduResults.forEachIndexed { i, r ->
                    Log.d("ViewModel", "结果[$i]: name=${r.name}, score=${r.score}")
                }

                if (baiduResults.isEmpty()) {
                    _recognitionError.value = "未能识别出动物，请尝试重新拍摄"
                    return@launch
                }

                val localAnimals = _animals.value.ifEmpty {
                    repository.getAllAnimals().also { _animals.value = it }
                }
                Log.d("ViewModel", "本地动物数: ${localAnimals.size}")

                val results = baiduResults.mapNotNull { baidu ->
                    if ((baidu.score ?: 0.0) < 0.05) {
                        Log.d("ViewModel", "过滤低置信度: ${baidu.name}, score=${baidu.score}")
                        return@mapNotNull null
                    }

                    val matched = recognitionRepository.matchWithLocalData(baidu, localAnimals)
                    RecognitionResult(
                        name = baidu.name ?: "未知动物",
                        matchedAnimal = matched,
                        confidence = (baidu.score ?: 0.0).toFloat(),
                        baikeInfo = baidu.baikeInfo
                    )
                }.sortedByDescending { it.confidence }

                Log.d("ViewModel", "映射后UI结果数: ${results.size}")
                results.forEachIndexed { i, r ->
                    Log.d("ViewModel", "UI结果[$i]: name=${r.name}, confidence=${r.confidence}, matched=${r.matchedAnimal != null}")
                }

                _recognitionResults.value = results

                results.firstOrNull()?.matchedAnimal?.let { animal ->
                    insertHistory(
                        History(
                            animalId = animal.id,
                            name = animal.cnname.firstOrNull() ?: "未知动物",
                            category = "${animal.classification.className} ${animal.classification.order}",
                            viewTime = System.currentTimeMillis()
                        )
                    )
                }

            } catch (e: Exception) {
                Log.e("ViewModel", "识别流程异常", e)
                _recognitionError.value = "识别失败：${e.message}"
            } finally {
                _isRecognizing.value = false
            }
        }
    }

    fun clearRecognitionState() {
        _recognitionResults.value = emptyList()
        _recognitionError.value = null
        _isRecognizing.value = false
    }
}