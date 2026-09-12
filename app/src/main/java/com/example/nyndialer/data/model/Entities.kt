package com.example.nyndialer.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

enum class CallType {
    INCOMING,
    OUTGOING,
    MISSED,
    REJECTED
}

enum class ServiceType {
    CELLULAR,
    SIP_VOIP
}

/**
 * Local call history Room entity.
 * Supports: phone number, duration, timestamp, and recording file path.
 */
@Entity(
    tableName = "call_records",
    indices = [
        Index(value = ["phoneNumber"]),
        Index(value = ["timestamp"])
    ]
)
data class CallRecord(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val phoneNumber: String,
    val contactName: String? = null,
    val callType: CallType = CallType.OUTGOING,
    val serviceType: ServiceType = ServiceType.CELLULAR,
    val simSlot: Int? = null,                   // 1 for SIM 1, 2 for SIM 2, null for VoIP
    val timestamp: Long = System.currentTimeMillis(),
    val duration: Long = 0L,                    // "duration" in seconds
    val durationSeconds: Int = 0,               // convenience accessor
    val recordingFilePath: String? = null,      // "recording file path"
    val recordingPath: String? = null,          // backwards compatibility accessor
    val isStarred: Boolean = false,
    val isSpam: Boolean = false,
    val note: String? = null
) {
    val effectiveDurationSec: Long
        get() = if (duration > 0L) duration else durationSeconds.toLong()

    val effectiveRecordingPath: String?
        get() = recordingFilePath ?: recordingPath
}

typealias CallHistory = CallRecord

@Entity(
    tableName = "blocked_numbers",
    indices = [Index(value = ["phoneNumber"], unique = true)]
)
data class BlockedNumber(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val phoneNumber: String,
    val contactName: String? = null,
    val blockedAt: Long = System.currentTimeMillis(),
    val isSpam: Boolean = false
)

@Entity(tableName = "speed_dials")
data class SpeedDialContact(
    @PrimaryKey
    val slot: Int, // 1 to 9
    val name: String,
    val phoneNumber: String,
    val serviceType: ServiceType = ServiceType.CELLULAR,
    val avatarColorHex: String = "#00E676"
)

@Entity(tableName = "sip_accounts")
data class SipAccount(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val username: String = "nayan24",
    val displayName: String = "Primary VoIP",
    val domain: String = "sip.antisip.com",
    val port: Int = 5060,
    val password: String = "account_auth",
    val transport: String = "UDP", // UDP, TCP, TLS
    val isRegistered: Boolean = true,
    val isActive: Boolean = true,
    val registeredAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "call_recordings")
data class CallRecording(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val filePath: String,
    val fileName: String,
    val phoneNumber: String,
    val contactName: String?,
    val serviceType: ServiceType = ServiceType.CELLULAR,
    val timestamp: Long = System.currentTimeMillis(),
    val durationMillis: Long = 0L,
    val fileSizeBytes: Long = 0L,
    val isStealthRecorded: Boolean = true
)

