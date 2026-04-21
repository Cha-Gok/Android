package com.roro.storage.presentation

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AudioFile
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material3.Button
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.modifier.modifierLocalOf
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.util.TimeUtils
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.roro.core.model.Folder
import com.roro.core.model.VoiceNote
import com.roro.core.navigation.Routes
import com.roro.core.ui.component.ChaGokBackground
import com.roro.core.ui.component.ChaGokBox
import com.roro.core.ui.component.ChaGokBoxSmall
import com.roro.core.ui.component.ChaGokNoteList
import com.roro.core.ui.component.ChaGokTopBar
import com.roro.core.ui.component.ChagokStartRecordFAB
import com.roro.core.ui.component.SummaryStatus
import com.roro.core.ui.theme.ChaGokTextStyle
import com.roro.core.ui.theme.ChaGokTheme
import com.roro.core.ui.theme.TextTertiary
import com.roro.core.util.formatDate
import com.roro.core.util.toast
import timber.log.Timber
import java.util.UUID

@Composable
fun StorageScreen(
    navController: NavController,
    viewModel: StorageViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val folders by viewModel.userFolders.collectAsState()
    val trashFolders by viewModel.trashFolders.collectAsState()
    val folderItemCount by viewModel.folderItemCountMap.collectAsState()
    val context = LocalContext.current
    var folderName by rememberSaveable { mutableStateOf("") }

    val defaultVoiceNote by viewModel.voiceNoteList.collectAsState()
    val trashVoiceNotes by viewModel.voiceNoteTrashList.collectAsState()
    val recentVoiceNotes by viewModel.voiceNoteRecentList.collectAsState()

    Timber.d("folderItemCount = ${folderItemCount.size}")
    Timber.d("trashFolders = ${trashFolders.size}")
    Timber.d("defaultVoiceNote = ${defaultVoiceNote.size}")


    if (uiState.isLoading) {
        Timber.d("로딩 중~")
    }

    uiState.errorMessage?.let {
        Timber.d("text $it")
    }

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is StorageEffect.ClearFolderInput -> {
                    folderName = ""
                }

                is StorageEffect.ShowToast -> context.toast(effect.message)
            }
        }
    }


    StorageScreenContent(
        uiState = uiState,
        navController = navController,
        defaultCount = defaultVoiceNote.size,
        privateFolderCount = folders.size,
        trashFolderCount = trashVoiceNotes.size + trashFolders.size,
        onClickFolderType = { type ->
            if (type == DefaultFolderType.PRIVATE) {
                viewModel.onIntent(StorageIntent.ClickFolderType(type = type))
                navController.navigate(Routes.storageFolder())
            } else {
                viewModel.onIntent(StorageIntent.ClickFolderType(type = type))
            }
        },

        )
}

