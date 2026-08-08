package com.realestate.app.data.auth

/**
 * The app's overall sign-in state — distinct from [com.realestate.app.viewmodel.AuthUiState],
 * which is the registration *form's* transient input state (what's typed, loading, error text).
 * This is what the rest of the app (Profile's referral section, anything else that needs to know
 * "who is signed in" without caring about form mechanics) actually depends on.
 *
 * Kept as a sealed type rather than a boolean specifically so a future OTP step (see
 * [OtpService]) has somewhere to add an intermediate state — e.g. an `AwaitingOtp` case — without
 * every existing reader needing to change from a boolean check to something else at that point.
 */
sealed class AuthenticationState {
    data object Unauthenticated : AuthenticationState()
    data class Authenticated(
        val userId: String,
        val mobileNumber: String,
        val referralCode: String
    ) : AuthenticationState()
}
