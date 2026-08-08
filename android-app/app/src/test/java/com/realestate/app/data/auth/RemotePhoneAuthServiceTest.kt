package com.realestate.app.data.auth

import com.realestate.app.data.network.ReferralApi
import com.realestate.app.data.network.UsersApi
import com.realestate.app.data.network.dto.RegisterRequestDto
import com.realestate.app.data.network.dto.RegisterResponseDto
import com.realestate.app.data.network.dto.ValidateReferralResponseDto
import java.io.IOException
import kotlinx.coroutines.runBlocking
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.Response

private fun backendError(message: String): Response<RegisterResponseDto> {
    val body = "{\"error\":\"$message\"}".toResponseBody("application/json".toMediaType())
    return Response.error(400, body)
}

private class FakeUsersApi(
    private val result: (RegisterRequestDto) -> Response<RegisterResponseDto> = {
        throw AssertionError("register() should not have been called")
    }
) : UsersApi {
    var callCount = 0
        private set

    override suspend fun register(body: RegisterRequestDto): Response<RegisterResponseDto> {
        callCount++
        return result(body)
    }
}

private class FakeReferralApi(
    private val result: (String) -> Response<ValidateReferralResponseDto> = {
        throw AssertionError("validateReferralCode() should not have been called")
    }
) : ReferralApi {
    var callCount = 0
        private set

    override suspend fun validateReferralCode(code: String): Response<ValidateReferralResponseDto> {
        callCount++
        return result(code)
    }
}

/** Covers the client-side portions of the referral-system spec's required test scenarios that
 *  live in this class: valid/invalid/nonexistent code, inactive referrer, self-referral, duplicate
 *  mobile — each expressed as the specific backend error message [RemotePhoneAuthService] must
 *  classify correctly — plus network-failure handling and the format short-circuit that keeps
 *  malformed input from ever reaching the network. */
class RemotePhoneAuthServiceTest {

    @Test
    fun `register succeeds and returns the backend-issued identity`() = runBlocking {
        val usersApi = FakeUsersApi(result = {
            Response.success(RegisterResponseDto(id = "u1", phoneNumber = "09120000000", referralCode = "REFCODE1", referrerId = "u0"))
        })
        val service = RemotePhoneAuthService(usersApi, FakeReferralApi())

        val result = service.register("09120000000", "REFCODE1")

        assertTrue(result is RegistrationResult.Success)
        result as RegistrationResult.Success
        assertEquals("u1", result.userId)
        assertEquals("REFCODE1", result.referralCode)
    }

    @Test
    fun `register rejects a malformed referral code without calling the network`() = runBlocking {
        val usersApi = FakeUsersApi()
        val service = RemotePhoneAuthService(usersApi, FakeReferralApi())

        val result = service.register("09120000000", "short")

        assertEquals(RegistrationResult.InvalidReferralCodeFormat, result)
        assertEquals(0, usersApi.callCount)
    }

    @Test
    fun `register classifies a nonexistent referral code`() = runBlocking {
        val usersApi = FakeUsersApi(result = { backendError("Invalid referral code") })
        val service = RemotePhoneAuthService(usersApi, FakeReferralApi())

        assertEquals(RegistrationResult.ReferralCodeNotFound, service.register("09120000000", "ABCD1234"))
    }

    @Test
    fun `register classifies an inactive referrer`() = runBlocking {
        val usersApi = FakeUsersApi(result = { backendError("Referral code is not usable") })
        val service = RemotePhoneAuthService(usersApi, FakeReferralApi())

        assertEquals(RegistrationResult.ReferrerNotEligible, service.register("09120000000", "ABCD1234"))
    }

    @Test
    fun `register classifies self-referral`() = runBlocking {
        val usersApi = FakeUsersApi(result = { backendError("Users cannot refer themselves") })
        val service = RemotePhoneAuthService(usersApi, FakeReferralApi())

        assertEquals(RegistrationResult.SelfReferral, service.register("09120000000", "ABCD1234"))
    }

    @Test
    fun `register classifies a duplicate mobile number`() = runBlocking {
        val usersApi = FakeUsersApi(result = { backendError("Phone number already registered") })
        val service = RemotePhoneAuthService(usersApi, FakeReferralApi())

        assertEquals(RegistrationResult.PhoneAlreadyRegistered, service.register("09120000000", "ABCD1234"))
    }

    @Test
    fun `register falls back to UnknownError for an unrecognized backend message`() = runBlocking {
        val usersApi = FakeUsersApi(result = { backendError("Something exploded") })
        val service = RemotePhoneAuthService(usersApi, FakeReferralApi())

        assertTrue(service.register("09120000000", "ABCD1234") is RegistrationResult.UnknownError)
    }

    @Test
    fun `register reports a network error without crashing`() = runBlocking {
        val usersApi = FakeUsersApi(result = { throw IOException("offline") })
        val service = RemotePhoneAuthService(usersApi, FakeReferralApi())

        assertEquals(RegistrationResult.NetworkError, service.register("09120000000", "ABCD1234"))
    }

    @Test
    fun `checkReferralCode rejects malformed input without calling the network`() = runBlocking {
        val referralApi = FakeReferralApi()
        val service = RemotePhoneAuthService(FakeUsersApi(), referralApi)

        val result = service.checkReferralCode("nope")

        assertEquals(ReferralCodeCheckResult.INVALID, result)
        assertEquals(0, referralApi.callCount)
    }

    @Test
    fun `checkReferralCode reports VALID and INVALID from the backend`() = runBlocking {
        val referralApi = FakeReferralApi(result = { code ->
            Response.success(ValidateReferralResponseDto(valid = code == "GOODCODE"))
        })
        val service = RemotePhoneAuthService(FakeUsersApi(), referralApi)

        assertEquals(ReferralCodeCheckResult.VALID, service.checkReferralCode("GOODCODE"))
        assertEquals(ReferralCodeCheckResult.INVALID, service.checkReferralCode("BADCODE1"))
    }

    @Test
    fun `checkReferralCode reports NETWORK_ERROR on IOException`() = runBlocking {
        val referralApi = FakeReferralApi(result = { throw IOException("offline") })
        val service = RemotePhoneAuthService(FakeUsersApi(), referralApi)

        assertEquals(ReferralCodeCheckResult.NETWORK_ERROR, service.checkReferralCode("ABCD1234"))
    }
}
