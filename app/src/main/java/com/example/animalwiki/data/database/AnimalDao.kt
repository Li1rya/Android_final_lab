package com.example.animalwiki.data.database
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.animalwiki.data.database.entity.AnimalEntity
@Dao
interface AnimalDao {
    @Query("SELECT * FROM animals WHERE cnname LIKE '%' || :keyword || '%' LIMIT 1")
    suspend fun getAnimalByName(keyword: String): AnimalEntity?
    @Query("SELECT * FROM animals WHERE id = :id")
    suspend fun getAnimalById(id: String): AnimalEntity?
    @Query("SELECT * FROM animals ORDER BY cnname ASC")
    suspend fun getAllAnimals(): List<AnimalEntity>
    @Query("""
        SELECT * FROM animals 
        WHERE cnname LIKE '%' || :keyword || '%' 
        OR latinName LIKE '%' || :keyword || '%'
        OR appearance LIKE '%' || :keyword || '%'
        OR habitat LIKE '%' || :keyword || '%'
        OR diet LIKE '%' || :keyword || '%'
    """)
    suspend fun searchAnimals(keyword: String): List<AnimalEntity>
    @Query("SELECT * FROM animals WHERE cnname LIKE '%' || :keyword || '%'")
    suspend fun searchByCnName(keyword: String): List<AnimalEntity>
    @Query("SELECT * FROM animals WHERE latinName LIKE '%' || :keyword || '%'")
    suspend fun searchByLatinName(keyword: String): List<AnimalEntity>
    @Query("""
        SELECT * FROM animals 
        WHERE className LIKE '%' || :keyword || '%' 
        OR `order` LIKE '%' || :keyword || '%' 
        OR family LIKE '%' || :keyword || '%'
    """)
    suspend fun searchByClassification(keyword: String): List<AnimalEntity>

    // ==================== 分类筛选查询（界门纲目科属种）====================
    @Query("SELECT DISTINCT kingdom FROM animals WHERE kingdom IS NOT NULL AND kingdom != '' ORDER BY kingdom")
    suspend fun getAllKingdoms(): List<String>

    @Query("SELECT DISTINCT phylum FROM animals WHERE kingdom = :kingdom AND phylum IS NOT NULL AND phylum != '' ORDER BY phylum")
    suspend fun getPhylaByKingdom(kingdom: String): List<String>

    @Query("SELECT DISTINCT className FROM animals WHERE phylum = :phylum AND className IS NOT NULL AND className != '' ORDER BY className")
    suspend fun getClassesByPhylum(phylum: String): List<String>

    @Query("SELECT DISTINCT `order` FROM animals WHERE className = :className AND `order` IS NOT NULL AND `order` != '' ORDER BY `order`")
    suspend fun getOrdersByClass(className: String): List<String>

    @Query("SELECT DISTINCT family FROM animals WHERE `order` = :order AND family IS NOT NULL AND family != '' ORDER BY family")
    suspend fun getFamiliesByOrder(order: String): List<String>

    @Query("SELECT DISTINCT genus FROM animals WHERE family = :family AND genus IS NOT NULL AND genus != '' ORDER BY genus")
    suspend fun getGeneraByFamily(family: String): List<String>

    @Query("SELECT DISTINCT species FROM animals WHERE genus = :genus AND species IS NOT NULL AND species != '' ORDER BY species")
    suspend fun getSpeciesByGenus(genus: String): List<String>

    // 带完整分类筛选的搜索（精确匹配）
    @Query("""
        SELECT * FROM animals 
        WHERE (:keyword IS NULL OR :keyword = '' OR cnname LIKE '%' || :keyword || '%' OR latinName LIKE '%' || :keyword || '%')
        AND (:kingdom IS NULL OR kingdom = :kingdom)
        AND (:phylum IS NULL OR phylum = :phylum)
        AND (:className IS NULL OR className = :className)
        AND (:order IS NULL OR `order` = :order)
        AND (:family IS NULL OR family = :family)
        AND (:genus IS NULL OR genus = :genus)
        AND (:species IS NULL OR species = :species)
        ORDER BY cnname ASC
    """)
    suspend fun searchWithClassificationFilter(
        keyword: String?,
        kingdom: String?,
        phylum: String?,
        className: String?,
        order: String?,
        family: String?,
        genus: String?,
        species: String?
    ): List<AnimalEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAnimal(animal: AnimalEntity)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(animals: List<AnimalEntity>)
    @Query("DELETE FROM animals WHERE id = :id")
    suspend fun deleteAnimal(id: String)
    @Query("DELETE FROM animals")
    suspend fun deleteAllAnimals()
    @Query("SELECT COUNT(*) FROM animals")
    suspend fun getCount(): Int
}
