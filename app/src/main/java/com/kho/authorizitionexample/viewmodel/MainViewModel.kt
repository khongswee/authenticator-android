package com.kho.authorizitionexample.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.kho.authorizitionexample.data.exception.AuthorizationErrorException
import com.kho.authorizitionexample.data.model.authen.AccessTokenMapRequest
import com.kho.authorizitionexample.data.model.authen.AccessTokenRequest
import com.kho.authorizitionexample.data.model.list.SomeListResponse
import com.kho.authorizitionexample.domain.data.GetListUseCaseData
import com.kho.authorizitionexample.domain.usecase.list.GetListUseCase
import com.kho.authorizitionexample.viewmodel.data.StateViewData
import io.reactivex.observers.DisposableSingleObserver

class MainViewModel(private val getListUseCase: GetListUseCase):ViewModel() {

    val listData = MutableLiveData<StateViewData<List<SomeListResponse>>>()

    fun getList(id: String, code: String, state: String) {
        listData.value = StateViewData.Loading
        getListUseCase.execute(
            object : DisposableSingleObserver<List<SomeListResponse>>() {
                override fun onSuccess(t: List<SomeListResponse>) {
                    listData.value = StateViewData.Success(t)

                }

                override fun onError(e: Throwable) {
                    if (e is AuthorizationErrorException){
                        listData.value = StateViewData.Error(e.messageError)
                    }else{
                        listData.value = StateViewData.Error(e.message ?: "Not found message.")
                    }
                }
            },
            GetListUseCaseData(AccessTokenRequest(id, AccessTokenMapRequest(code, state)), id)
        )
    }

}