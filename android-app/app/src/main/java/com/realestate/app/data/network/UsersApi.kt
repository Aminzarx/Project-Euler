package com.realestate.app.data.network

import com.realestate.app.data.network.dto.RegisterRequestDto
import com.realestate.app.data.network.dto.RegisterResponseDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface UsersApi {
    @POST("api/users/register")
    suspend fun register(@Body body: RegisterRequestDto): Response<RegisterResponseDto>
}
