import 'package:flutter/material.dart';

void main() {
  runApp(const KlinikRomanaApp());
}

// ==============================================================================
// 1. DATA MODELS & ENUMS
// ==============================================================================

enum PersonaRole {
  admin,
  perawat,
  dokter,
  farmasi,
  kepala,
}

class PersonaAccount {
  final PersonaRole role;
  final String name;
  final String title;
  final IconData icon;

  const PersonaAccount({
    required this.role,
    required this.name,
    required this.title,
    required this.icon,
  });
}

enum PayerType { bpjs, umum }

enum VisitStage {
  registrasi,
  triage,
  dokter,
  farmasi,
  kasir,
  selesai,
  dilewati,
  batal,
}

class Patient {
  final String id;
  final String nik;
  final String name;
  final String phone;
  final String gender;
  final String birthDate;
  final PayerType payerType;
  final String bpjsNumber;
  bool isBpjsActive;
  final String allergy;

  Patient({
    required this.id,
    required this.nik,
    required this.name,
    required this.phone,
    required this.gender,
    required this.birthDate,
    required this.payerType,
    this.bpjsNumber = '',
    this.isBpjsActive = true,
    this.allergy = 'Tidak ada riwayat alergi',
  });
}

class VitalSigns {
  final int systolic;
  final int diastolic;
  final int heartRate;
  final double bodyTemp;
  final int respiratoryRate;
  final double weight;
  final String chiefComplaints;

  VitalSigns({
    required this.systolic,
    required this.diastolic,
    required this.heartRate,
    required this.bodyTemp,
    required this.respiratoryRate,
    required this.weight,
    required this.chiefComplaints,
  });

  String get bloodPressureCategory {
    if (systolic < 90 || diastolic < 60) return 'Hipotensi';
    if (systolic <= 120 && diastolic <= 80) return 'Normal';
    if (systolic <= 139 || diastolic <= 89) return 'Prehipertensi';
    return 'Hipertensi';
  }
}

class PrescriptionItem {
  final String medicineId;
  final String medicineName;
  final String dosage;
  final String instruction;
  final int quantity;
  final int unitPrice;

  PrescriptionItem({
    required this.medicineId,
    required this.medicineName,
    required this.dosage,
    required this.instruction,
    required this.quantity,
    required this.unitPrice,
  });
}

class MedicalRecord {
  final String anamnesis;
  final String physicalExam;
  final String icd10Code;
  final String icd10Desc;
  final String therapyPlan;
  final bool needsPrescription;
  final List<PrescriptionItem> prescriptionItems;
  final String followUpNote;

  MedicalRecord({
    required this.anamnesis,
    required this.physicalExam,
    required this.icd10Code,
    required this.icd10Desc,
    required this.therapyPlan,
    required this.needsPrescription,
    required this.prescriptionItems,
    this.followUpNote = '',
  });
}

class Visit {
  final String id;
  final String queueNumber;
  final Patient patient;
  VisitStage stage;
  final DateTime registeredAt;
  VitalSigns? vitalSigns;
  MedicalRecord? medicalRecord;
  int consultationFee;
  int drugFee;
  bool isPaid;
  String? substitutionRequestNote;
  bool hasSubstitutionAlert;

  Visit({
    required this.id,
    required this.queueNumber,
    required this.patient,
    required this.stage,
    required this.registeredAt,
    this.vitalSigns,
    this.medicalRecord,
    this.consultationFee = 50000,
    this.drugFee = 0,
    this.isPaid = false,
    this.substitutionRequestNote,
    this.hasSubstitutionAlert = false,
  });

  int get totalFee => consultationFee + drugFee;
}

class InventoryItem {
  final String id;
  final String name;
  final String batch;
  int stock;
  final int minStock;
  final String unit;
  final int unitPrice;
  final String expiryDate;

  InventoryItem({
    required this.id,
    required this.name,
    required this.batch,
    required this.stock,
    required this.minStock,
    required this.unit,
    required this.unitPrice,
    required this.expiryDate,
  });

  bool get isLowStock => stock <= minStock;
}

class StockMutation {
  final String id;
  final DateTime timestamp;
  final String medicineName;
  final String type; // 'Penerimaan', 'Dispensing Resep', 'Penyesuaian Opname'
  final int quantityChange;
  final int finalStock;
  final String note;
  final String operatorName;

  StockMutation({
    required this.id,
    required this.timestamp,
    required this.medicineName,
    required this.type,
    required this.quantityChange,
    required this.finalStock,
    required this.note,
    required this.operatorName,
  });
}

class StockOpnameRequest {
  final String id;
  final DateTime date;
  final String medicineId;
  final String medicineName;
  final int systemStock;
  final int physicalCount;
  final int variance;
  final String reason;
  final String submittedBy;
  String status; // 'Menunggu Approval', 'Disetujui', 'Ditolak'
  String? managerNote;

  StockOpnameRequest({
    required this.id,
    required this.date,
    required this.medicineId,
    required this.medicineName,
    required this.systemStock,
    required this.physicalCount,
    required this.variance,
    required this.reason,
    required this.submittedBy,
    this.status = 'Menunggu Approval',
    this.managerNote,
  });
}

class StaffTask {
  final String id;
  final DateTime createdAt;
  final String assignee;
  final String taskInstruction;
  final String priority;
  bool isCompleted;

  StaffTask({
    required this.id,
    required this.createdAt,
    required this.assignee,
    required this.taskInstruction,
    required this.priority,
    this.isCompleted = false,
  });
}

// ==============================================================================
// 2. CENTRAL REACTIVE STATE ENGINE (SINGLE SOURCE OF TRUTH)
// ==============================================================================

class ClinicStateEngine extends ChangeNotifier {
  // Pre-configured 5 Personas
  final List<PersonaAccount> personas = const [
    PersonaAccount(
      role: PersonaRole.admin,
      name: 'Aditia Rach',
      title: 'Admin Pendaftaran & Kasir',
      icon: Icons.badge,
    ),
    PersonaAccount(
      role: PersonaRole.perawat,
      name: 'Rehan Pratama',
      title: 'Perawat Triage & TTV',
      icon: Icons.health_and_safety,
    ),
    PersonaAccount(
      role: PersonaRole.dokter,
      name: 'dr. Richard Pangihutan, Sp.PD',
      title: 'Dokter Spesialis Penyakit Dalam',
      icon: Icons.medical_services,
    ),
    PersonaAccount(
      role: PersonaRole.farmasi,
      name: 'Muhammad Haikal Siregar, S.Farm',
      title: 'Petugas Farmasi & Apoteker',
      icon: Icons.medication,
    ),
    PersonaAccount(
      role: PersonaRole.kepala,
      name: 'dr. Stephen Parnahean',
      title: 'Kepala Klinik Romana',
      icon: Icons.admin_panel_settings,
    ),
  ];

  late PersonaAccount _currentPersona;
  PersonaAccount get currentPersona => _currentPersona;

  int _selectedTab = 0;
  int get selectedTab => _selectedTab;

  String? callingQueueNumber;

  // Master Data
  final List<Patient> patients = [];
  final List<Visit> visits = [];
  final List<InventoryItem> inventory = [];
  final List<StockMutation> mutations = [];
  final List<StockOpnameRequest> opnameRequests = [];
  final List<StaffTask> staffTasks = [];

  ClinicStateEngine() {
    _currentPersona = personas[0]; // Starts on Admin Aditia Rach
    _seedInitialData();
  }

  void switchPersona(PersonaAccount newPersona) {
    _currentPersona = newPersona;
    _selectedTab = 0; // Reset tab on persona change
    notifyListeners();
  }

  void switchTab(int index) {
    _selectedTab = index;
    notifyListeners();
  }

  void _seedInitialData() {
    // 1. Initial Patients
    final siti = Patient(
      id = 'P-001',
      nik = '3171012304990001',
      name = 'Siti Rahma',
      phone = '081234567890',
      gender = 'Perempuan',
      birthDate = '15/04/1996',
      payerType = PayerType.bpjs,
      bpjsNumber = '000123456789',
      isBpjsActive = true,
      allergy = 'Alergi Dingin, Penisilin',
    );

    final budi = Patient(
      id = 'P-002',
      nik = '3171051108880002',
      name = 'Budi Santoso',
      phone = '085711223344',
      gender = 'Laki-laki',
      birthDate = '11/08/1988',
      payerType = PayerType.umum,
      isBpjsActive = false,
      allergy = 'Tidak ada riwayat alergi',
    );

    patients.addAll([siti, budi]);

    // 2. Initial Visit: Siti Rahma pre-enrolled in A-001
    visits.add(
      Visit(
        id = 'V-001',
        queueNumber = 'A-001',
        patient = siti,
        stage = VisitStage.triage,
        registeredAt = DateTime.now().subtract(const Duration(minutes: 25)),
        consultationFee = 50000,
      ),
    );

    // 3. Initial Inventory
    inventory.addAll([
      InventoryItem(
        id = 'MED-01',
        name = 'Paracetamol 500mg',
        batch = 'PCT-01',
        stock = 12,
        minStock = 20,
        unit = 'Tablet',
        unitPrice = 1500,
        expiryDate = '14/12/2026',
      ),
      InventoryItem(
        id = 'MED-02',
        name = 'Amoxicillin 500mg',
        batch = 'AMX-02',
        stock = 80,
        minStock = 30,
        unit = 'Tablet',
        unitPrice = 2500,
        expiryDate = '20/09/2025',
      ),
      InventoryItem(
        id = 'MED-03',
        name = 'Omeprazole 20mg',
        batch = 'OMP-01',
        stock = 5,
        minStock = 15,
        unit = 'Kapsul',
        unitPrice = 3500,
        expiryDate = '05/03/2026',
      ),
      InventoryItem(
        id = 'MED-04',
        name = 'Amlodipine 10mg',
        batch = 'AML-03',
        stock = 50,
        minStock = 10,
        unit = 'Tablet',
        unitPrice = 3000,
        expiryDate = '10/06/2027',
      ),
    ]);

    // 4. Initial Opname Request
    opnameRequests.add(
      StockOpnameRequest(
        id = 'OPN-001',
        date = DateTime.now(),
        medicineId = 'MED-01',
        medicineName = 'Paracetamol 500mg',
        systemStock = 15,
        physicalCount = 12,
        variance = -3,
        reason = 'Kemasan strip rusak dan remuk di rak penyimpanan',
        submittedBy = 'Muhammad Haikal Siregar, S.Farm',
      ),
    );

    // 5. Initial Staff Task
    staffTasks.add(
      StaffTask(
        id = 'TSK-001',
        createdAt = DateTime.now(),
        assignee = 'Muhammad Haikal Siregar, S.Farm',
        taskInstruction = 'Lakukan percepatan pemesanan Paracetamol & Omeprazole ke PBF Kimia Farma.',
        priority = 'Tinggi',
      ),
    );
  }

  // --- ACTIONS: ADMIN ---
  Patient registerNewPatient({
    required String name,
    required String nik,
    required String phone,
    required String gender,
    required String birthDate,
    required PayerType payerType,
    String bpjsNumber = '',
    bool isBpjsActive = true,
  }) {
    final newPatient = Patient(
      id = 'P-${patients.length + 1}',
      nik = nik,
      name = name,
      phone = phone,
      gender = gender,
      birthDate = birthDate,
      payerType = payerType,
      bpjsNumber = bpjsNumber,
      isBpjsActive = isBpjsActive,
    );
    patients.add(newPatient);
    notifyListeners();
    return newPatient;
  }

  Visit createVisit(Patient patient) {
    final prefix = patient.payerType == PayerType.bpjs ? 'A' : 'U';
    final count = visits.where((v) => v.queueNumber.startsWith(prefix)).length + 1;
    final queueNumber = '$prefix-${count.toString().padLeft(3, '0')}';

    final newVisit = Visit(
      id = 'V-${DateTime.now().millisecondsSinceEpoch}',
      queueNumber = queueNumber,
      patient = patient,
      stage = VisitStage.triage,
      registeredAt = DateTime.now(),
      consultationFee = 50000,
    );
    visits.add(newVisit);
    notifyListeners();
    return newVisit;
  }

  void checkoutPayment(Visit visit) {
    visit.isPaid = true;
    visit.stage = VisitStage.selesai;
    notifyListeners();
  }

  // --- ACTIONS: PERAWAT ---
  void callPatient(String queueNum) {
    callingQueueNumber = queueNum;
    final v = visits.firstWhere((element) => element.queueNumber == queueNum);
    v.stage = VisitStage.triage;
    notifyListeners();
  }

