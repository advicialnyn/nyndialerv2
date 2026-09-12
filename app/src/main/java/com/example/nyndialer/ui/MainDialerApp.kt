package com.example.nyndialer.ui

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Contacts
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Dialpad
import androidx.compose.material.icons.filled.FiberManualRecord
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.outlined.Contacts
import androidx.compose.material.icons.outlined.Dialpad
import androidx.compose.material.icons.outlined.GraphicEq
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Speed
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.nyndialer.data.model.ServiceType
import com.example.nyndialer.data.model.SpeedDialContact
import com.example.nyndialer.domain.Contact
import com.example.nyndialer.ui.components.DialerSettingsDialog
import com.example.nyndialer.ui.components.SipConfigDialog
import com.example.nyndialer.ui.components.SpeedDialEditDialog
import com.example.nyndialer.ui.screens.ActiveCallScreen
import com.example.nyndialer.ui.screens.CallLogScreen
import com.example.nyndialer.ui.screens.ContactsScreen
import com.example.nyndialer.ui.screens.DialerScreen
import com.example.nyndialer.ui.screens.RecordingsScreen
import com.example.nyndialer.ui.screens.SpeedDialScreen
import com.example.ui.theme.DialEmerald
import com.example.ui.theme.HangupRed
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.SipCyan

