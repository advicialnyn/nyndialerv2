package com.example.nyndialer.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.nyndialer.data.model.BlockedNumber
import com.example.nyndialer.data.model.CallRecord
import com.example.nyndialer.data.model.CallRecording
import com.example.nyndialer.data.model.CallType
import com.example.nyndialer.data.model.SipAccount
import com.example.nyndialer.data.model.SpeedDialContact
import kotlinx.coroutines.flow.Flow

/**
 * Room Data Access Object for local call history.
 * Supports querying by phone number, duration, timestamp, recording path, and call type.
 */
@Dao
interface CallRecordDao {
    @Query("SELECT * FROM call_records ORDER BY timestamp DESC")
    fun getAllCalls(): Flow<List<CallRecord>>

    @Query("SELECT * FROM call_records WHERE id = :id LIMIT 1")
    fun getCallById(id: Long): Flow<CallRecord?>

    @Query("SELECT * FROM call_records WHERE phoneNumber = :phoneNumber ORDER BY timestamp DESC")
    fun getCallsForNumber(phoneNumber: String): Flow<List<CallRecord>>

    @Query("SELECT * FROM call_records WHERE callType = :callType ORDER BY timestamp DESC")
    fun getCallsByType(callType: CallType): Flow<List<CallRecord>>

    @Query("SELECT * FROM call_records WHERE callType = 'MISSED' ORDER BY timestamp DESC")
    fun getMissedCalls(): Flow<List<CallRecord>>

    @Query("SELECT * FROM call_records WHERE (recordingPath IS NOT NULL OR recordingFilePath IS NOT NULL) ORDER BY timestamp DESC")
    fun getRecordedCalls(): Flow<List<CallRecord>>

    @Query("SELECT * FROM call_records WHERE phoneNumber LIKE '%' || :query || '%' OR contactName LIKE '%' || :query || '%' ORDER BY timestamp DESC")
    fun searchCalls(query: String): Flow<List<CallRecord>>

    @Query("SELECT COUNT(*) FROM call_records")
    fun getCallCount(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCall(call: CallRecord): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCalls(calls: List<CallRecord>): List<Long>

    @Update
    suspend fun updateCall(call: CallRecord)

    @Query("DELETE FROM call_records WHERE id = :id")
    suspend fun deleteCall(id: Long)

    @Query("DELETE FROM call_records WHERE phoneNumber = :phoneNumber")
    suspend fun deleteCallsForNumber(phoneNumber: String)

    @Query("DELETE FROM call_records")
    suspend fun clearAllCalls()
}

typealias CallHistoryDao = CallRecordDao

@Dao
interface BlockedNumberDao {
    @Query("SELECT * FROM blocked_numbers ORDER BY blockedAt DESC")
    fun getAllBlockedNumbers(): Flow<List<BlockedNumber>>

    @Query("SELECT EXISTS(SELECT 1 FROM blocked_numbers WHERE phoneNumber = :phoneNumber)")
    fun isNumberBlocked(phoneNumber: String): Flow<Boolean>

    @Query("SELECT EXISTS(SELECT 1 FROM blocked_numbers WHERE phoneNumber = :phoneNumber)")
    suspend fun isNumberBlockedDirect(phoneNumber: String): Boolean

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun blockNumber(blockedNumber: BlockedNumber): Long

    @Query("DELETE FROM blocked_numbers WHERE phoneNumber = :phoneNumber")
    suspend fun unblockNumber(phoneNumber: String)

    @Query("DELETE FROM blocked_numbers WHERE id = :id")
    suspend fun deleteBlockedNumber(id: Long)
}

@Dao
interface SpeedDialDao {
    @Query("SELECT * FROM speed_dials ORDER BY slot ASC")
    fun getAllSpeedDials(): Flow<List<SpeedDialContact>>

    @Query("SELECT * FROM speed_dials WHERE slot = :slot LIMIT 1")
    suspend fun getSpeedDial(slot: Int): SpeedDialContact?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSpeedDial(speedDial: SpeedDialContact)

    @Query("DELETE FROM speed_dials WHERE slot = :slot")
    suspend fun deleteSpeedDial(slot: Int)
}

@Dao
interface SipAccountDao {
    @Query("SELECT * FROM sip_accounts ORDER BY id ASC")
    fun getAllSipAccounts(): Flow<List<SipAccount>>

    @Query("SELECT * FROM sip_accounts WHERE isActive = 1 LIMIT 1")
    fun getActiveSipAccount(): Flow<SipAccount?>

    @Query("SELECT * FROM sip_accounts WHERE isActive = 1 LIMIT 1")
    suspend fun getActiveSipAccountDirect(): SipAccount?

    @Query("SELECT * FROM sip_accounts WHERE id = :id LIMIT 1")
    suspend fun getSipAccountById(id: Long): SipAccount?

    @Query("SELECT * FROM sip_accounts WHERE id = 1 LIMIT 1")
    fun getSipAccount(): Flow<SipAccount?>

    @Query("SELECT * FROM sip_accounts WHERE id = 1 LIMIT 1")
    suspend fun getSipAccountDirect(): SipAccount?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveSipAccount(account: SipAccount): Long

    @Query("UPDATE sip_accounts SET isActive = 0")
    suspend fun deactivateAll()

    @Query("UPDATE sip_accounts SET isActive = 1 WHERE id = :id")
    suspend fun activateAccount(id: Long)

    @Query("DELETE FROM sip_accounts WHERE id = :id")
    suspend fun deleteSipAccount(id: Long)
}

@Dao
interface CallRecordingDao {
    @Query("SELECT * FROM call_recordings ORDER BY timestamp DESC")
    fun getAllRecordings(): Flow<List<CallRecording>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecording(recording: CallRecording): Long

    @Query("DELETE FROM call_recordings WHERE id = :id")
    suspend fun deleteRecording(id: Long)
}