  void stopCalling() {
    callingQueueNumber = null;
    notifyListeners();
  }

  void skipPatient(Visit visit) {
    visit.stage = VisitStage.dilewati;
    if (callingQueueNumber == visit.queueNumber) callingQueueNumber = null;
    notifyListeners();
  }

  void cancelVisit(Visit visit) {
    visit.stage = VisitStage.batal;
    if (callingQueueNumber == visit.queueNumber) callingQueueNumber = null;
    notifyListeners();
  }

  void recallPatient(Visit visit) {
    visit.stage = VisitStage.triage;
    notifyListeners();
  }

  void submitVitalSigns(Visit visit, VitalSigns vitals) {
    visit.vitalSigns = vitals;
    visit.stage = VisitStage.dokter;
    if (callingQueueNumber == visit.queueNumber) callingQueueNumber = null;
    notifyListeners();
  }

  // --- ACTIONS: DOKTER ---
  void submitConsultation({
    required Visit visit,
    required MedicalRecord record,
  }) {
    visit.medicalRecord = record;
    visit.hasSubstitutionAlert = false;
    visit.substitutionRequestNote = null;

    if (record.needsPrescription && record.prescriptionItems.isNotEmpty()) {
      int totalDrug = 0;
      for (final item in record.prescriptionItems) {
        totalDrug += item.quantity * item.unitPrice;
      }
      visit.drugFee = totalDrug;
      visit.stage = VisitStage.farmasi;
    } else {
      visit.stage = VisitStage.kasir;
    }
    notifyListeners();
  }

  // --- ACTIONS: FARMASI ---
  void requestDrugSubstitution(Visit visit, String note) {
    visit.hasSubstitutionAlert = true;
    visit.substitutionRequestNote = note;
    visit.stage = VisitStage.dokter; // Sent back to doctor desk!
    notifyListeners();
  }

  void dispensePrescription(Visit visit) {
    if (visit.medicalRecord == null) return;

    for (final item in visit.medicalRecord!.prescriptionItems) {
      final med = inventory.firstWhere((m) => m.id == item.medicineId);
      med.stock = (med.stock - item.quantity).clamp(0, 99999);

      mutations.insert(
        0,
        StockMutation(
          id = 'MUT-${DateTime.now().millisecondsSinceEpoch}',
          timestamp = DateTime.now(),
          medicineName = med.name,
          type = 'Dispensing Resep',
          quantityChange = -item.quantity,
          finalStock = med.stock,
          note = 'Resep ${visit.queueNumber} (${visit.patient.name})',
          operatorName = _currentPersona.name,
        ),
      );
    }

    visit.stage = VisitStage.kasir;
    notifyListeners();
  }

  void submitStockOpname({
    required String medicineId,
    required int physicalCount,
    required String reason,
  }) {
    final med = inventory.firstWhere((m) => m.id == medicineId);
    final variance = physicalCount - med.stock;

    opnameRequests.insert(
      0,
      StockOpnameRequest(
        id = 'OPN-${DateTime.now().millisecondsSinceEpoch}',
        date = DateTime.now(),
        medicineId = med.id,
        medicineName = med.name,
        systemStock = med.stock,
        physicalCount = physicalCount,
        variance = variance,
        reason = reason,
        submittedBy = _currentPersona.name,
      ),
    );
    notifyListeners();
  }

  // --- ACTIONS: KEPALA KLINIK ---
  void reviewStockOpname(StockOpnameRequest request, bool isApproved, String note) {
    request.status = isApproved ? 'Disetujui' : 'Ditolak';
    request.managerNote = note;

    if (isApproved) {
      final med = inventory.firstWhere((m) => m.id == request.medicineId);
      med.stock = request.physicalCount;

      mutations.insert(
        0,
        StockMutation(
          id = 'MUT-${DateTime.now().millisecondsSinceEpoch}',
          timestamp = DateTime.now(),
          medicineName = med.name,
          type = 'Penyesuaian Opname',
          quantityChange = request.variance,
          finalStock = med.stock,
          note = 'Approval Kepala Klinik: ${request.reason}',
          operatorName = _currentPersona.name,
        ),
      );
    }
    notifyListeners();
  }

  void assignTask({
    required String assignee,
    required String instruction,
    required String priority,
  }) {
    staffTasks.insert(
      0,
      StaffTask(
        id = 'TSK-${DateTime.now().millisecondsSinceEpoch}',
        createdAt = DateTime.now(),
        assignee = assignee,
        taskInstruction = instruction,
        priority = priority,
      ),
    );
    notifyListeners();
  }
}

// ==============================================================================
// 3. MAIN WIDGET & THEME
// ==============================================================================

class KlinikRomanaApp extends StatelessWidget {
  const KlinikRomanaApp({super.key});

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'Klinik Romana',
      debugShowCheckedModeBanner: false,
      theme: ThemeData(
        useMaterial3: true,
        colorScheme: ColorScheme.fromSeed(
          seedColor: const Color(0xFF00796B),
          primary: const Color(0xFF00796B),
          secondary: const Color(0xFF00ACC1),
          surface: Colors.white,
        ),
        scaffoldBackgroundColor: const Color(0xFFF8F9FA),
        cardTheme: CardTheme(
          elevation: 1.5,
          shape: RoundedCornerShape(12),
          color: Colors.white,
        ),
        appBarTheme: const AppBarTheme(
          backgroundColor: Color(0xFF00796B),
          foregroundColor: Colors.white,
          elevation: 2,
        ),
      ),
      home: const ClinicMainScreen(),
    );
  }
}

class ClinicMainScreen extends StatefulWidget {
  const ClinicMainScreen({super.key});

  @override
  State<ClinicMainScreen> createState() => _ClinicMainScreenState();
}

class _ClinicMainScreenState extends State<ClinicMainScreen> {
  late final ClinicStateEngine _engine;

  @override
  void initState() {
    super.initState();
    _engine = ClinicStateEngine();
    _engine.addListener(_onStateChanged);
  }

  void _onStateChanged() {
    setState(() {});
  }

  @override
  void dispose() {
    _engine.removeListener(_onStateChanged);
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    final persona = _engine.currentPersona;

    return Scaffold(
      appBar: AppBar(
        title: Row(
          children: [
            Container(
              padding: const EdgeInsets.all(6),
              decoration: const BoxDecoration(
                color: Colors.white,
                shape: BoxShape.circle,
              ),
              child: const Icon(Icons.local_hospital, color: Color(0xFF00796B), size: 20),
            ),
            const SizedBox(width: 10),
            Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                const Text('Klinik Romana', style: TextStyle(fontSize: 16, fontWeight: FontWeight.bold)),
                Text(persona.name, style: const TextStyle(fontSize: 11, color: Colors.white70)),
              ],
            ),
          ],
        ),
        actions: [
          // Switch Persona Button
          PopupMenuButton<PersonaAccount>(
            tooltip: 'Ganti Akun Demo',
            onSelected: (selected) => _engine.switchPersona(selected),
            icon: Container(
              padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 5),
              decoration: BoxDecoration(
                color: Colors.white.withOpacity(0.2),
                borderRadius: BorderRadius.circular(20),
              ),
              child: Row(
                children: [
                  Icon(persona.icon, size: 16, color: Colors.white),
                  const SizedBox(width: 6),
                  Text(
                    persona.role.name.toUpperCase(),
                    style: const TextStyle(fontSize: 11, fontWeight: FontWeight.bold, color: Colors.white),
                  ),
                  const Icon(Icons.arrow_drop_down, size: 18, color: Colors.white),
                ],
              ),
            ),
            itemBuilder: (context) => _engine.personas.map((p) {
              return PopupMenuItem<PersonaAccount>(
                value: p,
                child: Row(
                  children: [
                    Icon(p.icon, color: const Color(0xFF00796B), size: 20),
                    const SizedBox(width: 10),
                    Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        Text(p.name, style: const TextStyle(fontWeight: FontWeight.bold, fontSize: 13)),
                        Text(p.title, style: const TextStyle(fontSize: 11, color: Colors.grey)),
                      ],
                    ),
                  ],
                ),
              );
            }).toList(),
          ),
          const SizedBox(width: 8),
        ],
      ),
      body: Column(
        children: [
          // Calling Patient Banner
          if (_engine.callingQueueNumber != null)
            Container(
              width: double.infinity,
              color: Colors.amber.shade800,
              padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 8),
              child: Row(
                mainAxisAlignment: MainAxisAlignment.spaceBetween,
                children: [
                  Row(
                    children: [
                      const Icon(Icons.volume_up, color: Colors.white, size: 20),
                      const SizedBox(width: 8),
                      Text(
                        'MEMANGGIL: Antrean ${_engine.callingQueueNumber}',
                        style: const TextStyle(color: Colors.white, fontWeight: FontWeight.bold, fontSize: 13),
                      ),
                    ],
                  ),
                  GestureDetector(
                    onTap: () => _engine.stopCalling(),
                    child: Container(
                      padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 2),
                      decoration: BoxDecoration(
                        color: Colors.black26,
                        borderRadius: BorderRadius.circular(4),
                      ),
                      child: const Text('Hentikan', style: TextStyle(color: Colors.white, fontSize: 11)),
                    ),
                  ),
                ],
              ),
            ),

          // Main Screen Content based on Active Persona
          Expanded(child: _buildPersonaView(persona.role)),
        ],
      ),
      bottomNavigationBar: _buildBottomNavigationBar(persona.role),
    );
  }

  Widget _buildPersonaView(PersonaRole role) {
    switch (role) {
      case PersonaRole.admin:
        return _AdminScreen(engine: _engine);
      case PersonaRole.perawat:
        return _PerawatScreen(engine: _engine);
      case PersonaRole.dokter:
        return _DokterScreen(engine: _engine);
      case PersonaRole.farmasi:
        return _FarmasiScreen(engine: _engine);
      case PersonaRole.kepala:
        return _KepalaKlinikScreen(engine: _engine);
    }
  }

  Widget _buildBottomNavigationBar(PersonaRole role) {
    List<BottomNavigationBarItem> items;
    switch (role) {
      case PersonaRole.admin:
        items = const [
          BottomNavigationBarItem(icon: Icon(Icons.person_add), label: 'Registrasi & Antrean'),
          BottomNavigationBarItem(icon: Icon(Icons.timeline), label: 'Status & Tracking'),
          BottomNavigationBarItem(icon: Icon(Icons.point_of_sale), label: 'Kasir & Pembayaran'),
        ];
        break;
      case PersonaRole.perawat:
        items = const [
          BottomNavigationBarItem(icon: Icon(Icons.queue), label: 'Antrean Triage'),
          BottomNavigationBarItem(icon: Icon(Icons.edit_note), label: 'Input TTV'),
          BottomNavigationBarItem(icon: Icon(Icons.table_chart), label: 'Rekap Vital Signs'),
        ];
        break;
      case PersonaRole.dokter:
        items = [
          BottomNavigationBarItem(
            icon: Stack(
              children: [
                const Icon(Icons.people),
                if (_engine.visits.any((v) => v.hasSubstitutionAlert))
                  Positioned(
                    right: 0,
                    top: 0,
                    child: Container(
                      width: 8,
                      height: 8,
                      decoration: const BoxDecoration(color: Colors.red, shape: BoxShape.circle),
                    ),
                  ),
              ],
            ),
            label: 'Antrean Pasien',
          ),
          const BottomNavigationBarItem(icon: Icon(Icons.medical_information), label: 'Konsultasi & Resep'),
          const BottomNavigationBarItem(icon: Icon(Icons.history_edu), label: 'Riwayat EMR'),
        ];
        break;
      case PersonaRole.farmasi:
        items = const [
          BottomNavigationBarItem(icon: Icon(Icons.receipt_long), label: 'Antrean Resep'),
          BottomNavigationBarItem(icon: Icon(Icons.inventory_2), label: 'Katalog & Alert'),
          BottomNavigationBarItem(icon: Icon(Icons.fact_check), label: 'Stok Opname'),
        ];
        break;
      case PersonaRole.kepala:
        items = const [
          BottomNavigationBarItem(icon: Icon(Icons.dashboard), label: 'Dashboard KPI'),
          BottomNavigationBarItem(icon: Icon(Icons.analytics), label: 'Analisis Operasional'),
          BottomNavigationBarItem(icon: Icon(Icons.approval), label: 'Persetujuan & Tugas'),
        ];
        break;
    }

    return BottomNavigationBar(
      currentIndex: _engine.selectedTab,
      onTap: (index) => _engine.switchTab(index),
      selectedItemColor: const Color(0xFF00796B),
      unselectedItemColor: Colors.grey,
      selectedLabelStyle: const TextStyle(fontWeight: FontWeight.bold, fontSize: 11),
      unselectedLabelStyle: const TextStyle(fontSize: 11),
      type: BottomNavigationBarType.fixed,
      items: items,
    );
  }
}

