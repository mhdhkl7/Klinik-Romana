# Klinik Romana - HCI & Operational HIS Architecture (Revised v3)

A comprehensive, production-grade clinical architecture for **Klinik Romana**, optimized for Human-Computer Interaction (HCI) and real-world clinic workflows. The system eliminates workflow friction through an integrated single-page consultation workspace, queue edge-case handling (Panggil / Skip / Batal), real-time patient journey stepper tracking, and bi-directional pharmacy drug substitution triggers.

Both the standalone, executable single-file Flutter application (`/main.dart`) and the synchronized native Android Jetpack Compose application will implement this exact UX matrix.

---

## User Review & Critical Decisions

> [!IMPORTANT]
> The following HCI and clinical workflow enhancements are confirmed:
> 1. **Single Consultation Workspace (Dokter)**: Integrated linear consultation screen combining Anamnesis, Physical Exam, ICD-10 Search, and E-Prescription in Tab 2, eliminating tab-switching friction. Tab 3 is repurposed for **Riwayat EMR Pasien (Patient History Lookup)**.
> 2. **Patient Tracking & Clinic Ledger (Admin)**: BPJS validation is integrated directly into the Registration modal. Tab 2 is repurposed for **Status Pasien & Tracking (Monitoring Posisi)** with live stage distribution.
> 3. **Queue Edge-Case Handling (Perawat)**: Triage queue includes full queue management actions: `[Panggil]`, `[Lewati / Absent]`, and `[Batal]`.
> 4. **Pharmacy Emergency Drug Substitution**: Rapid trigger `"Minta Substitusi Obat"` that flags the prescription and sends an urgent notification directly to the Doctor's consultation desk for immediate alternative drug selection.
> 5. **Patient Journey Stepper**: Visual progress indicator (`[Registrasi] ➔ [Triage TTV] ➔ [Pemeriksaan Dokter] ➔ [Farmasi] ➔ [Selesai]`) embedded on patient cards across all roles.

---

## 1. Optimized 3-Tab HIS Matrix per Persona

```
┌──────────────┬────────────────────────┬────────────────────────┬────────────────────────┐
│ Persona      │ Tab 1 (Operational)    │ Tab 2 (Main Work)      │ Tab 3 (Management/Log) │
├──────────────┼────────────────────────┼────────────────────────┼────────────────────────┤
│ Aditia Rach  │ Registrasi & Antrean   │ Status Pasien & Tracking│ Kasir & Pembayaran     │
│ (Admin)      │ (Include Cek BPJS)     │ (Monitoring Posisi)    │ (Checkout & Cetak)     │
├──────────────┼────────────────────────┼────────────────────────┼────────────────────────┤
│ Rehan Prat.  │ Antrean Triage         │ Form Input TTV         │ Rekap Vital Signs      │
│ (Perawat)    │ (Panggil/Skip/Batal)   │ (Tanda Vital & Keluhan)│ (Ringkasan Pasien)     │
├──────────────┼────────────────────────┼────────────────────────┼────────────────────────┤
│ dr. Richard  │ Antrean Pasien Dokter  │ Konsultasi & E-Resep   │ Riwayat EMR Pasien     │
│ (Dokter)     │ (Daftar Tunggu)        │ (Terintegrasi 1 Layar) │ (Medical History)      │
├──────────────┼────────────────────────┼────────────────────────┼────────────────────────┤
│ M. Haikal    │ Antrean Resep Masuk    │ Katalog Stok & Alert   │ Stok Opname & Audit    │
│ (Farmasi)    │ (Verifikasi & Handover)│ (Warning Threshold)    │ (Form Selisih Stok)    │
├──────────────┼────────────────────────┼────────────────────────┼────────────────────────┤
│ dr. Stephen  │ Executive Dashboard    │ Analisis Operasional   │ Persetujuan & Tugas    │
│ (Kepala)     │ (KPI Real-time)        │ (Bottleneck & Finansial│ (Approve Opname)       │
└──────────────┴────────────────────────┴────────────────────────┴────────────────────────┘
```

---

## 2. Central State Engine & Data Model

- **Clinic Name**: Klinik Romana
- **Personas**:
  1. `ADMIN_PENDAFTARAN`: Aditia Rach
  2. `PERAWAT_TRIAGE`: Rehan Pratama
  3. `DOKTER_SPESIALIS`: dr. Richard Pangihutan, Sp.PD
  4. `PETUGAS_FARMASI`: Muhammad Haikal Siregar, S.Farm
  5. `KEPALA_KLINIK`: dr. Stephen Parnahean
