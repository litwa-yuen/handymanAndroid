package com.lit.wa.yuen.handyman.repository

import android.content.Context
import com.lit.wa.yuen.handyman.models.SignInResult
import com.lit.wa.yuen.handyman.models.UserData

interface AuthRepository {
    suspend fun googleSignIn(): SignInResult
    suspend fun facebookSignIn(context: Context): SignInResult
    suspend fun emailSignUp(email: String, password: String, name: String): SignInResult
    suspend fun emailSignIn(email: String, password: String): SignInResult
    fun getSignedInUser(): UserData?
    suspend fun signOut()
}