package com.example.myapplication.auth

import android.accounts.Account
import android.accounts.AccountManager
import android.app.Application
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.example.myapplication.*
import com.example.myapplication.R
import com.example.myapplication.auth.api.UserApi
import com.example.myapplication.main.HustlrMainActivity
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import net.openid.appauth.*

/**
 * View Model class that prepared and manages data for Login Activity.
 * This class authenticates the user through OAuth and backend calls, and stores a Hustlr account
 * in AccountManager.
 *
 * @param application - application context of Login Activity
 */
class LoginViewModel(application: Application): AndroidViewModel(application) {

    val navigation: MutableLiveData<Pair<Int, Intent>> = MutableLiveData()
    val message: MutableLiveData<Pair<Int, Int>> = MutableLiveData()

    private var authService: AuthorizationService? = null
    private var disposable: Disposable? = null
    private val am: AccountManager by lazy { AccountManager.get(application) }
    private val api: UserApi by lazy { UserApi.create() }

    init {
        if (!am.getAccountsByType(HUSTLR_ACCOUNT_TYPE).isEmpty()) {
            val intent = Intent(getApplication(), HustlrMainActivity::class.java)
            navigation.postValue(Pair(START_NO_RESULT, intent))
        }
    }

    override fun onCleared() {
        super.onCleared()

        disposable?.dispose()
        disposable = null
        authService = null
    }

    /**
     * Build and send a /auth request to Google.
     * This function will redirect the user to Google's login screen.
     */
    fun sendGoogleAuthRequest() {
        val serviceConfig = AuthorizationServiceConfiguration(
            Uri.parse("https://accounts.google.com/o/oauth2/v2/auth"),
            Uri.parse("https://oauth2.googleapis.com/token")
        )
        val authRequestBuilder = AuthorizationRequest.Builder(
            serviceConfig,
            GOOGLE_CID,
            ResponseTypeValues.CODE,
            Uri.parse("myapplication.example.com:/auth"))
        val authRequest = authRequestBuilder
            .setScope("openid email profile")
            .build()
        authService = AuthorizationService(getApplication())
        val authIntent = authService!!.getAuthorizationRequestIntent(authRequest)
        navigation.postValue(Pair(RC_AUTH, authIntent))
    }

    /**
     * Build and send a /token request to Google and retrieve a userId through a backend call.
     * On success, this a Hustlr account will be stored in AccountManager.
     *
     * @param data - Intent from Auth Request
     */
    fun sendGoogleTokenRequest(data: Intent) {
        val authResp = AuthorizationResponse.fromIntent(data)
        val authEx = AuthorizationException.fromIntent(data)

        if (authResp != null) {
            authService!!.performTokenRequest(authResp.createTokenExchangeRequest()) { tokenResp, tokenEx ->
                if (tokenResp != null) {
                    disposable = api
                        .getUser(tokenResp.accessToken!!, tokenResp.idToken!!)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                            { userResp ->
                                val account = Account(userResp.properties.email, HUSTLR_ACCOUNT_TYPE)
                                val userData = Bundle().apply {
                                    putString("accessToken", tokenResp.accessToken)
                                    putString("refreshToken", tokenResp.refreshToken)
                                    putString("idToken", tokenResp.idToken)
                                    putString("tokenType", tokenResp.tokenType)
                                    putString("scope", tokenResp.scope)
                                    putLong("accessTokenExpiry", tokenResp.accessTokenExpirationTime!!)
                                    putString("userId", userResp._id)
                                }
                                am.addAccountExplicitly(account, "", userData)

                                val intent = Intent(getApplication(), HustlrMainActivity::class.java)
                                navigation.postValue(Pair(START_NO_RESULT, intent))
                            },
                            { err ->
                                Log.w(TAG, LOGIN_FAILED, err)
                                message.postValue(Pair(MSG_TOAST, R.string.login_fail))
                            }
                        )
                } else {
                    Log.w(TAG, LOGIN_FAILED, tokenEx)
                    message.postValue(Pair(MSG_TOAST, R.string.login_fail))
                }
            }
        } else {
            Log.w(TAG, LOGIN_FAILED, authEx)
            message.postValue(Pair(MSG_TOAST, R.string.login_fail))
        }
    }

    companion object {
        const val TAG = "LoginViewModel"
        const val LOGIN_FAILED = "Login Failed"
    }
}
