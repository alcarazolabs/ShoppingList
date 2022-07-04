package com.yonder.addtolist.di

import android.content.Context
import com.facebook.CallbackManager
import com.yonder.addtolist.common.utils.auth.FacebookUserProvider
import com.yonder.addtolist.common.utils.auth.GoogleUserProvider
import com.yonder.addtolist.common.utils.auth.GuestUserProvider
import com.yonder.addtolist.common.utils.auth.UserProvider
import com.yonder.core.base.mapper.Mapper
import com.yonder.addtolist.core.network.LoginService
import com.yonder.addtolist.core.network.responses.BaseResponse
import com.yonder.addtolist.core.network.responses.UserResponse
import com.yonder.addtolist.core.network.thread.CoroutineThread
import com.yonder.addtolist.data.local.UserPreferenceDataStore
import com.yonder.addtolist.domain.mapper.LoginMapper
import com.yonder.addtolist.domain.uimodel.UserUiModel
import com.yonder.addtolist.data.repository.LoginRepository
import com.yonder.addtolist.data.repository.LoginRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import retrofit2.Retrofit

/**
 * Yusuf Onder on 09,May,2021
 */

@[Module InstallIn(ViewModelComponent::class)]
interface LoginModule {

  @get:[Binds]
  val LoginMapper.loginMapper: Mapper<BaseResponse<UserResponse>, UserUiModel>

  @get:[Binds]
  val LoginRepositoryImpl.loginRepository: LoginRepository

  companion object {
    @[Provides]
    fun provideFacebookUserProvider(@ApplicationContext context: Context): UserProvider {
      return FacebookUserProvider(context)
    }

    @[Provides]
    fun provideGoogleUserProvider(@ApplicationContext context: Context): UserProvider {
      return GoogleUserProvider(context)
    }


    @[Provides]
    fun provideGuestUserProvider(@ApplicationContext context: Context): UserProvider {
      return GuestUserProvider(context)
    }

    @[Provides]
    fun provideFacebookCallbackManager(): CallbackManager {
      return CallbackManager.Factory.create()
    }

    @[Provides]
    fun provideLoginService(retrofit: Retrofit): LoginService {
      return retrofit.create(LoginService::class.java)
    }
  }
}
