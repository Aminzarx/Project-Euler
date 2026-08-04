package com.realestate.app.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.realestate.app.data.datastore.LocalDeviceSessionValidator
import com.realestate.app.data.datastore.SessionManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class AuthStep { PHONE, OTP }

data class AuthUiState(
    val step: AuthStep = AuthStep.PHONE,
    val mobileNumber: String = "",
    val otpInput: String = "",
    val generatedOtp: String? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val resendSecondsLeft: Int = 0
)

/**
 * Mobile number + OTP sign-in. There is no real SMS provider wired up, so the OTP is
 * generated on-device and shown in [AuthUiState.generatedOtp] for the demo build -
 * swap in a real SMS gateway call inside [sendOtp]/[resendOtp] when one is available.
 * Cross-device session enforcement is delegated to [LocalDeviceSessionValidator], a
 * seam meant to be replaced by a real backend check.
 */
class AuthViewModel(application: Application) : AndroidViewModel(application) {
    private val sessionManager = SessionManager(application)
    private val deviceValidator = LocalDeviceSessionValidator()

    val isLoggedIn: StateFlow<Boolean?> = sessionManager.isLoggedIn
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState

    fun updateMobileNumber(value: String) {
        _uiState.value = _uiState.value.copy(mobileNumber = value.filter { it.isDigit() }.take(11), error = null)
    }

    fun updateOtpInput(value: String) {
        _uiState.value = _uiState.value.copy(otpInput = value.filter { it.isDigit() }.take(5), error = null)
    }

    fun sendOtp() {
        val mobile = _uiState.value.mobileNumber
        if (!isValidIranianMobile(mobile)) {
            _uiState.value = _uiState.value.copy(error = "شماره موبایل معتبر نیست")
            return
        }
        _uiState.value = _uiState.value.copy(
            step = AuthStep.OTP,
            generatedOtp = generateOtp(),
            otpInput = "",
            error = null,
            resendSecondsLeft = 60
        )
        startResendCountdown()
    }

    fun resendOtp() {
        if (_uiState.value.resendSecondsLeft > 0) return
        _uiState.value = _uiState.value.copy(generatedOtp = generateOtp(), otpInput = "", resendSecondsLeft = 60)
        startResendCountdown()
    }

    fun editMobileNumber() {
        _uiState.value = AuthUiState(mobileNumber = _uiState.value.mobileNumber)
    }

    fun verifyOtp() {
        val state = _uiState.value
        if (state.otpInput.length != 5 || state.otpInput != state.generatedOtp) {
            _uiState.value = state.copy(error = "کد وارد شده صحیح نیست")
            return
        }
        viewModelScope.launch {
            _uiState.value = state.copy(isLoading = true, error = null)
            val deviceId = sessionManager.getOrCreateDeviceId()
            val allowed = deviceValidator.canActivate(state.mobileNumber, deviceId)
            if (allowed) {
                sessionManager.login(state.mobileNumber, deviceId)
                _uiState.value = AuthUiState()
            } else {
                _uiState.value = _uiState.value.copy(isLoading = false, error = "این حساب هم‌اکنون روی دستگاه دیگری فعال است")
            }
        }
    }

    fun logout() = viewModelScope.launch {
        sessionManager.logout()
    }

    private fun startResendCountdown() {
        viewModelScope.launch {
            while (_uiState.value.resendSecondsLeft > 0) {
                delay(1000)
                _uiState.value = _uiState.value.copy(
                    resendSecondsLeft = (_uiState.value.resendSecondsLeft - 1).coerceAtLeast(0)
                )
            }
        }
    }

    private fun generateOtp(): String = (10000..99999).random().toString()
}

private fun isValidIranianMobile(number: String): Boolean = Regex("^09\\d{9}$").matches(number)
