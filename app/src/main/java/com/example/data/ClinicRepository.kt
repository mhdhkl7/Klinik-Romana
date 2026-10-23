package com.example.data

import com.example.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.SimpleDateFormat
import java.util.*

data class ClinicState(
    val currentRole: Role = Role.ADMIN_PENDAFTARAN,
    val selectedTab: Int = 0,
    val patients: List<Patient> = emptyList(),
    val queue: List<QueueItem> = emptyList(),
    val medicines: List<MedicineItem> = emptyList(),
    val stockMutations: List<StockMutation> = emptyList(),
    val opnameRecords: List<StockOpnameRecord> = emptyList(),
    val icd10List: List<Icd10Entry> = emptyList(),
    val staffTasks: List<StaffTaskItem> = emptyList(),
    val activeCallingQueueId: String? = null,
    val toastMessage: String? = null
)

class ClinicRepository private constructor() {

    private val _state = MutableStateFlow(ClinicState())
    val state: StateFlow<ClinicState> = _state.asStateFlow()

    init {
        loadInitialSeedData()
    }

    private fun getCurrentTimeString(): String {
        val sdf = SimpleDateFormat("HH:mm 'WIB'", Locale.getDefault())
        return sdf.format(Date())
    }

    private fun getCurrentDateString(): String {
        val sdf = SimpleDateFormat("dd MMM yyyy", Locale("id", "ID"))
        return sdf.format(Date())
    }

    fun loadInitialSeedData() {
        // Patients specified: Siti Rahma & Budi Santoso
        val initialPatients = listOf(
            Patient(
                id = "P-001",
                nik = "3171012304990001",
                nama = "Siti Rahma",
                tanggalLahir = "15/04/1996",
                jenisKelamin = "Perempuan",
                alamat = "Jl. Melati No. 12, Jakarta",
                noHp = "081234567890",
                riwayatAlergi = "Alergi Dingin, Penisilin",
                noBpjs = "000123456789",
                statusBpjsAktif = true
            ),
            Patient(
                id = "P-002",
                nik = "3171051108880002",
                nama = "Budi Santoso",
                tanggalLahir = "11/08/1988",
                jenisKelamin = "Laki-laki",
                alamat = "Jl. Mawar No. 45, Jakarta",
                noHp = "085711223344",
                riwayatAlergi = "Tidak ada riwayat alergi",
                noBpjs = "",
                statusBpjsAktif = false
            )
        )

        // Inventory specified: Paracetamol (12/20), Amoxicillin (80/30), Omeprazole (5/15), Amlodipine (50/10)
        val initialMedicines = listOf(
            MedicineItem(
                id = "MED-01",
                code = "PCT-01",
                name = "Paracetamol 500mg",
                category = "Analgesik & Antipiretik",
                batchNumber = "PCT-01",
                expiryDate = "14/12/2026",
                currentStock = 12, // Low stock alert active (12 <= 20)
                minStockAlert = 20,
                unit = "Tablet",
                unitPrice = 1500L
            ),
            MedicineItem(
                id = "MED-02",
                code = "AMX-02",
                name = "Amoxicillin 500mg",
                category = "Antibiotik",
                batchNumber = "AMX-02",
                expiryDate = "20/09/2025",
                currentStock = 80, // Safe (80 > 30)
                minStockAlert = 30,
                unit = "Tablet",
                unitPrice = 2500L
            ),
            MedicineItem(
                id = "MED-03",
                code = "OMP-01",
                name = "Omeprazole 20mg",
                category = "Saluran Cerna / PPI",
                batchNumber = "OMP-01",
                expiryDate = "05/03/2026",
                currentStock = 5, // Low stock alert active (5 <= 15)
                minStockAlert = 15,
                unit = "Kapsul",
                unitPrice = 3500L
            ),
            MedicineItem(
                id = "MED-04",
                code = "AML-03",
                name = "Amlodipine 10mg",
                category = "Antihipertensi",
                batchNumber = "AML-03",
                expiryDate = "10/06/2027",
                currentStock = 50, // Safe (50 > 10)
                minStockAlert = 10,
                unit = "Tablet",
                unitPrice = 3000L
            )
        )

        val initialIcd10 = listOf(
            Icd10Entry("I10", "Essential (primary) hypertension", "Hipertensi Primer"),
            Icd10Entry("E11", "Type 2 diabetes mellitus", "Diabetes Melitus Tipe 2"),
            Icd10Entry("J00", "Acute nasopharyngitis [common cold]", "Common Cold / Flu"),
            Icd10Entry("K29.7", "Gastritis, unspecified", "Gastritis Akut"),
            Icd10Entry("R50.9", "Fever, unspecified", "Febris / Demam")
        )

        // Initial Visit: Siti Rahma pre-enrolled in A-001 (Triage)
        val initialQueue = listOf(
            QueueItem(
                id = "V-001",
                queueNumber = "A-001",
                patient = initialPatients[0], // Siti Rahma
                payerType = PayerType.BPJS,
                status = QueueStatus.MENUNGGU_TRIAGE,
                consultationTariff = 50000L,
                totalBill = 50000L,
                paymentStatus = "Klaim BPJS Terverifikasi",
                registeredAt = "08:00 WIB"
            )
        )

        val initialOpname = listOf(
            StockOpnameRecord(
                id = "OPN-001",
                date = getCurrentDateString(),
                medicineId = "MED-01",
                medicineName = "Paracetamol 500mg",
                systemStock = 15,
                physicalCount = 12,
                variance = -3,
                reason = "Kemasan strip rusak dan remuk di rak penyimpanan",
                submittedBy = "Muhammad Haikal Siregar, S.Farm",
                status = OpnameStatus.MENUNGGU_APPROVAL
            )
        )

        val initialTasks = listOf(
            StaffTaskItem(
                id = "TSK-001",
                createdAt = getCurrentTimeString(),
                assignee = "Muhammad Haikal Siregar, S.Farm",
                instruction = "Lakukan pemesanan darurat Paracetamol & Omeprazole ke PBF distributor.",
                priority = "Tinggi",
                isCompleted = false
            )
        )

        _state.value = ClinicState(
            currentRole = Role.ADMIN_PENDAFTARAN,
            selectedTab = 0,
            patients = initialPatients,
            queue = initialQueue,
            medicines = initialMedicines,
            stockMutations = emptyList(),
            opnameRecords = initialOpname,
            icd10List = initialIcd10,
            staffTasks = initialTasks,
            activeCallingQueueId = null,
            toastMessage = null
        )
    }

