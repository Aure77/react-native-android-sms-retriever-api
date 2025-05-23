package com.androidsmsretriever

import android.content.pm.PackageManager.NameNotFoundException
import com.facebook.react.bridge.ActivityEventListener
import com.facebook.react.bridge.Promise
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.WritableNativeMap
import com.facebook.react.module.annotations.ReactModule

@ReactModule(name = AndroidSmsRetrieverModule.NAME)
class AndroidSmsRetrieverModule(reactContext: ReactApplicationContext) :
  NativeAndroidSmsRetrieverSpec(reactContext) {

  private val mPhoneNumberHelper by lazy {
    PhoneNumberHelper()
  }

  private val mSmsHelper by lazy {
    SMSHelper(reactContext)
  }

  override fun getName(): String {
    return NAME
  }

  override fun getTypedExportedConstants(): MutableMap<String, Any> = hashMapOf("SMS_EVENT" to JSEventHelper.SMS_EVENT)

  override fun startSmsRetriever(promise: Promise?) {
    mSmsHelper.startSmsRetriever(promise)
  }

  override fun startSmsUserConsent(promise: Promise?) {
    mSmsHelper.startSmsUserConsent(promise)
  }

  override fun requestPhoneNumber(promise: Promise?) {
    val context = reactApplicationContext
    val activity = currentActivity
    val eventListener: ActivityEventListener = mPhoneNumberHelper.activityEventListener
    context.addActivityEventListener(eventListener)
    mPhoneNumberHelper.onPhoneNumberResultReceived {
      context.removeActivityEventListener(eventListener)
    }
    mPhoneNumberHelper.requestPhoneNumber(context, activity, promise)
  }

  override fun getAppHash(promise: Promise?) {
    try {
      val a = AppSignatureHelper(reactApplicationContext)
      val signature = a.appSignatures
      promise?.resolve(signature[0])
    } catch (e: NameNotFoundException) {
      promise?.reject(e)
    }
  }

  override fun getSmsRetrieverCompatibility(promise: Promise?) {
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
