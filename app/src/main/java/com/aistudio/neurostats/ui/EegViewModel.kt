package com.aistudio.neurostats.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.aistudio.neurostats.data.EegDataSource
import com.aistudio.neurostats.data.FilePlaybackDataSource
import com.aistudio.neurostats.data.SourceStatus
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class EegViewModel(application: Application) : AndroidViewModel(application) {
    private var currentDataSource: EegDataSource? = null
    private var streamingJob: Job? = null

    private val _status = MutableStateFlow(SourceStatus.DISCONNECTED)
    val status: StateFlow<SourceStatus> = _status.asStateFlow()

    private val _cognitiveLoadTrajectory = MutableStateFlow(0.0)
    val cognitiveLoadTrajectory: StateFlow<Double> = _cognitiveLoadTrajectory.asStateFlow()

    private val _eegChannels = MutableStateFlow(FloatArray(4) { 0f })
    val eegChannels: StateFlow<FloatArray> = _eegChannels.asStateFlow()

    private val _isRecording = MutableStateFlow(false)
    val isRecording: StateFlow<Boolean> = _isRecording.asStateFlow()
    private val recordedData = mutableListOf<FloatArray>()

    private val _signalQuality = MutableStateFlow(85)
    val signalQuality: StateFlow<Int> = _signalQuality.asStateFlow()

    fun setSource(source: EegDataSource) {
        currentDataSource = source
        // In a real app, observe the status from the source
    }

    fun setSourceToPlayback() {
        setSource(FilePlaybackDataSource(context = getApplication(), fileName = "eeg_test_data.csv"))
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
                    _eegChannels.value = frame.channels
                    if (_isRecording.value) {
                        recordedData.add(frame.channels.copyOf())
                    }
                }
            }
        }
    }

    fun toggleRecording() {
        _isRecording.value = !_isRecording.value
        if (_isRecording.value) {
            recordedData.clear()
        }
    }

    fun getRecordedDataAsCsv(): String {
        val sb = StringBuilder()
        sb.append("Index,Ch1,Ch2,Ch3,Ch4\n")
        recordedData.forEachIndexed { index, channels ->
            sb.append("$index,${channels.joinToString(",")}\n")
        }
        return sb.toString()
    }
}