// ==============================================================================
// 4. COMMON HCI COMPONENT: PATIENT PROGRESS STEPPER
// ==============================================================================

class PatientProgressStepper extends StatelessWidget {
  final VisitStage currentStage;

  const PatientProgressStepper({super.key, required this.currentStage});

  int _getStepIndex(VisitStage stage) {
    switch (stage) {
      case VisitStage.registrasi:
        return 0;
      case VisitStage.triage:
        return 1;
      case VisitStage.dokter:
        return 2;
      case VisitStage.farmasi:
        return 3;
      case VisitStage.kasir:
      case VisitStage.selesai:
        return 4;
      case VisitStage.dilewati:
      case VisitStage.batal:
        return -1;
    }
  }

  @override
  Widget build(BuildContext context) {
    if (currentStage == VisitStage.dilewati) {
      return Container(
        padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 3),
        decoration: BoxDecoration(color: Colors.amber.shade100, borderRadius: BorderRadius.circular(4)),
        child: Text('Antrean Dilewati (Absent)', style: TextStyle(color: Colors.amber.shade900, fontSize: 11, fontWeight: FontWeight.bold)),
      );
    }

    if (currentStage == VisitStage.batal) {
      return Container(
        padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 3),
        decoration: BoxDecoration(color: Colors.red.shade100, borderRadius: BorderRadius.circular(4)),
        child: Text('Kunjungan Dibatalkan', style: TextStyle(color: Colors.red.shade900, fontSize: 11, fontWeight: FontWeight.bold)),
      );
    }

    final currentIndex = _getStepIndex(currentStage);
    final steps = ['Reg', 'Triage', 'Dokter', 'Farmasi', 'Selesai'];

    return Row(
      children: List.generate(steps.length * 2 - 1, (index) {
        if (index.isOdd) {
          final lineActive = (index ~/ 2) < currentIndex;
          return Expanded(
            child: Container(
              height: 2,
              color: lineActive ? const Color(0xFF00796B) : Colors.grey.shade300,
            ),
          );
        }

        final stepIndex = index ~/ 2;
        final isDone = stepIndex < currentIndex;
        final isCurrent = stepIndex == currentIndex;

        return Column(
          children: [
            Container(
              width: 18,
              height: 18,
              decoration: BoxDecoration(
                shape: BoxShape.circle,
                color: isDone
                    ? const Color(0xFF00796B)
                    : isCurrent
                        ? const Color(0xFF00ACC1)
                        : Colors.grey.shade300,
              ),
              child: Center(
                child: isDone
                    ? const Icon(Icons.check, size: 12, color: Colors.white)
                    : Text(
                        '${stepIndex + 1}',
                        style: TextStyle(
                          fontSize: 10,
                          fontWeight: FontWeight.bold,
                          color: isCurrent ? Colors.white : Colors.grey.shade700,
                        ),
                      ),
              ),
            ),
            const SizedBox(height: 2),
            Text(
              steps[stepIndex],
              style: TextStyle(
                fontSize: 9,
                fontWeight: isCurrent ? FontWeight.bold : FontWeight.normal,
                color: isCurrent ? const Color(0xFF00796B) : Colors.grey.shade600,
              ),
            ),
          ],
        );
      }),
    );
  }
}

// ==============================================================================
// 5. ROLE 1: ADMIN PENDAFTARAN & KASIR (Aditia Rach)
// ==============================================================================

class _AdminScreen extends StatefulWidget {
  final ClinicStateEngine engine;
  const _AdminScreen({required this.engine});

  @override
  State<_AdminScreen> createState() => _AdminScreenState();
}

class _AdminScreenState extends State<_AdminScreen> {
  String searchKeyword = '';

  @override
  Widget build(BuildContext context) {
    switch (widget.engine.selectedTab) {
      case 0:
        return _buildTabRegistrasi();
      case 1:
        return _buildTabTracking();
      case 2:
        return _buildTabKasir();
      default:
        return const SizedBox();
    }
  }

  // TAB 1: REGISTRASI & ANTREAN
  Widget _buildTabRegistrasi() {
    final filtered = widget.engine.patients.where((p) {
      return p.name.toLowerCase().contains(searchKeyword.toLowerCase()) || p.nik.contains(searchKeyword);
    }).toList();

    return Padding(
      padding: const EdgeInsets.all(14.0),
      child: Column(
        children: [
          Row(
            children: [
              Expanded(
                child: TextField(
                  decoration: InputDecoration(
                    hintText: 'Cari pasien (NIK / Nama)...',
                    prefixIcon: const Icon(Icons.search),
                    filled: true,
                    fillColor: Colors.white,
                    contentPadding: const EdgeInsets.symmetric(vertical: 10),
                    border: OutlineInputBorder(borderRadius: BorderRadius.circular(10), borderSide: BorderSide.none),
                  ),
                  onChanged: (val) => setState(() => searchKeyword = val),
                ),
              ),
              const SizedBox(width: 10),
              ElevatedButton.icon(
                style: ElevatedButton.styleFrom(
                  backgroundColor: const Color(0xFF00796B),
                  foregroundColor: Colors.white,
                  shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(10)),
                  padding: const EdgeInsets.symmetric(horizontal: 14, vertical: 12),
                ),
                onPressed: () => _showNewPatientModal(),
                icon: const Icon(Icons.add, size: 18),
                label: const Text('Pasien Baru'),
              ),
            ],
          ),
          const SizedBox(height: 12),
          Expanded(
            child: ListView.builder(
              itemCount: filtered.length,
              itemBuilder: (context, idx) {
                final patient = filtered[idx];
                return Card(
                  margin: const EdgeInsets.only(bottom: 10),
                  child: Padding(
                    padding: const EdgeInsets.all(12.0),
                    child: Row(
                      crossAxisAlignment: CrossAxisAlignment.center,
                      children: [
                        CircleAvatar(
                          backgroundColor: const Color(0xFFE0F2F1),
                          child: Text(
                            patient.name.substring(0, 1),
                            style: const TextStyle(fontWeight: FontWeight.bold, color: Color(0xFF00796B)),
                          ),
                        ),
                        const SizedBox(width: 12),
                        Expanded(
                          child: Column(
                            crossAxisAlignment: CrossAxisAlignment.start,
                            children: [
                              Row(
                                children: [
                                  Text(patient.name, style: const TextStyle(fontWeight: FontWeight.bold, fontSize: 14)),
                                  const SizedBox(width: 6),
                                  Container(
                                    padding: const EdgeInsets.symmetric(horizontal: 6, vertical: 2),
                                    decoration: BoxDecoration(
                                      color: patient.payerType == PayerType.bpjs ? Colors.green.shade50 : Colors.blue.shade50,
                                      borderRadius: BorderRadius.circular(4),
                                    ),
                                    child: Text(
                                      patient.payerType == PayerType.bpjs ? 'BPJS' : 'UMUM',
                                      style: TextStyle(
                                        fontSize: 10,
                                        fontWeight: FontWeight.bold,
                                        color: patient.payerType == PayerType.bpjs ? Colors.green.shade800 : Colors.blue.shade800,
                                      ),
                                    ),
                                  ),
                                ],
                              ),
                              Text('NIK: ${patient.nik} • ${patient.gender}', style: const TextStyle(fontSize: 12, color: Colors.grey)),
                              if (patient.payerType == PayerType.bpjs)
                                Text('No. Kartu: ${patient.bpjsNumber}', style: const TextStyle(fontSize: 11, color: Colors.teal)),
                            ],
                          ),
                        ),
                        ElevatedButton(
                          style: ElevatedButton.styleFrom(
                            backgroundColor: const Color(0xFF00796B),
                            foregroundColor: Colors.white,
                            padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 6),
                          ),
                          onPressed: () {
                            final visit = widget.engine.createVisit(patient);
                            ScaffoldMessenger.of(context).showSnackBar(
                              SnackBar(
                                content: Text('Tiket Antrean ${visit.queueNumber} dibuat untuk ${patient.name}'),
                                backgroundColor: const Color(0xFF00796B),
                              ),
                            );
                          },
                          child: const Text('Buat Antrean', style: TextStyle(fontSize: 12)),
                        ),
                      ],
                    ),
                  ),
                );
              },
            ),
          ),
        ],
      ),
    );
  }

  void _showNewPatientModal() {
    final nameCtrl = TextEditingController();
    final nikCtrl = TextEditingController();
    final phoneCtrl = TextEditingController();
    PayerType selectedPayer = PayerType.bpjs;
    final bpjsCtrl = TextEditingController(text: '000${DateTime.now().millisecondsSinceEpoch % 100000000}');
    bool isBpjsActive = true;

    showModalBottomSheet(
      context: context,
      isScrollControlled: true,
      builder: (ctx) {
        return StatefulBuilder(
          builder: (ctx, setModalState) {
            return Padding(
              padding: EdgeInsets.only(bottom: MediaQuery.of(ctx).viewInsets.bottom, left: 16, right: 16, top: 16),
              child: SingleChildScrollView(
                child: Column(
                  mainAxisSize: MainAxisSize.min,
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Row(
                      mainAxisAlignment: MainAxisAlignment.spaceBetween,
                      children: [
                        const Text('Registrasi Pasien Baru', style: TextStyle(fontSize: 16, fontWeight: FontWeight.bold)),
                        IconButton(onPressed: () => Navigator.pop(ctx), icon: const Icon(Icons.close)),
                      ],
                    ),
                    const Divider(),
                    TextField(controller: nameCtrl, decoration: const InputDecoration(labelText: 'Nama Lengkap Pasien *')),
                    TextField(controller: nikCtrl, decoration: const InputDecoration(labelText: 'NIK (16 Digit) *'), keyboardType: TextInputType.number),
                    TextField(controller: phoneCtrl, decoration: const InputDecoration(labelText: 'Nomor WhatsApp / HP')),
                    const SizedBox(height: 12),
                    const Text('Jalur Pembiayaan & JKN:', style: TextStyle(fontWeight: FontWeight.bold, fontSize: 13)),
                    Row(
                      children: [
                        Radio<PayerType>(
                          value: PayerType.bpjs,
                          groupValue: selectedPayer,
                          onChanged: (val) => setModalState(() => selectedPayer = val!),
                        ),
                        const Text('BPJS Kesehatan'),
                        Radio<PayerType>(
                          value: PayerType.umum,
                          groupValue: selectedPayer,
                          onChanged: (val) => setModalState(() => selectedPayer = val!),
                        ),
                        const Text('Umum (Tarif Rp 50.000)'),
                      ],
                    ),
                    if (selectedPayer == PayerType.bpjs) ...[
                      TextField(controller: bpjsCtrl, decoration: const InputDecoration(labelText: 'Nomor Kartu BPJS')),
                      const SizedBox(height: 8),
                      Row(
                        mainAxisAlignment: MainAxisAlignment.spaceBetween,
                        children: [
                          const Text('Simulasi Status BPJS:', style: TextStyle(fontSize: 12)),
                          Switch(
                            value: isBpjsActive,
                            onChanged: (val) => setModalState(() => isBpjsActive = val),
                          ),
                          Text(
                            isBpjsActive ? 'JKN AKTIF' : 'NON-AKTIF',
                            style: TextStyle(
                              fontSize: 12,
                              fontWeight: FontWeight.bold,
                              color: isBpjsActive ? Colors.green : Colors.red,
                            ),
                          ),
                        ],
                      ),
                    ],
                    const SizedBox(height: 16),
                    SizedBox(
                      width: double.infinity,
                      child: ElevatedButton(
                        style: ElevatedButton.styleFrom(
                          backgroundColor: const Color(0xFF00796B),
                          foregroundColor: Colors.white,
                          padding: const EdgeInsets.symmetric(vertical: 12),
                        ),
                        onPressed: () {
                          if (nameCtrl.text.isEmpty || nikCtrl.text.isEmpty) return;
                          final patient = widget.engine.registerNewPatient(
                            name: nameCtrl.text,
                            nik: nikCtrl.text,
                            phone: phoneCtrl.text,
                            gender: 'Laki-laki',
                            birthDate: '01/01/1990',
                            payerType: selectedPayer,
                            bpjsNumber: bpjsCtrl.text,
                            isBpjsActive: isBpjsActive,
                          );
                          final visit = widget.engine.createVisit(patient);
                          Navigator.pop(ctx);
                          ScaffoldMessenger.of(context).showSnackBar(
                            SnackBar(content: Text('Pasien terdaftar & Antrean ${visit.queueNumber} dibuat')),
                          );
                        },
                        child: const Text('Simpan & Terbitkan Antrean'),
                      ),
                    ),
                    const SizedBox(height: 20),
                  ],
                ),
              ),
            );
          },
        );
      },
    );
  }

  // TAB 2: STATUS PASIEN & TRACKING
  Widget _buildTabTracking() {
    final activeVisits = widget.engine.visits.where((v) => v.stage != VisitStage.batal).toList();

    return Padding(
      padding: const EdgeInsets.all(14.0),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Row(
            mainAxisAlignment: MainAxisAlignment.spaceBetween,
            children: [
              const Text('Monitoring Posisi Pasien', style: TextStyle(fontSize: 15, fontWeight: FontWeight.bold)),
              Text('${activeVisits.length} Kunjungan Hari Ini', style: const TextStyle(fontSize: 12, color: Colors.grey)),
            ],
          ),
          const SizedBox(height: 10),
          Expanded(
            child: ListView.builder(
              itemCount: activeVisits.length,
              itemBuilder: (context, idx) {
                final visit = activeVisits[idx];
                return Card(
                  margin: const EdgeInsets.only(bottom: 12),
                  child: Padding(
                    padding: const EdgeInsets.all(14.0),
                    child: Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        Row(
                          mainAxisAlignment: MainAxisAlignment.spaceBetween,
                          children: [
                            Text(
                              '${visit.queueNumber} - ${visit.patient.name}',
                              style: const TextStyle(fontWeight: FontWeight.bold, fontSize: 14),
                            ),
                            Container(
                              padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 2),
                              decoration: BoxDecoration(
                                color: const Color(0xFF00796B).withOpacity(0.1),
                                borderRadius: BorderRadius.circular(4),
                              ),
                              child: Text(
                                visit.stage.name.toUpperCase(),
                                style: const TextStyle(fontSize: 10, fontWeight: FontWeight.bold, color: Color(0xFF00796B)),
                              ),
                            ),
                          ],
                        ),
                        const SizedBox(height: 10),
                        PatientProgressStepper(currentStage: visit.stage),
                      ],
                    ),
                  ),
                );
              },
            ),
          ),
        ],
      ),
    );
  }

  // TAB 3: KASIR & PEMBAYARAN
  Widget _buildTabKasir() {
    final kasirQueue = widget.engine.visits.where((v) => v.stage == VisitStage.kasir).toList();

    return Padding(
      padding: const EdgeInsets.all(14.0),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          const Text('Antrean Kasir & Pembayaran', style: TextStyle(fontSize: 15, fontWeight: FontWeight.bold)),
          const SizedBox(height: 10),
          if (kasirQueue.isEmpty)
            const Expanded(
              child: Center(
                child: Text('Tidak ada pasien menunggu di kasir.', style: TextStyle(color: Colors.grey)),
              ),
            )
          else
            Expanded(
              child: ListView.builder(
                itemCount: kasirQueue.length,
                itemBuilder: (context, idx) {
                  final visit = kasirQueue[idx];
                  return Card(
                    margin: const EdgeInsets.only(bottom: 10),
                    child: Padding(
                      padding: const EdgeInsets.all(12.0),
                      child: Column(
                        crossAxisAlignment: CrossAxisAlignment.start,
                        children: [
                          Row(
                            mainAxisAlignment: MainAxisAlignment.spaceBetween,
                            children: [
                              Text('${visit.queueNumber} - ${visit.patient.name}', style: const TextStyle(fontWeight: FontWeight.bold)),
                              Text(
                                visit.patient.payerType == PayerType.bpjs ? 'Klaim BPJS' : 'Rp ${visit.totalFee}',
                                style: const TextStyle(fontWeight: FontWeight.bold, color: Color(0xFF00796B)),
                              ),
                            ],
                          ),
                          const SizedBox(height: 6),
                          Text('Jasa Konsultasi: Rp ${visit.consultationFee}', style: const TextStyle(fontSize: 12, color: Colors.grey)),
                          Text('Obat / Farmasi: Rp ${visit.drugFee}', style: const TextStyle(fontSize: 12, color: Colors.grey)),
                          const SizedBox(height: 8),
                          ElevatedButton.icon(
                            style: ElevatedButton.styleFrom(
                              backgroundColor: const Color(0xFF00796B),
                              foregroundColor: Colors.white,
                              minimumSize: const Size(double.infinity, 36),
                            ),
                            onPressed: () => _showPaymentModal(visit),
                            icon: const Icon(Icons.payment, size: 16),
                            label: const Text('Proses Pembayaran'),
                          ),
                        ],
                      ),
                    ),
                  );
                },
              ),
            ),
        ],
      ),
    );
  }

  void _showPaymentModal(Visit visit) {
    showDialog(
      context: context,
      builder: (ctx) {
        return AlertDialog(
          title: Text('Checkout Kasir: ${visit.queueNumber}'),
          content: Column(
            mainAxisSize: MainAxisSize.min,
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Text('Pasien: ${visit.patient.name}'),
              Text('Penjamin: ${visit.patient.payerType == PayerType.bpjs ? "BPJS Kesehatan (JKN)" : "Umum (Mandiri)"}'),
              const Divider(),
              Row(
                mainAxisAlignment: MainAxisAlignment.spaceBetween,
                children: [
                  const Text('Konsultasi Dokter:'),
                  Text('Rp ${visit.consultationFee}'),
                ],
              ),
              Row(
                mainAxisAlignment: MainAxisAlignment.spaceBetween,
                children: [
                  const Text('Biaya Farmasi:'),
                  Text('Rp ${visit.drugFee}'),
                ],
              ),
              const Divider(),
              Row(
                mainAxisAlignment: MainAxisAlignment.spaceBetween,
                children: [
                  const Text('Total Tagihan:', style: TextStyle(fontWeight: FontWeight.bold)),
                  Text('Rp ${visit.totalFee}', style: const TextStyle(fontWeight: FontWeight.bold, color: Color(0xFF00796B), fontSize: 16)),
                ],
              ),
              if (visit.patient.payerType == PayerType.bpjs) ...[
                const SizedBox(height: 8),
                Container(
                  padding: const EdgeInsets.all(8),
                  decoration: BoxDecoration(color: Colors.green.shade50, borderRadius: BorderRadius.circular(6)),
                  child: const Text('✅ Ditanggung BPJS JKN (Iur Pasien: Rp 0)', style: TextStyle(color: Colors.green, fontSize: 11, fontWeight: FontWeight.bold)),
                ),
              ],
            ],
          ),
          actions: [
            TextButton(onPressed: () => Navigator.pop(ctx), child: const Text('Batal')),
            ElevatedButton(
              style: ElevatedButton.styleFrom(backgroundColor: const Color(0xFF00796B), foregroundColor: Colors.white),
              onPressed: () {
                widget.engine.checkoutPayment(visit);
                Navigator.pop(ctx);
                ScaffoldMessenger.of(context).showSnackBar(
                  SnackBar(content: Text('Kunjungan ${visit.queueNumber} (${visit.patient.name}) Selesai!')),
                );
              },
              child: const Text('Proses & Selesaikan'),
            ),
          ],
        );
      },
    );
  }
}

