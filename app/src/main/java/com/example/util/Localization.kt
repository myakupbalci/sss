package com.example.util

import com.example.data.model.InjectionSite

enum class AppLanguage(val code: String, val title: String, val flag: String) {
    TR("tr", "Türkçe", "🇹🇷"),
    EN("en", "English", "🇬🇧")
}

object Strings {
    // Navigation
    fun navHome(lang: AppLanguage) = if (lang == AppLanguage.EN) "Home" else "Ana Sayfa"
    fun navStatistics(lang: AppLanguage) = if (lang == AppLanguage.EN) "Statistics" else "İstatistik"
    fun navRotation(lang: AppLanguage) = if (lang == AppLanguage.EN) "Rotation" else "Rotasyon"
    fun navReminders(lang: AppLanguage) = if (lang == AppLanguage.EN) "Reminders" else "Hatırlatıcı"
    fun navStock(lang: AppLanguage) = if (lang == AppLanguage.EN) "Stock" else "Stok"
    fun navHistory(lang: AppLanguage) = if (lang == AppLanguage.EN) "History" else "Geçmiş"

    // Greetings
    fun greeting(lang: AppLanguage, hour: Int) = if (lang == AppLanguage.EN) {
        when (hour) {
            in 5..11 -> "Good Morning ☀️"
            in 12..17 -> "Good Afternoon 🌤️"
            in 18..22 -> "Good Evening 🌙"
            else -> "Good Night ✨"
        }
    } else {
        when (hour) {
            in 5..11 -> "Günaydın ☀️"
            in 12..17 -> "İyi Günler 🌤️"
            in 18..22 -> "İyi Akşamlar 🌙"
            else -> "İyi Geceler ✨"
        }
    }

    // Language Toggle
    fun languageSelectTitle(lang: AppLanguage) = if (lang == AppLanguage.EN) "Language" else "Dil"

    // Quick Dose Action
    fun quickDoseHeroTag(lang: AppLanguage) = if (lang == AppLanguage.EN) "QUICK INSULIN LOG" else "HIZLI İNSÜLİN KAYDI"
    fun quickDoseHeroTitle(lang: AppLanguage) = if (lang == AppLanguage.EN) "Log & Inject Dose" else "Doz Vur & Kaydet"
    fun quickDoseButton(lang: AppLanguage) = if (lang == AppLanguage.EN) "Log Dose" else "Giriş Yap"

    // 2-Hour Postprandial Glucose Banner
    fun postDoseGlucoseBannerTitle(lang: AppLanguage) = if (lang == AppLanguage.EN) "2-Hour Postprandial Blood Glucose" else "2 Saat Sonraki Tokluk Şekeri Değeri"
    fun postDoseGlucoseBannerSubtitle(lang: AppLanguage, units: Double, type: String, timeAgo: String) =
        if (lang == AppLanguage.EN) "%.1f IU %s dose ($timeAgo). Please check your postprandial blood glucose.".format(units, type)
        else "%.1f IU %s dozu ($timeAgo). Lütfen 2. saat tokluk şekerinizi ölçün.".format(units, type)
    fun pendingBadge(lang: AppLanguage) = if (lang == AppLanguage.EN) "PENDING" else "BEKLENİYOR"
    fun enterPostDoseGlucoseBtn(lang: AppLanguage) = if (lang == AppLanguage.EN) "Enter 2-Hr Postprandial Glucose" else "2 Saat Sonrası Tokluk Şekerini Gir"

