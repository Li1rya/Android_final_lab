package com.example.animalwiki.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.animalwiki.data.database.AnimalDatabase
import com.example.animalwiki.data.model.Animal
import com.example.animalwiki.data.repository.AnimalRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AnimalViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: AnimalRepository

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

    init {
        val dao = AnimalDatabase.getDatabase(application).animalDao()
        repository = AnimalRepository(application, dao)

        viewModelScope.launch {
            _isLoading.value = true
            repository.initializeDatabase()
            loadAllAnimals()
            _isLoading.value = false
        }
    }

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

    /** 获取图片资源 ID 列表 */
    fun getAnimalImages(animal: Animal): List<Int> {
        return repository.getImageResIds(getApplication(), animal.latinName, maxImages = 5)
    }

    /** 获取单张图片资源 ID */
    fun getAnimalImage(animal: Animal, index: Int = 1): Int {
        return repository.getImageResId(getApplication(), animal.latinName, index)
    }
}