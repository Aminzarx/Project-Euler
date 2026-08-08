package com.realestate.app.data.auth

/** Exactly 8 characters, uppercase A-Z and 0-9 — see the referral-system spec's format
 *  requirements. Applied only *after* [normalizeReferralCodeInput], never against raw input. */
private val REFERRAL_CODE_REGEX = Regex("^[A-Z0-9]{8}$")

/** trim spaces -> uppercase — the first two steps of the spec's mandated normalization order
 *  (trim -> uppercase -> validate format -> look up in the database). Every referral-code input
 *  field and every call into [PhoneAuthService] runs input through this before doing anything
 *  else with it, so "typed lowercase" or "typed with a stray leading space" are never treated as
 *  a different code than the canonical one. */
fun normalizeReferralCodeInput(raw: String): String = raw.trim().uppercase()

/** Format-only check (length + character set) — does not touch the network. A referral code
 *  failing this can never be valid regardless of what the backend would say, so callers should
 *  short-circuit here before spending a request on [PhoneAuthService.checkReferralCode]. */
fun isValidReferralCodeFormat(code: String): Boolean = REFERRAL_CODE_REGEX.matches(code)
