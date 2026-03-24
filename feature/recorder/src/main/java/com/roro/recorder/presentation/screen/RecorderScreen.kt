package com.roro.recorder.presentation.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import com.roro.core.navigation.Routes
import timber.log.Timber


@Composable
fun RecorderScreen(
    navController: NavController,
    // viewModel: RecorderViewModel = hiltViewModel()
) {
    Surface(
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Text("Recorder Screen")

            Button(
                onClick = {
                    // 이동 예시
                    Timber.d("Timber D")
                    Timber.i("Timber I")
                    Timber.w("Timber W")
                    try {
                        throw RuntimeException("Test Exception for logE")
                    } catch (e: Exception) {
                        Timber.e(e,"Timber E")
                    }
                    navController.navigate(Routes.recorderDetail(fileId = "testId"))
                }
            ) {
                Text("go Detail")
            }
        }
    }
}