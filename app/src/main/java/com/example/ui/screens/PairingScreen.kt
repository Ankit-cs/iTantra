package com.example.ui.screens

import android.os.Build
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.CellTower
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LinkOff
import androidx.compose.material.icons.filled.Radar
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.outlined.PersonAdd
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ConnectionStatus
import com.example.model.PeerDevice
import com.example.model.TransportProtocol
import com.example.ui.theme.MinimalColorsInstance
import com.example.viewmodel.MissionControlViewModel
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * Minimal Mesh Link Screen with Radar visualization.
 */
@Composable
fun PairingScreen(
    viewModel: MissionControlViewModel,
    modifier: Modifier = Modifier
) {
    val colors = MinimalColorsInstance
    val activeProtocol by viewModel.activeProtocol.collectAsState()
    val connectionStatus by viewModel.connectionStatus.collectAsState()
    val discoveredPeers by viewModel.discoveredPeers.collectAsState()
    val connectedPeer by viewModel.connectedPeer.collectAsState()
    val connectedPeers by viewModel.connectedPeers.collectAsState()
    val telemetry by viewModel.telemetry.collectAsState()

    var manualIpInput by remember { mutableStateOf("") }
    var selectedPeer by remember { mutableStateOf<PeerDevice?>(null) }
    
    val isDiscovering = connectionStatus == ConnectionStatus.SEARCHING
    
    val infiniteTransition = rememberInfiniteTransition(label = "radar_sweep")
    val animatedAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(3000, easing = LinearEasing), RepeatMode.Restart),
        label = "sweep_angle"
    )
    val sweepAngle = if (isDiscovering) animatedAngle else 0f
    
    // Combine all peers to plot them on the radar
    val allPeers = (discoveredPeers + connectedPeers).distinctBy { it.id }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(colors.background)
            .padding(horizontal = 20.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // Section 1: Screen Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Mesh Radar",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Medium,
                        color = colors.textPrimary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Device ID: ${Build.MODEL}_${Build.ID.takeLast(6)}",
                        fontSize = 13.sp,
                        color = colors.textSecondary
                    )
                }
                
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(if (isDiscovering) colors.accentContainer else colors.surface)
                        .border(1.dp, if (isDiscovering) colors.accent else colors.outline, RoundedCornerShape(20.dp))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Filled.Radar,
                            contentDescription = null,
                            tint = if (isDiscovering) colors.accent else colors.textPrimary,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = if (isDiscovering) "SCANNING" else "STANDBY",
                            style = androidx.compose.material3.MaterialTheme.typography.labelSmall.copy(
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            color = if (isDiscovering) colors.accent else colors.textPrimary
                        )
                    }
                }
            }
        }

        // Section 2: Action Controls & Protocol
        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                // Protocol Selector
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(colors.surface)
                        .border(1.dp, colors.outline, RoundedCornerShape(12.dp))
                        .padding(4.dp)
                ) {
                    Row(modifier = Modifier.fillMaxWidth()) {
                        val protocols = listOf(
                            Triple(TransportProtocol.BLUETOOTH, "Bluetooth", Icons.Default.Bluetooth),
                            Triple(TransportProtocol.WIFI_DIRECT, "Wi-Fi Mesh", Icons.Default.Wifi),
                            Triple(TransportProtocol.BLE, "BLE Beacon", Icons.Default.CellTower)
                        )

                        protocols.forEach { (proto, label, icon) ->
                            val isSelected = activeProtocol == proto
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) colors.accentContainer else Color.Transparent)
                                    .clickable { viewModel.switchProtocol(proto) }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = icon,
                                        contentDescription = null,
                                        tint = if (isSelected) colors.accent else colors.textSecondary,
                                        modifier = Modifier.size(15.dp)
                                    )
                                    Text(
                                        text = label,
                                        fontSize = 13.sp,
                                        fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal,
                                        color = if (isSelected) colors.accent else colors.textSecondary
                                    )
                                }
                            }
                        }
                    }
                }

                // Search Peers Button
                Button(
                    onClick = {
                        if (isDiscovering) {
                            viewModel.transportLayer.stopDiscovery()
                        } else {
                            viewModel.scanForPeers(activeProtocol)
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isDiscovering) colors.accent else colors.textPrimary,
                        contentColor = colors.background
                    )
                ) {
                    Icon(
                        Icons.Filled.Radar,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = if (isDiscovering) "Stop Scan" else "Search Peers",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        // Section 3: RADAR CANVAS
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(
                    modifier = Modifier
                        .size(310.dp)
                        .clip(CircleShape)
                        .background(colors.surface)
                        .border(1.5.dp, colors.outline, CircleShape)
                        .pointerInput(allPeers) {
                            detectTapGestures { offset ->
                                val center = Offset(size.width / 2f, size.height / 2f)
                                val maxR = size.width / 2f * 0.85f

                                selectedPeer = allPeers.minByOrNull { peer ->
                                    val normDist = ((peer.signalStrengthDbm + 100).coerceIn(0, 70) / 70f).coerceIn(0.15f, 0.95f)
                                    val r = (1f - normDist) * maxR
                                    val angleDeg = (peer.id.hashCode() % 360).toDouble()
                                    val rad = angleDeg * PI / 180.0
                                    val x = center.x + (r * cos(rad)).toFloat()
                                    val y = center.y + (r * sin(rad)).toFloat()
                                    val dx = offset.x - x
                                    val dy = offset.y - y
                                    dx * dx + dy * dy
                                }
                            }
                        }
                ) {
                    val center = Offset(size.width / 2f, size.height / 2f)
                    val maxRadius = size.width / 2f * 0.85f
                    val ringColor = colors.outline.copy(alpha = 0.5f)

                    // Concentric Range Rings
                    listOf(0.25f, 0.50f, 0.75f, 1.0f).forEach { fraction ->
                        drawCircle(
                            color = ringColor,
                            radius = maxRadius * fraction,
                            center = center,
                            style = Stroke(width = 1.dp.toPx())
                        )
                    }

                    // Crosshairs
                    drawLine(
                        color = ringColor,
                        start = Offset(center.x, center.y - maxRadius),
                        end = Offset(center.x, center.y + maxRadius),
                        strokeWidth = 1.dp.toPx()
                    )
                    drawLine(
                        color = ringColor,
                        start = Offset(center.x - maxRadius, center.y),
                        end = Offset(center.x + maxRadius, center.y),
                        strokeWidth = 1.dp.toPx()
                    )

                    // Rotating Radar Sweep Line
                    if (isDiscovering) {
                        rotate(sweepAngle, pivot = center) {
                            drawLine(
                                color = colors.accent,
                                start = center,
                                end = Offset(center.x, center.y - maxRadius),
                                strokeWidth = 2.dp.toPx()
                            )
                        }
                    }

                    // Center Node: This Device
                    drawCircle(
                        color = colors.textPrimary,
                        radius = 7.dp.toPx(),
                        center = center
                    )
                    drawCircle(
                        color = colors.background,
                        radius = 3.dp.toPx(),
                        center = center
                    )

                    // Plot Live Peer Nodes as Radial Blips
                    allPeers.forEach { peer ->
                        val isConnected = connectedPeers.any { it.id == peer.id }
                        val normDist = ((peer.signalStrengthDbm + 100).coerceIn(0, 70) / 70f).coerceIn(0.15f, 0.95f)
                        val r = (1f - normDist) * maxRadius
                        val angleDeg = (peer.id.hashCode() % 360).toDouble()
                        val rad = angleDeg * PI / 180.0
                        val x = center.x + (r * cos(rad)).toFloat()
                        val y = center.y + (r * sin(rad)).toFloat()

                        val blipColor = if (isConnected) colors.accent else colors.textSecondary

                        // Glow ring around blip
                        drawCircle(
                            color = blipColor.copy(alpha = 0.2f),
                            radius = 12.dp.toPx(),
                            center = Offset(x, y)
                        )
                        // Solid blip dot
                        drawCircle(
                            color = blipColor,
                            radius = 6.dp.toPx(),
                            center = Offset(x, y)
                        )
                    }
                }
            }
        }

        // Section 4: Selected Node or Connected Nodes list
        if (selectedPeer != null) {
            val peer = selectedPeer!!
            val isConnected = connectedPeers.any { it.id == peer.id }
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(colors.surface)
                        .border(1.dp, if (isConnected) colors.accent else colors.outline, RoundedCornerShape(16.dp))
                        .padding(16.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    if (isConnected) {
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = null,
                                            tint = colors.accent,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                    }
                                    Text(
                                        text = peer.name,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = colors.textPrimary
                                    )
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "${peer.protocol.name} · RSSI: ${peer.signalStrengthDbm} dBm",
                                    fontSize = 13.sp,
                                    color = colors.textSecondary
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (isConnected) colors.accentContainer else colors.outline)
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = if (isConnected) "CONNECTED" else "DISCOVERED",
                                    style = androidx.compose.material3.MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.sp
                                    ),
                                    color = if (isConnected) colors.accent else colors.textPrimary
                                )
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            Button(
                                onClick = {
                                    if (isConnected) viewModel.disconnectPeer(peer.id)
                                    else viewModel.connectToPeer(peer)
                                },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isConnected) colors.surface else colors.textPrimary,
                                    contentColor = if (isConnected) colors.textPrimary else colors.background
                                ),
                                border = if (isConnected) androidx.compose.foundation.BorderStroke(1.dp, colors.outline) else null,
                                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                            ) {
                                Icon(
                                    imageVector = if (isConnected) Icons.Filled.LinkOff else Icons.Outlined.PersonAdd,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(Modifier.width(6.dp))
                                Text(
                                    text = if (isConnected) "Disconnect" else "Connect",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }
        } else {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(colors.surface)
                        .border(1.dp, colors.outline, RoundedCornerShape(16.dp))
                        .padding(14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "${allPeers.size} live hardware nodes mapped · Tap Search Peers to scan",
                        fontSize = 13.sp,
                        color = colors.textSecondary,
                        textAlign = TextAlign.Center
                    )
                }
            }
            
            // Show connected peers underneath if we haven't selected one specifically
            if (connectedPeers.isNotEmpty()) {
                item {
                    Text(
                        text = "Connected Peers",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = colors.textSecondary,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }
                items(connectedPeers) { peer ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(colors.surface)
                            .border(1.dp, colors.accent, RoundedCornerShape(16.dp))
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        tint = colors.accent,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = peer.name,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = colors.textPrimary
                                    )
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "${peer.protocol.name} · ${peer.address}",
                                    fontSize = 13.sp,
                                    color = colors.textSecondary
                                )
                            }

                            OutlinedButton(
                                onClick = { viewModel.disconnectPeer(peer.id) },
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Disconnect", fontSize = 13.sp, color = colors.textPrimary)
                            }
                        }
                    }
                }
            }
        }

        // Section 5: Direct IP Connection
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Direct IP link",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium,
                    color = colors.textPrimary
                )
                Text(
                    text = "Connect directly to a peer on the local network",
                    fontSize = 13.sp,
                    color = colors.textSecondary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = manualIpInput,
                        onValueChange = { manualIpInput = it },
                        placeholder = { Text("192.168.1.100", color = colors.textSecondary) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = colors.accent,
                            unfocusedBorderColor = colors.outline,
                            focusedContainerColor = colors.surface,
                            unfocusedContainerColor = colors.surface,
                            focusedTextColor = colors.textPrimary,
                            unfocusedTextColor = colors.textPrimary
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    )

                    OutlinedButton(
                        onClick = {
                            if (manualIpInput.isNotBlank()) {
                                viewModel.connectDirectIp(manualIpInput.trim(), 8889)
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.height(56.dp)
                    ) {
                        Text("Connect", fontSize = 13.sp, color = colors.textPrimary)
                    }
                }
            }
        }
    }
}
