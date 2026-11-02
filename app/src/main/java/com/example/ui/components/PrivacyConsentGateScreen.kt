package com.example.ui.components

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.receiver.NotificationHelper
import com.example.ui.theme.AlertPharmacyRuby
import com.example.ui.theme.AlertRubyContainer
import com.example.ui.theme.BioGreenContainer
import com.example.ui.theme.BioHealingGreen
import com.example.ui.theme.ChartSlotBg
import com.example.ui.theme.HealthBg
import com.example.ui.theme.HealthBorder
import com.example.ui.theme.HealthCyanBright
import com.example.ui.theme.HealthMint
import com.example.ui.theme.HealthSurface
import com.example.ui.theme.HealthSurfaceVariant
import com.example.ui.theme.HealthTeal
import com.example.ui.theme.HealthTealBright
import com.example.ui.theme.IconOnAccent
import com.example.ui.theme.TextHealthPrimary
import com.example.ui.theme.TextHealthSecondary
import com.example.ui.theme.WarningAmberContainer
import com.example.ui.theme.WarningAmberHealth
import com.example.util.AppLanguage

object PrivacyConsentManager {
    private const val PREFS_NAME = "insulintracker_privacy_prefs"
    private const val KEY_CONSENT_ACCEPTED = "privacy_and_medical_consent_accepted_v1"

    fun isConsentAccepted(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean(KEY_CONSENT_ACCEPTED, false)
    }

    fun setConsentAccepted(context: Context, accepted: Boolean) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(KEY_CONSENT_ACCEPTED, accepted).apply()
    }
}

data class PrivacyArticleItem(
    val number: Int,
    val title: String,
    val description: String,
    val isLegalMedicalDisclaimer: Boolean = false,
    val isNotificationArticle: Boolean = false
)

