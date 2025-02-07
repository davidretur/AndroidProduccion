package com.example.dialogcompose

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.Dimension
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.dialogcompose.ui.theme.DialogComposeTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DialogComposeTheme {
                Surface( modifier = Modifier.fillMaxSize() ) {
                    var show by rememberSaveable { mutableStateOf(false) }
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center){
                        Button(onClick = {show = true}) {
                            Text(text = "Dialogo")
                        }
                    }
                    MyDialog(show,{show = false},{Log.i("aris","click")})

                }
            }
        }
    }
}




@Composable
fun MyDialog(show:Boolean,
             onDismiss:()-> Unit,
             onConfirm:()-> Unit
             ){
    if (show) {
        AlertDialog(
            onDismissRequest = { onDismiss()},
            confirmButton = {
                TextButton(onClick = { onConfirm()}) {
                    Text(text = "ConfirmButton")
                }
            },
            dismissButton = {
                TextButton(onClick = { onDismiss()}) {
                    Text(text = "DismissButton")
                }
            },
            title = { Text(text = "Confirmar") },
            text = { Text(text = "Este es el body de mi dialog") },
        )
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    DialogComposeTheme {

    }
}