package com.realestate.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import com.realestate.app.ui.RealEstateApp
import com.realestate.app.ui.theme.RealEstateAppTheme
import com.realestate.app.viewmodel.PropertyViewModel

class MainActivity : ComponentActivity() {
    private val viewModel: PropertyViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            RealEstateAppTheme {
                RealEstateApp(viewModel = viewModel)
            }
        }
    }
}
