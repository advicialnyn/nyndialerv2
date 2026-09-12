package com.example.nyndialer.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nyndialer.data.model.ServiceType
import com.example.nyndialer.data.model.SpeedDialContact
import com.example.nyndialer.domain.Contact
import com.example.ui.theme.DialEmerald
import com.example.ui.theme.HangupRed
import com.example.ui.theme.SipCyan

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SpeedDialEditDialog(
    initialSlot: Int,
    existingContact: SpeedDialContact?,
    availableContacts: List<Contact>,
    onDismiss: () -> Unit,
    onSave: (SpeedDialContact) -> Unit,
    onDelete: (Int) -> Unit
) {
    var selectedSlot by remember { mutableStateOf(initialSlot) }
    var name by remember { mutableStateOf(existingContact?.name ?: "") }
    var phoneNumber by remember { mutableStateOf(existingContact?.phoneNumber ?: "") }
    var serviceType by remember { mutableStateOf(existingContact?.serviceType ?: ServiceType.CELLULAR) }
    var selectedColor by remember { mutableStateOf(existingContact?.avatarColorHex ?: "#00E676") }

    val colorPalette = listOf(
        "#00E676", "#06B6D4", "#3B82F6", "#8B5CF6", "#EC4899", "#F59E0B", "#EF4444"
    )

    var expandedDropdown by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.testTag("speed_dial_edit_dialog"),
        shape = RoundedCornerShape(24.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(
                    color = DialEmerald.copy(alpha = 0.15f),
                    shape = CircleShape,
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Speed,
                        contentDescription = null,
                        tint = DialEmerald,
                        modifier = Modifier
                            .padding(8.dp)
                            .size(24.dp)
                    )
                }
                Column {
                    Text(
                        text = "Speed Dial #$selectedSlot",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Assign contact for instant 1-tap dial",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Slot selector 1-9
                Text(
                    text = "Keypad Key Slot (1–9)",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items((1..9).toList()) { slot ->
                        FilterChip(
                            selected = selectedSlot == slot,
                            onClick = { selectedSlot = slot },
                            label = { Text("$slot", fontWeight = FontWeight.Bold) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = DialEmerald,
                                selectedLabelColor = Color.Black
                            )
                        )
                    }
                }

                // Quick choose from Google Contacts dropdown
                ExposedDropdownMenuBox(
                    expanded = expandedDropdown,
                    onExpandedChange = { expandedDropdown = !expandedDropdown }
                ) {
                    OutlinedTextField(
                        value = if (name.isNotEmpty()) "$name ($phoneNumber)" else "Select from contacts...",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Pick from Contacts") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedDropdown) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = expandedDropdown,
                        onDismissRequest = { expandedDropdown = false }
                    ) {
                        availableContacts.take(12).forEach { contact ->
                            DropdownMenuItem(
                                text = {
                                    Column {
                                        Text(contact.name, fontWeight = FontWeight.SemiBold)
                                        Text(
                                            contact.phoneNumber,
                                            fontSize = 12.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                },
                                onClick = {
                                    name = contact.name
                                    phoneNumber = contact.phoneNumber
                                    selectedColor = contact.avatarColorHex
                                    serviceType = if (contact.isSipUri) ServiceType.SIP_VOIP else ServiceType.CELLULAR
                                    expandedDropdown = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Display Name") },
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = phoneNumber,
                    onValueChange = { phoneNumber = it },
                    label = { Text("Phone Number or SIP URI") },
                    leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // Service toggle (Cellular vs SIP)
                Text(
                    text = "Default Service for this slot",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = serviceType == ServiceType.CELLULAR,
                        onClick = { serviceType = ServiceType.CELLULAR },
                        leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null, modifier = Modifier.size(16.dp)) },
                        label = { Text("Cellular") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = DialEmerald.copy(alpha = 0.2f),
                            selectedLabelColor = DialEmerald
                        )
                    )
                    FilterChip(
                        selected = serviceType == ServiceType.SIP_VOIP,
                        onClick = { serviceType = ServiceType.SIP_VOIP },
                        leadingIcon = { Icon(Icons.Default.Public, contentDescription = null, modifier = Modifier.size(16.dp)) },
                        label = { Text("SIP VoIP") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = SipCyan.copy(alpha = 0.2f),
                            selectedLabelColor = SipCyan
                        )
                    )
                }

                // Avatar color badge picker
                Text(
                    text = "Avatar Color",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    colorPalette.forEach { hex ->
                        val color = try {
                            Color(android.graphics.Color.parseColor(hex))
                        } catch (_: Exception) {
                            DialEmerald
                        }
                        val isSelected = selectedColor.equals(hex, ignoreCase = true)
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .background(color, CircleShape)
                                .then(
                                    if (isSelected) Modifier.border(2.dp, Color.White, CircleShape)
                                    else Modifier
                                )
                                .clickable { selectedColor = hex }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank() && phoneNumber.isNotBlank()) {
                        val contact = SpeedDialContact(
                            slot = selectedSlot,
                            name = name.trim(),
                            phoneNumber = phoneNumber.trim(),
                            serviceType = serviceType,
                            avatarColorHex = selectedColor
                        )
                        onSave(contact)
                        onDismiss()
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = DialEmerald,
                    contentColor = Color.Black
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Save Slot", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (existingContact != null) {
                    OutlinedButton(
                        onClick = {
                            onDelete(selectedSlot)
                            onDismiss()
                        },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = HangupRed),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Clear")
                    }
                }
                TextButton(onClick = onDismiss) {
                    Text("Cancel")
                }
            }
        }
    )
}
