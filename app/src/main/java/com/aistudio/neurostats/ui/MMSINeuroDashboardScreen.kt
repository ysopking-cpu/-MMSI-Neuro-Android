package com.aistudio.neurostats.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.aistudio.neurostats.data.SourceStatus
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MMSINeuroDashboardScreen(
    viewModel: EegViewModel = viewModel()
) {
    val streamStatus by viewModel.status.collectAsState()
    val currentWTask by viewModel.cognitiveLoadTrajectory.collectAsState()
    val signalQuality by viewModel.signalQuality.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("MMSI Neuro", style = MaterialTheme.typography.titleMedium) },
                actions = {
                    SignalHealthBadge(qualityPercent = signalQuality)
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            
            // Simplified Source Selector
            Card(
                modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp)
            ) {
                Row(modifier = Modifier.padding(16.dp)) {
                    Text("Source: Playback", style = MaterialTheme.typography.bodyMedium)
                }
            }

            // ZONE 2: Echtzeit-Trajektorien-Anzeige (Center Stage)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(vertical = 12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp).fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "Kognitive Systemlast W(t)",
                        style = MaterialTheme.typography.labelLarge
                    )
                    
                    Text(
                        text = String.format(Locale.US, "%.3f", currentWTask),
                        style = MaterialTheme.typography.displayMedium,
                        color = if (currentWTask > 0.8f) Color.Red else MaterialTheme.colorScheme.primary
                    )
                }
            }

            // ZONE 3: Daumen-freundliche Primary Action Bar
            Button(
                onClick = { viewModel.toggleStreaming() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (streamStatus == SourceStatus.STREAMING) 
                        MaterialTheme.colorScheme.error 
                    else 
                        MaterialTheme.colorScheme.primary
                )
            ) {
                Text(
                    text = if (streamStatus == SourceStatus.STREAMING) "Messung Stoppen" else "Messung Starten",
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }
    }
}

@Composable
fun SignalHealthBadge(qualityPercent: Int) {
    val badgeColor = when {
        qualityPercent > 75 -> Color(0xFF4CAF50)
        qualityPercent > 40 -> Color(0xFFFFC107)
        else -> Color(0xFFF44336)
    }

    Surface(
        color = badgeColor.copy(alpha = 0.2f),
        contentColor = badgeColor,
        shape = RoundedCornerShape(50)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(badgeColor, CircleShape)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "$qualityPercent% Signal",
                style = MaterialTheme.typography.labelSmall
            )
        }
    }
}
