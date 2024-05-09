package com.ragl.divide

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.ragl.divide.ui.theme.DivideTheme
import com.ragl.divide.ui.DivideApp
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DivideTheme {
                DivideApp()
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DivideAppPreview() {
    DivideTheme {
        DivideApp()
    }
}