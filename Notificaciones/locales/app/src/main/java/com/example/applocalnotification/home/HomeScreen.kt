package com.example.applocalnotification.home

import android.view.View
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel

@Composable
fun HomeScreen(
    viewModel: HomeViewModel
){
val state = viewModel.state
    val context = LocalContext.current

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ){
        TextField(value = state.name, onValueChange = {viewModel.changeName(it)})
        Button(onClick = {viewModel.sendNotification(context)})
        {
            Text(text = "Enviar Notificacion")
        }
    }
}