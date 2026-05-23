package com.roro.storage.presentation.search

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import com.roro.core.domain.model.FileType
import com.roro.core.ui.component.ChaGokBackground
import com.roro.core.ui.component.ChaGokItemBox
import com.roro.core.ui.component.ChaGokSearchBar
import com.roro.core.ui.theme.ChaGokTextStyle
import com.roro.core.ui.theme.PrimaryColor
import com.roro.core.ui.theme.TextSecondary

@Composable
fun SearchScreen(
    searchType: String, navController: NavController, viewModel: SearchViewModel = hiltViewModel<SearchViewModel>()
) {
    LaunchedEffect(searchType) {
        viewModel.onIntent(SearchIntent.InitSearchType(searchType = searchType))
    }

    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                SearchEffect.NavigateBack -> {
                    navController.popBackStack()
                }

                is SearchEffect.NavigateToDetail -> {}
                is SearchEffect.ShowToast -> {}
            }
        }
    }

    SearchScreenContent(
        uiState = uiState,
        onBackClick = { viewModel.onIntent(SearchIntent.ClickClose) },
        onQueryChange = { query -> viewModel.onIntent(SearchIntent.ChangeQuery(query)) },
        onSearch = { viewModel.onIntent(SearchIntent.ClickKeyboardSearch) })
}

@Composable
fun SearchScreenContent(
    uiState: SearchUiState,
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit,
    onQueryChange: (String) -> Unit,
    onSearch: () -> Unit,
) {
    ChaGokBackground {
        Box(modifier = modifier.fillMaxSize()) {
            Column(
                modifier = modifier
                    .fillMaxSize()
                    .padding(top = 20.dp)
                    .padding(horizontal = 20.dp)
            ) {
                ChaGokSearchBar(
                    query = uiState.searchQuery,
                    onQueryChange = onQueryChange,
                    onBackClick = onBackClick,
                    onSearch = onSearch,
                )
                val annotatedSearchCount = buildAnnotatedString {
                    withStyle(style = SpanStyle(color = PrimaryColor)) {
                        append("전체")
                    }
                    withStyle(style = SpanStyle(color = TextSecondary)) {
                        append(" 검색 결과 ")
                    }

                    withStyle(style = SpanStyle(color = PrimaryColor)) {
                        append("${uiState.itemList.size}")
                    }
                }
                Spacer(modifier = modifier.height(24.dp))
                Text(
                    text = annotatedSearchCount,
                    style = ChaGokTextStyle.Title3,
                )
                Spacer(modifier = modifier.height(24.dp))
                LazyColumn(
                    modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(vertical = 16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(
                        items = uiState.itemList, key = { it.id }) { item ->
                        when (item) {
                            is SearchItem.HomeSearchItem -> {
                                ChaGokItemBox(
                                    title = item.title,
                                    createAt = item.createAt,
                                    type = if (item.isFolder) FileType.FOLDER else FileType.VOICE_NOTE,
                                    count = item.count,
                                    folderName = item.folderName,
                                    duration = item.duration,
                                    onClick = { },
                                )
                            }

                            is SearchItem.TrashSearchItem -> {
                                ChaGokItemBox(
                                    title = item.title,
                                    createAt = item.createAt,
                                    type = if (item.isFolder) FileType.FOLDER else FileType.VOICE_NOTE,
                                    count = item.count,
                                    folderName = item.folderName,
                                    duration = item.duration,
                                    onClick = { },
                                )
                            }

                            is SearchItem.FolderSearchItem -> {
                                ChaGokItemBox(
                                    title = item.title,
                                    createAt = item.createAt,
                                    type = FileType.FOLDER ,
                                    count = item.count,
                                    onClick = { },
                                )
                            }
                        }

                    }
                }


            }
        }
    }
}

@Composable
@Preview
fun SearchScreenContentPreview() {
    SearchScreenContent(
        uiState = SearchUiState(),
        onBackClick = { },
        onQueryChange = { },
        onSearch = { },
    )
}