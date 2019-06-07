package com.kho.authorizitionexample.data.model.authen

data class AccessTokenRequest(val id: String, val mapRequest: AccessTokenMapRequest)

class AccessTokenMapRequest(code: String, state: String) : HashMap<String, String>(3) {
    init {
        put("code", code)
        put("state", state)
    }
}
