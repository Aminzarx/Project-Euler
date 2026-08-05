package com.realestate.app.ui.components

import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation

/**
 * Many Persian-language Android keyboards default to Persian ("۰۱۲۳...") or Arabic-Indic
 * ("٠١٢٣...") numerals instead of ASCII digits. `Char.isDigit()` accepts both (they're Unicode
 * decimal digits), so a naive digit filter would happily display them — but every calculation in
 * this app (`toDoubleOrNull()`, `toLongOrNull()`) only understands ASCII "0"-"9" and silently
 * returns 0 for anything else. Converting to ASCII here, instead of merely rejecting non-ASCII
 * digits, keeps the field usable for anyone typing on a Persian-numeral keyboard while guaranteeing
 * every amount that reaches a calculation is actually parseable.
 */
internal fun normalizeDigits(input: String): String = input.map { ch ->
    when (ch) {
        in '۰'..'۹' -> '0' + (ch - '۰')
        in '٠'..'٩' -> '0' + (ch - '٠')
        else -> ch
    }
}.joinToString("")

/**
 * Groups digits with thousands separators for display (e.g. "1,500,000") while the underlying
 * value stays a plain digit string — used on every field that takes a monetary amount so large
 * numbers stay readable while typing.
 */
class ThousandsSeparatorTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val raw = text.text
        val grouped = groupThousands(raw)
        val mapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                var seen = 0
                for (i in grouped.indices) {
                    if (seen == offset) return i
                    if (grouped[i] != ',') seen++
                }
                return grouped.length
            }

            override fun transformedToOriginal(offset: Int): Int {
                var count = 0
                val limit = offset.coerceIn(0, grouped.length)
                for (i in 0 until limit) {
                    if (grouped[i] != ',') count++
                }
                return count
            }
        }
        return TransformedText(AnnotatedString(grouped), mapping)
    }
}

private fun groupThousands(raw: String): String {
    if (raw.isEmpty()) return raw
    val dotIndex = raw.indexOf('.')
    val intPart = if (dotIndex >= 0) raw.substring(0, dotIndex) else raw
    val fracPart = if (dotIndex >= 0) raw.substring(dotIndex) else ""
    if (intPart.isEmpty()) return raw
    val grouped = intPart.reversed().chunked(3).joinToString(",").reversed()
    return grouped + fracPart
}

/** Digits beyond this are not a realistic toman amount for this app — guards against unbounded,
 *  meaningless input rather than an actual currency limit. */
private const val MAX_DIGITS = 15

/**
 * A money-amount input: numeric keyboard, live thousands-grouping, and a label that always
 * states the unit/basis (e.g. "قیمت کل ملک (تومان)" vs "قیمت هر متر (تومان)") so it's never
 * ambiguous what number the user is expected to type. Pass [errorText] to show a validation
 * message and switch the field into its error styling.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoneyField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    helperText: String? = null,
    errorText: String? = null
) {
    OutlinedTextField(
        value = value,
        onValueChange = { input ->
            onValueChange(normalizeDigits(input).filter { it in '0'..'9' }.take(MAX_DIGITS))
        },
        label = { Text(label) },
        supportingText = when {
            errorText != null -> {
                { Text(errorText, color = MaterialTheme.colorScheme.error) }
            }
            helperText != null -> {
                { Text(helperText) }
            }
            else -> null
        },
        isError = errorText != null,
        visualTransformation = ThousandsSeparatorTransformation(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        singleLine = true,
        modifier = modifier
    )
}
