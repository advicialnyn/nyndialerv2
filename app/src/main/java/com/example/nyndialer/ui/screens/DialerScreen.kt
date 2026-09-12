package com.example.nyndialer.ui.screens

import android.view.HapticFeedbackConstants
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nyndialer.data.model.ServiceType
import com.example.nyndialer.data.model.SipAccount
import com.example.nyndialer.domain.T9MatchResult
import com.example.ui.theme.DialEmerald
import com.example.ui.theme.SipCyan

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DialerScreen(
    dialedNumber: String,
    currentService: ServiceType,
    sipAccount: SipAccount?,
    allSipAccounts: List<SipAccount> = emptyList(),
    selectedSimSlot: Int = 1,
    sim1Label: String = "SIM 1",
    sim2Label: String = "SIM 2",
    isSipOnline: Boolean,
    t9Matches: List<T9MatchResult>,
    onDigitClick: (Char) -> Unit,
    onDigitLongClick: (Char) -> Unit,
    onBackspaceClick: () -> Unit,
    onBackspaceLongClick: () -> Unit,
    onServiceToggle: () -> Unit,
    onSelectSimSlot: (Int) -> Unit = {},
    onSelectSipAccount: (Long) -> Unit = {},
    onCallClick: () -> Unit,
    onMatchClick: (String, String) -> Unit,
    onOpenSipSettings: () -> Unit
) {
    val view = LocalView.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 20.dp, vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Top Bar: Service Mode Chip (Cellular / SIP VoIP) + SIP Status Indicator + Dual SIM Bar
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 2.dp, bottom = 4.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Service Toggle Pill
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (currentService == ServiceType.SIP_VOIP) SipCyan.copy(alpha = 0.5f)
                        else DialEmerald.copy(alpha = 0.5f)
                    ),
                    modifier = Modifier.testTag("service_toggle_pill")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        FilterChip(
                            selected = currentService == ServiceType.CELLULAR,
                            onClick = {
                                view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                                if (currentService != ServiceType.CELLULAR) onServiceToggle()
                            },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Phone,
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp)
                                )
                            },
                            label = { Text("Cellular", fontSize = 12.sp, fontWeight = FontWeight.SemiBold) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = DialEmerald,
                                selectedLabelColor = Color.Black
                            ),
                            border = null
                        )

                        Spacer(modifier = Modifier.width(4.dp))

                        FilterChip(
                            selected = currentService == ServiceType.SIP_VOIP,
                            onClick = {
                                view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                                if (currentService != ServiceType.SIP_VOIP) onServiceToggle()
                            },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Public,
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp)
                                )
                            },
                            label = { Text("SIP VoIP", fontSize = 12.sp, fontWeight = FontWeight.SemiBold) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = SipCyan,
                                selectedLabelColor = Color.Black
                            ),
                            border = null
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Settings icon for SIP config
                IconButton(
                    onClick = {
                        view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                        onOpenSipSettings()
                    },
                    modifier = Modifier
                        .size(36.dp)
                        .testTag("sip_settings_icon_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "SIP Settings",
                        tint = if (isSipOnline) SipCyan else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // Dual SIM Switcher Bar when in Cellular mode
            if (currentService == ServiceType.CELLULAR) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 2.dp, bottom = 4.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                    ) {
                        Row(
                            modifier = Modifier.padding(2.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (selectedSimSlot == 1) DialEmerald else Color.Transparent,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .combinedClickable(
                                        onClick = {
                                            view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                                            onSelectSimSlot(1)
                                        }
                                    )
                                    .testTag("sim_slot_1_pill")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Phone,
                                        contentDescription = null,
                                        tint = if (selectedSimSlot == 1) Color.Black else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Text(
                                        text = sim1Label,
                                        fontSize = 11.sp,
                                        fontWeight = if (selectedSimSlot == 1) FontWeight.Bold else FontWeight.Medium,
                                        color = if (selectedSimSlot == 1) Color.Black else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (selectedSimSlot == 2) DialEmerald else Color.Transparent,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .combinedClickable(
                                        onClick = {
                                            view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                                            onSelectSimSlot(2)
                                        }
                                    )
                                    .testTag("sim_slot_2_pill")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Phone,
                                        contentDescription = null,
                                        tint = if (selectedSimSlot == 2) Color.Black else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Text(
                                        text = sim2Label,
                                        fontSize = 11.sp,
                                        fontWeight = if (selectedSimSlot == 2) FontWeight.Bold else FontWeight.Medium,
                                        color = if (selectedSimSlot == 2) Color.Black else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // SIP Status line & Multi-Account Switcher
            if (currentService == ServiceType.SIP_VOIP) {
                val accountDomain = sipAccount?.domain ?: "sip.antisip.com"
                Row(
                    modifier = Modifier.padding(bottom = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = if (isSipOnline) "● Online: ${sipAccount?.username ?: "nayan24"}@$accountDomain" else "○ SIP Connecting...",
                        fontSize = 11.sp,
                        color = if (isSipOnline) SipCyan else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (allSipAccounts.size > 1) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = SipCyan.copy(alpha = 0.2f),
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .combinedClickable(
                                    onClick = {
                                        view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                                        val currIdx = allSipAccounts.indexOfFirst { it.id == sipAccount?.id }
                                        val nextIdx = if (currIdx >= 0 && currIdx + 1 < allSipAccounts.size) currIdx + 1 else 0
                                        onSelectSipAccount(allSipAccounts[nextIdx].id)
                                    }
                                )
                                .testTag("dialer_switch_sip_acc_btn")
                        ) {
                            Text(
                                text = "Switch Account ▾",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = SipCyan,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }
        }

        // Smart Contact Search Results Preview (if typing digits)
        AnimatedVisibility(
            visible = dialedNumber.isNotEmpty() && t9Matches.isNotEmpty(),
            enter = slideInVertically() + fadeIn(),
            exit = slideOutVertically() + fadeOut()
        ) {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 140.dp)
                    .padding(vertical = 4.dp)
            ) {
                LazyColumn(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    items(t9Matches.take(4)) { match ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .combinedClickable(
                                    onClick = {
                                        onMatchClick(match.contact.phoneNumber, match.contact.name)
                                    }
                                )
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                val avatarColor = try {
                                    Color(android.graphics.Color.parseColor(match.contact.avatarColorHex))
                                } catch (_: Exception) {
                                    DialEmerald
                                }
                                Box(
                                    modifier = Modifier
                                        .size(34.dp)
                                        .background(avatarColor, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = match.contact.name.take(1).uppercase(),
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }

                                Column {
                                    // Highlight matched substring in name
                                    val annotatedName = buildAnnotatedString {
                                        val name = match.contact.name
                                        val range = match.matchedNameRange
                                        if (range != null && range.first in name.indices && range.last < name.length) {
                                            append(name.substring(0, range.first))
                                            withStyle(
                                                SpanStyle(
                                                    color = DialEmerald,
                                                    fontWeight = FontWeight.ExtraBold
                                                )
                                            ) {
                                                append(name.substring(range.first, range.last + 1))
                                            }
                                            append(name.substring(range.last + 1))
                                        } else {
                                            append(name)
                                        }
                                    }
                                    Text(
                                        text = annotatedName,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = match.contact.phoneNumber,
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Icon(
                                imageVector = Icons.Default.Call,
                                contentDescription = "Call",
                                tint = DialEmerald,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }

        // Display Dialed Number
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                contentAlignment = Alignment.Center
            ) {
                if (dialedNumber.isEmpty()) {
                    Text(
                        text = "Enter number or search T9",
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                } else {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = dialedNumber,
                            fontSize = if (dialedNumber.length > 13) 24.sp else 32.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.testTag("dialed_number_display")
                        )
                    }
                }
            }
        }

        // Minimal Tactile Dialpad Grid (3x4)
        val keypadRows = listOf(
            listOf(KeypadButtonData('1', "∞"), KeypadButtonData('2', "ABC"), KeypadButtonData('3', "DEF")),
            listOf(KeypadButtonData('4', "GHI"), KeypadButtonData('5', "JKL"), KeypadButtonData('6', "MNO")),
            listOf(KeypadButtonData('7', "PQRS"), KeypadButtonData('8', "TUV"), KeypadButtonData('9', "WXYZ")),
            listOf(KeypadButtonData('*', ""), KeypadButtonData('0', "+"), KeypadButtonData('#', ""))
        )

        Column(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(horizontal = 8.dp)
        ) {
            keypadRows.forEach { row ->
                Row(
                    horizontalArrangement = Arrangement.spacedBy(22.dp),
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    row.forEach { button ->
                        KeypadButton(
                            data = button,
                            modifier = Modifier.weight(1f),
                            onClick = {
                                view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                                onDigitClick(button.digit)
                            },
                            onLongClick = {
                                view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
                                onDigitLongClick(button.digit)
                            }
                        )
                    }
                }
            }
        }

        // Bottom Call Action Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp, top = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Speed Dial shortcut / info
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.size(52.dp)
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Icon(
                        imageVector = Icons.Default.Speed,
                        contentDescription = "Hold 1-9 for Speed Dial",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            // Primary Call Button
            val callBtnColor = if (currentService == ServiceType.SIP_VOIP) SipCyan else DialEmerald
            Box(
                modifier = Modifier
                    .size(68.dp)
                    .clip(CircleShape)
                    .background(callBtnColor)
                    .combinedClickable(
                        onClick = {
                            view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                            onCallClick()
                        }
                    )
                    .testTag("dialer_call_button"),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Call,
                        contentDescription = "Call via ${currentService.name}",
                        tint = Color.Black,
                        modifier = Modifier.size(if (currentService == ServiceType.CELLULAR) 26.dp else 30.dp)
                    )
                    if (currentService == ServiceType.CELLULAR) {
                        Text(
                            text = if (selectedSimSlot == 1) "SIM 1" else "SIM 2",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.Black.copy(alpha = 0.85f),
                            lineHeight = 10.sp
                        )
                    }
                }
            }

            // Backspace button with long-press clear
            Surface(
                shape = CircleShape,
                color = if (dialedNumber.isNotEmpty()) MaterialTheme.colorScheme.surfaceVariant else Color.Transparent,
                modifier = Modifier.size(52.dp)
            ) {
                if (dialedNumber.isNotEmpty()) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .fillMaxSize()
                            .combinedClickable(
                                onClick = {
                                    view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                                    onBackspaceClick()
                                },
                                onLongClick = {
                                    view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
                                    onBackspaceLongClick()
                                }
                            )
                            .testTag("dialer_backspace_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Backspace,
                            contentDescription = "Backspace",
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }
        }
    }
}

data class KeypadButtonData(
    val digit: Char,
    val subText: String
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun KeypadButton(
    data: KeypadButtonData,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    val view = LocalView.current
    Surface(
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
        modifier = modifier
            .height(64.dp)
            .clip(CircleShape)
            .combinedClickable(
                onClick = {
                    view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                    onClick()
                },
                onLongClick = {
                    view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
                    onLongClick()
                }
            )
            .testTag("keypad_button_${data.digit}")
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = data.digit.toString(),
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Box(
                modifier = Modifier.height(13.dp),
                contentAlignment = Alignment.Center
            ) {
                if (data.digit == '1') {
                    Text(
                        text = "∞",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                        lineHeight = 12.sp
                    )
                } else if (data.subText.isNotEmpty()) {
                    Text(
                        text = data.subText,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        letterSpacing = 1.sp,
                        lineHeight = 11.sp
                    )
                } else {
                    // Reserve identical vertical space for * and # so all keypad digits stay aligned
                    Spacer(modifier = Modifier.height(11.dp))
                }
            }
        }
    }
}
