package com.realestate.app.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.realestate.app.data.auth.AuthenticationState
import com.realestate.app.data.auth.PhoneAuthService
import com.realestate.app.data.auth.ReferralCodeCheckResult
import com.realestate.app.data.auth.RegistrationResult
import com.realestate.app.data.auth.RemotePhoneAuthService
import com.realestate.app.data.auth.isValidReferralCodeFormat
import com.realestate.app.data.auth.normalizeReferralCodeInput
import com.realestate.app.data.datastore.LocalDeviceSessionValidator
import com.realestate.app.data.datastore.SessionManager
import com.realestate.app.data.isValidIranianMobile
import com.realestate.app.data.network.RetrofitClient
import com.realestate.app.ui.components.normalizeDigits
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class AuthStep { PHONE, OTP }

/** Live-typing feedback for the referral code field — see the spec's UX: format/lookup feedback
 *  without creating an account prematurely. [CHECKING]/[VALID]/[NOT_FOUND]/[NETWORK_ERROR] only
 *  ever come from a code that already passed [isValidReferralCodeFormat]; anything shorter than
 *  that is always [EMPTY] or [INVALID_FORMAT], purely client-side, no request sent. */
enum class ReferralFieldStatus { EMPTY, INVALID_FORMAT, CHECKING, VALID, NOT_FOUND, NETWORK_ERROR }

data class AuthUiState(
    val step: AuthStep = AuthStep.PHONE,
    val mobileNumber: String = "",
    val referralCode: String = "",
    val referralStatus: ReferralFieldStatus = ReferralFieldStatus.EMPTY,
    val otpInput: String = "",
    val generatedOtp: String? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val resendSecondsLeft: Int = 0
)

/**
 * Mobile number + referral code registration — see the referral-system spec: every new account
 * must go through [PhoneAuthService.register], which enforces the referral gate at the business
 * layer (backend/src/engines/ReferralEngine.ts), not just here in the UI.
 *
 * OTP is a prepared-but-dormant step: [AuthStep.OTP] and every OTP-related method below
 * ([updateOtpInput], [sendOtp], [resendOtp], [verifyOtp], [editMobileNumber]) still exist exactly
 * as before and still work as a local, no-real-SMS demo, but nothing in the active registration
 * path (see [register]) transitions into that step anymore — see [PhoneAuthService.requestOtp]/
 * [PhoneAuthService.verifyOtp] for the real seam a future SMS gateway would use instead. Cross
 * -device session enforcement is delegated to [LocalDeviceSessionValidator], a seam meant to be
 * replaced by a real backend check.
 */
