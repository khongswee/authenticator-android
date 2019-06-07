package com.kho.authorizitionexample.data.exception

import java.io.IOException

class AuthorizationErrorException(val messageError: String) : IOException(){
    override val message: String?
        get() = messageError
}