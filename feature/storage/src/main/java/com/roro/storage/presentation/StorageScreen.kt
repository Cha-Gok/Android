package com.roro.storage.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.roro.core.model.Folder
import com.roro.core.navigation.Routes
import com.roro.core.ui.theme.ChaGokTheme
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
        navController = navController,
        folderName = folderName,
        onFolderNameChange = { folderName = it },
        onCreateFolder = { folderName ->
            viewModel.onIntent(StorageIntent.CreateFolder(folderName = folderName))
        },
        onCreateVoice = { folderName ->
            viewModel.onIntent(StorageIntent.CreateDummyVoiceNote(folderName = folderName))
        },
        onRemoveFolder = { folder ->
            viewModel.onIntent(StorageIntent.MoveToTrash(folder = folder))
        },
        folders = folders,
        folderItem = folderItemCount,
        onRestoreFolder = { folder ->
            viewModel.onIntent(StorageIntent.RestoreFromTrash(folder = folder))
        },
        trashFolders = trashFolders,
    )
}

@Composable
internal fun StorageScreenContent(
    navController: NavController,
    folderName: String,
    onFolderNameChange: (String) -> Unit,
    onCreateFolder: (String) -> Unit,
    onCreateVoice: (String) -> Unit,
    onRemoveFolder: (Folder) -> Unit,
    onRestoreFolder: (Folder) -> Unit,
    folders: List<Folder>,
    folderItem: Map<UUID?, Int>,
    trashFolders: List<Folder>,
) {
    Surface(
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Text("Storage Screen")

            TextField(
                value = folderName,
                onValueChange = onFolderNameChange,
                label = { Text("폴더 이름") }
            )

            Row {
                Button(
                    onClick = { onCreateFolder(folderName.trim()) },
                    enabled = folderName.isNotBlank()
                ) {
                    Text("폴더 생성")
                }
                Button(
                    onClick = { onCreateVoice(folderName.trim()) },
                    enabled = folderName.isNotBlank()
                ) {
                    Text("녹음 파일 생성")
                }
            }

            Spacer(modifier = Modifier.height(24.dp))


            Text(
                text = "전체 노트", modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
            )


            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 30.dp),
                contentPadding = PaddingValues(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(folders) { folder ->
                    val count = folderItem[folder.id] ?: 0

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable{
                                navController.navigate(Routes.storageDetail(folderId =
                                folder.id.toString()))
                            }
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color(0xFFBABABB))
                            .border(
                                width = 1.dp,
                                color = Color(0xFF2B2B31),
                                shape = RoundedCornerShape(14.dp)
                            )
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Folder,
                            contentDescription = null,
                            tint = Color(0xFFEDEDED),
                            modifier = Modifier.size(20.dp)
                        )

                        Spacer(modifier = Modifier.width(12.dp))

                        Text(
                            text = "${folder.name} (${count})",
                            color = Color(0xFFF5F5F5),
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.weight(1f)
                        )
                        Icon(
                            imageVector = Icons.Default.Remove,
                            contentDescription = "Remove folder",
                            tint = Color.Red,
                            modifier = Modifier
                                .size(20.dp)
                                .padding(2.dp)
                                .clickable { onRemoveFolder(folder) }
                        )
                    }
                }
            }
            Text(
                text = "휴지통", modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
            )
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 30.dp),
                contentPadding = PaddingValues(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(trashFolders) { folder ->
                    val count = folderItem[folder.id] ?: 0
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color(0xFFBABABB))
                            .border(
                                width = 1.dp,
                                color = Color(0xFF2B2B31),
                                shape = RoundedCornerShape(14.dp)
                            )
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Folder,
                            contentDescription = null,
                            tint = Color(0xFFEDEDED),
                            modifier = Modifier.size(20.dp)
                        )

                        Spacer(modifier = Modifier.width(12.dp))

                        Text(
                            text = "${folder.name} ( $count )",
                            color = Color(0xFFF5F5F5),
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.weight(1f)
                        )
                        Icon(
                            imageVector = Icons.Default.Remove,
                            contentDescription = "Remove folder",
                            tint = Color.Red,
                            modifier = Modifier
                                .size(20.dp)
                                .padding(2.dp)
                                .clickable { onRestoreFolder(folder) }
                        )
                    }
                }
            }

        }
    }
}

@Preview
@Composable
fun StorageScreenPreview() {
    ChaGokTheme {
        StorageScreenContent(
            navController = rememberNavController(),
            folderName = "",
            onFolderNameChange = {},
            onCreateFolder = { /* preview no-op */ },
            onRemoveFolder = { /* preview no-op */ },
            folders = listOf(
                Folder(name = "ㄱㄱ"),
                Folder(name = "기본폴더"),
            ),
            onCreateVoice = { },
            onRestoreFolder = {},
            folderItem = mapOf(

            ),
            trashFolders = listOf(
                Folder(name = "휴지통1"),
                Folder(name = "휴지통2"),
            )
        )
    }
}