- **Initial Patients**:
  - `Patient 1`: **Siti Rahma** (NIK: `3171012304990001`, BPJS: `000123456789`, Aktif JKN) - Pre-enrolled in Triage Queue `A-001`.
  - `Patient 2`: **Budi Santoso** (NIK: `3171051108880002`, Umum) - In patient database ready for admission.
- **Initial Inventory**:
  - `Paracetamol 500mg`: Stock 12, Min 20, Batch `PCT-01`, Unit Tablet (**Low-Stock Alert**)
  - `Amoxicillin 500mg`: Stock 80, Min 30, Batch `AMX-02`, Unit Tablet (Safe)
  - `Omeprazole 20mg`: Stock 5, Min 15, Batch `OMP-01`, Unit Kapsul (**Low-Stock Alert**)
  - `Amlodipine 10mg`: Stock 50, Min 10, Batch `AML-03`, Unit Tablet (Safe)
- **Tariffs**: Umum (Rp 50.000), Klaim BPJS (Rp 120.000).
- **Queue Status Stages**:
  1. `REGISTRASI` / `MENUNGGU_TRIAGE`
  2. `SEDANG_TRIAGE` (Calling / TTV In-progress)
  3. `SIAP_DIPERIKSA_DOKTER`
  4. `SEDANG_DIPERIKSA_DOKTER`
  5. `MENUNGGU_FARMASI` (or `SUBSTITUSI_OBAT_DIMINTA` when pharmacy requests substitution)
  6. `OBAT_DISERAHKAN` / `MENUNGGU_PEMBAYARAN`
  7. `SELESAI`
  8. `DILEWATI` (Absent / Skip queue)
  9. `BATAL` (Cancelled visit)

---

## 3. Screen Specifications & HCI Flow

### 1. Admin Pendaftaran & Kasir (Aditia Rach)
- **Tab 1 (Registrasi & Antrean)**:
  - Patient search bar (Siti Rahma, Budi Santoso) + "Daftar Pasien Baru" button.
  - Interactive Registration Modal with inline BPJS verification:
    - Input NIK, Nama, No. HP, Jenis Kelamin, Tanggal Lahir.
    - Payer toggle (BPJS vs Umum): if BPJS, displays JKN Active Badge + "Simulasi Status BPJS (Aktif/Non-Aktif)" switch. If Umum, binds consultation tariff Rp 50.000.
    - "Cetak Antrean & Daftarkan" button: generates ticket (`A-001` or `U-001`) and sends directly to Perawat Triage.
- **Tab 2 (Status Pasien & Tracking)**:
  - Live visitor ledger showing all clinic visits today.
  - Each item features the **Visual Progress Stepper**:
    `[1. Reg] ➔ [2. Triage] ➔ [3. Dokter] ➔ [4. Farmasi] ➔ [5. Selesai]`
  - Room position badges showing where each patient currently is.
- **Tab 3 (Kasir & Pembayaran)**:
  - Visits in checkout stage (`MENUNGGU_PEMBAYARAN`).
  - Checkout modal: itemized doctor fee + pharmacy medicine charges + BPJS waiver status.
  - "Proses & Selesaikan Kunjungan" button: marks status as `SELESAI` and issues printable digital receipt.

### 2. Perawat Triage (Rehan Pratama)
- **Tab 1 (Antrean Triage)**:
  - Queue list of patients awaiting initial assessment (Siti Rahma `A-001`).
  - Actions for each patient:
    - `[🔊 Panggil]`: triggers audio/visual calling banner and marks status `SEDANG_TRIAGE`.
    - `[⏭ Lewati / Absent]`: marks patient as `DILEWATI` and moves them to bottom of queue. Can be recalled anytime.
    - `[❌ Batal]`: marks visit as `BATAL` with confirmation dialog.
    - `[📝 Input TTV]`: switches to Tab 2 with patient context loaded.
- **Tab 2 (Form Input TTV)**:
  - Comprehensive clinical inputs: Blood Pressure (Systolic & Diastolic mmHg), Heart Rate (bpm), Body Temperature (°C), Respiratory Rate (x/min), Weight (kg), Height (cm), and Chief Complaints (Keluhan Utama).
  - "Simpan TTV & Teruskan ke Dokter" button: saves vitals into central state and transitions status to `SIAP_DIPERIKSA_DOKTER`.
- **Tab 3 (Rekap Vital Signs)**:
  - Summary table of all examined patients with their vital metrics, BMI, and triage classification.

