package com.realestate.app.data.network

import com.realestate.app.data.network.dto.ValidateReferralResponseDto
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

interface ReferralApi {
    /** Lets the registration screen check a code as the agent finishes typing it, before ever
     *  submitting the form — see AuthViewModel.updateReferralCode. */
    @GET("api/referral/validate/{code}")
    suspend fun validateReferralCode(@Path("code") code: String): Response<ValidateReferralResponseDto>
}
