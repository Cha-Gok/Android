package com.roro.storage.presentation.search

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavController
import com.roro.core.ui.component.ChaGokBackground
import com.roro.core.ui.theme.Gray850

@Composable
fun SearchScreen(
    searchType: String,
    navController: NavController
) {

    SearchScreenContent()
}

@Composable
fun SearchScreenContent() {
    ChaGokBackground {
        Box(modifier = Modifier.fillMaxSize()) {
            Column {
                Text("SCREEN", color = Gray850)
            }
        }
    }
}

@Composable
@Preview
fun SearchScreenContentPreview() {
    SearchScreenContent()
}