// ==============================================================================
// 6. ROLE 2: PERAWAT - TRIAGE & TTV (Rehan Pratama)
// ==============================================================================

class _PerawatScreen extends StatefulWidget {
  final ClinicStateEngine engine;
  const _PerawatScreen({required this.engine});

  @override
  State<_PerawatScreen> createState() => _PerawatScreenState();
}

class _PerawatScreenState extends State<_PerawatScreen> {
  Visit? selectedVisitForTtv;

  // TTV Form Controllers
  final sysCtrl = TextEditingController(text: '120');
  final diaCtrl = TextEditingController(text: '80');
  final hrCtrl = TextEditingController(text: '78');
  final tempCtrl = TextEditingController(text: '36.6');
  final rrCtrl = TextEditingController(text: '18');
  final weightCtrl = TextEditingController(text: '60');
  final complaintCtrl = TextEditingController();

  @override
  Widget build(BuildContext context) {
    switch (widget.engine.selectedTab) {
      case 0:
        return _buildTabAntreanTriage();
      case 1:
        return _buildTabInputTtv();
      case 2:
        return _buildTabRekapVitals();
      default:
        return const SizedBox();
    }
  }

  // TAB 1: ANTREAN TRIAGE (PANGGIL / SKIP / BATAL)
  Widget _buildTabAntreanTriage() {
    final triageQueue = widget.engine.visits.where((v) => v.stage == VisitStage.triage || v.stage == VisitStage.dilewati).toList();

    return Padding(
      padding: const EdgeInsets.all(14.0),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Row(
            mainAxisAlignment: MainAxisAlignment.spaceBetween,
            children: [
              const Text('Antrean Menunggu Triage', style: TextStyle(fontSize: 15, fontWeight: FontWeight.bold)),
              Text('${triageQueue.length} Pasien', style: const TextStyle(fontSize: 12, color: Colors.grey)),
            ],
          ),
          const SizedBox(height: 10),
          if (triageQueue.isEmpty)
            const Expanded(
              child: Center(child: Text('Tidak ada antrean triage saat ini.', style: TextStyle(color: Colors.grey))),
            )
          else
            Expanded(
              child: ListView.builder(
                itemCount: triageQueue.length,
                itemBuilder: (context, idx) {
                  final visit = triageQueue[idx];
                  final isSkipped = visit.stage == VisitStage.dilewati;

                  return Card(
                    margin: const EdgeInsets.only(bottom: 12),
                    child: Padding(
                      padding: const EdgeInsets.all(12.0),
                      child: Column(
                        crossAxisAlignment: CrossAxisAlignment.start,
                        children: [
                          Row(
                            mainAxisAlignment: MainAxisAlignment.spaceBetween,
                            children: [
                              Row(
                                children: [
                                  Text(visit.queueNumber, style: const TextStyle(fontSize: 16, fontWeight: FontWeight.bold, color: Color(0xFF00796B))),
                                  const SizedBox(width: 8),
                                  Text(visit.patient.name, style: const TextStyle(fontSize: 14, fontWeight: FontWeight.bold)),
                                ],
                              ),
                              if (isSkipped)
                                Container(
                                  padding: const EdgeInsets.symmetric(horizontal: 6, vertical: 2),
                                  decoration: BoxDecoration(color: Colors.amber.shade100, borderRadius: BorderRadius.circular(4)),
                                  child: const Text('DILEWATI', style: TextStyle(fontSize: 10, fontWeight: FontWeight.bold, color: Colors.brown)),
                                ),
                            ],
                          ),
                          const SizedBox(height: 4),
                          Text('NIK: ${visit.patient.nik} • ${visit.patient.gender} • HP: ${visit.patient.phone}', style: const TextStyle(fontSize: 11, color: Colors.grey)),
                          const SizedBox(height: 10),
                          Row(
                            children: [
                              // Panggil Button
                              Expanded(
                                child: OutlinedButton.icon(
                                  style: OutlinedButton.styleFrom(
                                    foregroundColor: const Color(0xFF00796B),
                                    side: const BorderSide(color: Color(0xFF00796B)),
                                    padding: const EdgeInsets.symmetric(vertical: 6),
                                  ),
                                  onPressed: () => widget.engine.callPatient(visit.queueNumber),
                                  icon: const Icon(Icons.volume_up, size: 14),
                                  label: const Text('Panggil', style: TextStyle(fontSize: 11)),
                                ),
                              ),
                              const SizedBox(width: 6),
                              // Skip Button
                              Expanded(
                                child: OutlinedButton.icon(
                                  style: OutlinedButton.styleFrom(
                                    foregroundColor: Colors.amber.shade900,
                                    side: BorderSide(color: Colors.amber.shade700),
                                    padding: const EdgeInsets.symmetric(vertical: 6),
                                  ),
                                  onPressed: () => isSkipped ? widget.engine.recallPatient(visit) : widget.engine.skipPatient(visit),
                                  icon: const Icon(Icons.skip_next, size: 14),
                                  label: Text(isSkipped ? 'Panggil Balik' : 'Lewati', style: const TextStyle(fontSize: 11)),
                                ),
                              ),
                              const SizedBox(width: 6),
                              // Input TTV Button
                              Expanded(
                                child: ElevatedButton.icon(
                                  style: ElevatedButton.styleFrom(
                                    backgroundColor: const Color(0xFF00796B),
                                    foregroundColor: Colors.white,
                                    padding: const EdgeInsets.symmetric(vertical: 6),
                                  ),
                                  onPressed: () {
                                    setState(() => selectedVisitForTtv = visit);
                                    widget.engine.switchTab(1); // Jump to Tab 2 Form Input TTV
                                  },
                                  icon: const Icon(Icons.edit, size: 14),
                                  label: const Text('Input TTV', style: TextStyle(fontSize: 11)),
                                ),
                              ),
                            ],
                          ),
                        ],
                      ),
                    ),
                  );
                },
              ),
            ),
        ],
      ),
    );
  }

  // TAB 2: FORM INPUT TANDA VITAL (TTV)
  Widget _buildTabInputTtv() {
    final targetVisit = selectedVisitForTtv ?? widget.engine.visits.firstWhere((v) => v.stage == VisitStage.triage, orElse: () => widget.engine.visits.first);

    return SingleChildScrollView(
      padding: const EdgeInsets.all(14.0),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Container(
            padding: const EdgeInsets.all(12),
            decoration: BoxDecoration(
              color: const Color(0xFF00796B).withOpacity(0.08),
              borderRadius: BorderRadius.circular(8),
            ),
            child: Row(
              mainAxisAlignment: MainAxisAlignment.spaceBetween,
              children: [
                Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Text('Pasien: ${targetVisit.queueNumber} - ${targetVisit.patient.name}', style: const TextStyle(fontWeight: FontWeight.bold, fontSize: 14)),
                    Text('Alergi: ${targetVisit.patient.allergy}', style: TextStyle(fontSize: 11, color: Colors.red.shade700, fontWeight: FontWeight.bold)),
                  ],
                ),
                ElevatedButton(
                  onPressed: () => widget.engine.switchTab(0),
                  child: const Text('Ganti Pasien', style: TextStyle(fontSize: 11)),
                ),
              ],
            ),
          ),
          const SizedBox(height: 12),
          const Text('Tanda-Tanda Vital (TTV) Pasien:', style: TextStyle(fontWeight: FontWeight.bold, fontSize: 13)),
          const SizedBox(height: 10),
          Row(
            children: [
              Expanded(
                child: TextField(
                  controller: sysCtrl,
                  decoration: const InputDecoration(labelText: 'Sistolik (mmHg)', border: OutlineInputBorder()),
                  keyboardType: TextInputType.number,
                ),
              ),
              const SizedBox(width: 8),
              Expanded(
                child: TextField(
                  controller: diaCtrl,
                  decoration: const InputDecoration(labelText: 'Diastolik (mmHg)', border: OutlineInputBorder()),
                  keyboardType: TextInputType.number,
                ),
              ),
            ],
          ),
          const SizedBox(height: 10),
          Row(
            children: [
              Expanded(
                child: TextField(
                  controller: hrCtrl,
                  decoration: const InputDecoration(labelText: 'Nadi (bpm)', border: OutlineInputBorder()),
                  keyboardType: TextInputType.number,
                ),
              ),
              const SizedBox(width: 8),
              Expanded(
                child: TextField(
                  controller: tempCtrl,
                  decoration: const InputDecoration(labelText: 'Suhu (°C)', border: OutlineInputBorder()),
                  keyboardType: TextInputType.number,
                ),
              ),
            ],
          ),
          const SizedBox(height: 10),
          Row(
            children: [
              Expanded(
                child: TextField(
                  controller: rrCtrl,
                  decoration: const InputDecoration(labelText: 'Nafas (x/menit)', border: OutlineInputBorder()),
                  keyboardType: TextInputType.number,
                ),
              ),
              const SizedBox(width: 8),
              Expanded(
                child: TextField(
                  controller: weightCtrl,
                  decoration: const InputDecoration(labelText: 'Berat Badan (kg)', border: OutlineInputBorder()),
                  keyboardType: TextInputType.number,
                ),
              ),
            ],
          ),
          const SizedBox(height: 10),
          TextField(
            controller: complaintCtrl,
            decoration: const InputDecoration(
              labelText: 'Keluhan Utama *',
              hintText: 'cth: Pusing, demam 2 hari, mual',
              border: OutlineInputBorder(),
            ),
            maxLines: 2,
          ),
          const SizedBox(height: 16),
          SizedBox(
            width: double.infinity,
            child: ElevatedButton.icon(
              style: ElevatedButton.styleFrom(
                backgroundColor: const Color(0xFF00796B),
                foregroundColor: Colors.white,
                padding: const EdgeInsets.symmetric(vertical: 12),
              ),
              onPressed: () {
                final vitals = VitalSigns(
                  systolic: int.tryParse(sysCtrl.text) ?? 120,
                  diastolic: int.tryParse(diaCtrl.text) ?? 80,
                  heartRate: int.tryParse(hrCtrl.text) ?? 78,
                  bodyTemp: double.tryParse(tempCtrl.text) ?? 36.6,
                  respiratoryRate: int.tryParse(rrCtrl.text) ?? 18,
                  weight: double.tryParse(weightCtrl.text) ?? 60,
                  chiefComplaints: complaintCtrl.text.isEmpty ? 'Pemeriksaan rutin' : complaintCtrl.text,
                );
                widget.engine.submitVitalSigns(targetVisit, vitals);
                ScaffoldMessenger.of(context).showSnackBar(
                  SnackBar(content: Text('TTV ${targetVisit.queueNumber} disimpan & diteruskan ke Dokter!')),
                );
                widget.engine.switchTab(0);
              },
              icon: const Icon(Icons.send),
              label: const Text('Simpan TTV & Teruskan ke Dokter'),
            ),
          ),
        ],
      ),
    );
  }

  // TAB 3: REKAP VITAL SIGNS
  Widget _buildTabRekapVitals() {
    final evaluated = widget.engine.visits.where((v) => v.vitalSigns != null).toList();

    return Padding(
      padding: const EdgeInsets.all(14.0),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          const Text('Rekap Vital Signs Hari Ini', style: TextStyle(fontSize: 15, fontWeight: FontWeight.bold)),
          const SizedBox(height: 10),
          if (evaluated.isEmpty)
            const Expanded(child: Center(child: Text('Belum ada data pemeriksaan vital signs.', style: TextStyle(color: Colors.grey))))
          else
            Expanded(
              child: ListView.builder(
                itemCount: evaluated.length,
                itemBuilder: (context, idx) {
                  final v = evaluated[idx];
                  final vit = v.vitalSigns!;
                  return Card(
                    margin: const EdgeInsets.only(bottom: 10),
                    child: Padding(
                      padding: const EdgeInsets.all(12.0),
                      child: Column(
                        crossAxisAlignment: CrossAxisAlignment.start,
                        children: [
                          Row(
                            mainAxisAlignment: MainAxisAlignment.spaceBetween,
                            children: [
                              Text('${v.queueNumber} - ${v.patient.name}', style: const TextStyle(fontWeight: FontWeight.bold)),
                              Text(vit.bloodPressureCategory, style: TextStyle(color: vit.bloodPressureCategory == 'Normal' ? Colors.green : Colors.red, fontWeight: FontWeight.bold, fontSize: 11)),
                            ],
                          ),
                          const SizedBox(height: 4),
                          Text('TD: ${vit.systolic}/${vit.diastolic} mmHg • Nadi: ${vit.heartRate} bpm • Suhu: ${vit.bodyTemp}°C • BB: ${vit.weight} kg', style: const TextStyle(fontSize: 12)),
                          Text('Keluhan: "${vit.chiefComplaints}"', style: const TextStyle(fontSize: 11, color: Colors.grey)),
                        ],
                      ),
                    ),
                  );
                },
              ),
            ),
        ],
      ),
    );
  }
}

