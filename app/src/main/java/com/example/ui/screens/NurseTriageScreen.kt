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
import com.example.model.QueueItem
import com.example.model.QueueStatus
import com.example.model.TriageVitals
import com.example.ui.components.PatientProgressStepper
import com.example.ui.components.StatusBadge
import com.example.ui.theme.*

@Composable
fun NurseTriageScreen(
    state: ClinicState,
    repository: ClinicRepository
) {
    val selectedTab = state.selectedTab
    var activeVisitForTtv by remember { mutableStateOf<QueueItem?>(null) }

    // Tab 0: Queue items awaiting Triage or Skipped
    val triageWaitingPatients = state.queue.filter {
        it.status == QueueStatus.MENUNGGU_TRIAGE || it.status == QueueStatus.SEDANG_TRIAGE || it.status == QueueStatus.DILEWATI
    }
    val evaluatedPatients = state.queue.filter { it.triageVitals != null }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ClinicBgLight)
    ) {
        when (selectedTab) {
            0 -> {
                // TAB 0: Antrean Triage (Panggil / Skip / Batal)
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
                                    Icon(Icons.Default.HealthAndSafety, contentDescription = null, tint = Color.White, modifier = Modifier.size(24.dp))
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Stasiun Triage & Skrining Pasien",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        color = OnClinicalTealContainer
                                    )
                                    Text(
                                        text = "Perawat: Rehan Pratama • ${triageWaitingPatients.size} pasien menunggu pemeriksaan",
                                        fontSize = 12.sp,
                                        color = Color(0xFF004D40)
                                    )
                                }
                            }
                        }
                    }

                    item {
                        Text(
                            text = "Daftar Antrean Triage (${triageWaitingPatients.size})",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0F172A)
                        )
                    }

                    if (triageWaitingPatients.isEmpty()) {
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Box(modifier = Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                                    Text("Tidak ada pasien menunggu di ruang tunggu triage.", color = Color.Gray)
                                }
                            }
                        }
                    } else {
                        items(triageWaitingPatients) { item ->
                            val isSkipped = item.status == QueueStatus.DILEWATI
                            val isCalling = state.activeCallingQueueId == item.id

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
                                            text = item.status.label,
                                            bg = item.status.badgeBg,
                                            textColor = item.status.badgeText
                                        )
                                    }

                                    if (item.patient.riwayatAlergi != "Tidak ada riwayat alergi") {
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text("⚠️ Alergi: ${item.patient.riwayatAlergi}", fontSize = 11.sp, color = StatusAlertRed, fontWeight = FontWeight.SemiBold)
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))
                                    PatientProgressStepper(currentStatus = item.status)
                                    Spacer(modifier = Modifier.height(10.dp))

                                    // Action buttons: Panggil, Skip/Absent, Batal, Input TTV
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        OutlinedButton(
                                            onClick = {
                                                if (isCalling) repository.stopCallingPatient() else repository.callPatient(item.id)
                                            },
                                            modifier = Modifier.weight(1f),
                                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp)
                                        ) {
                                            Icon(if (isCalling) Icons.Default.VolumeOff else Icons.Default.VolumeUp, contentDescription = null, modifier = Modifier.size(14.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(if (isCalling) "Stop" else "Panggil", fontSize = 11.sp)
                                        }

                                        OutlinedButton(
                                            onClick = {
                                                if (isSkipped) repository.recallPatient(item.id) else repository.skipPatient(item.id)
                                            },
                                            modifier = Modifier.weight(1f),
                                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp)
                                        ) {
                                            Icon(Icons.Default.SkipNext, contentDescription = null, modifier = Modifier.size(14.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(if (isSkipped) "Recall" else "Lewati", fontSize = 11.sp)
                                        }

                                        Button(
                                            onClick = {
                                                activeVisitForTtv = item
                                                repository.setSelectedTab(1) // Go to Tab 1 Form Input TTV
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = ClinicalTealPrimary),
                                            modifier = Modifier.weight(1.3f),
                                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp)
                                        ) {
                                            Icon(Icons.Default.EditNote, contentDescription = null, modifier = Modifier.size(15.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Input TTV", fontSize = 11.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            1 -> {
                // TAB 1: Form Input TTV Pasien
                val targetVisit = activeVisitForTtv ?: state.queue.firstOrNull { it.status == QueueStatus.MENUNGGU_TRIAGE } ?: state.queue.firstOrNull()

                if (targetVisit == null) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Tidak ada antrean pasien untuk pemeriksaan TTV.", color = Color.Gray)
                    }
                } else {
                    TtvInputFormBody(
                        queueItem = targetVisit,
                        onSave = { vitals ->
                            repository.submitTriageVitals(targetVisit.id, vitals)
                            repository.setSelectedTab(0)
                        },
                        onChangePatient = { repository.setSelectedTab(0) }
                    )
                }
            }

            2 -> {
                // TAB 2: Rekap Vital Signs Pasien
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    item {
                        Text(
                            text = "Rekapitulasi Tanda Vital Pasien (${evaluatedPatients.size} Pasien)",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0F172A)
                        )
                    }

                    if (evaluatedPatients.isEmpty()) {
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Box(modifier = Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                                    Text("Belum ada data pemeriksaan vital signs hari ini.", color = Color.Gray)
                                }
                            }
                        }
                    } else {
                        items(evaluatedPatients) { item ->
                            val v = item.triageVitals!!
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text("${item.queueNumber} - ${item.patient.nama}", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                        StatusBadge(
                                            text = v.bloodPressureCategory,
                                            bg = if (v.bloodPressureCategory == "Normal") StatusDoneGreenBg else StatusAlertRedBg,
                                            textColor = if (v.bloodPressureCategory == "Normal") StatusDoneGreenText else StatusAlertRedText
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        "TD: ${v.systolic}/${v.diastolic} mmHg • HR: ${v.heartRate} bpm • Suhu: ${v.bodyTemp}°C • BB: ${v.weightKg} kg",
                                        fontSize = 12.sp,
                                        color = Color(0xFF334155)
                                    )
                                    Text("Keluhan Utama: \"${v.chiefComplaints}\"", fontSize = 11.sp, color = Color.Gray)
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
fun TtvInputFormBody(
    queueItem: QueueItem,
    onSave: (TriageVitals) -> Unit,
    onChangePatient: () -> Unit
) {
    var systolic by remember { mutableStateOf("120") }
    var diastolic by remember { mutableStateOf("80") }
    var heartRate by remember { mutableStateOf("76") }
    var bodyTemp by remember { mutableStateOf("36.6") }
    var respiratoryRate by remember { mutableStateOf("18") }
    var weightKg by remember { mutableStateOf("60.0") }
    var heightCm by remember { mutableStateOf("165.0") }
    var chiefComplaints by remember { mutableStateOf("") }

    val sys = systolic.toIntOrNull() ?: 120
    val dia = diastolic.toIntOrNull() ?: 80
    val bpCategory = when {
        sys < 90 || dia < 60 -> "Hipotensi"
        sys <= 120 && dia <= 80 -> "Normal"
        sys <= 139 || dia <= 89 -> "Prehipertensi"
        else -> "Hipertensi"
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
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
                        Text("Pasien: ${queueItem.queueNumber} - ${queueItem.patient.nama}", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text("Alergi: ${queueItem.patient.riwayatAlergi}", fontSize = 11.sp, color = StatusAlertRedText, fontWeight = FontWeight.Bold)
                    }
                    OutlinedButton(onClick = onChangePatient, contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)) {
                        Text("Ganti Pasien", fontSize = 10.sp)
                    }
                }
            }
        }

        item {
            Text("Tekanan Darah (mmHg) - Kategori: $bpCategory", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = systolic,
                    onValueChange = { systolic = it },
                    label = { Text("Sistolik") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f)
                )
                Text("/", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                OutlinedTextField(
                    value = diastolic,
                    onValueChange = { diastolic = it },
                    label = { Text("Diastolik") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = heartRate,
                    onValueChange = { heartRate = it },
                    label = { Text("Nadi (bpm)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = bodyTemp,
                    onValueChange = { bodyTemp = it },
                    label = { Text("Suhu (°C)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = respiratoryRate,
                    onValueChange = { respiratoryRate = it },
                    label = { Text("Nafas (x/menit)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = weightKg,
                    onValueChange = { weightKg = it },
                    label = { Text("Berat (kg)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            OutlinedTextField(
                value = chiefComplaints,
                onValueChange = { chiefComplaints = it },
                label = { Text("Keluhan Utama Pasien *") },
                placeholder = { Text("cth: Pusing, demam 2 hari, mual") },
                minLines = 2,
                modifier = Modifier.fillMaxWidth()
            )
        }

        item {
            Spacer(modifier = Modifier.height(6.dp))
            Button(
                onClick = {
                    val vitals = TriageVitals(
                        systolic = sys,
                        diastolic = dia,
                        heartRate = heartRate.toIntOrNull() ?: 76,
                        bodyTemp = bodyTemp.toFloatOrNull() ?: 36.6f,
                        respiratoryRate = respiratoryRate.toIntOrNull() ?: 18,
                        weightKg = weightKg.toFloatOrNull() ?: 60f,
                        heightCm = heightCm.toFloatOrNull() ?: 165f,
                        chiefComplaints = chiefComplaints.ifBlank { "Pemeriksaan rutin" }
                    )
                    onSave(vitals)
                },
                colors = ButtonDefaults.buttonColors(containerColor = ClinicalTealPrimary),
                modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp)
            ) {
                Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Simpan TTV & Teruskan ke Dokter")
            }
        }
    }
}
