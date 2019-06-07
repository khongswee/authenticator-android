package com.kho.authorizitionexample.data.local

import android.content.Context
import com.orhanobut.hawk.Hawk

interface SharedPreference {
    fun remove(key: String)
    fun removeAll()
    fun contains(key: String): Boolean
    fun <T> put(key: String, value: T): Boolean
    fun <T> get(key: String): T
}
class SharePreferenceImpl (context: Context) : SharedPreference {

    init {
        Hawk.init(context).build()
    }

    override fun <T> put(key: String, value: T): Boolean = Hawk.put(key, value)

    override fun <T> get(key: String): T = Hawk.get<T>(key)

    override fun contains(key: String): Boolean {
        return Hawk.contains(key)
    }

    override fun remove(key: String) {
        Hawk.delete(key)
    }

    override fun removeAll() {
        removeAll()
    }
}