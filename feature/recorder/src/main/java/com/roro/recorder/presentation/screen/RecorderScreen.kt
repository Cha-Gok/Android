package com.roro.recorder.presentation.screen

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.roro.core.navigation.Routes
import com.roro.core.ui.component.ChaGokBackground
import com.roro.core.ui.component.ChaGokBox
import com.roro.core.ui.component.ChaGokNoteList
import com.roro.core.ui.component.ChaGokTopBar
import com.roro.core.ui.component.ChagokRadioDialog
import com.roro.core.ui.component.ChagokStartRecordFAB
import com.roro.core.ui.component.RecordingLanguage
import com.roro.core.ui.component.SummaryStatus
import com.roro.recorder.presentation.RecordViewModel

// 여기가 곧 기본 녹음 화면으로 구현 (예정)
// 사용 안해
@Composable
fun RecorderScreen(
    navController: NavController,
    viewModel: RecordViewModel = hiltViewModel()
) {

    // ------------------------------------ 테스트용 파일 ㅇ넣기
    var pickedUri by remember { mutableStateOf<Uri?>(null) }
    val context = LocalContext.current

    val filePicker = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        pickedUri = uri
        uri?.let { viewModel.startTranscribeFromUri(it, context.applicationContext) }
    }
    // navigationEvent 수신 → ResultScreen 이동
    LaunchedEffect(Unit) {
        viewModel.navigationEvent.collect { voiceNoteId ->
            navController.navigate(Routes.recordResult(voiceNoteId))
        }
    }
    //-----------------------------------------------------------------



    var showDropdown by remember { mutableStateOf(false) }
    var showLanguageDialog by remember { mutableStateOf(false) }
    var selectedLanguage by remember { mutableStateOf(RecordingLanguage.KOREAN) }

    ChaGokBackground {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                ChaGokTopBar(
                    title = "차곡",
                    onFirstActionClick = { /* 검색 */ },
                    onSecondActionClick = { showDropdown = true },
                    // 설정 아이콘 자리에 드롭다운 앵커
                    secondActionTrailingContent = {
                        DropdownMenu(
                            expanded = showDropdown,
                            onDismissRequest = { showDropdown = false },
                        ) {
                            DropdownMenuItem(
                                text = { Text("녹음 언어 설정") },
                                onClick = {
                                    showDropdown = false
                                    showLanguageDialog = true
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("약관 보기") },
                                onClick = { showDropdown = false }
                            )
                        }
                    }
                )
            },
            floatingActionButton = {
                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // 임시 테스트 버튼
                    Button(
                        onClick = { filePicker.launch("audio/*") },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF3D3D4E)
                        )
                    ) {
                        Text("파일 STT 테스트", color = Color.White, fontSize = 12.sp)
                    }

                    ChagokStartRecordFAB(
                        onClick = { navController.navigate(Routes.RECORD_DETAIL) }
                    )
                }
            }
        ) { paddingValues ->

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
            ) {
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(sampleBoxItems) { item ->
                        ChaGokBox(
                            text = item.label,
                            count = item.count,
                            isSelected = item.isSelected,
                            onClick = {},
                            modifier = Modifier
                                .width(92.dp)
                                .height(120.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                sampleVoiceNotes.forEach { note ->
                    ChaGokNoteList(
                        title = note.title,
                        time = note.time,
                        summaryStatus = note.summaryStatus,
                        modifier = Modifier.padding(horizontal = 20.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }

    // 언어 설정 다이얼로그
    if (showLanguageDialog) {
        ChagokRadioDialog(
            title = "녹음 언어 변경",
            options = RecordingLanguage.entries,
            selectedOption = selectedLanguage,
            onOptionSelected = { selectedLanguage = it },
            onDismiss = { showLanguageDialog = false },
            onConfirm = { showLanguageDialog = false }
        )
    }
}

// 샘플 데이터
private data class BoxItem(val label: String, val count: Int, val isSelected: Boolean)
private data class VoiceNoteItem(val title: String, val time: String, val summaryStatus: SummaryStatus)

private val sampleBoxItems = listOf(
    BoxItem("최근 기록", 0, isSelected = true),
    BoxItem("기본 폴더", 2, isSelected = false),
    BoxItem("폴더2", 0, isSelected = false),
    BoxItem("폴더3", 0, isSelected = false),
)

private val sampleVoiceNotes = listOf(
    VoiceNoteItem("오전 취업 관련 강의", "오후 3:23 · 2시간 12분", SummaryStatus.COMPLETED),
    VoiceNoteItem("오전 취업 관련 강의", "오후 3:23 · 2시간 12분", SummaryStatus.NONE),
)

