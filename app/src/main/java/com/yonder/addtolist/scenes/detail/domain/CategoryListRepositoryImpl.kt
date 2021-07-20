package com.yonder.addtolist.scenes.detail.domain

import com.yonder.addtolist.core.mapper.ListMapperImpl
import com.yonder.addtolist.core.mapper.Mapper
import com.yonder.addtolist.core.network.exceptions.RoomResultException
import com.yonder.addtolist.core.network.exceptions.ServerResultException
import com.yonder.addtolist.core.network.responses.Result
import com.yonder.addtolist.data.local.UserPreferenceDataStore
import com.yonder.addtolist.local.entity.CategoryEntity
import com.yonder.addtolist.local.entity.CategoryWithProducts
import com.yonder.addtolist.local.entity.ProductEntity
import com.yonder.addtolist.local.entity.ProductEntitySummary
import com.yonder.addtolist.scenes.list.data.local.datasource.CategoryDataSource
import com.yonder.addtolist.scenes.list.data.remote.ShoppingListApiService
import com.yonder.addtolist.scenes.list.domain.mapper.CategoryProductsMapper
import com.yonder.addtolist.scenes.list.domain.model.ProductUiModel
import com.yonder.addtolist.scenes.list.domain.model.TranslationUiModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

/**
 * @author yusuf.onder
 * Created on 19.07.2021
 */
class CategoryListRepositoryImpl @Inject constructor(
  private val apiService: ShoppingListApiService,
  private val categoryDataSource: CategoryDataSource,
  private val userPreferenceDataStore: UserPreferenceDataStore,
  private val mapper: CategoryProductsMapper
) : CategoryListRepository {

  override fun fetchWord(query: String): Flow<Result<List<ProductEntitySummary>>> = flow {
    emit(Result.Loading)
    emit(Result.Success(categoryDataSource.getProductsByQuery(query)))
  }.catch { e ->
    e.printStackTrace()
    emit(Result.Error(RoomResultException()))
  }

  override fun fetchCategories(): Flow<Result<List<CategoryWithProducts>>> = flow {
    if (!userPreferenceDataStore.isFetchedCategories()) {
      emit(Result.Loading)
      val result = apiService.getCategories(null)
      val entities = mapper.map(result)
      entities.list.forEach { category ->
        val categories = ListMapperImpl(
          CategoryEntityMapper(
            categoryImage = category.image
          )
        ).map(category.translationResponses)
        val products = ListMapperImpl(
          ProductEntityMapper(
            categoryImage = category.image,
            categoryId = "${category.id}"
          )
        ).map(category.products)

        categoryDataSource.insertAll(categories)
        categoryDataSource.insertAllProducts(products)
        userPreferenceDataStore.setFetchedCategories()
      }
    }
    emit(Result.Success(categoryDataSource.getCategories()))
  }.catch { e ->
    e.printStackTrace()
    emit(Result.Error(ServerResultException()))
  }
}

class CategoryEntityMapper(
  private val categoryImage: String
) : Mapper<TranslationUiModel, CategoryEntity> {
  override fun map(input: TranslationUiModel): CategoryEntity {
    return CategoryEntity("${input.categoryId}", input.name, categoryImage, input.languageId)
  }
}


class ProductEntityMapper(
  private val categoryId: String,
  private val categoryImage: String
) : Mapper<ProductUiModel, ProductEntity> {
  override fun map(input: ProductUiModel): ProductEntity {
    return ProductEntity(
      input.id,
      categoryId,
      input.name,
      input.isPopular,
      input.languageId,
      categoryImage
    )
  }
}