enum class NavTab(val title: String, val activeIcon: ImageVector, val inactiveIcon: ImageVector) {
    RECENTS("Recents", Icons.Filled.History, Icons.Outlined.History),
    KEYPAD("Dialer", Icons.Filled.Dialpad, Icons.Outlined.Dialpad),
    CONTACTS("Contacts", Icons.Filled.Contacts, Icons.Outlined.Contacts),
    SPEED_DIAL("Speed Dial", Icons.Filled.Speed, Icons.Outlined.Speed),
    RECORDINGS("Recordings", Icons.Filled.GraphicEq, Icons.Outlined.GraphicEq)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainDialerApp(viewModel: DialerViewModel = viewModel()) {
    val context = LocalContext.current

    // Permission launcher for Call, Contacts, Audio Recording
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions[Manifest.permission.READ_CONTACTS] == true) {
            viewModel.syncContacts()
        }
    }

    LaunchedEffect(Unit) {
        val permissionsToRequest = mutableListOf<String>()
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            permissionsToRequest.add(Manifest.permission.READ_CONTACTS)
        }
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            permissionsToRequest.add(Manifest.permission.RECORD_AUDIO)
        }
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            permissionsToRequest.add(Manifest.permission.CALL_PHONE)
        }
        if (permissionsToRequest.isNotEmpty()) {
            permissionLauncher.launch(permissionsToRequest.toTypedArray())
        }
    }

    val darkModeSetting by viewModel.darkModeSetting.collectAsStateWithLifecycle()
    val colorThemeSetting by viewModel.colorThemeSetting.collectAsStateWithLifecycle()
    val isSystemDark = isSystemInDarkTheme()
    val isDarkTheme = when (darkModeSetting) {
        "dark" -> true
        "light" -> false
        else -> isSystemDark
    }

    MyApplicationTheme(themeId = colorThemeSetting, darkTheme = isDarkTheme) {
        // State collection
        val dialedNumber by viewModel.dialedDigits.collectAsStateWithLifecycle()
        val currentService by viewModel.currentServiceType.collectAsStateWithLifecycle()
        val sipAccount by viewModel.sipAccount.collectAsStateWithLifecycle()
        val allSipAccounts by viewModel.allSipAccounts.collectAsStateWithLifecycle()
        val selectedSimSlot by viewModel.selectedSimSlot.collectAsStateWithLifecycle()
        val sim1Label by viewModel.sim1Label.collectAsStateWithLifecycle()
        val sim2Label by viewModel.sim2Label.collectAsStateWithLifecycle()
        val isSipOnline by viewModel.isSipOnline.collectAsStateWithLifecycle()
        val t9Matches by viewModel.t9SearchResults.collectAsStateWithLifecycle()
        val contacts by viewModel.contacts.collectAsStateWithLifecycle()
        val isSyncingContacts by viewModel.isSyncingContacts.collectAsStateWithLifecycle()
        val lastGoogleSyncTime by viewModel.lastGoogleSyncTime.collectAsStateWithLifecycle()
        val speedDials by viewModel.speedDials.collectAsStateWithLifecycle()
        val callLogs by viewModel.callLogs.collectAsStateWithLifecycle()
        val recordings by viewModel.recordings.collectAsStateWithLifecycle()
        val autoRecordEnabled by viewModel.autoRecordEnabled.collectAsStateWithLifecycle()
        val stealthModeEnabled by viewModel.stealthRecordEnabled.collectAsStateWithLifecycle()
        val showRecordingsTab by viewModel.showRecordingsTab.collectAsStateWithLifecycle()
        val activeCall by viewModel.activeCall.collectAsStateWithLifecycle()

        val blockedNumbers by viewModel.blockedNumbers.collectAsStateWithLifecycle()
        val dtmfEnabled by viewModel.dtmfToneEnabled.collectAsStateWithLifecycle()
        val vibrateEnabled by viewModel.vibrateFeedback.collectAsStateWithLifecycle()

        val playingRecordingId by viewModel.playingRecordingId.collectAsStateWithLifecycle()
        val isPlayingAudio by viewModel.isPlayingAudio.collectAsStateWithLifecycle()
        val playbackProgress by viewModel.playbackProgress.collectAsStateWithLifecycle()

        // Dynamic tabs based on user preference (Recordings tab toggleable)
        val visibleTabs = remember(showRecordingsTab) {
            if (showRecordingsTab) {
                listOf(NavTab.RECENTS, NavTab.KEYPAD, NavTab.CONTACTS, NavTab.SPEED_DIAL, NavTab.RECORDINGS)
            } else {
                listOf(NavTab.RECENTS, NavTab.KEYPAD, NavTab.CONTACTS, NavTab.SPEED_DIAL)
            }
        }

        // Gesture Navigation Pager (initial page 0 = RECENTS)
        val pagerState = rememberPagerState(
            initialPage = 0,
            pageCount = { visibleTabs.size }
        )
        val coroutineScope = rememberCoroutineScope()

        // Dialog states
        var showSettingsDialog by remember { mutableStateOf(false) }
        var showSipConfigDialog by remember { mutableStateOf(false) }
        var editingSpeedDialSlot by remember { mutableStateOf<Int?>(null) }
        var editingSpeedDialContact by remember { mutableStateOf<SpeedDialContact?>(null) }

        Scaffold(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.statusBars),
            topBar = {
                // Sleek Minimal Top App Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Logo & App Name
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = DialEmerald,
                            modifier = Modifier.size(10.dp)
                        ) {}
                        Text(
                            text = "nyndialer",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = (-0.5).sp,
                            color = MaterialTheme.colorScheme.onBackground
                        )

                        // Mode chip
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (currentService == ServiceType.SIP_VOIP) SipCyan.copy(alpha = 0.15f) else DialEmerald.copy(alpha = 0.15f),
                            modifier = Modifier.padding(start = 4.dp)
                        ) {
                            Text(
                                text = if (currentService == ServiceType.SIP_VOIP) "SIP VOIP" else "CELLULAR",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (currentService == ServiceType.SIP_VOIP) SipCyan else DialEmerald,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    // Actions: Color Themes, Dark mode, & Settings
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        // Themes & Settings Button
                        IconButton(
                            onClick = { showSettingsDialog = true },
                            modifier = Modifier
                                .size(38.dp)
                                .testTag("theme_picker_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Palette,
                                contentDescription = "Themes & Customization",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        // Dark mode toggle
                        IconButton(
                            onClick = { viewModel.toggleDarkMode() },
                            modifier = Modifier
                                .size(38.dp)
                                .testTag("theme_toggle_button")
                        ) {
                            Icon(
                                imageVector = if (isDarkTheme) Icons.Default.LightMode else Icons.Default.DarkMode,
                                contentDescription = "Toggle Dark Mode",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        // SIP VoIP Config shortcut
                        IconButton(
                            onClick = { showSipConfigDialog = true },
                            modifier = Modifier
                                .size(38.dp)
                                .testTag("top_sip_config_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = "SIP Configuration",
                                tint = if (isSipOnline) SipCyan else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            },
            bottomBar = {
                // Bottom Navigation Bar with consistent window insets
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    modifier = Modifier
                        .windowInsetsPadding(WindowInsets.navigationBars)
                        .testTag("main_bottom_nav")
                ) {
                    visibleTabs.forEachIndexed { index, tab ->
                        val selected = pagerState.currentPage == index
                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                coroutineScope.launch {
                                    pagerState.animateScrollToPage(index)
                                }
                            },
                            icon = {
                                Icon(
                                    imageVector = if (selected) tab.activeIcon else tab.inactiveIcon,
                                    contentDescription = tab.title,
                                    modifier = Modifier.size(22.dp)
                                )
                            },
                            label = {
                                Text(
                                    text = tab.title,
                                    fontSize = 11.sp,
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Color.Black,
                                indicatorColor = DialEmerald,
                                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            modifier = Modifier.testTag("nav_tab_${tab.name.lowercase()}")
                        )
                    }
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                // Tab Content with Gesture Swiping (HorizontalPager)
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.fillMaxSize()
                ) { pageIndex ->
                    when (visibleTabs.getOrNull(pageIndex)) {
                        NavTab.RECENTS -> {
                            CallLogScreen(
                                callLogs = callLogs,
                                onCallBack = { number, name, service ->
                                    viewModel.makeCall(number, name, service)
                                },
                                onDeleteCall = { id -> viewModel.deleteCallLog(id) },
                                onClearAll = { viewModel.clearAllCallLogs() },
                                onOpenRecording = {
                                    if (!showRecordingsTab) {
                                        viewModel.setShowRecordingsTab(true)
                                    }
                                    coroutineScope.launch {
                                        val targetIdx = visibleTabs.indexOf(NavTab.RECORDINGS).takeIf { it >= 0 } ?: (visibleTabs.size - 1)
                                        pagerState.animateScrollToPage(targetIdx)
                                    }
                                },
                                onBlockNumber = { number, name ->
                                    viewModel.blockNumber(number, name, isSpam = true)
                                }
                            )
                        }
                        NavTab.KEYPAD -> {
                            DialerScreen(
                                dialedNumber = dialedNumber,
                                currentService = currentService,
                                sipAccount = sipAccount,
                                allSipAccounts = allSipAccounts,
                                selectedSimSlot = selectedSimSlot,
                                sim1Label = sim1Label,
                                sim2Label = sim2Label,
                                isSipOnline = isSipOnline,
                                t9Matches = t9Matches,
                                onDigitClick = { digit -> viewModel.appendDigit(digit) },
                                onDigitLongClick = { digit ->
                                    if (digit in '1'..'9') {
                                        val slot = digit.digitToInt()
                                        viewModel.dialSpeedSlot(slot)
                                    } else if (digit == '0') {
                                        viewModel.appendDigit('+')
                                    }
                                },
                                onBackspaceClick = { viewModel.backspace() },
                                onBackspaceLongClick = { viewModel.clearDialpad() },
                                onServiceToggle = { viewModel.toggleServiceType() },
                                onSelectSimSlot = { viewModel.selectSimSlot(it) },
                                onSelectSipAccount = { viewModel.setActiveSipAccount(it) },
                                onCallClick = { viewModel.makeCall() },
                                onMatchClick = { number, name ->
                                    viewModel.setDialedNumber(number)
                                    viewModel.makeCall(number, name)
                                },
                                onOpenSipSettings = { showSipConfigDialog = true }
                            )
                        }
                        NavTab.CONTACTS -> {
                            ContactsScreen(
                                contacts = contacts,
                                isSyncing = isSyncingContacts,
                                lastSyncTime = lastGoogleSyncTime,
                                onSyncClick = { viewModel.syncContacts() },
                                onCallCellular = { contact ->
                                    viewModel.makeCall(
                                        number = contact.phoneNumber,
                                        contactName = contact.name,
                                        forcedService = ServiceType.CELLULAR
                                    )
                                },
                                onCallSip = { contact ->
                                    viewModel.makeCall(
                                        number = contact.phoneNumber,
                                        contactName = contact.name,
                                        forcedService = ServiceType.SIP_VOIP
                                    )
                                },
                                onAssignSpeedDial = { contact ->
                                    editingSpeedDialSlot = 1
                                    editingSpeedDialContact = SpeedDialContact(
                                        slot = 1,
                                        name = contact.name,
                                        phoneNumber = contact.phoneNumber,
                                        serviceType = if (contact.isSipUri) ServiceType.SIP_VOIP else ServiceType.CELLULAR,
                                        avatarColorHex = contact.avatarColorHex
                                    )
                                },
                                onAddNewContact = { newContact ->
                                    viewModel.addNewContact(newContact)
                                }
                            )
                        }
                        NavTab.SPEED_DIAL -> {
                            SpeedDialScreen(
                                speedDials = speedDials,
                                onDialSlot = { slot -> viewModel.dialSpeedSlot(slot) },
                                onEditSlot = { slot, contact ->
                                    editingSpeedDialSlot = slot
                                    editingSpeedDialContact = contact
                                }
                            )
                        }
                        NavTab.RECORDINGS -> {
                            RecordingsScreen(
                                recordings = recordings,
                                autoRecordEnabled = autoRecordEnabled,
                                stealthModeEnabled = stealthModeEnabled,
                                storagePath = viewModel.internalStoragePath,
                                playingRecordingId = playingRecordingId,
                                isPlayingAudio = isPlayingAudio,
                                playbackProgress = playbackProgress,
                                onToggleAutoRecord = { enabled -> viewModel.setAutoRecord(enabled) },
                                onToggleStealthMode = { enabled -> viewModel.setStealthRecord(enabled) },
                                onPlayRecording = { rec -> viewModel.playRecording(rec) },
                                onPausePlayback = { viewModel.pausePlayback() },
                                onSeekPlayback = { fraction -> viewModel.seekPlayback(fraction) },
                                onDeleteRecording = { rec -> viewModel.deleteRecording(rec) }
                            )
                        }
                        null -> {}
                    }
                }

                // Active Call Full-Screen Overlay
                AnimatedVisibility(
                    visible = activeCall != null,
                    enter = slideInVertically(initialOffsetY = { it }),
                    exit = slideOutVertically(targetOffsetY = { it })
                ) {
                    activeCall?.let { callInfo ->
                        ActiveCallScreen(
                            callInfo = callInfo,
                            onSwitchService = { viewModel.switchActiveCallService() },
                            onToggleMute = { viewModel.toggleMute() },
                            onToggleSpeaker = { viewModel.toggleSpeaker() },
                            onToggleHold = { viewModel.toggleHold() },
                            onToggleRecord = { viewModel.toggleCallRecording() },
                            onEndCall = { viewModel.endCall() }
                        )
                    }
                }
            }
        }

        // Dialogs
        if (showSettingsDialog) {
            DialerSettingsDialog(
                currentThemeId = colorThemeSetting,
                onSelectTheme = { viewModel.setColorTheme(it) },
                darkModeSetting = darkModeSetting,
                onSetDarkMode = { viewModel.setDarkMode(it) },
                selectedSimSlot = selectedSimSlot,
                onSelectSimSlot = { viewModel.selectSimSlot(it) },
                sim1Label = sim1Label,
                sim2Label = sim2Label,
                onSaveSimLabels = { s1, s2 -> viewModel.setSimLabels(s1, s2) },
                allSipAccounts = allSipAccounts,
                onSelectSipAccount = { viewModel.setActiveSipAccount(it) },
                dtmfEnabled = dtmfEnabled,
                onToggleDtmf = { viewModel.setDtmfToneEnabled(it) },
                vibrateEnabled = vibrateEnabled,
                onToggleVibrate = { viewModel.setVibrateFeedback(it) },
                autoRecordEnabled = autoRecordEnabled,
                onToggleAutoRecord = { viewModel.setAutoRecord(it) },
                stealthRecordEnabled = stealthModeEnabled,
                onToggleStealthRecord = { viewModel.setStealthRecord(it) },
                showRecordingsTab = showRecordingsTab,
                onToggleShowRecordingsTab = { viewModel.setShowRecordingsTab(it) },
                onOpenRecordings = {
                    showSettingsDialog = false
                    if (!showRecordingsTab) {
                        viewModel.setShowRecordingsTab(true)
                    }
                    coroutineScope.launch {
                        val idx = visibleTabs.indexOf(NavTab.RECORDINGS).takeIf { it >= 0 } ?: (visibleTabs.size - 1)
                        pagerState.animateScrollToPage(idx)
                    }
                },
                blockedNumbers = blockedNumbers,
                onBlockNumber = { num, name, isSpam -> viewModel.blockNumber(num, name, isSpam) },
                onUnblockNumber = { viewModel.unblockNumber(it) },
                onOpenSipSettings = {
                    showSettingsDialog = false
                    showSipConfigDialog = true
                },
                onSyncContacts = { viewModel.syncContacts() },
                isSyncingContacts = isSyncingContacts,
                onDismiss = { showSettingsDialog = false }
            )
        }

        if (showSipConfigDialog) {
            SipConfigDialog(
                initialAccount = sipAccount,
                allAccounts = allSipAccounts,
                onSelectAccount = { viewModel.setActiveSipAccount(it) },
                onDeleteAccount = { viewModel.deleteSipAccount(it) },
                onDismiss = { showSipConfigDialog = false },
                onSave = { account ->
                    viewModel.saveSipAccount(account)
                }
            )
        }

        if (editingSpeedDialSlot != null) {
            SpeedDialEditDialog(
                initialSlot = editingSpeedDialSlot ?: 1,
                existingContact = editingSpeedDialContact,
                availableContacts = contacts,
                onDismiss = {
                    editingSpeedDialSlot = null
                    editingSpeedDialContact = null
                },
                onSave = { contact ->
                    viewModel.saveSpeedDial(contact)
                },
                onDelete = { slot ->
                    viewModel.removeSpeedDial(slot)
                }
            )
        }
    }
}