    fun setRole(newRole: Role) {
        _state.value = _state.value.copy(currentRole = newRole, selectedTab = 0)
    }

    fun setSelectedTab(tabIndex: Int) {
        _state.value = _state.value.copy(selectedTab = tabIndex)
    }

    fun dismissToast() {
        _state.value = _state.value.copy(toastMessage = null)
    }

    // --- ROLE 1: ADMIN ACTIONS ---
    fun registerNewPatientAndQueue(
        nama: String,
        nik: String,
        tanggalLahir: String,
        jenisKelamin: String,
        alamat: String,
        noHp: String,
        riwayatAlergi: String,
        payerType: PayerType,
        noBpjs: String,
        isBpjsActive: Boolean,
        consultationTariff: Long
    ): QueueItem {
        val newPatient = Patient(
            id = "P-${System.currentTimeMillis() % 10000}",
            nik = nik,
            nama = nama,
            tanggalLahir = tanggalLahir,
            jenisKelamin = jenisKelamin,
            alamat = alamat,
            noHp = noHp,
            riwayatAlergi = if (riwayatAlergi.isBlank()) "Tidak ada riwayat alergi" else riwayatAlergi,
            noBpjs = if (payerType.isBpjs) noBpjs else "",
            statusBpjsAktif = if (payerType.isBpjs) isBpjsActive else false
        )

        val prefix = if (payerType.isBpjs) "A" else "U"
        val existingCount = _state.value.queue.count { it.queueNumber.startsWith(prefix) } + 1
        val queueNumber = "$prefix-${String.format("%03d", existingCount)}"

        val newQueueItem = QueueItem(
            id = "V-${System.currentTimeMillis() % 100000}",
            queueNumber = queueNumber,
            patient = newPatient,
            payerType = payerType,
            status = QueueStatus.MENUNGGU_TRIAGE,
            consultationTariff = consultationTariff,
            totalBill = consultationTariff,
            paymentStatus = if (payerType.isBpjs && isBpjsActive) "Klaim BPJS Terverifikasi" else "Belum Bayar",
            registeredAt = getCurrentTimeString()
        )

        _state.value = _state.value.copy(
            patients = _state.value.patients + newPatient,
            queue = _state.value.queue + newQueueItem,
            toastMessage = "Antrean $queueNumber ($nama) berhasil dibuat!"
        )
        return newQueueItem
    }

