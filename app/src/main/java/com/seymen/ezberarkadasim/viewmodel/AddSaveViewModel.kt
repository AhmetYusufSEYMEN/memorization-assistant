package com.seymen.ezberarkadasim.viewmodel

import android.Manifest
import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import com.seymen.ezberarkadasim.R
import kotlin.system.exitProcess

class AddSaveViewModel(application: Application) : AndroidViewModel(application) {

    fun checkPermission(activity: Activity, context: Context) {
        if (ContextCompat.checkSelfPermission(context,Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            val permissions = arrayOf(Manifest.permission.RECORD_AUDIO, Manifest.permission.INTERNET)
            ActivityCompat.requestPermissions(activity, permissions,0)
        }
    }
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