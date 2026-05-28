package com.example.animalwiki.ui.viewmodel

import android.app.Application
import android.graphics.Bitmap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.animalwiki.data.model.Animal
import com.example.animalwiki.data.model.Favorite
import com.example.animalwiki.data.model.History
import com.example.animalwiki.data.model.INatTaxon
import com.example.animalwiki.data.model.SearchMode        // ✅ 新增
import com.example.animalwiki.data.network.RetrofitClient
import com.example.animalwiki.data.repository.AnimalRepository
import com.example.animalwiki.data.repository.INatRecognitionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class RecognitionResult(
    val taxon: INatTaxon,
    val matchedAnimal: Animal?,
    val confidence: Float
)

class AnimalViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = AnimalRepository.getInstance(application)
    private val recognitionRepository = INatRecognitionRepository(RetrofitClient.apiService)

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

    // ✅ 新增：搜索模式状态
    private val _searchMode = MutableStateFlow(SearchMode.ALL)
    val searchMode: StateFlow<SearchMode> = _searchMode.asStateFlow()

    private val _historyList = MutableStateFlow<List<History>>(emptyList())
    val historyList: StateFlow<List<History>> = _historyList.asStateFlow()

    private val _favoriteList = MutableStateFlow<List<Favorite>>(emptyList())
    val favoriteList: StateFlow<List<Favorite>> = _favoriteList.asStateFlow()

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
        viewModelScope.launch {
            repository.getAllFavorites().collect { _favoriteList.value = it }
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

    // ✅ 新增：切换搜索模式，切换后自动用当前关键词重新搜索
    fun onSearchModeChange(mode: SearchMode) {
        _searchMode.value = mode
        if (_searchQuery.value.isNotBlank()) {
            searchAnimals(_searchQuery.value)
        }
    }

    // ✅ 修改：带模式的搜索
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
        viewModelScope.launch { repository.deleteFavorite(favorite) }
    }

    fun clearAllFavorites() {
        viewModelScope.launch { repository.clearAllFavorites() }
    }

    fun recognizeImage(bitmap: Bitmap) {
        viewModelScope.launch {
            _isRecognizing.value = true
            _recognitionError.value = null
            _recognitionResults.value = emptyList()

            try {
                val taxons = recognitionRepository.recognizeAnimal(bitmap)
                if (taxons.isEmpty()) {
                    _recognitionError.value = "未能识别出动物，请尝试重新拍摄"
                    return@launch
                }
                val localAnimals = _animals.value.ifEmpty {
                    repository.getAllAnimals().also { _animals.value = it }
                }
                val results = taxons.mapNotNull { taxon ->
                    val visionScore = taxon.rank?.let { 0.85f } ?: 0.5f
                    val matched = recognitionRepository.matchWithLocalData(taxon, localAnimals)
                    RecognitionResult(
                        taxon = taxon,
                        matchedAnimal = matched,
                        confidence = visionScore
                    )
                }.sortedByDescending { it.confidence }

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