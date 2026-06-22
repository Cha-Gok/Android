package com.roro.recorder.presentation.viewModel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import com.roro.core.domain.model.SummaryStatus
import com.roro.core.domain.model.FileListSheetMode
import com.roro.core.domain.model.FolderItem
import com.roro.core.util.toUUIDOrNull
import com.roro.recorder.domain.usecase.CreateUserFolderUseCase
import com.roro.recorder.domain.usecase.GetVoiceNoteUseCase
import com.roro.recorder.domain.usecase.MoveToFolderUseCase
import com.roro.recorder.domain.usecase.MoveToTrashVoiceNotesUseCase
import com.roro.recorder.domain.usecase.ObserveFolders
import com.roro.recorder.domain.usecase.RegenerateSummaryUseCase
import com.roro.recorder.domain.usecase.UpdateVoiceNoteTitleUseCase
import com.roro.recorder.domain.usecase.VoiceNoteResult
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import timber.log.Timber
import java.io.File
import java.util.UUID
import javax.inject.Inject

sealed class RecordResultUiState {
    object Loading : RecordResultUiState()

    interface ContentState {
        val result: VoiceNoteResult
        val isMenuExpanded: Boolean
        val isBottomSheet: Boolean
        val sheetMode: FileListSheetMode
        val folderList: List<FolderItem>
        val selectedFolder: FolderItem?
        val createFolderName: String
        val errorMessage: String?
        val showDeleteDialog: Boolean
    }

    data class Success(
        override val result: VoiceNoteResult,
        override val isMenuExpanded: Boolean = false,
        override val isBottomSheet: Boolean = false,
        override val sheetMode: FileListSheetMode = FileListSheetMode.FOLDER_LIST,
        override val folderList: List<FolderItem> = emptyList(),
        override val selectedFolder: FolderItem? = null,
        override val createFolderName: String = "",
        override val errorMessage: String? = null,
        override val showDeleteDialog: Boolean = false
    ) : RecordResultUiState(), ContentState

    data class NoSpeech(
        override val result: VoiceNoteResult,
        override val isMenuExpanded: Boolean = false,
        override val isBottomSheet: Boolean = false,
        override val sheetMode: FileListSheetMode = FileListSheetMode.FOLDER_LIST,
        override val folderList: List<FolderItem> = emptyList(),
        override val selectedFolder: FolderItem? = null,
        override val createFolderName: String = "",
        override val errorMessage: String? = null,
        override val showDeleteDialog: Boolean = false
    ) : RecordResultUiState(), ContentState   // STT 없음

    data class SummaryError(
        override val result: VoiceNoteResult,
        override val isMenuExpanded: Boolean = false,
        override val isBottomSheet: Boolean = false,
        override val sheetMode: FileListSheetMode = FileListSheetMode.FOLDER_LIST,
        override val folderList: List<FolderItem> = emptyList(),
        override val selectedFolder: FolderItem? = null,
        override val createFolderName: String = "",
        override val errorMessage: String? = null,
        override val showDeleteDialog: Boolean = false
    ) : RecordResultUiState(), ContentState // 요약 실패

    data class SummaryGenerating(
        override val result: VoiceNoteResult,
        override val isMenuExpanded: Boolean = false,
        override val isBottomSheet: Boolean = false,
        override val sheetMode: FileListSheetMode = FileListSheetMode.FOLDER_LIST,
        override val folderList: List<FolderItem> = emptyList(),
        override val selectedFolder: FolderItem? = null,
        override val createFolderName: String = "",
        override val errorMessage: String? = null,
        override val showDeleteDialog: Boolean = false
    ) : RecordResultUiState(), ContentState

    data class Error(val message: String) : RecordResultUiState()
}

enum class SummaryDisplayState {
    Generating, Success, Error, NoSpeech, Insufficient
}

data class PlayerUiState(
    val currentPositionMs: Long = 0L, val durationMs: Long = 0L, val isPlaying: Boolean = false
)

sealed interface RecordResultIntent {
    // 1. 초기화 및 데이터 로드
    data class Initialize(val voiceNoteId: String) : RecordResultIntent

    // 2. 제목 편집 관련 (기존 기능 유지)
    data object ClickEditTitle : RecordResultIntent
    data class UpdateTitle(val newTitle: String) : RecordResultIntent
    data object ConfirmEditTitle : RecordResultIntent

    // 3. 더보기 메뉴 제어 (FileListIntent 스타일)
    data class ShowMoreMenu(val isShow: Boolean) : RecordResultIntent
    data object ClickMoveToFolder : RecordResultIntent // 기록 이동하기 클릭 시 호출

