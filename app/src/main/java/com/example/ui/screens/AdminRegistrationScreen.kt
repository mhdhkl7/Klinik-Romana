package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.example.ui.components.PatientProgressStepper
import com.example.ui.components.StatCard
import com.example.ui.components.StatusBadge
import com.example.ui.theme.*
import java.text.NumberFormat
import java.util.*

@Composable
fun AdminRegistrationScreen(
    state: ClinicState,
    repository: ClinicRepository
) {
    val selectedTab = state.selectedTab
    var showNewPatientDialog by remember { mutableStateOf(false) }
    var selectedPatientForVisit by remember { mutableStateOf<Patient?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    var checkoutQueueItem by remember { mutableStateOf<QueueItem?>(null) }

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
        when (selectedTab) {
            0 -> {
                // TAB 0: Registrasi & Antrean (Fast Patient Lookup & New Registration)
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
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "Loket Admisi & Pendaftaran Pasien",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = OnClinicalTealContainer
                                )
                                Text(
                                    text = "Petugas: Aditia Rach • Cek status JKN & terbitkan antrean poli.",
                                    fontSize = 12.sp,
                                    color = Color(0xFF004D40)
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Button(
                                    onClick = { showNewPatientDialog = true },
                                    colors = ButtonDefaults.buttonColors(containerColor = ClinicalTealPrimary),
                                    modifier = Modifier.fillMaxWidth().testTag("btn_register_new_patient")
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Registrasi Pasien Baru")
                                }
                            }
                        }
                    }

                    item {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("Cari pasien terdaftar (Siti Rahma, Budi Santoso)...") },
                            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                            trailingIcon = {
                                if (searchQuery.isNotEmpty()) {
                                    IconButton(onClick = { searchQuery = "" }) {
                                        Icon(Icons.Default.Clear, contentDescription = "Clear")
                                    }
                                }
                            },
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp)
                        )
                    }

                    // Existing Patients list
                    val filteredPatients = state.patients.filter {
                        it.nama.contains(searchQuery, ignoreCase = true) || it.nik.contains(searchQuery)
                    }

                    if (filteredPatients.isNotEmpty()) {
                        item {
                            Text(
                                text = "Database Pasien Terdaftar (${filteredPatients.size})",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0F172A)
                            )
                        }

                        items(filteredPatients) { patient ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(14.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(patient.nama, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                            Spacer(modifier = Modifier.width(6.dp))
                                            val isBpjs = patient.noBpjs.isNotEmpty() && patient.statusBpjsAktif
                                            StatusBadge(
                                                text = if (isBpjs) "BPJS AKTIF" else "UMUM",
                                                bg = if (isBpjs) StatusDoneGreenBg else Color(0xFFF1F5F9),
                                                textColor = if (isBpjs) StatusDoneGreenText else Color(0xFF475569)
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text("NIK: ${patient.nik} • ${patient.jenisKelamin} • HP: ${patient.noHp}", fontSize = 11.sp, color = Color.Gray)
                                        if (patient.riwayatAlergi != "Tidak ada riwayat alergi") {
                                            Text("⚠️ Alergi: ${patient.riwayatAlergi}", fontSize = 11.sp, color = StatusAlertRed, fontWeight = FontWeight.SemiBold)
                                        }
                                    }

                                    Button(
                                        onClick = { selectedPatientForVisit = patient },
                                        colors = ButtonDefaults.buttonColors(containerColor = ClinicalTealPrimary),
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                                    ) {
                                        Icon(Icons.Default.ConfirmationNumber, contentDescription = null, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Buat Kunjungan", fontSize = 11.sp)
                                    }
                                }
                            }
                        }
                    }

                    item {
                        Text(
                            text = "Antrean Kunjungan Aktif (${state.queue.size} Pasien)",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0F172A)
                        )
                    }

                    items(state.queue.reversed()) { item ->
                        QueueCardAdmin(item = item, onCheckoutClick = { checkoutQueueItem = item })
                    }
                }
            }

            1 -> {
                // TAB 1: Status Pasien & Tracking (Buku Tamu Harian & Visual Stepper)
                val activeVisits = state.queue.filter { it.status != QueueStatus.BATAL }

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
                                    text = "Monitoring Lokasi & Status Pasien (HIS Tracking)",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = OnClinicalTealContainer
                                )
                                Text(
                                    text = "Memantau posisi tahapan pasien secara real-time di seluruh unit layanan.",
                                    fontSize = 12.sp,
                                    color = Color(0xFF004D40)
                                )
                            }
                        }
                    }

                    item {
                        Text(
                            text = "Daftar Pasien Sedang Berjalan (${activeVisits.size} Pasien)",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0F172A)
                        )
                    }

                    items(activeVisits) { item ->
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
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = item.queueNumber,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 16.sp,
                                            color = ClinicalTealPrimary
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = item.patient.nama,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp
                                        )
                                    }
                                    StatusBadge(
                                        text = item.status.label,
                                        bg = item.status.badgeBg,
                                        textColor = item.status.badgeText
                                    )
                                }

                                Spacer(modifier = Modifier.height(10.dp))
                                PatientProgressStepper(currentStatus = item.status)
                            }
                        }
                    }
                }
            }

            2 -> {
                // TAB 2: Kasir & Pembayaran (Checkout)
                val waitingPayment = state.queue.filter { it.status == QueueStatus.MENUNGGU_PEMBAYARAN }
                val completedToday = state.queue.filter { it.status == QueueStatus.SELESAI }

                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    item {
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            StatCard(
                                title = "Perlu Checkout",
                                value = "${waitingPayment.size}",
                                subtitle = "Pasien selesai periksa",
                                icon = Icons.Default.ReceiptLong,
                                iconTint = StatusWaitYellow,
                                iconBg = StatusWaitYellowBg,
                                modifier = Modifier.weight(1f)
                            )
                            StatCard(
                                title = "Kunjungan Selesai",
                                value = "${completedToday.size}",
                                subtitle = "Hari ini",
                                icon = Icons.Default.CheckCircle,
                                iconTint = StatusDoneGreen,
                                iconBg = StatusDoneGreenBg,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    item {
                        Text(
                            text = "Menunggu Pembayaran / Klaim BPJS",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0F172A)
                        )
                    }

                    if (waitingPayment.isEmpty()) {
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Box(modifier = Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                                    Text("Tidak ada pasien menunggu kasir saat ini.", color = Color.Gray)
                                }
                            }
                        }
                    } else {
                        items(waitingPayment) { item ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = item.queueNumber,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 16.sp,
                                                color = ClinicalTealPrimary
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = item.patient.nama,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 15.sp
                                            )
                                        }
                                        StatusBadge(
                                            text = item.payerType.displayName,
                                            bg = if (item.payerType.isBpjs) StatusDoneGreenBg else StatusInfoBlueBg,
                                            textColor = if (item.payerType.isBpjs) StatusDoneGreenText else StatusInfoBlueText
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))
                                    HorizontalDivider()
                                    Spacer(modifier = Modifier.height(10.dp))

                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text("Jasa Konsultasi Dokter:", fontSize = 12.sp, color = Color.Gray)
                                        Text(currencyFormat.format(item.consultationTariff), fontSize = 12.sp)
                                    }
                                    if (item.drugBill > 0) {
                                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                            Text("Biaya Obat & Farmasi:", fontSize = 12.sp, color = Color.Gray)
                                            Text(currencyFormat.format(item.drugBill), fontSize = 12.sp)
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text("Total Tagihan:", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                        Text(
                                            currencyFormat.format(item.totalBill),
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp,
                                            color = ClinicalTealPrimary
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(12.dp))

                                    Button(
                                        onClick = { checkoutQueueItem = item },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = if (item.payerType.isBpjs) StatusDoneGreen else ClinicalTealPrimary
                                        ),
                                        modifier = Modifier.fillMaxWidth().testTag("btn_process_checkout_${item.queueNumber}")
                                    ) {
                                        Icon(Icons.Default.Payment, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(if (item.payerType.isBpjs) "Verifikasi Klaim BPJS & Cetak Bukti" else "Terima Pembayaran & Cetak Kuitansi")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Modal: Registrasi Pasien Baru dengan Simulasi Status BPJS
    if (showNewPatientDialog) {
        NewPatientRegistrationDialog(
            onDismiss = { showNewPatientDialog = false },
            onSave = { nama, nik, tgl, jk, alamat, hp, alergi, payer, noBpjs, bpjsActive, tariff ->
                repository.registerNewPatientAndQueue(
                    nama, nik, tgl, jk, alamat, hp, alergi, payer, noBpjs, bpjsActive, tariff
                )
                showNewPatientDialog = false
            }
        )
    }

    // Modal: Buat Kunjungan Pasien Terdaftar
    selectedPatientForVisit?.let { patient ->
        CreateVisitExistingPatientDialog(
            patient = patient,
            onDismiss = { selectedPatientForVisit = null },
            onCreateVisit = { payerType, tariff ->
                repository.createVisitForPatient(patient, payerType, tariff)
                selectedPatientForVisit = null
            }
        )
    }

    // Modal: Checkout Kasir
    checkoutQueueItem?.let { item ->
        CheckoutReceiptDialog(
            queueItem = item,
            currencyFormat = currencyFormat,
            onDismiss = { checkoutQueueItem = null },
            onCompleteCheckout = {
                repository.checkoutPayment(item.id)
                checkoutQueueItem = null
            }
        )
    }
}

@Composable
fun QueueCardAdmin(
    item: QueueItem,
    onCheckoutClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(10.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(ClinicalTealContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = item.queueNumber,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = ClinicalTealPrimary
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = item.patient.nama,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        StatusBadge(
                            text = item.payerType.name,
                            bg = if (item.payerType.isBpjs) StatusDoneGreenBg else Color(0xFFF1F5F9),
                            textColor = if (item.payerType.isBpjs) StatusDoneGreenText else Color(0xFF475569)
                        )
                    }
                    Text(
                        text = "Daftar: ${item.registeredAt} • NIK: ${item.patient.nik.take(10)}...",
                        fontSize = 11.sp,
                        color = Color.Gray
                    )
                }

                StatusBadge(
                    text = item.status.label,
                    bg = item.status.badgeBg,
                    textColor = item.status.badgeText
                )
            }

            Spacer(modifier = Modifier.height(10.dp))
            PatientProgressStepper(currentStatus = item.status)

            if (item.status == QueueStatus.MENUNGGU_PEMBAYARAN) {
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = onCheckoutClick,
                    colors = ButtonDefaults.buttonColors(containerColor = StatusWaitYellow),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Proses Checkout Kasir", fontSize = 11.sp, color = Color.White)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewPatientRegistrationDialog(
    onDismiss: () -> Unit,
    onSave: (
        nama: String,
        nik: String,
        tanggalLahir: String,
        jenisKelamin: String,
        alamat: String,
        noHp: String,
        riwayatAlergi: String,
        payerType: PayerType,
        noBpjs: String,
        bpjsActive: Boolean,
        tariff: Long
    ) -> Unit
) {
    var nama by remember { mutableStateOf("") }
    var nik by remember { mutableStateOf("") }
    var tanggalLahir by remember { mutableStateOf("12/04/1992") }
    var jenisKelamin by remember { mutableStateOf("Laki-laki") }
    var alamat by remember { mutableStateOf("") }
    var noHp by remember { mutableStateOf("") }
    var riwayatAlergi by remember { mutableStateOf("") }
    var payerType by remember { mutableStateOf(PayerType.BPJS) }
    var noBpjs by remember { mutableStateOf("000" + (10000000..99999999).random()) }
    var isBpjsActive by remember { mutableStateOf(true) }
    var consultationTariff by remember { mutableStateOf("50000") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.PersonAdd, contentDescription = null, tint = ClinicalTealPrimary)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Registrasi Pasien Baru")
            }
        },
        text = {
            LazyColumn(
                modifier = Modifier.fillMaxWidth().heightIn(max = 480.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item {
                    OutlinedTextField(
                        value = nama,
                        onValueChange = { nama = it },
                        label = { Text("Nama Lengkap Pasien *") },
                        modifier = Modifier.fillMaxWidth().testTag("input_patient_name")
                    )
                }
                item {
                    OutlinedTextField(
                        value = nik,
                        onValueChange = { if (it.length <= 16) nik = it },
                        label = { Text("Nomor Induk Kependudukan (NIK) *") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth().testTag("input_patient_nik")
                    )
                }
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = tanggalLahir,
                            onValueChange = { tanggalLahir = it },
                            label = { Text("Tgl Lahir (DD/MM/YYYY)") },
                            modifier = Modifier.weight(1f)
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Jenis Kelamin", fontSize = 12.sp, color = Color.Gray)
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                FilterChip(
                                    selected = jenisKelamin == "Laki-laki",
                                    onClick = { jenisKelamin = "Laki-laki" },
                                    label = { Text("L") }
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                FilterChip(
                                    selected = jenisKelamin == "Perempuan",
                                    onClick = { jenisKelamin = "Perempuan" },
                                    label = { Text("P") }
                                )
                            }
                        }
                    }
                }
                item {
                    OutlinedTextField(
                        value = alamat,
                        onValueChange = { alamat = it },
                        label = { Text("Alamat Tempat Tinggal") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item {
                    OutlinedTextField(
                        value = noHp,
                        onValueChange = { noHp = it },
                        label = { Text("Nomor Handphone / WhatsApp") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item {
                    OutlinedTextField(
                        value = riwayatAlergi,
                        onValueChange = { riwayatAlergi = it },
                        label = { Text("Riwayat Alergi Obat / Makanan") },
                        placeholder = { Text("cth: Alergi Amoksisilin, Seafood") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // Payment Path Logic Section with BPJS simulation
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = ClinicalSurfaceVariant),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("Jalur Pembiayaan & JKN", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                FilterChip(
                                    selected = payerType == PayerType.BPJS,
                                    onClick = {
                                        payerType = PayerType.BPJS
                                        consultationTariff = "50000"
                                    },
                                    label = { Text("BPJS Kesehatan") }
                                )
                                FilterChip(
                                    selected = payerType == PayerType.UMUM,
                                    onClick = {
                                        payerType = PayerType.UMUM
                                        consultationTariff = "50000"
                                    },
                                    label = { Text("Pasien Umum") }
                                )
                            }

                            if (payerType == PayerType.BPJS) {
                                Spacer(modifier = Modifier.height(8.dp))
                                OutlinedTextField(
                                    value = noBpjs,
                                    onValueChange = { noBpjs = it },
                                    label = { Text("Nomor Kartu BPJS (13 Digit)") },
                                    modifier = Modifier.fillMaxWidth()
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Simulasi Status JKN:", fontSize = 12.sp)
                                    Switch(
                                        checked = isBpjsActive,
                                        onCheckedChange = { isBpjsActive = it }
                                    )
                                    Text(
                                        text = if (isBpjsActive) "AKTIF" else "NON-AKTIF",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = if (isBpjsActive) StatusDoneGreenText else StatusAlertRed
                                    )
                                }
                            } else {
                                Spacer(modifier = Modifier.height(8.dp))
                                OutlinedTextField(
                                    value = consultationTariff,
                                    onValueChange = { consultationTariff = it },
                                    label = { Text("Tarif Konsultasi Dokter (Rp)") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (nama.isNotBlank() && nik.isNotBlank()) {
                        onSave(
                            nama,
                            nik,
                            tanggalLahir,
                            jenisKelamin,
                            alamat.ifBlank { "Jakarta" },
                            noHp.ifBlank { "0812345678" },
                            riwayatAlergi,
                            payerType,
                            noBpjs,
                            isBpjsActive,
                            consultationTariff.toLongOrNull() ?: 50000L
                        )
                    }
                },
                enabled = nama.isNotBlank() && nik.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = ClinicalTealPrimary),
                modifier = Modifier.testTag("btn_save_new_patient")
            ) {
                Text("Cetak Antrean & Simpan")
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
fun CreateVisitExistingPatientDialog(
    patient: Patient,
    onDismiss: () -> Unit,
    onCreateVisit: (PayerType, Long) -> Unit
) {
    var payerType by remember { mutableStateOf(if (patient.noBpjs.isNotBlank()) PayerType.BPJS else PayerType.UMUM) }
    var tariff by remember { mutableStateOf("50000") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Buat Antrean Kunjungan") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Pasien: ${patient.nama}", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                Text("NIK: ${patient.nik} • ${patient.jenisKelamin}", fontSize = 12.sp, color = Color.Gray)

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = payerType == PayerType.BPJS,
                        onClick = { payerType = PayerType.BPJS },
                        label = { Text("BPJS Kesehatan") }
                    )
                    FilterChip(
                        selected = payerType == PayerType.UMUM,
                        onClick = { payerType = PayerType.UMUM },
                        label = { Text("Umum") }
                    )
                }

                if (payerType == PayerType.BPJS) {
                    Text("Nomor Kartu: ${patient.noBpjs.ifBlank { "000123456789" }}", fontSize = 12.sp)
                    Text("✅ Status Kepesertaan JKN Terverifikasi Aktif", fontSize = 11.sp, color = StatusDoneGreenText)
                } else {
                    OutlinedTextField(
                        value = tariff,
                        onValueChange = { tariff = it },
                        label = { Text("Tarif Konsultasi (Rp)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onCreateVisit(payerType, tariff.toLongOrNull() ?: 50000L) },
                colors = ButtonDefaults.buttonColors(containerColor = ClinicalTealPrimary)
            ) {
                Text("Buat Nomor Antrean")
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
fun CheckoutReceiptDialog(
    queueItem: QueueItem,
    currencyFormat: NumberFormat,
    onDismiss: () -> Unit,
    onCompleteCheckout: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Receipt, contentDescription = null, tint = ClinicalTealPrimary)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Kuitansi & Rincian Pembayaran")
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = ClinicalSurfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("KLINIK PRATAMA ROMANA", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text("No. Antrean: ${queueItem.queueNumber} • Tgl: ${queueItem.registeredAt}", fontSize = 11.sp, color = Color.Gray)
                        Text("Pasien: ${queueItem.patient.nama} (${queueItem.patient.nik})", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        Text("Penjamin: ${queueItem.payerType.displayName}", fontSize = 11.sp, color = ClinicalTealPrimary)
                    }
                }

                HorizontalDivider()

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Jasa Pelayanan Dokter", fontSize = 12.sp)
                    Text(currencyFormat.format(queueItem.consultationTariff), fontSize = 12.sp)
                }

                if (queueItem.clinicalRecord?.prescriptionItems?.isNotEmpty() == true) {
                    Text("Rincian Obat & Farmasi:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    queueItem.clinicalRecord.prescriptionItems.forEach { rx ->
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(" • ${rx.medicineName} (${rx.quantity}x)", fontSize = 11.sp, color = Color.Gray)
                        }
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Subtotal Farmasi", fontSize = 12.sp)
                        Text(currencyFormat.format(queueItem.drugBill), fontSize = 12.sp)
                    }
                }

                HorizontalDivider()

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Total Tagihan", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text(
                        currencyFormat.format(queueItem.totalBill),
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = ClinicalTealPrimary
                    )
                }

                if (queueItem.payerType.isBpjs) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = StatusDoneGreenBg)
                    ) {
                        Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Verified, contentDescription = null, tint = StatusDoneGreen, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                "Ditanggung Penuh BPJS Kesehatan (Iur Bayar: Rp 0)",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = StatusDoneGreenText
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onCompleteCheckout,
                colors = ButtonDefaults.buttonColors(containerColor = ClinicalTealPrimary),
                modifier = Modifier.testTag("btn_complete_checkout")
            ) {
                Text(if (queueItem.payerType.isBpjs) "Kirim Klaim & Selesaikan" else "Terima Uang & Selesai")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Tutup")
            }
        }
    )
}
