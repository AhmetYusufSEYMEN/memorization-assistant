package com.seymen.ezberarkadasim.viewmodel

import android.app.Activity
import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import com.seymen.ezberarkadasim.R
import kotlin.system.exitProcess

class MenuViewModel(application: Application) : AndroidViewModel(application) {
    fun isNetworkAvailable(context: Context): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        var activeNetworkInfo: NetworkInfo? = null
        activeNetworkInfo = cm.activeNetworkInfo
        return activeNetworkInfo != null && activeNetworkInfo.isConnectedOrConnecting
    }
    fun checkNetAndClose(context: Context, activity: Activity){
        if(!isNetworkAvailable(context)){
            Toast.makeText(context, R.string.tst_connct, Toast.LENGTH_LONG).show()
            Handler(Looper.getMainLooper()).postDelayed(
                {
                    activity.finish()
                    exitProcess(0)
                }, 4000)
        }
    }
}