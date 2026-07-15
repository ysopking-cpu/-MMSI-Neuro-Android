package com.aistudio.neurostats.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aistudio.neurostats.data.EegDataSource
import com.aistudio.neurostats.data.SourceStatus
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class EegViewModel : ViewModel() {
    private var currentDataSource: EegDataSource? = null
    private var streamingJob: Job? = null

    private val _status = MutableStateFlow(SourceStatus.DISCONNECTED)
    val status: StateFlow<SourceStatus> = _status.asStateFlow()

    private val _cognitiveLoadTrajectory = MutableStateFlow(0.0)
    val cognitiveLoadTrajectory: StateFlow<Double> = _cognitiveLoadTrajectory.asStateFlow()

    private val _signalQuality = MutableStateFlow(85)
    val signalQuality: StateFlow<Int> = _signalQuality.asStateFlow()

    fun setSource(source: EegDataSource) {
        currentDataSource = source
        // In a real app, observe the status from the source
    }

    fun switchMode(mode: String) {
        // Implementation for mode switching
    }

    fun toggleStreaming() {
        if (_status.value == SourceStatus.STREAMING) {
            streamingJob?.cancel()
            _status.value = SourceStatus.DISCONNECTED
        } else {
            _status.value = SourceStatus.STREAMING
            streamingJob = viewModelScope.launch {
                currentDataSource?.streamEegData()?.collect { frame ->
                    // Here we would call the W(t) calculation logic
                    _cognitiveLoadTrajectory.value = frame.channels.sum().toDouble()
                }
            }
        }
    }
}
