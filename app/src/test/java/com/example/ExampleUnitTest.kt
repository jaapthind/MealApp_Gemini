package com.example

import org.junit.Assert.*
import org.junit.Test

class ExampleUnitTest {
  @Test
  fun addition_isCorrect() {
    assertEquals(4, 2 + 2)
  }

  @Test
  fun testFormatFloat() {
    assertEquals("150", formatFloat(150.0f))
    assertEquals("1.5", formatFloat(1.5f))
    assertEquals("2.4", formatFloat(2.4f))
    assertEquals("0", formatFloat(0f))
  }

  @Test
  fun testGetAugmentedNutrients() {
    // Soy Milk test ID "37"
    val rawNutrients = mapOf("Calories" to 80f, "Dietary Fiber" to 1.5f, "Protein" to 7f)
    val augmented = getAugmentedNutrients("37", rawNutrients)
    
    assertEquals(80f, augmented["Calories"])
    assertEquals(1.5f, augmented["Dietary Fiber (g)"])
    assertEquals(7f, augmented["Protein (g)"])
    // Soy Milk thiamine, riboflavin, folate B12, D check
    assertEquals(0.15f, augmented["B1 - Thiamine (mg)"])
    assertEquals(0.2f, augmented["B2 - Riboflavin"])
    assertEquals(1.5f, augmented["B12 - Cobalamin (mcg)"])
    assertEquals(2.5f, augmented["D (mcg)"])
  }
}
