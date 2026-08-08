package com.realestate.app.data.auth

/** What a future real OTP verification could resolve to — modeled now, used by nothing in the
 *  active flow yet, so wiring in a real SMS gateway later is a matter of implementing
 *  [OtpService], not inventing a new result shape under time pressure. */
sealed class OtpVerificationResult {
    data object Verified : OtpVerificationResult()
    data class Incorrect(val attemptsRemaining: Int? = null) : OtpVerificationResult()
    data object Expired : OtpVerificationResult()
    data class Failed(val message: String) : OtpVerificationResult()
}

/**
 * Seam for real OTP/SMS verification. OTP is not active yet (see the referral-system spec's OTP
 * section) — [PhoneAuthService]'s active registration path never calls this. [DisabledOtpService]
 * is the only implementation today; a real SMS-gateway-backed one plugs in here later without
 * touching [PhoneAuthService] or any screen that doesn't specifically opt into calling it.
 */
interface OtpService {
    val isEnabled: Boolean
    suspend fun requestOtp(mobileNumber: String): Result<Unit>
    suspend fun verifyOtp(mobileNumber: String, code: String): OtpVerificationResult
}

/** The only [OtpService] wired up today. Every call fails explicitly rather than pretending to
 *  send or verify anything real — see the spec: "Do NOT create fake OTP functionality." */
object DisabledOtpService : OtpService {
    override val isEnabled: Boolean = false

    override suspend fun requestOtp(mobileNumber: String): Result<Unit> =
        Result.failure(IllegalStateException("OTP is not enabled yet"))

    override suspend fun verifyOtp(mobileNumber: String, code: String): OtpVerificationResult =
        OtpVerificationResult.Failed("OTP is not enabled yet")
}
