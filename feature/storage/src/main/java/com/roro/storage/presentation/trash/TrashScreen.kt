package com.roro.storage.presentation.trash

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController

@Composable
internal fun TrashScreen(
    navController: NavController,
    viewModel: TrashViewModel = hiltViewModel<TrashViewModel>()
) {
    TrashScreenContent(
        navController = navController
    )

}

@Composable
private fun TrashScreenContent(
    navController: NavController
) {
    Text("trashView")
}


@Composable
@Preview
private fun TrashScreenContentPreview() {
    TrashScreenContent(
        navController = rememberNavController(),
    )
}
