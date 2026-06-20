package com.roro.storage.presentation.home.setting

import android.webkit.CookieManager
import android.webkit.WebView
import android.webkit.WebViewClient
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavController
import com.roro.core.ui.component.ChaGokBackground
import com.roro.core.ui.component.ChaGokTopBarV2
import timber.log.Timber

import android.graphics.Bitmap
import android.webkit.ConsoleMessage
import android.webkit.WebResourceResponse


@Composable
fun WebViewScreen(
    navController: NavController,
    url: String,
) {
    var errorMessage by remember(url) { mutableStateOf<String?>(null) }

    LaunchedEffect(url) {
        Timber.d("WebViewScreen url = $url")
    }

    ChaGokBackground {
        Column(modifier = Modifier.fillMaxSize()) {
            ChaGokTopBarV2(
                title = "",
                showBackButton = true,
                onBackClick = { navController.popBackStack() }
            )
            Box(modifier = Modifier.fillMaxSize()) {
                AndroidView(
                    factory = { context ->
                        WebView(context).apply {

                            Timber.d(
                                "WebView Version = ${
                                    WebView.getCurrentWebViewPackage()?.versionName
                                }"
                            )

                            webChromeClient = object : WebChromeClient() {
                                override fun onConsoleMessage(
                                    consoleMessage: ConsoleMessage
                                ): Boolean {

                                    Timber.e(
                                        """
                        JS ERROR
                        message=${consoleMessage.message()}
                        line=${consoleMessage.lineNumber()}
                        source=${consoleMessage.sourceId()}
                        level=${consoleMessage.messageLevel()}
                        """.trimIndent()
                                    )

                                    return true
                                }
                            }

                            webViewClient = object : WebViewClient() {

                                override fun onPageStarted(
                                    view: WebView?,
                                    url: String?,
                                    favicon: Bitmap?
                                ) {
                                    Timber.d("PAGE STARTED = $url")
                                }

                                override fun onPageFinished(
                                    view: WebView?,
                                    url: String?
                                ) {
                                    Timber.d("PAGE FINISHED = $url")
                                }

                                override fun onReceivedHttpError(
                                    view: WebView?,
                                    request: WebResourceRequest?,
                                    errorResponse: WebResourceResponse?
                                ) {
                                    Timber.e(
                                        "HTTP ERROR " +
                                                "code=${errorResponse?.statusCode} " +
                                                "url=${request?.url}"
                                    )
                                }

                                override fun onReceivedError(
                                    view: WebView?,
                                    request: WebResourceRequest?,
                                    error: WebResourceError?
                                ) {
                                    super.onReceivedError(view, request, error)

                                    Timber.e(
                                        "WEBVIEW ERROR " +
                                                "url=${request?.url} " +
                                                "error=${error?.description}"
                                    )

                                    if (request?.isForMainFrame == true) {
                                        errorMessage =
                                            error?.description?.toString()
                                                ?: "페이지를 불러오지 못했어요."
                                    }
                                }
                            }

                            settings.apply {
                                javaScriptEnabled = true
                                domStorageEnabled = true
                                databaseEnabled = true

                                javaScriptCanOpenWindowsAutomatically = true
                                setSupportMultipleWindows(true)

                                loadWithOverviewMode = true
                                useWideViewPort = true

                                loadsImagesAutomatically = true

                                allowFileAccess = true
                                allowContentAccess = true

                                cacheMode = WebSettings.LOAD_DEFAULT

                                mixedContentMode =
                                    WebSettings.MIXED_CONTENT_ALWAYS_ALLOW

                                userAgentString =
                                    WebSettings.getDefaultUserAgent(context)
                            }

                            CookieManager.getInstance().setAcceptCookie(true)
                            CookieManager.getInstance()
                                .setAcceptThirdPartyCookies(this, true)

                            setBackgroundColor(android.graphics.Color.WHITE)

                            loadUrl(url)
                        }
                    },
                    update = { webView ->
                        if (webView.url != url) {
                            errorMessage = null
                            webView.loadUrl(url)
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )

                errorMessage?.let { message ->
                    Text(
                        text = message,
                        color = Color.White,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            }
        }
    }
}
