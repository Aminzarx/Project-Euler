package com.realestate.app.data

/**
 * A valid Iranian mobile number after stripping everything but digits: 11 digits, starting with
 * "09" (e.g. 09123456789). Accepts an already-normalized (Latin-digit) string — callers typing in
 * Persian numerals should run [normalizeDigits] first, same as every other numeric field in the app.
 */
fun isValidIranianMobile(phone: String): Boolean {
    val digits = phone.filter { it.isDigit() }
    return digits.length == 11 && digits.startsWith("09")
}
