package com.example.nazenani.blekotlin

import android.app.Activity
import android.content.pm.PackageManager
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AlertDialog
import android.util.Log

// https://qiita.com/kazhida/items/a650e95fb15c540b597a

interface PermissionHelper: ActivityCompat.OnRequestPermissionsResultCallback {
    val message: String?
    val caption: String?
    val REQUEST_CODE: Int
    //val PERMISSION: Array<String>
    val PERMISSION: String

    fun execute(activity: Activity) {
        Log.d("TEST", PERMISSION)
        if (ContextCompat.checkSelfPermission(activity, PERMISSION) != PackageManager.PERMISSION_GRANTED) {
            // 以前に許諾して、今後表示しないとしていた場合は、ここにはこない
            if (ActivityCompat.shouldShowRequestPermissionRationale(activity, PERMISSION) && message != null) {
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

    private fun requestPermission(activity: Activity) {
        val permissions = arrayOf(PERMISSION);
        ActivityCompat.requestPermissions(activity, permissions, REQUEST_CODE);
    }

    fun onAllowed() {}
    fun onDenied() {}

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        if (requestCode == REQUEST_CODE) {
            if (permissions.size > 0 && permissions[0] == PERMISSION && grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // 許諾されたので、やりたいことをやる
                onAllowed();
            } else {
                onDenied();
            }
        }
    }
}
