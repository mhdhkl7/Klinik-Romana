package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ClinicRepository
import com.example.data.ClinicState
import com.example.model.*
import com.example.ui.components.StatCard
import com.example.ui.components.StatusBadge
import com.example.ui.theme.*
import java.text.NumberFormat
import java.util.*

@Composable
fun ClinicDashboardScreen(
    state: ClinicState,
    repository: ClinicRepository
) {
    val selectedTab = state.selectedTab
    var selectedOpnameForReview by remember { mutableStateOf<StockOpnameRecord?>(null) }
    var showAssignTaskDialog by remember { mutableStateOf(false) }

    val currencyFormat = remember {
        NumberFormat.getCurrencyInstance(Locale("id", "ID")).apply {
            maximumFractionDigits = 0
        }
    }

    val totalVisits = state.queue.size
    val bpjsCount = state.queue.count { it.payerType == PayerType.BPJS }
    val umumCount = state.queue.count { it.payerType != PayerType.BPJS }

    // Revenue calculation: BPJS Claim Rp 120.000, Umum Rp 50.000
    val bpjsRevenue = bpjsCount * 120000L
    val umumRevenue = umumCount * 50000L
    val lowStockCount = state.medicines.count { it.isLowStock || it.isOutOfStock }
    val pendingOpnames = state.opnameRecords.filter { it.status == OpnameStatus.MENUNGGU_APPROVAL }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ClinicBgLight)
    ) {
        when (selectedTab) {
            0 -> {
                // TAB 0: Executive Dashboard KPI Real-Time
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = ClinicalTealPrimary),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "Executive Clinic Dashboard",
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    text = "Kepala Klinik: dr. Stephen Parnahean • Real-time Monitoring",
                                    fontSize = 12.sp,
                                    color = ClinicalTealLight
                                )
                            }
                        }
                    }

                    item {
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            StatCard(
                                title = "Total Kunjungan",
                                value = "$totalVisits Pasien",
                                subtitle = "$bpjsCount BPJS • $umumCount Umum",
                                icon = Icons.Default.People,
                                iconTint = ClinicalTealPrimary,
                                iconBg = ClinicalTealContainer,
                                modifier = Modifier.weight(1f)
                            )
                            StatCard(
                                title = "Peringatan Stok",
                                value = "$lowStockCount Obat",
                                subtitle = "Di bawah batas minimum",
                                icon = Icons.Default.Warning,
                                iconTint = StatusAlertRed,
                                iconBg = StatusAlertRedBg,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    item {
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            StatCard(
                                title = "Klaim BPJS (JKN)",
                                value = currencyFormat.format(bpjsRevenue),
                                subtitle = "Tarif Rp 120.000 / Pasien",
                                icon = Icons.Default.Security,
                                iconTint = StatusDoneGreen,
                                iconBg = StatusDoneGreenBg,
                                modifier = Modifier.weight(1f)
                            )
                            StatCard(
                                title = "Kas Pasien Umum",
                                value = currencyFormat.format(umumRevenue),
                                subtitle = "Tarif Rp 50.000 / Pasien",
                                icon = Icons.Default.Payments,
                                iconTint = StatusInfoBlue,
                                iconBg = StatusInfoBlueBg,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text("Sebaran Antrean Unit Layanan Hari Ini", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Spacer(modifier = Modifier.height(10.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceAround
                                ) {
                                    StageBadgeColumn("Triage", state.queue.count { it.status == QueueStatus.MENUNGGU_TRIAGE }, StatusWaitYellow)
                                    StageBadgeColumn("Dokter", state.queue.count { it.status == QueueStatus.SIAP_DIPERIKSA_DOKTER }, StatusInfoBlue)
                                    StageBadgeColumn("Farmasi", state.queue.count { it.status == QueueStatus.MENUNGGU_FARMASI }, StatusWaitYellow)
                                    StageBadgeColumn("Kasir", state.queue.count { it.status == QueueStatus.MENUNGGU_PEMBAYARAN }, StatusWaitYellow)
                                    StageBadgeColumn("Selesai", state.queue.count { it.status == QueueStatus.SELESAI }, StatusDoneGreen)
                                }
                            }
                        }
                    }
                }
            }

            1 -> {
                // TAB 1: Analisis Operasional & Bottlenecks
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        Text("Analisis Efisiensi & Bottleneck Alur Pelayanan", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    }

                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                BottleneckItem("Kecepatan Triage Perawat", "Lancar (< 7 menit / pasien)", StatusDoneGreenText)
                                HorizontalDivider()
                                BottleneckItem("Waktu Tunggu Konsultasi Dokter", "Rata-rata 12 menit / pasien", StatusInfoBlueText)
                                HorizontalDivider()
                                BottleneckItem("Logistik Stok Farmasi", "Waspada: Paracetamol (12) & Omeprazole (5)", StatusAlertRed)
                            }
                        }
                    }
                }
            }

            2 -> {
                // TAB 2: Persetujuan & Tugas
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Persetujuan Stok Opname (${pendingOpnames.size})", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            Button(
                                onClick = { showAssignTaskDialog = true },
                                colors = ButtonDefaults.buttonColors(containerColor = ClinicalTealPrimary),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Icon(Icons.Default.AddTask, contentDescription = null, modifier = Modifier.size(15.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Tugaskan Staf", fontSize = 11.sp)
                            }
                        }
                    }

                    if (pendingOpnames.isEmpty()) {
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Box(modifier = Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                                    Text("Semua pengajuan stok opname telah ditinjau.", color = Color.Gray, fontSize = 12.sp)
                                }
                            }
                        }
                    } else {
                        items(pendingOpnames) { opn ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(opn.medicineName, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                        Text(
                                            "Selisih: ${opn.variance}",
                                            fontWeight = FontWeight.Bold,
                                            color = StatusAlertRed
                                        )
                                    }
                                    Text("Sistem: ${opn.systemStock} ➔ Fisik: ${opn.physicalCount} • Oleh: ${opn.submittedBy}", fontSize = 11.sp, color = Color.Gray)
                                    Text("Alasan: \"${opn.reason}\"", fontSize = 12.sp)
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.End
                                    ) {
                                        OutlinedButton(
                                            onClick = { repository.approveStockOpname(opn.id, false, note = "Ditolak: hitung ulang") },
                                            colors = ButtonDefaults.outlinedButtonColors(contentColor = StatusAlertRed),
                                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                                        ) {
                                            Text("Tolak", fontSize = 11.sp)
                                        }
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Button(
                                            onClick = { repository.approveStockOpname(opn.id, true, note = "Disetujui untuk penyesuaian") },
                                            colors = ButtonDefaults.buttonColors(containerColor = StatusDoneGreen),
                                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                                        ) {
                                            Text("Setujui (Update Stok)", fontSize = 11.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    item {
                        Text("Daftar Tugas Staf (${state.staffTasks.size})", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }

                    items(state.staffTasks) { task ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(task.instruction, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    Text("Untuk: ${task.assignee} • Prioritas: ${task.priority}", fontSize = 10.sp, color = Color.Gray)
                                }
                                Checkbox(
                                    checked = task.isCompleted,
                                    onCheckedChange = { repository.toggleTaskComplete(task.id) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAssignTaskDialog) {
        var assignee by remember { mutableStateOf("Muhammad Haikal Siregar, S.Farm") }
        var instruction by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showAssignTaskDialog = false },
            title = { Text("Tugaskan Staf / Buat Instruksi") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Pilih Staf:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    OutlinedTextField(
                        value = assignee,
                        onValueChange = { assignee = it },
                        label = { Text("Nama Staf") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = instruction,
                        onValueChange = { instruction = it },
                        label = { Text("Instruksi Tugas *") },
                        minLines = 2,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (instruction.isNotBlank()) {
                            repository.assignStaffTask(assignee, instruction, "Tinggi")
                            showAssignTaskDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ClinicalTealPrimary)
                ) {
                    Text("Kirim Tugas")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAssignTaskDialog = false }) {
                    Text("Batal")
                }
            }
        )
    }
}

@Composable
fun StageBadgeColumn(title: String, count: Int, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(color.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "$count",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = color
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(title, fontSize = 11.sp, color = Color.Gray)
    }
}

@Composable
fun BottleneckItem(title: String, status: String, statusColor: Color) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, fontSize = 13.sp, fontWeight = FontWeight.Bold)
        Text(status, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = statusColor)
    }
}