    // Home Chart: Glucose & Insulin Dynamics
    fun chartTitle(lang: AppLanguage) = if (lang == AppLanguage.EN) "Average Glucose Profile" else "Ortalama Şeker Profili"
    fun chartSubtitle(lang: AppLanguage) = if (lang == AppLanguage.EN) "Last 7 Days Fasting & Postprandial Averages" else "Son 7 Gün Ortalaması (Açlık ve Tokluk)"
    fun chartSlotMorning(lang: AppLanguage) = if (lang == AppLanguage.EN) "Morning" else "Sabah"
    fun chartSlotLunch(lang: AppLanguage) = if (lang == AppLanguage.EN) "Lunch" else "Öğle"
    fun chartSlotDinner(lang: AppLanguage) = if (lang == AppLanguage.EN) "Dinner" else "Akşam"
    fun chartSlotBedtime(lang: AppLanguage) = if (lang == AppLanguage.EN) "Bedtime" else "Yatarken"
    fun chartLegendFasting(lang: AppLanguage) = if (lang == AppLanguage.EN) "Fasting" else "Açlık"
    fun chartLegendPost(lang: AppLanguage) = if (lang == AppLanguage.EN) "2-Hr Post" else "Tokluk"
    fun chartTargetFasting(lang: AppLanguage) = if (lang == AppLanguage.EN) "Target: 70–130 mg/dL" else "Hedef: 70–130 mg/dL"
    fun chartTargetPost(lang: AppLanguage) = if (lang == AppLanguage.EN) "Target: < 180 mg/dL" else "Hedef: < 180 mg/dL"
    fun chartAvgFasting(lang: AppLanguage) = if (lang == AppLanguage.EN) "Avg Fasting" else "Ortalama Açlık"
    fun chartAvgPost(lang: AppLanguage) = if (lang == AppLanguage.EN) "Avg 2-Hr Post" else "Ortalama Tokluk"
    fun chartViewToday(lang: AppLanguage) = if (lang == AppLanguage.EN) "Today" else "Bugün"
    fun chartView7Days(lang: AppLanguage) = if (lang == AppLanguage.EN) "Last 7 Days Avg" else "Son 7 Gün Ortalaması"
    fun chartTapHint(lang: AppLanguage) = if (lang == AppLanguage.EN) "Tap any time slot to see details" else "Detay için bir zaman dilimine dokunun"
    fun chartEmpty(lang: AppLanguage) = if (lang == AppLanguage.EN)
        "No readings recorded for this filter. Log fasting and postprandial glucose with your doses to see the dynamic bars."
    else
        "Bu filtre için henüz şeker ölçümü kaydedilmedi. Doz girişlerinde açlık ve tokluk değerlerinizi kaydederek grafiği burada görebilirsiniz."

    // Today's Dose List Header & Empty States
    fun todayDosesTitle(lang: AppLanguage, count: Int) = if (lang == AppLanguage.EN) "TODAY'S LOGGED DOSES ($count)" else "BUGÜNÜN DOZ LİSTESİ ($count)"
    fun noDosesTodayTitle(lang: AppLanguage) = if (lang == AppLanguage.EN) "No doses logged today yet" else "Bugün henüz doz kaydı girilmedi"
    fun noDosesTodaySubtitle(lang: AppLanguage) = if (lang == AppLanguage.EN)
        "You can easily log your insulin doses using the 'Log Dose' button above."
    else
        "Yukarıdaki 'Doz Vur & Kaydet' butonu veya hızlı kısayollar ile kolayca kaydedebilirsiniz."

