package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.RotateRight
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import android.content.Intent
import androidx.compose.runtime.mutableStateOf
import com.example.data.model.DoseRecord
import com.example.receiver.NotificationHelper
import com.example.ui.components.BloodGlucoseEntryDialog
import com.example.ui.components.PrivacyConsentGateScreen
import com.example.ui.components.PrivacyConsentManager
import com.example.ui.screens.HistoryScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.RemindersScreen
import com.example.ui.screens.RotationScreen
import com.example.ui.screens.StatisticsScreen
import com.example.ui.screens.StockScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.NeonCyanDark
import com.example.viewmodel.InsulinViewModel
import com.example.viewmodel.UiEvent
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

sealed class Screen(val title: String, val icon: ImageVector, val tag: String) {
    object Home : Screen("Ana Sayfa", Icons.Default.Home, "nav_home")
    object Statistics : Screen("İstatistik", Icons.Default.BarChart, "nav_statistics")
    object Rotation : Screen("Rotasyon", Icons.Default.RotateRight, "nav_rotation")
    object Reminders : Screen("Hatırlatıcı", Icons.Default.Alarm, "nav_reminders")
    object Stock : Screen("Stok", Icons.Default.Inventory2, "nav_stock")
    object History : Screen("Geçmiş", Icons.Default.History, "nav_history")
}

class MainActivity : ComponentActivity() {
    private val viewModel: InsulinViewModel by viewModels()
    private val intentDoseTrigger = mutableStateOf<Long?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        handleIntent(intent)

        setContent {
            MyApplicationTheme {
                val context = LocalContext.current
                val appLanguage by viewModel.appLanguage.collectAsStateWithLifecycle()
                var hasAcceptedConsent by remember {
                    mutableStateOf(PrivacyConsentManager.isConsentAccepted(context))
                }

                if (!hasAcceptedConsent) {
                    PrivacyConsentGateScreen(
                        lang = appLanguage,
                        onLanguageSelected = { lang -> viewModel.setLanguage(lang) },
                        onAccepted = {
                            hasAcceptedConsent = true
                        }
                    )
                } else {
                    MainApp(viewModel = viewModel, intentDoseId = intentDoseTrigger.value)
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        if (intent?.getBooleanExtra(NotificationHelper.EXTRA_OPEN_BG_DIALOG, false) == true) {
            val doseId = intent.getLongExtra(NotificationHelper.EXTRA_DOSE_ID, -1L)
            if (doseId != -1L) {
                intentDoseTrigger.value = doseId
            }
        }
    }
}

@Composable
fun MainApp(viewModel: InsulinViewModel, intentDoseId: Long? = null) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    // Request notification permission if Android 13+
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { /* Handled */ }
    )

