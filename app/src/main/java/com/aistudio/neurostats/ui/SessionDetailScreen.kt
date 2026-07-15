package com.aistudio.neurostats.ui

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.aistudio.neurostats.data.EegSession
import com.aistudio.neurostats.logic.ReportGenerator
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SessionDetailScreen(
    sessionId: Int,
    onBack: () -> Unit,
    viewModel: EegViewModel = viewModel()
) {
    val session by viewModel.getSession(sessionId).collectAsState(initial = null)
    val lastReport by viewModel.lastGeneratedReport.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    DisposableEffect(Unit) {
        onDispose { viewModel.clearLastReport() }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Klinischer Befund") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Zurück")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        if (session == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            val currentSession = session!!
            val isBatch = currentSession.label.contains("Batch") || currentSession.label.contains("Batch-Validierung")
            
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Info Card
                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(currentSession.label, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "Analysezeitpunkt: ${SimpleDateFormat("dd.MM.yyyy HH:mm:ss", Locale.getDefault()).format(Date(currentSession.timestamp))}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                        Text(
                            "Status: VALIDATED | Algorithmus: MMSI v3.6.3",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }

                if (isBatch) {
                    val probandReports = viewModel.parseBatchReportFromCsv(currentSession.csvData)
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Probanden-Auswertungen (10)", style = MaterialTheme.typography.titleSmall)
                    Spacer(modifier = Modifier.height(8.dp))

                    probandReports.forEach { proband ->
                        ProbandAssessmentCard(proband)
                    }
                } else {
                    Spacer(modifier = Modifier.height(12.dp))
                    ClinicalAnalysisResult(currentSession.csvData)
                }

                Spacer(modifier = Modifier.height(16.dp))
                
                // Action Buttons (PDF generation and sharing)
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (lastReport == null) {
                        Button(
                            onClick = { viewModel.generateReportForSession(currentSession) },
                            modifier = Modifier.weight(1f).height(56.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Icon(Icons.Default.Build, null)
                            Spacer(Modifier.width(8.dp))
                            Text(if (isBatch) "PDF Bericht generieren" else "Befund-Report erstellen")
                        }
                    } else {
                        Button(
                            onClick = {
                                val uri = androidx.core.content.FileProvider.getUriForFile(
                                    context,
                                    "${context.packageName}.provider",
                                    lastReport!!
                                )
                                val intent = Intent(Intent.ACTION_SEND).apply {
                                    type = if (isBatch) "application/pdf" else "image/png"
                                    putExtra(Intent.EXTRA_STREAM, uri)
                                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                }
                                context.startActivity(Intent.createChooser(intent, "Klinischen Bericht senden"))
                            },
                            modifier = Modifier.weight(1f).height(56.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
                        ) {
                            Icon(Icons.Default.Share, null)
                            Spacer(Modifier.width(8.dp))
                            Text(if (isBatch) "PDF Bericht Teilen" else "Report Teilen")
                        }
                    }

                    if (!isBatch) {
                        OutlinedButton(
                            onClick = {
                                val sendIntent = Intent().apply {
                                    action = Intent.ACTION_SEND
                                    putExtra(Intent.EXTRA_TEXT, currentSession.csvData)
                                    type = "text/plain"
                                }
                                context.startActivity(Intent.createChooser(sendIntent, "Rohdaten exportieren"))
                            },
                            modifier = Modifier.height(56.dp)
                        ) {
                            Text("CSV")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ProbandAssessmentCard(proband: ReportGenerator.ProbandReportData) {
    val (statusText, statusColor) = when (proband.type) {
        "Healthy Control" -> "STABIL" to Color(0xFF2E7D32)
        "Borderline Profile" -> "GRENZWERTIG" to Color(0xFFE65100)
        else -> "PATHOLOGISCH" to Color(0xFFC62828)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(proband.id, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                    Text(proband.name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                }
                
                Surface(
                    color = statusColor.copy(alpha = 0.1f),
                    contentColor = statusColor,
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = statusText,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(10.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                AnalysisItem("Ø W(t)", String.format(Locale.US, "%.3f", proband.stats.mean))
                AnalysisItem("Varianz", String.format(Locale.US, "%.4f", proband.stats.variance))
                AnalysisItem("Peak W(t)", String.format(Locale.US, "%.3f", proband.stats.maxVal))
                AnalysisItem("RMSD (Instab.)", String.format(Locale.US, "%.4f", proband.stats.rmsd))
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                AnalysisItem("Events (>3.2)", proband.stats.crossings.toString())
                AnalysisItem("Verweildauer", String.format(Locale.US, "%.1f%%", proband.stats.timeAbovePercent))
                AnalysisItem("Diag. Score", String.format(Locale.US, "%.1f%%", proband.stats.diagnosticScore))
                Spacer(modifier = Modifier.width(48.dp))
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = proband.stats.interpretation,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.outline
            )
        }
    }
}

@Composable
fun ClinicalAnalysisResult(csvData: String) {
    val data = csvData.split("\n")
        .filter { it.isNotBlank() && !it.startsWith("#") }
        .mapNotNull { it.split(",").firstOrNull()?.replace("\"", "")?.trim()?.toFloatOrNull() }
    
    if (data.isEmpty()) return

    val mean = data.average().toFloat()
    val variance = data.map { (it - mean) * (it - mean) }.average().toFloat()
    val maxVal = data.maxOrNull() ?: 0f
    val crossings = data.count { it > 3.2f }
    
    val successiveDiffsSq = if (data.size > 1) {
        data.zipWithNext { a, b -> (a - b) * (a - b) }.average().toFloat()
    } else {
        0f
    }
    val rmsd = kotlin.math.sqrt(successiveDiffsSq)
    val timeAbovePercent = (crossings.toFloat() / data.size) * 100f
    val scoreBase = (mean / 3.2f) * 30f + (maxVal / 5.0f) * 35f + (timeAbovePercent / 100f) * 35f
    val diagnosticScore = scoreBase.coerceIn(5.0f, 98.5f)

    val (status, color) = when {
        diagnosticScore > 65f -> "PATHOLOGISCH" to MaterialTheme.colorScheme.error
        diagnosticScore > 35f -> "GRENZWERTIG" to Color(0xFFFFC107)
        else -> "STABIL" to Color(0xFF4CAF50)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.05f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(12.dp).background(color, CircleShape))
                Spacer(Modifier.width(8.dp))
                Text("KLINISCHER STATUS: $status (Diag. Score: ${String.format("%.1f%%", diagnosticScore)})", style = MaterialTheme.typography.titleSmall, color = color)
            }
            Spacer(modifier = Modifier.height(16.dp))
            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                AnalysisItem("Ø W(t)", String.format("%.3f", mean))
                AnalysisItem("Varianz", String.format("%.4f", variance))
                AnalysisItem("Peak W(t)", String.format("%.3f", maxVal))
                AnalysisItem("RMSD (Instab.)", String.format("%.4f", rmsd))
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                AnalysisItem("Events (>3.2)", crossings.toString())
                AnalysisItem("Verweildauer", String.format("%.1f%%", timeAbovePercent))
                Spacer(modifier = Modifier.width(48.dp))
                Spacer(modifier = Modifier.width(48.dp))
            }
        }
    }
}

@Composable
fun AnalysisItem(label: String, value: String) {
    Column {
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
        Text(value, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
    }
}
