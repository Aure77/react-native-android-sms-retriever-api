package com.androidsmsretriever

import android.content.BroadcastReceiver
import android.content.IntentFilter
import android.util.Log
import com.facebook.react.bridge.Promise
import com.facebook.react.bridge.ReactApplicationContext
import com.google.android.gms.auth.api.phone.SmsRetriever


class SMSHelper(private val mContext: ReactApplicationContext) {
  private var smsVerificationReceiver: BroadcastReceiver? = null

  //region - Package Access
  fun startSmsRetriever(promise: Promise?) {
    if (!GooglePlayServicesHelper.isAvailable(mContext)) {
      promise!!.reject(
        GooglePlayServicesHelper.UNAVAILABLE_ERROR_TYPE,
        GooglePlayServicesHelper.UNAVAILABLE_ERROR_MESSAGE
      )
      return
    }
    if (!GooglePlayServicesHelper.hasSupportedVersion(mContext)) {
      promise!!.reject(
        GooglePlayServicesHelper.UNSUPORTED_VERSION_ERROR_TYPE,
        GooglePlayServicesHelper.UNSUPORTED_VERSION_ERROR_MESSAGE
      )
      return
    }
    val client = SmsRetriever.getClient(mContext)
    val task = client.startSmsRetriever()
    task.addOnSuccessListener {
      val registered = tryToRegisterReceiver()
      promise!!.resolve(registered)
    }
    task.addOnFailureListener {
      unregisterReceiverIfNeeded()
      promise!!.reject(TASK_FAILURE_ERROR_TYPE, TASK_FAILURE_ERROR_MESSAGE)
    }
  }


  fun startSmsUserConsent(promise: Promise?) {
    if (!GooglePlayServicesHelper.isAvailable(mContext)) {
      promise!!.reject(
        GooglePlayServicesHelper.UNAVAILABLE_ERROR_TYPE,
        GooglePlayServicesHelper.UNAVAILABLE_ERROR_MESSAGE
      )
      return
    }
    if (!GooglePlayServicesHelper.hasSupportedVersion(mContext)) {
      promise!!.reject(
        GooglePlayServicesHelper.UNSUPORTED_VERSION_ERROR_TYPE,
        GooglePlayServicesHelper.UNSUPORTED_VERSION_ERROR_MESSAGE
      )
      return
    }
    val client = SmsRetriever.getClient(mContext)
    val task = client.startSmsUserConsent(null);
    task.addOnSuccessListener {
      val registered = tryToRegisterReceiver()
      promise!!.resolve(registered)
    }
    task.addOnFailureListener {
      unregisterReceiverIfNeeded()
      promise!!.reject(TASK_FAILURE_ERROR_TYPE, TASK_FAILURE_ERROR_MESSAGE)
    }
  }
  //endregion

  // region - Privates
  private fun tryToRegisterReceiver(): Boolean {
    smsVerificationReceiver = SmsBroadcastReceiver(mContext)
    val intentFilter = IntentFilter(SmsRetriever.SMS_RETRIEVED_ACTION)
    return try {
      mContext.registerReceiver(smsVerificationReceiver, intentFilter, SmsRetriever.SEND_PERMISSION, null)
      true
    } catch (e: Exception) {
      Log.w(TAG, e)
      false
    }
  }

  private fun unregisterReceiverIfNeeded() {
    if (smsVerificationReceiver == null) {
      return
    }
    try {
      mContext.unregisterReceiver(smsVerificationReceiver)
    } catch (e: Exception) {
      Log.w(TAG, e)
    }
  }
  //endregion

  companion object {
    private const val TAG = "SMSHelper"
    private const val TASK_FAILURE_ERROR_TYPE = "TASK_FAILURE_ERROR_TYPE"
    private const val TASK_FAILURE_ERROR_MESSAGE = "Task failed."
  }

}