fun getPrivacyPolicyArticles(isEn: Boolean): List<PrivacyArticleItem> {
    return if (isEn) {
        listOf(
            PrivacyArticleItem(
                number = 1,
                title = "1. On-Device Local Data Storage",
                description = "All insulin doses (Basal & Bolus), fasting and 2-hour postprandial blood glucose measurements, injection site rotation history, and reminder schedules you record are stored exclusively on your own device's local database. Your records are never automatically uploaded to external cloud servers."
            ),
            PrivacyArticleItem(
                number = 2,
                title = "2. Zero Third-Party Sharing & Data Security",
                description = "Your personal health data is strictly confidential and is never sold, shared with advertising networks, analytics companies, or third-party entities. Uninstalling the app or clearing app storage permanently deletes all locally stored health records."
            ),
            PrivacyArticleItem(
                number = 3,
                title = "3. User Control & Data Accuracy",
                description = "You retain full control over all data entered into the application. The accuracy of entered insulin units (IU), blood glucose values (mg/dL), injection sites, and timestamps is the sole responsibility of the user. You can review or delete your history at any time."
            ),
            PrivacyArticleItem(
                number = 4,
                title = "4. Legal Medical Disclaimer — Strictly For Tracking Only (Not Medical Advice)",
                description = "IMPORTANT LEGAL NOTICE: This application is NOT a medical device and the information, charts, average glucose/insulin profiles, injection site rotation suggestions, and reminders provided herein DO NOT constitute medical advice, clinical diagnosis, prescription, or professional medical direction. This application is provided strictly as a personal logbook and self-tracking tool.\n\n" +
                    "• Never start, stop, or alter your insulin dosage, medication schedule, or diabetes treatment plan based solely on information displayed in this app. Always consult your physician or qualified healthcare provider for any medical decisions.\n" +
                    "• In case of any medical emergency (such as severe hypoglycemia or hyperglycemia), call emergency services (112 / 911) or visit the nearest hospital immediately.\n" +
                    "• By using this app, you acknowledge and agree that the developer assumes no legal or medical liability for any direct or indirect health outcomes, user data entry errors, or delayed notifications.",
                isLegalMedicalDisclaimer = true
            ),
            PrivacyArticleItem(
                number = 5,
                title = "5. Phone Notifications, Reminder Alarms & Blood Glucose Alerts",
                description = "In order to send scheduled fixed-time Basal and mealtime Bolus insulin reminders, pre-dose fasting blood glucose prompts, and 2-hour postprandial blood glucose measurement alerts directly to your phone's notification panel, the app requests Notification Permission (POST_NOTIFICATIONS) and Exact Alarm Permission (SCHEDULE_EXACT_ALARM). These permissions are used solely to deliver your health reminders to your phone and can be managed at any time in Settings.",
                isNotificationArticle = true
            )
        )
    } else {
        listOf(
            PrivacyArticleItem(
                number = 1,
                title = "1. Cihaz İçi Yerel Veri Saklama",
                description = "Kaydettiğiniz tüm Bazal ve Bolus insülin dozları, açlık ve 2. saat tokluk kan şekeri ölçümleri, enjeksiyon rotasyon geçmişiniz ve hatırlatıcı ayarlarınız yalnızca kendi telefonunuzdaki yerel veritabanında saklanır. Verileriniz harici sunuculara otomatik olarak aktarılmaz."
            ),
            PrivacyArticleItem(
                number = 2,
                title = "2. Üçüncü Taraflarla Paylaşım Yoktur ve Veri Güvenliği",
                description = "Kişisel sağlık verileriniz hiçbir reklam ağı, analiz şirketi, veri aracı kurumları veya üçüncü taraf sunucularla kesinlikle paylaşılmaz, satılmaz ve ticari amaçla işlenmez. Uygulamayı telefonunuzdan kaldırdığınızda tüm yerel kayıtlar cihazınızdan silinir."
            ),
            PrivacyArticleItem(
                number = 3,
                title = "3. Kullanıcı Kontrolü ve Veri Doğruluğu",
                description = "Uygulamaya girilen insülin doz miktarlarının (IU), açlık/tokluk kan şekeri değerlerinin (mg/dL), enjeksiyon bölgelerinin ve saat kayıtlarının doğruluğu tamamen kullanıcının kendi kontrolündedir. Kayıtlarınızı dilediğiniz zaman inceleyebilir veya silebilirsiniz."
            ),
            PrivacyArticleItem(
                number = 4,
                title = "4. Yasal Tıbbi Sorumluluk Reddi — Tıbbi Yönlendirme Değildir, Sadece Takip Amaçlıdır",
                description = "ÖNEMLİ YASAL BİLGİLENDİRME VE SORUMLULUK REDDİ: Bu uygulama tıbbi bir cihaz değildir. Uygulama içerisinde yer alan hiçbir bilgi, istatistik, ortalama şeker/insülin profili, enjeksiyon bölgesi rotasyon önerisi veya hatırlatıcı bildirim KESİNLİKLE TIBBİ BİR TEŞHİS, TEDAVİ, REÇETE VEYA HEKİM YÖNLENDİRMESİ NİTELİĞİ TAŞIMAZ. Bu uygulama yalnızca kullanıcının kendi kişisel kayıtlarını tutması ve günlük takibini kolaylaştırması amacıyla sunulmaktadır.\n\n" +
                    "• İnsülin doz miktarlarınızı, ilaç saatlerinizi, tedavi planınızı veya kan şekeri müdahalelerinizi asla yalnızca bu uygulamadaki bilgilere dayanarak belirlemeyiniz ve değiştirmeyiniz. Her türlü doz ayarı ve tıbbi karar için mutlaka uzman hekiminize danışınız.\n" +
                    "• Hipoglisemi (düşük kan şekeri) veya hiperglisemi (yüksek kan şekeri) gibi acil tıbbi durumlarda derhal 112 Acil Yardım hattına veya en yakın sağlık kuruluşuna başvurunuz.\n" +
                    "• Uygulamanın kullanımından, yanlış veri girişinden veya bildirim/alarm gecikmelerinden doğabilecek doğrudan ya da dolaylı hiçbir sağlık ve hukuki sonuçtan uygulama geliştiricisi yasal olarak sorumlu tutulamaz.",
                isLegalMedicalDisclaimer = true
            ),
            PrivacyArticleItem(
                number = 5,
                title = "5. Telefon Bildirimleri, Hatırlatıcı Alarmlar ve Kan Şekeri İzinleri",
                description = "Uygulamada kurduğunuz Bazal/Bolus insülin hatırlatıcılarının, doz öncesi açlık kan şekeri taleplerinin ve insülin dozundan 2 saat sonraki tokluk kan şekeri ölçüm uyarılarının telefonunuza bildirim ve sesli/titreşimli alarm olarak gidebilmesi için Bildirim İzni (POST_NOTIFICATIONS) ve Alarm İzni (SCHEDULE_EXACT_ALARM) talep edilir. Bu izinler yalnızca sağlık hatırlatmalarınızı telefonunuza iletmek için kullanılır ve Ayarlar menüsünden her zaman yönetilebilir.",
                isNotificationArticle = true
            )
        )
    }
}

