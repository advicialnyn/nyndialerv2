package com.example.nyndialer.data

import android.content.Context
import android.content.SharedPreferences
import com.example.nyndialer.data.model.CallRecord
import com.example.nyndialer.data.model.CallRecording
import com.example.nyndialer.data.model.ServiceType
import com.example.nyndialer.data.model.SipAccount
import com.example.nyndialer.data.model.SpeedDialContact
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class DialerRepository(
    private val database: NynDialerDatabase,
    private val context: Context
) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("nyndialer_settings", Context.MODE_PRIVATE)

    val allCalls: Flow<List<CallRecord>> = database.callRecordDao().getAllCalls()
    val allSpeedDials: Flow<List<SpeedDialContact>> = database.speedDialDao().getAllSpeedDials()
    val allSipAccounts: Flow<List<SipAccount>> = database.sipAccountDao().getAllSipAccounts()
    val sipAccount: Flow<SipAccount?> = database.sipAccountDao().getActiveSipAccount()
    val activeSipAccount: Flow<SipAccount?> = database.sipAccountDao().getActiveSipAccount()
    val allRecordings: Flow<List<CallRecording>> = database.callRecordingDao().getAllRecordings()
    val allBlockedNumbers: Flow<List<com.example.nyndialer.data.model.BlockedNumber>> =
        database.blockedNumberDao().getAllBlockedNumbers()

    // Dual SIM preferences
    private val _selectedSimSlot = MutableStateFlow(prefs.getInt("selected_sim_slot", 1))
    val selectedSimSlot: StateFlow<Int> = _selectedSimSlot.asStateFlow()

    private val _sim1Label = MutableStateFlow(prefs.getString("sim_1_label", "SIM 1 (Primary)") ?: "SIM 1 (Primary)")
    val sim1Label: StateFlow<String> = _sim1Label.asStateFlow()

    private val _sim2Label = MutableStateFlow(prefs.getString("sim_2_label", "SIM 2 (Secondary)") ?: "SIM 2 (Secondary)")
    val sim2Label: StateFlow<String> = _sim2Label.asStateFlow()

    private val _defaultSimPreference = MutableStateFlow(prefs.getString("default_sim_pref", "ASK_OR_SELECT") ?: "ASK_OR_SELECT")
    val defaultSimPreference: StateFlow<String> = _defaultSimPreference.asStateFlow()

    // Settings StateFlows
    private val _colorThemeSetting = MutableStateFlow(prefs.getString("color_theme", "pitch_black") ?: "pitch_black")
    val colorThemeSetting: StateFlow<String> = _colorThemeSetting.asStateFlow()

    private val _darkModeSetting = MutableStateFlow(prefs.getString("dark_mode", "dark") ?: "dark")
    val darkModeSetting: StateFlow<String> = _darkModeSetting.asStateFlow()

    private val _dtmfToneEnabled = MutableStateFlow(prefs.getBoolean("dtmf_tones", true))
    val dtmfToneEnabled: StateFlow<Boolean> = _dtmfToneEnabled.asStateFlow()

    private val _vibrateFeedback = MutableStateFlow(prefs.getBoolean("vibrate_feedback", true))
    val vibrateFeedback: StateFlow<Boolean> = _vibrateFeedback.asStateFlow()

    private val _autoRecordEnabled = MutableStateFlow(prefs.getBoolean("auto_record", true))
    val autoRecordEnabled: StateFlow<Boolean> = _autoRecordEnabled.asStateFlow()

    private val _stealthRecordEnabled = MutableStateFlow(prefs.getBoolean("stealth_record", true))
    val stealthRecordEnabled: StateFlow<Boolean> = _stealthRecordEnabled.asStateFlow()

    private val _showRecordingsTab = MutableStateFlow(prefs.getBoolean("show_recordings_tab", true))
    val showRecordingsTab: StateFlow<Boolean> = _showRecordingsTab.asStateFlow()

    private val _defaultServiceType = MutableStateFlow(
        try {
            ServiceType.valueOf(prefs.getString("default_service", ServiceType.CELLULAR.name) ?: ServiceType.CELLULAR.name)
        } catch (_: Exception) {
            ServiceType.CELLULAR
        }
    )
    val defaultServiceType: StateFlow<ServiceType> = _defaultServiceType.asStateFlow()

    private val _lastGoogleSyncTime = MutableStateFlow(prefs.getLong("last_google_sync", System.currentTimeMillis() - 120_000))
    val lastGoogleSyncTime: StateFlow<Long> = _lastGoogleSyncTime.asStateFlow()

    fun setColorTheme(themeId: String) {
        prefs.edit().putString("color_theme", themeId).apply()
        _colorThemeSetting.value = themeId
    }

    fun setDtmfToneEnabled(enabled: Boolean) {
        prefs.edit().putBoolean("dtmf_tones", enabled).apply()
        _dtmfToneEnabled.value = enabled
    }

    fun setVibrateFeedback(enabled: Boolean) {
        prefs.edit().putBoolean("vibrate_feedback", enabled).apply()
        _vibrateFeedback.value = enabled
    }

    fun setDarkModeSetting(mode: String) {
        prefs.edit().putString("dark_mode", mode).apply()
        _darkModeSetting.value = mode
    }

    fun setAutoRecord(enabled: Boolean) {
        prefs.edit().putBoolean("auto_record", enabled).apply()
        _autoRecordEnabled.value = enabled
    }

    fun setStealthRecord(enabled: Boolean) {
        prefs.edit().putBoolean("stealth_record", enabled).apply()
        _stealthRecordEnabled.value = enabled
    }

    fun setShowRecordingsTab(show: Boolean) {
        prefs.edit().putBoolean("show_recordings_tab", show).apply()
        _showRecordingsTab.value = show
    }

    fun setDefaultServiceType(type: ServiceType) {
        prefs.edit().putString("default_service", type.name).apply()
        _defaultServiceType.value = type
    }

    fun updateLastGoogleSync(timestamp: Long = System.currentTimeMillis()) {
        prefs.edit().putLong("last_google_sync", timestamp).apply()
        _lastGoogleSyncTime.value = timestamp
    }

    suspend fun insertCall(call: CallRecord): Long =
        database.callRecordDao().insertCall(call)

    suspend fun deleteCall(id: Long) =
        database.callRecordDao().deleteCall(id)

    suspend fun clearAllCalls() =
        database.callRecordDao().clearAllCalls()

    suspend fun setSpeedDial(contact: SpeedDialContact) =
        database.speedDialDao().insertSpeedDial(contact)

    suspend fun deleteSpeedDial(slot: Int) =
        database.speedDialDao().deleteSpeedDial(slot)

    suspend fun saveSipAccount(account: SipAccount) =
        database.sipAccountDao().saveSipAccount(account)

    suspend fun insertRecording(recording: CallRecording): Long =
        database.callRecordingDao().insertRecording(recording)

    suspend fun deleteRecording(id: Long) =
        database.callRecordingDao().deleteRecording(id)

    suspend fun blockNumber(phoneNumber: String, contactName: String? = null, isSpam: Boolean = false): Long {
        return database.blockedNumberDao().blockNumber(
            com.example.nyndialer.data.model.BlockedNumber(
                phoneNumber = phoneNumber,
                contactName = contactName,
                isSpam = isSpam
            )
        )
    }

    suspend fun unblockNumber(phoneNumber: String) {
        database.blockedNumberDao().unblockNumber(phoneNumber)
    }

    fun setSelectedSimSlot(slot: Int) {
        prefs.edit().putInt("selected_sim_slot", slot).apply()
        _selectedSimSlot.value = slot
    }

    fun setSimLabels(sim1: String, sim2: String) {
        prefs.edit().putString("sim_1_label", sim1).putString("sim_2_label", sim2).apply()
        _sim1Label.value = sim1
        _sim2Label.value = sim2
    }

    fun setDefaultSimPreference(pref: String) {
        prefs.edit().putString("default_sim_pref", pref).apply()
        _defaultSimPreference.value = pref
    }

    suspend fun switchSipAccount(id: Long) {
        database.sipAccountDao().deactivateAll()
        database.sipAccountDao().activateAccount(id)
    }

    suspend fun deleteSipAccount(id: Long) {
        database.sipAccountDao().deleteSipAccount(id)
    }

    fun isNumberBlocked(phoneNumber: String): Flow<Boolean> =
        database.blockedNumberDao().isNumberBlocked(phoneNumber)
}
