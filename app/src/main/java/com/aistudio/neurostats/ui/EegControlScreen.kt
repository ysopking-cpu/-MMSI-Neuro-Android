package com.aistudio.neurostats.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.aistudio.neurostats.data.SourceStatus
import java.util.Locale

@Composable
fun EegControlScreen(viewModel: EegViewModel = viewModel()) {
    val currentStatus by viewModel.status.collectAsState()
    val currentWTask by viewModel.cognitiveLoadTrajectory.collectAsState()

    Column(modifier = Modifier.padding(16.dp).fillMaxSize()) {
        Text(text = "MMSI Neuro Ingestion Panel", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))

        Row {
            Button(onClick = { /* Setup Playback Source */ }) { Text("Dataset Playback") }
            Spacer(modifier = Modifier.width(8.dp))
            Button(onClick = { /* Setup Live Source */ }) { Text("Live EEG (BLE)") }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(text = "Status: ${currentStatus.name}", color = if (currentStatus == SourceStatus.STREAMING) Color.Green else Color.Gray)

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { viewModel.toggleStreaming() },
            colors = ButtonDefaults.buttonColors(containerColor = if (currentStatus == SourceStatus.STREAMING) Color.Red else Color.Blue)
        ) {
            Text(if (currentStatus == SourceStatus.STREAMING) "Stop Stream" else "Start Stream")
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(text = "Aktuelle Systemlast W(t): ${String.format(Locale.US, "%.4f", currentWTask)}")
    }
}