// ==============================================================================
// 7. ROLE 3: DOKTER - CLINICAL EMR & E-RESEP (dr. Richard Pangihutan, Sp.PD)
// ==============================================================================

class _DokterScreen extends StatefulWidget {
  final ClinicStateEngine engine;
  const _DokterScreen({required this.engine});

  @override
  State<_DokterScreen> createState() => _DokterScreenState();
}

class _DokterScreenState extends State<_DokterScreen> {
  Visit? activeVisit;

  // Single Consultation Workspace Controllers
  final anamnesisCtrl = TextEditingController(text: 'Keluhan pusing dan lemas sejak 2 hari lalu.');
  final physicalCtrl = TextEditingController(text: 'Keadaan umum: Compos Mentis. Paru vesikuler, Jantung normal.');
  String selectedIcd10 = 'I10 - Hipertensi Primer';
  final therapyCtrl = TextEditingController(text: 'Diet rendah garam, istirahat teratur, hidrasi cairan 2L/hari.');
  bool needsPrescription = true;
  List<PrescriptionItem> currentRxItems = [];
  final followUpCtrl = TextEditingController(text: 'Kontrol kembali dalam 7 hari.');

  @override
  void initState() {
    super.initState();
    // Default prescription item from inventory
    if (widget.engine.inventory.isNotEmpty) {
      final med = widget.engine.inventory.first;
      currentRxItems.add(
        PrescriptionItem(
          medicineId: med.id,
          medicineName: med.name,
          dosage: '500mg',
          instruction: '3x1 sesudah makan',
          quantity: 10,
          unitPrice: med.unitPrice,
        ),
      );
    }
  }

  @override
  Widget build(BuildContext context) {
    switch (widget.engine.selectedTab) {
      case 0:
        return _buildTabAntreanDokter();
      case 1:
        return _buildTabKonsultasiSingleWorkspace();
      case 2:
        return _buildTabRiwayatEmr();
      default:
        return const SizedBox();
    }
  }

