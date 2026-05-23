package com.roro.storage.presentation.trash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.roro.core.domain.model.TrashItem
import com.roro.core.domain.model.FileType
import com.roro.core.util.formatTime
import com.roro.core.util.toDeletedAtString
import com.roro.storage.domain.ObserveTrashFolderItemCountUseCase
import com.roro.storage.domain.ObserveTrashFoldersUseCase
import com.roro.storage.domain.ObserveTrashVoiceNotesUseCase
import com.roro.storage.domain.RemoveFolderUseCase
import com.roro.storage.domain.RemoveVoiceNoteUseCase
import com.roro.storage.domain.RestoreFolderUseCase
import com.roro.storage.domain.RestoreVoiceNoteUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class TrashViewModel @Inject constructor(
    private val observeTrashFoldersUseCase: ObserveTrashFoldersUseCase,
    private val observeFolderItemCount: ObserveTrashFolderItemCountUseCase,
    private val observeTrashVoiceNoteUseCase: ObserveTrashVoiceNotesUseCase,
    private val restoreVoiceNoteUseCase: RestoreVoiceNoteUseCase,
    private val restoreFolderUseCase: RestoreFolderUseCase,
    private val removeVoiceNoteUseCase: RemoveVoiceNoteUseCase,
    private val removeFolderUseCase: RemoveFolderUseCase
) : ViewModel() {
    private val _uiState = MutableStateFlow(TrashUiState(isLoading = true))
    val uiState: StateFlow<TrashUiState> = _uiState.asStateFlow()

    private val _effect = MutableSharedFlow<TrashEffect>()
    val effect = _effect.asSharedFlow()

    init {
        initialize()
    }

    fun onIntent(intent: TrashIntent) {
        when (intent) {
            // 1. 탑바 & 메뉴
            TrashIntent.ClickSearch -> emitEffect(TrashEffect.NavigateToSearch)
            TrashIntent.ClickMore -> _uiState.update { it.copy(isMenuExpanded = true) }
            TrashIntent.DismissMoreMenu -> _uiState.update { it.copy(isMenuExpanded = false) }

            // 2. 드롭다운 항목
            TrashIntent.ClickSelectMode -> _uiState.update {
                it.copy(isSelectMode = true, isMenuExpanded = false)
            }

            TrashIntent.ClickSelectAllItem -> _uiState.update { state ->
                state.copy(
                    isSelectMode = true,
                    isMenuExpanded = false,
                    selectedIds = state.item.map { it.id }.toSet()
                )
            }

            // [수정] 휴지통 비우기 클릭 시 다이얼로그 타입 설정
            TrashIntent.ClickEmptyTrash -> _uiState.update {
                it.copy(
                    isDialog = true,
                    activeDialogType = TrashDialogType.EMPTY_TRASH, // Enum 추가 필요
                    isMenuExpanded = false
                )
            }

            // 3. 선택 모드 액션
            TrashIntent.ClickCloseSelectMode -> _uiState.update {
                it.copy(isSelectMode = false, selectedIds = emptySet())
            }

            is TrashIntent.ClickSelectItem -> toggleSelection(intent.id)
            TrashIntent.ClickRestoreItems -> handleRestore()

            // [수정] 삭제 버튼 클릭 시 바로 삭제하지 않고 다이얼로그 띄우기
            TrashIntent.ClickRemoveItems -> {
                if (_uiState.value.selectedIds.isNotEmpty()) {
                    _uiState.update {
                        it.copy(isDialog = true, activeDialogType = TrashDialogType.REMOVE_SELECTED)
                    }
                }
            }

            // 4. 다이얼로그 액션
            TrashIntent.DialogCancel -> _uiState.update {
                it.copy(isDialog = false, activeDialogType = null)
            }

            TrashIntent.DialogConfirm -> {
                when (_uiState.value.activeDialogType) {
                    TrashDialogType.EMPTY_TRASH -> emptyTrashAll()
                    TrashDialogType.REMOVE_SELECTED -> handleRemove()
                    null -> _uiState.update { it.copy(isDialog = false) }
                }
            }

            // 5. 일반 클릭
            is TrashIntent.ClickFolder -> emitEffect(TrashEffect.NavigateToDetail)
            is TrashIntent.ClickVoiceNote -> emitEffect(TrashEffect.NavigateToDetail)
        }
    }

    private fun toggleSelection(id: UUID) {
        _uiState.update { state ->
            val newSelected = if (state.selectedIds.contains(id)) {
                state.selectedIds - id
            } else {
                state.selectedIds + id
            }
            state.copy(selectedIds = newSelected)
        }
    }

    // [로직] 실제 선택 삭제 수행
    private fun handleRemove() {
        val ids = _uiState.value.selectedIds
        viewModelScope.launch {
            try {
                val itemsToRemove = _uiState.value.item.filter { it.id in ids }
                itemsToRemove.forEach { item ->
                    when (item.type) {
                        FileType.FOLDER -> removeFolderUseCase(item.id)
                        FileType.VOICE_NOTE -> removeVoiceNoteUseCase(item.id)
                    }
                }

                _uiState.update {
                    it.copy(isDialog = false, activeDialogType = null, isSelectMode = false, selectedIds = emptySet())
                }
                _effect.emit(TrashEffect.ShowToast("${ids.size}개의 항목이 영구 삭제되었습니다."))
            } catch (e: Exception) {
                Timber.e(e, "항목 삭제 중 오류 발생")
                _effect.emit(TrashEffect.ShowToast("삭제에 실패했습니다."))
            }
        }
    }

    private fun handleRestore() {
        val ids = _uiState.value.selectedIds
        if (ids.isEmpty()) return

        viewModelScope.launch {
            try {
                val itemsToRestore = _uiState.value.item.filter { it.id in ids }
                itemsToRestore.forEach { item ->
                    when (item.type) {
                        FileType.FOLDER -> restoreFolderUseCase(item.id)
                        FileType.VOICE_NOTE -> restoreVoiceNoteUseCase(item.id)
                    }
                }

                _uiState.update {
                    it.copy(isSelectMode = false, selectedIds = emptySet())
                }
                _effect.emit(TrashEffect.ShowToast("${ids.size}개의 항목이 복원되었습니다."))
            } catch (e: Exception) {
                Timber.e(e, "항목 복원 중 오류 발생")
                _effect.emit(TrashEffect.ShowToast("복원에 실패했습니다."))
            }
        }
    }

    // [로직] 휴지통 전체 비우기 수행
    private fun emptyTrashAll() {
        viewModelScope.launch {
            try {
                val allItems = _uiState.value.item
                allItems.forEach { item ->
                    when (item.type) {
                        FileType.FOLDER -> removeFolderUseCase(item.id)
                        FileType.VOICE_NOTE -> removeVoiceNoteUseCase(item.id)
                    }
                }
                _uiState.update { it.copy(isDialog = false, activeDialogType = null) }
                _effect.emit(TrashEffect.ShowToast("휴지통을 비웠습니다."))
            } catch (e: Exception) {
                _effect.emit(TrashEffect.ShowToast("휴지통 비우기에 실패했습니다."))
            }
        }
    }

    private fun emitEffect(effect: TrashEffect) {
        viewModelScope.launch { _effect.emit(effect) }
    }

    private fun initialize() {
        viewModelScope.launch {
            combine(
                observeTrashFoldersUseCase(),
                observeTrashVoiceNoteUseCase(),
                observeFolderItemCount()
            ) { folder, voiceNote, folderCount ->
                val folderItems = folder.map { f ->
                    val countInfo = folderCount.find { it.id == f.id }
                    TrashItem(
                        id = f.id,
                        title = f.name,
                        deletedAt = f.deletedAt ?: 0L,
                        firstText = "${countInfo?.noteCount ?: 0}개 항목",
                        secondText = (f.deletedAt ?: 0L).toDeletedAtString(),
                        type = FileType.FOLDER
                    )
                }

                val voiceNoteItems = voiceNote.map { note ->
                    TrashItem(
                        id = note.id,
                        title = note.title,
                        deletedAt = note.deletedAt ?: 0L,
                        firstText = note.createdAt.formatTime(),
                        secondText = (note.deletedAt ?: 0L).toDeletedAtString(),
                        type = FileType.VOICE_NOTE,
                    )
                }

                (folderItems + voiceNoteItems).sortedByDescending { it.deletedAt }
            }.collect { list ->
                _uiState.update {
                    it.copy(
                        item = list,
                        isLoading = false
                    )
                }
            }
        }
    }
}