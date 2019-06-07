package com.kho.authorizitionexample.data.repository.list

import com.kho.authorizitionexample.data.model.list.SomeListResponse
import com.kho.authorizitionexample.data.remote.service.AppService
import io.reactivex.Single

interface ListRespository{
    fun getList(typeId: String):Single<List<SomeListResponse>>
}
class ListRepositoryImpl(private val service:AppService):ListRespository{
    override fun getList(typeId: String): Single<List<SomeListResponse>> = service.getSmsType(typeId)
}