  // TAB 1: ANTREAN PASIEN DOKTER
  Widget _buildTabAntreanDokter() {
    final doctorQueue = widget.engine.visits.where((v) => v.stage == VisitStage.dokter).toList();

    return Padding(
      padding: const EdgeInsets.all(14.0),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Row(
            mainAxisAlignment: MainAxisAlignment.spaceBetween,
            children: [
              const Text('Daftar Pasien Menunggu Dokter', style: TextStyle(fontSize: 15, fontWeight: FontWeight.bold)),
              Text('${doctorQueue.length} Pasien', style: const TextStyle(fontSize: 12, color: Colors.grey)),
            ],
          ),
          const SizedBox(height: 10),
          if (doctorQueue.isEmpty)
            const Expanded(child: Center(child: Text('Tidak ada pasien di ruang tunggu dokter.', style: TextStyle(color: Colors.grey))))
          else
            Expanded(
              child: ListView.builder(
                itemCount: doctorQueue.length,
                itemBuilder: (context, idx) {
                  final visit = doctorQueue[idx];
                  return Card(
                    margin: const EdgeInsets.only(bottom: 12),
                    child: Padding(
                      padding: const EdgeInsets.all(12.0),
                      child: Column(
                        crossAxisAlignment: CrossAxisAlignment.start,
                        children: [
                          if (visit.hasSubstitutionAlert)
                            Container(
                              margin: const EdgeInsets.only(bottom: 8),
                              padding: const EdgeInsets.all(8),
                              decoration: BoxDecoration(color: Colors.red.shade100, borderRadius: BorderRadius.circular(6)),
                              child: Row(
                                children: [
                                  const Icon(Icons.warning, color: Colors.red, size: 18),
                                  const SizedBox(width: 8),
                                  Expanded(
                                    child: Text(
                                      'MINTA SUBSTITUSI OBAT DARI FARMASI: "${visit.substitutionRequestNote}"',
                                      style: TextStyle(color: Colors.red.shade900, fontWeight: FontWeight.bold, fontSize: 11),
                                    ),
                                  ),
                                ],
                              ),
                            ),
                          Row(
                            mainAxisAlignment: MainAxisAlignment.spaceBetween,
                            children: [
                              Text('${visit.queueNumber} - ${visit.patient.name}', style: const TextStyle(fontWeight: FontWeight.bold, fontSize: 14)),
                              Text(visit.patient.payerType == PayerType.bpjs ? 'BPJS' : 'Umum', style: const TextStyle(color: Color(0xFF00796B), fontWeight: FontWeight.bold)),
                            ],
                          ),
                          if (visit.vitalSigns != null) ...[
                            const SizedBox(height: 4),
                            Text('TD: ${visit.vitalSigns!.systolic}/${visit.vitalSigns!.diastolic} mmHg • Suhu: ${visit.vitalSigns!.bodyTemp}°C', style: const TextStyle(fontSize: 12)),
                            Text('Keluhan: "${visit.vitalSigns!.chiefComplaints}"', style: const TextStyle(fontSize: 11, color: Colors.grey)),
                          ],
                          const SizedBox(height: 10),
                          SizedBox(
                            width: double.infinity,
                            child: ElevatedButton.icon(
                              style: ElevatedButton.styleFrom(backgroundColor: const Color(0xFF00796B), foregroundColor: Colors.white),
                              onPressed: () {
                                setState(() => activeVisit = visit);
                                widget.engine.switchTab(1); // Jump to Single Consultation Workspace
                              },
                              icon: const Icon(Icons.play_arrow, size: 16),
                              label: const Text('Mulai Konsultasi & E-Resep'),
                            ),
                          ),
                        ],
                      ),
                    ),
                  );
                },
              ),
            ),
        ],
      ),
    );
  }

  // TAB 2: KONSULTASI & E-RESEP (SINGLE WORKSPACE WRAPPED IN SingleChildScrollView)
  Widget _buildTabKonsultasiSingleWorkspace() {
    final visit = activeVisit ?? widget.engine.visits.firstWhere((v) => v.stage == VisitStage.dokter, orElse: () => widget.engine.visits.first);

    return SingleChildScrollView(
      padding: const EdgeInsets.all(14.0),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          // Banner Substitusi jika ada
          if (visit.hasSubstitutionAlert)
            Container(
              margin: const EdgeInsets.only(bottom: 12),
              padding: const EdgeInsets.all(12),
              decoration: BoxDecoration(color: Colors.red.shade100, borderRadius: BorderRadius.circular(8), border: Border.all(color: Colors.red)),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  const Row(
                    children: [
                      Icon(Icons.notification_important, color: Colors.red),
                      SizedBox(width: 8),
                      Text('PERMINTAAN SUBSTITUSI OBAT DARI APOTEKER', style: TextStyle(fontWeight: FontWeight.bold, color: Colors.red)),
                    ],
                  ),
                  const SizedBox(height: 4),
                  Text('Catatan: "${visit.substitutionRequestNote}"', style: TextStyle(color: Colors.red.shade900, fontSize: 12)),
                ],
              ),
            ),

          // Patient Header
          Container(
            padding: const EdgeInsets.all(12),
            decoration: BoxDecoration(color: Colors.white, borderRadius: BorderRadius.circular(10), border: Border.all(color: Colors.grey.shade300)),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Row(
                  mainAxisAlignment: MainAxisAlignment.spaceBetween,
                  children: [
                    Text('${visit.queueNumber} - ${visit.patient.name}', style: const TextStyle(fontWeight: FontWeight.bold, fontSize: 15)),
                    Text(visit.patient.gender, style: const TextStyle(color: Colors.grey, fontSize: 12)),
                  ],
                ),
                Text('Alergi: ${visit.patient.allergy}', style: TextStyle(color: Colors.red.shade700, fontWeight: FontWeight.bold, fontSize: 12)),
                if (visit.vitalSigns != null)
                  Text('TTV: TD ${visit.vitalSigns!.systolic}/${visit.vitalSigns!.diastolic} | HR ${visit.vitalSigns!.heartRate} | Temp ${visit.vitalSigns!.bodyTemp}°C | Keluhan: ${visit.vitalSigns!.chiefComplaints}', style: const TextStyle(fontSize: 11, color: Colors.teal)),
              ],
            ),
          ),
          const SizedBox(height: 12),

          // Clinical Inputs
          const Text('1. Anamnesis / Temuan Klinis *', style: TextStyle(fontWeight: FontWeight.bold, fontSize: 13)),
          const SizedBox(height: 6),
          TextField(controller: anamnesisCtrl, maxLines: 2, decoration: const InputDecoration(border: OutlineInputBorder(), hintText: 'Hasil wawancara medis...')),
          const SizedBox(height: 10),

          const Text('2. Pemeriksaan Fisik *', style: TextStyle(fontWeight: FontWeight.bold, fontSize: 13)),
          const SizedBox(height: 6),
          TextField(controller: physicalCtrl, maxLines: 2, decoration: const InputDecoration(border: OutlineInputBorder(), hintText: 'Status lokalis & fisik...')),
          const SizedBox(height: 10),

          const Text('3. Diagnosis ICD-10 *', style: TextStyle(fontWeight: FontWeight.bold, fontSize: 13)),
          const SizedBox(height: 6),
          DropdownButtonFormField<String>(
            value: selectedIcd10,
            decoration: const InputDecoration(border: OutlineInputBorder()),
            items: const [
              DropdownMenuItem(value: 'I10 - Hipertensi Primer', child: Text('I10 - Hipertensi Primer')),
              DropdownMenuItem(value: 'E11 - Diabetes Melitus Tipe 2', child: Text('E11 - Diabetes Melitus Tipe 2')),
              DropdownMenuItem(value: 'J00 - Common Cold / Nasofaringitis', child: Text('J00 - Common Cold / Nasofaringitis')),
              DropdownMenuItem(value: 'K29.7 - Gastritis Akut', child: Text('K29.7 - Gastritis Akut')),
              DropdownMenuItem(value: 'R50.9 - Febris Tanpa Sebab Spesifik', child: Text('R50.9 - Febris Tanpa Sebab Spesifik')),
            ],
            onChanged: (val) => setState(() => selectedIcd10 = val!),
          ),
          const SizedBox(height: 10),

          const Text('4. Rencana Tindakan & Edukasi', style: TextStyle(fontWeight: FontWeight.bold, fontSize: 13)),
          const SizedBox(height: 6),
          TextField(controller: therapyCtrl, decoration: const InputDecoration(border: OutlineInputBorder())),
          const SizedBox(height: 14),

          // E-Prescription Integrated Builder
          Container(
            padding: const EdgeInsets.all(12),
            decoration: BoxDecoration(
              color: Colors.teal.shade50,
              borderRadius: BorderRadius.circular(10),
              border: Border.all(color: Colors.teal.shade200),
            ),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Row(
                  mainAxisAlignment: MainAxisAlignment.spaceBetween,
                  children: [
                    const Text('Perlu Resep Obat Farmasi?', style: TextStyle(fontWeight: FontWeight.bold, fontSize: 13)),
                    Switch(value: needsPrescription, onChanged: (v) => setState(() => needsPrescription = v)),
                  ],
                ),
                if (needsPrescription) ...[
                  const Divider(),
                  ...currentRxItems.map((rx) {
                    final med = widget.engine.inventory.firstWhere((m) => m.id == rx.medicineId);
                    return Card(
                      margin: const EdgeInsets.only(bottom: 6),
                      child: Padding(
                        padding: const EdgeInsets.all(8.0),
                        child: Row(
                          mainAxisAlignment: MainAxisAlignment.spaceBetween,
                          children: [
                            Column(
                              crossAxisAlignment: CrossAxisAlignment.start,
                              children: [
                                Text(rx.medicineName, style: const TextStyle(fontWeight: FontWeight.bold, fontSize: 13)),
                                Text('${rx.dosage} • ${rx.instruction} • Qty: ${rx.quantity}', style: const TextStyle(fontSize: 11, color: Colors.grey)),
                              ],
                            ),
                            Text('Stok: ${med.stock}', style: TextStyle(fontSize: 11, color: med.isLowStock ? Colors.red : Colors.green, fontWeight: FontWeight.bold)),
                          ],
                        ),
                      ),
                    );
                  }),
                  const SizedBox(height: 6),
                  ElevatedButton.icon(
                    onPressed: () => _showAddMedicineDialog(),
                    icon: const Icon(Icons.add, size: 16),
                    label: const Text('Tambah Obat Lain', style: TextStyle(fontSize: 11)),
                  ),
                ],
              ],
            ),
          ),
          const SizedBox(height: 16),

          // Submit Consultation
          SizedBox(
            width: double.infinity,
            child: ElevatedButton.icon(
              style: ElevatedButton.styleFrom(
                backgroundColor: const Color(0xFF00796B),
                foregroundColor: Colors.white,
                padding: const EdgeInsets.symmetric(vertical: 14),
              ),
              onPressed: () {
                final record = MedicalRecord(
                  anamnesis: anamnesisCtrl.text,
                  physicalExam: physicalCtrl.text,
                  icd10Code: selectedIcd10.split(' - ')[0],
                  icd10Desc: selectedIcd10.split(' - ')[1],
                  therapyPlan: therapyCtrl.text,
                  needsPrescription: needsPrescription,
                  prescriptionItems: needsPrescription ? currentRxItems : [],
                  followUpNote: followUpCtrl.text,
                );
                widget.engine.submitConsultation(visit: visit, record: record);
                ScaffoldMessenger.of(context).showSnackBar(
                  SnackBar(content: Text('Konsultasi ${visit.queueNumber} disimpan & diteruskan!')),
                );
                widget.engine.switchTab(0);
              },
              icon: const Icon(Icons.check_circle),
              label: const Text('Simpan Rekam Medis & Kirim Resep', style: TextStyle(fontSize: 14)),
            ),
          ),
          const SizedBox(height: 20),
        ],
      ),
    );
  }

  void _showAddMedicineDialog() {
    InventoryItem selectedMed = widget.engine.inventory.first;
    final qtyCtrl = TextEditingController(text: '10');
    final instructionCtrl = TextEditingController(text: '3x1 sesudah makan');

    showDialog(
      context: context,
      builder: (ctx) {
        return StatefulBuilder(
          builder: (ctx, setDlgState) {
            return AlertDialog(
              title: const Text('Tambah Obat ke E-Resep'),
              content: Column(
                mainAxisSize: MainAxisSize.min,
                children: [
                  DropdownButtonFormField<InventoryItem>(
                    value: selectedMed,
                    items: widget.engine.inventory.map((m) {
                      return DropdownMenuItem(
                        value: m,
                        child: Text('${m.name} (Stok: ${m.stock})'),
                      );
                    }).toList(),
                    onChanged: (val) => setDlgState(() => selectedMed = val!),
                  ),
                  TextField(controller: qtyCtrl, decoration: const InputDecoration(labelText: 'Jumlah (Qty)'), keyboardType: TextInputType.number),
                  TextField(controller: instructionCtrl, decoration: const InputDecoration(labelText: 'Aturan Pakai')),
                ],
              ),
              actions: [
                TextButton(onPressed: () => Navigator.pop(ctx), child: const Text('Batal')),
                ElevatedButton(
                  onPressed: () {
                    setState(() {
                      currentRxItems.add(
                        PrescriptionItem(
                          medicineId: selectedMed.id,
                          medicineName: selectedMed.name,
                          dosage: 'Standar',
                          instruction: instructionCtrl.text,
                          quantity: int.tryParse(qtyCtrl.text) ?? 10,
                          unitPrice: selectedMed.unitPrice,
                        ),
                      );
                    });
                    Navigator.pop(ctx);
                  },
                  child: const Text('Tambah'),
                ),
              ],
            );
          },
        );
      },
    );
  }

  // TAB 3: RIWAYAT EMR PASIEN
  Widget _buildTabRiwayatEmr() {
    final emrVisits = widget.engine.visits.where((v) => v.medicalRecord != null).toList();

    return Padding(
      padding: const EdgeInsets.all(14.0),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          const Text('Riwayat Medis Pasien Klinik', style: TextStyle(fontSize: 15, fontWeight: FontWeight.bold)),
          const SizedBox(height: 10),
          if (emrVisits.isEmpty)
            const Expanded(child: Center(child: Text('Belum ada rekam medis tersimpan.', style: TextStyle(color: Colors.grey))))
          else
            Expanded(
              child: ListView.builder(
                itemCount: emrVisits.length,
                itemBuilder: (context, idx) {
                  final v = emrVisits[idx];
                  final rec = v.medicalRecord!;
                  return Card(
                    margin: const EdgeInsets.only(bottom: 12),
                    child: Padding(
                      padding: const EdgeInsets.all(12.0),
                      child: Column(
                        crossAxisAlignment: CrossAxisAlignment.start,
                        children: [
                          Row(
                            mainAxisAlignment: MainAxisAlignment.spaceBetween,
                            children: [
                              Text('${v.queueNumber} - ${v.patient.name}', style: const TextStyle(fontWeight: FontWeight.bold, fontSize: 14)),
                              Text(rec.icd10Code, style: const TextStyle(color: Color(0xFF00796B), fontWeight: FontWeight.bold)),
                            ],
                          ),
                          Text('Diagnosa: ${rec.icd10Desc}', style: const TextStyle(fontSize: 12, fontWeight: FontWeight.bold)),
                          const SizedBox(height: 4),
                          Text('Anamnesis: ${rec.anamnesis}', style: const TextStyle(fontSize: 11, color: Colors.grey)),
                          Text('Terapi: ${rec.therapyPlan}', style: const TextStyle(fontSize: 11)),
                          if (rec.prescriptionItems.isNotEmpty)
                            Text('Resep: ${rec.prescriptionItems.map((e) => e.medicineName).join(', ')}', style: const TextStyle(fontSize: 11, color: Colors.teal)),
                        ],
                      ),
                    ),
                  );
                },
              ),
            ),
        ],
      ),
    );
  }
}

