package com.kho.authorizitionexample.data.model.list

import com.google.gson.annotations.SerializedName

data class SomeListResponse(
        @SerializedName("id") val id: Int,
        @SerializedName("name") val name: String
)