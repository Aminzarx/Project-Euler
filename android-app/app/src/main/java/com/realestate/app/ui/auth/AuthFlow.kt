package com.realestate.app.ui.auth

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.realestate.app.viewmodel.AuthStep
import com.realestate.app.viewmodel.AuthViewModel

@Composable
fun AuthFlow(viewModel: AuthViewModel) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    AnimatedContent(
        targetState = state.step,
        transitionSpec = { fadeIn(animationSpec = tween(220)) togetherWith fadeOut(animationSpec = tween(180)) },
        label = "auth-step"
    ) { step ->
        when (step) {
            AuthStep.PHONE -> PhoneEntryScreen(
                state = state,
                onMobileChange = viewModel::updateMobileNumber,
                onSubmit = viewModel::sendOtp
            )

            AuthStep.OTP -> OtpVerificationScreen(
                state = state,
                onOtpChange = viewModel::updateOtpInput,
                onVerify = viewModel::verifyOtp,
                onResend = viewModel::resendOtp,
                onEditNumber = viewModel::editMobileNumber
            )
        }
    }
}
