package com.kho.authorizitionexample.data.local

import com.kho.authorizitionexample.data.model.authen.TokenResponse

interface TokenManager {

    fun save(info: TokenResponse)
    fun get(): TokenResponse?
    fun delete()

}

class TokenManagerImpl (private val prefs: SharedPreference) : TokenManager {
    override fun save(info: TokenResponse) {
        prefs.put(TOKEN_TRUE_ID_PREFS_KEY, info)
    }

    override fun get(): TokenResponse? {
        return prefs.get<TokenResponse>(TOKEN_TRUE_ID_PREFS_KEY)
    }

    override fun delete() {
        prefs.remove(TOKEN_TRUE_ID_PREFS_KEY)
    }

    companion object {
        private const val TOKEN_TRUE_ID_PREFS_KEY = "PREFS_TOKEN_TRUE_ID"
    }

}