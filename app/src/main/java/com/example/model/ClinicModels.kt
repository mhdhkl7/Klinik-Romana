package com.example.model

import androidx.compose.ui.graphics.Color
import com.example.ui.theme.*

enum class Role(
    val title: String,
    val staffName: String,
    val shortName: String,
    val subtitle: String
) {
    ADMIN_PENDAFTARAN("Admin Pendaftaran", "Aditia Rach", "Admin", "Registrasi & Kasir"),
    PERAWAT_TRIAGE("Perawat Triage", "Rehan Pratama", "Perawat", "Pemeriksaan Tanda Vital"),
    DOKTER("Dokter Spesialis", "dr. Richard Pangihutan, Sp.PD", "Dokter", "Penyakit Dalam & E-Resep"),
    FARMASI_STOK("Petugas Farmasi", "Muhammad Haikal Siregar, S.Farm", "Farmasi", "Dispensing & Stok Obat"),
    KEPALA_KLINIK("Kepala Klinik", "dr. Stephen Parnahean", "Kepala", "Executive Dashboard & Approval")
}

enum class PayerType(val displayName: String, val isBpjs: Boolean) {
    BPJS("BPJS Kesehatan (JKN)", true),
    UMUM("Pasien Umum (Mandiri)", false),
    ASURANSI_SWASTA("Asuransi Rekanan Swasta", false)
}

enum class QueueStatus(
    val label: String,
    val badgeBg: Color,
    val badgeText: Color,
    val stageIndex: Int // 1: Reg/Triage, 2: Dokter, 3: Farmasi, 4: Kasir/Selesai
) {
    MENUNGGU_TRIAGE("Menunggu Triage", StatusWaitYellowBg, StatusWaitYellowText, 1),
    SEDANG_TRIAGE("Sedang Triage", StatusInfoBlueBg, StatusInfoBlueText, 1),
    SIAP_DIPERIKSA_DOKTER("Siap Diperiksa Dokter", StatusWaitYellowBg, StatusWaitYellowText, 2),
    SEDANG_DIPERIKSA_DOKTER("Pemeriksaan Dokter", StatusInfoBlueBg, StatusInfoBlueText, 2),
    MENUNGGU_FARMASI("Menunggu Farmasi", StatusWaitYellowBg, StatusWaitYellowText, 3),
    MENUNGGU_PEMBAYARAN("Menunggu Kasir", StatusWaitYellowBg, StatusWaitYellowText, 4),
    SELESAI("Kunjungan Selesai", StatusDoneGreenBg, StatusDoneGreenText, 5),
    DILEWATI("Dilewati (Absent)", Color(0xFFFEF3C7), Color(0xFF92400E), 1),
    BATAL("Dibatalkan", Color(0xFFFEE2E2), Color(0xFF991B1B), 0)
}

data class Patient(
    val id: String,
    val nik: String,
    val nama: String,
    val tanggalLahir: String,
    val jenisKelamin: String, // "Laki-laki" | "Perempuan"
    val alamat: String,
    val noHp: String,
    val riwayatAlergi: String = "Tidak ada riwayat alergi",
    val noBpjs: String = "",
    val statusBpjsAktif: Boolean = true
)

data class TriageVitals(
    val systolic: Int,
    val diastolic: Int,
    val heartRate: Int,
    val bodyTemp: Float,
    val respiratoryRate: Int,
    val weightKg: Float,
    val heightCm: Float,
    val chiefComplaints: String
) {
    val bmi: Float
        get() = if (heightCm > 0) weightKg / ((heightCm / 100f) * (heightCm / 100f)) else 0f

    val bloodPressureCategory: String
        get() = when {
            systolic < 90 || diastolic < 60 -> "Hipotensi"
            systolic <= 120 && diastolic <= 80 -> "Normal"
            systolic <= 139 || diastolic <= 89 -> "Prehipertensi"
            else -> "Hipertensi"
        }

    val isAbnormal: Boolean
        get() = systolic > 140 || diastolic > 90 || bodyTemp >= 38.0f || heartRate > 100 || heartRate < 60
}

data class PrescriptionItem(
    val medicineId: String,
    val medicineName: String,
    val dosage: String,
    val frequency: String,
    val quantity: Int,
    val aturanPakai: String,
    val unitPrice: Long = 2000L
)

data class ClinicalRecord(
    val anamnesis: String,
    val physicalExam: String,
    val icd10Code: String,
    val icd10Name: String,
    val therapyPlan: String,
    val needsPrescription: Boolean,
    val prescriptionItems: List<PrescriptionItem> = emptyList(),
    val doctorNotes: String = ""
)

data class QueueItem(
    val id: String,
    val queueNumber: String, // e.g. A-001, U-001
    val patient: Patient,
    val payerType: PayerType,
    val status: QueueStatus,
    val consultationTariff: Long = 50000L,
    val drugBill: Long = 0L,
    val totalBill: Long = 50000L,
    val paymentStatus: String = "Belum Bayar",
    val triageVitals: TriageVitals? = null,
    val clinicalRecord: ClinicalRecord? = null,
    val registeredAt: String = "08:15 WIB",
    val isCalling: Boolean = false,
    val coordDoctorNote: String? = null,
    val hasSubstitutionAlert: Boolean = false,
    val substitutionNote: String? = null
)

data class MedicineItem(
    val id: String,
    val code: String,
    val name: String,
    val category: String,
    val batchNumber: String,
    val expiryDate: String,
    val currentStock: Int,
    val minStockAlert: Int,
    val unit: String,
    val unitPrice: Long
) {
    val isOutOfStock: Boolean get() = currentStock <= 0
    val isLowStock: Boolean get() = currentStock <= minStockAlert
}

enum class StockMutationType(val label: String, val isPositive: Boolean) {
    MASUK("Penerimaan Baru", true),
    KELUAR_RESEP("Dispensing E-Resep", false),
    PENYESUAIAN_OPNAME("Penyesuaian Opname", false)
}

data class StockMutation(
    val id: String,
    val timestamp: String,
    val medicineName: String,
    val type: StockMutationType,
    val quantityChange: Int,
    val resultingStock: Int,
    val referenceInfo: String,
    val operator: String
)

enum class OpnameStatus(val label: String, val bg: Color, val text: Color) {
    MENUNGGU_APPROVAL("Menunggu Approval", StatusWaitYellowBg, StatusWaitYellowText),
    APPROVED("Disetujui Kepala Klinik", StatusDoneGreenBg, StatusDoneGreenText),
    REJECTED("Ditolak", StatusAlertRedBg, StatusAlertRedText)
}

data class StockOpnameRecord(
    val id: String,
    val date: String,
    val medicineId: String,
    val medicineName: String,
    val systemStock: Int,
    val physicalCount: Int,
    val variance: Int,
    val reason: String,
    val submittedBy: String,
    val status: OpnameStatus,
    val approvedBy: String? = null,
    val approvalNote: String? = null
)

data class Icd10Entry(
    val code: String,
    val description: String,
    val commonTerm: String
)

data class StaffTaskItem(
    val id: String,
    val createdAt: String,
    val assignee: String,
    val instruction: String,
    val priority: String,
    val isCompleted: Boolean = false
)