    fun createVisitForPatient(patient: Patient, payerType: PayerType, tariff: Long): QueueItem {
        val prefix = if (payerType.isBpjs) "A" else "U"
        val existingCount = _state.value.queue.count { it.queueNumber.startsWith(prefix) } + 1
        val queueNumber = "$prefix-${String.format("%03d", existingCount)}"

        val newQueueItem = QueueItem(
            id = "V-${System.currentTimeMillis() % 100000}",
            queueNumber = queueNumber,
            patient = patient,
            payerType = payerType,
            status = QueueStatus.MENUNGGU_TRIAGE,
            consultationTariff = tariff,
            totalBill = tariff,
            paymentStatus = if (payerType.isBpjs && patient.statusBpjsAktif) "Klaim BPJS Terverifikasi" else "Belum Bayar",
            registeredAt = getCurrentTimeString()
        )

        _state.value = _state.value.copy(
            queue = _state.value.queue + newQueueItem,
            toastMessage = "Antrean $queueNumber (${patient.nama}) berhasil dibuat!"
        )
        return newQueueItem
    }

    fun checkoutPayment(queueId: String) {
        val currentQueue = _state.value.queue.toMutableList()
        val index = currentQueue.indexOfFirst { it.id == queueId }
        if (index != -1) {
            val item = currentQueue[index]
            val updated = item.copy(
                status = QueueStatus.SELESAI,
                paymentStatus = if (item.payerType.isBpjs) "Klaim BPJS Disetujui" else "Lunas"
            )
            currentQueue[index] = updated
            _state.value = _state.value.copy(
                queue = currentQueue,
                toastMessage = "Kunjungan ${item.queueNumber} (${item.patient.nama}) Selesai!"
            )
        }
    }

    // --- ROLE 2: PERAWAT ACTIONS ---
    fun callPatient(queueId: String) {
        val currentQueue = _state.value.queue.toMutableList()
        val index = currentQueue.indexOfFirst { it.id == queueId }
        if (index != -1) {
            val item = currentQueue[index]
            currentQueue[index] = item.copy(status = QueueStatus.SEDANG_TRIAGE)
            _state.value = _state.value.copy(
                queue = currentQueue,
                activeCallingQueueId = queueId,
                toastMessage = "🔊 Memanggil ${item.queueNumber} (${item.patient.nama})"
            )
        }
    }

    fun stopCallingPatient() {
        _state.value = _state.value.copy(activeCallingQueueId = null)
    }

    fun skipPatient(queueId: String) {
        val currentQueue = _state.value.queue.toMutableList()
        val index = currentQueue.indexOfFirst { it.id == queueId }
        if (index != -1) {
            val item = currentQueue[index]
            currentQueue[index] = item.copy(status = QueueStatus.DILEWATI)
            _state.value = _state.value.copy(
                queue = currentQueue,
                activeCallingQueueId = if (_state.value.activeCallingQueueId == queueId) null else _state.value.activeCallingQueueId,
                toastMessage = "Antrean ${item.queueNumber} dilewati (Absent)."
            )
        }
    }

    fun recallPatient(queueId: String) {
        val currentQueue = _state.value.queue.toMutableList()
        val index = currentQueue.indexOfFirst { it.id == queueId }
        if (index != -1) {
            val item = currentQueue[index]
            currentQueue[index] = item.copy(status = QueueStatus.MENUNGGU_TRIAGE)
            _state.value = _state.value.copy(
                queue = currentQueue,
                toastMessage = "Antrean ${item.queueNumber} dikembalikan ke antrean aktif."
            )
        }
    }

    fun cancelVisit(queueId: String) {
        val currentQueue = _state.value.queue.toMutableList()
        val index = currentQueue.indexOfFirst { it.id == queueId }
        if (index != -1) {
            val item = currentQueue[index]
            currentQueue[index] = item.copy(status = QueueStatus.BATAL)
            _state.value = _state.value.copy(
                queue = currentQueue,
                activeCallingQueueId = if (_state.value.activeCallingQueueId == queueId) null else _state.value.activeCallingQueueId,
                toastMessage = "Kunjungan ${item.queueNumber} dibatalkan."
            )
        }
    }

    fun submitTriageVitals(queueId: String, vitals: TriageVitals) {
        val currentQueue = _state.value.queue.toMutableList()
        val index = currentQueue.indexOfFirst { it.id == queueId }
        if (index != -1) {
            val item = currentQueue[index]
            currentQueue[index] = item.copy(
                triageVitals = vitals,
                status = QueueStatus.SIAP_DIPERIKSA_DOKTER
            )
            _state.value = _state.value.copy(
                queue = currentQueue,
                activeCallingQueueId = null,
                toastMessage = "TTV ${item.queueNumber} tersimpan. Pasien dialihkan ke Dokter."
            )
        }
    }

