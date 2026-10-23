package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ClinicRepository
import com.example.data.ClinicState
import com.example.model.*
import com.example.ui.components.StatusBadge
import com.example.ui.theme.*

@Composable
fun PharmacyScreen(
    state: ClinicState,
    repository: ClinicRepository
) {
    val selectedTab = state.selectedTab
    var substitutionDialogItem by remember { mutableStateOf<QueueItem?>(null) }
    var dispenseConfirmItem by remember { mutableStateOf<QueueItem?>(null) }

    val activeRxQueue = state.queue.filter { it.status == QueueStatus.MENUNGGU_FARMASI }
    val lowStockMedicines = state.medicines.filter { it.isLowStock || it.isOutOfStock }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ClinicBgLight)
    ) {
        when (selectedTab) {
            0 -> {
                // TAB 0: Antrean Resep Masuk
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = ClinicalTealContainer),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier.size(42.dp).clip(CircleShape).background(ClinicalTealPrimary),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.Medication, contentDescription = null, tint = Color.White, modifier = Modifier.size(24.dp))
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Instalasi Farmasi & Apotek",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        color = OnClinicalTealContainer
                                    )
                                    Text(
                                        text = "Apoteker: Muhammad Haikal Siregar, S.Farm • ${activeRxQueue.size} resep baru",
                                        fontSize = 12.sp,
                                        color = Color(0xFF004D40)
                                    )
                                }
                            }
                        }
                    }

                    item {
                        Text(
                            text = "Antrean E-Resep Masuk (${activeRxQueue.size})",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0F172A)
                        )
                    }

                    if (activeRxQueue.isEmpty()) {
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Box(modifier = Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                                    Text("Tidak ada resep baru menunggu penyiapan obat.", color = Color.Gray)
                                }
                            }
                        }
                    } else {
                        items(activeRxQueue) { item ->
                            val rxItems = item.clinicalRecord?.prescriptionItems ?: emptyList()

                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "${item.queueNumber} - ${item.patient.nama}",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp
                                        )
                                        StatusBadge(
                                            text = item.payerType.displayName,
                                            bg = if (item.payerType.isBpjs) StatusDoneGreenBg else StatusInfoBlueBg,
                                            textColor = if (item.payerType.isBpjs) StatusDoneGreenText else StatusInfoBlueText
                                        )
                                    }

                                    HorizontalDivider()
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text("Rincian Resep Dokter:", fontSize = 12.sp, fontWeight = FontWeight.Bold)

                                    rxItems.forEach { rx ->
                                        val med = state.medicines.find { it.id == rx.medicineId }
                                        val currentStock = med?.currentStock ?: 0
                                        val isSufficient = currentStock >= rx.quantity

                                        Card(
                                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                            colors = CardDefaults.cardColors(
                                                containerColor = if (isSufficient) ClinicalSurfaceVariant else StatusAlertRedBg
                                            )
                                        ) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth().padding(8.dp),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Column {
                                                    Text(rx.medicineName, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                                    Text("${rx.dosage} • Aturan: ${rx.frequency} (${rx.aturanPakai})", fontSize = 11.sp, color = Color.Gray)
                                                    Text("Jumlah: ${rx.quantity} ${med?.unit ?: "item"}", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                                                }
                                                Text(
                                                    text = "Stok: $currentStock",
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 12.sp,
                                                    color = if (isSufficient) StatusDoneGreenText else StatusAlertRed
                                                )
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        OutlinedButton(
                                            onClick = { substitutionDialogItem = item },
                                            modifier = Modifier.weight(1f),
                                            colors = ButtonDefaults.outlinedButtonColors(contentColor = StatusAlertRed)
                                        ) {
                                            Icon(Icons.Default.SwapHoriz, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Minta Substitusi", fontSize = 11.sp)
                                        }

                                        Button(
                                            onClick = { dispenseConfirmItem = item },
                                            colors = ButtonDefaults.buttonColors(containerColor = ClinicalTealPrimary),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Serahkan Obat", fontSize = 11.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            1 -> {
                // TAB 1: Katalog Stok & Alert
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (lowStockMedicines.isNotEmpty()) {
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = StatusAlertRedBg)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Warning, contentDescription = null, tint = StatusAlertRed)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            "PERINGATAN: ${lowStockMedicines.size} Obat di Bawah Batas Minimum!",
                                            fontWeight = FontWeight.Bold,
                                            color = StatusAlertRedText,
                                            fontSize = 12.sp
                                        )
                                    }
                                    lowStockMedicines.forEach { med ->
                                        Text(
                                            " • ${med.name}: Sisa ${med.currentStock} ${med.unit} (Batas Min: ${med.minStockAlert})",
                                            fontSize = 11.sp,
                                            color = StatusAlertRedText
                                        )
                                    }
                                }
                            }
                        }
                    }

                    item {
                        Text("Katalog Obat & Stok Real-Time", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                    }

                    items(state.medicines) { med ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(med.name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    Text("Batch: ${med.batchNumber} • Exp: ${med.expiryDate}", fontSize = 11.sp, color = Color.Gray)
                                    Text("Harga: Rp ${med.unitPrice} / ${med.unit}", fontSize = 11.sp, color = ClinicalTealPrimary)
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        "${med.currentStock} ${med.unit}",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (med.isLowStock) StatusAlertRed else ClinicalTealPrimary
                                    )
                                    Text("Batas Min: ${med.minStockAlert}", fontSize = 10.sp, color = Color.Gray)
                                }
                            }
                        }
                    }
                }
            }

            2 -> {
                // TAB 2: Stok Opname & Audit
                PharmacyOpnameTab(state = state, repository = repository)
            }
        }
    }

    // Modal: Minta Substitusi Obat ke Dokter
    substitutionDialogItem?.let { item ->
        var note by remember { mutableStateOf("Stok fisik di rak apotek kosong/rusak. Mohon berikan resep obat alternatif pengganti.") }
        AlertDialog(
            onDismissRequest = { substitutionDialogItem = null },
            title = { Text("Minta Substitusi Obat ke Dokter") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Pasien: ${item.queueNumber} - ${item.patient.nama}", fontWeight = FontWeight.Bold)
                    OutlinedTextField(
                        value = note,
                        onValueChange = { note = it },
                        label = { Text("Catatan untuk Dokter *") },
                        minLines = 3,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        repository.requestDrugSubstitution(item.id, note)
                        substitutionDialogItem = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = StatusAlertRed)
                ) {
                    Text("Kirim Balik ke Dokter")
                }
            },
            dismissButton = {
                TextButton(onClick = { substitutionDialogItem = null }) {
                    Text("Batal")
                }
            }
        )
    }

    // Modal: Konfirmasi Serahkan Obat & Potong Stok
    dispenseConfirmItem?.let { item ->
        AlertDialog(
            onDismissRequest = { dispenseConfirmItem = null },
            title = { Text("Konfirmasi Penyerahan Obat") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Pasien: ${item.queueNumber} (${item.patient.nama})", fontWeight = FontWeight.Bold)
                    Text("Stok obat sistem akan otomatis dipotong dan dicatat ke Log Mutasi Farmasi.", fontSize = 12.sp, color = Color.Gray)
                    HorizontalDivider()
                    item.clinicalRecord?.prescriptionItems?.forEach { rx ->
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(" • ${rx.medicineName}", fontSize = 12.sp)
                            Text("-${rx.quantity}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = StatusAlertRed)
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        repository.dispensePrescription(item.id)
                        dispenseConfirmItem = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ClinicalTealPrimary)
                ) {
                    Text("Serahkan & Potong Stok")
                }
            },
            dismissButton = {
                TextButton(onClick = { dispenseConfirmItem = null }) {
                    Text("Batal")
                }
            }
        )
    }
}

