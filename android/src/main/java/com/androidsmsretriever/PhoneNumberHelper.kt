package com.androidsmsretriever

import android.app.Activity
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import com.androidsmsretriever.GooglePlayServicesHelper.hasSupportedVersion
import com.androidsmsretriever.GooglePlayServicesHelper.isAvailable
import com.facebook.react.bridge.ActivityEventListener
import com.facebook.react.bridge.BaseActivityEventListener
import com.facebook.react.bridge.Promise
import com.google.android.gms.auth.api.identity.GetPhoneNumberHintIntentRequest
import com.google.android.gms.auth.api.identity.Identity


class PhoneNumberHelper {
  private var mPromise: Promise? = null
  private var mListener: Listener? = null

  fun onPhoneNumberResultReceived(listener: Listener) {
    mListener = listener
  }

  fun requestPhoneNumber(context: Context, activity: Activity?, promise: Promise?) {
    if (promise == null) {
      callAndResetListener()
      return
    }
    mPromise = promise
    if (!isAvailable(context)) {
      promiseReject(
        GooglePlayServicesHelper.UNAVAILABLE_ERROR_TYPE,
        GooglePlayServicesHelper.UNAVAILABLE_ERROR_MESSAGE
      )
      callAndResetListener()
      return
    }
    if (!hasSupportedVersion(context)) {
      promiseReject(
        GooglePlayServicesHelper.UNSUPORTED_VERSION_ERROR_TYPE,
        GooglePlayServicesHelper.UNSUPORTED_VERSION_ERROR_MESSAGE
      )
      callAndResetListener()
      return
    }
    if (activity == null) {
      promiseReject(ACTIVITY_NULL_ERROR_TYPE, ACTIVITY_NULL_ERROR_MESSAGE)
      callAndResetListener()
      return
    }

    val hintRequest: GetPhoneNumberHintIntentRequest = GetPhoneNumberHintIntentRequest.builder().build()
    Identity.getSignInClient(context)
      .getPhoneNumberHintIntent(hintRequest)
      .addOnSuccessListener { result: PendingIntent ->
        try {
          val intent = IntentSenderRequest.Builder(result).build()
          activity.startIntentSenderForResult(
            intent.intentSender,
            REQUEST_PHONE_NUMBER_REQUEST_CODE, null, 0, 0, 0
          )
        } catch (e: Exception) {
          Log.e(TAG, "Launching the PendingIntent failed")
          promiseReject(SEND_INTENT_ERROR_TYPE, SEND_INTENT_ERROR_MESSAGE)
          callAndResetListener()
        }
      }
      .addOnFailureListener {
        Log.e(TAG, "Phone Number Hint failed")
        promiseReject(SEND_INTENT_ERROR_TYPE, SEND_INTENT_ERROR_MESSAGE)
        callAndResetListener()
      }
  }

  private fun callAndResetListener() {
    if (mListener != null) {
      mListener!!.phoneNumberResultReceived()
      mListener = null
    }
  }
  //endregion

  //region - Promises
  private fun promiseResolve(value: Any) {
    if (mPromise != null) {
      mPromise!!.resolve(value)
      mPromise = null
    }
  }

  private fun promiseReject(type: String, message: String) {
    if (mPromise != null) {
      mPromise!!.reject(type, message)
      mPromise = null
    }
  }

  //region - Package Access
  val activityEventListener: ActivityEventListener = object : BaseActivityEventListener() {
    override fun onActivityResult(
      activity: Activity,
      requestCode: Int,
      resultCode: Int,
      data: Intent?
    ) {
      super.onActivityResult(activity, requestCode, resultCode, data)
      if (requestCode == REQUEST_PHONE_NUMBER_REQUEST_CODE && resultCode == Activity.RESULT_OK && data != null) {
        try {
          val phoneNumber = Identity.getSignInClient(activity).getPhoneNumberFromIntent(data)
          promiseResolve(phoneNumber)
          callAndResetListener()
        } catch(e: Exception) {
          Log.e(TAG, "Phone Number Hint failed")
          promiseReject(ACTIVITY_RESULT_NOOK_ERROR_TYPE, ACTIVITY_RESULT_NOOK_ERROR_MESSAGE)
          callAndResetListener()
        }
      }
    }
  }
  //endregion

  //region - Classes
  fun interface Listener {
    fun phoneNumberResultReceived()
  }
  //endregion

  companion object {
    private const val TAG = "PhoneNumberHelper"
    private const val REQUEST_PHONE_NUMBER_REQUEST_CODE = 1
    private const val ACTIVITY_NULL_ERROR_TYPE = "ACTIVITY_NULL_ERROR_TYPE"
    private const val ACTIVITY_RESULT_NOOK_ERROR_TYPE = "ACTIVITY_RESULT_NOOK_ERROR_TYPE"
    private const val SEND_INTENT_ERROR_TYPE = "SEND_INTENT_ERROR_TYPE"
    private const val ACTIVITY_NULL_ERROR_MESSAGE = "Activity is null."
    private const val ACTIVITY_RESULT_NOOK_ERROR_MESSAGE =
      "There was an error trying to get the phone number."
    private const val SEND_INTENT_ERROR_MESSAGE = "There was an error trying to send intent."
  }
}
