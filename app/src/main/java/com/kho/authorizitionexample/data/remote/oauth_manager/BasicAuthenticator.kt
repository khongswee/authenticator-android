package com.kho.authorizitionexample.data.remote.oauth_manager

import com.kho.authorizitionexample.data.Constants
import com.kho.authorizitionexample.data.exception.AuthorizationErrorException
import com.kho.authorizitionexample.data.model.authen.TokenResponse
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route

class BasicAuthenticator(private val onAuthTokenDelegate: OAuthTokenDelegate) : Authenticator {

    override fun authenticate(route: Route, response: Response): Request? {
        return if (hasBearerAuthorizationToken(response)) {
            val previousRetryCount = retryCount(response)
            reAuthenticateRequestUsingRefreshToken(
                response?.request(),
                previousRetryCount + 1
            )
        } else {
            null
        }
    }

    private fun hasBearerAuthorizationToken(response: Response?): Boolean {
        response?.let { response ->
            val authorizationHeader = response.request().header(Constants.httpHeaderAuthorization)
            return authorizationHeader.startsWith(Constants.httpHeaderBearerTokenPrefix)
        }
        return false
    }

    private fun retryCount(response: Response?): Int {
        return response?.request()?.header(Constants.httpHeaderRetryCount)?.toInt() ?: 0
    }

    @Synchronized
    private fun reAuthenticateRequestUsingRefreshToken(staleRequest: Request?, retryCount: Int): Request? {

        if (retryCount > Constants.OAUTH_RE_AUTH_RETRY_LIMIT) {
            throw AuthorizationErrorException("Retry count exceeded")
        }

        onAuthTokenDelegate.onRefreshToken()?.let { newBearerToken ->
            return rewriteRequest(staleRequest, retryCount, newBearerToken)
        }

        return null
    }

    private fun rewriteRequest(
        staleRequest: Request?, retryCount: Int, token: String
    ): Request? {
        return staleRequest?.newBuilder()
            ?.header(
                Constants.httpHeaderAuthorization,
                token
            )
            ?.header(
                Constants.httpHeaderRetryCount,
                "$retryCount"
            )
            ?.build()
    }

}