@Composable
internal fun StorageScreenContent(
    uiState: StorageUiState,
    navController: NavController,
    defaultCount: Int,
    privateFolderCount: Int,
    trashFolderCount: Int,
    onClickFolderType: (DefaultFolderType) -> Unit,
) {
    // 1. 스크롤 상태 기억
    val listState = rememberLazyListState()

    // 2. 스크롤 여부 판단 (첫 번째 아이템이 화면 위로 넘어가면 collapsed 상태로 간주)
    val isScrolled by remember {
        derivedStateOf {
            listState.firstVisibleItemIndex > 0 || listState.firstVisibleItemScrollOffset > 0
        }
    }

    ChaGokBackground {
        Box(modifier = Modifier.fillMaxSize()) {
            Column {
                ChaGokTopBar(
                    title = "차곡",
                    onFirstActionClick = { },
                    onSecondActionClick = {},
                )
                FolderList(
                    isCollapse = isScrolled,
                    selectedFolderType = uiState.selectedFolderType,
                    onFolderTypeClick = onClickFolderType,
                    defaultCount = defaultCount,
                    privateFolderCount = privateFolderCount,
                    trashFolderCount = trashFolderCount,
                )

                val displayList = uiState.voiceNote

                // list empty check
                if (displayList.isEmpty()) {
                    // 2. 남은 공간 전체를 차지하여 텍스트를 정중앙에 배치
                    Spacer(modifier = Modifier.height(96.dp))
                    Text(
                        text = "아직 녹음된 기록이 없습니다.\n녹음 버튼을 눌러 첫 기록을 시작해보세요.",
                        color = TextTertiary,
                        style = ChaGokTextStyle.Body1,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                } else {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentPadding = PaddingValues(top = 32.dp, start = 20.dp, end = 20.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(
                            items = displayList,
                            key = { it.id }
                        ) { item ->
                            FolderItemList(voiceNote = item)
                        }
                    }
                }


            }
            // 5. 플로팅 버튼을 Box의 오른쪽 하단에 배치
            ChagokStartRecordFAB(
                onClick = {
                    navController.navigate(Routes.RECORDER)
                },
                modifier = Modifier
                    .align(Alignment.BottomEnd) // 우측 하단 정렬
                    .padding(end = 42.dp, bottom = 64.dp), // 화면 끝에서 여백
            )
        }
    }
}

@Composable
fun FolderItemList(
    voiceNote: VoiceNote,
    modifier: Modifier = Modifier
) {
    ChaGokNoteList(
        title = voiceNote.title,
        summaryStatus = SummaryStatus.COMPLETED,
        onClick = { },
        time = voiceNote.createdAt.formatDate(""),
        modifier = modifier.fillMaxWidth()
    )
}

@Composable
fun FolderList(
    isCollapse: Boolean,
    defaultCount: Int,
    privateFolderCount: Int,
    trashFolderCount: Int,
    selectedFolderType: DefaultFolderType,
    onFolderTypeClick: (DefaultFolderType) -> Unit,
    modifier: Modifier = Modifier
) {
    // 높이 처리
    val height by animateDpAsState(
        targetValue = if (isCollapse) 48.dp else 120.dp,
        label = "FolderListHeight"
    )
    LazyRow(
        modifier = modifier
            .fillMaxWidth()
            .height(height),
        contentPadding = PaddingValues(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        items(DefaultFolderType.entries) { type ->
            if (isCollapse) {
                ChaGokBoxSmall(
                    text = type.title,
                    isSelected = type == selectedFolderType,
                    icon = type.icon,
                    onClick = { onFolderTypeClick(type) },
                    modifier = Modifier
                        .width(116.dp)
                        .height(38.dp)
                )
            } else {
                ChaGokBox(
                    text = type.title,
                    count = when (type) {
                        DefaultFolderType.RECENT -> 0
                        DefaultFolderType.DEFAULT -> defaultCount
                        DefaultFolderType.PRIVATE -> privateFolderCount
                        DefaultFolderType.TRASH -> trashFolderCount
                    },
                    isSelected = type == selectedFolderType,
                    icon = type.icon,
                    onClick = {
                        onFolderTypeClick(type)
                    },
                    modifier = Modifier
                        .width(92.dp)
                        .height(120.dp)
                )
            }
        }
    }
}

@Preview
@Composable
fun StorageScreenPreview() {
    StorageScreenContent(
        navController = rememberNavController(),
        uiState = StorageUiState(
            selectedFolderType = DefaultFolderType.RECENT,
            isLoading = false,
            errorMessage = null
        ),
        onClickFolderType = {},
        defaultCount = 0,
        privateFolderCount = 0,
        trashFolderCount = 0,
    )
}

enum class DefaultFolderType(
    val title: String,
    val icon: ImageVector
) {
    RECENT("최근 기록", Icons.Default.Schedule),
    DEFAULT("기본 폴더", Icons.Outlined.Folder),
    PRIVATE("개인 폴더", Icons.Outlined.Folder),
    TRASH("휴지통", Icons.Outlined.Delete),
}

enum class FolderDialogType { CREATE, RENAME, DELETE_SELECTED }