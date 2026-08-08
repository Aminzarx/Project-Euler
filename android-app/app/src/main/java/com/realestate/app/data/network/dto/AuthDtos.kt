package com.realestate.app.data.network.dto

/** Mirrors backend/src/routes/users.routes.ts's registerSchema. */
data class RegisterRequestDto(
    val phoneNumber: String,
    val referralCode: String
)

/** Mirrors the JSON shape POST /api/users/register responds with on success (201). Deliberately
 *  does not include anything about the referrer beyond what the backend itself sends — the
 *  backend never sends the referrer's identity to the new user either (see the referral-validate
 *  endpoint's response, which was hardened not to leak ownerId — same principle here). */
data class RegisterResponseDto(
    val id: String,
    val phoneNumber: String,
    val referralCode: String,
    val referrerId: String?
)

/** GET /api/referral/validate/:code's response — intentionally just a boolean, no owner identity
 *  (see backend/src/routes/referral.routes.ts). */
data class ValidateReferralResponseDto(
    val valid: Boolean
)

/** Every non-2xx response from this backend is `{ "error": "<message>" }` (see
 *  backend/src/middleware/errorHandler.ts) — used to classify which specific Persian message the
 *  UI should show (see RemotePhoneAuthService.classifyRegistrationError). */
data class ApiErrorDto(
    val error: String? = null
)
