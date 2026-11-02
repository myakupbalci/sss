package com.example.data.model

enum class BodyAspect {
    ANTERIOR, // Ön Görünüm
    POSTERIOR // Arka Görünüm
}

enum class InjectionSite(
    val id: String,
    val displayName: String,
    val anatomicalGroup: String,
    val aspect: BodyAspect,
    val recommendedDoseType: DoseType?,
    val absorptionRate: String,
    val rotationTips: String
) {
    ABDOMEN_LEFT(
        id = "abdomen_left",
        displayName = "Sol Karın",
        anatomicalGroup = "Karın",
        aspect = BodyAspect.ANTERIOR,
        recommendedDoseType = DoseType.BOLUS,
        absorptionRate = "Hızlı Emilim (Öğün dozu için ideal)",
        rotationTips = "Göbek deliğinden en az 2-3 parmak uzakta olmalıdır."
    ),
    ABDOMEN_RIGHT(
        id = "abdomen_right",
        displayName = "Sağ Karın",
        anatomicalGroup = "Karın",
        aspect = BodyAspect.ANTERIOR,
        recommendedDoseType = DoseType.BOLUS,
        absorptionRate = "Hızlı Emilim (Öğün dozu için ideal)",
        rotationTips = "Göbek deliğinden en az 2-3 parmak uzakta olmalıdır."
    ),
    ARM_LEFT(
        id = "arm_left",
        displayName = "Sol Dış Kol",
        anatomicalGroup = "Kol",
        aspect = BodyAspect.ANTERIOR,
        recommendedDoseType = DoseType.BOLUS,
        absorptionRate = "Orta Hızlı Emilim",
        rotationTips = "Üst kolun dış arka yağlı dokusu tercih edilmelidir."
    ),
    ARM_RIGHT(
        id = "arm_right",
        displayName = "Sağ Dış Kol",
        anatomicalGroup = "Kol",
        aspect = BodyAspect.ANTERIOR,
        recommendedDoseType = DoseType.BOLUS,
        absorptionRate = "Orta Hızlı Emilim",
        rotationTips = "Üst kolun dış arka yağlı dokusu tercih edilmelidir."
    ),
    THIGH_LEFT(
        id = "thigh_left",
        displayName = "Sol Baldır Ön Yüzü",
        anatomicalGroup = "Baldır Ön Yüzü",
        aspect = BodyAspect.ANTERIOR,
        recommendedDoseType = DoseType.BASAL,
        absorptionRate = "Yavaş ve Dengeli (Bazal için ideal)",
        rotationTips = "Baldırın ön-dış üst bölgesine uygulanmalıdır."
    ),
    THIGH_RIGHT(
        id = "thigh_right",
        displayName = "Sağ Baldır Ön Yüzü",
        anatomicalGroup = "Baldır Ön Yüzü",
        aspect = BodyAspect.ANTERIOR,
        recommendedDoseType = DoseType.BASAL,
        absorptionRate = "Yavaş ve Dengeli (Bazal için ideal)",
        rotationTips = "Baldırın ön-dış üst bölgesine uygulanmalıdır."
    ),
    GLUTEAL_LEFT(
        id = "gluteal_left",
        displayName = "Sol Kalça",
        anatomicalGroup = "Kalça",
        aspect = BodyAspect.POSTERIOR,
        recommendedDoseType = DoseType.BASAL,
        absorptionRate = "En Yavaş Emilim (Gece Bazalı için ideal)",
        rotationTips = "Kalçanın üst dış çeyreğine yapılmalıdır."
    ),
    GLUTEAL_RIGHT(
        id = "gluteal_right",
        displayName = "Sağ Kalça",
        anatomicalGroup = "Kalça",
        aspect = BodyAspect.POSTERIOR,
        recommendedDoseType = DoseType.BASAL,
        absorptionRate = "En Yavaş Emilim (Gece Bazalı için ideal)",
        rotationTips = "Kalçanın üst dış çeyreğine yapılmalıdır."
    );

    companion object {
        fun getRecommendedRotation(lastSite: InjectionSite?, doseType: DoseType? = null): InjectionSite {
            if (lastSite == null) {
                return if (doseType == DoseType.BASAL) THIGH_RIGHT else ABDOMEN_RIGHT
            }

            return when (lastSite) {
                ABDOMEN_LEFT -> ABDOMEN_RIGHT
                ABDOMEN_RIGHT -> if (doseType == DoseType.BASAL) THIGH_LEFT else ABDOMEN_LEFT
                ARM_LEFT -> ARM_RIGHT
                ARM_RIGHT -> ARM_LEFT
                THIGH_LEFT -> THIGH_RIGHT
                THIGH_RIGHT -> if (doseType == DoseType.BOLUS) ABDOMEN_LEFT else THIGH_LEFT
                GLUTEAL_LEFT -> GLUTEAL_RIGHT
                GLUTEAL_RIGHT -> GLUTEAL_LEFT
            }
        }

        fun getRotationAdviceText(lastSite: InjectionSite?, nextSite: InjectionSite): String {
            return if (lastSite != null) {
                "En son ${lastSite.displayName}'a vurdun, şimdi ${nextSite.displayName} bölgesini tercih et."
            } else {
                "Rotasyon için ilk olarak ${nextSite.displayName} bölgesi önerilir."
            }
        }
    }
}
