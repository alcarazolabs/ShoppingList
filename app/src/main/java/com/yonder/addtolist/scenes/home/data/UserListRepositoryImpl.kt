package com.yonder.addtolist.scenes.home.data

import com.yonder.addtolist.core.mapper.ListMapperImpl
import com.yonder.addtolist.core.network.exceptions.ServerResultException
import com.yonder.addtolist.core.network.responses.Result
import com.yonder.addtolist.scenes.home.data.local.datasource.UserListDataSource
import com.yonder.addtolist.local.entity.UserListEntity
import com.yonder.addtolist.scenes.home.data.remote.ApiService
import com.yonder.addtolist.core.network.request.CreateUserListRequest
import com.yonder.addtolist.local.entity.UserListWithProducts
import com.yonder.addtolist.scenes.home.data.remote.response.UserListResponse
import com.yonder.addtolist.scenes.home.domain.mapper.UserListResponseToEntityMapper
import com.yonder.addtolist.scenes.home.domain.mapper.UserListProductMapper
import com.yonder.addtolist.scenes.home.domain.mapper.UserListRequestMapper
import com.yonder.addtolist.scenes.home.domain.repository.UserListRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import timber.log.Timber
import javax.inject.Inject

/**
 * Yusuf Onder on 12,May,2021
 */

class UserListRepositoryImpl @Inject constructor(
  private val apiService: ApiService,
  private val localDataSource: UserListDataSource,
  private val userListRequestToEntityMapper : UserListRequestMapper,
  private val responseToEntityMapper: UserListResponseToEntityMapper
) : UserListRepository {

  override fun createUserList(request: CreateUserListRequest): Flow<Result<UserListEntity>> = flow {
    emit(Result.Loading)
    val response = apiService.createUserList(request)
    val entity = userListRequestToEntityMapper.map(request)
    if (response.success == true) {
      entity.id = response.data?.id
      entity.sync = true
      emit(Result.Success(entity))
    } else {
      emit(Result.Error<UserListEntity>(ServerResultException()))
    }
    localDataSource.insert(entity)
  }.catch { error ->
    localDataSource.insert(userListRequestToEntityMapper.map(request))
    error.printStackTrace()
    emit(Result.Error<UserListEntity>(error))
  }


  private suspend fun insertLists(list: List<UserListResponse>?) {
    localDataSource.insertAll(
      ListMapperImpl(responseToEntityMapper)
        .map(list.orEmpty())
    )
  }



  private suspend fun insertProducts(list: List<UserListResponse>?) {
    list?.forEach { userList ->
      val productEntities =
        ListMapperImpl(
          UserListProductMapper(userList.uuid)
        ).map(userList.userListProducts)
      Timber.d("productEntities => ${productEntities.size}")
      localDataSource.insertProducts(productEntities)
    }
  }

  override fun getUserLists(): Flow<Result<List<UserListWithProducts>>> = flow {
    emit(Result.Loading)
    val localUserLists = localDataSource.getUserListWithProducts()
    if (localUserLists.isNotEmpty()) {
      emit(Result.Success(localUserLists))
    }
    val response = apiService.getUserLists()
    if (response.success == true) {
      if (localUserLists.isEmpty()) {
        val userLists = response.data
        insertLists(userLists)
        insertProducts(userLists)
      }
    } else {
      emit(Result.Error<List<UserListWithProducts>>(ServerResultException()))
    }
    emit(Result.Success(localDataSource.getUserListWithProducts()))

  }.catch { error ->
    error.printStackTrace()
    emit(Result.Error<List<UserListWithProducts>>(error))
    emit(Result.Success(localDataSource.getUserListWithProducts()))
  }


}

