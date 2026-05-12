package com.roro.chagok

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Environment
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
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

import android.Manifest
import android.net.Uri
import android.provider.Settings


/**
 * 기능 설명:
 * - 앱의 진입점
 * - Compose UI를 시작하고 앱의 루트 화면을 표시
 *
 * @author sehoon
 * @since 2026. 2. 28.
 */
//@AndroidEntryPoint
//class MainActivity : ComponentActivity() {
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        enableEdgeToEdge()
//        setContent {
//            ChaGokTheme {
//                AppScaffold()
//            }
//        }
//    }
//}

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // 마이크 + 외부 저장소 권한 요청
        requestPermissionLauncher.launch(
            arrayOf(
                Manifest.permission.RECORD_AUDIO,  // ✅ 추가
                Manifest.permission.READ_EXTERNAL_STORAGE
            )
        )

        // Android 11+ 외부 저장소
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (!Environment.isExternalStorageManager()) {
                val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION).apply {
                    data = Uri.parse("package:$packageName")
                }
                startActivity(intent)
            }
        }

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