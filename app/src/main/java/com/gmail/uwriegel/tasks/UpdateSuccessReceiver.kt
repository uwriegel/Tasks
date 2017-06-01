package com.gmail.uwriegel.tasks

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.ResultReceiver

/**
 * Created by urieg on 01.06.2017.
 */
class UpdateSuccessReceiver(var context: Context, handler: Handler)
    : ResultReceiver(handler) {

    override fun onReceiveResult(resultCode: Int, resultData: Bundle?) {
        val mainActivity = context as MainActivity
        mainActivity.notifyDataSetChanged()
    }
}