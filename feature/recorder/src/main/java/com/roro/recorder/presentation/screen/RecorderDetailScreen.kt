package com.roro.recorder.presentation.screen

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.roro.recorder.presentation.RecordViewModel
import com.roro.recorder.presentation.uiState.RecordState
import timber.log.Timber

@Composable
fun RecorderDetailScreen(
    navController: NavController,
    fileId: String,
    viewModel: RecordViewModel = hiltViewModel()
) {

    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current

    // 🎤 권한 요청
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        Timber.d("🎤 권한 결과: $isGranted")

        if (isGranted) {
            viewModel.startRecording()
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

            // 🔥 상태 표시
            when (state) {
                is RecordState.Idle -> Text("대기 중")
                is RecordState.Recording -> Text("🎤 녹음 중...")
                is RecordState.Processing -> Text("⏳ 처리 중...")
                is RecordState.Success -> Text("✅ 완료: ${(state as RecordState.Success).summary}")
                is RecordState.Error -> Text("❌ 오류: ${(state as RecordState.Error).message}")
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 🎙 녹음 버튼
            Button(
                onClick = {
                    Timber.d("🎯 버튼 클릭, 현재 상태: $state")

                    viewModel.checkAICore(context)

//                    when (state) {
//                        is RecordState.Recording -> {
//                            Timber.d("🛑 녹음 중 → stopRecording")
//                            viewModel.stopRecording()
//                        }
//
//                        else -> {
//                            val permissionCheck = ContextCompat.checkSelfPermission(
//                                context,
//                                Manifest.permission.RECORD_AUDIO
//                            )
//
//                            if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
//                                Timber.d("✅ 권한 있음 → startRecording")
//                                viewModel.startRecording()
//                            } else {
//                                Timber.d("⚠️ 권한 없음 → 요청")
//                                permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
//                            }
//                        }
//                    }
                }
            ) {
                Text(
                    when (state) {
                        is RecordState.Recording -> "녹음 정지"
                        is RecordState.Processing -> "처리 중..."
                        else -> "녹음 시작"
                    }
                )
            }


            // 요약용 입력받을 문자열 여기서 확인
            var inputText by remember { mutableStateOf("") }

            OutlinedTextField(
                value = inputText,
                onValueChange = { inputText = it },
                label = { Text("요약할 텍스트 입력") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                minLines = 3
            )

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = {
                    viewModel.summarizeText(context, inputText)
                }
            ) {
                Text("🧠 요약 테스트")
            }


            Spacer(modifier = Modifier.height(12.dp))

            // 🔥 처리 중일 때 버튼 비활성화
            if (state is RecordState.Processing) {
                CircularProgressIndicator()
            }
        }
    }
}