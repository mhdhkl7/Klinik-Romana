package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import com.example.ui.components.PatientProgressStepper
import com.example.ui.components.StatusBadge
import com.example.ui.theme.*

@Composable
fun DoctorConsultationScreen(
    state: ClinicState,
    repository: ClinicRepository
) {
    val selectedTab = state.selectedTab
    var activeVisitForExam by remember { mutableStateOf<QueueItem?>(null) }

    val doctorQueue = state.queue.filter {
        it.status == QueueStatus.SIAP_DIPERIKSA_DOKTER || it.status == QueueStatus.SEDANG_DIPERIKSA_DOKTER
    }
    val emrCompletedVisits = state.queue.filter { it.clinicalRecord != null }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ClinicBgLight)
    ) {
        when (selectedTab) {
            0 -> {
                // TAB 0: Antrean Pasien Dokter (Daftar Tunggu & Notifikasi Substitusi)
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
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
                                    Icon(Icons.Default.MedicalServices, contentDescription = null, tint = Color.White, modifier = Modifier.size(24.dp))
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Ruang Konsultasi Dokter Spesialis",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        color = OnClinicalTealContainer
                                    )
                                    Text(
                                        text = "dr. Richard Pangihutan, Sp.PD • ${doctorQueue.size} pasien menunggu",
                                        fontSize = 12.sp,
                                        color = Color(0xFF004D40)
                                    )
                                }
                            }
                        }
                    }

                    item {
                        Text(
                            text = "Daftar Pasien Siap Diperiksa (${doctorQueue.size})",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0F172A)
                        )
                    }

                    if (doctorQueue.isEmpty()) {
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Box(modifier = Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                                    Text("Tidak ada pasien di ruang tunggu dokter.", color = Color.Gray)
                                }
                            }
                        }
                    } else {
                        items(doctorQueue) { item ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    // Emergency banner if Pharmacy requested drug substitution!
                                    if (item.hasSubstitutionAlert) {
                                        Card(
                                            modifier = Modifier.fillMaxWidth(),
                                            colors = CardDefaults.cardColors(containerColor = StatusAlertRedBg)
                                        ) {
                                            Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                                                Icon(Icons.Default.NotificationImportant, contentDescription = null, tint = StatusAlertRed)
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text(
                                                    text = "MINTA SUBSTITUSI OBAT DARI FARMASI: \"${item.substitutionNote}\"",
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = StatusAlertRedText
                                                )
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(8.dp))
                                    }

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Box(
                                                modifier = Modifier
                                                    .size(46.dp)
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .background(ClinicalTealContainer),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = item.queueNumber,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 16.sp,
                                                    color = ClinicalTealPrimary
                                                )
                                            }
                                            Spacer(modifier = Modifier.width(10.dp))
                                            Column {
                                                Text(item.patient.nama, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                                Text("NIK: ${item.patient.nik} • ${item.patient.jenisKelamin}", fontSize = 11.sp, color = Color.Gray)
                                            }
                                        }

                                        StatusBadge(
                                            text = item.payerType.displayName,
                                            bg = if (item.payerType.isBpjs) StatusDoneGreenBg else StatusInfoBlueBg,
                                            textColor = if (item.payerType.isBpjs) StatusDoneGreenText else StatusInfoBlueText
                                        )
                                    }

                                    if (item.triageVitals != null) {
                                        val v = item.triageVitals
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(
                                            "TTV: TD ${v.systolic}/${v.diastolic} mmHg (${v.bloodPressureCategory}) • HR ${v.heartRate} bpm • Suhu ${v.bodyTemp}°C",
                                            fontSize = 11.sp,
                                            color = Color(0xFF334155)
                                        )
                                        Text("Keluhan: \"${v.chiefComplaints}\"", fontSize = 11.sp, color = Color.Gray)
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))
                                    PatientProgressStepper(currentStatus = item.status)
                                    Spacer(modifier = Modifier.height(10.dp))

                                    Button(
                                        onClick = {
                                            activeVisitForExam = item
                                            repository.setSelectedTab(1) // Open Single Consultation Workspace
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = ClinicalTealPrimary),
                                        modifier = Modifier.fillMaxWidth().testTag("btn_start_consultation_${item.queueNumber}")
                                    ) {
                                        Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Mulai Konsultasi & E-Resep")
                                    }
                                }
                            }
                        }
                    }
                }
            }

            1 -> {
                // TAB 1: Konsultasi & E-Resep (SINGLE CONSULTATION WORKSPACE WRAPPED IN SingleChildScrollView)
                val targetVisit = activeVisitForExam ?: state.queue.firstOrNull { it.status == QueueStatus.SIAP_DIPERIKSA_DOKTER } ?: state.queue.firstOrNull()

                if (targetVisit == null) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Tidak ada pasien siap untuk konsultasi dokter.", color = Color.Gray)
                    }
                } else {
                    DoctorSingleWorkspace(
                        queueItem = targetVisit,
                        medicines = state.medicines,
                        icd10List = state.icd10List,
                        onSave = { clinicalRecord ->
                            repository.submitConsultation(targetVisit.id, clinicalRecord)
                            repository.setSelectedTab(0)
                        },
                        onChangePatient = { repository.setSelectedTab(0) }
                    )
                }
            }

            2 -> {
                // TAB 2: Riwayat EMR Pasien
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    item {
                        Text(
                            text = "Rekam Medis (EMR) Pasien Klinik (${emrCompletedVisits.size})",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0F172A)
                        )
                    }

                    if (emrCompletedVisits.isEmpty()) {
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Box(modifier = Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                                    Text("Belum ada rekam medis tersimpan hari ini.", color = Color.Gray)
                                }
                            }
                        }
                    } else {
                        items(emrCompletedVisits) { item ->
                            val r = item.clinicalRecord!!
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
                                        Text("${item.queueNumber} - ${item.patient.nama}", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                        Text(r.icd10Code, fontWeight = FontWeight.Bold, color = ClinicalTealPrimary)
                                    }
                                    Text("Diagnosa: ${r.icd10Name}", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    Text("Anamnesis: ${r.anamnesis}", fontSize = 11.sp, color = Color.Gray)
                                    Text("Terapi: ${r.therapyPlan}", fontSize = 11.sp)
                                    if (r.prescriptionItems.isNotEmpty()) {
                                        Text("Resep Obat: ${r.prescriptionItems.joinToString { it.medicineName }}", fontSize = 11.sp, color = Color(0xFF00796B), fontWeight = FontWeight.SemiBold)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DoctorSingleWorkspace(
    queueItem: QueueItem,
    medicines: List<MedicineItem>,
    icd10List: List<Icd10Entry>,
    onSave: (ClinicalRecord) -> Unit,
    onChangePatient: () -> Unit
) {
    val scrollState = rememberScrollState()

    var anamnesis by remember {
        mutableStateOf(
            if (queueItem.triageVitals != null) "Keluhan: ${queueItem.triageVitals.chiefComplaints}. Tidak ada riwayat trauma." else ""
        )
    }
    var physicalExam by remember {
        mutableStateOf(
            if (queueItem.triageVitals != null) "Compos Mentis. TD ${queueItem.triageVitals.systolic}/${queueItem.triageVitals.diastolic} mmHg. Paru vesikuler, Jantung normal." else ""
        )
    }
    var selectedIcd10 by remember { mutableStateOf(icd10List.firstOrNull() ?: Icd10Entry("I10", "Hypertension", "Hipertensi")) }
    var icd10Expanded by remember { mutableStateOf(false) }
    var therapyPlan by remember { mutableStateOf("Diet rendah garam, istirahat cukup, hidrasi 2L air/hari.") }
    var needsPrescription by remember { mutableStateOf(true) }

    var prescriptionItems by remember {
        mutableStateOf(
            listOf(
                PrescriptionItem(
                    medicineId = medicines.firstOrNull()?.id ?: "MED-01",
                    medicineName = medicines.firstOrNull()?.name ?: "Paracetamol 500mg",
                    dosage = "500mg",
                    frequency = "3 x 1 tablet",
                    quantity = 10,
                    aturanPakai = "Sesudah makan bila sakit"
                )
            )
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Substitution alert if present
        if (queueItem.hasSubstitutionAlert) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = StatusAlertRedBg)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.NotificationImportant, contentDescription = null, tint = StatusAlertRed)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("PERMINTAAN SUBSTITUSI OBAT DARI APOTEKER", fontWeight = FontWeight.Bold, color = StatusAlertRedText, fontSize = 13.sp)
                    }
                    Text("Catatan: \"${queueItem.substitutionNote}\"", fontSize = 11.sp, color = StatusAlertRedText)
                }
            }
        }

        // Patient Header Box
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = ClinicalTealContainer)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Pasien: ${queueItem.queueNumber} - ${queueItem.patient.nama}", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    Text("Alergi: ${queueItem.patient.riwayatAlergi}", fontSize = 11.sp, color = StatusAlertRedText, fontWeight = FontWeight.Bold)
                    if (queueItem.triageVitals != null) {
                        Text(
                            "TTV: TD ${queueItem.triageVitals.systolic}/${queueItem.triageVitals.diastolic} | Suhu ${queueItem.triageVitals.bodyTemp}°C | Keluhan: ${queueItem.triageVitals.chiefComplaints}",
                            fontSize = 11.sp,
                            color = Color(0xFF004D40)
                        )
                    }
                }
                OutlinedButton(onClick = onChangePatient, contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)) {
                    Text("Ganti Pasien", fontSize = 10.sp)
                }
            }
        }

        // 1. Anamnesis
        Text("1. Anamnesis / Temuan Klinis *", fontSize = 12.sp, fontWeight = FontWeight.Bold)
        OutlinedTextField(
            value = anamnesis,
            onValueChange = { anamnesis = it },
            minLines = 2,
            modifier = Modifier.fillMaxWidth()
        )

        // 2. Pemeriksaan Fisik
        Text("2. Pemeriksaan Fisik *", fontSize = 12.sp, fontWeight = FontWeight.Bold)
        OutlinedTextField(
            value = physicalExam,
            onValueChange = { physicalExam = it },
            minLines = 2,
            modifier = Modifier.fillMaxWidth()
        )

        // 3. ICD-10
        Text("3. Diagnosis ICD-10 *", fontSize = 12.sp, fontWeight = FontWeight.Bold)
        Box {
            OutlinedTextField(
                value = "${selectedIcd10.code} - ${selectedIcd10.commonTerm}",
                onValueChange = {},
                readOnly = true,
                trailingIcon = {
                    IconButton(onClick = { icd10Expanded = true }) {
                        Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                    }
                },
                modifier = Modifier.fillMaxWidth().clickable { icd10Expanded = true }
            )
            DropdownMenu(expanded = icd10Expanded, onDismissRequest = { icd10Expanded = false }) {
                icd10List.forEach { entry ->
                    DropdownMenuItem(
                        text = { Text("${entry.code} - ${entry.commonTerm} (${entry.description})") },
                        onClick = {
                            selectedIcd10 = entry
                            icd10Expanded = false
                        }
                    )
                }
            }
        }

        // 4. Therapy
        Text("4. Rencana Terapi & Edukasi", fontSize = 12.sp, fontWeight = FontWeight.Bold)
        OutlinedTextField(
            value = therapyPlan,
            onValueChange = { therapyPlan = it },
            modifier = Modifier.fillMaxWidth()
        )

        // 5. Integrated E-Prescription Builder
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = ClinicalSurfaceVariant)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Perlu Resep Obat Farmasi?", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Switch(checked = needsPrescription, onCheckedChange = { needsPrescription = it })
                }

                if (needsPrescription) {
                    HorizontalDivider()
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Daftar Obat yang Diresepkan:", fontSize = 12.sp, fontWeight = FontWeight.Bold)

                    prescriptionItems.forEach { rx ->
                        val med = medicines.find { it.id == rx.medicineId }
                        Card(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(rx.medicineName, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    Text("${rx.dosage} • Aturan: ${rx.frequency} (${rx.aturanPakai}) • Jml: ${rx.quantity}", fontSize = 11.sp, color = Color.Gray)
                                }
                                Text("Stok: ${med?.currentStock ?: 0}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if ((med?.currentStock ?: 0) < rx.quantity) StatusAlertRed else StatusDoneGreenText)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Button(
                        onClick = {
                            val next = medicines.firstOrNull { m -> prescriptionItems.none { it.medicineId == m.id } } ?: medicines.first()
                            prescriptionItems = prescriptionItems + PrescriptionItem(
                                medicineId = next.id,
                                medicineName = next.name,
                                dosage = "10mg",
                                frequency = "1x1",
                                quantity = 10,
                                aturanPakai = "Pagi hari"
                            )
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = ClinicalTealPrimary),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Tambah Obat Lain", fontSize = 11.sp)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
        Button(
            onClick = {
                val record = ClinicalRecord(
                    anamnesis = anamnesis,
                    physicalExam = physicalExam,
                    icd10Code = selectedIcd10.code,
                    icd10Name = selectedIcd10.commonTerm,
                    therapyPlan = therapyPlan,
                    needsPrescription = needsPrescription,
                    prescriptionItems = if (needsPrescription) prescriptionItems else emptyList(),
                    doctorNotes = "Kontrol berkala bila ada keluhan."
                )
                onSave(record)
            },
            colors = ButtonDefaults.buttonColors(containerColor = ClinicalTealPrimary),
            modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp)
        ) {
            Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Simpan Rekam Medis & Kirim Resep", fontSize = 14.sp)
        }
    }
}
