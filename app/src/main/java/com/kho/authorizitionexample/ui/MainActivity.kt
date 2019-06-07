package com.kho.authorizitionexample.ui

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.kho.authorizitionexample.R
import com.kho.authorizitionexample.viewmodel.MainViewModel
import com.kho.authorizitionexample.viewmodel.data.StateViewData
import org.koin.android.viewmodel.ext.android.viewModel


class MainActivity : AppCompatActivity() {


    private val mainViewModel: MainViewModel by viewModel()

    val code = "xxx"
    val state = "xxx"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mainViewModel.getList("xxx", code, state)

        mainViewModel.listData.observe(this, Observer {
            when (it) {
                StateViewData.Loading -> {

                }
                is StateViewData.Success -> {
                    Log.d("Data", it.data.toString())
                }
                is StateViewData.Error -> {
                    Log.e("Error", it.error)
                }
            }
        })

    }


}
