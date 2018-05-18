package com.example.nazenani.blekotlin

import android.app.Activity
import android.content.pm.PackageManager
import android.support.v4.app.ActivityCompat
import android.support.v7.app.AlertDialog
import android.util.Log

// https://qiita.com/kazhida/items/a650e95fb15c540b597a

interface PermissionHelper: ActivityCompat.OnRequestPermissionsResultCallback {
    val message: String?
    val caption: String?
    val REQUEST_CODE: Int
    val PERMISSIONS: Array<String>

    fun requests(activity: Activity) {
        if (PERMISSIONS.isNotEmpty()) {
            for (permission in PERMISSIONS) {
                if (ActivityCompat.checkSelfPermission(activity, permission) != PackageManager.PERMISSION_GRANTED) {
                    // 以前に許諾して、今後表示しないとしていた場合は、ここにはこない
                    if (ActivityCompat.shouldShowRequestPermissionRationale(activity, permission) && message != null) {
                        // ユーザに許諾してもらうために、なんで必要なのかを説明する
                        val builder = AlertDialog.Builder(activity);
                        builder.setMessage(message);
                        builder.setPositiveButton(if (caption == null) "OK" else caption) { dialog, which ->
                            //  許諾要求
                            requestPermission(activity);
                        }
                        builder.show();
                    } else {
                        //  許諾要求
                        requestPermission(activity);
                    }
                } else {
                    // 許諾されているので、やりたいことをやる
                    onAllowed();
                }
            }

        }
    }

    private fun requestPermission(activity: Activity) {
        ActivityCompat.requestPermissions(activity, PERMISSIONS, REQUEST_CODE);
    }

    fun onAllowed() {}
    fun onDenied() {}

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        var isAllow: Boolean = true
        if (requestCode == REQUEST_CODE) {
            for (result in grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    isAllow = false
                    break
                }
            }

            if (isAllow) {
                // 許諾されたので、やりたいことをやる
                onAllowed();
            } else {
                onDenied();
            }
        }
    }
}
