package com.kho.authorizitionexample.data.remote

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.kho.authorizitionexample.data.Constants
import com.kho.authorizitionexample.data.local.TokenManager
import com.kho.authorizitionexample.data.remote.oauth_manager.BasicAuthInterceptor
import com.kho.authorizitionexample.data.remote.oauth_manager.BasicAuthenticator
import com.kho.authorizitionexample.data.remote.oauth_manager.OAuthTokenDelegate
import com.kho.authorizitionexample.data.remote.oauth_manager.OAuthTokenDelegateImpl
import com.kho.authorizitionexample.data.remote.service.AuthenticatorService
import com.kho.authorizitionexample.data.remote.service.AppService
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

class RemoteServiceFactory(
    private val tokenManager: TokenManager
    , private val authenticator: BasicAuthenticator
    , private val authInterceptor: BasicAuthInterceptor
    , private val oAuth: OAuthTokenDelegate
    , val baseUrl: String
) {

    fun createAppService(): AppService {
        val client = OkHttpClient.Builder()
            .addNetworkInterceptor(createIntentcepLogging())
            .authenticator(authenticator)
            .addInterceptor(createHeaderInterceptorToken(tokenManager))
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()

        val retrofit = createBaseRetrofitBuilder(client)
        val service = retrofit.build().create(AppService::class.java)
        oAuth.setOAuth(createOAuthService())
        return service
    }

    fun createOAuthService(): AuthenticatorService {

        val client = OkHttpClient.Builder()
            .addNetworkInterceptor(createIntentcepLogging())
            .addInterceptor(authInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()

        val retrofit = createBaseRetrofitBuilder(client)

        return retrofit.build().create(AuthenticatorService::class.java)
    }

    private fun createBaseRetrofitBuilder(okHttpClient: OkHttpClient): Retrofit.Builder {
        val gson: Gson = GsonBuilder().create()
        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())

    }

    private fun createIntentcepLogging(): Interceptor {
        return HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
    }

    private fun createHeaderInterceptorToken(tokenManager: TokenManager) = Interceptor {
        val request = it.request()
        val tokenResponse = tokenManager.get()
        val newRequest = request.newBuilder()
            .header(
                Constants.httpHeaderAuthorization,
                """${tokenResponse?.tokenType} ${tokenResponse?.accessToken}"""
            )
            .build()

        it.proceed(newRequest)
    }
}