@Composable
fun PharmacyOpnameTab(
    state: ClinicState,
    repository: ClinicRepository
) {
    var selectedMed by remember { mutableStateOf(state.medicines.first()) }
    var physicalCountStr by remember { mutableStateOf("${selectedMed.currentStock}") }
    var reason by remember { mutableStateOf("Penghitungan fisik rak penyimpanan apotek") }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(10.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text("Formulir Pengajuan Stok Opname Fisik", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    Spacer(modifier = Modifier.height(10.dp))

                    Text("Pilih Obat:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    DropdownButtonBox(
                        items = state.medicines,
                        selected = selectedMed,
                        onSelect = {
                            selectedMed = it
                            physicalCountStr = "${it.currentStock}"
                        }
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = physicalCountStr,
                        onValueChange = { physicalCountStr = it },
                        label = { Text("Hitungan Fisik di Rak *") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = reason,
                        onValueChange = { reason = it },
                        label = { Text("Alasan Selisih / Kerusakan *") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = {
                            val count = physicalCountStr.toIntOrNull() ?: selectedMed.currentStock
                            repository.submitStockOpname(selectedMed.id, count, reason)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = ClinicalTealPrimary),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Ajukan Koreksi Stok ke Kepala Klinik")
                    }
                }
            }
        }

        item {
            Text("Log Mutasi Obat Real-Time (${state.stockMutations.size})", fontSize = 14.sp, fontWeight = FontWeight.Bold)
        }

        items(state.stockMutations) { mut ->
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
                    Column {
                        Text(mut.medicineName, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text("${mut.referenceInfo} • Oleh: ${mut.operator}", fontSize = 11.sp, color = Color.Gray)
                    }
                    Text(
                        "${if (mut.quantityChange > 0) "+${mut.quantityChange}" else "${mut.quantityChange}"} (Sisa: ${mut.resultingStock})",
                        fontWeight = FontWeight.Bold,
                        color = if (mut.quantityChange < 0) StatusAlertRed else StatusDoneGreenText
                    )
                }
            }
        }
    }
}

@Composable
fun DropdownButtonBox(
    items: List<MedicineItem>,
    selected: MedicineItem,
    onSelect: (MedicineItem) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Box {
        OutlinedTextField(
            value = "${selected.name} (Stok Sistem: ${selected.currentStock})",
            onValueChange = {},
            readOnly = true,
            trailingIcon = {
                IconButton(onClick = { expanded = true }) {
                    Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                }
            },
            modifier = Modifier.fillMaxWidth()
        )
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            items.forEach { m ->
                DropdownMenuItem(
                    text = { Text("${m.name} (Stok: ${m.currentStock})") },
                    onClick = {
                        onSelect(m)
                        expanded = false
                    }
                )
            }
        }
    }
}