class AuthViewModel(
    application: Application,
    private val phoneAuthService: PhoneAuthService = RemotePhoneAuthService(RetrofitClient.usersApi, RetrofitClient.referralApi)
) : AndroidViewModel(application) {
    private val sessionManager = SessionManager(application)
    private val deviceValidator = LocalDeviceSessionValidator()

    val isLoggedIn: StateFlow<Boolean?> = sessionManager.isLoggedIn
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    /** Who's signed in, for screens that need the account identity (Profile's referral section)
     *  rather than just a yes/no gate — see [AuthenticationState]. */
    val authenticationState: StateFlow<AuthenticationState> = combine(
        sessionManager.isLoggedIn, sessionManager.mobileNumber, sessionManager.userId, sessionManager.referralCode
    ) { loggedIn, mobile, userId, code ->
        if (loggedIn && mobile != null && userId != null && code != null) {
            AuthenticationState.Authenticated(userId, mobile, code)
        } else {
            AuthenticationState.Unauthenticated
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AuthenticationState.Unauthenticated)

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState

    private var referralCheckJob: Job? = null

    fun updateMobileNumber(value: String) {
        _uiState.value = _uiState.value.copy(
            mobileNumber = normalizeDigits(value).filter { it.isDigit() }.take(11),
            error = null
        )
    }

    /** Normalizes (trim -> uppercase, per the spec) on every keystroke and kicks off a debounced
     *  live lookup once the result is a format-valid 8-character code — see [ReferralFieldStatus]. */
    fun updateReferralCode(value: String) {
        val normalized = normalizeReferralCodeInput(value).filter { it.isLetterOrDigit() }.take(8)
        referralCheckJob?.cancel()
        when {
            normalized.isEmpty() ->
                _uiState.value = _uiState.value.copy(referralCode = normalized, referralStatus = ReferralFieldStatus.EMPTY, error = null)
            !isValidReferralCodeFormat(normalized) ->
                _uiState.value = _uiState.value.copy(referralCode = normalized, referralStatus = ReferralFieldStatus.INVALID_FORMAT, error = null)
            else -> {
                _uiState.value = _uiState.value.copy(referralCode = normalized, referralStatus = ReferralFieldStatus.CHECKING, error = null)
                referralCheckJob = viewModelScope.launch {
                    delay(400)
                    val result = phoneAuthService.checkReferralCode(normalized)
                    if (_uiState.value.referralCode != normalized) return@launch // input moved on while this was in flight
                    _uiState.value = _uiState.value.copy(
                        referralStatus = when (result) {
                            ReferralCodeCheckResult.VALID -> ReferralFieldStatus.VALID
                            ReferralCodeCheckResult.INVALID -> ReferralFieldStatus.NOT_FOUND
                            ReferralCodeCheckResult.NETWORK_ERROR -> ReferralFieldStatus.NETWORK_ERROR
                        }
                    )
                }
            }
        }
    }

    /** The active registration path: Mobile Number + Referral Code -> Account Creation/Access,
     *  with no OTP hop (see the class doc comment). */
    fun register() {
        val state = _uiState.value
        if (!isValidIranianMobile(state.mobileNumber)) {
            _uiState.value = state.copy(error = "شماره موبایل معتبر نیست")
            return
        }
        if (!isValidReferralCodeFormat(state.referralCode)) {
            _uiState.value = state.copy(error = "کد معرف معتبر نیست")
            return
        }
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            when (val result = phoneAuthService.register(state.mobileNumber, state.referralCode)) {
                is RegistrationResult.Success -> {
                    sessionManager.login(result.mobileNumber, result.userId, result.referralCode)
                    _uiState.value = AuthUiState()
                }
                RegistrationResult.InvalidReferralCodeFormat, RegistrationResult.ReferralCodeNotFound ->
                    _uiState.value = _uiState.value.copy(isLoading = false, error = "کد معرف معتبر نیست")
                RegistrationResult.ReferrerNotEligible ->
                    _uiState.value = _uiState.value.copy(isLoading = false, error = "این کد معرف قابل استفاده نیست")
                RegistrationResult.SelfReferral ->
                    _uiState.value = _uiState.value.copy(isLoading = false, error = "نمی‌توانید از کد معرف خودتان استفاده کنید")
                RegistrationResult.PhoneAlreadyRegistered ->
                    _uiState.value = _uiState.value.copy(isLoading = false, error = "این شماره قبلاً ثبت شده است")
                RegistrationResult.NetworkError ->
                    _uiState.value = _uiState.value.copy(isLoading = false, error = "خطا در اتصال به اینترنت. دوباره تلاش کنید")
                is RegistrationResult.UnknownError ->
                    _uiState.value = _uiState.value.copy(isLoading = false, error = "خطایی رخ داد. دوباره تلاش کنید")
            }
        }
    }

    fun updateOtpInput(value: String) {
        _uiState.value = _uiState.value.copy(otpInput = value.filter { it.isDigit() }.take(5), error = null)
    }

    /** Dormant demo path — see the class doc comment. Not reachable from the active registration
     *  screen; kept working so [AuthStep.OTP]/OtpVerificationScreen stay real, compilable,
     *  ready-to-wire code rather than a stub that would bit-rot silently. */
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
