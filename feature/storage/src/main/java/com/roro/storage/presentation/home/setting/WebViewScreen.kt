package com.roro.storage.presentation.home.setting

import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavController
import com.roro.core.ui.component.ChaGokBackground
import com.roro.core.ui.component.ChaGokTopBarV2

@Composable
fun WebViewScreen(
    navController: NavController,
    url: String,
) {
    ChaGokBackground {
        Column(modifier = Modifier.fillMaxSize()) {
            ChaGokTopBarV2(
                title = "",
                showBackButton = true,
                onBackClick = { navController.popBackStack() }
            )
            AndroidView(
                factory = { context ->
                    WebView(context).apply {
                        webViewClient = WebViewClient()
                        settings.javaScriptEnabled = true
                        loadUrl(url)
                    }
                },
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}