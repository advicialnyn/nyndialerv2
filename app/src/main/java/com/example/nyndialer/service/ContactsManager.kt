package com.example.nyndialer.service

import android.content.Context
import android.content.pm.PackageManager
import android.provider.ContactsContract
import androidx.core.content.ContextCompat
import com.example.nyndialer.data.DialerRepository
import com.example.nyndialer.domain.Contact
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext

class ContactsManager(
    private val context: Context,
    private val repository: DialerRepository
) {
    private val _contacts = MutableStateFlow<List<Contact>>(emptyList())
    val contacts: StateFlow<List<Contact>> = _contacts.asStateFlow()

    private val _isSyncing = MutableStateFlow(false)
    val isSyncing: StateFlow<Boolean> = _isSyncing.asStateFlow()

    private val defaultGoogleContacts = listOf(
        Contact(
            id = "g_1",
            name = "Mom",
            phoneNumber = "+1 (555) 234-5678",
            email = "mom@family.org",
            sourceAccount = "Google (nayanmridha24@gmail.com)",
            isGoogleContact = true,
            avatarColorHex = "#EC4899"
        ),
        Contact(
            id = "g_2",
            name = "Alex Rivera",
            phoneNumber = "+1 (555) 876-5432",
            email = "alex.rivera@techcorp.io",
            sourceAccount = "Google (nayanmridha24@gmail.com)",
            isGoogleContact = true,
            avatarColorHex = "#10B981"
        ),
        Contact(
            id = "g_3",
            name = "Sarah Chen",
            phoneNumber = "+1 (555) 345-6789",
            email = "sarah.chen@google.com",
            sourceAccount = "Google (nayanmridha24@gmail.com)",
            isGoogleContact = true,
            avatarColorHex = "#F59E0B"
        ),
        Contact(
            id = "g_4",
            name = "David Kim",
            phoneNumber = "+1 (555) 987-6543",
            email = "dkim@startup.dev",
            sourceAccount = "Google (nayanmridha24@gmail.com)",
            isGoogleContact = true,
            avatarColorHex = "#6366F1"
        ),
        Contact(
            id = "g_5",
            name = "Elena Rostova",
            phoneNumber = "+1 (555) 432-1098",
            email = "elena.r@designstudio.net",
            sourceAccount = "Google (nayanmridha24@gmail.com)",
            isGoogleContact = true,
            avatarColorHex = "#8B5CF6"
        ),
        Contact(
            id = "g_6",
            name = "Marcus Brody",
            phoneNumber = "+1 (555) 654-3210",
            email = "mbrody@financehub.com",
            sourceAccount = "Google (nayanmridha24@gmail.com)",
            isGoogleContact = true,
            avatarColorHex = "#3B82F6"
        ),
        Contact(
            id = "g_7",
            name = "Office VoIP PBX",
            phoneNumber = "1002@sip.antisip.com",
            email = "pbx@office-net.org",
            sourceAccount = "Google (nayanmridha24@gmail.com)",
            isGoogleContact = true,
            avatarColorHex = "#06B6D4",
            isSipUri = true
        ),
        Contact(
            id = "g_8",
            name = "Tech Support SIP",
            phoneNumber = "support@sip.antisip.com",
            email = "support@sip.antisip.com",
            sourceAccount = "SIP Directory",
            isGoogleContact = false,
            avatarColorHex = "#14B8A6",
            isSipUri = true
        ),
        Contact(
            id = "g_9",
            name = "Nayon Mridha",
            phoneNumber = "+1 (555) 019-2834",
            email = "nayanmridha24@gmail.com",
            sourceAccount = "Google (nayanmridha24@gmail.com)",
            isGoogleContact = true,
            avatarColorHex = "#00E676"
        )
    )

    init {
        _contacts.value = defaultGoogleContacts
    }

    suspend fun syncContacts() {
        _isSyncing.value = true
        withContext(Dispatchers.IO) {
            try {
                val hasPermission = ContextCompat.checkSelfPermission(
                    context,
                    android.Manifest.permission.READ_CONTACTS
                ) == PackageManager.PERMISSION_GRANTED

                val loaded = mutableListOf<Contact>()
                if (hasPermission) {
                    val cursor = context.contentResolver.query(
                        ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                        arrayOf(
                            ContactsContract.CommonDataKinds.Phone.CONTACT_ID,
                            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                            ContactsContract.CommonDataKinds.Phone.NUMBER
                        ),
                        null,
                        null,
                        "${ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME} ASC"
                    )

                    cursor?.use { c ->
                        val nameCol = c.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
                        val numCol = c.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
                        val idCol = c.getColumnIndex(ContactsContract.CommonDataKinds.Phone.CONTACT_ID)

                        val palette = listOf("#00E676", "#06B6D4", "#3B82F6", "#8B5CF6", "#EC4899", "#F59E0B", "#10B981")
                        var idx = 0

                        while (c.moveToNext()) {
                            val name = if (nameCol >= 0) c.getString(nameCol) ?: "Unknown" else "Unknown"
                            val num = if (numCol >= 0) c.getString(numCol) ?: "" else ""
                            val id = if (idCol >= 0) c.getString(idCol) ?: "c_$idx" else "c_$idx"

                            if (num.isNotBlank() && loaded.none { it.phoneNumber == num }) {
                                loaded.add(
                                    Contact(
                                        id = "device_$id",
                                        name = name,
                                        phoneNumber = num,
                                        sourceAccount = "Google (nayanmridha24@gmail.com)",
                                        isGoogleContact = true,
                                        avatarColorHex = palette[idx % palette.size],
                                        isSipUri = num.contains("@")
                                    )
                                )
                                idx++
                            }
                        }
                    }
                }

                // If device returned contacts, merge with default contacts (ensuring user's contacts are present)
                if (loaded.isNotEmpty()) {
                    val merged = (loaded + defaultGoogleContacts).distinctBy { it.phoneNumber }
                    _contacts.value = merged.sortedBy { it.name }
                } else {
                    _contacts.value = defaultGoogleContacts.sortedBy { it.name }
                }

                repository.updateLastGoogleSync(System.currentTimeMillis())
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isSyncing.value = false
            }
        }
    }

    fun addContact(contact: Contact) {
        val current = _contacts.value.toMutableList()
        current.add(0, contact)
        _contacts.value = current.sortedBy { it.name }
    }
}
