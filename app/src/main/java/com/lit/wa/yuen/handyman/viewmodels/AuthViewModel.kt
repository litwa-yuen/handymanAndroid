package com.lit.wa.yuen.handyman.viewmodels

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lit.wa.yuen.handyman.models.SignInResult
import com.lit.wa.yuen.handyman.models.UserData
import com.lit.wa.yuen.handyman.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

// --- ViewModel for Authentication and State Management ---

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _state = MutableStateFlow(AuthState())
    val state = _state.asStateFlow()

    init {
        // Check for existing signed-in user on launch
        _state.update { it.copy(currentUser = authRepository.getSignedInUser()) }
    }

    fun onGoogleSignInClicked() {
        if (_state.value.isLoading) return
        _state.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            val result = authRepository.googleSignIn()
            handleSignInResult(result)
        }
    }

    fun onEmailSignUpClicked(name: String, email: String, password: String) {
        if (_state.value.isLoading) return
        _state.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            val result = authRepository.emailSignUp(email, password, name)
            handleSignInResult(result)
        }
    }

    fun onEmailSignInClicked(email: String, password: String) {
        if (_state.value.isLoading) return
        _state.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            val result = authRepository.emailSignIn(email, password)
            handleSignInResult(result)
        }
    }

    fun onSignOutClicked() {
        viewModelScope.launch {
            authRepository.signOut()
            _state.update { AuthState() } // Reset state on sign out
        }
    }

    // Helper function to process sign-in results
    private fun handleSignInResult(result: SignInResult) {
        _state.update {
            it.copy(
                currentUser = result.data,
                error = result.errorMessage,
                isLoading = false
            )
        }
    }

    // --- New Feature: Facebook Sign-in Handler ---
    fun onFacebookSignInClicked(context: Context) {
        if (_state.value.isLoading) return
        _state.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            val result = authRepository.facebookSignIn(context)
            handleSignInResult(result)
        }
    }
}

data class AuthState(
    val currentUser: UserData? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)