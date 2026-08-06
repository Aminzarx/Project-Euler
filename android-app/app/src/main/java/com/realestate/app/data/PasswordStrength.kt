package com.realestate.app.data

/** Pure, deterministic — no Android dependency, so the meter's exact behavior is unit-testable
 *  without an emulator, same reasoning as FileCrypto/ExportEnvelope. */
enum class PasswordStrengthLevel(val label: String) {
    WEAK("ضعیف"),
    MEDIUM("متوسط"),
    STRONG("قوی"),
    VERY_STRONG("خیلی قوی")
}

data class PasswordStrengthResult(
    val level: PasswordStrengthLevel,
    /** Whether the password satisfies the hard floor ([MIN_PASSWORD_LENGTH]) — this, not the
     *  level, is what actually gates the export button. A password can be [WEAK] and still meet
     *  the minimum; it just isn't recommended. */
    val meetsMinimum: Boolean,
    /** 0-100, for driving a progress-bar-style meter. */
    val score: Int
)

const val MIN_PASSWORD_LENGTH = 8
const val RECOMMENDED_PASSWORD_LENGTH = 12

/**
 * Scored on two independent axes that both matter for a password brute-forced offline (which is
 * exactly the threat model here — PBKDF2 in FileCrypto.kt slows guessing down, but a short or
 * low-variety password is still cheap to guess no matter how many iterations wrap it):
 * length (up to 40 of the 100 points) and character-class variety (up to 60, 15 per class:
 * lowercase/uppercase/digit/symbol). A long password of one repeated character still scores
 * low on variety; a short password with every class present still scores low on length — neither
 * axis alone can carry a password to [VERY_STRONG].
 */
fun evaluatePasswordStrength(password: String): PasswordStrengthResult {
    if (password.isEmpty()) return PasswordStrengthResult(PasswordStrengthLevel.WEAK, meetsMinimum = false, score = 0)

    val meetsMinimum = password.length >= MIN_PASSWORD_LENGTH

    val lengthScore = when {
        password.length >= 16 -> 40
        password.length >= RECOMMENDED_PASSWORD_LENGTH -> 30
        password.length >= MIN_PASSWORD_LENGTH -> 20
        else -> (password.length * 20) / MIN_PASSWORD_LENGTH
    }
    val varietyCount = listOf(
        password.any { it.isLowerCase() },
        password.any { it.isUpperCase() },
        password.any { it.isDigit() },
        password.any { !it.isLetterOrDigit() }
    ).count { it }
    val score = (lengthScore + varietyCount * 15).coerceIn(0, 100)

    val level = when {
        !meetsMinimum -> PasswordStrengthLevel.WEAK
        score >= 80 -> PasswordStrengthLevel.VERY_STRONG
        score >= 60 -> PasswordStrengthLevel.STRONG
        score >= 35 -> PasswordStrengthLevel.MEDIUM
        else -> PasswordStrengthLevel.WEAK
    }
    return PasswordStrengthResult(level, meetsMinimum, score)
}

/** The helper line shown under the strength meter — always says exactly what's missing or
 *  confirms it's fine, never just a bare color. */
fun passwordStrengthHelperText(result: PasswordStrengthResult, password: String): String = when {
    password.isEmpty() -> "حداقل $MIN_PASSWORD_LENGTH کاراکتر — ترکیب حروف بزرگ/کوچک، عدد و نماد توصیه می‌شود"
    !result.meetsMinimum -> "حداقل $MIN_PASSWORD_LENGTH کاراکتر لازم است (${password.length} کاراکتر وارد شده)"
    result.level == PasswordStrengthLevel.WEAK || result.level == PasswordStrengthLevel.MEDIUM ->
        "برای امنیت بیشتر، رمز عبور $RECOMMENDED_PASSWORD_LENGTH کاراکتر یا بیشتر با حروف بزرگ/کوچک، عدد و نماد توصیه می‌شود"
    else -> "رمز عبور مناسبی است"
}
