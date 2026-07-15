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
import android.content.Intent
import androidx.compose.ui.platform.LocalContext
import com.aistudio.neurostats.data.SourceStatus
import com.patrykandpatrick.vico.compose.axis.horizontal.bottomAxis
import com.patrykandpatrick.vico.compose.axis.vertical.startAxis
import com.patrykandpatrick.vico.compose.chart.Chart
import com.patrykandpatrick.vico.compose.chart.line.lineChart
import com.patrykandpatrick.vico.core.entry.ChartEntryModelProducer
import com.patrykandpatrick.vico.core.entry.FloatEntry
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MMSINeuroDashboardScreen(
    viewModel: EegViewModel = viewModel()
) {
    val streamStatus by viewModel.status.collectAsState()
    val currentWTask by viewModel.cognitiveLoadTrajectory.collectAsState()
    val signalQuality by viewModel.signalQuality.collectAsState()
    val isRecording by viewModel.isRecording.collectAsState()
    val eegChannels by viewModel.eegChannels.collectAsState()
    val modelProducer = remember { ChartEntryModelProducer() }
    val context = LocalContext.current
    
    // Simple chart update
    LaunchedEffect(eegChannels) {
        modelProducer.setEntries(
            eegChannels.mapIndexed { index, value -> FloatEntry(index.toFloat(), value) }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("MMSI Neuro", style = MaterialTheme.typography.titleMedium) },
                actions = {
                    ConnectionStatusBadge(status = streamStatus)
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
                    Button(onClick = { viewModel.setSourceToPlayback() }) {
                        Text("Load Test Data", style = MaterialTheme.typography.bodyMedium)
                    }
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
                        text = "Echtzeit EEG Kanal 1",
                        style = MaterialTheme.typography.labelLarge
                    )
                    
                    val model = modelProducer.getModel()
                    if (model != null) {
                        Chart(
                            chart = lineChart(),
                            model = model,
                            modifier = Modifier.fillMaxWidth().height(200.dp),
                            startAxis = startAxis(title = "Amplitude (µV)"),
                            bottomAxis = bottomAxis(title = "Time (s)")
                        )
                    } else {
                        Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                            Text("Keine Daten")
                        }
                    }
                    
                    Text(
                        text = "Kognitive Systemlast W(t): ${String.format(Locale.US, "%.3f", currentWTask)}",
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }

            // ZONE 3: Daumen-freundliche Primary Action Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Button(
                    onClick = { viewModel.toggleRecording() },
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isRecording) 
                            MaterialTheme.colorScheme.secondary 
                        else 
                            MaterialTheme.colorScheme.tertiary
                    )
                ) {
                    Text(
                        text = if (isRecording) "Aufnahme Stoppen" else "Aufnahme Starten",
                        style = MaterialTheme.typography.titleMedium
                    )
                }

                Button(
                    onClick = {
                        val csvData = viewModel.getRecordedDataAsCsv()
                        val intent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_TEXT, csvData)
                        }
                        context.startActivity(Intent.createChooser(intent, "Export CSV"))
                    },
                    modifier = Modifier
                        .weight(0.5f)
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    )
                ) {
                    Text("CSV", style = MaterialTheme.typography.titleMedium)
                }

                Button(
                    onClick = { viewModel.toggleStreaming() },
                    modifier = Modifier
                        .weight(1f)
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
}

@Composable
fun ConnectionStatusBadge(status: SourceStatus) {
    val (badgeColor, label) = when (status) {
        SourceStatus.DISCONNECTED -> Color.Red to "Disconnected"
        SourceStatus.CONNECTED -> Color(0xFFFFC107) to "Connected"
        SourceStatus.STREAMING -> Color(0xFF4CAF50) to "Streaming"
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
                text = label,
                style = MaterialTheme.typography.labelSmall
            )
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
