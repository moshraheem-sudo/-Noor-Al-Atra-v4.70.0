package com.example.ui.screens.prayer

import android.Manifest
import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.model.AppLanguage
import com.example.data.model.ThemeMode
import com.example.ui.AppTab
import com.example.ui.PrayerTimesViewModel
import com.example.ui.components.AdhanNowPlayingBanner
import com.example.ui.components.AppBottomNavBar
import com.example.ui.components.AppUpdateDialog
import com.example.ui.theme.AppColors
import com.example.ui.theme.PrayerTimesTheme
import com.example.utils.AppStrings
import com.example.utils.LocationHelper
import com.example.utils.PrayerNotificationHelper
import com.example.utils.UpdateCheckStatus

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrayerTimesMainScreen(
    onBack: () -> Unit,
    initialTab: AppTab = AppTab.PRAYER_TIMES,
    viewModel: PrayerTimesViewModel = viewModel()
) {
    LaunchedEffect(initialTab) {
        if (initialTab != AppTab.PRAYER_TIMES) {
            viewModel.selectTab(initialTab)
        }
    }

    val appLanguage by viewModel.appLanguage.collectAsState()
    val activeThemeMode = com.example.ui.theme.ThemeManager.currentThemeMode

    PrayerTimesTheme(themeMode = activeThemeMode) {
        val layoutDirection = if (appLanguage == AppLanguage.ARABIC) LayoutDirection.Rtl else LayoutDirection.Ltr
        CompositionLocalProvider(LocalLayoutDirection provides layoutDirection) {
            PrayerAppContent(
                viewModel = viewModel,
                themeMode = viewModel.themeMode.collectAsState().value,
                onBack = onBack
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrayerAppContent(
    viewModel: PrayerTimesViewModel = viewModel(),
    themeMode: ThemeMode = ThemeMode.LIGHT,
    onBack: () -> Unit = {}
) {
    val context = LocalContext.current
    val currentTab by viewModel.currentTab.collectAsState()
    val appLanguage by viewModel.appLanguage.collectAsState()
    val selectedCity by viewModel.selectedCity.collectAsState()
    val prayerData by viewModel.prayerTimesData.collectAsState()
    val nextPrayer by viewModel.nextPrayer.collectAsState()
    val liveTime by viewModel.currentLiveTime.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val isBackgroundSyncing by viewModel.isBackgroundSyncing.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val isOfflineMode by viewModel.isOfflineMode.collectAsState()
    val notificationsMap by viewModel.notificationsMap.collectAsState()
    val prayerVisibilityMap by viewModel.prayerVisibilityMap.collectAsState()
    val calculationMethod by viewModel.calculationMethod.collectAsState()
    val midnightMethod by viewModel.midnightMethod.collectAsState()
    val showAsrSeparate by viewModel.showAsrSeparate.collectAsState()
    val showIshaSeparate by viewModel.showIshaSeparate.collectAsState()
    val isGpsLocating by viewModel.isGpsLocating.collectAsState()
    val gpsStatusMessage by viewModel.gpsStatusMessage.collectAsState()
    val appUpdateStatus by viewModel.updateStatus.collectAsStateWithLifecycle()
    var showUpdateDialog by remember { mutableStateOf(false) }

    val adhanPlaybackState by com.example.utils.AdhanAudioService.playbackState.collectAsStateWithLifecycle()
    val isAdhanPlaying = adhanPlaybackState.status == com.example.utils.AdhanPlaybackStatus.PLAYING
    val onStopAdhan: () -> Unit = {
        com.example.utils.AdhanAudioService.stopAdhan(context)
    }

    LaunchedEffect(appUpdateStatus) {
        if (appUpdateStatus is UpdateCheckStatus.UpdateAvailable ||
            appUpdateStatus is UpdateCheckStatus.Downloading ||
            appUpdateStatus is UpdateCheckStatus.DownloadReady ||
            appUpdateStatus is UpdateCheckStatus.PermissionRequired
        ) {
            showUpdateDialog = true
        }
    }

    var userRequestedGps by remember { mutableStateOf(false) }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (granted && userRequestedGps) {
            userRequestedGps = false
            if (!LocationHelper.isLocationServiceEnabled(context)) {
                android.widget.Toast.makeText(context, AppStrings.gpsToastEnable(appLanguage), android.widget.Toast.LENGTH_LONG).show()
                LocationHelper.openLocationSettings(context)
                viewModel.setGpsStatusMessage(AppStrings.gpsStatusOpeningSettings(appLanguage))
            } else {
                viewModel.locateViaGps(context, forceRefresh = true)
            }
        } else {
            userRequestedGps = false
        }
    }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { _ -> }

    val onTriggerGps = {
        userRequestedGps = true
        if (!LocationHelper.hasLocationPermission(context)) {
            locationPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        } else if (!LocationHelper.isLocationServiceEnabled(context)) {
            userRequestedGps = false
            android.widget.Toast.makeText(context, AppStrings.gpsToastEnable(appLanguage), android.widget.Toast.LENGTH_LONG).show()
            LocationHelper.openLocationSettings(context)
            viewModel.setGpsStatusMessage(AppStrings.gpsStatusDisabled(appLanguage))
        } else {
            userRequestedGps = false
            viewModel.locateViaGps(context, forceRefresh = true)
        }
    }

    LaunchedEffect(Unit) {
        PrayerNotificationHelper.createNotificationChannel(context)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
        viewModel.checkForAppUpdates()
    }

    BackHandler(enabled = currentTab == AppTab.SETTINGS) {
        viewModel.selectTab(AppTab.PRAYER_TIMES)
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = if (currentTab == AppTab.SETTINGS) {
                                if (appLanguage == AppLanguage.ARABIC) "إعدادات المواقيت" else "Prayer Settings"
                            } else {
                                AppStrings.appTitle(appLanguage)
                            },
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = AppColors.current.textTitle
                        )
                        val isAr = appLanguage == AppLanguage.ARABIC
                        val cityName = if (isAr) selectedCity.nameAr else selectedCity.nameEn
                        if (currentTab != AppTab.SETTINGS) {
                            Text(
                                text = cityName,
                                style = MaterialTheme.typography.bodySmall,
                                color = AppColors.current.tealAccentLight,
                                fontSize = 12.sp
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = {
                        if (currentTab == AppTab.SETTINGS) {
                            viewModel.selectTab(AppTab.PRAYER_TIMES)
                        } else {
                            onBack()
                        }
                    }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "الرجوع",
                            tint = AppColors.current.textTitle
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            if (currentTab == AppTab.SETTINGS) {
                                viewModel.selectTab(AppTab.PRAYER_TIMES)
                            } else {
                                viewModel.selectTab(AppTab.SETTINGS)
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = if (appLanguage == AppLanguage.ARABIC) "الإعدادات" else "Settings",
                            tint = if (currentTab == AppTab.SETTINGS) AppColors.current.tealAccentLight else AppColors.current.textTitle
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = AppColors.current.surface
                )
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentTab) {
                AppTab.PRAYER_TIMES -> {
                    PrayerHomeScreen(
                        currentLanguage = appLanguage,
                        prayerData = prayerData,
                        nextPrayer = nextPrayer,
                        liveTime = liveTime,
                        selectedCity = selectedCity,
                        onSelectCity = { city -> viewModel.changeCity(city) },
                        notificationsMap = notificationsMap,
                        prayerVisibilityMap = prayerVisibilityMap,
                        errorMessage = errorMessage,
                        isOfflineMode = isOfflineMode,
                        isBackgroundSyncing = isBackgroundSyncing,
                        isLoading = isLoading,
                        isGpsLocating = isGpsLocating,
                        gpsStatusMessage = gpsStatusMessage,
                        onGpsClick = onTriggerGps,
                        onRefreshClick = { viewModel.refreshPrayerTimes() },
                        onToggleNotification = { prayerType: com.example.data.model.PrayerType -> viewModel.toggleNotification(prayerType) }
                    )
                }
                AppTab.SETTINGS -> {
                    val hijriSyncStatus by viewModel.hijriSyncStatus.collectAsStateWithLifecycle()
                    val githubAutoSyncEnabled by viewModel.githubAutoSyncEnabled.collectAsStateWithLifecycle()
                    val githubRepoOwner by viewModel.githubRepoOwner.collectAsStateWithLifecycle()
                    val githubRepoName by viewModel.githubRepoName.collectAsStateWithLifecycle()
                    val githubToken by viewModel.githubToken.collectAsStateWithLifecycle()
                    val githubWebhookUrl by viewModel.githubWebhookUrl.collectAsStateWithLifecycle()
                    val isSendingDispatch by viewModel.isSendingDispatch.collectAsStateWithLifecycle()
                    val dispatchStatusMessage by viewModel.dispatchStatusMessage.collectAsStateWithLifecycle()
                    val lastHijriSyncTime by viewModel.lastHijriSyncTime.collectAsStateWithLifecycle()
                    val prayerOffsets by viewModel.prayerOffsets.collectAsStateWithLifecycle()
                    val manualHijriOffset by viewModel.manualHijriOffset.collectAsStateWithLifecycle()
                    val manualHijriDateOverride by viewModel.manualHijriDateOverride.collectAsStateWithLifecycle()
                    val selectedMuezzin by viewModel.selectedMuezzin.collectAsStateWithLifecycle()
                    val isAdhanAudioEnabled by viewModel.isAdhanAudioEnabled.collectAsStateWithLifecycle()
                    val adhanPlaybackState by viewModel.adhanPlaybackState.collectAsStateWithLifecycle()
                    val muezzinDownloadStatuses by viewModel.muezzinDownloadStatuses.collectAsStateWithLifecycle()
                    val prayerAlarmConfigs by viewModel.prayerAlarmConfigs.collectAsStateWithLifecycle()

                    PrayerSettingsScreen(
                        currentLanguage = appLanguage,
                        onLanguageChange = { lang: AppLanguage -> viewModel.setAppLanguage(lang) },
                        currentThemeMode = themeMode,
                        onThemeModeChange = { mode: ThemeMode -> viewModel.setThemeMode(mode) },
                        selectedCity = selectedCity,
                        onSelectCity = { city -> viewModel.changeCity(city) },
                        prayerData = prayerData,
                        isGpsLocating = isGpsLocating,
                        gpsStatusMessage = gpsStatusMessage,
                        onGpsLocate = onTriggerGps,
                        calculationMethod = calculationMethod,
                        onCalculationMethodChange = { method: com.example.data.model.CalculationMethod -> viewModel.setCalculationMethod(method) },
                        midnightMethod = midnightMethod,
                        onMidnightMethodChange = { method: com.example.data.model.MidnightMethod -> viewModel.setMidnightMethod(method) },
                        showAsrSeparate = showAsrSeparate,
                        onShowAsrSeparateChange = { show: Boolean -> viewModel.setShowAsrSeparate(show) },
                        showIshaSeparate = showIshaSeparate,
                        onShowIshaSeparateChange = { show: Boolean -> viewModel.setShowIshaSeparate(show) },
                        notificationsMap = notificationsMap,
                        onToggleNotification = { prayerType: com.example.data.model.PrayerType -> viewModel.toggleNotification(prayerType) },
                        prayerAlarmConfigs = prayerAlarmConfigs,
                        onUpdatePrayerAlarmConfig = { config: com.example.data.model.PrayerCustomAlarmConfig -> viewModel.updatePrayerAlarmConfig(config) },
                        onPreviewCustomPrayerAlarm = { config: com.example.data.model.PrayerCustomAlarmConfig -> viewModel.previewCustomPrayerAlarm(config) },
                        prayerVisibilityMap = prayerVisibilityMap,
                        onTogglePrayerVisibility = { prayerType: com.example.data.model.PrayerType -> viewModel.togglePrayerVisibility(prayerType) },
                        hijriSyncStatus = hijriSyncStatus,
                        onSyncHijriDate = { viewModel.syncHijriDate() },
                        githubAutoSyncEnabled = githubAutoSyncEnabled,
                        onGithubAutoSyncChange = { enabled: Boolean -> viewModel.setGithubAutoSyncEnabled(enabled) },
                        githubRepoOwner = githubRepoOwner,
                        githubRepoName = githubRepoName,
                        githubToken = githubToken,
                        githubWebhookUrl = githubWebhookUrl,
                        onSaveGithubSettings = { owner: String, name: String, token: String, webhook: String ->
                            viewModel.saveGithubSettings(owner, name, token, webhook)
                        },
                        isSendingDispatch = isSendingDispatch,
                        dispatchStatusMessage = dispatchStatusMessage,
                        onSendGithubDispatch = { viewModel.sendGithubDispatch() },
                        lastHijriSyncTime = lastHijriSyncTime,
                        onTestNotification = { viewModel.sendTestNotification() },
                        prayerOffsets = prayerOffsets,
                        onAdjustPrayerOffset = { prayerType: com.example.data.model.PrayerType, delta: Int -> viewModel.adjustPrayerOffset(prayerType, delta) },
                        onResetAllPrayerOffsets = { viewModel.resetAllPrayerOffsets() },
                        manualHijriOffset = manualHijriOffset,
                        onAdjustManualHijriOffset = { delta: Int -> viewModel.adjustManualHijriOffset(delta) },
                        manualHijriCustomDate = manualHijriDateOverride,
                        onSetManualHijriCustomDate = { customDate: String? -> viewModel.setManualHijriCustomDate(customDate) },
                        onResetManualHijri = { viewModel.resetManualHijri() },
                        selectedMuezzin = selectedMuezzin,
                        onSelectMuezzin = { muezzin: com.example.data.model.Muezzin -> viewModel.setSelectedMuezzin(muezzin) },
                        isAdhanAudioEnabled = isAdhanAudioEnabled,
                        onToggleAdhanAudio = { enabled: Boolean -> viewModel.setAdhanAudioEnabled(enabled) },
                        adhanPlaybackState = adhanPlaybackState,
                        onPreviewMuezzin = { muezzin: com.example.data.model.Muezzin -> viewModel.previewMuezzin(muezzin) },
                        onStopAdhanPlayback = { viewModel.stopAdhanPlayback() },
                        muezzinDownloadStatuses = muezzinDownloadStatuses,
                        onDownloadAllMuezzins = { viewModel.downloadAllMuezzins() },
                        onDownloadMuezzin = { muezzin: com.example.data.model.Muezzin -> viewModel.downloadMuezzin(muezzin) },
                        appUpdateStatus = appUpdateStatus,
                        onCheckForUpdates = { viewModel.checkForAppUpdates() },
                        onDownloadAndInstallUpdate = { url: String, name: String -> viewModel.downloadAndInstallAppUpdate(url, name) },
                        onCancelDownload = { viewModel.cancelAppUpdateDownload() },
                        onResetUpdateStatus = { viewModel.resetAppUpdateStatus() }
                    )
                }
            }

            AnimatedVisibility(
                visible = isAdhanPlaying,
                enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
                modifier = Modifier.align(Alignment.BottomCenter)
            ) {
                AdhanNowPlayingBanner(
                    currentLanguage = appLanguage,
                    onStop = onStopAdhan,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }
        }
    }

    if (showUpdateDialog && (
        appUpdateStatus is UpdateCheckStatus.UpdateAvailable ||
        appUpdateStatus is UpdateCheckStatus.Downloading ||
        appUpdateStatus is UpdateCheckStatus.DownloadReady ||
        appUpdateStatus is UpdateCheckStatus.PermissionRequired ||
        appUpdateStatus is UpdateCheckStatus.Error
    )) {
        AppUpdateDialog(
            updateStatus = appUpdateStatus,
            currentLanguage = appLanguage,
            onDismiss = {
                showUpdateDialog = false
                viewModel.resetAppUpdateStatus()
            },
            onDownloadNow = { downloadUrl, apkFileName ->
                viewModel.downloadAndInstallAppUpdate(downloadUrl, apkFileName)
            },
            onCancelDownload = {
                viewModel.cancelAppUpdateDownload()
                showUpdateDialog = false
            },
            onRetryCheck = {
                viewModel.checkForAppUpdates()
            }
        )
    }
}
