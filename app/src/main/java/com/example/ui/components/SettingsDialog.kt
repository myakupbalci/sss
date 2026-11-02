package com.example.ui.components

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.ui.theme.AlertPharmacyRuby
import com.example.ui.theme.AppAccentColor
import com.example.ui.theme.AppThemeMode
import com.example.ui.theme.BioGreenContainer
import com.example.ui.theme.BioHealingGreen
import com.example.ui.theme.HealthBg
import com.example.ui.theme.HealthBorder
import com.example.ui.theme.HealthMint
import com.example.ui.theme.HealthSurface
import com.example.ui.theme.HealthSurfaceVariant
import com.example.ui.theme.HealthTeal
import com.example.ui.theme.HealthTealBright
import com.example.ui.theme.TextHealthMuted
import com.example.ui.theme.TextHealthPrimary
import com.example.ui.theme.TextHealthSecondary
import com.example.ui.theme.ThemeState
import com.example.util.AppLanguage

private fun checkNotificationsEnabled(context: Context): Boolean {
    val managerEnabled = NotificationManagerCompat.from(context).areNotificationsEnabled()
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        val permGranted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED
        permGranted && managerEnabled
    } else {
        managerEnabled
    }
}

private fun openSystemNotificationSettings(context: Context) {
    try {
        val intent = Intent().apply {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                action = Settings.ACTION_APP_NOTIFICATION_SETTINGS
                putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
            } else {
                action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                data = Uri.fromParts("package", context.packageName, null)
            }
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    } catch (_: Exception) {
        try {
            val fallback = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.fromParts("package", context.packageName, null)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(fallback)
        } catch (_: Exception) {
        }
    }
}