    // Quick Dose Dialog
    fun dialogTitle(lang: AppLanguage) = if (lang == AppLanguage.EN) "💉 Log Insulin Dose" else "💉 İnsülin Dozu Kaydet"
    fun basalTab(lang: AppLanguage) = if (lang == AppLanguage.EN) "BASAL (Long-Acting)" else "BAZAL (Uzun Etkili)"
    fun bolusTab(lang: AppLanguage) = if (lang == AppLanguage.EN) "BOLUS (Rapid-Acting)" else "BOLUS (Hızlı Etkili)"
    fun doseAmountLabel(lang: AppLanguage) = if (lang == AppLanguage.EN) "Dose Amount (IU):" else "Doz Miktarı (IU):"
    fun enterManually(lang: AppLanguage) = if (lang == AppLanguage.EN) "Enter Manually" else "Elle Değer Gir"
    fun doneManual(lang: AppLanguage) = if (lang == AppLanguage.EN) "Done" else "Tamam"
    fun clickToEdit(lang: AppLanguage) = if (lang == AppLanguage.EN) "(Tap to edit)" else "(Değiştirmek için tıkla)"
    fun exactDosePrompt(lang: AppLanguage) = if (lang == AppLanguage.EN) "Enter Exact Dose (IU)" else "Tam Doz Miktarını Yazın (IU)"
    fun injectionSiteSectionTitle(lang: AppLanguage) = if (lang == AppLanguage.EN) "Injection Site & Rotation:" else "Enjeksiyon Bölgesi ve Rotasyon:"
    fun lastInjectedSite(lang: AppLanguage, siteName: String) = if (lang == AppLanguage.EN) "Last Injected: $siteName" else "Son Vurulan: $siteName"
    fun restSiteWarning(lang: AppLanguage) = if (lang == AppLanguage.EN) "(Rest this site to prevent tissue hardening)" else "(Bu bölgeyi dinlendirin - doku sertleşmesini önleyin)"
    fun noPriorSite(lang: AppLanguage) = if (lang == AppLanguage.EN) "No prior injection recorded yet" else "Henüz önceki enjeksiyon kaydı yok"
    fun anteriorTab(lang: AppLanguage) = if (lang == AppLanguage.EN) "Anterior (Abdomen, Thigh, Arm)" else "Ön Beden (Karın, Baldır, Kol)"
    fun posteriorTab(lang: AppLanguage) = if (lang == AppLanguage.EN) "Posterior (Gluteal)" else "Arka Beden (Kalça)"
    fun mealTagLabel(lang: AppLanguage) = if (lang == AppLanguage.EN) "Meal / Tag:" else "Öğün / Etiket:"
    fun fastingGlucoseInputLabel(lang: AppLanguage) = if (lang == AppLanguage.EN) "Pre-Dose Fasting Blood Glucose (mg/dL) *" else "Doz Öncesi Açlık Kan Şekeri (mg/dL) *"
    fun fastingGlucoseInputPlaceholder(lang: AppLanguage) = if (lang == AppLanguage.EN) "e.g. 115" else "Örn: 115"
    fun targetFastingHint(lang: AppLanguage) = if (lang == AppLanguage.EN) "Clinical target: 70–130 mg/dL" else "Klinik hedef aralığı: 70–130 mg/dL"
    fun notesLabel(lang: AppLanguage) = if (lang == AppLanguage.EN) "Notes (Optional):" else "Notlar (İsteğe bağlı):"
    fun notesPlaceholder(lang: AppLanguage) = if (lang == AppLanguage.EN) "e.g., Heavy carbohydrate dinner" else "Örn: Ağır karbonhidratlı akşam yemeği"
    fun saveDoseButton(lang: AppLanguage, units: Double) = if (lang == AppLanguage.EN) "Save & Log Dose (%.1f IU)".format(units) else "Dozu Vuruldum & Kaydet (%.1f IU)".format(units)

