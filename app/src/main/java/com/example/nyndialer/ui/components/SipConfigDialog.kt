package com.example.nyndialer.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nyndialer.data.model.SipAccount
import com.example.ui.theme.DialEmerald
import com.example.ui.theme.SipCyan

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.RadioButtonChecked
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults

@Composable
fun SipConfigDialog(
    initialAccount: SipAccount?,
    allAccounts: List<SipAccount> = emptyList(),
    onSelectAccount: (Long) -> Unit = {},
    onDeleteAccount: (Long) -> Unit = {},
    onDismiss: () -> Unit,
    onSave: (SipAccount) -> Unit
) {
    var selectedEditAccountId by remember { mutableStateOf<Long?>(initialAccount?.id) }
    var isAddingNew by remember { mutableStateOf(false) }

    var username by remember(selectedEditAccountId, isAddingNew) {
        val target = if (isAddingNew) null else allAccounts.find { it.id == selectedEditAccountId } ?: initialAccount
        mutableStateOf(target?.username ?: if (isAddingNew) "" else "nayan24")
    }
    var displayName by remember(selectedEditAccountId, isAddingNew) {
        val target = if (isAddingNew) null else allAccounts.find { it.id == selectedEditAccountId } ?: initialAccount
        mutableStateOf(target?.displayName ?: if (isAddingNew) "" else "Nayan VoIP")
    }
    var domain by remember(selectedEditAccountId, isAddingNew) {
        val target = if (isAddingNew) null else allAccounts.find { it.id == selectedEditAccountId } ?: initialAccount
        mutableStateOf(target?.domain ?: if (isAddingNew) "sip.antisip.com" else "sip.antisip.com")
    }
    var portText by remember(selectedEditAccountId, isAddingNew) {
        val target = if (isAddingNew) null else allAccounts.find { it.id == selectedEditAccountId } ?: initialAccount
        mutableStateOf((target?.port ?: 5060).toString())
    }
    var password by remember(selectedEditAccountId, isAddingNew) {
        val target = if (isAddingNew) null else allAccounts.find { it.id == selectedEditAccountId } ?: initialAccount
        mutableStateOf(target?.password ?: "")
    }
    var transport by remember(selectedEditAccountId, isAddingNew) {
        val target = if (isAddingNew) null else allAccounts.find { it.id == selectedEditAccountId } ?: initialAccount
        mutableStateOf(target?.transport ?: "UDP")
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.testTag("sip_config_dialog"),
        shape = RoundedCornerShape(24.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(
                    color = SipCyan.copy(alpha = 0.15f),
                    shape = CircleShape,
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Public,
                        contentDescription = null,
                        tint = SipCyan,
                        modifier = Modifier
                            .padding(8.dp)
                            .size(24.dp)
                    )
                }
                Column {
                    Text(
                        text = "SIP Accounts & VoIP",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Manage multiple SIP accounts & active line",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        text = {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(340.dp)
                    .padding(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Section 1: All SIP Accounts & Switcher
                if (allAccounts.isNotEmpty()) {
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "SELECT ACTIVE ACCOUNT (${allAccounts.size})",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = SipCyan,
                                letterSpacing = 1.sp
                            )
                            if (!isAddingNew) {
                                TextButton(
                                    onClick = {
                                        isAddingNew = true
                                        selectedEditAccountId = null
                                    },
                                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 4.dp, vertical = 0.dp)
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp), tint = SipCyan)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("+ Add Account", fontSize = 11.sp, color = SipCyan)
                                }
                            }
                        }
                    }

                    items(allAccounts, key = { it.id }) { acc ->
                        val isCurrentActive = acc.isActive || (initialAccount?.id == acc.id)
                        val isEditingThis = selectedEditAccountId == acc.id && !isAddingNew

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isCurrentActive) SipCyan.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                            border = androidx.compose.foundation.BorderStroke(
                                width = if (isCurrentActive) 1.5.dp else 1.dp,
                                color = if (isCurrentActive) SipCyan else MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onSelectAccount(acc.id)
                                    selectedEditAccountId = acc.id
                                    isAddingNew = false
                                }
                                .testTag("sip_account_item_${acc.id}")
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(
                                        imageVector = if (isCurrentActive) Icons.Default.RadioButtonChecked else Icons.Default.RadioButtonUnchecked,
                                        contentDescription = if (isCurrentActive) "Active SIP Account" else "Switch to this Account",
                                        tint = if (isCurrentActive) SipCyan else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Column {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Text(
                                                text = acc.displayName,
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            if (isCurrentActive) {
                                                Surface(
                                                    shape = RoundedCornerShape(4.dp),
                                                    color = SipCyan.copy(alpha = 0.2f)
                                                ) {
                                                    Text(
                                                        text = "ACTIVE",
                                                        fontSize = 9.sp,
                                                        fontWeight = FontWeight.Black,
                                                        color = SipCyan,
                                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                                    )
                                                }
                                            }
                                        }
                                        Text(
                                            text = "${acc.username}@${acc.domain}:${acc.port}",
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    IconButton(
                                        onClick = {
                                            selectedEditAccountId = acc.id
                                            isAddingNew = false
                                        },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.Edit,
                                            contentDescription = "Edit Credentials",
                                            tint = if (isEditingThis) SipCyan else MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }

                                    if (allAccounts.size > 1) {
                                        IconButton(
                                            onClick = { onDeleteAccount(acc.id) },
                                            modifier = Modifier.size(28.dp)
                                        ) {
                                            Icon(
                                                Icons.Default.Delete,
                                                contentDescription = "Delete Account",
                                                tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    item {
                        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                    }
                }

                // Section 2: Edit or Add Account Form
                item {
                    Text(
                        text = if (isAddingNew) "ADD NEW SIP PROFILE" else "EDIT ACCOUNT CREDENTIALS",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = 1.sp
                    )
                }

                item {
                    OutlinedTextField(
                        value = displayName,
                        onValueChange = { displayName = it },
                        label = { Text("Account Name / Label") },
                        placeholder = { Text("e.g. Work VoIP, Home SIP") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("sip_display_name_input")
                    )
                }

                item {
                    OutlinedTextField(
                        value = domain,
                        onValueChange = { domain = it },
                        label = { Text("SIP Domain / Server") },
                        leadingIcon = { Icon(Icons.Default.Dns, contentDescription = null, tint = SipCyan) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = SipCyan,
                            focusedLabelColor = SipCyan
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("sip_domain_input")
                    )
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = username,
                            onValueChange = { username = it },
                            label = { Text("Username") },
                            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                            singleLine = true,
                            modifier = Modifier
                                .weight(1.5f)
                                .testTag("sip_user_input")
                        )

                        OutlinedTextField(
                            value = portText,
                            onValueChange = { portText = it },
                            label = { Text("Port") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("sip_port_input")
                        )
                    }
                }

                item {
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("SIP Password") },
                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                        visualTransformation = PasswordVisualTransformation(),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("sip_password_input")
                    )
                }

                item {
                    Text(
                        text = "Transport Protocol",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth().padding(top = 4.dp)
                    ) {
                        listOf("UDP", "TCP", "TLS").forEach { proto ->
                            FilterChip(
                                selected = transport == proto,
                                onClick = { transport = proto },
                                label = { Text(proto, fontSize = 12.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = SipCyan.copy(alpha = 0.2f),
                                    selectedLabelColor = SipCyan
                                )
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val portNum = portText.toIntOrNull() ?: 5060
                    val targetId = if (isAddingNew) 0L else (selectedEditAccountId ?: 0L)
                    val account = SipAccount(
                        id = targetId,
                        username = username.ifBlank { "nayan24" },
                        displayName = displayName.ifBlank { "SIP VoIP" },
                        domain = domain.ifBlank { "sip.antisip.com" },
                        port = portNum,
                        password = password,
                        transport = transport,
                        isActive = true,
                        isRegistered = true,
                        registeredAt = System.currentTimeMillis()
                    )
                    onSave(account)
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = SipCyan,
                    contentColor = Color.Black
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.testTag("save_sip_button")
            ) {
                Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(if (isAddingNew) "Add & Activate" else "Save & Connect", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("cancel_sip_button")
            ) {
                Text("Close", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    )
}
