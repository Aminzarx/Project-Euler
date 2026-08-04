package com.realestate.app.data.wallet

import kotlinx.coroutines.delay
import java.util.UUID

sealed class PaymentResult {
    data class Success(val referenceId: String) : PaymentResult()
    data class Failure(val message: String) : PaymentResult()
}

/**
 * Charges the wallet. Swap [MockPaymentGateway] for a real gateway (Zarinpal, IDPay, ...)
 * by implementing this interface - the rest of the wallet feature does not need to change.
 */
interface PaymentGateway {
    suspend fun charge(amount: Long): PaymentResult
}

/** Simulates an instant, always-successful payment. No real money moves. */
class MockPaymentGateway : PaymentGateway {
    override suspend fun charge(amount: Long): PaymentResult {
        delay(1200)
        return PaymentResult.Success(referenceId = "MOCK-${UUID.randomUUID().toString().take(8).uppercase()}")
    }
}
