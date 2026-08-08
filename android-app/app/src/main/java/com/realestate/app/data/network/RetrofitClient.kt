package com.realestate.app.data.network

import com.realestate.app.BuildConfig
import java.util.concurrent.TimeUnit
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * Single Retrofit instance for the referral/registration backend (see /backend at the repo
 * root) — every API interface ([UsersApi], [ReferralApi]) is created from this one client rather
 * than each constructing its own, so base URL/timeout/logging configuration lives in exactly one
 * place. [BuildConfig.API_BASE_URL] is set from `apiBaseUrl` in build.gradle.kts, defaulting to
 * the Android emulator's localhost alias for local development — never hard-code a different
 * value here, override it at build time instead.
 */
object RetrofitClient {
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY else HttpLoggingInterceptor.Level.NONE
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl(BuildConfig.API_BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val usersApi: UsersApi by lazy { retrofit.create(UsersApi::class.java) }
    val referralApi: ReferralApi by lazy { retrofit.create(ReferralApi::class.java) }
}
