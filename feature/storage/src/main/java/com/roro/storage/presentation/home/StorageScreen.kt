package com.roro.storage.presentation.home

import androidx.compose.animation.core.animateDpAsState
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.roro.core.datastore.Language
import com.roro.core.model.VoiceNote
import com.roro.core.navigation.Routes
import com.roro.core.ui.component.ChaGokBackground
import com.roro.core.ui.component.ChaGokBox
import com.roro.core.ui.component.ChaGokBoxSmall
import com.roro.core.ui.component.ChaGokLanguageDialog
import com.roro.core.ui.component.ChaGokNoteList
import com.roro.core.ui.component.ChaGokSettingsDropdown
import com.roro.core.ui.component.ChaGokTopBar
import com.roro.core.ui.component.ChagokStartRecordFAB
import com.roro.core.ui.component.SummaryStatus
import com.roro.core.ui.theme.ChaGokTextStyle
import com.roro.core.ui.theme.TextPrimary
import com.roro.core.ui.theme.TextTertiary
import com.roro.core.util.formatDate
import com.roro.core.util.toast
import timber.log.Timber
import java.time.Instant.ofEpochMilli
import java.time.LocalDate
import java.time.ZoneId.systemDefault

@Composable
fun StorageScreen(
    navController: NavController,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    
    uiState.errorMessage?.let {
        Timber.d("text $it")
    }

    LaunchedEffect(Unit) {
        viewModel.onIntent(HomeIntent.Initialize)

        viewModel.effect.collect { effect ->
            when (effect) {
                is HomeEffect.ShowToast -> context.toast(effect.message)

                HomeEffect.NavigateToPrivate -> {
                    navController.navigate(Routes.STORAGE_FOLDER)
                }

                HomeEffect.NavigateToRecord -> {
                    navController.navigate(Routes.RECORDER)
                }

                HomeEffect.NavigateToTrash -> {
                    navController.navigate(Routes.TRASH)
                }

                HomeEffect.NavigateToSearch -> {
                    Timber.d("검색창 이동")
                }

                HomeEffect.NavigateToTos -> {
                    navController.navigate(Routes.TOS)
                }
            }
        }
    }

    // UI 데이터 전달
    StorageScreenContent(
        uiState = uiState,
        navController = navController,
        defaultCount = uiState.defaultFolderCount,
        privateFolderCount = uiState.privateFolderCount,
        trashFolderCount = uiState.trashCount,
        onClickFolderType = { type ->
            viewModel.onIntent(HomeIntent.ClickFolderType(type = type))
        },
        onSearchClick = { viewModel.onIntent(HomeIntent.ClickSearch) },
        onSettingClick = { viewModel.onIntent(HomeIntent.ClickSetting) },
        onTosClick = { viewModel.onIntent(HomeIntent.ClickTos) },
        onLanguageSelect = { viewModel.onIntent(HomeIntent.SelectLanguageOption(it)) },
        onConfirmDialog = { viewModel.onIntent(HomeIntent.ConfirmDialog) },
        onDismissDialog = { viewModel.onIntent(HomeIntent.DismissDialog) },
    )
}

