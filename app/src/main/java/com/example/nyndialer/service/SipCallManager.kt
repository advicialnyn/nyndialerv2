package com.example.nyndialer.service

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.SystemClock
import com.example.nyndialer.data.DialerRepository
import com.example.nyndialer.data.model.CallRecord
import com.example.nyndialer.data.model.CallType
import com.example.nyndialer.data.model.ServiceType
import com.example.nyndialer.data.model.SipAccount
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

enum class CallState {
    IDLE,
    OUTGOING_DIALING,
    RINGING,
    ACTIVE,
    HANDING_OFF,
    ENDED
}

data class ActiveCallInfo(
    val phoneNumber: String,
    val contactName: String?,
    val serviceType: ServiceType,
    val simSlot: Int? = null,
    val callState: CallState = CallState.OUTGOING_DIALING,
    val callDurationSec: Int = 0,
    val isMuted: Boolean = false,
    val isSpeakerOn: Boolean = false,
    val isHold: Boolean = false,
    val isRecording: Boolean = false,
    val recordingPath: String? = null,
    val voipCodec: String = "Opus (48kHz)",
    val latencyMs: Int = 24,
    val jitterMs: Int = 8,
    val packetLossPct: Float = 0.0f
)

class SipCallManager(
    private val context: Context,
    private val repository: DialerRepository,
    private val recordingManager: CallRecordingManager
) {
    private val _activeCall = MutableStateFlow<ActiveCallInfo?>(null)
    val activeCall: StateFlow<ActiveCallInfo?> = _activeCall.asStateFlow()

    private val _sipRegistrationStatus = MutableStateFlow("Registered (sip.antisip.com:5060)")
    val sipRegistrationStatus: StateFlow<String> = _sipRegistrationStatus.asStateFlow()

    private val _isSipOnline = MutableStateFlow(true)
    val isSipOnline: StateFlow<Boolean> = _isSipOnline.asStateFlow()

    private var callDurationJob: Job? = null
    private var callSimulationJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.Main)
    private var callStartTime = 0L

    fun registerSip(account: SipAccount) {
        scope.launch {
            _sipRegistrationStatus.value = "Registering with ${account.domain}..."
            _isSipOnline.value = false
            delay(1200)
            _isSipOnline.value = true
            _sipRegistrationStatus.value = "Registered (${account.username}@${account.domain}:${account.port})"
            repository.saveSipAccount(account.copy(isRegistered = true, registeredAt = System.currentTimeMillis()))
        }
    }

    /**
     * Starts a call using either Cellular or SIP VoIP.
     */
    fun startCall(
        phoneNumber: String,
        contactName: String?,
        serviceType: ServiceType,
        simSlot: Int? = null,
        autoRecord: Boolean = true,
        isStealth: Boolean = true
    ) {
        if (_activeCall.value != null && _activeCall.value?.callState != CallState.ENDED) return

        val initialCall = ActiveCallInfo(
            phoneNumber = phoneNumber,
            contactName = contactName,
            serviceType = serviceType,
            simSlot = if (serviceType == ServiceType.CELLULAR) (simSlot ?: 1) else null,
            callState = CallState.OUTGOING_DIALING,
            isRecording = false
        )
        _activeCall.value = initialCall

        callSimulationJob?.cancel()
        callSimulationJob = scope.launch {
            delay(1000)
            _activeCall.value = _activeCall.value?.copy(callState = CallState.RINGING)
            delay(1800)
            _activeCall.value = _activeCall.value?.copy(callState = CallState.ACTIVE)
            callStartTime = SystemClock.elapsedRealtime()

            // If auto-recording is enabled, initiate stealth recording immediately
            if (autoRecord) {
                recordingManager.startRecording(
                    phoneNumber = phoneNumber,
                    contactName = contactName,
                    serviceType = serviceType,
                    isStealth = isStealth
                )
                _activeCall.value = _activeCall.value?.copy(isRecording = true)
            }

            startTimer()
        }
    }

    /**
     * Seamless switching between Cellular and SIP VoIP during an active call!
     */
    fun switchServiceType() {
        val current = _activeCall.value ?: return
        if (current.callState != CallState.ACTIVE && current.callState != CallState.HANDING_OFF) return

        val newService = if (current.serviceType == ServiceType.CELLULAR) {
            ServiceType.SIP_VOIP
        } else {
            ServiceType.CELLULAR
        }

        scope.launch {
            // Handing off seamlessly
            _activeCall.value = current.copy(callState = CallState.HANDING_OFF)
            delay(800) // seamless buffer handoff
            _activeCall.value = _activeCall.value?.copy(
                serviceType = newService,
                callState = CallState.ACTIVE,
                voipCodec = if (newService == ServiceType.SIP_VOIP) "Opus Wideband (48kHz)" else "VoLTE AMR-WB",
                latencyMs = if (newService == ServiceType.SIP_VOIP) 28 else 18
            )
        }
    }

    fun toggleMute() {
        _activeCall.value = _activeCall.value?.let { it.copy(isMuted = !it.isMuted) }
    }

    fun toggleSpeaker() {
        _activeCall.value = _activeCall.value?.let { it.copy(isSpeakerOn = !it.isSpeakerOn) }
    }

    fun toggleHold() {
        _activeCall.value = _activeCall.value?.let { it.copy(isHold = !it.isHold) }
    }

    /**
     * Discreet toggle for recording during call.
     */
    fun toggleRecording(isStealth: Boolean = true) {
        val current = _activeCall.value ?: return
        if (current.isRecording) {
            scope.launch {
                recordingManager.stopRecording()
                _activeCall.value = _activeCall.value?.copy(isRecording = false)
            }
        } else {
            recordingManager.startRecording(
                phoneNumber = current.phoneNumber,
                contactName = current.contactName,
                serviceType = current.serviceType,
                isStealth = isStealth
            )
            _activeCall.value = _activeCall.value?.copy(isRecording = true)
        }
    }

    fun endCall() {
        val current = _activeCall.value ?: return
        callSimulationJob?.cancel()
        callDurationJob?.cancel()

        scope.launch {
            var recordingPath: String? = null
            if (current.isRecording) {
                val savedRec = recordingManager.stopRecording()
                recordingPath = savedRec?.filePath
            }

            // Save to Call Log
            repository.insertCall(
                CallRecord(
                    phoneNumber = current.phoneNumber,
                    contactName = current.contactName,
                    callType = CallType.OUTGOING,
                    serviceType = current.serviceType,
                    simSlot = current.simSlot,
                    timestamp = System.currentTimeMillis(),
                    durationSeconds = current.callDurationSec,
                    recordingPath = recordingPath
                )
            )

            _activeCall.value = current.copy(callState = CallState.ENDED)
            delay(500)
            _activeCall.value = null
        }
    }

    private fun startTimer() {
        callDurationJob?.cancel()
        callDurationJob = scope.launch {
            while (isActive && _activeCall.value?.callState == CallState.ACTIVE) {
                delay(1000)
                _activeCall.value = _activeCall.value?.let {
                    it.copy(callDurationSec = it.callDurationSec + 1)
                }
            }
        }
    }

    /**
     * Real native dialer fallback trigger if user wants system cellular dialer.
     */
    fun placeSystemCellularCall(phoneNumber: String) {
        try {
            val intent = Intent(Intent.ACTION_CALL).apply {
                data = Uri.parse("tel:${Uri.encode(phoneNumber)}")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            val dialIntent = Intent(Intent.ACTION_DIAL).apply {
                data = Uri.parse("tel:${Uri.encode(phoneNumber)}")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(dialIntent)
        }
    }
}
