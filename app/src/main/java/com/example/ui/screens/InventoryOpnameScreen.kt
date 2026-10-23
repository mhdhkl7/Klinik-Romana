package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import java.text.NumberFormat
import java.util.*

@Composable
fun InventoryOpnameScreen(
    state: ClinicState,
    repository: ClinicRepository
) {
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Katalog Stok, 1: Form Opname Fisik, 2: Log Mutasi
    var showReceiptDialog by remember { mutableStateOf(false) }
    var selectedMedForOpname by remember { mutableStateOf<MedicineItem?>(null) }
    var searchQuery by remember { mutableStateOf("") }

    val currencyFormat = remember {
        NumberFormat.getCurrencyInstance(Locale("id", "ID")).apply {
            maximumFractionDigits = 0
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ClinicBgLight)
    ) {
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = Color.White,
            contentColor = ClinicalTealPrimary
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("Katalog & Stok", fontWeight = FontWeight.SemiBold) },
                icon = { Icon(Icons.Default.Inventory, contentDescription = null, modifier = Modifier.size(18.dp)) }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = {
                    val pendingCount = state.opnameRecords.count { it.status == OpnameStatus.MENUNGGU_APPROVAL }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Stok Opname", fontWeight = FontWeight.SemiBold)
                        if (pendingCount > 0) {
                            Spacer(modifier = Modifier.width(4.dp))
                            Badge(containerColor = StatusWaitYellow) {
                                Text("$pendingCount", color = Color.White)
                            }
                        }
                    }
                },
                icon = { Icon(Icons.Default.FactCheck, contentDescription = null, modifier = Modifier.size(18.dp)) }
            )
            Tab(
                selected = selectedTab == 2,
                onClick = { selectedTab = 2 },
                text = { Text("Log Mutasi", fontWeight = FontWeight.SemiBold) },
                icon = { Icon(Icons.Default.History, contentDescription = null, modifier = Modifier.size(18.dp)) }
            )
        }

        when (selectedTab) {
            0 -> {
                // Tab 0: Inventory Catalog
                Column(
                    modifier = Modifier.fillMaxSize().padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = { Text("Cari obat atau kode...") },
                            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                            singleLine = true,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = { showReceiptDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = ClinicalTealPrimary),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.testTag("btn_receive_stock")
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Terima Obat")
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    val filteredMeds = state.medicines.filter {
                        it.name.contains(searchQuery, ignoreCase = true) || it.code.contains(searchQuery, ignoreCase = true)
                    }

                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(filteredMeds) { med ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(med.name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text("(${med.code})", fontSize = 11.sp, color = Color.Gray)
                                        }
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            "Batch: ${med.batchNumber} • Exp: ${med.expiryDate}",
                                            fontSize = 11.sp,
                                            color = Color.Gray
                                        )
                                        Text(
                                            "Kategori: ${med.category} • Harga: ${currencyFormat.format(med.unitPrice)}/${med.unit}",
                                            fontSize = 11.sp,
                                            color = Color(0xFF334155)
                                        )
                                    }

                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(
                                            text = "${med.currentStock} ${med.unit}",
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = if (med.isOutOfStock) StatusAlertRed else if (med.isLowStock) StatusWaitYellow else ClinicalTealPrimary
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        StatusBadge(
                                            text = if (med.isOutOfStock) "KOSONG" else if (med.isLowStock) "STOK KRITIS" else "AMAN",
                                            bg = if (med.isOutOfStock) StatusAlertRedBg else if (med.isLowStock) StatusWaitYellowBg else StatusDoneGreenBg,
                                            textColor = if (med.isOutOfStock) StatusAlertRedText else if (med.isLowStock) StatusWaitYellowText else StatusDoneGreenText
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        OutlinedButton(
                                            onClick = { selectedMedForOpname = med },
                                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                            shape = RoundedCornerShape(6.dp)
                                        ) {
                                            Text("Opname", fontSize = 10.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            1 -> {
                // Tab 1: Stock Opname Audit Module
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = ClinicalTealContainer),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text(
                                    text = "Audit Fisik & Stok Opname Obat",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = OnClinicalTealContainer
                                )
                                Text(
                                    text = "Bandingkan hitungan fisik di rak apotek dengan saldo sistem. Setiap selisih (+/-) membutuhkan persetujuan Kepala Klinik.",
                                    fontSize = 12.sp,
                                    color = Color(0xFF004D40)
                                )
                            }
                        }
                    }

                    item {
                        Text(
                            text = "Riwayat & Pengajuan Opname (${state.opnameRecords.size})",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0F172A)
                        )
                    }

                    items(state.opnameRecords) { opn ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(opn.medicineName, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                        Text("ID: ${opn.id} • Tgl: ${opn.date}", fontSize = 11.sp, color = Color.Gray)
                                    }
                                    StatusBadge(
                                        text = opn.status.label,
                                        bg = opn.status.bg,
                                        textColor = opn.status.text
                                    )
                                }

                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceAround
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text("Stok Sistem", fontSize = 11.sp, color = Color.Gray)
                                        Text("${opn.systemStock}", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    }
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text("Hitung Fisik", fontSize = 11.sp, color = Color.Gray)
                                        Text("${opn.physicalCount}", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    }
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text("Selisih (Variance)", fontSize = 11.sp, color = Color.Gray)
                                        Text(
                                            "${if (opn.variance > 0) "+${opn.variance}" else "${opn.variance}"}",
                                            fontWeight = FontWeight.ExtraBold,
                                            fontSize = 14.sp,
                                            color = if (opn.variance == 0) StatusDoneGreenText else StatusAlertRed
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Alasan Selisih: \"${opn.reason}\"",
                                    fontSize = 12.sp,
                                    color = Color(0xFF334155),
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = "Diajukan oleh: ${opn.submittedBy}",
                                    fontSize = 11.sp,
                                    color = Color.Gray
                                )

                                if (opn.approvedBy != null) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Catatan Pimpinan (${opn.approvedBy}): \"${opn.approvalNote}\"",
                                        fontSize = 11.sp,
                                        color = ClinicalTealPrimary,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }
                    }
                }
            }

            2 -> {
                // Tab 2: Stock Mutation History
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    item {
                        Text(
                            text = "Log Mutasi Obat Real-Time (${state.stockMutations.size} Transaksi)",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0F172A)
                        )
                    }

                    items(state.stockMutations) { mut ->
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
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(38.dp)
                                            .clip(CircleShape)
                                            .background(
                                                if (mut.type == StockMutationType.MASUK) StatusDoneGreenBg else StatusAlertRedBg
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = if (mut.type == StockMutationType.MASUK) Icons.Default.ArrowDownward else Icons.Default.ArrowUpward,
                                            contentDescription = null,
                                            tint = if (mut.type == StockMutationType.MASUK) StatusDoneGreen else StatusAlertRed,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(mut.medicineName, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                        Text(mut.referenceInfo, fontSize = 11.sp, color = Color(0xFF334155))
                                        Text("Oleh: ${mut.operator} • ${mut.timestamp}", fontSize = 10.sp, color = Color.Gray)
                                    }
                                }

                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = "${if (mut.quantityChange > 0) "+${mut.quantityChange}" else "${mut.quantityChange}"}",
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 15.sp,
                                        color = if (mut.quantityChange > 0) StatusDoneGreenText else StatusAlertRed
                                    )
                                    Text("Sisa: ${mut.resultingStock}", fontSize = 11.sp, color = Color.Gray)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Modal: Opname Physical Count Input
    selectedMedForOpname?.let { med ->
        OpnameEntryDialog(
            medicine = med,
            onDismiss = { selectedMedForOpname = null },
            onSubmitOpname = { count, reason ->
                repository.submitStockOpname(med.id, count, reason)
                selectedMedForOpname = null
                selectedTab = 1
            }
        )
    }

    // Modal: Manual Receipt of Stock
    if (showReceiptDialog) {
        StockReceiptDialog(
            medicines = state.medicines,
            onDismiss = { showReceiptDialog = false },
            onSaveReceipt = { medId, qty, batch, supplier ->
                repository.addManualStockReceipt(medId, qty, batch, supplier)
                showReceiptDialog = false
            }
        )
    }
}