// ==============================================================================
// 8. ROLE 4: FARMASI & MUTASI STOK (Muhammad Haikal Siregar, S.Farm)
// ==============================================================================

class _FarmasiScreen extends StatefulWidget {
  final ClinicStateEngine engine;
  const _FarmasiScreen({required this.engine});

  @override
  State<_FarmasiScreen> createState() => _FarmasiScreenState();
}

class _FarmasiScreenState extends State<_FarmasiScreen> {
  @override
  Widget build(BuildContext context) {
    switch (widget.engine.selectedTab) {
      case 0:
        return _buildTabAntreanResep();
      case 1:
        return _buildTabKatalogAlert();
      case 2:
        return _buildTabStokOpname();
      default:
        return const SizedBox();
    }
  }

  // TAB 1: ANTREAN RESEP MASUK (VERIFIKASI & HANDOVER)
  Widget _buildTabAntreanResep() {
    final rxQueue = widget.engine.visits.where((v) => v.stage == VisitStage.farmasi).toList();

    return Padding(
      padding: const EdgeInsets.all(14.0),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Row(
            mainAxisAlignment: MainAxisAlignment.spaceBetween,
            children: [
              const Text('Antrean E-Resep Masuk', style: TextStyle(fontSize: 15, fontWeight: FontWeight.bold)),
              Text('${rxQueue.length} Resep Menunggu', style: const TextStyle(fontSize: 12, color: Colors.grey)),
            ],
          ),
          const SizedBox(height: 10),
          if (rxQueue.isEmpty)
            const Expanded(child: Center(child: Text('Tidak ada resep menunggu dispensing.', style: TextStyle(color: Colors.grey))))
          else
            Expanded(
              child: ListView.builder(
                itemCount: rxQueue.length,
                itemBuilder: (context, idx) {
                  final visit = rxQueue[idx];
                  final items = visit.medicalRecord?.prescriptionItems ?? [];

                  return Card(
                    margin: const EdgeInsets.only(bottom: 12),
                    child: Padding(
                      padding: const EdgeInsets.all(12.0),
                      child: Column(
                        crossAxisAlignment: CrossAxisAlignment.start,
                        children: [
                          Row(
                            mainAxisAlignment: MainAxisAlignment.spaceBetween,
                            children: [
                              Text('${visit.queueNumber} - ${visit.patient.name}', style: const TextStyle(fontWeight: FontWeight.bold, fontSize: 14)),
                              Text(visit.patient.payerType == PayerType.bpjs ? 'BPJS' : 'Umum', style: const TextStyle(color: Color(0xFF00796B), fontWeight: FontWeight.bold)),
                            ],
                          ),
                          const Divider(),
                          ...items.map((it) {
                            final med = widget.engine.inventory.firstWhere((m) => m.id == it.medicineId);
                            return Padding(
                              padding: const EdgeInsets.symmetric(vertical: 2.0),
                              child: Row(
                                mainAxisAlignment: MainAxisAlignment.spaceBetween,
                                children: [
                                  Text('• ${it.medicineName} (${it.quantity} ${med.unit})', style: const TextStyle(fontSize: 12)),
                                  Text('Stok Rak: ${med.stock}', style: TextStyle(fontSize: 11, color: med.stock >= it.quantity ? Colors.green : Colors.red, fontWeight: FontWeight.bold)),
                                ],
                              ),
                            );
                          }),
                          const SizedBox(height: 10),
                          Row(
                            children: [
                              // Minta Substitusi Obat Button
                              Expanded(
                                child: OutlinedButton.icon(
                                  style: OutlinedButton.styleFrom(
                                    foregroundColor: Colors.red,
                                    side: const BorderSide(color: Colors.red),
                                    padding: const EdgeInsets.symmetric(vertical: 8),
                                  ),
                                  onPressed: () => _showSubstitutionDialog(visit),
                                  icon: const Icon(Icons.swap_horiz, size: 16),
                                  label: const Text('Minta Substitusi', style: TextStyle(fontSize: 11)),
                                ),
                              ),
                              const SizedBox(width: 8),
                              // Handover & Deduct Stock Button
                              Expanded(
                                child: ElevatedButton.icon(
                                  style: ElevatedButton.styleFrom(
                                    backgroundColor: const Color(0xFF00796B),
                                    foregroundColor: Colors.white,
                                    padding: const EdgeInsets.symmetric(vertical: 8),
                                  ),
                                  onPressed: () {
                                    widget.engine.dispensePrescription(visit);
                                    ScaffoldMessenger.of(context).showSnackBar(
                                      SnackBar(content: Text('Obat ${visit.queueNumber} diserahkan & stok otomatis dipotong!')),
                                    );
                                  },
                                  icon: const Icon(Icons.check, size: 16),
                                  label: const Text('Serahkan Obat', style: TextStyle(fontSize: 11)),
                                ),
                              ),
                            ],
                          ),
                        ],
                      ),
                    ),
                  );
                },
              ),
            ),
        ],
      ),
    );
  }

  void _showSubstitutionDialog(Visit visit) {
    final noteCtrl = TextEditingController(text: 'Stok obat di rak habis/rusak. Mohon ganti dengan obat alternatif.');

    showDialog(
      context: context,
      builder: (ctx) {
        return AlertDialog(
          title: const Text('Minta Substitusi Obat ke Dokter'),
          content: Column(
            mainAxisSize: MainAxisSize.min,
            children: [
              Text('Pasien: ${visit.queueNumber} - ${visit.patient.name}'),
              const SizedBox(height: 8),
              TextField(
                controller: noteCtrl,
                maxLines: 3,
                decoration: const InputDecoration(labelText: 'Alasan / Rekomendasi Obat Pengganti', border: OutlineInputBorder()),
              ),
            ],
          ),
          actions: [
            TextButton(onPressed: () => Navigator.pop(ctx), child: const Text('Batal')),
            ElevatedButton(
              style: ElevatedButton.styleFrom(backgroundColor: Colors.red, foregroundColor: Colors.white),
              onPressed: () {
                widget.engine.requestDrugSubstitution(visit, noteCtrl.text);
                Navigator.pop(ctx);
                ScaffoldMessenger.of(context).showSnackBar(
                  const SnackBar(content: Text('Peringatan substitusi dikirimkan ke layar Dokter!')),
                );
              },
              child: const Text('Kirim ke Dokter'),
            ),
          ],
        );
      },
    );
  }

  // TAB 2: KATALOG STOK & ALERT
  Widget _buildTabKatalogAlert() {
    final lowStock = widget.engine.inventory.where((m) => m.isLowStock).toList();

    return Padding(
      padding: const EdgeInsets.all(14.0),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          if (lowStock.isNotEmpty)
            Container(
              margin: const EdgeInsets.only(bottom: 12),
              padding: const EdgeInsets.all(10),
              decoration: BoxDecoration(color: Colors.red.shade100, borderRadius: BorderRadius.circular(8)),
              child: Row(
                children: [
                  const Icon(Icons.warning, color: Colors.red, size: 20),
                  const SizedBox(width: 8),
                  Text('PERINGATAN: ${lowStock.length} Obat di Bawah Batas Minimum!', style: TextStyle(color: Colors.red.shade900, fontWeight: FontWeight.bold, fontSize: 12)),
                ],
              ),
            ),
          const Text('Katalog Inventaris Obat', style: TextStyle(fontSize: 15, fontWeight: FontWeight.bold)),
          const SizedBox(height: 10),
          Expanded(
            child: ListView.builder(
              itemCount: widget.engine.inventory.length,
              itemBuilder: (context, idx) {
                final med = widget.engine.inventory[idx];
                return Card(
                  margin: const EdgeInsets.only(bottom: 10),
                  child: Padding(
                    padding: const EdgeInsets.all(12.0),
                    child: Row(
                      mainAxisAlignment: MainAxisAlignment.spaceBetween,
                      children: [
                        Column(
                          crossAxisAlignment: CrossAxisAlignment.start,
                          children: [
                            Text(med.name, style: const TextStyle(fontWeight: FontWeight.bold, fontSize: 14)),
                            Text('Batch: ${med.batch} • Exp: ${med.expiryDate}', style: const TextStyle(fontSize: 11, color: Colors.grey)),
                            Text('Harga: Rp ${med.unitPrice} / ${med.unit}', style: const TextStyle(fontSize: 11, color: Colors.teal)),
                          ],
                        ),
                        Column(
                          crossAxisAlignment: CrossAxisAlignment.end,
                          children: [
                            Text(
                              '${med.stock} ${med.unit}',
                              style: TextStyle(fontSize: 16, fontWeight: FontWeight.bold, color: med.isLowStock ? Colors.red : const Color(0xFF00796B)),
                            ),
                            Text('Min: ${med.minStock}', style: const TextStyle(fontSize: 10, color: Colors.grey)),
                          ],
                        ),
                      ],
                    ),
                  ),
                );
              },
            ),
          ),
        ],
      ),
    );
  }

  // TAB 3: STOK OPNAME & AUDIT
  Widget _buildTabStokOpname() {
    InventoryItem selectedMed = widget.engine.inventory.first;
    final physicalCtrl = TextEditingController(text: '${selectedMed.stock}');
    final reasonCtrl = TextEditingController(text: 'Hitungan fisik rak penyimpanan');

    return SingleChildScrollView(
      padding: const EdgeInsets.all(14.0),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          const Text('Form Pengajuan Stok Opname Fisik', style: TextStyle(fontSize: 15, fontWeight: FontWeight.bold)),
          const SizedBox(height: 10),
          Card(
            child: Padding(
              padding: const EdgeInsets.all(12.0),
              child: Column(
                children: [
                  DropdownButtonFormField<InventoryItem>(
                    value: selectedMed,
                    decoration: const InputDecoration(labelText: 'Pilih Obat', border: OutlineInputBorder()),
                    items: widget.engine.inventory.map((m) {
                      return DropdownMenuItem(value: m, child: Text('${m.name} (Sistem: ${m.stock})'));
                    }).toList(),
                    onChanged: (val) {
                      if (val != null) {
                        setState(() {
                          selectedMed = val;
                          physicalCtrl.text = '${val.stock}';
                        });
                      }
                    },
                  ),
                  const SizedBox(height: 10),
                  TextField(controller: physicalCtrl, decoration: const InputDecoration(labelText: 'Hitungan Fisik di Rak *', border: OutlineInputBorder()), keyboardType: TextInputType.number),
                  const SizedBox(height: 10),
                  TextField(controller: reasonCtrl, decoration: const InputDecoration(labelText: 'Alasan Selisih / Kerusakan *', border: OutlineInputBorder())),
                  const SizedBox(height: 12),
                  SizedBox(
                    width: double.infinity,
                    child: ElevatedButton(
                      style: ElevatedButton.styleFrom(backgroundColor: const Color(0xFF00796B), foregroundColor: Colors.white),
                      onPressed: () {
                        final count = int.tryParse(physicalCtrl.text) ?? selectedMed.stock;
                        widget.engine.submitStockOpname(medicineId: selectedMed.id, physicalCount: count, reason: reasonCtrl.text);
                        ScaffoldMessenger.of(context).showSnackBar(
                          SnackBar(content: Text('Opname ${selectedMed.name} diajukan ke Kepala Klinik!')),
                        );
                      },
                      child: const Text('Ajukan Koreksi Stok ke Kepala Klinik'),
                    ),
                  ),
                ],
              ),
            ),
          ),
          const SizedBox(height: 16),
          const Text('Log Mutasi Farmasi Real-Time', style: TextStyle(fontSize: 14, fontWeight: FontWeight.bold)),
          const SizedBox(height: 8),
          ...widget.engine.mutations.take(6).map((mut) {
            return Card(
              margin: const EdgeInsets.only(bottom: 6),
              child: ListTile(
                dense: true,
                leading: Icon(
                  mut.quantityChange < 0 ? Icons.arrow_circle_up : Icons.arrow_circle_down,
                  color: mut.quantityChange < 0 ? Colors.red : Colors.green,
                ),
                title: Text(mut.medicineName, style: const TextStyle(fontWeight: FontWeight.bold, fontSize: 13)),
                subtitle: Text('${mut.note} • ${mut.operatorName}', style: const TextStyle(fontSize: 11)),
                trailing: Text(
                  '${mut.quantityChange > 0 ? "+" : ""}${mut.quantityChange} (Sisa: ${mut.finalStock})',
                  style: TextStyle(fontWeight: FontWeight.bold, color: mut.quantityChange < 0 ? Colors.red : Colors.green),
                ),
              ),
            );
          }),
        ],
      ),
    );
  }
}