    // --- ROLE 3: DOKTER ACTIONS ---
    fun submitConsultation(queueId: String, clinicalRecord: ClinicalRecord) {
        val currentQueue = _state.value.queue.toMutableList()
        val index = currentQueue.indexOfFirst { it.id == queueId }
        if (index != -1) {
            val item = currentQueue[index]
            var calculatedDrugBill = 0L
            if (clinicalRecord.needsPrescription) {
                clinicalRecord.prescriptionItems.forEach { rx ->
                    calculatedDrugBill += rx.quantity * rx.unitPrice
                }
            }

            val nextStatus = if (clinicalRecord.needsPrescription && clinicalRecord.prescriptionItems.isNotEmpty()) {
                QueueStatus.MENUNGGU_FARMASI
            } else {
                QueueStatus.MENUNGGU_PEMBAYARAN
            }

            val total = item.consultationTariff + calculatedDrugBill

            currentQueue[index] = item.copy(
                clinicalRecord = clinicalRecord,
                status = nextStatus,
                drugBill = calculatedDrugBill,
                totalBill = total,
                hasSubstitutionAlert = false,
                substitutionNote = null
            )

            _state.value = _state.value.copy(
                queue = currentQueue,
                toastMessage = if (clinicalRecord.needsPrescription) "Konsultasi selesai. E-Resep dikirim ke Farmasi." else "Konsultasi selesai. Pasien menuju Kasir."
            )
        }
    }

    // --- ROLE 4: FARMASI ACTIONS ---
    fun requestDrugSubstitution(queueId: String, note: String) {
        val currentQueue = _state.value.queue.toMutableList()
        val index = currentQueue.indexOfFirst { it.id == queueId }
        if (index != -1) {
            val item = currentQueue[index]
            currentQueue[index] = item.copy(
                status = QueueStatus.SIAP_DIPERIKSA_DOKTER, // Re-routed back to doctor desk!
                hasSubstitutionAlert = true,
                substitutionNote = note
            )
            _state.value = _state.value.copy(
                queue = currentQueue,
                toastMessage = "Permintaan substitusi obat dikirimkan balik ke meja Dokter."
            )
        }
    }

    fun dispensePrescription(queueId: String, pharmacistName: String = "Muhammad Haikal Siregar, S.Farm") {
        val currentQueue = _state.value.queue.toMutableList()
        val index = currentQueue.indexOfFirst { it.id == queueId }
        if (index == -1) return

        val item = currentQueue[index]
        val rxItems = item.clinicalRecord?.prescriptionItems ?: emptyList()

        val updatedMedicines = _state.value.medicines.toMutableList()
        val newMutations = _state.value.stockMutations.toMutableList()

        rxItems.forEach { rxItem ->
            val medIndex = updatedMedicines.indexOfFirst { it.id == rxItem.medicineId }
            if (medIndex != -1) {
                val med = updatedMedicines[medIndex]
                val newStock = (med.currentStock - rxItem.quantity).coerceAtLeast(0)
                updatedMedicines[medIndex] = med.copy(currentStock = newStock)

                newMutations.add(
                    0,
                    StockMutation(
                        id = "MUT-${System.currentTimeMillis() % 100000}",
                        timestamp = getCurrentTimeString(),
                        medicineName = med.name,
                        type = StockMutationType.KELUAR_RESEP,
                        quantityChange = -rxItem.quantity,
                        resultingStock = newStock,
                        referenceInfo = "Resep ${item.queueNumber} (${item.patient.nama})",
                        operator = pharmacistName
                    )
                )
            }
        }

        currentQueue[index] = item.copy(status = QueueStatus.MENUNGGU_PEMBAYARAN)

        _state.value = _state.value.copy(
            queue = currentQueue,
            medicines = updatedMedicines,
            stockMutations = newMutations,
            toastMessage = "Obat ${item.queueNumber} diserahkan & stok otomatis terpotong!"
        )
    }

    fun addManualStockReceipt(
        medicineId: String,
        quantityAdded: Int,
        batchNumber: String,
        supplier: String,
        operator: String = "Muhammad Haikal Siregar, S.Farm"
    ) {
        val currentMeds = _state.value.medicines.toMutableList()
        val index = currentMeds.indexOfFirst { it.id == medicineId }
        if (index == -1) return

        val med = currentMeds[index]
        val newStock = med.currentStock + quantityAdded
        currentMeds[index] = med.copy(
            currentStock = newStock,
            batchNumber = if (batchNumber.isNotBlank()) batchNumber else med.batchNumber
        )

        val newMutation = StockMutation(
            id = "MUT-${System.currentTimeMillis() % 100000}",
            timestamp = getCurrentTimeString(),
            medicineName = med.name,
            type = StockMutationType.MASUK,
            quantityChange = quantityAdded,
            resultingStock = newStock,
            referenceInfo = "Penerimaan PBF: $supplier (Batch: $batchNumber)",
            operator = operator
        )

        _state.value = _state.value.copy(
            medicines = currentMeds,
            stockMutations = listOf(newMutation) + _state.value.stockMutations,
            toastMessage = "Penerimaan stok ${med.name} (+$quantityAdded) berhasil dicatat!"
        )
    }