### 3. Dokter Spesialis (dr. Richard Pangihutan, Sp.PD)
- **Tab 1 (Antrean Pasien Dokter)**:
  - List of patients awaiting doctor consultation (`SIAP_DIPERIKSA_DOKTER`).
  - Emergency banner if Pharmacy requested a drug substitution!
  - Clicking patient opens the Single Consultation Workspace in Tab 2.
- **Tab 2 (Konsultasi & E-Resep - Single Workspace)**:
  - **All-in-one linear consultation sheet**:
    1. *Patient Demographic & Triage Header*: Displays nurse TTV (BP, HR, Temp, BMI, Complaints) and prominent allergy warnings.
    2. *Clinical Findings*: Anamnesis / Anamnese lanjutan, Physical Examination notes.
    3. *ICD-10 Search & Select*: Primary care code selector (I10 Hipertensi, E11 DM, J00 Common Cold, K29 Gastritis, etc.).
    4. *Therapy & Education*: Doctor recommendations and follow-up plan.
    5. *E-Prescription Module*: Toggle "Perlu Resep Obat? (Ya/Tidak)". When YES: live drug selector showing inventory stock, dose (e.g. 3x1), instructions (sesudah makan), and quantity with "Tambah Obat" button.
    6. *Substitution Request Alert*: If Pharmacy requested an alternative drug, the doctor sees the pharmacist's note right here and can swap the medicine immediately.
    7. *Submission*: "Simpan Rekam Medis & Kirim Resep" button: commits consultation and routes patient to Farmasi (or Kasir if no drugs).
- **Tab 3 (Riwayat EMR Pasien)**:
  - Historical lookup of previous medical consultations, diagnoses, and past prescriptions.

### 4. Petugas Farmasi (Muhammad Haikal Siregar, S.Farm)
- **Tab 1 (Antrean Resep Masuk)**:
  - Incoming doctor prescriptions.
  - Interactive Actions:
    - `[⚠️ Minta Substitusi Obat]`: Opens a rapid modal to specify which drug is out of stock / damaged on shelf and recommend an alternative. Directly flags the prescription in the Doctor's consultation desk!
    - `[✅ Konfirmasi Penyerahan Obat]`: Automatically deducts inventory stock, writes a `KELUAR_RESEP` mutation entry, and routes the patient to Kasir.
- **Tab 2 (Katalog Stok & Alert)**:
  - Prominent red alert banner for medicines under minimum threshold (`Paracetamol 500mg` at 12/20, `Omeprazole 20mg` at 5/15).
  - Searchable inventory list with batch numbers and expiry dates.
  - Quick "Penerimaan Stok PBF" dialog.
- **Tab 3 (Stok Opname & Audit)**:
  - Physical count audit form: Select drug, enter physical count, auto-calculate variance (`Fisik - Sistem`), input discrepancy reason, and submit for approval.
  - Full mutation log timeline (Penerimaan, Dispensing Resep, Opname Adjustments).

### 5. Kepala Klinik (dr. Stephen Parnahean)
- **Tab 1 (Executive Dashboard)**:
  - Real-time KPI summary cards: Total Visits, Active Queue by Stage, Expiring/Low Stock warnings, BPJS claim revenue (Rp 120.000/patient) vs Umum cash (Rp 50.000/patient).
- **Tab 2 (Analisis Operasional)**:
  - Patient flow throughput, bottleneck analytics, and stage duration tracking.
- **Tab 3 (Persetujuan & Tugas)**:
  - Pending Stock Opname approval list: "Setujui" (officially updates inventory stock in Central State) or "Tolak".
  - "Tugaskan Staf / Buat Instruksi" modal to assign tasks to clinic staff.

---

## 4. Technical Deliverables

1. **Standalone Flutter Application (`/main.dart`)**:
   - Single-file, 100% complete Flutter app using `package:flutter/material.dart`.
   - Zero placeholders, zero truncation, complete reactive state engine, full implementations of all 5 personas and their respective 3 tabs, modals, dialogs, and stepper indicators.
2. **Synchronized Native Android Jetpack Compose App**:
   - Synchronize `ClinicModels.kt`, `ClinicRepository.kt`, `MainActivity.kt`, and screen composables to implement the exact same 3-tab HIS matrix, persona roster, and patient stepper so the live streaming emulator reflects this solution seamlessly.
3. **Verification**:
   - Run `compile_applet` to confirm complete compilation of the native Android application.
