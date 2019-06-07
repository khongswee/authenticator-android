package com.kho.authorizitionexample.data.remote.service

import com.kho.authorizitionexample.data.model.authen.AccessTokenMapRequest
import com.kho.authorizitionexample.data.model.authen.TokenResponse
import io.reactivex.Single
import retrofit2.Call
import retrofit2.http.*

interface AuthenticatorService {

    @GET("uaa/api/social/signin/{providerId}")
    fun getAccessToken(
        @Path("providerId") providerId: String
        , @QueryMap request: AccessTokenMapRequest
    ): Single<TokenResponse>

    @FormUrlEncoded
    @POST("uaa/oauth/token")
    fun refreshToken(@Field("refresh_token") refreshToken: String, @Field("grant_type") type: String = "refresh_token"): Call<TokenResponse>
}