@Composable
internal fun StorageScreenContent(
    uiState: HomeUiState,
    navController: NavController,
    defaultCount: Int,
    privateFolderCount: Int,
    trashFolderCount: Int,
    onClickFolderType: (DefaultFolderType) -> Unit,
    onSearchClick: () -> Unit,
    onSettingClick: () -> Unit,
    onTosClick: () -> Unit,
    onLanguageSelect: (Language) -> Unit, // 추가
    onConfirmDialog: () -> Unit,        // 추가
    onDismissDialog: () -> Unit,        // 추가
) {
    // 1. 스크롤 상태 기억
    val listState = rememberLazyListState()

    var isCollapsed by remember { mutableStateOf(false) }

    // 2. 중첩 스크롤 연결: 수치 변화가 아닌 '액션'에 반응하도록 설정
    val nestedScrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(
                available: Offset, source: NestedScrollSource
            ): Offset {
                // 사용자가 아래로 스크롤하면(available.y < 0) 바로 접음
                if (available.y < -1f && !isCollapsed) {
                    isCollapsed = true
                }

                // 사용자가 위로 스크롤할 때(available.y > 0)
                // 리스트가 맨 위(index == 0, offset == 0)에 도달했을 때만 펼침
                if (available.y > 1f && isCollapsed) {
                    if (listState.firstVisibleItemIndex == 0 && listState.firstVisibleItemScrollOffset == 0) {
                        isCollapsed = false
                    }
                }
                return Offset.Zero
            }
        }
    }

    ChaGokBackground {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .nestedScroll(nestedScrollConnection) // 연결
        ) {
            Column {
                var isMenuExpanded by remember { mutableStateOf(false) }
                ChaGokTopBar(title = "차곡", onFirstActionClick = { onSearchClick() }, onSecondActionClick = {
                    isMenuExpanded = true
                }, secondActionTrailingContent = {
                    ChaGokSettingsDropdown(expanded = isMenuExpanded, onDismissRequest = { isMenuExpanded = false }, onLanguageSettingClick = {
                        onSettingClick()
                        isMenuExpanded = false
                    }, onTosClick = {
                        onTosClick()
                        isMenuExpanded = false
                    })
                })
                FolderList(
                    isCollapse = isCollapsed,
                    selectedFolderType = uiState.selectedFolderType,
                    onFolderTypeClick = onClickFolderType,
                    defaultCount = defaultCount,
                    privateFolderCount = privateFolderCount,
                    trashFolderCount = trashFolderCount,
                )

                val displayList = uiState.displayVoiceNotes

                // 1. 날짜 그룹화 로직 (displayList가 Long 밀리초를 가진다고 가정)
                val groupedNotes = remember(displayList, uiState.selectedFolderType) {
                    if (uiState.selectedFolderType == DefaultFolderType.DEFAULT) {
                        val now = LocalDate.now()
                        displayList.groupBy { note ->
                            // Long 값을 LocalDate로 변환
                            val noteDate = ofEpochMilli(note.createdAt).atZone(systemDefault()).toLocalDate()

                            when {
                                noteDate.isEqual(now) -> "오늘"
                                noteDate.isAfter(now.minusDays(7)) -> "최근 7일"
                                else -> "이전"
                            }
                        }
                    } else {
                        emptyMap()
                    }
                }

                // list empty check
                if (displayList.isEmpty()) {
                    // 2. 남은 공간 전체를 차지하여 텍스트를 정중앙에 배치
                    Spacer(modifier = Modifier.height(96.dp))
                    Text(
                        text = "아직 녹음된 기록이 없습니다.\n녹음 버튼을 눌러 첫 기록을 시작해보세요.", color = TextTertiary, style = ChaGokTextStyle.Body1, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth()
                    )
                } else {
                    LazyColumn(
                        state = listState, modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f), contentPadding = PaddingValues(
                            top = 24.dp, start = 20.dp, end = 20.dp, bottom = 20.dp
                        ), verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // 2. 기본 폴더일 때만 섹션 헤더 표시
                        if (uiState.selectedFolderType == DefaultFolderType.DEFAULT && groupedNotes.isNotEmpty()) {
                            val sections = listOf("오늘", "최근 7일", "이전")
                            sections.forEach { section ->
                                val notesInSection = groupedNotes[section]
                                if (!notesInSection.isNullOrEmpty()) {
                                    item(key = section) {
                                        Text(
                                            text = section, style = ChaGokTextStyle.Title3, color = TextPrimary, modifier = Modifier.padding(vertical = 8.dp)
                                        )
                                    }
                                    items(
                                        items = notesInSection, key = { it.id }) { item ->
                                        FolderItemList(voiceNote = item)
                                    }
                                }
                            }
                        } else {
                            // 3. 최근 기록(RECENT) 등 다른 타입일 때 (일반 리스트)
                            items(
                                items = displayList, key = { it.id }) { item ->
                                FolderItemList(voiceNote = item)
                            }
                        }
                    }
                }
            }

            if (uiState.isDialog) {
                ChaGokLanguageDialog(
                    title = "녹음 언어 변경", selectedLanguage = uiState.selectedTempLanguage, onLanguageSelected = { lang ->
                        onLanguageSelect(lang)
                    }, dismissText = "취소", confirmText = "저장하기", onDismiss = onDismissDialog, onConfirm = onConfirmDialog
                )
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
    voiceNote: VoiceNote, modifier: Modifier = Modifier
) {
    ChaGokNoteList(
        title = voiceNote.title, summaryStatus = SummaryStatus.COMPLETED, onClick = { }, time = voiceNote.createdAt.formatDate(""), modifier = modifier.fillMaxWidth()
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
        targetValue = if (isCollapse) 48.dp else 120.dp, label = "FolderListHeight"
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
                    text = type.title, isSelected = type == selectedFolderType, icon = type.icon, onClick = { onFolderTypeClick(type) }, modifier = Modifier
                        .width(116.dp)
                        .height(38.dp)
                )
            } else {
                ChaGokBox(
                    text = type.title, count = when (type) {
                        DefaultFolderType.RECENT -> 0
                        DefaultFolderType.DEFAULT -> defaultCount
                        DefaultFolderType.PRIVATE -> privateFolderCount
                        DefaultFolderType.TRASH -> trashFolderCount
                    }, isSelected = type == selectedFolderType, icon = type.icon, onClick = {
                        onFolderTypeClick(type)
                    }, modifier = Modifier
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
        uiState = HomeUiState(
            selectedFolderType = DefaultFolderType.RECENT, isLoading = false, errorMessage = null
        ),
        onClickFolderType = {},
        defaultCount = 0,
        privateFolderCount = 0,
        trashFolderCount = 0,
        onSearchClick = { },
        onSettingClick = { },
        onLanguageSelect = { },
        onConfirmDialog = { },
        onDismissDialog = { },
        onTosClick = { },
    )
}

enum class DefaultFolderType(
    val title: String, val icon: ImageVector
) {
    RECENT("최근 기록", Icons.Default.Schedule), DEFAULT("기본 폴더", Icons.Outlined.Folder), PRIVATE("개인 폴더", Icons.Outlined.Folder), TRASH("휴지통", Icons.Outlined.Delete),
}

enum class FolderDialogType { CREATE, RENAME, DELETE_SELECTED }