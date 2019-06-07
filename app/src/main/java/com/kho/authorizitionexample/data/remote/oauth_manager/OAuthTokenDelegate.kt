package com.kho.authorizitionexample.data.remote.oauth_manager

import com.kho.authorizitionexample.data.exception.AuthorizationErrorException
import com.kho.authorizitionexample.data.local.TokenManager
import com.kho.authorizitionexample.data.model.authen.TokenResponse
import com.kho.authorizitionexample.data.remote.service.AuthenticatorService
import org.json.JSONObject

interface OAuthTokenDelegate {
    fun onRefreshToken(): String?
    fun setOAuth(service: AuthenticatorService)
}

class OAuthTokenDelegateImpl(
    private val tokenManagerImpl: TokenManager
) : OAuthTokenDelegate {
    override fun setOAuth(service: AuthenticatorService) {
        this.service = service
    }

    var service: AuthenticatorService? = null

    override fun onRefreshToken(): String? {
        val oAuthToken = tokenManagerImpl.get() ?: TokenResponse()
        service?.let {
            val response = it.refreshToken(oAuthToken.refreshToken).execute()
            if (response.isSuccessful) {
                response.body()?.let {
                    saveToken(it)
                    return """${it.tokenType} ${it.accessToken}"""
                }
            } else {
                val jsonObject = JSONObject(response.errorBody()?.string())
                throw AuthorizationErrorException(jsonObject.getString("error"))
            }
        } ?: run {
            return null
        }
    }

    private fun saveToken(token: TokenResponse) {
        tokenManagerImpl.save(token)
    }
}
