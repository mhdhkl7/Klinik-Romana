package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.QueueItem
import com.example.model.Role
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClinicTopBar(
    currentRole: Role,
    activeCallingQueue: QueueItem?,
    onRoleSelected: (Role) -> Unit,
    onResetDemoData: () -> Unit
) {
    var showRoleMenu by remember { mutableStateOf(false) }
    var showResetDialog by remember { mutableStateOf(false) }

    Surface(
        color = ClinicalTealPrimary,
        tonalElevation = 4.dp
    ) {
        Column(modifier = Modifier.fillMaxWidth().statusBarsPadding()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Clinic Branding
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(Color.White),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocalHospital,
                            contentDescription = "Logo Klinik",
                            tint = ClinicalTealPrimary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Klinik Romana",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "${currentRole.staffName} (${currentRole.shortName})",
                            fontSize = 11.sp,
                            color = ClinicalTealLight
                        )
                    }
                }

                // Persona Switcher Button
                Box {
                    Button(
                        onClick = { showRoleMenu = true },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.White.copy(alpha = 0.2f),
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(20.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                        modifier = Modifier.testTag("role_switcher_button")
                    ) {
                        Icon(
                            imageVector = getRoleIcon(currentRole),
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = currentRole.shortName.uppercase(),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = "Ganti Akun Demo",
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    DropdownMenu(
                        expanded = showRoleMenu,
                        onDismissRequest = { showRoleMenu = false }
                    ) {
                        Text(
                            text = "GANTI AKUN DEMO PERSONA",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Gray,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                        HorizontalDivider()
                        Role.values().forEach { role ->
                            DropdownMenuItem(
                                text = {
                                    Column {
                                        Text(
                                            text = role.staffName,
                                            fontWeight = FontWeight.Bold,
                                            color = if (role == currentRole) ClinicalTealPrimary else Color.Unspecified
                                        )
                                        Text(
                                            text = "${role.title} • ${role.subtitle}",
                                            fontSize = 11.sp,
                                            color = Color.Gray
                                        )
                                    }
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = getRoleIcon(role),
                                        contentDescription = null,
                                        tint = if (role == currentRole) ClinicalTealPrimary else Color.Gray
                                    )
                                },
                                trailingIcon = {
                                    if (role == currentRole) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = "Aktif",
                                            tint = ClinicalTealPrimary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                },
                                onClick = {
                                    onRoleSelected(role)
                                    showRoleMenu = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Reset Demo Data Button
                IconButton(
                    onClick = { showResetDialog = true },
                    modifier = Modifier.size(36.dp).testTag("reset_demo_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Reset Data Simulasi",
                        tint = Color.White
                    )
                }
            }

            // Calling Patient Audio/Visual Banner
            AnimatedVisibility(
                visible = activeCallingQueue != null,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                if (activeCallingQueue != null) {
                    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
                    val alpha by infiniteTransition.animateFloat(
                        initialValue = 0.85f,
                        targetValue = 1.0f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(600, easing = LinearEasing),
                            repeatMode = RepeatMode.Reverse
                        ),
                        label = "pulseAlpha"
                    )

                    Surface(
                        color = Color(0xFFE65100).copy(alpha = alpha),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.VolumeUp,
                                    contentDescription = "Panggilan Suara",
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "🔊 PANGGILAN: Antrean ${activeCallingQueue.queueNumber} (${activeCallingQueue.patient.nama})",
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Text(
                                text = "Menuju Triage",
                                color = Color.White,
                                fontSize = 11.sp,
                                modifier = Modifier
                                    .background(Color.Black.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }
        }
    }

    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = { Text("Reset Data Simulasi?") },
            text = { Text("Kembalikan antrean, rekam medis, dan stok obat ke kondisi awal demonstrasi Klinik Romana.") },
            confirmButton = {
                Button(
                    onClick = {
                        onResetDemoData()
                        showResetDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ClinicalTealPrimary)
                ) {
                    Text("Ya, Reset Data")
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) {
                    Text("Batal")
                }
            }
        )
    }
}

fun getRoleIcon(role: Role): ImageVector = when (role) {
    Role.ADMIN_PENDAFTARAN -> Icons.Default.AssignmentInd
    Role.PERAWAT_TRIAGE -> Icons.Default.Thermostat
    Role.DOKTER -> Icons.Default.MedicalServices
    Role.FARMASI_STOK -> Icons.Default.Medication
    Role.KEPALA_KLINIK -> Icons.Default.Assessment
}

@Composable
fun StatusBadge(
    text: String,
    bg: Color,
    textColor: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(bg)
            .padding(horizontal = 8.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = textColor
        )
    }
}

@Composable
fun StatCard(
    title: String,
    value: String,
    subtitle: String,
    icon: ImageVector,
    iconTint: Color,
    iconBg: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(iconBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(text = title, fontSize = 12.sp, color = Color.Gray)
                Text(
                    text = value,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0F172A)
                )
                Text(text = subtitle, fontSize = 11.sp, color = iconTint)
            }
        }
    }
}

@Composable
fun PatientProgressStepper(
    currentStatus: com.example.model.QueueStatus,
    modifier: Modifier = Modifier
) {
    if (currentStatus == com.example.model.QueueStatus.DILEWATI) {
        Surface(
            color = Color(0xFFFEF3C7),
            shape = RoundedCornerShape(4.dp),
            modifier = modifier
        ) {
            Text(
                text = "⚠️ Antrean Dilewati (Absent)",
                color = Color(0xFF92400E),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
            )
        }
        return
    }

    if (currentStatus == com.example.model.QueueStatus.BATAL) {
        Surface(
            color = Color(0xFFFEE2E2),
            shape = RoundedCornerShape(4.dp),
            modifier = modifier
        ) {
            Text(
                text = "❌ Kunjungan Dibatalkan",
                color = Color(0xFF991B1B),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
            )
        }
        return
    }

    val activeStage = currentStatus.stageIndex
    val steps = listOf("Reg", "Triage", "Dokter", "Farmasi", "Selesai")

    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        steps.forEachIndexed { index, stepName ->
            val stepNumber = index + 1
            val isDone = stepNumber < activeStage
            val isCurrent = stepNumber == activeStage

            val circleColor = when {
                isDone -> ClinicalTealPrimary
                isCurrent -> Color(0xFF00ACC1)
                else -> Color(0xFFE2E8F0)
            }
            val textColor = when {
                isDone || isCurrent -> Color.White
                else -> Color(0xFF64748B)
            }
            val labelColor = when {
                isCurrent -> ClinicalTealPrimary
                isDone -> Color(0xFF334155)
                else -> Color(0xFF94A3B8)
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(18.dp)
                        .clip(CircleShape)
                        .background(circleColor),
                    contentAlignment = Alignment.Center
                ) {
                    if (isDone) {
                        Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(12.dp))
                    } else {
                        Text(
                            text = "$stepNumber",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = textColor
                        )
                    }
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = stepName,
                    fontSize = 9.sp,
                    fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                    color = labelColor
                )
            }

            if (index < steps.size - 1) {
                val lineColor = if (stepNumber < activeStage) ClinicalTealPrimary else Color(0xFFE2E8F0)
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(2.dp)
                        .padding(horizontal = 2.dp)
                        .background(lineColor)
                )
            }
        }
    }
}

