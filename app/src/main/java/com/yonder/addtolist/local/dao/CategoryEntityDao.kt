package com.yonder.addtolist.local.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.yonder.addtolist.local.entity.CategoryEntity
import com.yonder.addtolist.local.entity.CategoryWithProducts
import com.yonder.addtolist.local.entity.ProductEntity
import com.yonder.addtolist.local.entity.UserListEntity

/**
 * Yusuf Onder on 12,May,2021
 */
@Dao
interface CategoryEntityDao {

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  fun insertAll(categoryList: List<CategoryEntity>)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insert(category: CategoryEntity)

  @Query("SELECT * FROM category ")
  fun loadAll(): LiveData<List<CategoryEntity>>

  @Query("SELECT * FROM category WHERE language_id  = :languageId")
  fun findByLanguageId(languageId: Int): LiveData<List<CategoryEntity>>

  @Transaction
  @Query("SELECT * FROM category WHERE categoryId  = :categoryId")
  fun findById(categoryId: String): LiveData<List<CategoryWithProducts>>

  @Transaction
  @Query("SELECT * FROM category")
  suspend fun getAllUserListWithProducts(): List<CategoryWithProducts>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertAll(products: List<ProductEntity>)

}