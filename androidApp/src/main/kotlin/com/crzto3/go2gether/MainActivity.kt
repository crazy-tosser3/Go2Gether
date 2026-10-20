package com.crzto3.go2gether

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.crzto3.go2gether.data.KeyValueStore
import com.crzto3.go2gether.ui.App

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        KeyValueStore.init(this)

        setContent {
            App()
        }
    }
}
