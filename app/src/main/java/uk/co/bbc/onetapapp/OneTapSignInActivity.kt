package uk.co.bbc.onetapapp

import android.content.ContentValues.TAG
import android.content.IntentSender
import android.os.Bundle
import android.util.Log
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.auth.api.identity.SignInClient
import com.google.android.gms.auth.api.identity.SignInCredential
import com.google.android.gms.common.api.ApiException

class OneTapSignInActivity : AppCompatActivity() {
    private lateinit var oneTapClient: SignInClient

    private val loginResultHandler: ActivityResultLauncher<IntentSenderRequest> =
        registerForActivityResult(
            ActivityResultContracts.StartIntentSenderForResult()
        ) { result: ActivityResult ->
            try {
                val credential: SignInCredential?
                credential = oneTapClient.getSignInCredentialFromIntent(result.data)
                val username = credential.id
                val password = credential.password
                Log.d(TAG, "username: $username, password: $password")

            } catch (e: ApiException) {
                Log.d(TAG, e.localizedMessage ?: "")
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        oneTapClient = Identity.getSignInClient(this)
        val signInRequest = BeginSignInRequest.builder().setPasswordRequestOptions(
                BeginSignInRequest.PasswordRequestOptions.builder().setSupported(true).build()
            ).setAutoSelectEnabled(false).build()
        oneTapClient.beginSignIn(signInRequest).addOnSuccessListener(this) { result ->
                try {
                    loginResultHandler.launch(
                        IntentSenderRequest.Builder(result.pendingIntent.intentSender).build()
                    )
                } catch (e: IntentSender.SendIntentException) {
                    e.printStackTrace()
                }

            }.addOnFailureListener(this) {
                Log.d(TAG, it.message.toString())
            }
    }
}