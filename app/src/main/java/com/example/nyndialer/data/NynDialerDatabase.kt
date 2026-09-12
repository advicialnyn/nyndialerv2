package com.example.nyndialer.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.nyndialer.data.dao.BlockedNumberDao
import com.example.nyndialer.data.dao.CallRecordDao
import com.example.nyndialer.data.dao.CallRecordingDao
import com.example.nyndialer.data.dao.SipAccountDao
import com.example.nyndialer.data.dao.SpeedDialDao
import com.example.nyndialer.data.model.BlockedNumber
import com.example.nyndialer.data.model.CallRecord
import com.example.nyndialer.data.model.CallRecording
import com.example.nyndialer.data.model.CallType
import com.example.nyndialer.data.model.ServiceType
import com.example.nyndialer.data.model.SipAccount
import com.example.nyndialer.data.model.SpeedDialContact
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        CallRecord::class,
        BlockedNumber::class,
        SpeedDialContact::class,
        SipAccount::class,
        CallRecording::class
    ],
    version = 2,
    exportSchema = false
)
abstract class NynDialerDatabase : RoomDatabase() {
    abstract fun callRecordDao(): CallRecordDao
    fun callHistoryDao(): CallRecordDao = callRecordDao()
    abstract fun blockedNumberDao(): BlockedNumberDao
    abstract fun speedDialDao(): SpeedDialDao
    abstract fun sipAccountDao(): SipAccountDao
    abstract fun callRecordingDao(): CallRecordingDao

    companion object {
        @Volatile
        private var INSTANCE: NynDialerDatabase? = null

        fun getInstance(context: Context): NynDialerDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    NynDialerDatabase::class.java,
                    "nyndialer_database.db"
                ).fallbackToDestructiveMigration()
                .addCallback(object : Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        // Pre-populate with initial speed dials and SIP account
                        CoroutineScope(Dispatchers.IO).launch {
                            val database = getInstance(context)
                            populateInitialData(database)
                        }
                    }
                }).build()
                INSTANCE = instance
                instance
            }
        }

        private suspend fun populateInitialData(db: NynDialerDatabase) {
            val speedDialDao = db.speedDialDao()
            speedDialDao.insertSpeedDial(
                SpeedDialContact(
                    slot = 1,
                    name = "Voicemail",
                    phoneNumber = "*86",
                    serviceType = ServiceType.CELLULAR,
                    avatarColorHex = "#3B82F6"
                )
            )
            speedDialDao.insertSpeedDial(
                SpeedDialContact(
                    slot = 2,
                    name = "Mom",
                    phoneNumber = "+1 (555) 234-5678",
                    serviceType = ServiceType.CELLULAR,
                    avatarColorHex = "#EC4899"
                )
            )
            speedDialDao.insertSpeedDial(
                SpeedDialContact(
                    slot = 3,
                    name = "Office VoIP",
                    phoneNumber = "1002@sip.antisip.com",
                    serviceType = ServiceType.SIP_VOIP,
                    avatarColorHex = "#06B6D4"
                )
            )
            speedDialDao.insertSpeedDial(
                SpeedDialContact(
                    slot = 4,
                    name = "Alex Rivera",
                    phoneNumber = "+1 (555) 876-5432",
                    serviceType = ServiceType.CELLULAR,
                    avatarColorHex = "#10B981"
                )
            )
            speedDialDao.insertSpeedDial(
                SpeedDialContact(
                    slot = 5,
                    name = "Tech Support SIP",
                    phoneNumber = "support@sip.antisip.com",
                    serviceType = ServiceType.SIP_VOIP,
                    avatarColorHex = "#8B5CF6"
                )
            )

            val sipDao = db.sipAccountDao()
            sipDao.saveSipAccount(
                SipAccount(
                    id = 1,
                    username = "nayan_work",
                    displayName = "Work VoIP",
                    domain = "sip.antisip.com",
                    port = 5060,
                    password = "••••••••",
                    transport = "UDP",
                    isRegistered = true,
                    isActive = true,
                    registeredAt = System.currentTimeMillis()
                )
            )
            sipDao.saveSipAccount(
                SipAccount(
                    id = 2,
                    username = "nayan_home",
                    displayName = "Personal SIP",
                    domain = "sip.linphone.org",
                    port = 5060,
                    password = "••••••••",
                    transport = "TLS",
                    isRegistered = true,
                    isActive = false,
                    registeredAt = System.currentTimeMillis()
                )
            )

            // Seed a few initial recent call logs
            val callDao = db.callRecordDao()
            val now = System.currentTimeMillis()
            callDao.insertCall(
                CallRecord(
                    phoneNumber = "+1 (555) 234-5678",
                    contactName = "Mom",
                    callType = CallType.INCOMING,
                    serviceType = ServiceType.CELLULAR,
                    timestamp = now - (15 * 60 * 1000), // 15 mins ago
                    duration = 142L,
                    durationSeconds = 142,
                    recordingFilePath = null,
                    recordingPath = null
                )
            )
            callDao.insertCall(
                CallRecord(
                    phoneNumber = "1002@sip.antisip.com",
                    contactName = "Office VoIP",
                    callType = CallType.OUTGOING,
                    serviceType = ServiceType.SIP_VOIP,
                    timestamp = now - (2 * 3600 * 1000), // 2 hours ago
                    duration = 380L,
                    durationSeconds = 380,
                    recordingFilePath = "/data/user/0/com.aistudio.nyndialer.krvq/files/call_recordings/rec_office_voip.m4a",
                    recordingPath = "/data/user/0/com.aistudio.nyndialer.krvq/files/call_recordings/rec_office_voip.m4a"
                )
            )
            callDao.insertCall(
                CallRecord(
                    phoneNumber = "+1 (555) 876-5432",
                    contactName = "Alex Rivera",
                    callType = CallType.MISSED,
                    serviceType = ServiceType.CELLULAR,
                    timestamp = now - (5 * 3600 * 1000), // 5 hours ago
                    duration = 0L,
                    durationSeconds = 0,
                    recordingFilePath = null,
                    recordingPath = null
                )
            )

            // Pre-populate a spam blocked number
            val blockedDao = db.blockedNumberDao()
            blockedDao.blockNumber(
                BlockedNumber(
                    phoneNumber = "+1 (800) 555-0199",
                    contactName = "Robocall Telemarketer",
                    blockedAt = now - (24 * 3600 * 1000),
                    isSpam = true
                )
            )
        }
    }
}
