package com.example.animalwiki.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.animalwiki.data.database.entity.HistoryEntity
import com.example.animalwiki.data.model.Animal
import com.example.animalwiki.data.model.Favorite
import com.example.animalwiki.data.model.History // 新增导入
import com.example.animalwiki.data.repository.AnimalRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import kotlin.random.Random


class AnimalViewModel(application: Application) : AndroidViewModel(application) {

    // 修改：直接获取Repository单例，不再手动创建
    private val repository = AnimalRepository.getInstance(application)

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

    // ==================== 初始化 ====================
    init {
        viewModelScope.launch {
            _isLoading.value = true
            repository.initializeDatabase()
            loadAllAnimals()
            _isLoading.value = false
        }

        // 监听历史记录变化
        viewModelScope.launch {
            repository.getAllHistory().collect { historyList ->
                _historyList.value = historyList
            }
        }

        // 新增：监听收藏记录变化
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

    // ==================== 新增：收藏记录相关操作方法 ====================
    suspend fun toggleFavorite(animal: Animal): Boolean {
        val isNowFavorite = repository.toggleFavorite(animal)
        // 关键：更新本地状态，触发UI刷新
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
}