@Composable
fun SettingsDialog(
    lang: AppLanguage,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val isEn = lang == AppLanguage.EN

    var notificationsEnabled by remember {
        mutableStateOf(checkNotificationsEnabled(context))
    }
    var showPrivacyDetails by remember { mutableStateOf(true) }

    // Refresh notification state when returning from system settings
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                notificationsEnabled = checkNotificationsEnabled(context)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        notificationsEnabled = checkNotificationsEnabled(context)
        if (granted) {
            Toast.makeText(
                context,
                if (isEn) "✓ Notifications enabled!" else "✓ Bildirim izni açıldı!",
                Toast.LENGTH_SHORT
            ).show()
        } else {
            openSystemNotificationSettings(context)
        }
    }

    val handleNotificationAction = {
        if (!notificationsEnabled && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val hasPerm = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            if (!hasPerm) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            } else {
                openSystemNotificationSettings(context)
            }
        } else {
            openSystemNotificationSettings(context)
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 24.dp)
                .testTag("settings_dialog"),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = HealthSurface),
            border = BorderStroke(1.4.dp, HealthBorder),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 1. HEADER
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(HealthTeal, RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = if (isEn) "App Settings" else "Uygulama Ayarları",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Black,
                                color = TextHealthPrimary
                            )
                            Text(
                                text = if (isEn) "Theme, colors, notifications & privacy" else "Tema, renk, bildirim izinleri ve gizlilik",
                                style = MaterialTheme.typography.bodySmall,
                                color = HealthMint,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(36.dp)
                            .background(HealthSurfaceVariant, CircleShape)
                            .testTag("close_settings_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = if (isEn) "Close" else "Kapat",
                            tint = TextHealthPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                // 2. THEME MODE: DAY (GÜNDÜZ) & NIGHT (GECE)
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    color = HealthSurfaceVariant,
                    border = BorderStroke(1.dp, HealthBorder)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (ThemeState.themeMode == AppThemeMode.DAY) Icons.Default.LightMode else Icons.Default.DarkMode,
                                contentDescription = null,
                                tint = HealthTealBright,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isEn) "THEME APPEARANCE (DAY / NIGHT)" else "TEMA GÖRÜNÜMÜ (GÜNDÜZ / GECE)",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = HealthMint,
                                letterSpacing = 0.5.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // Day Theme Card
                            val isDaySelected = ThemeState.themeMode == AppThemeMode.DAY
                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(14.dp))
                                    .clickable { ThemeState.themeMode = AppThemeMode.DAY }
                                    .testTag("theme_day_btn"),
                                shape = RoundedCornerShape(14.dp),
                                color = if (isDaySelected) HealthTeal.copy(alpha = 0.22f) else HealthSurface,
                                border = BorderStroke(
                                    width = if (isDaySelected) 2.dp else 1.dp,
                                    color = if (isDaySelected) HealthTealBright else HealthBorder
                                )
                            ) {
                                Column(
                                    modifier = Modifier.padding(12.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .background(Color(0xFFFEF3C7), CircleShape)
                                            .border(1.dp, Color(0xFFF59E0B), CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.LightMode,
                                            contentDescription = null,
                                            tint = Color(0xFFD97706),
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = if (isEn) "Day Theme" else "Gündüz Teması",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = TextHealthPrimary
                                    )
                                    Text(
                                        text = if (isEn) "Light & Fresh" else "Aydınlık Görünüm",
                                        fontSize = 10.sp,
                                        color = TextHealthMuted,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }

                            // Night Theme Card
                            val isNightSelected = ThemeState.themeMode == AppThemeMode.NIGHT
                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(14.dp))
                                    .clickable { ThemeState.themeMode = AppThemeMode.NIGHT }
                                    .testTag("theme_night_btn"),
                                shape = RoundedCornerShape(14.dp),
                                color = if (isNightSelected) HealthTeal.copy(alpha = 0.22f) else HealthSurface,
                                border = BorderStroke(
                                    width = if (isNightSelected) 2.dp else 1.dp,
                                    color = if (isNightSelected) HealthTealBright else HealthBorder
                                )
                            ) {
                                Column(
                                    modifier = Modifier.padding(12.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .background(Color(0xFF0F172A), CircleShape)
                                            .border(1.dp, Color(0xFF38BDF8), CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.DarkMode,
                                            contentDescription = null,
                                            tint = Color(0xFF38BDF8),
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = if (isEn) "Night Theme" else "Gece Teması",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = TextHealthPrimary
                                    )
                                    Text(
                                        text = if (isEn) "Dark Medical" else "Koyu Görünüm",
                                        fontSize = 10.sp,
                                        color = TextHealthMuted,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }
                }

                // 3. ACCENT COLOR SELECTION (TEMA RENGİ)
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    color = HealthSurfaceVariant,
                    border = BorderStroke(1.dp, HealthBorder)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Palette,
                                contentDescription = null,
                                tint = HealthTealBright,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isEn) "THEME ACCENT COLOR" else "TEMA RENK SEÇİMİ",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = HealthMint,
                                letterSpacing = 0.5.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            AppAccentColor.entries.forEach { accent ->
                                val isSelected = ThemeState.accentColor == accent
                                Surface(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(12.dp))
                                        .clickable { ThemeState.accentColor = accent }
                                        .testTag("accent_${accent.name.lowercase()}"),
                                    shape = RoundedCornerShape(12.dp),
                                    color = HealthSurface,
                                    border = BorderStroke(
                                        width = if (isSelected) 2.dp else 1.dp,
                                        color = if (isSelected) accent.previewColor else HealthBorder
                                    )
                                ) {
                                    Column(
                                        modifier = Modifier.padding(vertical = 10.dp, horizontal = 4.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(26.dp)
                                                .background(accent.previewColor, CircleShape),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            if (isSelected) {
                                                Icon(
                                                    imageVector = Icons.Default.Check,
                                                    contentDescription = null,
                                                    tint = Color.White,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(
                                            text = if (isEn) accent.titleEn.substringBefore(" ") else accent.titleTr.substringBefore(" "),
                                            fontSize = 10.sp,
                                            fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.SemiBold,
                                            color = TextHealthPrimary,
                                            maxLines = 1
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // 4. APP NOTIFICATION PERMISSIONS (UYGULAMA BİLDİRİM İZİNLERİ)
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    color = HealthSurfaceVariant,
                    border = BorderStroke(1.dp, HealthBorder)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .background(
                                            if (notificationsEnabled) BioGreenContainer else AlertPharmacyRuby.copy(alpha = 0.18f),
                                            RoundedCornerShape(10.dp)
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = if (notificationsEnabled) Icons.Default.NotificationsActive else Icons.Default.NotificationsOff,
                                        contentDescription = null,
                                        tint = if (notificationsEnabled) BioHealingGreen else AlertPharmacyRuby,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = if (isEn) "App Notification Permissions" else "Uygulama Bildirim İzinleri",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = TextHealthPrimary
                                    )
                                    Text(
                                        text = if (notificationsEnabled) {
                                            if (isEn) "Active • Dose & 2-hr glucose alerts enabled" else "Açık • Doz ve 2. saat şeker uyarıları aktif"
                                        } else {
                                            if (isEn) "Disabled • Tap to enable notifications" else "Kapalı • Bildirimleri açmak için dokunun"
                                        },
                                        fontSize = 11.sp,
                                        color = if (notificationsEnabled) BioHealingGreen else AlertPharmacyRuby,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }

                            Switch(
                                checked = notificationsEnabled,
                                onCheckedChange = { handleNotificationAction() },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = BioHealingGreen,
                                    uncheckedThumbColor = TextHealthMuted,
                                    uncheckedTrackColor = HealthSurface
                                ),
                                modifier = Modifier.testTag("notification_permission_switch")
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Button(
                            onClick = { handleNotificationAction() },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (notificationsEnabled) HealthTeal else BioHealingGreen
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("open_notification_settings_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.NotificationsActive,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (notificationsEnabled) {
                                    if (isEn) "Manage Notification Settings" else "Bildirim İzinlerini Yönet / Ayarlar"
                                } else {
                                    if (isEn) "Enable App Notifications" else "Uygulama Bildirimlerini Aç"
                                },
                                fontSize = 13.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White
                            )
                        }
                    }
                }

                // 5. PRIVACY POLICY (GİZLİLİK İLKELERİ)
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    color = HealthSurfaceVariant,
                    border = BorderStroke(1.dp, HealthBorder)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showPrivacyDetails = !showPrivacyDetails }
                                .testTag("privacy_policy_header"),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.PrivacyTip,
                                    contentDescription = null,
                                    tint = HealthTealBright,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = if (isEn) "Privacy Policy & Data Security" else "Gizlilik İlkeleri ve Veri Güvenliği",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = TextHealthPrimary
                                    )
                                    Text(
                                        text = if (isEn) "100% On-Device Local Medical Storage" else "%100 Cihaz İçi Güvenli Yerel Depolama",
                                        fontSize = 11.sp,
                                        color = HealthMint,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }

                            Icon(
                                imageVector = if (showPrivacyDetails) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                contentDescription = null,
                                tint = TextHealthPrimary
                            )
                        }

                        AnimatedVisibility(visible = showPrivacyDetails) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 10.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                getPrivacyPolicyArticles(isEn).forEach { article ->
                                    PrivacyPolicyArticleCard(
                                        article = article,
                                        isEn = isEn,
                                        notificationsGranted = notificationsEnabled,
                                        onRequestNotificationPermission = handleNotificationAction
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
