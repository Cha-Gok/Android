package com.roro.recorder.presentation.screen

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.roro.recorder.presentation.RecordViewModel
import com.roro.recorder.presentation.uiState.RecordState
import kotlinx.coroutines.flow.MutableStateFlow
import timber.log.Timber

@Composable
fun RecorderDetailScreen(
    navController: NavController,
    fileId: String,
    viewModel: RecordViewModel = hiltViewModel()
) {
    val state          by viewModel.state.collectAsStateWithLifecycle()
    val summarizeState by viewModel.summarizeState.collectAsStateWithLifecycle()
    val sttResult      by viewModel.sttResult.collectAsStateWithLifecycle()
    val context        = LocalContext.current

    var pickedUri by remember { mutableStateOf<Uri?>(null) }

    // 오디오 파일 피커
    val filePicker = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        pickedUri = uri
        uri?.let { viewModel.startTranscribeFromUri(it, context.applicationContext) }
    }

    // 마이크 권한 → startRecording
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        Timber.d("🎤 권한 결과: $isGranted")
        if (isGranted) viewModel.startRecording()
    }

    Surface(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("🛠 Dev Test — $fileId", style = MaterialTheme.typography.titleMedium)

            HorizontalDivider()

            // ── 상태 표시 ──────────────────────────────────────────
            Text("RecordState: $state", style = MaterialTheme.typography.bodySmall)
            Text("SummarizeState: $summarizeState", style = MaterialTheme.typography.bodySmall)

            HorizontalDivider()

            // ── 녹음 ──────────────────────────────────────────────
            SectionLabel("🎙 Recording")

            Button(onClick = {
                val granted = ContextCompat.checkSelfPermission(
                    context, Manifest.permission.RECORD_AUDIO
                ) == PackageManager.PERMISSION_GRANTED
                if (granted) viewModel.startRecording()
                else permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
            }, modifier = Modifier.fillMaxWidth()) {
                Text("startRecording()")
            }

            Button(onClick = {
                viewModel.stopRecording()
            }, modifier = Modifier.fillMaxWidth()) {
                Text("stopRecording()")
            }

            HorizontalDivider()

            // ── STT ───────────────────────────────────────────────
            SectionLabel("🎤 STT")

            Button(onClick = {
                viewModel.checkSTT()
            }, modifier = Modifier.fillMaxWidth()) {
                Text("checkSTT()")
            }

            Button(onClick = {
                filePicker.launch("audio/*")
            }, modifier = Modifier.fillMaxWidth()) {
                Text("startSTTFromFile() ← 파일 선택")
            }

            pickedUri?.let {
                Text("선택된 파일: ${it.lastPathSegment}", style = MaterialTheme.typography.bodySmall)
            }

            // STT 결과 표시
            if (sttResult.isNotBlank()) {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = sttResult,
                        modifier = Modifier.padding(12.dp),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            HorizontalDivider()

            // ── 요약 결과 표시 ────────────────────────────────────
            SectionLabel("🤖 Summarization")

            when (val s = summarizeState) {
                is RecordViewModel.SummarizeState.Loading ->
                    CircularProgressIndicator()
                is RecordViewModel.SummarizeState.Success ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = s.summary,
                            modifier = Modifier.padding(12.dp),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                is RecordViewModel.SummarizeState.Error ->
                    Text("요약 실패: ${s.message}", color = MaterialTheme.colorScheme.error)
                else -> Unit
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelLarge,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    )
}

