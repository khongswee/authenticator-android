package com.kho.authorizitionexample

import android.app.Application
import com.kho.authorizitionexample.data.local.SharePreferenceImpl
import com.kho.authorizitionexample.data.local.SharedPreference
import com.kho.authorizitionexample.data.local.TokenManager
import com.kho.authorizitionexample.data.local.TokenManagerImpl
import com.kho.authorizitionexample.data.remote.RemoteServiceFactory
import com.kho.authorizitionexample.data.remote.oauth_manager.BasicAuthInterceptor
import com.kho.authorizitionexample.data.remote.oauth_manager.BasicAuthenticator
import com.kho.authorizitionexample.data.remote.oauth_manager.OAuthTokenDelegate
import com.kho.authorizitionexample.data.remote.oauth_manager.OAuthTokenDelegateImpl
import com.kho.authorizitionexample.data.repository.authen.AuthenRepository
import com.kho.authorizitionexample.data.repository.authen.AuthenRepositoryImpl
import com.kho.authorizitionexample.data.repository.list.ListRepositoryImpl
import com.kho.authorizitionexample.data.repository.list.ListRespository
import com.kho.authorizitionexample.data.thread.JobExecutor
import com.kho.authorizitionexample.data.thread.ThreadExecutor
import com.kho.authorizitionexample.domain.thread.PostExecutionThread
import com.kho.authorizitionexample.domain.thread.UiThread
import com.kho.authorizitionexample.domain.usecase.list.GetListUseCase
import com.kho.authorizitionexample.viewmodel.MainViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.android.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.dsl.module

class MainApplication : Application() {

    private val remoteModule = module {

        factory { RemoteServiceFactory(get(), get(), get(), get(), baseUrl).createAppService() }
        factory { RemoteServiceFactory(get(), get(), get(), get(), baseUrl).createOAuthService() }
    }

    private val authenticator = module {
        single { BasicAuthenticator(get()) }
    }
    private val authInterceptor = module {
        single { BasicAuthInterceptor("xxx", "xxx") }
    }
    private val oAuth = module {
        single<OAuthTokenDelegate> { OAuthTokenDelegateImpl(get()) }
    }

    private val baseUrl = "/"

    private val dataModule = module {
        single<SharedPreference> { SharePreferenceImpl(androidContext()) }
        single<TokenManager> { TokenManagerImpl(get()) }
    }

    private val viewModelModule = module {
        viewModel { MainViewModel(get()) }
    }

    private val usecaseModule = module {
        single { GetListUseCase(get(), get(), get(), get(),get()) }
        single<PostExecutionThread> { UiThread() }
    }

    private val repositoryModule = module {
        single<AuthenRepository> { AuthenRepositoryImpl(get()) }
        single<ListRespository> { ListRepositoryImpl(get()) }
        single<ThreadExecutor> { JobExecutor() }
    }


    private fun setUpDI() {
        startKoin {
            androidContext(this@MainApplication)
            modules(remoteModule, dataModule, repositoryModule
                , usecaseModule, viewModelModule
            ,authInterceptor,authenticator,oAuth)
        }
    }

    override fun onCreate() {
        super.onCreate()
        setUpDI()
    }

}