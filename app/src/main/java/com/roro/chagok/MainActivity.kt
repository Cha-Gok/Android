package com.roro.chagok

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.roro.chagok.navigation.AppScaffold
import com.roro.core.ui.theme.ChaGokTheme
import dagger.hilt.android.AndroidEntryPoint

/**
 * 기능 설명:
 * - 앱의 진입점
 * - Compose UI를 시작하고 앱의 루트 화면을 표시
 *
 * @author sehoon
 * @since 2026. 2. 28.
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ChaGokTheme {
                AppScaffold()
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    ChaGokTheme {
        AppScaffold()
    }
}