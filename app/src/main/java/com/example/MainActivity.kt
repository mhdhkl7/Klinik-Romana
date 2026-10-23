package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ClinicRepository
import com.example.model.Role
import com.example.ui.components.ClinicTopBar
import com.example.ui.screens.*
import com.example.ui.theme.ClinicalTealContainer
import com.example.ui.theme.ClinicalTealPrimary
import com.example.ui.theme.KlinikRomanaTheme
import com.example.ui.theme.StatusAlertRed

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            KlinikRomanaTheme {
                KlinikRomanaApp()
            }
        }
    }
}

data class RoleTabItem(
    val title: String,
    val icon: ImageVector,
    val hasAlert: Boolean = false
)

@Composable
fun KlinikRomanaApp() {
    val repository = remember { ClinicRepository.getInstance() }
    val state by repository.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.toastMessage) {
        state.toastMessage?.let { msg ->
            snackbarHostState.showSnackbar(
                message = msg,
                duration = SnackbarDuration.Short
            )
            repository.dismissToast()
        }
    }

    // Hardware Back Button: returns to Admin if in another role
    BackHandler(enabled = state.currentRole != Role.ADMIN_PENDAFTARAN) {
        repository.setRole(Role.ADMIN_PENDAFTARAN)
    }

    val activeCallingQueue = state.activeCallingQueueId?.let { callingId ->
        state.queue.find { it.id == callingId }
    }

    // 3 Dedicated Operational Tabs per Persona (as per HCI matrix)
    val tabs = when (state.currentRole) {
        Role.ADMIN_PENDAFTARAN -> listOf(
            RoleTabItem("Registrasi & Antrean", Icons.Default.PersonAdd),
            RoleTabItem("Status & Tracking", Icons.Default.Timeline),
            RoleTabItem("Kasir & Bayar", Icons.Default.PointOfSale)
        )
        Role.PERAWAT_TRIAGE -> listOf(
            RoleTabItem("Antrean Triage", Icons.Default.Queue),
            RoleTabItem("Input TTV", Icons.Default.EditNote),
            RoleTabItem("Rekap Vitals", Icons.Default.TableChart)
        )
        Role.DOKTER -> listOf(
            RoleTabItem(
                "Antrean Pasien",
                Icons.Default.People,
                hasAlert = state.queue.any { it.hasSubstitutionAlert }
            ),
            RoleTabItem("Konsultasi & Resep", Icons.Default.MedicalInformation),
            RoleTabItem("Riwayat EMR", Icons.Default.HistoryEdu)
        )
        Role.FARMASI_STOK -> listOf(
            RoleTabItem("Antrean Resep", Icons.Default.ReceiptLong),
            RoleTabItem("Katalog & Alert", Icons.Default.Inventory2),
            RoleTabItem("Stok Opname", Icons.Default.FactCheck)
        )
        Role.KEPALA_KLINIK -> listOf(
            RoleTabItem("Dashboard KPI", Icons.Default.Dashboard),
            RoleTabItem("Analisis Operasional", Icons.Default.Analytics),
            RoleTabItem("Persetujuan & Tugas", Icons.Default.Approval)
        )
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets.safeDrawing,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            ClinicTopBar(
                currentRole = state.currentRole,
                activeCallingQueue = activeCallingQueue,
                onRoleSelected = { role -> repository.setRole(role) },
                onResetDemoData = { repository.loadInitialSeedData() }
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = Color.White,
                tonalElevation = 6.dp,
                modifier = Modifier.testTag("clinic_persona_bottom_navigation")
            ) {
                tabs.forEachIndexed { index, tabItem ->
                    val isSelected = state.selectedTab == index
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { repository.setSelectedTab(index) },
                        icon = {
                            BadgedBox(
                                badge = {
                                    if (tabItem.hasAlert) {
                                        Badge(containerColor = StatusAlertRed) {
                                            Text("!", color = Color.White, fontSize = 9.sp)
                                        }
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = tabItem.icon,
                                    contentDescription = tabItem.title,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        },
                        label = {
                            Text(
                                text = tabItem.title,
                                fontSize = 11.sp,
                                maxLines = 1
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = ClinicalTealPrimary,
                            selectedTextColor = ClinicalTealPrimary,
                            indicatorColor = ClinicalTealContainer,
                            unselectedIconColor = Color.Gray,
                            unselectedTextColor = Color.Gray
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Crossfade(targetState = state.currentRole, label = "personaRoleCrossfade") { role ->
                when (role) {
                    Role.ADMIN_PENDAFTARAN -> AdminRegistrationScreen(state = state, repository = repository)
                    Role.PERAWAT_TRIAGE -> NurseTriageScreen(state = state, repository = repository)
                    Role.DOKTER -> DoctorConsultationScreen(state = state, repository = repository)
                    Role.FARMASI_STOK -> PharmacyScreen(state = state, repository = repository)
                    Role.KEPALA_KLINIK -> ClinicDashboardScreen(state = state, repository = repository)
                }
            }
        }
    }
}
