package com.juan.prohealth

import android.os.Build
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.common.Priority
import com.androidnetworking.interfaces.JSONArrayRequestListener
import com.androidnetworking.interfaces.JSONObjectRequestListener
import com.androidnetworking.interfaces.StringRequestListener
import com.juan.prohealth.security.UniqueDeviceID
import java.net.URL
import java.util.*

class SyncData {

    companion object {

        val URL_BASE = "http://coagutest.es/api/"
//        val URL_BASE = "http://192.168.1.116/CoagutestWeb/api/"
        val URL_LOGIN = "registerUser.php"

        fun validateDevice(event: JSONObjectRequestListener) {
            AndroidNetworking.get(URL_BASE + URL_LOGIN)
                    .addQueryParameter("imei", AppContext.getIMEI())
                    .addQueryParameter("id_generated", UniqueDeviceID.getUniqueId())
                    .addQueryParameter("modelo", Build.MODEL)
                    .addQueryParameter("os", "Android")
                    .addQueryParameter("osversion", Build.VERSION.RELEASE)
                    .addQueryParameter("location", Locale.getDefault().language)
                    .setPriority(Priority.HIGH)
                    .build()
                    .getAsJSONObject(event)
        }

/*
        fun requestMedia(event: JSONArrayRequestListener) {
            AndroidNetworking.get(URL_BASE + URL_MEDIA_GETALL)
//                    .addQueryParameter("all", currentPath)
                    .setPriority(Priority.HIGH)
                    .build()
                    .getAsJSONArray(event)
        }
*/


    }

}