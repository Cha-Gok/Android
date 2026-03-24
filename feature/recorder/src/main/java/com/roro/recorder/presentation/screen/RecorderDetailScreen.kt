package com.roro.recorder.presentation.screen

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.roro.recorder.presentation.viewModel.RecordViewModel
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import androidx.compose.ui.platform.LocalContext

@Composable
fun RecorderDetailScreen(
    navController: NavController,
    fileId: String,
    viewModel: RecordViewModel = hiltViewModel()
) {
    val isRecording by viewModel.isRecording.collectAsStateWithLifecycle()
    val isPaused by viewModel.isPaused.collectAsStateWithLifecycle()

    val context = LocalContext.current

    // 🎤 마이크 권한 요청
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) @androidx.annotation.RequiresPermission(android.Manifest.permission.RECORD_AUDIO) { isGranted ->
        if (isGranted) {
            viewModel.startRecording()   // 🔥 핵심
        }
    }

    Surface(modifier = Modifier.fillMaxSize()) {
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Recorder Screen")
            Text("받은 값: $fileId")

            Spacer(modifier = Modifier.height(24.dp))

            // 🎙 1. 시작 / 정지 버튼
            Button(
                onClick = {
                    if (isRecording) {
                        viewModel.stopRecording()
                    } else {
                        val permissionCheck = ContextCompat.checkSelfPermission(
                            context,
                            Manifest.permission.RECORD_AUDIO
                        )

                        if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
                            viewModel.startRecording()   // ✅ 안전
                        } else {
                            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                        }
                    }
                }
            ){
                Text(
                    if (isRecording) "녹음 정지"
                    else "녹음 시작"
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // ⏸ 2. 일시정지 / 재개 버튼
            Button(
                onClick = {
                    if (isPaused) {
                        viewModel.resumeRecording()
                    } else {
                        viewModel.pauseRecording()
                    }
                },
                enabled = isRecording   // 🔥 녹음 중일 때만 활성화
            ) {
                Text(
                    when {
                        !isRecording -> "일시정지"
                        isPaused -> "재개"
                        else -> "일시정지"
                    }
                )
            }
        }
    }
}