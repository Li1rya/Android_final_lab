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
import com.example.animalwiki.data.model.SearchFilter
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

    // ==================== 新增：筛选状态 ====================
    private val _searchFilter = MutableStateFlow(SearchFilter())
    val searchFilter: StateFlow<SearchFilter> = _searchFilter.asStateFlow()

    // 分类选项
    private val _kingdomOptions = MutableStateFlow<List<String>>(emptyList())
    val kingdomOptions: StateFlow<List<String>> = _kingdomOptions.asStateFlow()

    private val _phylumOptions = MutableStateFlow<List<String>>(emptyList())
    val phylumOptions: StateFlow<List<String>> = _phylumOptions.asStateFlow()

    private val _classOptions = MutableStateFlow<List<String>>(emptyList())
    val classOptions: StateFlow<List<String>> = _classOptions.asStateFlow()

    private val _orderOptions = MutableStateFlow<List<String>>(emptyList())
    val orderOptions: StateFlow<List<String>> = _orderOptions.asStateFlow()

    private val _familyOptions = MutableStateFlow<List<String>>(emptyList())
    val familyOptions: StateFlow<List<String>> = _familyOptions.asStateFlow()

    private val _genusOptions = MutableStateFlow<List<String>>(emptyList())
    val genusOptions: StateFlow<List<String>> = _genusOptions.asStateFlow()

    private val _speciesOptions = MutableStateFlow<List<String>>(emptyList())
    val speciesOptions: StateFlow<List<String>> = _speciesOptions.asStateFlow()

    private val _historyList = MutableStateFlow<List<History>>(emptyList())
    val historyList: StateFlow<List<History>> = _historyList.asStateFlow()
    private val _favoriteList = MutableStateFlow<List<Favorite>>(emptyList())
    val favoriteList: StateFlow<List<Favorite>> = _favoriteList.asStateFlow()
    private val _folderList = MutableStateFlow<List<Folder>>(emptyList())
    val folderList: StateFlow<List<Folder>> = _folderList.asStateFlow()
    private val _currentFolderId = MutableStateFlow(0L)
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
            loadClassificationOptions()
            _isLoading.value = false
        }
        viewModelScope.launch {
            repository.getAllHistory().collect { _historyList.value = it }
        }
        viewModelScope.launch {
            _currentFolderId
                .flatMapLatest { folderId ->
                    repository.getFavoritesByFolder(folderId)
                }
                .collect { favorites ->
                    _favoriteList.value = favorites
                }
        }
        viewModelScope.launch {
            repository.getAllFolders().collect { folders ->
                _folderList.value = folders
                if (folders.isNotEmpty() && _currentFolderId.value == 0L) {
                    _currentFolderId.value = folders.first().id
                }
            }
        }
    }

    // 加载分类选项
    private suspend fun loadClassificationOptions() {
        _kingdomOptions.value = repository.getAllKingdoms()
    }

    // 界选择变化时，加载对应的门
    fun onKingdomSelected(kingdom: String?) {
        _searchFilter.value = _searchFilter.value.copy(
            kingdom = kingdom,
            phylum = null,
            className = null,
            order = null,
            family = null,
            genus = null,
            species = null
        )
        viewModelScope.launch {
            _phylumOptions.value = kingdom?.let { repository.getPhylaByKingdom(it) } ?: emptyList()
            _classOptions.value = emptyList()
            _orderOptions.value = emptyList()
            _familyOptions.value = emptyList()
            _genusOptions.value = emptyList()
            _speciesOptions.value = emptyList()
            executeSearch()
        }
    }

    // 门选择变化时，加载对应的纲
    fun onPhylumSelected(phylum: String?) {
        _searchFilter.value = _searchFilter.value.copy(
            phylum = phylum,
            className = null,
            order = null,
            family = null,
            genus = null,
            species = null
        )
        viewModelScope.launch {
            _classOptions.value = phylum?.let { repository.getClassesByPhylum(it) } ?: emptyList()
            _orderOptions.value = emptyList()
            _familyOptions.value = emptyList()
            _genusOptions.value = emptyList()
            _speciesOptions.value = emptyList()
            executeSearch()
        }
    }

    // 纲选择变化时，加载对应的目
    fun onClassSelected(className: String?) {
        _searchFilter.value = _searchFilter.value.copy(
            className = className,
            order = null,
            family = null,
            genus = null,
            species = null
        )
        viewModelScope.launch {
            _orderOptions.value = className?.let { repository.getOrdersByClass(it) } ?: emptyList()
            _familyOptions.value = emptyList()
            _genusOptions.value = emptyList()
            _speciesOptions.value = emptyList()
            executeSearch()
        }
    }

    // 目选择变化时，加载对应的科
    fun onOrderSelected(order: String?) {
        _searchFilter.value = _searchFilter.value.copy(
            order = order,
            family = null,
            genus = null,
            species = null
        )
        viewModelScope.launch {
            _familyOptions.value = order?.let { repository.getFamiliesByOrder(it) } ?: emptyList()
            _genusOptions.value = emptyList()
            _speciesOptions.value = emptyList()
            executeSearch()
        }
    }

    // 科选择变化时，加载对应的属
    fun onFamilySelected(family: String?) {
        _searchFilter.value = _searchFilter.value.copy(
            family = family,
            genus = null,
            species = null
        )
        viewModelScope.launch {
            _genusOptions.value = family?.let { repository.getGeneraByFamily(it) } ?: emptyList()
            _speciesOptions.value = emptyList()
            executeSearch()
        }
    }

    // 属选择变化时，加载对应的种
    fun onGenusSelected(genus: String?) {
        _searchFilter.value = _searchFilter.value.copy(
            genus = genus,
            species = null
        )
        viewModelScope.launch {
            _speciesOptions.value = genus?.let { repository.getSpeciesByGenus(it) } ?: emptyList()
            executeSearch()
        }
    }

    // 种选择变化
    fun onSpeciesSelected(species: String?) {
        _searchFilter.value = _searchFilter.value.copy(species = species)
        viewModelScope.launch {
            executeSearch()
        }
    }

    // 收藏夹筛选变化
    fun onFavoriteFolderSelected(folderId: Long?) {
        _searchFilter.value = _searchFilter.value.copy(
            favoriteFolderId = folderId,
            onlyFavorites = folderId != null
        )
        viewModelScope.launch {
            executeSearch()
        }
    }

    // 清除所有筛选
    fun clearAllFilters() {
        _searchFilter.value = SearchFilter()
        viewModelScope.launch {
            _phylumOptions.value = emptyList()
            _classOptions.value = emptyList()
            _orderOptions.value = emptyList()
            _familyOptions.value = emptyList()
            _genusOptions.value = emptyList()
            _speciesOptions.value = emptyList()
            executeSearch()
        }
    }

    fun loadAllAnimals() {
        viewModelScope.launch { _animals.value = repository.getAllAnimals() }
    }
    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
        viewModelScope.launch {
            executeSearch()
        }
    }
    fun onSearchModeChange(mode: SearchMode) {
        _searchMode.value = mode
        viewModelScope.launch {
            executeSearch()
        }
    }

    // 执行带筛选的搜索
    private suspend fun executeSearch() {
        if (_searchQuery.value.isBlank() && !_searchFilter.value.hasAnyFilter()) {
            _searchResults.value = emptyList()
            return
        }
        _searchResults.value = repository.searchWithFilter(
            keyword = _searchQuery.value,
            filter = _searchFilter.value
        )
    }
    fun searchAnimals(keyword: String) {
        viewModelScope.launch {
            executeSearch()
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
    fun insertFolder(name: String) {
        viewModelScope.launch {
            repository.insertFolder(name)
        }
    }
    suspend fun deleteFolder(folder: Folder): Boolean {
        if (folder.isDefault) {
            return false
        }
        return repository.deleteFolder(folder)
    }
    fun switchFolder(folderId: Long) {
        _currentFolderId.value = folderId
    }
    fun getFavoritesByFolder(folderId: Long): Flow<List<Favorite>> {
        return repository.getFavoritesByFolder(folderId)
    }
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
