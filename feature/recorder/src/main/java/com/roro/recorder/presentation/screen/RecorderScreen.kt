package com.roro.recorder.presentation.screen

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.roro.core.navigation.Routes
import com.roro.recorder.presentation.RecordViewModel
import timber.log.Timber
import java.io.File


@Composable
fun RecorderScreen(
    navController: NavController,
    viewModel: RecordViewModel = hiltViewModel()
) {
    val transcribeState by viewModel.transcribeState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    val filePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument() // ✅ GetContent → OpenDocument
    ) { uri: Uri? ->
        uri?.let { viewModel.startTranscribeFromUri(it, context) }
    }

    val audioPermission = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            filePicker.launch(arrayOf("audio/*"))
        } else {
            Timber.e("🎤 권한 거부됨")
        }
    }


    Surface(modifier = Modifier.fillMaxSize()) {
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Recorder Screen")

            when (val state = transcribeState) {
                is RecordViewModel.TranscribeState.Idle -> { }
                is RecordViewModel.TranscribeState.Loading -> {
                    CircularProgressIndicator()
                    Text("변환 중...")
                }
                is RecordViewModel.TranscribeState.Success -> {
                    Text(state.text)
                }
                is RecordViewModel.TranscribeState.Error -> {
                    Text("오류: ${state.message}", color = Color.Red)
                }
            }

            Button(onClick = {
                audioPermission.launch(android.Manifest.permission.RECORD_AUDIO) // ✅ 권한 먼저 요청
            }) {
                Text("파일 선택")
            }

            // ✅ 살린 버튼
            Button(
                onClick = {
                    Timber.d("Timber D")
                    Timber.i("Timber I")
                    Timber.w("Timber W")
                    try {
                        throw RuntimeException("Test Exception for logE")
                    } catch (e: Exception) {
                        Timber.e(e, "Timber E")
                    }
                    navController.navigate(Routes.recorderDetail(fileId = "testId"))
                }
            ) {
                Text("go Detail")
            }
        }
    }
}