@Composable
fun PrivacyConsentGateScreen(
    lang: AppLanguage,
    onLanguageSelected: (AppLanguage) -> Unit,
    onAccepted: () -> Unit
) {
    val context = LocalContext.current
    val isEn = lang == AppLanguage.EN

    var isTermsChecked by remember { mutableStateOf(false) }
    var showDeclinedBlockingState by remember { mutableStateOf(false) }
    var showUncheckedError by remember { mutableStateOf(false) }
    var notificationsGranted by remember {
        mutableStateOf(NotificationHelper.areNotificationsGranted(context))
    }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        notificationsGranted = NotificationHelper.areNotificationsGranted(context)
        if (granted) {
            Toast.makeText(
                context,
                if (isEn) "✓ Phone notifications enabled!" else "✓ Telefon bildirim izni açıldı!",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    val completeAcceptance = {
        PrivacyConsentManager.setConsentAccepted(context, true)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val hasPerm = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            if (!hasPerm) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
        onAccepted()
    }

    // Prevent back button from bypassing consent gate; closes app instead
    BackHandler {
        (context as? Activity)?.finishAffinity()
    }

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .testTag("privacy_consent_gate_screen"),
        color = HealthBg
    ) {
        if (showDeclinedBlockingState) {
            // Declined State: App refuses to open until accepted or exited
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .navigationBarsPadding()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("privacy_declined_blocking_card"),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = HealthSurface),
                    border = BorderStroke(2.dp, AlertPharmacyRuby),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .background(AlertRubyContainer, CircleShape)
                                .border(2.dp, AlertPharmacyRuby, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                tint = AlertPharmacyRuby,
                                modifier = Modifier.size(34.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = if (isEn)
                                "Consent Required to Use the App"
                            else
                                "Onay Olmadan Uygulama Açılamaz",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Black,
                            color = TextHealthPrimary,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = if (isEn)
                                "To use this application, you must accept the Privacy Policy, Data Security terms, and the Legal Medical Disclaimer (Article 4) stating that this app is strictly for personal tracking and does not provide medical advice."
                            else
                                "Uygulamayı kullanabilmeniz için Gizlilik İlkelerini, Veri Güvenliği koşullarını ve 4. maddede yer alan 'bu uygulamanın tıbbi bir yönlendirme olmayıp yalnızca kişisel takip amaçlı olduğuna' dair Yasal Tıbbi Sorumluluk Reddini kabul etmeniz gerekmektedir. Kabul edilmediği takdirde uygulama açılmaz.",
                            fontSize = 13.sp,
                            lineHeight = 19.sp,
                            color = TextHealthSecondary,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(22.dp))

                        Button(
                            onClick = { showDeclinedBlockingState = false },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("review_terms_again_btn"),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = HealthTeal,
                                contentColor = IconOnAccent
                            )
                        ) {
                            Text(
                                text = if (isEn) "Review Terms & Accept" else "Koşulları Tekrar İncele ve Onayla",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 14.sp,
                                color = IconOnAccent
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedButton(
                            onClick = {
                                (context as? Activity)?.finishAffinity()
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("exit_app_btn"),
                            shape = RoundedCornerShape(14.dp),
                            border = BorderStroke(1.5.dp, AlertPharmacyRuby),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = AlertPharmacyRuby)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = null,
                                tint = AlertPharmacyRuby,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isEn) "Exit Application" else "Uygulamadan Çık (Kapat)",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.5.sp,
                                color = AlertPharmacyRuby
                            )
                        }
                    }
                }
            }
        } else {
            // Main First-Launch Consent Screen
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .navigationBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                // Top Bar with Title & TR/EN Switcher
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
                                .size(44.dp)
                                .background(HealthTeal, RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.PrivacyTip,
                                contentDescription = null,
                                tint = IconOnAccent,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = if (isEn) "Privacy & Legal Medical Consent" else "Gizlilik ve Yasal Tıbbi Onay",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Black,
                                color = TextHealthPrimary
                            )
                            Text(
                                text = if (isEn) "Mandatory First-Launch Agreement" else "Uygulama İlk Giriş Zorunlu Kullanıcı Onayı",
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = HealthMint
                            )
                        }
                    }

                    // TR / EN Language Switcher
                    Surface(
                        color = HealthSurfaceVariant,
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, HealthBorder)
                    ) {
                        Row(modifier = Modifier.padding(3.dp)) {
                            Surface(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { onLanguageSelected(AppLanguage.TR) },
                                color = if (lang == AppLanguage.TR) BioGreenContainer else Color.Transparent
                            ) {
                                Text(
                                    text = "🇹🇷 TR",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = if (lang == AppLanguage.TR) BioHealingGreen else TextHealthSecondary,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                            Surface(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { onLanguageSelected(AppLanguage.EN) },
                                color = if (lang == AppLanguage.EN) HealthSurface else Color.Transparent
                            ) {
                                Text(
                                    text = "🇬🇧 EN",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = if (lang == AppLanguage.EN) HealthCyanBright else TextHealthSecondary,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Scrollable Articles Container
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = HealthSurface),
                    border = BorderStroke(1.5.dp, HealthBorder)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Surface(
                            color = HealthSurfaceVariant,
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, HealthBorder),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = if (isEn)
                                    "Welcome! Before using the application, please carefully read and accept all 5 articles regarding Privacy Policy, Data Security, Legal Medical Disclaimer, and Notification Permissions."
                                else
                                    "Hoş geldiniz! Uygulamayı kullanmaya başlamadan önce lütfen aşağıdaki 5 maddelik Gizlilik İlkeleri, Veri Güvenliği, Yasal Tıbbi Sorumluluk Reddi ve Bildirim İzinleri koşullarını dikkatlice okuyup onaylayınız.",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextHealthPrimary,
                                lineHeight = 17.sp,
                                modifier = Modifier.padding(12.dp)
                            )
                        }

                        val articles = getPrivacyPolicyArticles(isEn)
                        articles.forEach { article ->
                            PrivacyPolicyArticleCard(
                                article = article,
                                isEn = isEn,
                                notificationsGranted = notificationsGranted,
                                onRequestNotificationPermission = {
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                        val hasPerm = ContextCompat.checkSelfPermission(
                                            context,
                                            Manifest.permission.POST_NOTIFICATIONS
                                        ) == PackageManager.PERMISSION_GRANTED
                                        if (!hasPerm) {
                                            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                        } else {
                                            NotificationHelper.openAppNotificationSettings(context)
                                        }
                                    } else {
                                        NotificationHelper.openAppNotificationSettings(context)
                                    }
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Mandatory Acceptance Checkbox Card
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .clickable {
                            isTermsChecked = !isTermsChecked
                            if (isTermsChecked) showUncheckedError = false
                        }
                        .testTag("privacy_consent_checkbox_row"),
                    shape = RoundedCornerShape(14.dp),
                    color = if (isTermsChecked) BioGreenContainer else if (showUncheckedError) AlertRubyContainer else HealthSurface,
                    border = BorderStroke(
                        1.5.dp,
                        when {
                            isTermsChecked -> BioHealingGreen
                            showUncheckedError -> AlertPharmacyRuby
                            else -> HealthBorder
                        }
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = isTermsChecked,
                            onCheckedChange = {
                                isTermsChecked = it
                                if (it) showUncheckedError = false
                            },
                            colors = CheckboxDefaults.colors(
                                checkedColor = BioHealingGreen,
                                uncheckedColor = if (showUncheckedError) AlertPharmacyRuby else HealthTealBright,
                                checkmarkColor = IconOnAccent
                            ),
                            modifier = Modifier.testTag("privacy_consent_checkbox")
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (isEn)
                                    "I have read, understood, and accept all Privacy & Data Security policies and confirm that this app is for personal tracking only and is NOT medical advice (Article 4)."
                                else
                                    "Tüm Gizlilik İlkelerini, Veri Güvenliği koşullarını ve 4. maddedeki bu bilgilerin tıbbi bir yönlendirme olmayıp yalnızca takip amaçlı kullanılabileceğine dair Yasal Tıbbi Beyanı okudum ve kabul ediyorum.",
                                fontSize = 11.5.sp,
                                lineHeight = 15.5.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = TextHealthPrimary
                            )
                            if (showUncheckedError && !isTermsChecked) {
                                Spacer(modifier = Modifier.height(3.dp))
                                Text(
                                    text = if (isEn)
                                        "⚠️ Please check the confirmation box above to continue."
                                    else
                                        "⚠️ Devam etmek için lütfen yukarıdaki onay kutucuğunu işaretleyin.",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Black,
                                    color = AlertPharmacyRuby
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Action Buttons: Decline vs Accept
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = { showDeclinedBlockingState = true },
                        modifier = Modifier
                            .weight(0.42f)
                            .height(50.dp)
                            .testTag("decline_privacy_btn"),
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.5.dp, AlertPharmacyRuby),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = AlertPharmacyRuby)
                    ) {
                        Text(
                            text = if (isEn) "Decline" else "Kabul Etmiyorum",
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = AlertPharmacyRuby,
                            maxLines = 1
                        )
                    }

                    Button(
                        onClick = {
                            if (!isTermsChecked) {
                                showUncheckedError = true
                                Toast.makeText(
                                    context,
                                    if (isEn)
                                        "Please check the consent box to accept the Privacy Policy and Legal Medical Disclaimer."
                                    else
                                        "Lütfen devam etmek için Gizlilik İlkeleri ve Yasal Tıbbi Beyan onay kutucuğunu işaretleyin.",
                                    Toast.LENGTH_SHORT
                                ).show()
                            } else {
                                completeAcceptance()
                            }
                        },
                        modifier = Modifier
                            .weight(0.58f)
                            .height(50.dp)
                            .testTag("accept_privacy_btn"),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isTermsChecked) BioHealingGreen else HealthTeal,
                            contentColor = IconOnAccent
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = IconOnAccent,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isEn) "Accept & Start" else "Kabul Et ve Başla",
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.Black,
                            color = IconOnAccent,
                            maxLines = 1
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PrivacyPolicyArticleCard(
    article: PrivacyArticleItem,
    isEn: Boolean,
    notificationsGranted: Boolean = true,
    onRequestNotificationPermission: (() -> Unit)? = null
) {
    val bgColor = when {
        article.isLegalMedicalDisclaimer -> WarningAmberContainer.copy(alpha = 0.65f)
        article.isNotificationArticle -> ChartSlotBg
        else -> ChartSlotBg
    }
    val borderColor = when {
        article.isLegalMedicalDisclaimer -> WarningAmberHealth
        article.isNotificationArticle -> HealthTealBright
        else -> HealthBorder.copy(alpha = 0.75f)
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("privacy_article_${article.number}"),
        shape = RoundedCornerShape(14.dp),
        color = bgColor,
        border = BorderStroke(if (article.isLegalMedicalDisclaimer || article.isNotificationArticle) 1.5.dp else 1.dp, borderColor)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (article.isLegalMedicalDisclaimer) {
                    Icon(
                        imageVector = Icons.Default.Gavel,
                        contentDescription = null,
                        tint = WarningAmberHealth,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                } else if (article.isNotificationArticle) {
                    Icon(
                        imageVector = Icons.Default.NotificationsActive,
                        contentDescription = null,
                        tint = HealthTealBright,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                }
                Text(
                    text = article.title,
                    fontSize = 12.5.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = if (article.isLegalMedicalDisclaimer) WarningAmberHealth else TextHealthPrimary
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = article.description,
                fontSize = 11.5.sp,
                color = if (article.isLegalMedicalDisclaimer) TextHealthPrimary else TextHealthSecondary,
                fontWeight = if (article.isLegalMedicalDisclaimer) FontWeight.SemiBold else FontWeight.Normal,
                lineHeight = 16.5.sp
            )

            if (article.isNotificationArticle && onRequestNotificationPermission != null) {
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (notificationsGranted) {
                            if (isEn) "✓ Phone Notification Permission: Active" else "✓ Telefon Bildirim İzni: Açık"
                        } else {
                            if (isEn) "⚠️ Phone Notification Permission: Required" else "⚠️ Telefon Bildirim İzni: Kapalı"
                        },
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (notificationsGranted) BioHealingGreen else AlertPharmacyRuby,
                        modifier = Modifier.weight(1f)
                    )
                    if (!notificationsGranted) {
                        Button(
                            onClick = onRequestNotificationPermission,
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = HealthTeal,
                                contentColor = IconOnAccent
                            ),
                            modifier = Modifier.height(34.dp)
                        ) {
                            Text(
                                text = if (isEn) "Grant Permission" else "Bildirim İzni Ver",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = IconOnAccent
                            )
                        }
                    }
                }
            }
        }
    }
}
