package com.example.nyndialer.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.nyndialer.data.DialerRepository
import com.example.nyndialer.data.NynDialerDatabase
import com.example.nyndialer.data.model.CallRecord
import com.example.nyndialer.data.model.CallRecording
import com.example.nyndialer.data.model.ServiceType
import com.example.nyndialer.data.model.SipAccount
import com.example.nyndialer.data.model.SpeedDialContact
import com.example.nyndialer.domain.Contact
import com.example.nyndialer.domain.T9Engine
import com.example.nyndialer.domain.T9MatchResult
import com.example.nyndialer.service.ActiveCallInfo
import com.example.nyndialer.service.CallRecordingManager
import com.example.nyndialer.service.ContactsManager
import com.example.nyndialer.service.SipCallManager
import com.example.nyndialer.service.ToneFeedbackManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class DialerViewModel(application: Application) : AndroidViewModel(application) {
    val database = NynDialerDatabase.getInstance(application)
    val repository = DialerRepository(database, application)
    val recordingManager = CallRecordingManager(application, repository)
    val sipCallManager = SipCallManager(application, repository, recordingManager)
    val contactsManager = ContactsManager(application, repository)
    val toneFeedbackManager = ToneFeedbackManager(application)

    // Contacts & Sync
    val contacts: StateFlow<List<Contact>> = contactsManager.contacts
    val isSyncingContacts: StateFlow<Boolean> = contactsManager.isSyncing
    val lastGoogleSyncTime: StateFlow<Long> = repository.lastGoogleSyncTime

    // Dialpad State
    private val _dialedDigits = MutableStateFlow("")
    val dialedDigits: StateFlow<String> = _dialedDigits.asStateFlow()

    private val _currentServiceType = MutableStateFlow(ServiceType.CELLULAR)
    val currentServiceType: StateFlow<ServiceType> = _currentServiceType.asStateFlow()

    // Smart T9 search results
    val t9SearchResults: StateFlow<List<T9MatchResult>> = combine(
        contacts,
        _dialedDigits
    ) { contactList, query ->
        if (query.isBlank()) emptyList()
        else T9Engine.search(contactList, query)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Database Flows
    val callLogs: StateFlow<List<CallRecord>> = repository.allCalls
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val blockedNumbers: StateFlow<List<com.example.nyndialer.data.model.BlockedNumber>> = repository.allBlockedNumbers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val speedDials: StateFlow<List<SpeedDialContact>> = repository.allSpeedDials
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val sipAccount: StateFlow<SipAccount?> = repository.sipAccount
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val allSipAccounts: StateFlow<List<SipAccount>> = repository.allSipAccounts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val recordings: StateFlow<List<CallRecording>> = repository.allRecordings
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Dual SIM StateFlows
    val selectedSimSlot: StateFlow<Int> = repository.selectedSimSlot
    val sim1Label: StateFlow<String> = repository.sim1Label
    val sim2Label: StateFlow<String> = repository.sim2Label
    val defaultSimPreference: StateFlow<String> = repository.defaultSimPreference

    // App Preferences
    val colorThemeSetting: StateFlow<String> = repository.colorThemeSetting
    val darkModeSetting: StateFlow<String> = repository.darkModeSetting
    val dtmfToneEnabled: StateFlow<Boolean> = repository.dtmfToneEnabled
    val vibrateFeedback: StateFlow<Boolean> = repository.vibrateFeedback
    val autoRecordEnabled: StateFlow<Boolean> = repository.autoRecordEnabled
    val stealthRecordEnabled: StateFlow<Boolean> = repository.stealthRecordEnabled
    val showRecordingsTab: StateFlow<Boolean> = repository.showRecordingsTab

    // Active Call
    val activeCall: StateFlow<ActiveCallInfo?> = sipCallManager.activeCall
    val isSipOnline: StateFlow<Boolean> = sipCallManager.isSipOnline
    val sipStatusString: StateFlow<String> = sipCallManager.sipRegistrationStatus

    // Audio Playback
    val playingRecordingId: StateFlow<Long?> = recordingManager.playingRecordingId
    val isPlayingAudio: StateFlow<Boolean> = recordingManager.isPlaying
    val playbackProgress: StateFlow<CallRecordingManager.ProgressState> = recordingManager.currentProgress
    val internalStoragePath: String = recordingManager.internalStoragePath

    init {
        // Sync contacts on launch
        viewModelScope.launch {
            contactsManager.syncContacts()
        }
    }

    // Dialpad actions
    fun appendDigit(char: Char) {
        if (dtmfToneEnabled.value) {
            toneFeedbackManager.playDtmf(char)
        }
        if (vibrateFeedback.value) {
            toneFeedbackManager.vibrateKeyClick()
        }
        _dialedDigits.value += char
    }

    fun sendInCallDtmf(char: Char) {
        if (dtmfToneEnabled.value) {
            toneFeedbackManager.playDtmf(char)
        }
        if (vibrateFeedback.value) {
            toneFeedbackManager.vibrateKeyClick()
        }
    }

    fun backspace() {
        if (_dialedDigits.value.isNotEmpty()) {
            if (vibrateFeedback.value) {
                toneFeedbackManager.vibrateHeavyClick()
            }
            _dialedDigits.value = _dialedDigits.value.dropLast(1)
        }
    }

    fun clearDialpad() {
        if (vibrateFeedback.value) {
            toneFeedbackManager.vibrateLongPress()
        }
        _dialedDigits.value = ""
    }

    fun setDialedNumber(number: String) {
        _dialedDigits.value = number
    }

    fun setServiceType(serviceType: ServiceType) {
        _currentServiceType.value = serviceType
        repository.setDefaultServiceType(serviceType)
        if (vibrateFeedback.value) {
            toneFeedbackManager.vibrateSimSwitch()
        }
    }

    fun toggleServiceType() {
        val next = if (_currentServiceType.value == ServiceType.CELLULAR) {
            ServiceType.SIP_VOIP
        } else {
            ServiceType.CELLULAR
        }
        setServiceType(next)
    }

    // Dual SIM actions
    fun setSelectedSimSlot(slot: Int) {
        repository.setSelectedSimSlot(slot)
        if (vibrateFeedback.value) {
            toneFeedbackManager.vibrateSimSwitch()
        }
    }

    fun selectSimSlot(slot: Int) = setSelectedSimSlot(slot)

    fun setSimLabels(sim1: String, sim2: String) {
        repository.setSimLabels(sim1, sim2)
    }

    fun setDefaultSimPreference(pref: String) {
        repository.setDefaultSimPreference(pref)
    }

    // Calling Actions
    fun makeCall(
        number: String = _dialedDigits.value,
        contactName: String? = null,
        forcedService: ServiceType? = null,
        forcedSimSlot: Int? = null
    ) {
        val target = number.trim()
        if (target.isBlank()) return

        // Look up contact name if not passed
        val name = contactName ?: contacts.value.find {
            T9Engine.cleanNumber(it.phoneNumber) == T9Engine.cleanNumber(target)
        }?.name

        val service = forcedService ?: _currentServiceType.value
        val simSlot = if (service == ServiceType.CELLULAR) {
            forcedSimSlot ?: selectedSimSlot.value
        } else null

        if (vibrateFeedback.value) {
            toneFeedbackManager.vibrateHeavyClick()
        }

        sipCallManager.startCall(
            phoneNumber = target,
            contactName = name,
            serviceType = service,
            simSlot = simSlot,
            autoRecord = autoRecordEnabled.value,
            isStealth = stealthRecordEnabled.value
        )
    }

    fun switchActiveCallService() {
        sipCallManager.switchServiceType()
    }

    fun toggleMute() {
        sipCallManager.toggleMute()
    }

    fun toggleSpeaker() {
        sipCallManager.toggleSpeaker()
    }

    fun toggleHold() {
        sipCallManager.toggleHold()
    }

    fun toggleCallRecording() {
        sipCallManager.toggleRecording(stealthRecordEnabled.value)
    }

    fun endCall() {
        sipCallManager.endCall()
    }

    // Speed Dial actions
    fun dialSpeedSlot(slot: Int) {
        val speedContact = speedDials.value.find { it.slot == slot }
        if (speedContact != null) {
            makeCall(
                number = speedContact.phoneNumber,
                contactName = speedContact.name,
                forcedService = speedContact.serviceType
            )
        }
    }

    fun saveSpeedDial(contact: SpeedDialContact) {
        viewModelScope.launch {
            repository.setSpeedDial(contact)
        }
    }

    fun removeSpeedDial(slot: Int) {
        viewModelScope.launch {
            repository.deleteSpeedDial(slot)
        }
    }

    // SIP config
    fun saveSipAccount(account: SipAccount) {
        viewModelScope.launch {
            sipCallManager.registerSip(account)
        }
    }

    fun switchSipAccount(id: Long) {
        viewModelScope.launch {
            repository.switchSipAccount(id)
            val acc = allSipAccounts.value.find { it.id == id }
            if (acc != null) {
                sipCallManager.registerSip(acc)
            }
            if (vibrateFeedback.value) {
                toneFeedbackManager.vibrateSimSwitch()
            }
        }
    }

    fun setActiveSipAccount(id: Long) = switchSipAccount(id)

    fun deleteSipAccount(id: Long) {
        viewModelScope.launch {
            repository.deleteSipAccount(id)
        }
    }

    // Contacts
    fun syncContacts() {
        viewModelScope.launch {
            contactsManager.syncContacts()
        }
    }

    fun addNewContact(contact: Contact) {
        contactsManager.addContact(contact)
    }

    // Call Log
    fun deleteCallLog(id: Long) {
        viewModelScope.launch {
            repository.deleteCall(id)
        }
    }

    fun clearAllCallLogs() {
        viewModelScope.launch {
            repository.clearAllCalls()
        }
    }

    // Recordings & Playback
    fun playRecording(recording: CallRecording) {
        recordingManager.playRecording(recording)
    }

    fun pausePlayback() {
        recordingManager.pausePlayback()
    }

    fun resumePlayback() {
        recordingManager.resumePlayback()
    }

    fun seekPlayback(fraction: Float) {
        recordingManager.seekPlaybackTo(fraction)
    }

    fun stopPlayback() {
        recordingManager.stopPlayback()
    }

    fun deleteRecording(recording: CallRecording) {
        viewModelScope.launch {
            recordingManager.deleteRecording(recording)
        }
    }

    // Settings
    fun toggleDarkMode() {
        val current = darkModeSetting.value
        val next = when (current) {
            "dark" -> "light"
            "light" -> "dark"
            else -> "dark"
        }
        repository.setDarkModeSetting(next)
    }

    fun setDarkModeSetting(mode: String) {
        repository.setDarkModeSetting(mode)
    }

    fun setDarkMode(mode: String) = setDarkModeSetting(mode)

    fun triggerHapticFeedback(type: String = "click") {
        if (!vibrateFeedback.value) return
        when (type) {
            "heavy" -> toneFeedbackManager.vibrateHeavyClick()
            "sim" -> toneFeedbackManager.vibrateSimSwitch()
            "long" -> toneFeedbackManager.vibrateLongPress()
            "double" -> toneFeedbackManager.vibrateDoubleTick()
            else -> toneFeedbackManager.vibrateKeyClick()
        }
    }

    fun setAutoRecord(enabled: Boolean) {
        repository.setAutoRecord(enabled)
    }

    fun setStealthRecord(enabled: Boolean) {
        repository.setStealthRecord(enabled)
    }

    fun setShowRecordingsTab(show: Boolean) {
        repository.setShowRecordingsTab(show)
    }

    fun setColorTheme(themeId: String) {
        repository.setColorTheme(themeId)
    }

    fun setDtmfToneEnabled(enabled: Boolean) {
        repository.setDtmfToneEnabled(enabled)
    }

    fun setVibrateFeedback(enabled: Boolean) {
        repository.setVibrateFeedback(enabled)
    }

    fun blockNumber(phoneNumber: String, contactName: String? = null, isSpam: Boolean = false) {
        viewModelScope.launch {
            repository.blockNumber(phoneNumber, contactName, isSpam)
        }
    }

    fun unblockNumber(phoneNumber: String) {
        viewModelScope.launch {
            repository.unblockNumber(phoneNumber)
        }
    }

    override fun onCleared() {
        super.onCleared()
        recordingManager.stopPlayback()
        toneFeedbackManager.release()
    }
}
