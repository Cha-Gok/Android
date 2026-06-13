//package com.roro.storage.presentation.filelist
//
//import androidx.compose.foundation.clickable
//import androidx.compose.foundation.layout.Arrangement
//import androidx.compose.foundation.layout.Column
//import androidx.compose.foundation.layout.Row
//import androidx.compose.foundation.layout.Spacer
//import androidx.compose.foundation.layout.fillMaxSize
//import androidx.compose.foundation.layout.fillMaxWidth
//import androidx.compose.foundation.layout.height
//import androidx.compose.foundation.layout.padding
//import androidx.compose.foundation.layout.size
//import androidx.compose.foundation.lazy.LazyColumn
//import androidx.compose.foundation.lazy.items
//import androidx.compose.material.icons.Icons
//import androidx.compose.material.icons.filled.Brush
//import androidx.compose.material3.Button
//import androidx.compose.material3.Checkbox
//import androidx.compose.material3.Icon
//import androidx.compose.material3.Surface
//import androidx.compose.material3.Text
//import androidx.compose.material3.TextField
//import androidx.compose.runtime.Composable
//import androidx.compose.runtime.LaunchedEffect
//import androidx.compose.runtime.collectAsState
//import androidx.compose.runtime.getValue
//import androidx.compose.runtime.mutableStateListOf
//import androidx.compose.runtime.mutableStateOf
//import androidx.compose.runtime.remember
//import androidx.compose.runtime.saveable.rememberSaveable
//import androidx.compose.runtime.setValue
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.platform.LocalContext
//import androidx.compose.ui.tooling.preview.Preview
//import androidx.compose.ui.unit.dp
//import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
//import androidx.navigation.NavController
//import androidx.navigation.compose.rememberNavController
//import com.roro.core.model.VoiceNote
//import com.roro.core.ui.theme.ChaGokTheme
//import com.roro.core.util.toast
//import com.roro.storage.presentation.StorageEffect
//import com.roro.storage.presentation.StorageIntent
//import com.roro.storage.presentation.home.StorageViewModel
//import timber.log.Timber
//import java.util.UUID
//
//@Composable
//fun StorageDetailScreen(
//    navController: NavController,
//    folderId: String,
//    viewModel: StorageViewModel = hiltViewModel()
//) {
//    val uiState by viewModel.uiState.collectAsState()
//    val context = LocalContext.current
//    val uuid = UUID.fromString(folderId)
//    val voiceNotes by viewModel.voiceNoteFolderList.collectAsState()
//    var voiceNoteName by rememberSaveable { mutableStateOf("") }
//
//    // 추후 디테일 전용 이펙트 생성
//    LaunchedEffect(Unit) {
//        viewModel.effect.collect { effect ->
//            when (effect) {
//                is StorageEffect.ShowToast -> context.toast(effect.message)
//                StorageEffect.ClearFolderInput -> {}
//            }
//        }
//    }
//    LaunchedEffect(uuid) {
////        viewModel.observeVoiceNoteByFolder(uuid)
//    }
//
//    LaunchedEffect(Unit) {
//        viewModel.effect.collect { effect ->
//            when (effect) {
//                is StorageEffect.ClearFolderInput -> {
//                    voiceNoteName = ""
//                }
//
//                is StorageEffect.ShowToast -> context.toast(effect.message)
//            }
//        }
//    }
//
//    if (uiState.isLoading) {
//        Timber.d("로딩 중~")
//    }
//
//    uiState.errorMessage?.let {
//        Timber.d("text $it")
//    }
//
//    StorageDetailScreenContent(
//        navController = navController,
//        voiceNoteName = voiceNoteName,
//        folderId = uuid,
//        onFolderNameChange = { voiceNoteName = it },
//        voiceNotes = voiceNotes,
//        onRemoveVoiceNotes = { ids ->
//            viewModel.onIntent(StorageIntent.MoveToTrashVoiceNotes(ids = ids))
//        },
//        renameVoiceNote = { voiceNote ->
//            viewModel.onIntent(StorageIntent.RenameVoiceNote(voiceNote))
//        },
//    )
//}
//
//@Composable
//internal fun StorageDetailScreenContent(
//    navController: NavController,
//    folderId: UUID,
//    voiceNoteName: String,
//    onFolderNameChange: (String) -> Unit,
//    voiceNotes: List<VoiceNote>,
//    onRemoveVoiceNotes: (List<UUID>) -> Unit,
//    renameVoiceNote: (VoiceNote) -> Unit,
//) {
//
//    val selectedIds = remember(voiceNotes) { mutableStateListOf<UUID>() }
//
//    Surface(
//        modifier = Modifier.fillMaxSize()
//    ) {
//        Column(
//            modifier = Modifier
//                .fillMaxSize()
//                .padding(16.dp)
//        ) {
//
//            Text("Storage Detail Screen")
//
//            Text("Folder UUID: $folderId")
//
//            TextField(
//                value = voiceNoteName,
//                onValueChange = onFolderNameChange,
//                label = { Text("폴더 이름") }
//            )
//
//            Spacer(modifier = Modifier.height(12.dp))
//
//            LazyColumn(
//                modifier = Modifier.weight(1f),
//                verticalArrangement = Arrangement.spacedBy(12.dp)
//            ) {
//                items(voiceNotes) { note ->
//                    VoiceNoteRow(
//                        title = note.title,
//                        id = note.id,
//                        checked = selectedIds.contains(note.id),
//                        onCheckedChange = { checked ->
//                            if (checked) {
//                                if (!selectedIds.contains(note.id)) {
//                                    selectedIds.add(note.id)
//                                }
//                            } else {
//                                selectedIds.remove(note.id)
//                            }
//                        },
//                        renameVoiceNote = { voiceNote ->
//                            renameVoiceNote(voiceNote)
//                        },
//                        voiceNoteName = voiceNoteName
//                    )
//                }
//            }
//
//            Spacer(modifier = Modifier.height(16.dp))
//
//            Button(
//                onClick = {
//                    onRemoveVoiceNotes(selectedIds.toList())
//                    selectedIds.clear()
//                },
//                modifier = Modifier.fillMaxWidth(),
//                enabled = selectedIds.isNotEmpty()
//            ) {
//                Text("선택 삭제 테스트 (${selectedIds.size})")
//            }
//        }
//    }
//}
//
//@Composable
//fun VoiceNoteRow(
//    title: String,
//    id: UUID,
//    voiceNoteName: String,
//    checked: Boolean,
//    onCheckedChange: (Boolean) -> Unit,
//    renameVoiceNote: (VoiceNote) -> Unit
//) {
//    val voiceNote = VoiceNote(
//        id = id,
//        title = title,
//    )
//    Row(
//        modifier = Modifier
//            .fillMaxWidth()
//            .padding(vertical = 8.dp),
//        horizontalArrangement = Arrangement.Start
//    ) {
//        Checkbox(
//            checked = checked,
//            onCheckedChange = onCheckedChange
//        )
//
//        Text(
//            text = "$title $id",
//            modifier = Modifier.weight(1f)
//        )
//
//        Icon(
//            imageVector = Icons.Default.Brush,
//            contentDescription = "Rename voice note",
//            tint = Color.Red,
//            modifier = Modifier
//                .size(20.dp)
//                .padding(2.dp)
//                .clickable {
//                    renameVoiceNote(
//                        voiceNote.copy(
//                            title = voiceNoteName
//                        )
//                    )
//                }
//        )
//    }
//}
//
//@Preview
//@Composable
//fun StorageDetailScreenPreview() {
//    ChaGokTheme {
//        StorageDetailScreenContent(
//            navController = rememberNavController(),
//            folderId = UUID.fromString("00000000-0000-0000-0000-000000000000"),
//            voiceNotes = listOf(
//                VoiceNote(
//                    id = UUID.randomUUID(),
//                    title = "1"
//                ),
//                VoiceNote(
//                    id = UUID.randomUUID(),
//                    title = "2"
//                ),
//                VoiceNote(
//                    id = UUID.randomUUID(),
//                    title = "3"
//                ),
//            ),
//            onFolderNameChange = {},
//            voiceNoteName = "",
//            onRemoveVoiceNotes = {},
//            renameVoiceNote = {},
//        )
//    }
//}
