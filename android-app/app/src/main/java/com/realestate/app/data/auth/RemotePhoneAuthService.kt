package com.realestate.app.data.auth

import com.google.gson.Gson
import com.realestate.app.data.network.ReferralApi
import com.realestate.app.data.network.UsersApi
import com.realestate.app.data.network.dto.ApiErrorDto
import com.realestate.app.data.network.dto.RegisterRequestDto
import java.io.IOException
import retrofit2.Response

/** Real backend-backed implementation of [PhoneAuthService] — the only one wired into the app
 *  today (see AuthViewModel). [otpService] defaults to [DisabledOtpService], matching the spec's
 *  "OTP is not active yet." */
class RemotePhoneAuthService(
    private val usersApi: UsersApi,
    private val referralApi: ReferralApi,
    private val otpService: OtpService = DisabledOtpService
) : PhoneAuthService {

    override suspend fun register(mobileNumber: String, referralCode: String): RegistrationResult {
        // Format is checked here too (not just in the UI/ViewModel) — this is the business-layer
        // gate the spec requires; a caller that skipped the UI (a future bulk-import path, say)
        // still can't slip an 8-character-format violation past it.
        if (!isValidReferralCodeFormat(referralCode)) return RegistrationResult.InvalidReferralCodeFormat

        return try {
            val response = usersApi.register(RegisterRequestDto(mobileNumber, referralCode))
            if (response.isSuccessful) {
                val body = response.body() ?: return RegistrationResult.UnknownError("پاسخ نامعتبر از سرور")
                RegistrationResult.Success(body.id, body.phoneNumber, body.referralCode)
            } else {
                classifyRegistrationError(response)
            }
        } catch (e: IOException) {
            RegistrationResult.NetworkError
        }
    }

    override suspend fun checkReferralCode(referralCode: String): ReferralCodeCheckResult {
        if (!isValidReferralCodeFormat(referralCode)) return ReferralCodeCheckResult.INVALID
        return try {
            val response = referralApi.validateReferralCode(referralCode)
            val body = response.body()
            when {
                response.isSuccessful && body?.valid == true -> ReferralCodeCheckResult.VALID
                response.isSuccessful -> ReferralCodeCheckResult.INVALID
                else -> ReferralCodeCheckResult.NETWORK_ERROR
            }
        } catch (e: IOException) {
            ReferralCodeCheckResult.NETWORK_ERROR
        }
    }

    override suspend fun requestOtp(mobileNumber: String): Result<Unit> = otpService.requestOtp(mobileNumber)

    override suspend fun verifyOtp(mobileNumber: String, code: String): OtpVerificationResult =
        otpService.verifyOtp(mobileNumber, code)

    /** Maps the backend's plain-English error messages (see backend/src/engines/ReferralEngine.ts
     *  and backend/src/middleware/errorHandler.ts — every rejection is a 400 with `{ error:
     *  "<message>" }`, there are no structured error codes yet) to the specific outcome the UI
     *  needs. Deliberately conservative: anything not recognized falls through to [UnknownError]
     *  rather than being guessed into the wrong specific case. */
    private fun classifyRegistrationError(response: Response<*>): RegistrationResult {
        val message = parseErrorMessage(response) ?: return RegistrationResult.UnknownError("خطای نامشخص")
        return when {
            "already registered" in message -> RegistrationResult.PhoneAlreadyRegistered
            "cannot refer themselves" in message -> RegistrationResult.SelfReferral
            "not usable" in message -> RegistrationResult.ReferrerNotEligible
            "Invalid referral code" in message || "required" in message -> RegistrationResult.ReferralCodeNotFound
            else -> RegistrationResult.UnknownError(message)
        }
    }

    private fun parseErrorMessage(response: Response<*>): String? {
        val raw = response.errorBody()?.string() ?: return null
        return runCatching { Gson().fromJson(raw, ApiErrorDto::class.java).error }.getOrNull()
    }
}