// ==============================================================================
// 9. ROLE 5: KEPALA KLINIK (dr. Stephen Parnahean)
// ==============================================================================

class _KepalaKlinikScreen extends StatefulWidget {
  final ClinicStateEngine engine;
  const _KepalaKlinikScreen({required this.engine});

  @override
  State<_KepalaKlinikScreen> createState() => _KepalaKlinikScreenState();
}

class _KepalaKlinikScreenState extends State<_KepalaKlinikScreen> {
  @override
  Widget build(BuildContext context) {
    switch (widget.engine.selectedTab) {
      case 0:
        return _buildTabExecutiveKpi();
      case 1:
        return _buildTabAnalisisOperasional();
      case 2:
        return _buildTabPersetujuanTugas();
      default:
        return const SizedBox();
    }
  }

  // TAB 1: EXECUTIVE DASHBOARD KPI
  Widget _buildTabExecutiveKpi() {
    final total = widget.engine.visits.length;
    final bpjsVisits = widget.engine.visits.where((v) => v.patient.payerType == PayerType.bpjs).length;
    final umumVisits = total - bpjsVisits;
    final lowStockCount = widget.engine.inventory.where((m) => m.isLowStock).length;
    final bpjsClaimTotal = bpjsVisits * 120000;
    final umumCashTotal = umumVisits * 50000;

    return SingleChildScrollView(
      padding: const EdgeInsets.all(14.0),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          const Text('Executive Clinic Dashboard', style: TextStyle(fontSize: 16, fontWeight: FontWeight.bold)),
          const SizedBox(height: 10),
          Row(
            children: [
              Expanded(child: _kpiCard('Total Kunjungan', '$total Pasien', '$bpjsVisits BPJS | $umumVisits Umum', Icons.people, Colors.teal)),
              const SizedBox(width: 10),
              Expanded(child: _kpiCard('Peringatan Stok', '$lowStockCount Obat', 'Di bawah batas minimum', Icons.warning, Colors.red)),
            ],
          ),
          const SizedBox(height: 10),
          Row(
            children: [
              Expanded(child: _kpiCard('Klaim BPJS (JKN)', 'Rp $bpjsClaimTotal', 'Tarif Rp 120.000 / Pasien', Icons.security, Colors.green)),
              const SizedBox(width: 10),
              Expanded(child: _kpiCard('Kas Pasien Umum', 'Rp $umumCashTotal', 'Tarif Rp 50.000 / Pasien', Icons.payments, Colors.blue)),
            ],
          ),
          const SizedBox(height: 16),
          const Text('Sebaran Posisi Antrean Pasien', style: TextStyle(fontSize: 14, fontWeight: FontWeight.bold)),
          const SizedBox(height: 8),
          Card(
            child: Padding(
              padding: const EdgeInsets.all(14.0),
              child: Row(
                mainAxisAlignment: MainAxisAlignment.spaceAround,
                children: [
                  _stageCount('Triage', widget.engine.visits.where((v) => v.stage == VisitStage.triage).length, Colors.amber),
                  _stageCount('Dokter', widget.engine.visits.where((v) => v.stage == VisitStage.dokter).length, Colors.blue),
                  _stageCount('Farmasi', widget.engine.visits.where((v) => v.stage == VisitStage.farmasi).length, Colors.purple),
                  _stageCount('Kasir', widget.engine.visits.where((v) => v.stage == VisitStage.kasir).length, Colors.orange),
                  _stageCount('Selesai', widget.engine.visits.where((v) => v.stage == VisitStage.selesai).length, Colors.green),
                ],
              ),
            ),
          ),
        ],
      ),
    );
  }

  Widget _kpiCard(String title, String value, String sub, IconData icon, Color color) {
    return Card(
      child: Padding(
        padding: const EdgeInsets.all(12.0),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Row(
              mainAxisAlignment: MainAxisAlignment.spaceBetween,
              children: [
                Text(title, style: const TextStyle(fontSize: 11, color: Colors.grey)),
                Icon(icon, color: color, size: 18),
              ],
            ),
            const SizedBox(height: 4),
            Text(value, style: const TextStyle(fontSize: 16, fontWeight: FontWeight.bold)),
            const SizedBox(height: 2),
            Text(sub, style: TextStyle(fontSize: 10, color: color, fontWeight: FontWeight.bold)),
          ],
        ),
      ),
    );
  }

  Widget _stageCount(String name, int count, Color color) {
    return Column(
      children: [
        CircleAvatar(radius: 16, backgroundColor: color.withOpacity(0.15), child: Text('$count', style: TextStyle(color: color, fontWeight: FontWeight.bold, fontSize: 13))),
        const SizedBox(height: 4),
        Text(name, style: const TextStyle(fontSize: 10, color: Colors.grey)),
      ],
    );
  }

  // TAB 2: ANALISIS OPERASIONAL
  Widget _buildTabAnalisisOperasional() {
    return Padding(
      padding: const EdgeInsets.all(14.0),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          const Text('Analisis Efisiensi & Bottleneck Alur', style: TextStyle(fontSize: 15, fontWeight: FontWeight.bold)),
          const SizedBox(height: 10),
          Card(
            child: Padding(
              padding: const EdgeInsets.all(12.0),
              child: Column(
                children: [
                  _bottleneckRow('Kecepatan Triage Perawat', 'Normal (< 7 Menit)', Colors.green),
                  const Divider(),
                  _bottleneckRow('Antrean Konsultasi Dokter', 'Rata-rata 12 Menit / Pasien', Colors.blue),
                  const Divider(),
                  _bottleneckRow('Dispensing Farmasi', 'Perlu Waspada (Stok Kritis Paracetamol)', Colors.amber.shade900),
                ],
              ),
            ),
          ),
        ],
      ),
    );
  }

  Widget _bottleneckRow(String label, String status, Color color) {
    return Row(
      mainAxisAlignment: MainAxisAlignment.spaceBetween,
      children: [
        Text(label, style: const TextStyle(fontSize: 13, fontWeight: FontWeight.bold)),
        Text(status, style: TextStyle(fontSize: 12, color: color, fontWeight: FontWeight.bold)),
      ],
    );
  }

  // TAB 3: PERSETUJUAN & TUGAS (APPROVE OPNAME & DELEGASI)
  Widget _buildTabPersetujuanTugas() {
    final pendingOpnames = widget.engine.opnameRequests.where((r) => r.status == 'Menunggu Approval').toList();

    return SingleChildScrollView(
      padding: const EdgeInsets.all(14.0),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Row(
            mainAxisAlignment: MainAxisAlignment.spaceBetween,
            children: [
              const Text('Persetujuan Stok Opname', style: TextStyle(fontSize: 15, fontWeight: FontWeight.bold)),
              ElevatedButton.icon(
                style: ElevatedButton.styleFrom(backgroundColor: const Color(0xFF00796B), foregroundColor: Colors.white),
                onPressed: () => _showAssignTaskDialog(),
                icon: const Icon(Icons.add_task, size: 16),
                label: const Text('Tugaskan Staf', style: TextStyle(fontSize: 11)),
              ),
            ],
          ),
          const SizedBox(height: 10),
          if (pendingOpnames.isEmpty)
            const Text('Tidak ada permintaan persetujuan opname pending.', style: TextStyle(color: Colors.grey))
          else
            ...pendingOpnames.map((opn) {
              return Card(
                margin: const EdgeInsets.only(bottom: 10),
                child: Padding(
                  padding: const EdgeInsets.all(12.0),
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      Row(
                        mainAxisAlignment: MainAxisAlignment.spaceBetween,
                        children: [
                          Text(opn.medicineName, style: const TextStyle(fontWeight: FontWeight.bold, fontSize: 14)),
                          Text('Selisih: ${opn.variance}', style: const TextStyle(fontWeight: FontWeight.bold, color: Colors.red)),
                        ],
                      ),
                      Text('Sistem: ${opn.systemStock} ➔ Fisik: ${opn.physicalCount} • Oleh: ${opn.submittedBy}', style: const TextStyle(fontSize: 11, color: Colors.grey)),
                      Text('Alasan: "${opn.reason}"', style: const TextStyle(fontSize: 12)),
                      const SizedBox(height: 8),
                      Row(
                        mainAxisAlignment: MainAxisAlignment.end,
                        children: [
                          OutlinedButton(
                            style: OutlinedButton.styleFrom(foregroundColor: Colors.red),
                            onPressed: () => widget.engine.reviewStockOpname(opn, false, 'Ditolak: Mohon hitung ulang fisik rak'),
                            child: const Text('Tolak'),
                          ),
                          const SizedBox(width: 8),
                          ElevatedButton(
                            style: ElevatedButton.styleFrom(backgroundColor: Colors.green, foregroundColor: Colors.white),
                            onPressed: () => widget.engine.reviewStockOpname(opn, true, 'Disetujui untuk pemutakhiran saldo'),
                            child: const Text('Setujui (Update Stok)'),
                          ),
                        ],
                      ),
                    ],
                  ),
                ),
              );
            }),
          const SizedBox(height: 16),
          const Text('Daftar Tugas & Instruksi Pimpinan', style: TextStyle(fontSize: 14, fontWeight: FontWeight.bold)),
          const SizedBox(height: 8),
          ...widget.engine.staffTasks.map((tsk) {
            return Card(
              margin: const EdgeInsets.only(bottom: 6),
              child: ListTile(
                dense: true,
                title: Text(tsk.taskInstruction, style: const TextStyle(fontSize: 12, fontWeight: FontWeight.bold)),
                subtitle: Text('Untuk: ${tsk.assignee} • Prioritas: ${tsk.priority}', style: const TextStyle(fontSize: 10)),
                trailing: Checkbox(
                  value: tsk.isCompleted,
                  onChanged: (val) {
                    setState(() => tsk.isCompleted = val ?? false);
                  },
                ),
              ),
            );
          }),
        ],
      ),
    );
  }

  void _showAssignTaskDialog() {
    String assignee = widget.engine.personas[3].name; // Farmasi default
    final taskCtrl = TextEditingController();

    showDialog(
      context: context,
      builder: (ctx) {
        return AlertDialog(
          title: const Text('Tugaskan Staf / Instruksi'),
          content: Column(
            mainAxisSize: MainAxisSize.min,
            children: [
              DropdownButtonFormField<String>(
                value: assignee,
                items: widget.engine.personas.map((p) => DropdownMenuItem(value: p.name, child: Text('${p.name} (${p.title})'))).toList(),
                onChanged: (val) => assignee = val!,
              ),
              const SizedBox(height: 10),
              TextField(controller: taskCtrl, maxLines: 2, decoration: const InputDecoration(labelText: 'Instruksi Tugas *', border: OutlineInputBorder())),
            ],
          ),
          actions: [
            TextButton(onPressed: () => Navigator.pop(ctx), child: const Text('Batal')),
            ElevatedButton(
              style: ElevatedButton.styleFrom(backgroundColor: const Color(0xFF00796B), foregroundColor: Colors.white),
              onPressed: () {
                if (taskCtrl.text.isNotEmpty) {
                  widget.engine.assignTask(assignee: assignee, instruction: taskCtrl.text, priority: 'Tinggi');
                  Navigator.pop(ctx);
                }
              },
              child: const Text('Kirim Tugas'),
            ),
          ],
        );
      },
    );
  }
}