    fun submitStockOpname(
        medicineId: String,
        physicalCount: Int,
        discrepancyReason: String,
        staffName: String = "Muhammad Haikal Siregar, S.Farm"
    ) {
        val med = _state.value.medicines.find { it.id == medicineId } ?: return
        val variance = physicalCount - med.currentStock

        val newOpname = StockOpnameRecord(
            id = "OPN-${System.currentTimeMillis() % 100000}",
            date = getCurrentDateString(),
            medicineId = med.id,
            medicineName = med.name,
            systemStock = med.currentStock,
            physicalCount = physicalCount,
            variance = variance,
            reason = discrepancyReason,
            submittedBy = staffName,
            status = OpnameStatus.MENUNGGU_APPROVAL
        )

        _state.value = _state.value.copy(
            opnameRecords = listOf(newOpname) + _state.value.opnameRecords,
            toastMessage = "Laporan Stok Opname ${med.name} diajukan ke Kepala Klinik."
        )
    }

    // --- ROLE 5: KEPALA KLINIK ACTIONS ---
    fun approveStockOpname(
        opnameId: String,
        isApproved: Boolean,
        managerName: String = "dr. Stephen Parnahean",
        note: String = ""
    ) {
        val currentOpnames = _state.value.opnameRecords.toMutableList()
        val index = currentOpnames.indexOfFirst { it.id == opnameId }
        if (index == -1) return

        val opn = currentOpnames[index]
        val updatedOpn = opn.copy(
            status = if (isApproved) OpnameStatus.APPROVED else OpnameStatus.REJECTED,
            approvedBy = managerName,
            approvalNote = note.ifBlank { if (isApproved) "Disetujui untuk pemutakhiran saldo" else "Ditolak, hitung ulang fisik" }
        )
        currentOpnames[index] = updatedOpn

        val updatedMedicines = _state.value.medicines.toMutableList()
        val newMutations = _state.value.stockMutations.toMutableList()

        if (isApproved) {
            val medIndex = updatedMedicines.indexOfFirst { it.id == opn.medicineId }
            if (medIndex != -1) {
                val med = updatedMedicines[medIndex]
                updatedMedicines[medIndex] = med.copy(currentStock = opn.physicalCount)

                newMutations.add(
                    0,
                    StockMutation(
                        id = "MUT-${System.currentTimeMillis() % 100000}",
                        timestamp = getCurrentTimeString(),
                        medicineName = med.name,
                        type = StockMutationType.PENYESUAIAN_OPNAME,
                        quantityChange = opn.variance,
                        resultingStock = opn.physicalCount,
                        referenceInfo = "Approval Opname #${opn.id} oleh $managerName",
                        operator = managerName
                    )
                )
            }
        }

        _state.value = _state.value.copy(
            opnameRecords = currentOpnames,
            medicines = updatedMedicines,
            stockMutations = newMutations,
            toastMessage = if (isApproved) "Opname ${opn.medicineName} disetujui & stok sistem disesuaikan." else "Opname ${opn.medicineName} ditolak."
        )
    }

    fun assignStaffTask(assignee: String, instruction: String, priority: String) {
        val newTask = StaffTaskItem(
            id = "TSK-${System.currentTimeMillis() % 100000}",
            createdAt = getCurrentTimeString(),
            assignee = assignee,
            instruction = instruction,
            priority = priority,
            isCompleted = false
        )
        _state.value = _state.value.copy(
            staffTasks = listOf(newTask) + _state.value.staffTasks,
            toastMessage = "Tugas berhasil dikirim ke $assignee."
        )
    }

    fun toggleTaskComplete(taskId: String) {
        val updatedTasks = _state.value.staffTasks.map {
            if (it.id == taskId) it.copy(isCompleted = !it.isCompleted) else it
        }
        _state.value = _state.value.copy(staffTasks = updatedTasks)
    }

    companion object {
        @Volatile
        private var instance: ClinicRepository? = null

        fun getInstance(): ClinicRepository {
            return instance ?: synchronized(this) {
                instance ?: ClinicRepository().also { instance = it }
            }
        }
    }
}