    // Body Rotation Assistant Component
    fun rotationAssistantTitle(lang: AppLanguage) = if (lang == AppLanguage.EN) "Injection Rotation Assistant" else "Enjeksiyon Rotasyon Asistanı"
    fun rotationAssistantSubtitle(lang: AppLanguage) = if (lang == AppLanguage.EN) "Lipohypertrophy (Tissue Hardening) Protection" else "Lipohipertrofi (Doku Sertleşmesi) Koruması"
    fun rotationAdviceTitle(lastSite: InjectionSite?, recommendedSite: InjectionSite, lang: AppLanguage): String {
        return if (lang == AppLanguage.EN) {
            if (lastSite != null) "Last injected at ${siteDisplayName(lastSite, lang)}. Recommended: ${siteDisplayName(recommendedSite, lang)}."
            else "Recommended site: ${siteDisplayName(recommendedSite, lang)}."
        } else {
            InjectionSite.getRotationAdviceText(lastSite, recommendedSite)
        }
    }
    fun rotationAdviceSubtitle(lang: AppLanguage) = if (lang == AppLanguage.EN)
        "Rotating sites prevents fat tissue buildup (lipohypertrophy) and ensures steady insulin absorption."
    else
        "Aynı noktaya üst üste insülin vurmak emilimi bozar ve doku sertleşmesi (lipohipertrofi) yapar."
    fun quickZoneSelection(lang: AppLanguage) = if (lang == AppLanguage.EN) "Quick Site Selection:" else "Hızlı Bölge Seçimi:"
    fun selectedSiteLabel(lang: AppLanguage, siteName: String) = if (lang == AppLanguage.EN) "Selected: $siteName" else "Seçili: $siteName"
    fun rotationBadge(lang: AppLanguage) = if (lang == AppLanguage.EN) "★ ROTATION RECOMMENDATION" else "★ ROTASYON TAVSİYESİ"
    fun absorptionSpeed(lang: AppLanguage, rate: String) = if (lang == AppLanguage.EN) "⚡ Absorption: $rate" else "⚡ Emilim: $rate"
    fun clinicalRule(lang: AppLanguage, tips: String) = if (lang == AppLanguage.EN) "📌 Clinical Rule: $tips" else "📌 Klinik Kural: $tips"

    // Callout labels
    fun calloutRightArm(lang: AppLanguage) = if (lang == AppLanguage.EN) "Right Arm" else "Sağ Kol"
    fun calloutLeftArm(lang: AppLanguage) = if (lang == AppLanguage.EN) "Left Arm" else "Sol Kol"
    fun calloutRightAbdomen(lang: AppLanguage) = if (lang == AppLanguage.EN) "Right Abdomen" else "Sağ Karın"
    fun calloutLeftAbdomen(lang: AppLanguage) = if (lang == AppLanguage.EN) "Left Abdomen" else "Sol Karın"
    fun calloutRightThigh(lang: AppLanguage) = if (lang == AppLanguage.EN) "Right Anterior Thigh" else "Sağ Baldır Ön Yüz"
    fun calloutLeftThigh(lang: AppLanguage) = if (lang == AppLanguage.EN) "Left Anterior Thigh" else "Sol Baldır Ön Yüz"
    fun calloutRightGluteal(lang: AppLanguage) = if (lang == AppLanguage.EN) "Right Gluteal" else "Sağ Kalça"
    fun calloutLeftGluteal(lang: AppLanguage) = if (lang == AppLanguage.EN) "Left Gluteal" else "Sol Kalça"

    // Chips tags
    fun tagRecommended(lang: AppLanguage) = if (lang == AppLanguage.EN) "★ REC" else "★ TAVSİYE"
    fun tagLast(lang: AppLanguage) = if (lang == AppLanguage.EN) "● LAST" else "● SON"

    // Common Meal Tags
    fun mealTagBreakfast(lang: AppLanguage) = if (lang == AppLanguage.EN) "Pre-Breakfast" else "Kahvaltı Öncesi"
    fun mealTagLunch(lang: AppLanguage) = if (lang == AppLanguage.EN) "Lunch" else "Öğle Yemeği"
    fun mealTagDinner(lang: AppLanguage) = if (lang == AppLanguage.EN) "Dinner" else "Akşam Yemeği"
    fun mealTagSnack(lang: AppLanguage) = if (lang == AppLanguage.EN) "Snack" else "Ara Öğün"
    fun mealTagCorrection(lang: AppLanguage) = if (lang == AppLanguage.EN) "Correction" else "Düzeltme Dozu"
    fun mealTagEveningBasal(lang: AppLanguage) = if (lang == AppLanguage.EN) "Evening Basal" else "Akşam Bazalı"
    fun mealTagMorningBasal(lang: AppLanguage) = if (lang == AppLanguage.EN) "Morning Basal" else "Sabah Bazalı"
    fun mealTagNightDose(lang: AppLanguage) = if (lang == AppLanguage.EN) "Night Dose" else "Gece Dozu"

