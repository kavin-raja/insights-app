package com.example.insightsapp.data.auth

import android.app.Activity
import com.google.firebase.FirebaseException
import com.google.firebase.auth.*
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.concurrent.TimeUnit
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class FirebaseAuthService {
    private val auth = FirebaseAuth.getInstance()
    private var verificationId: String? = null
    private var resendToken: PhoneAuthProvider.ForceResendingToken? = null

    suspend fun sendOtp(phoneNumber: String, activity: Activity): Result<String> {
        return suspendCancellableCoroutine { continuation ->
            val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                    // Auto-verification completed
                    continuation.resume(Result.success("Auto-verified"))
                }

                override fun onVerificationFailed(e: FirebaseException) {
                    continuation.resume(Result.failure(e))
                }

                override fun onCodeSent(
                    verificationId: String,
                    token: PhoneAuthProvider.ForceResendingToken
                ) {
                    this@FirebaseAuthService.verificationId = verificationId
                    this@FirebaseAuthService.resendToken = token
                    continuation.resume(Result.success("OTP sent successfully"))
                }
            }

            val options = PhoneAuthOptions.newBuilder(auth)
                .setPhoneNumber(phoneNumber)
                .setTimeout(60L, TimeUnit.SECONDS)
                .setActivity(activity)
                .setCallbacks(callbacks)
                .build()

            PhoneAuthProvider.verifyPhoneNumber(options)
        }
    }

    suspend fun verifyOtp(otp: String): Result<FirebaseUser> {
        return suspendCancellableCoroutine { continuation ->
            val verificationId = this.verificationId
            if (verificationId == null) {
                continuation.resume(Result.failure(Exception("No verification ID found. Please request OTP first.")))
                return@suspendCancellableCoroutine
            }

            val credential = PhoneAuthProvider.getCredential(verificationId, otp)

            auth.signInWithCredential(credential)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val user = task.result?.user
                        if (user != null) {
                            continuation.resume(Result.success(user))
                        } else {
                            continuation.resume(Result.failure(Exception("Authentication successful but user is null")))
                        }
                    } else {
                        continuation.resume(Result.failure(task.exception ?: Exception("Verification failed")))
                    }
                }
        }
    }

    suspend fun resendOtp(phoneNumber: String, activity: Activity): Result<String> {
        return suspendCancellableCoroutine { continuation ->
            val resendToken = this.resendToken
            if (resendToken == null) {
                continuation.resume(Result.failure(Exception("No resend token available")))
                return@suspendCancellableCoroutine
            }

            val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                    continuation.resume(Result.success("Auto-verified"))
                }

                override fun onVerificationFailed(e: FirebaseException) {
                    continuation.resume(Result.failure(e))
                }

                override fun onCodeSent(
                    verificationId: String,
                    token: PhoneAuthProvider.ForceResendingToken
                ) {
                    this@FirebaseAuthService.verificationId = verificationId
                    this@FirebaseAuthService.resendToken = token
                    continuation.resume(Result.success("OTP resent successfully"))
                }
            }

            val options = PhoneAuthOptions.newBuilder(auth)
                .setPhoneNumber(phoneNumber)
                .setTimeout(60L, TimeUnit.SECONDS)
                .setActivity(activity)
                .setForceResendingToken(resendToken)
                .setCallbacks(callbacks)
                .build()

            PhoneAuthProvider.verifyPhoneNumber(options)
        }
    }

    fun getCurrentUser(): FirebaseUser? = auth.currentUser
    fun signOut() = auth.signOut()
}
