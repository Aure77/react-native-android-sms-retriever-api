package com.androidsmsretriever

import android.content.pm.PackageManager.NameNotFoundException
import com.facebook.react.bridge.ActivityEventListener
import com.facebook.react.bridge.Promise
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReactContextBaseJavaModule
import com.facebook.react.bridge.ReactMethod
import com.facebook.react.bridge.WritableNativeMap

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
    constants["SMS_EVENT"] = JSEventHelper.SMS_EVENT
    return constants
  }

  @ReactMethod
  fun startSmsRetriever(promise: Promise?) {
    mSmsHelper.startSmsRetriever(promise)
  }

  @ReactMethod
  fun startSmsUserConsent(promise: Promise?) {
    mSmsHelper.startSmsUserConsent(promise)
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

  @ReactMethod
  fun getSmsRetrieverCompatibility(promise: Promise?) {
    val available = GooglePlayServicesHelper.isAvailable(reactApplicationContext)
    val hasSupportedVersion = GooglePlayServicesHelper.hasSupportedVersion(reactApplicationContext)
    val map = WritableNativeMap()
    map.putBoolean("isGooglePlayServicesAvailable", available)
    map.putBoolean("hasGooglePlayServicesSupportedVersion", hasSupportedVersion)
    promise?.resolve(map)
  }

  companion object {
    const val NAME = "AndroidSmsRetriever"
  }
}