    val requestNotificationPermIfNeeded = {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val hasPermission = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            if (!hasPermission) {
                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    LaunchedEffect(Unit) {
        requestNotificationPermIfNeeded()

        viewModel.eventFlow.collectLatest { event ->
            when (event) {
                is UiEvent.ShowToast -> {
                    Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
                }
                is UiEvent.StockLowAlert -> {
                    // Stock section removed per user request
                }
            }
        }
    }

    // State collections
    val todaySummary by viewModel.todaySummary.collectAsStateWithLifecycle()
    val todayDoses by viewModel.todayDoses.collectAsStateWithLifecycle()
    val allDoses by viewModel.allDoses.collectAsStateWithLifecycle()
    val reminders by viewModel.reminders.collectAsStateWithLifecycle()
    val lastDose by viewModel.lastDose.collectAsStateWithLifecycle()
    val activeReminder by viewModel.activeReminderDue.collectAsStateWithLifecycle()
    val pendingGlucoseCheckDose by viewModel.pendingGlucoseCheckDose.collectAsStateWithLifecycle()
    val appLanguage by viewModel.appLanguage.collectAsStateWithLifecycle()
    val recommendedSite = viewModel.getRecommendedSite()

    var activeGlucoseDose by remember { mutableStateOf<DoseRecord?>(null) }

    LaunchedEffect(intentDoseId, allDoses) {
        if (intentDoseId != null && intentDoseId != -1L) {
            val target = allDoses.find { it.id == intentDoseId } ?: pendingGlucoseCheckDose
            if (target != null) {
                activeGlucoseDose = target
            }
        }
    }

    var selectedTab by remember { mutableIntStateOf(0) }
    val screens = listOf(
        Screen.Home,
        Screen.Statistics,
        Screen.Rotation,
        Screen.Reminders,
        Screen.History
    )

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            NavigationBar(
                modifier = Modifier.testTag("bottom_navigation_bar"),
                containerColor = com.example.ui.theme.HealthSurface
            ) {
                screens.forEachIndexed { index, screen ->
                    val labelText = when (screen) {
                        Screen.Home -> com.example.util.Strings.navHome(appLanguage)
                        Screen.Statistics -> com.example.util.Strings.navStatistics(appLanguage)
                        Screen.Rotation -> com.example.util.Strings.navRotation(appLanguage)
                        Screen.Reminders -> com.example.util.Strings.navReminders(appLanguage)
                        Screen.Stock -> com.example.util.Strings.navStock(appLanguage)
                        Screen.History -> com.example.util.Strings.navHistory(appLanguage)
                    }
                    NavigationBarItem(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        icon = { Icon(imageVector = screen.icon, contentDescription = labelText) },
                        label = {
                            Text(
                                text = labelText,
                                maxLines = 1,
                                fontSize = 10.sp,
                                fontWeight = if (selectedTab == index) FontWeight.ExtraBold else FontWeight.SemiBold
                            )
                        },
                        modifier = Modifier.testTag(screen.tag),
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = com.example.ui.theme.IconOnAccent,
                            selectedTextColor = com.example.ui.theme.HealthTealBright,
                            indicatorColor = com.example.ui.theme.HealthTeal,
                            unselectedIconColor = com.example.ui.theme.TextHealthSecondary,
                            unselectedTextColor = com.example.ui.theme.TextHealthSecondary
                        )
                    )
                }
            }
        }
    ) { paddingValues ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            color = MaterialTheme.colorScheme.background
        ) {
            when (selectedTab) {
                0 -> HomeScreen(
                    todaySummary = todaySummary,
                    todayDoses = todayDoses,
                    lastDose = lastDose,
                    recommendedSite = recommendedSite,
                    activeReminder = activeReminder,
                    pendingGlucoseCheckDose = pendingGlucoseCheckDose,
                    allDoses = allDoses,
                    appLanguage = appLanguage,
                    onLanguageSelected = { lang -> viewModel.setLanguage(lang) },
                    onConfirmActiveReminder = { reminder ->
                        requestNotificationPermIfNeeded()
                        viewModel.confirmDose(reminder)
                    },
                    onSnoozeActiveReminder15 = { reminder ->
                        requestNotificationPermIfNeeded()
                        viewModel.snoozeReminder(reminder)
                    },
                    onRecordDose = { units, type, site, mealTag, bg, notes, brand, trackFasting, trackPost ->
                        if (trackFasting || trackPost) {
                            requestNotificationPermIfNeeded()
                        }
                        viewModel.recordManualDose(
                            units = units,
                            type = type,
                            site = site,
                            mealTag = mealTag,
                            bloodGlucose = bg,
                            notes = notes,
                            insulinName = brand,
                            trackFastingGlucose = trackFasting,
                            trackPostGlucose = trackPost
                        )
                    },
                    onOpenGlucoseDialogForDose = { dose -> activeGlucoseDose = dose },
                    onSendTestGlucoseNotification = { doseId, units, type ->
                        requestNotificationPermIfNeeded()
                        viewModel.triggerInstant2HourGlucoseNotification(doseId, units, type)
                    },
                    onDeleteDose = { dose -> viewModel.deleteDose(dose) },
                    onNavigateToRotation = { selectedTab = 2 },
                    onNavigateToReminders = { selectedTab = 3 }
                )
                1 -> StatisticsScreen(
                    doses = allDoses,
                    lang = appLanguage
                )
                2 -> RotationScreen(
                    lastDose = lastDose,
                    recommendedSite = recommendedSite,
                    allDoses = allDoses,
                    lang = appLanguage
                )
                3 -> RemindersScreen(
                    reminders = reminders,
                    recommendedSite = recommendedSite,
                    onSaveReminder = { reminder -> viewModel.saveReminder(reminder) },
                    onToggleReminder = { reminder, enabled -> viewModel.toggleReminder(reminder, enabled) },
                    onDeleteReminder = { reminder -> viewModel.deleteReminder(reminder) },
                    lang = appLanguage
                )
                4 -> HistoryScreen(
                    doses = allDoses,
                    onDeleteDose = { dose -> viewModel.deleteDose(dose) },
                    lang = appLanguage,
                    onSeedTrialData = { viewModel.seedOneWeekTrialData(force = true) }
                )
            }
        }
    }

    activeGlucoseDose?.let { dose ->
        BloodGlucoseEntryDialog(
            dose = dose,
            onSaveGlucose = { doseId, glucose ->
                viewModel.updateDosePostGlucose(doseId, glucose)
                activeGlucoseDose = null
            },
            onSendTestNotification = { doseId, units, type ->
                requestNotificationPermIfNeeded()
                viewModel.triggerInstant2HourGlucoseNotification(doseId, units, type)
            },
            onDismiss = {
                activeGlucoseDose = null
            },
            lang = appLanguage
        )
    }
}
