package com.kho.authorizitionexample.viewmodel.data

sealed class StateViewData<out R> {

    data class Success<out T>(val data: T) : StateViewData<T>()
    data class Error(val error: String) : StateViewData<Nothing>()
    object Loading : StateViewData<Nothing>()

}