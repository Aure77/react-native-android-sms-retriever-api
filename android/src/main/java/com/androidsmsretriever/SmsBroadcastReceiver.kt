package com.androidsmsretriever

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.facebook.react.bridge.BaseActivityEventListener
import com.facebook.react.bridge.ReactApplicationContext
import com.google.android.gms.auth.api.phone.SmsRetriever
import com.google.android.gms.common.api.CommonStatusCodes
import com.google.android.gms.common.api.Status


class SmsBroadcastReceiver(private val mContext: ReactApplicationContext) : BroadcastReceiver() {

  private val activityResultListener = object : BaseActivityEventListener() {
    override fun onActivityResult(
      activity: Activity,
      requestCode: Int,
      resultCode: Int,
      data: Intent?
    ) {
      when (requestCode) {
        SMS_CONSENT_REQUEST ->
          if (resultCode == Activity.RESULT_OK && data != null) {
            // Get SMS message content
            val message = data.getStringExtra(SmsRetriever.EXTRA_SMS_MESSAGE)
            mJSEventHelper.emitJSEvent(MESSAGE_KEY, message)
          }
      }
    }
  }

  private val mJSEventHelper by lazy {
    JSEventHelper(mContext)
  }

  init {
    mContext.addActivityEventListener(activityResultListener)
  }

  override fun onReceive(context: Context, intent: Intent) {
    if (SmsRetriever.SMS_RETRIEVED_ACTION == intent.action) {
      val extras = intent.extras
      if (extras == null) {
        mJSEventHelper.emitJSEvent(EXTRAS_KEY, EXTRAS_NULL_ERROR_MESSAGE)
        return
      }
      val status = extras.get(SmsRetriever.EXTRA_STATUS) as Status?
      if (status == null) {
        mJSEventHelper.emitJSEvent(STATUS_KEY, STATUS_NULL_ERROR_MESSAGE)
        return
      }
      when (status.statusCode) {
        CommonStatusCodes.SUCCESS -> {
          // Try get consent intent
          val consentIntent = extras.getParcelable<Intent>(SmsRetriever.EXTRA_CONSENT_INTENT)
          if (consentIntent != null) {
            startConsentIntent(consentIntent)
          } else {
            // Get SMS message content
            val message = extras.getString(SmsRetriever.EXTRA_SMS_MESSAGE)
            mJSEventHelper.emitJSEvent(MESSAGE_KEY, message)
          }
        }
        CommonStatusCodes.TIMEOUT -> {
          mJSEventHelper.emitJSEvent(TIMEOUT_KEY, TIMEOUT_ERROR_MESSAGE)
        }
      }
    }
  }

  private fun startConsentIntent(
    consentIntent: Intent
  ) {
    try {
      // Start activity to show consent dialog to user, activity must be started in
      // 5 minutes, otherwise you'll receive another TIMEOUT intent
      if (mContext.currentActivity is Activity && mContext.currentActivity != null) {
        mContext.currentActivity!!.startActivityForResult(consentIntent, SMS_CONSENT_REQUEST)

      } else {
        Log.e(TAG, "currentActivity should be an instanceof Activity.");
      }
    } catch (e: ActivityNotFoundException) {
      Log.w(TAG, e)
      mJSEventHelper.emitJSEvent(ERROR_KEY, CONSENT_REQUEST_ERROR_MESSAGE)
    }
  }

  companion object {
    private const val TAG = "SmsBroadcastReceiver"
    private const val SMS_CONSENT_REQUEST = 1
    private const val EXTRAS_KEY = "extras"
    private const val MESSAGE_KEY = "message"
    private const val STATUS_KEY = "status"
    private const val TIMEOUT_KEY = "timeout"
    private const val ERROR_KEY = "error"
    private const val EXTRAS_NULL_ERROR_MESSAGE = "Extras is null."
    private const val STATUS_NULL_ERROR_MESSAGE = "Status is null."
    private const val TIMEOUT_ERROR_MESSAGE = "Timeout error."
    private const val CONSENT_REQUEST_ERROR_MESSAGE = "SMS_CONSENT_REQUEST error."
  }

}
