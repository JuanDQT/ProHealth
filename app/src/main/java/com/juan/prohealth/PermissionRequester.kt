package com.juan.prohealth

import android.app.Activity
import com.karumi.dexter.Dexter
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.single.PermissionListener

class PermissionRequester(
    private val activity: Activity,
    private val permission: String
) {
    fun request(continuation: (Boolean) -> Unit) {
        Dexter
            .withActivity(activity)
            .withPermission(permission)
            .withListener(object : PermissionListener {
                override fun onPermissionGranted(response: PermissionGrantedResponse) {
                    continuation(true)
                }

                override fun onPermissionDenied(response: PermissionDeniedResponse) {
                    continuation(false)
                }

                override fun onPermissionRationaleShouldBeShown(
                    permission: PermissionRequest,
                    token: PermissionToken
                ) = token.continuePermissionRequest()
            }
            ).check()
    }
}