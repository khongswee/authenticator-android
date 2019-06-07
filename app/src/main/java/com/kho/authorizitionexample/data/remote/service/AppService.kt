package com.kho.authorizitionexample.data.remote.service

import com.kho.authorizitionexample.data.model.list.SomeListResponse
import io.reactivex.Single
import retrofit2.http.*

interface AppService {

    companion object {
        private const val PATH_SMS = "/notification/api/types"
    }

    @GET("$PATH_SMS")
    fun getSmsType(@Query("name.equals") typeId: String): Single<List<SomeListResponse>>


}