    // Site Display Names
    fun siteDisplayName(site: InjectionSite, lang: AppLanguage): String {
        return if (lang == AppLanguage.EN) {
            when (site) {
                InjectionSite.ABDOMEN_LEFT -> "Left Abdomen"
                InjectionSite.ABDOMEN_RIGHT -> "Right Abdomen"
                InjectionSite.ARM_LEFT -> "Left Arm"
                InjectionSite.ARM_RIGHT -> "Right Arm"
                InjectionSite.THIGH_LEFT -> "Left Anterior Thigh"
                InjectionSite.THIGH_RIGHT -> "Right Anterior Thigh"
                InjectionSite.GLUTEAL_LEFT -> "Left Gluteal"
                InjectionSite.GLUTEAL_RIGHT -> "Right Gluteal"
            }
        } else {
            when (site) {
                InjectionSite.ABDOMEN_LEFT -> "Sol Karın"
                InjectionSite.ABDOMEN_RIGHT -> "Sağ Karın"
                InjectionSite.ARM_LEFT -> "Sol Dış Kol"
                InjectionSite.ARM_RIGHT -> "Sağ Dış Kol"
                InjectionSite.THIGH_LEFT -> "Sol Baldır Ön Yüzü"
                InjectionSite.THIGH_RIGHT -> "Sağ Baldır Ön Yüzü"
                InjectionSite.GLUTEAL_LEFT -> "Sol Kalça"
                InjectionSite.GLUTEAL_RIGHT -> "Sağ Kalça"
            }
        }
    }

    fun siteAbsorptionRate(site: InjectionSite, lang: AppLanguage): String {
        return if (lang == AppLanguage.EN) {
            when (site) {
                InjectionSite.ABDOMEN_LEFT, InjectionSite.ABDOMEN_RIGHT -> "Rapid Absorption (Ideal for mealtime bolus)"
                InjectionSite.ARM_LEFT, InjectionSite.ARM_RIGHT -> "Moderate Absorption Rate"
                InjectionSite.THIGH_LEFT, InjectionSite.THIGH_RIGHT -> "Slow & Steady Absorption (Ideal for basal)"
                InjectionSite.GLUTEAL_LEFT, InjectionSite.GLUTEAL_RIGHT -> "Slowest Absorption (Ideal for night basal)"
            }
        } else {
            site.absorptionRate
        }
    }

    fun siteRotationTips(site: InjectionSite, lang: AppLanguage): String {
        return if (lang == AppLanguage.EN) {
            when (site) {
                InjectionSite.ABDOMEN_LEFT, InjectionSite.ABDOMEN_RIGHT -> "Stay at least 2–3 fingerbreadths away from navel."
                InjectionSite.ARM_LEFT, InjectionSite.ARM_RIGHT -> "Use outer fatty tissue of upper arm."
                InjectionSite.THIGH_LEFT, InjectionSite.THIGH_RIGHT -> "Inject into upper-outer front aspect of thigh."
                InjectionSite.GLUTEAL_LEFT, InjectionSite.GLUTEAL_RIGHT -> "Inject into upper-outer quadrant of buttock."
            }
        } else {
            site.rotationTips
        }
    }

    // Legend helpers
    fun legendRecommended(lang: AppLanguage) = if (lang == AppLanguage.EN) "Recommended Site" else "Önerilen Rotasyon"
    fun legendLast(lang: AppLanguage) = if (lang == AppLanguage.EN) "Last Injected" else "Son Vurulan"
    fun legendSelected(lang: AppLanguage) = if (lang == AppLanguage.EN) "Selected" else "Seçili"
    fun delete(lang: AppLanguage) = if (lang == AppLanguage.EN) "Delete" else "Sil"
    fun now(lang: AppLanguage) = if (lang == AppLanguage.EN) "NOW" else "ŞİMDİ"
    fun dosesCount(lang: AppLanguage, count: Int) = if (lang == AppLanguage.EN) "$count Doses" else "$count Doz"
}

