package com.lit.wa.yuen.handyman.repository.impl

import android.content.Context
import androidx.activity.result.ActivityResultRegistryOwner
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.Companion.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.UserProfileChangeRequest
import com.lit.wa.yuen.handyman.R
import com.lit.wa.yuen.handyman.models.SignInResult
import com.lit.wa.yuen.handyman.models.UserData
import com.lit.wa.yuen.handyman.repository.AuthRepository
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class AuthRepositoryImpl @Inject constructor(
    private val auth: FirebaseAuth,
    private val context: Context // Application Context
) : AuthRepository {

    override suspend fun googleSignIn(): SignInResult {
        try {
            // 1. Setup Google ID Option
            val googleIdOption = GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(true)
                .setServerClientId(context.getString(R.string.default_web_client_id))
                .setAutoSelectEnabled(true)
                .build()

            // 2. Create Request
            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()

            // 3. Get Credential (This launches the UI BottomSheet)
            // Note: use the Activity context passed from UI, not App context
            val credentialManager = CredentialManager.create(context)
            val result = credentialManager.getCredential(
                request = request,
                context = context
            )

            // 4. Parse Result
            val credential = result.credential
            if (credential is CustomCredential && credential.type == TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)

                val googleIdToken = googleIdTokenCredential.idToken
                val firebaseCredential = GoogleAuthProvider.getCredential(googleIdToken, null)

                // 5. Sign in to Firebase
                val authResult = auth.signInWithCredential(firebaseCredential).await()
                val user = authResult.user

                return SignInResult(
                    data = user?.run {
                        UserData(
                            userId = uid,
                            username = displayName,
                            email = email,
                            profilePictureUrl = photoUrl?.toString()
                        )
                    },
                    errorMessage = null
                )
            } else {
                return SignInResult(data = null, errorMessage = "Invalid Credential Type")
            }
        } catch (e: Exception) {
            return SignInResult(data = null, errorMessage = e.message)
        }
    }

    override fun getSignedInUser(): UserData? {
        return auth.currentUser?.run {
            UserData(uid, displayName, photoUrl?.toString(), email)
        }
    }

    override suspend fun signOut() {
        auth.signOut()
    }

    override suspend fun emailSignUp(email: String, password: String, name: String): SignInResult {
        return try {
            val authResult = auth.createUserWithEmailAndPassword(email, password).await()
            val user = authResult.user

            if (user != null) {
                // 1. Create a request to update the user's profile with the provided name
                val profileUpdates = UserProfileChangeRequest.Builder()
                    .setDisplayName(name)
                    .build()

                // 2. Apply the profile update
                user.updateProfile(profileUpdates).await()
            }

            SignInResult(
                data = user?.run {
                    UserData(
                        userId = uid,
                        // Use the new displayName (set above), fall back to email if somehow missed
                        email = email,
                        username = displayName ?: name,
                        profilePictureUrl = photoUrl?.toString()
                    )
                },
                errorMessage = null
            )
        } catch (e: Exception) {
            SignInResult(data = null, errorMessage = e.message)
        }
    }


    // --- New: Email Sign-In Logic ---
    override suspend fun emailSignIn(email: String, password: String): SignInResult {
        return try {
            val authResult = auth.signInWithEmailAndPassword(email, password).await()
            val user = authResult.user

            SignInResult(
                data = user?.run {
                    UserData(
                        userId = uid,
                        username = displayName,
                        profilePictureUrl = photoUrl?.toString(),
                        email = email
                    )
                },
                errorMessage = null
            )
        } catch (e: Exception) {
            SignInResult(data = null, errorMessage = e.message)
        }
    }

    override suspend fun facebookSignIn(context: Context): SignInResult {
        // Note: The Activity context must be passed to this function, not the application context.
        val activity = context as? ActivityResultRegistryOwner
            ?: return SignInResult(data = null, errorMessage = "Context must be an ActivityResultRegistryOwner")

        return try {
            // Use a coroutine to wait for the Facebook SDK's callback result.
            val loginResult = suspendCancellableCoroutine<LoginResult> { continuation ->
                val loginManager = LoginManager.getInstance()
                val callbackManager = CallbackManager.Factory.create()

                // Define the callback to handle success, cancellation, or error.
                val callback = object : FacebookCallback<LoginResult> {
                    override fun onSuccess(result: LoginResult) {
                        // Resume the coroutine with the successful result.
                        continuation.resume(result)
                    }

                    override fun onCancel() {
                        // Cancel the coroutine if the user cancels the login.
                        continuation.cancel(kotlin.coroutines.cancellation.CancellationException("Facebook login was cancelled by the user."))
                    }

                    override fun onError(error: FacebookException) {
                        // Resume with an exception if an error occurs.
                        continuation.resumeWithException(error)
                    }
                }

                loginManager.registerCallback(callbackManager, callback)

                // Make sure to unregister the callback when the coroutine is cancelled.
                continuation.invokeOnCancellation {
                    loginManager.unregisterCallback(callbackManager)
                }

                // Start the login flow.
                loginManager.logIn(activity, callbackManager, listOf("email", "public_profile"))
            }

            // --- Coroutine has resumed, we now have the login result ---

            // 1. Create a Firebase credential using the Facebook Access Token.
            val accessToken = loginResult.accessToken
            val firebaseCredential = FacebookAuthProvider.getCredential(accessToken.token)

            // 2. Sign in to Firebase.
            val authResult = auth.signInWithCredential(firebaseCredential).await()
            val user = authResult.user

            // 3. Return the successful sign-in result.
            SignInResult(
                data = user?.run {
                    UserData(
                        userId = uid,
                        username = displayName,
                        profilePictureUrl = photoUrl?.toString(),
                        email = email
                    )
                },
                errorMessage = null
            )
        } catch (e: Exception) {
            // Catch exceptions from the coroutine (e.g., login error, cancellation) or Firebase.
            SignInResult(data = null, errorMessage = e.message)
        }
    }

}