@Composable
fun OpnameEntryDialog(
    medicine: MedicineItem,
    onDismiss: () -> Unit,
    onSubmitOpname: (physicalCount: Int, reason: String) -> Unit
) {
    var physicalCountStr by remember { mutableStateOf("${medicine.currentStock}") }
    var reason by remember { mutableStateOf("Penghitungan fisik berkala lemari obat") }

    val physicalCount = physicalCountStr.toIntOrNull() ?: medicine.currentStock
    val variance = physicalCount - medicine.currentStock

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.FactCheck, contentDescription = null, tint = ClinicalTealPrimary)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Stok Opname Fisik")
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(medicine.name, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                Text("Saldo Sistem Saat Ini: ${medicine.currentStock} ${medicine.unit}", fontSize = 12.sp, color = Color.Gray)

                OutlinedTextField(
                    value = physicalCountStr,
                    onValueChange = { physicalCountStr = it },
                    label = { Text("Jumlah Hitungan Fisik di Rak *") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth().testTag("input_physical_count")
                )

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = if (variance == 0) StatusDoneGreenBg else StatusAlertRedBg
                    )
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Selisih Fisik vs Sistem:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Text(
                            "${if (variance > 0) "+$variance" else "$variance"} ${medicine.unit}",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (variance == 0) StatusDoneGreenText else StatusAlertRed
                        )
                    }
                }

                OutlinedTextField(
                    value = reason,
                    onValueChange = { reason = it },
                    label = { Text("Alasan Selisih / Keterangan *") },
                    placeholder = { Text("cth: Kemasan rusak, selisih kartu stok") },
                    minLines = 2,
                    modifier = Modifier.fillMaxWidth().testTag("input_opname_reason")
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onSubmitOpname(physicalCount, reason) },
                colors = ButtonDefaults.buttonColors(containerColor = ClinicalTealPrimary),
                modifier = Modifier.testTag("btn_submit_opname")
            ) {
                Text("Kirim untuk Approval Pimpinan")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Batal")
            }
        }
    )
}

