package com.mindnest.app

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.mindnest.app.ui.NestApp
import com.mindnest.app.ui.NestViewModel
import com.mindnest.app.ui.captureTarget
import com.mindnest.app.ui.theme.MindnestTheme

class MainActivity : ComponentActivity() {

    private val vm: NestViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        if (savedInstanceState == null) handleIntent(intent)
        setContent {
            MindnestTheme {
                NestApp(vm)
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        when (intent?.action) {
            ACTION_CAPTURE -> if (vm.editor == null) vm.startCapture(vm.backStack.last().captureTarget())
            Intent.ACTION_SEND -> {
                val subject = intent.getStringExtra(Intent.EXTRA_SUBJECT)
                val text = intent.getStringExtra(Intent.EXTRA_TEXT)
                val combined = listOfNotNull(subject, text).filter { it.isNotBlank() }.joinToString("\n")
                if (combined.isNotBlank()) vm.startCapture(folderId = null, prefill = combined)
            }
        }
    }

    companion object {
        const val ACTION_CAPTURE = "com.mindnest.app.CAPTURE"
    }
}
