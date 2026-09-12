package com.example.nyndialer.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.CallMade
import androidx.compose.material.icons.automirrored.filled.CallMissed
import androidx.compose.material.icons.automirrored.filled.CallReceived
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.ClearAll
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nyndialer.data.model.CallRecord
import com.example.nyndialer.data.model.CallType
import com.example.nyndialer.data.model.ServiceType
import com.example.ui.theme.DialEmerald
import com.example.ui.theme.HangupRed
import com.example.ui.theme.SipCyan
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun CallLogScreen(
    callLogs: List<CallRecord>,
    onCallBack: (String, String?, ServiceType) -> Unit,
    onDeleteCall: (Long) -> Unit,
    onClearAll: () -> Unit,
    onOpenRecording: (String) -> Unit,
    onBlockNumber: (String, String?) -> Unit = { _, _ -> }
) {
    var selectedFilter by remember { mutableStateOf("ALL") }
    var searchQuery by remember { mutableStateOf("") }
    var showClearConfirm by remember { mutableStateOf(false) }
    var viewingCallDetails by remember { mutableStateOf<CallRecord?>(null) }

    val filteredCalls = remember(callLogs, selectedFilter, searchQuery) {
        val query = searchQuery.trim().lowercase()
        callLogs.filter { call ->
            val matchesFilter = when (selectedFilter) {
                "MISSED" -> call.callType == CallType.MISSED
                "SIP" -> call.serviceType == ServiceType.SIP_VOIP
                "RECORDED" -> call.effectiveRecordingPath != null
                else -> true
            }
            val matchesSearch = if (query.isEmpty()) {
                true
            } else {
                val cleanQuery = query.filter { it.isDigit() }
                val cleanNumber = call.phoneNumber.filter { it.isDigit() }
                (call.contactName?.lowercase()?.contains(query) == true) ||
                    call.phoneNumber.contains(query, ignoreCase = true) ||
                    (cleanQuery.isNotEmpty() && cleanNumber.contains(cleanQuery)) ||
                    call.callType.name.lowercase().contains(query) ||
                    (query == "sip" && call.serviceType == ServiceType.SIP_VOIP)
            }
            matchesFilter && matchesSearch
        }
    }

    // Date category grouping
    val groupedCalls = remember(filteredCalls) {
        val now = java.util.Calendar.getInstance()
        val todayStart = java.util.Calendar.getInstance().apply {
            set(java.util.Calendar.HOUR_OF_DAY, 0)
            set(java.util.Calendar.MINUTE, 0)
            set(java.util.Calendar.SECOND, 0)
            set(java.util.Calendar.MILLISECOND, 0)
        }.timeInMillis
        val yesterdayStart = todayStart - 86_400_000L

        val map = linkedMapOf<String, MutableList<CallRecord>>()
        for (call in filteredCalls) {
            val group = when {
                call.timestamp >= todayStart -> "Today"
                call.timestamp >= yesterdayStart -> "Yesterday"
                else -> "Older"
            }
            map.getOrPut(group) { mutableListOf() }.add(call)
        }
        map
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
            .testTag("call_log_screen")
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = "Recents & History",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "${filteredCalls.size} of ${callLogs.size} records",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (callLogs.isNotEmpty()) {
                IconButton(onClick = { showClearConfirm = true }) {
                    Icon(
                        imageVector = Icons.Default.ClearAll,
                        contentDescription = "Clear all calls",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Smart Search Engine Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Search name, number or type...", fontSize = 13.sp) },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Clear search",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 10.dp)
                .testTag("search_call_logs_input")
        )

        // Filter chips: All, Missed, SIP, Recorded
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("ALL" to "All", "MISSED" to "Missed", "SIP" to "SIP VoIP", "RECORDED" to "Recorded").forEach { (filterKey, label) ->
                val selected = selectedFilter == filterKey
                FilterChip(
                    selected = selected,
                    onClick = { selectedFilter = filterKey },
                    label = { Text(label, fontSize = 12.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = DialEmerald,
                        selectedLabelColor = Color.Black
                    ),
                    modifier = Modifier.testTag("filter_$filterKey")
                )
            }
        }

        if (filteredCalls.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Phone,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = if (searchQuery.isNotEmpty()) "No calls match \"$searchQuery\"" else "No calls in this category",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 14.sp
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                groupedCalls.forEach { (categoryHeader, callsInGroup) ->
                    item(key = "header_$categoryHeader") {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp, bottom = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = categoryHeader.uppercase(),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.primary,
                                letterSpacing = 1.2.sp
                            )
                            Text(
                                text = "${callsInGroup.size}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    items(callsInGroup, key = { it.id }) { call ->
                        CallLogItem(
                            call = call,
                            onClick = { viewingCallDetails = call },
                            onCallBack = {
                                onCallBack(call.phoneNumber, call.contactName, call.serviceType)
                            },
                            onDelete = { onDeleteCall(call.id) },
                            onOpenRecording = {
                                val rec = call.effectiveRecordingPath
                                if (rec != null) {
                                    onOpenRecording(rec)
                                }
                            }
                        )
                    }
                }
            }
        }
    }

    if (showClearConfirm) {
        AlertDialog(
            onDismissRequest = { showClearConfirm = false },
            title = { Text("Clear All Call History?") },
            text = { Text("This will remove all recent call logs from local database.") },
            confirmButton = {
                Button(
                    onClick = {
                        onClearAll()
                        showClearConfirm = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = HangupRed)
                ) {
                    Text("Clear All")
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearConfirm = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Google Dialer Style Call Details Dialog
    viewingCallDetails?.let { call ->
        val effectiveDur = call.effectiveDurationSec
        val durText = if (effectiveDur > 0) {
            val m = effectiveDur / 60
            val s = effectiveDur % 60
            "${m}m ${s}s"
        } else {
            if (call.callType == CallType.MISSED) "Missed call" else "0s"
        }
        val dateText = SimpleDateFormat("EEEE, MMM d, yyyy • hh:mm a", Locale.getDefault()).format(Date(call.timestamp))

        AlertDialog(
            onDismissRequest = { viewingCallDetails = null },
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = if (call.callType == CallType.MISSED) HangupRed.copy(alpha = 0.15f) else DialEmerald.copy(alpha = 0.15f),
                        modifier = Modifier.size(36.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = when (call.callType) {
                                    CallType.OUTGOING -> Icons.AutoMirrored.Filled.CallMade
                                    CallType.INCOMING -> Icons.AutoMirrored.Filled.CallReceived
                                    CallType.MISSED -> Icons.AutoMirrored.Filled.CallMissed
                                    CallType.REJECTED -> Icons.AutoMirrored.Filled.CallMissed
                                },
                                contentDescription = null,
                                tint = if (call.callType == CallType.MISSED) HangupRed else DialEmerald,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    Column {
                        Text(
                            text = call.contactName ?: call.phoneNumber,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        if (call.contactName != null) {
                            Text(
                                text = call.phoneNumber,
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    HorizontalDivider()

                    // Call Details info
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Type & Service:", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(
                            text = "${call.callType.name} • ${if (call.serviceType == ServiceType.SIP_VOIP) "SIP VoIP" else "Cellular"}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Duration:", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(text = durText, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Time:", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(text = dateText, fontSize = 11.sp, fontWeight = FontWeight.Normal)
                    }

                    val recPath = call.effectiveRecordingPath
                    if (recPath != null) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Call Recording:", fontSize = 12.sp, color = SipCyan)
                            TextButton(
                                onClick = {
                                    viewingCallDetails = null
                                    onOpenRecording(recPath)
                                }
                            ) {
                                Text("Play Recording", fontSize = 12.sp, color = SipCyan)
                            }
                        }
                    }

                    HorizontalDivider()

                    // Call actions
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                viewingCallDetails = null
                                onCallBack(call.phoneNumber, call.contactName, ServiceType.CELLULAR)
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Cellular", fontSize = 12.sp)
                        }

                        Button(
                            onClick = {
                                viewingCallDetails = null
                                onCallBack(call.phoneNumber, call.contactName, ServiceType.SIP_VOIP)
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = SipCyan, contentColor = Color.Black)
                        ) {
                            Text("SIP Call", fontSize = 12.sp)
                        }
                    }

                    // Block / Report Spam Button (Google Dialer feature)
                    OutlinedButton(
                        onClick = {
                            onBlockNumber(call.phoneNumber, call.contactName)
                            viewingCallDetails = null
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = HangupRed)
                    ) {
                        Icon(imageVector = Icons.Default.Block, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Block / Report Spam", fontSize = 12.sp)
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { viewingCallDetails = null }) {
                    Text("Close")
                }
            }
        )
    }
}

@Composable
fun CallLogItem(
    call: CallRecord,
    onClick: () -> Unit,
    onCallBack: () -> Unit,
    onDelete: () -> Unit,
    onOpenRecording: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("call_log_item_${call.id}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                // Direction icon
                val (dirIcon, dirColor) = when (call.callType) {
                    CallType.OUTGOING -> Icons.AutoMirrored.Filled.CallMade to DialEmerald
                    CallType.INCOMING -> Icons.AutoMirrored.Filled.CallReceived to Color(0xFF3B82F6)
                    CallType.MISSED -> Icons.AutoMirrored.Filled.CallMissed to HangupRed
                    CallType.REJECTED -> Icons.AutoMirrored.Filled.CallMissed to HangupRed
                }

                Surface(
                    shape = CircleShape,
                    color = dirColor.copy(alpha = 0.15f),
                    modifier = Modifier.size(40.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = dirIcon,
                            contentDescription = null,
                            tint = dirColor,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = call.contactName ?: call.phoneNumber,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (call.callType == CallType.MISSED) HangupRed else MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        // Service tag
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = if (call.serviceType == ServiceType.SIP_VOIP) SipCyan.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Text(
                                text = if (call.serviceType == ServiceType.SIP_VOIP) "SIP" else "Cell",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (call.serviceType == ServiceType.SIP_VOIP) SipCyan else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                            )
                        }
                    }

                    if (call.contactName != null) {
                        Text(
                            text = call.phoneNumber,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    val dateStr = SimpleDateFormat("MMM d, hh:mm a", Locale.getDefault()).format(Date(call.timestamp))
                    val effectiveDur = call.effectiveDurationSec
                    val durStr = if (effectiveDur > 0) {
                        val m = effectiveDur / 60
                        val s = effectiveDur % 60
                        " • ${m}m ${s}s"
                    } else ""
                    Text(
                        text = "$dateStr$durStr",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
            }

            // Action buttons
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // If recording exists
                if (call.effectiveRecordingPath != null) {
                    IconButton(
                        onClick = onOpenRecording,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.GraphicEq,
                            contentDescription = "Recorded Audio",
                            tint = SipCyan,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                // Call Back button
                IconButton(
                    onClick = onCallBack,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Call,
                        contentDescription = "Call Back",
                        tint = DialEmerald,
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Delete log item
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete entry",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}