    // 4. 파일 이동 바텀 시트 (FileListIntent 로직 이식)
    data class ShowBottomSheet(val isShow: Boolean) : RecordResultIntent
    data class SelectTargetFolder(val folder: FolderItem) : RecordResultIntent
    data object ConfirmMove : RecordResultIntent // 최종 이동 확정

    // 5. 바텀시트 내 폴더 생성 로직 (FileListIntent 로직 이식)
    data class ChangeSheetMode(val mode: FileListSheetMode) : RecordResultIntent
    data class UpdateNewFolderName(val name: String) : RecordResultIntent
    data object ConfirmCreateFolder : RecordResultIntent

    // 6. 삭제 다이얼로그 (FileListIntent 로직 이식)
    data class ShowDeleteDialog(val isShow: Boolean) : RecordResultIntent
    data object ConfirmDelete : RecordResultIntent
}

sealed interface RecordResultEffect {
    data class ShowToast(val message: String) : RecordResultEffect
    data object NavigateBack : RecordResultEffect
    // 필요 시 편집 완료 후 알림 등 추가
}

@HiltViewModel
class RecordResultViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val getVoiceNoteUseCase: GetVoiceNoteUseCase,
    private val regenerateSummaryUseCase: RegenerateSummaryUseCase,
    private val updateVoiceNoteTitleUseCase: UpdateVoiceNoteTitleUseCase,  // ✅
    private val observeFolders: ObserveFolders,
    private val createUserFolderUseCase: CreateUserFolderUseCase,
    private val moveToTrashVoiceNotesUseCase: MoveToTrashVoiceNotesUseCase,
    private val moveToFolderUseCase: MoveToFolderUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<RecordResultUiState>(RecordResultUiState.Loading)
    val uiState: StateFlow<RecordResultUiState> = _uiState.asStateFlow()

    private val _playerUiState = MutableStateFlow(PlayerUiState())
    val playerUiState: StateFlow<PlayerUiState> = _playerUiState.asStateFlow()

    private var exoPlayer: ExoPlayer? = null

    private var _currentVoiceNoteId: UUID? = null  // 재생성 시 사용
    private var summaryRefreshJob: Job? = null

    private val _effect = MutableSharedFlow<RecordResultEffect>()
    val effect = _effect.asSharedFlow()

    // onIntent
    fun onIntent(intent: RecordResultIntent) {
        // Success, NoSpeech, SummaryError 상태인 경우에만 로직 수행
        val currentState = _uiState.value as? RecordResultUiState.ContentState

        when (intent) {
            // 1. 초기화
            is RecordResultIntent.Initialize -> {
                load(intent.voiceNoteId)
            }

            // 2. 제목 편집 (모든 ContentState에서 작동하도록 내부 로직 수정 필요)
            is RecordResultIntent.ClickEditTitle -> {
                startTitleEdit()
            }

            is RecordResultIntent.UpdateTitle -> {
                onTitleChange(intent.newTitle)
            }

            is RecordResultIntent.ConfirmEditTitle -> {
                confirmTitleEdit()
            }

            // 3. 더보기 메뉴 제어
            is RecordResultIntent.ShowMoreMenu -> {
                updateContentState { it.copyAny(isMenuExpanded = intent.isShow) }
            }

            is RecordResultIntent.ClickMoveToFolder -> {
                onMoveClick()
            }

            // 4. 바텀시트 제어
            is RecordResultIntent.ShowBottomSheet -> {
                updateContentState { it.copyAny(isBottomSheet = intent.isShow) }
            }

            is RecordResultIntent.SelectTargetFolder -> {
                updateContentState { it.copyAny(selectedFolder = intent.folder) }
            }

            is RecordResultIntent.ConfirmMove -> {
                currentState?.selectedFolder?.let { moveVoiceNote(it.id.toUUIDOrNull()) }
            }

            // 5. 바텀시트 내 모드 변경 및 폴더 생성
            is RecordResultIntent.ChangeSheetMode -> {
                updateContentState { it.copyAny(sheetMode = intent.mode, errorMessage = null) }
            }

            is RecordResultIntent.UpdateNewFolderName -> {
                updateContentState { it.copyAny(createFolderName = intent.name) }
            }

            is RecordResultIntent.ConfirmCreateFolder -> {
                confirmCreateFolder()
            }

            // 6. 삭제 다이얼로그 제어
            is RecordResultIntent.ShowDeleteDialog -> {
                updateContentState { it.copyAny(showDeleteDialog = intent.isShow) }
            }

            is RecordResultIntent.ConfirmDelete -> {
                removeVoiceNote()
            }
        }
    }

    // ── VoiceNote 로드 ────────────────────────────────────────────────────────

    fun load(voiceNoteId: String) {
        viewModelScope.launch {
            try {
                val id = UUID.fromString(voiceNoteId)
                _currentVoiceNoteId = id
                val result = getVoiceNoteUseCase(id)
                if (result != null) {
                    _uiState.value = result.toUiState()
                    if (result.summaryStatus == SummaryStatus.GENERATING) {
                        stopSummaryRefresh()
                        startSummaryRefresh(id)
                    } else {
                        stopSummaryRefresh()
                    }
                    preparePlayer(result.audioPath)
                } else {
                    _uiState.value = RecordResultUiState.Error("데이터를 찾을 수 없어요")
                }
            } catch (e: Exception) {
                _uiState.value = RecordResultUiState.Error(e.message ?: "오류가 발생했어요")
            }
        }
    }

    private fun VoiceNoteResult.toUiState(): RecordResultUiState {
        return when {
            sttText.isBlank() -> RecordResultUiState.NoSpeech(this)
            summaryStatus == SummaryStatus.INSUFFICIENT -> RecordResultUiState.NoSpeech(this)
            summaryStatus == SummaryStatus.GENERATING -> RecordResultUiState.SummaryGenerating(this)
            summaryStatus == SummaryStatus.FAIL -> RecordResultUiState.SummaryError(this)
            summaryText.isBlank() -> RecordResultUiState.SummaryError(this)
            else -> RecordResultUiState.Success(this)
        }
    }

    private fun startSummaryRefresh(voiceNoteId: UUID) {
        if (summaryRefreshJob?.isActive == true) return
        summaryRefreshJob = viewModelScope.launch {
            while (isActive) {
                delay(1500L)
                val result = getVoiceNoteUseCase(voiceNoteId) ?: continue
                if (result.summaryStatus != SummaryStatus.GENERATING) {
                    _uiState.value = result.toUiState()
                    stopSummaryRefresh()
                    return@launch
                }
            }
        }
    }

    private fun stopSummaryRefresh() {
        summaryRefreshJob?.cancel()
        summaryRefreshJob = null
    }

    // ── ExoPlayer 초기화 (Main thread 필수) ──────────────────────────────────

    fun preparePlayer(audioPath: String) {
        if (audioPath.isBlank()) return

        viewModelScope.launch(Dispatchers.Main) {  // ✅ ExoPlayer는 Main thread에서만 생성/제어
            exoPlayer?.release()
            exoPlayer = ExoPlayer.Builder(context).build().also { player ->
                val mediaItem = MediaItem.fromUri(File(audioPath).toURI().toString())
                player.setMediaItem(mediaItem)
                player.prepare()
            }
            startPositionUpdater()
        }
    }

    // 재생 위치 100ms마다 업데이트, isActive로 코루틴 종료 시 자동 중단
    private fun startPositionUpdater() {
        viewModelScope.launch(Dispatchers.Main) {
            while (isActive) {
                val player = exoPlayer
                if (player != null) {
                    _playerUiState.value = PlayerUiState(
                        currentPositionMs = player.currentPosition, durationMs = player.duration.coerceAtLeast(0L), isPlaying = player.isPlaying
                    )
                }
                delay(100L)
            }
        }
    }

    // ── 플레이어 컨트롤 (모두 Main thread) ───────────────────────────────────

    fun play() {
        viewModelScope.launch(Dispatchers.Main) { exoPlayer?.play() }
    }

    fun pause() {
        viewModelScope.launch(Dispatchers.Main) { exoPlayer?.pause() }
    }

    fun seekTo(positionMs: Long) {
        viewModelScope.launch(Dispatchers.Main) { exoPlayer?.seekTo(positionMs) }
    }

    fun rewind5() {
        viewModelScope.launch(Dispatchers.Main) {
            val player = exoPlayer ?: return@launch
            player.seekTo((player.currentPosition - 5_000L).coerceAtLeast(0L))
        }
    }

    fun forward5() {
        viewModelScope.launch(Dispatchers.Main) {
            val player = exoPlayer ?: return@launch
            player.seekTo((player.currentPosition + 5_000L).coerceAtMost(player.duration))
        }
    }

    // ── 제목 편집 ─────────────────────────────────────────────────────────────

    private val _isTitleEditing = MutableStateFlow(false)
    val isTitleEditing: StateFlow<Boolean> = _isTitleEditing.asStateFlow()

    private val _editingTitle = MutableStateFlow("")
    val editingTitle: StateFlow<String> = _editingTitle.asStateFlow()

    // 제목 탭 → 편집 모드 진입
    fun startTitleEdit() {
        val current = _uiState.value as? RecordResultUiState.ContentState ?: return
        _editingTitle.value = current.result.title
        _isTitleEditing.value = true
    }

    // 편집 중 텍스트 변경
    fun onTitleChange(value: String) {
        _editingTitle.value = value
    }

    // 완료 버튼 or 키보드 내리기 → 저장 후 편집 모드 종료
    fun confirmTitleEdit() {
        val current = _uiState.value as? RecordResultUiState.ContentState ?: return
        val voiceNoteId = _currentVoiceNoteId ?: return
        _isTitleEditing.value = false

        viewModelScope.launch {
            val finalTitle = updateVoiceNoteTitleUseCase(
                voiceNoteId = voiceNoteId, newTitle = _editingTitle.value, currentTitle = current.result.title
            )
            // UiState 제목 갱신 + updatedAt 갱신
            updateContentState {
                it.copyAny(
                    result = current.result.copy(
                        title = finalTitle,
                        updatedAt = System.currentTimeMillis()
                    )
                )
            }
        }
    }

    // ── 재생성 ────────────────────────────────────────────────────────────────

    private val _isRegenerating = MutableStateFlow(false)
    val isRegenerating: StateFlow<Boolean> = _isRegenerating.asStateFlow()

    // 스크립트 수정 후 배너 + 빨간 점 표시용
    private val _isScriptModified = MutableStateFlow(false)
    val isScriptModified: StateFlow<Boolean> = _isScriptModified.asStateFlow()

    // ScriptEditScreen 저장 완료 후 호출
    fun onScriptSaved() {
        _isScriptModified.value = true
    }

    fun regenerateSummary() {
        val current = _uiState.value as? RecordResultUiState.ContentState ?: return

        viewModelScope.launch {
            _isRegenerating.value = true
            try {
                val regenerated = regenerateSummaryUseCase(
                    voiceNoteId = _currentVoiceNoteId ?: return@launch, sttText = current.result.sttText
                )
                _uiState.value = RecordResultUiState.Success(
                    current.result.copy(
                        summaryText = regenerated.summaryText, keywords = regenerated.keywords
                    )
                )
                _isScriptModified.value = false  // 재생성 완료 시 배너 + 점 사라짐
            } catch (e: Exception) {
                Timber.tag("RecordResultVM").e(e, "재생성 실패")
            } finally {
                _isRegenerating.value = false
            }
        }
    }

    // ── 정리 ─────────────────────────────────────────────────────────────────

    override fun onCleared() {
        super.onCleared()
        stopSummaryRefresh()
        exoPlayer?.release()
        exoPlayer = null
    }

    // ── 더보기 메뉴 ─────────────────────────────────────────────────────────────────
    fun toggleMenu(expanded: Boolean) {
        updateContentState { it.copyAny(isMenuExpanded = expanded) }
    }

    // 1. 기록 이동하기 클릭 (메인 로직)
    private fun onMoveClick() {
        val currentState = _uiState.value as? RecordResultUiState.ContentState ?: return

        viewModelScope.launch {
            try {
                observeFolders().collect { folders ->
                    updateContentState {
                        it.copyAny(
                            isBottomSheet = true, folderList = folders, sheetMode = FileListSheetMode.FOLDER_LIST, isMenuExpanded = false
                        )
                    }
                }
            } catch (e: Exception) {
                Timber.e(e, "폴더 목록 로드 실패")
            }
        }
    }


    // 2. 편집하기 클릭 (기존의 제목 편집 로직 연결)
    fun onEditClick() {
        toggleMenu(false)
        startTitleEdit()
    }

    // ── 바텀시트 제어 및 폴더 이동 ──────────────────────────────────────────────────

    fun hideBottomSheet() {
        updateContentState { it.copyAny(isBottomSheet = false) }
    }

    // ── 폴더 이동 및 생성 로직 ──────────────────────────────────────────────────


    private fun moveVoiceNote(targetFolderId: UUID?) {
        val voiceNoteId = _currentVoiceNoteId ?: return
        val folderIdString = targetFolderId?.toString() ?: return

        viewModelScope.launch {
            try {
                // ✅ UseCase 형식에 맞춤: List<UUID>와 String 전달
                moveToFolderUseCase(
                    voiceNoteId = listOf(voiceNoteId), folderId = folderIdString
                )

                updateContentState { it.copyAny(isBottomSheet = false) }
                _effect.emit(RecordResultEffect.ShowToast("기록을 이동했어요"))

                // 이동 후 데이터 갱신 (폴더 정보 등)
                load(voiceNoteId.toString())
            } catch (e: Exception) {
                Timber.e(e, "이동 실패")
            }
        }
    }

    // 새 폴더 생성 확정

    private fun confirmCreateFolder() {
        val currentState = _uiState.value as? RecordResultUiState.ContentState ?: return
        val folderName = currentState.createFolderName

        if (folderName.isBlank()) return

        viewModelScope.launch {
            try {
                createUserFolderUseCase(folderName)
                // 생성 성공 시 다시 폴더 목록으로 전환
                updateContentState {
                    it.copyAny(
                        sheetMode = FileListSheetMode.FOLDER_LIST,
                        createFolderName = ""
                    )
                }
            } catch (e: Exception) {
                updateContentState { it.copyAny(errorMessage = "폴더 생성에 실패했어요") }
            }
        }
    }

    // 기록 삭제 (휴지통 이동)
    private fun removeVoiceNote() {
        val voiceNoteId = _currentVoiceNoteId ?: return
        viewModelScope.launch {
            try {
                moveToTrashVoiceNotesUseCase(listOf(voiceNoteId))
                _effect.emit(RecordResultEffect.NavigateBack)
                _effect.emit(RecordResultEffect.ShowToast("기록을 삭제했어요"))
            } catch (e: Exception) {
                Timber.e(e, "삭제 실패")
            }
        }
    }

    private fun updateContentState(transform: (RecordResultUiState.ContentState) -> RecordResultUiState) {
        val current = _uiState.value as? RecordResultUiState.ContentState ?: return
        _uiState.value = transform(current)
    }

    private fun RecordResultUiState.ContentState.copyAny(
        isMenuExpanded: Boolean = this.isMenuExpanded,
        isBottomSheet: Boolean = this.isBottomSheet,
        sheetMode: FileListSheetMode = this.sheetMode,
        folderList: List<FolderItem> = this.folderList,
        selectedFolder: FolderItem? = this.selectedFolder,
        createFolderName: String = this.createFolderName,
        errorMessage: String? = this.errorMessage,
        showDeleteDialog: Boolean = this.showDeleteDialog,
        result: VoiceNoteResult = this.result // 결과 데이터도 복사 가능하도록 추가
    ): RecordResultUiState {
        return when (this) {
            is RecordResultUiState.Success -> this.copy(
                isMenuExpanded = isMenuExpanded,
                isBottomSheet = isBottomSheet,
                sheetMode = sheetMode,
                folderList = folderList,
                selectedFolder = selectedFolder,
                createFolderName = createFolderName,
                errorMessage = errorMessage,
                showDeleteDialog = showDeleteDialog,
                result = result
            )
            // ✅ NoSpeech에서도 모든 필드를 복사해줘야 합니다!
            is RecordResultUiState.NoSpeech -> this.copy(
                isMenuExpanded = isMenuExpanded,
                isBottomSheet = isBottomSheet,
                sheetMode = sheetMode,
                folderList = folderList,
                selectedFolder = selectedFolder,
                createFolderName = createFolderName,
                errorMessage = errorMessage,
                showDeleteDialog = showDeleteDialog,
                result = result
            )

            is RecordResultUiState.SummaryError -> this.copy(
                isMenuExpanded = isMenuExpanded,
                isBottomSheet = isBottomSheet,
                sheetMode = sheetMode,
                folderList = folderList,
                selectedFolder = selectedFolder,
                createFolderName = createFolderName,
                errorMessage = errorMessage,
                showDeleteDialog = showDeleteDialog,
                result = result
            )

            is RecordResultUiState.SummaryGenerating -> this.copy(
                isMenuExpanded = isMenuExpanded,
                isBottomSheet = isBottomSheet,
                sheetMode = sheetMode,
                folderList = folderList,
                selectedFolder = selectedFolder,
                createFolderName = createFolderName,
                errorMessage = errorMessage,
                showDeleteDialog = showDeleteDialog,
                result = result
            )

            else -> this as RecordResultUiState
        }
    }
}
