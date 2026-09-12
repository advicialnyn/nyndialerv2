package com.example.nyndialer.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material.icons.filled.Dialpad
import androidx.compose.material.icons.filled.FiberManualRecord
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nyndialer.data.model.ServiceType
import com.example.nyndialer.service.ActiveCallInfo
import com.example.nyndialer.service.CallState
import com.example.ui.theme.DialEmerald
import com.example.ui.theme.HangupRed
import com.example.ui.theme.SipCyan
import com.example.ui.theme.WarningAmber

@Composable
fun ActiveCallScreen(
    callInfo: ActiveCallInfo,
    onSwitchService: () -> Unit,
    onToggleMute: () -> Unit,
    onToggleSpeaker: () -> Unit,
    onToggleHold: () -> Unit,
    onToggleRecord: () -> Unit,
    onEndCall: () -> Unit
) {
    var showInCallDialpad by remember { mutableStateOf(false) }

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    val serviceColor by animateColorAsState(
        targetValue = if (callInfo.serviceType == ServiceType.SIP_VOIP) SipCyan else DialEmerald,
        label = "serviceColor"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF0F1524),
                        Color(0xFF070A10)
                    )
                )
            )
            .padding(24.dp)
            .testTag("active_call_screen")
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Status Bar: Service Chip & Seamless Switch button
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(top = 16.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = serviceColor.copy(alpha = 0.15f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, serviceColor.copy(alpha = 0.4f)),
                    modifier = Modifier.padding(bottom = 8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = if (callInfo.serviceType == ServiceType.SIP_VOIP) Icons.Default.Public else Icons.Default.Phone,
                            contentDescription = null,
                            tint = serviceColor,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = if (callInfo.serviceType == ServiceType.SIP_VOIP) "SIP VoIP Session" else "Cellular Network",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = serviceColor
                        )
                    }
                }

                // Seamless Service Switcher Button
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFF1E2638),
                    modifier = Modifier
                        .clickable(enabled = callInfo.callState == CallState.ACTIVE) {
                            onSwitchService()
                        }
                        .testTag("switch_service_button")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.SwapHoriz,
                            contentDescription = "Switch service",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = if (callInfo.serviceType == ServiceType.CELLULAR) "Seamlessly Switch to SIP VoIP" else "Seamlessly Switch to Cellular",
                            fontSize = 12.sp,
                            color = Color(0xFFCBD5E1)
                        )
                    }
                }

                // Call State / Duration
                Spacer(modifier = Modifier.height(16.dp))
                val stateText = when (callInfo.callState) {
                    CallState.OUTGOING_DIALING -> "Dialing..."
                    CallState.RINGING -> "Ringing..."
                    CallState.HANDING_OFF -> "Seamlessly handing off service..."
                    CallState.ACTIVE -> {
                        val minutes = callInfo.callDurationSec / 60
                        val seconds = callInfo.callDurationSec % 60
                        String.format("%02d:%02d", minutes, seconds)
                    }
                    CallState.ENDED -> "Call Ended"
                    else -> ""
                }

                Text(
                    text = stateText,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (callInfo.callState == CallState.HANDING_OFF) WarningAmber else Color(0xFF94A3B8)
                )

                // Discreet Recording Active Badge & Indicator
                AnimatedVisibility(visible = callInfo.isRecording) {
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = Color(0xFF221114),
                        border = androidx.compose.foundation.BorderStroke(1.dp, HangupRed.copy(alpha = 0.6f)),
                        modifier = Modifier.padding(top = 10.dp)
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                // Pulsing red recording beacon
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .scale(pulseScale)
                                        .background(HangupRed, CircleShape)
                                )

                                Text(
                                    text = "REC ● IN PROGRESS",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = HangupRed,
                                    letterSpacing = 1.sp
                                )

                                // Audio activity visualizer bars
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(3.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    val bar1Height = (8 + (pulseScale * 8)).dp
                                    val bar2Height = (14 - (pulseScale * 4)).dp
                                    val bar3Height = (10 + (pulseScale * 6)).dp
                                    Box(modifier = Modifier.width(3.dp).height(bar1Height).background(HangupRed, RoundedCornerShape(2.dp)))
                                    Box(modifier = Modifier.width(3.dp).height(bar2Height).background(HangupRed, RoundedCornerShape(2.dp)))
                                    Box(modifier = Modifier.width(3.dp).height(bar3Height).background(HangupRed, RoundedCornerShape(2.dp)))
                                }
                            }

                            Text(
                                text = "Secure Internal Storage (/nyndialer_recordings/)",
                                fontSize = 10.sp,
                                color = Color(0xFFE2E8F0).copy(alpha = 0.8f),
                                modifier = Modifier.padding(top = 2.dp)
                            )
                        }
                    }
                }
            }

            // Center: Avatar & Caller Info
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .scale(if (callInfo.callState == CallState.RINGING) pulseScale else 1f)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(serviceColor.copy(alpha = 0.35f), Color(0xFF1E2638))
                            ),
                            shape = CircleShape
                        )
                        .border(2.dp, serviceColor.copy(alpha = 0.6f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    val initial = (callInfo.contactName?.take(1)
                        ?: callInfo.phoneNumber.filter { it.isLetter() }.take(1).ifEmpty { "#" }).uppercase()
                    Text(
                        text = initial,
                        fontSize = 44.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = callInfo.contactName ?: callInfo.phoneNumber,
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                if (callInfo.contactName != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = callInfo.phoneNumber,
                        fontSize = 15.sp,
                        color = Color(0xFF94A3B8)
                    )
                }

                // SIP VoIP Session Telemetry
                if (callInfo.serviceType == ServiceType.SIP_VOIP) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF131A29)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.padding(horizontal = 24.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Codec", fontSize = 10.sp, color = Color(0xFF64748B))
                                Text("Opus Wideband", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = SipCyan)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Latency", fontSize = 10.sp, color = Color(0xFF64748B))
                                Text("${callInfo.latencyMs} ms", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = DialEmerald)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Jitter", fontSize = 10.sp, color = Color(0xFF64748B))
                                Text("${callInfo.jitterMs} ms", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
                            }
                        }
                    }
                }
            }

            // Bottom In-Call Controls
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 20.dp)
            ) {
                // 2 Rows of call buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    CallControlButton(
                        icon = if (callInfo.isMuted) Icons.Default.MicOff else Icons.Default.Mic,
                        label = if (callInfo.isMuted) "Unmute" else "Mute",
                        isActive = callInfo.isMuted,
                        onClick = onToggleMute,
                        testTag = "call_mute_button"
                    )

                    CallControlButton(
                        icon = Icons.Default.VolumeUp,
                        label = "Speaker",
                        isActive = callInfo.isSpeakerOn,
                        onClick = onToggleSpeaker,
                        testTag = "call_speaker_button"
                    )

                    CallControlButton(
                        icon = Icons.Default.Pause,
                        label = if (callInfo.isHold) "Resume" else "Hold",
                        isActive = callInfo.isHold,
                        onClick = onToggleHold,
                        testTag = "call_hold_button"
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    CallControlButton(
                        icon = Icons.Default.Dialpad,
                        label = "Keypad",
                        isActive = showInCallDialpad,
                        onClick = { showInCallDialpad = !showInCallDialpad },
                        testTag = "call_keypad_button"
                    )

                    // Discreet Call Recording button
                    CallControlButton(
                        icon = Icons.Default.FiberManualRecord,
                        label = if (callInfo.isRecording) "Recording..." else "Record Call",
                        isActive = callInfo.isRecording,
                        activeColor = HangupRed,
                        onClick = onToggleRecord,
                        testTag = "call_record_toggle_button"
                    )
                }

                Spacer(modifier = Modifier.height(28.dp))

                // End Call Button
                Box(
                    modifier = Modifier
                        .size(68.dp)
                        .clip(CircleShape)
                        .background(HangupRed)
                        .clickable { onEndCall() }
                        .testTag("end_call_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CallEnd,
                        contentDescription = "End Call",
                        tint = Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun CallControlButton(
    icon: ImageVector,
    label: String,
    isActive: Boolean,
    activeColor: Color = DialEmerald,
    onClick: () -> Unit,
    testTag: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable { onClick() }
            .testTag(testTag)
    ) {
        Surface(
            shape = CircleShape,
            color = if (isActive) activeColor.copy(alpha = 0.25f) else Color(0xFF1E2638),
            border = androidx.compose.foundation.BorderStroke(
                1.dp,
                if (isActive) activeColor else Color(0xFF2C374E)
            ),
            modifier = Modifier.size(56.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = if (isActive) activeColor else Color(0xFFE2E8F0),
                    modifier = Modifier.size(24.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = label,
            fontSize = 12.sp,
            color = if (isActive) activeColor else Color(0xFF94A3B8)
        )
    }
}
