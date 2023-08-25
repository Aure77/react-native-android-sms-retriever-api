package com.androidsmsretriever

import android.util.Log
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.WritableNativeMap
import com.facebook.react.modules.core.DeviceEventManagerModule

class JSEventHelper(private val mContext: ReactApplicationContext?) {

  fun emitJSEvent(key: String, message: String?) {
    if (mContext == null) {
      Log.w(TAG, "react application context is null")
      return
    }
    if (!mContext.hasActiveReactInstance()) {
      Log.w(TAG, "no active react instance found in context")
      return
    }
    val map = WritableNativeMap()
    map.putString(key, message)
    mContext.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter::class.java).emit(
      SMS_EVENT, map
    )
  }

  companion object {
    const val SMS_EVENT = "com.androidsmsretriever:SmsEvent"
    private const val TAG = "JSEventHelper"
  }
}
