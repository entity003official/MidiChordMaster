package com.midichordmaster

import android.content.Context
import android.media.midi.*
import android.os.Handler
import android.os.Looper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class MidiManager(private val context: Context) {
    
    private var midiManager: android.media.midi.MidiManager? = null
    private var midiDevice: MidiDevice? = null
    private var inputPort: MidiInputPort? = null
    private var midiListener: ((MidiEvent) -> Unit)? = null
    
    private val _isConnected = MutableStateFlow(false)
    val isConnected: StateFlow<Boolean> = _isConnected
    
    private val midiReceiver = object : MidiReceiver() {
        override fun onSend(msg: ByteArray, offset: Int, count: Int, timestamp: Long) {
            if (count >= 3) {
                val status = msg[offset].toInt() and 0xFF
                val data1 = msg[offset + 1].toInt() and 0x7F
                val data2 = if (count > 2) msg[offset + 2].toInt() and 0x7F else 0
                
                val midiEvent = when (status and 0xF0) {
                    0x90 -> { // Note On
                        if (data2 > 0) {
                            MidiEvent(MidiEvent.Type.NOTE_ON, data1, data2, timestamp)
                        } else {
                            MidiEvent(MidiEvent.Type.NOTE_OFF, data1, 0, timestamp)
                        }
                    }
                    0x80 -> { // Note Off
                        MidiEvent(MidiEvent.Type.NOTE_OFF, data1, data2, timestamp)
                    }
                    0xB0 -> { // Control Change
                        MidiEvent(MidiEvent.Type.CONTROL_CHANGE, data1, data2, timestamp)
                    }
                    0xE0 -> { // Pitch Bend
                        MidiEvent(MidiEvent.Type.PITCH_BEND, data1, data2, timestamp)
                    }
                    else -> null
                }
                
                midiEvent?.let { event ->
                    Handler(Looper.getMainLooper()).post {
                        midiListener?.invoke(event)
                    }
                }
            }
        }
    }
    
    init {
        midiManager = context.getSystemService(Context.MIDI_SERVICE) as? android.media.midi.MidiManager
    }
    
    fun setMidiListener(listener: (MidiEvent) -> Unit) {
        midiListener = listener
    }
    
    suspend fun connectMidi(): Boolean {
        return try {
            val manager = midiManager ?: return false
            val deviceInfos = manager.devices

            // Try to connect to the first available MIDI device with output ports
            for (deviceInfo in deviceInfos) {
                if (deviceInfo.outputPortCount > 0) {
                    manager.openDevice(deviceInfo, { device ->
                        if (device != null) {
                            midiDevice = device
                            val outputPort = device.openOutputPort(0)
                            outputPort?.connect(midiReceiver)
                            _isConnected.value = true
                        }
                    }, Handler(Looper.getMainLooper()))
                    return true
                }
            }
            
            // If no external MIDI device, mark as connected for virtual use
            _isConnected.value = true
            true
        } catch (e: Exception) {
            _isConnected.value = false
            false
        }
    }
    
    fun disconnect() {
        try {
            inputPort?.close()
            midiDevice?.close()
            _isConnected.value = false
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    fun sendMidiEvent(event: MidiEvent) {
        try {
            val message = when (event.type) {
                MidiEvent.Type.NOTE_ON -> {
                    byteArrayOf(
                        (0x90).toByte(), // Note On
                        event.note.toByte(),
                        event.velocity.toByte()
                    )
                }
                MidiEvent.Type.NOTE_OFF -> {
                    byteArrayOf(
                        (0x80).toByte(), // Note Off
                        event.note.toByte(),
                        event.velocity.toByte()
                    )
                }
                else -> return
            }
            
            inputPort?.send(message, 0, message.size)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
