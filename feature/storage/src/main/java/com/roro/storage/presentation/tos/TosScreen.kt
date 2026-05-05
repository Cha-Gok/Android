package com.roro.storage.presentation.tos

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.roro.storage.presentation.trash.TrashViewModel

@Composable
internal fun TosScreen(
    navController: NavController,
    viewModel: TrashViewModel = hiltViewModel<TrashViewModel>() // 임시
) {
    TosScreenContent(
        navController = navController
    )

}

@Composable
private fun TosScreenContent(
    navController: NavController
) {
    Text("tos")
}


@Composable
@Preview
private fun TrashScreenContentPreview() {
    TosScreenContent(
        navController = rememberNavController(),
    )
}
