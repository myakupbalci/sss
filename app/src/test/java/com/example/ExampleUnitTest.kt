package com.example

import com.example.data.model.DoseType
import com.example.data.model.InjectionSite
import com.example.data.model.InsulinStock
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {
  @Test
  fun addition_isCorrect() {
    assertEquals(4, 2 + 2)
  }

  @Test
  fun verifyInjectionSiteRotationLogic() {
    // When last site was ABDOMEN_LEFT, recommended should rotate to ABDOMEN_RIGHT
    val nextFromLeftAbdomen = InjectionSite.getRecommendedRotation(InjectionSite.ABDOMEN_LEFT)
    assertEquals(InjectionSite.ABDOMEN_RIGHT, nextFromLeftAbdomen)

    // When last site was THIGH_LEFT, recommended should rotate to THIGH_RIGHT
    val nextFromThigh = InjectionSite.getRecommendedRotation(InjectionSite.THIGH_LEFT)
    assertEquals(InjectionSite.THIGH_RIGHT, nextFromThigh)

    // Verify advice text
    val advice = InjectionSite.getRotationAdviceText(InjectionSite.ABDOMEN_LEFT, nextFromLeftAbdomen)
    assertTrue(advice.contains("Sol Karın"))
    assertTrue(advice.contains("Sağ Karın"))
  }

  @Test
  fun verifyInsulinStockCalculationsAndLowStockAlert() {
    // Low stock scenario
    val lowStock = InsulinStock(
      id = 1,
      type = DoseType.BASAL,
      brandName = "Lantus SoloStar",
      unitsPerPen = 300,
      totalUnitsRemaining = 80.0,
      lowStockThresholdUnits = 90.0
    )
    assertTrue(lowStock.isLowStock)
    assertEquals(0, lowStock.fullPensCount)

    // Normal stock scenario
    val normalStock = InsulinStock(
      id = 2,
      type = DoseType.BOLUS,
      brandName = "NovoRapid FlexPen",
      unitsPerPen = 300,
      totalUnitsRemaining = 450.0,
      lowStockThresholdUnits = 100.0
    )
    assertFalse(normalStock.isLowStock)
    assertEquals(1, normalStock.fullPensCount)
    assertEquals(1.5, normalStock.pensRemaining, 0.01)
  }

  @Test
  fun verifyDoseTypeMetadata() {
    assertEquals("Bazal (Uzun Etkili)", DoseType.BASAL.title)
    assertEquals("Bolus (Hızlı Etkili)", DoseType.BOLUS.title)
  }
}
