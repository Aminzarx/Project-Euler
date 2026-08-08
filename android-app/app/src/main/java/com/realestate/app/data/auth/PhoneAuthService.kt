package com.realestate.app.data.auth

/** Every business-rule rejection the backend's registration endpoint can produce (see
 *  backend/src/engines/ReferralEngine.ts) gets its own case here rather than a single generic
 *  "failed" string, so the UI can show the exact message the product spec requires for each one
 *  instead of relaying a raw server message to the agent. */
sealed class RegistrationResult {
    data class Success(val userId: String, val mobileNumber: String, val referralCode: String) : RegistrationResult()
    data object InvalidReferralCodeFormat : RegistrationResult()
    data object ReferralCodeNotFound : RegistrationResult()
    data object ReferrerNotEligible : RegistrationResult()
    data object SelfReferral : RegistrationResult()
    data object PhoneAlreadyRegistered : RegistrationResult()
    data object NetworkError : RegistrationResult()
    data class UnknownError(val message: String) : RegistrationResult()
}

enum class ReferralCodeCheckResult { VALID, INVALID, NETWORK_ERROR }

/**
 * The one authoritative entry point for turning a (mobile number, referral code) pair into an
 * account. Every place in this app that can create a user must go through this — never
 * re-implement referral validation inline in a screen or ViewModel (see the referral-system
 * spec's "Important Architecture Requirement": one centralized mechanism, not scattered
 * conditions).
 *
 * [requestOtp]/[verifyOtp] exist so the *future* flow (Mobile → OTP → Referral → Account, per the
 * spec's OTP section) is a matter of the UI calling different methods on this same interface —
 * not a rewrite of the registration path. They delegate to an [OtpService], which is
 * [DisabledOtpService] until a real SMS gateway is wired up; nothing in the active registration
 * path ([register]) calls them today.
 */
interface PhoneAuthService {
    /** [mobileNumber] and [referralCode] must already be normalized (see PhoneValidation.kt /
     *  ReferralCodeValidation.kt) — this is the business-layer gate, not a UI-layer one, but it
     *  still trusts its caller to have done basic input hygiene rather than re-deriving it. */
    suspend fun register(mobileNumber: String, referralCode: String): RegistrationResult

    /** Live "does this code look usable" check, e.g. while the agent is still typing it in — see
     *  the spec's "کد معرف معتبر است" confirmation UX. Never creates or reserves anything; a
     *  VALID result here is not a guarantee [register] will succeed a moment later (the code
     *  could be consumed or its owner blocked in between), so [register] always re-validates. */
    suspend fun checkReferralCode(referralCode: String): ReferralCodeCheckResult

    suspend fun requestOtp(mobileNumber: String): Result<Unit>
    suspend fun verifyOtp(mobileNumber: String, code: String): OtpVerificationResult
}