@Composable
fun StockReceiptDialog(
    medicines: List<MedicineItem>,
    onDismiss: () -> Unit,
    onSaveReceipt: (medId: String, qty: Int, batch: String, supplier: String) -> Unit
) {
    var selectedMed by remember { mutableStateOf(medicines.first()) }
    var qtyStr by remember { mutableStateOf("50") }
    var batchNumber by remember { mutableStateOf("BCH-2024-12P") }
    var supplier by remember { mutableStateOf("PBF Kimia Farma Trading") }
    var expanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.AddBusiness, contentDescription = null, tint = ClinicalTealPrimary)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Penerimaan Obat Baru (PBF)")
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Pilih Obat:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                Box {
                    OutlinedTextField(
                        value = "${selectedMed.name} (Stok: ${selectedMed.currentStock})",
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
                        medicines.forEach { m ->
                            DropdownMenuItem(
                                text = { Text("${m.name} (${m.code})") },
                                onClick = {
                                    selectedMed = m
                                    expanded = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = qtyStr,
                    onValueChange = { qtyStr = it },
                    label = { Text("Jumlah Masuk (${selectedMed.unit}) *") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = batchNumber,
                    onValueChange = { batchNumber = it },
                    label = { Text("Nomor Batch Pabrikan") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = supplier,
                    onValueChange = { supplier = it },
                    label = { Text("PBF Distributor") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val q = qtyStr.toIntOrNull() ?: 10
                    onSaveReceipt(selectedMed.id, q, batchNumber, supplier)
                },
                colors = ButtonDefaults.buttonColors(containerColor = ClinicalTealPrimary)
            ) {
                Text("Simpan Penerimaan")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Batal")
            }
        }
    )
}
