package com.androidsmsretriever

import android.content.pm.PackageManager.NameNotFoundException
import com.facebook.react.bridge.ActivityEventListener
import com.facebook.react.bridge.Promise
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReactContextBaseJavaModule
import com.facebook.react.bridge.ReactMethod

class AndroidSmsRetrieverModule(reactContext: ReactApplicationContext) :
  ReactContextBaseJavaModule(reactContext) {

  private val mPhoneNumberHelper by lazy {
    PhoneNumberHelper()
  }

  private val mSmsHelper by lazy {
    SMSHelper(reactContext)
  }

  override fun getName(): String {
    return NAME
  }

  override fun getConstants(): MutableMap<String, Any> {
    val constants: MutableMap<String, Any> = HashMap()
    constants["SMS_EVENT"] = SmsBroadcastReceiver.SMS_EVENT
    return constants
  }

  @ReactMethod
  fun startSmsRetriever(promise: Promise?) {
    mSmsHelper.startSmsRetriever(promise)
  }

  @ReactMethod
  fun requestPhoneNumber(promise: Promise?) {
    val context = reactApplicationContext
    val activity = currentActivity
    val eventListener: ActivityEventListener = mPhoneNumberHelper.activityEventListener
    context.addActivityEventListener(eventListener)
    mPhoneNumberHelper.onPhoneNumberResultReceived {
      context.removeActivityEventListener(eventListener)
    }
    mPhoneNumberHelper.requestPhoneNumber(context, activity, promise)
  }

  @ReactMethod
  fun getAppHash(promise: Promise?) {
    try {
      val a = AppSignatureHelper(reactApplicationContext)
      val signature = a.appSignatures
      promise?.resolve(signature[0])
    } catch (e: NameNotFoundException) {
      promise?.reject(e)
    }
  }

  companion object {
    const val NAME = "AndroidSmsRetriever"
  }
}
