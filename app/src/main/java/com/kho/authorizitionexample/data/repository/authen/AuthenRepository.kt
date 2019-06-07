package com.kho.authorizitionexample.data.repository.authen

import com.kho.authorizitionexample.data.model.authen.AccessTokenRequest
import com.kho.authorizitionexample.data.model.authen.TokenResponse
import com.kho.authorizitionexample.data.remote.service.AuthenticatorService
import io.reactivex.Single


interface AuthenRepository{
    fun login(request: AccessTokenRequest):Single<TokenResponse>
}

class AuthenRepositoryImpl(private val api:AuthenticatorService):AuthenRepository{
    override fun login(request: AccessTokenRequest): Single<TokenResponse> = api.getAccessToken(request.id,request.mapRequest)
}