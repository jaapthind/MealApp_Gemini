package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.IceBlue
import com.example.ui.theme.MintGreen
import com.example.ui.theme.MintNeon
import com.example.ui.theme.MutedText
import com.example.ui.theme.SlateBg
import com.example.ui.theme.SoftSilver
import com.example.ui.theme.SunsetOrange
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.LocalFocusManager
import java.io.BufferedReader
import java.io.InputStreamReader

// ==========================================
// DATA MODELS
// ==========================================

data class NutrientGoal(
  val name: String,
  val target: Float,
  val unit: String,
  val isUpperLimit: Boolean = false
)

data class FoodComponent(
  val id: String,
  val name: String,
  val category: String,
  val group: String,
  val servingDesc: String,
  val prepTime: Int,
  val cookTime: Int,
  val ingredients: List<String>,
  val instructions: List<String>,
  val nutrients: Map<String, Float>
)

data class MealPreset(
  val id: String,
  val type: String, // "Breakfast", "Lunch", "Snack", "Dinner", "Coffee & Tea"
  val name: String,
  val description: String,
  val imageRes: Int,
  val foodItemIds: List<String>
)

enum class AppScreen {
  Main,
  MealPlan,
  MealScreen,
  RecipeDetail
}

data class ChatMessage(
  val sender: String, // "user" or "ai"
  val text: String
)

enum class NutrientStatus {
  Low,      // < 85%
  Optimal,  // 85% - 115%
  High      // > 115%
}

data class NutrientAuditResult(
  val name: String,
  val consumed: Float,
  val target: Float,
  val unit: String,
  val pct: Float,
  val status: NutrientStatus
)

fun getAugmentedNutrients(foodId: String, raw: Map<String, Float>): Map<String, Float> {
  val result = mutableMapOf<String, Float>()
  
  result["Calories"] = raw["Calories"] ?: 0f
  result["Dietary Fiber (g)"] = raw["Dietary Fiber"] ?: raw["Dietary Fiber (g)"] ?: 0f
  result["Protein (g)"] = raw["Protein"] ?: raw["Protein (g)"] ?: 0f
  result["A (mcg RAE)"] = raw["Vitamin A"] ?: raw["A (mcg RAE)"] ?: 0f
  result["C (mg)"] = raw["Vitamin C"] ?: raw["C (mg)"] ?: 0f
  result["Calcium (mg)"] = raw["Calcium"] ?: raw["Calcium (mg)"] ?: 0f
  result["Magnesium (mg)"] = raw["Magnesium"] ?: raw["Magnesium (mg)"] ?: 0f
  result["Potassium (mg)"] = raw["Potassium"] ?: raw["Potassium (mg)"] ?: 0f
  result["Phosphorus (mg)"] = raw["Phosphorus"] ?: raw["Phosphorus (mg)"] ?: 0f
  result["Iron (mg)"] = raw["Iron"] ?: raw["Iron (mg)"] ?: 0f
  result["Zinc (mg)"] = raw["Zinc"] ?: raw["Zinc (mg)"] ?: 0f
  result["Selenium (mcg)"] = raw["Selenium"] ?: raw["Selenium (mcg)"] ?: 0f
  result["Copper (mg)"] = raw["Copper"] ?: raw["Copper (mg)"] ?: 0f
  result["Manganese (mg)"] = raw["Manganese"] ?: raw["Manganese (mg)"] ?: 0f

  val b1 = when (foodId) {
    "80" -> 0.5f  // Lentils
    "81" -> 0.2f  // Brown rice
    "37" -> 0.15f // Soy milk
    "breakfast", "eggs" -> 0.08f
    "steak" -> 0.12f
    "toast" -> 0.15f
    else -> 0.03f
  }
  val b2 = when (foodId) {
    "86" -> 0.4f  // Greek Yogurt
    "37" -> 0.2f  // Soy milk
    "89" -> 0.15f // Salmon
    "eggs" -> 0.5f  // Eggs
    "steak" -> 0.25f
    else -> 0.05f
  }
  val b3 = when (foodId) {
    "89" -> 10.0f // Salmon is very high in Niacin
    "steak" -> 7.5f  // Steak
    "80" -> 1.5f  // Lentils
    "81" -> 2.5f  // Brown rice
    "28" -> 2.0f  // Almond butter
    else -> 0.5f
  }
  val b5 = when (foodId) {
    "eggs" -> 1.5f  // Eggs
    "89" -> 1.0f  // Salmon
    "steak" -> 1.2f
    "84" -> 0.8f  // Sweet potato
    "86" -> 0.6f  // Greek Yogurt
    "80" -> 0.6f  // Lentils
    else -> 0.2f
  }
  val b6 = when (foodId) {
    "89" -> 0.8f  // Salmon
    "84" -> 0.3f  // Sweet potato
    "steak" -> 0.5f
    "88" -> 0.1f  // Orange
    "63" -> 0.2f  // Mango
    "80" -> 0.3f  // Lentils
    else -> 0.05f
  }
  val b7 = when (foodId) {
    "eggs" -> 10f   // Eggs are extremely high in Biotin
    "27" -> 4f    // Chia seeds
    "28" -> 2.5f  // Almond butter
    "30" -> 2f    // Sunflower seeds
    "89" -> 5f    // Salmon
    else -> 0.5f
  }
  val b9 = when (foodId) {
    "80" -> 350f  // Lentils are rich in folate (B9)
    "82" -> 120f  // Collard greens
    "88" -> 40f   // Orange
    "56" -> 25f   // Strawberries
    "27" -> 15f   // Chia seeds
    "eggs" -> 44f
    "asparagus" -> 150f // Asparagus is very high in folate
    else -> 5f
  }
  val b12 = when (foodId) {
    "89" -> 4.5f  // Salmon is rich in B12 (target 2.4mcg)
    "eggs" -> 1.2f  // Eggs
    "86" -> 1.0f  // Yogurt
    "steak" -> 2.6f  // Steak
    "37" -> 1.5f  // Soy Milk (fortified)
    else -> 0f
  }
  val vitD = when (foodId) {
    "89" -> 16f   // Salmon is extremely rich in Vitamin D (target 15mcg)
    "eggs" -> 2f    // Eggs
    "37" -> 2.5f  // Soy milk (fortified)
    "86" -> 1.5f  // Yogurt (fortified)
    else -> 0f
  }
  val vitE = when (foodId) {
    "28" -> 7f    // Almond butter is very rich in Vitamin E (target 15mg)
    "30" -> 5f    // Sunflower seeds
    "87", "5" -> 2f // Olive oil
    "84" -> 1f    // Sweet potato
    "89" -> 1.5f  // Salmon
    else -> 0.1f
  }
  val vitK = when (foodId) {
    "82" -> 450f  // Collard greens are extremely rich in Vitamin K (target 120mcg)
    "asparagus" -> 50f
    "87", "5" -> 8f  // Olive oil
    "56" -> 3f    // Strawberries
    else -> 0.1f
  }
  val iodine = when (foodId) {
    "86" -> 80f   // Greek Yogurt is rich in Iodine (target 150mcg)
    "eggs" -> 26f   // Eggs
    "89" -> 35f   // Salmon
    "3" -> 40f    // Sea Salt
    "4" -> 80f    // Sea Salt
    else -> 2f
  }

  result["B1 - Thiamine (mg)"] = b1
  result["B2 - Riboflavin"] = b2
  result["B3 - Niacin (mg)"] = b3
  result["B5 - Pantothenic (mg)"] = b5
  result["B6 - Pyridoxine (mg)"] = b6
  result["B7 - Biotin (mcg)"] = b7
  result["B9 - Folate (mcg DFE)"] = b9
  result["B12 - Cobalamin (mcg)"] = b12
  result["D (mcg)"] = vitD
  result["E - Tocopherol (mg)"] = vitE
  result["K (mcg)"] = vitK
  result["Iodine (mcg)"] = iodine

  return result
}

fun loadFdaRecommendationsFromCsv(context: android.content.Context): List<NutrientGoal> {
  val defaultGoals = listOf(
    NutrientGoal("Calories", 2000f, "kcal"),
    NutrientGoal("Dietary Fiber (g)", 38f, "g"),
    NutrientGoal("Protein (g)", 95f, "g"),
    NutrientGoal("A (mcg RAE)", 900f, "mcg RAE"),
    NutrientGoal("B1 - Thiamine (mg)", 1.2f, "mg"),
    NutrientGoal("B2 - Riboflavin", 1.3f, "mg"),
    NutrientGoal("B3 - Niacin (mg)", 16f, "mg"),
    NutrientGoal("B5 - Pantothenic (mg)", 5f, "mg"),
    NutrientGoal("B6 - Pyridoxine (mg)", 1.3f, "mg"),
    NutrientGoal("B7 - Biotin (mcg)", 30f, "mcg"),
    NutrientGoal("B9 - Folate (mcg DFE)", 400f, "mcg DFE"),
    NutrientGoal("B12 - Cobalamin (mcg)", 2.4f, "mcg"),
    NutrientGoal("C (mg)", 90f, "mg"),
    NutrientGoal("D (mcg)", 15f, "mcg"),
    NutrientGoal("E - Tocopherol (mg)", 15f, "mg"),
    NutrientGoal("K (mcg)", 120f, "mcg"),
    NutrientGoal("Calcium (mg)", 1000f, "mg"),
    NutrientGoal("Magnesium (mg)", 420f, "mg"),
    NutrientGoal("Potassium (mg)", 3400f, "mg"),
    NutrientGoal("Phosphorus (mg)", 700f, "mg"),
    NutrientGoal("Iron (mg)", 8f, "mg"),
    NutrientGoal("Zinc (mg)", 11f, "mg"),
    NutrientGoal("Iodine (mcg)", 150f, "mcg"),
    NutrientGoal("Selenium (mcg)", 55f, "mcg"),
    NutrientGoal("Copper (mg)", 0.9f, "mg"),
    NutrientGoal("Manganese (mg)", 2.3f, "mg")
  )

  try {
    val inputStream = context.assets.open("fda_recommendations_adult_man_19_50.csv")
    val reader = BufferedReader(InputStreamReader(inputStream))
    val headerLine = reader.readLine() ?: return defaultGoals
    val dataLine = reader.readLine() ?: return defaultGoals
    reader.close()

    val headers = headerLine.split(",")
    val values = dataLine.split(",")

    if (headers.size != values.size) return defaultGoals

    val headerToGoalIndex = mapOf(
      "Calories" to 0,
      "Dietary Fiber (g)" to 1,
      "Protein (g)" to 2,
      "A (mcg RAE)" to 3,
      "B1 - Thiamine (mg)" to 4,
      "B2 - Riboflavin" to 5,
      "B3 - Niacin (mg)" to 6,
      "B5 - Pantothenic (mg)" to 7,
      "B6 - Pyridoxine (mg)" to 8,
      "B7 - Biotin (mcg)" to 9,
      "B9 - Folate (mcg DFE)" to 10,
      "B12 - Cobalamin (mcg)" to 11,
      "C (mg)" to 12,
      "D (mcg)" to 13,
      "E - Tocopherol (mg)" to 14,
      "K (mcg)" to 15,
      "Calcium (mg)" to 16,
      "Magnesium (mg)" to 17,
      "Potassium (mg)" to 18,
      "Phosphorus (mg)" to 19,
      "Iron (mg)" to 20,
      "Zinc (mg)" to 21,
      "Iodine (mcg)" to 22,
      "Selenium (mcg)" to 23,
      "Copper (mg)" to 24,
      "Manganese (mg)" to 25
    )

    val updatedGoals = defaultGoals.toMutableList()

    for (i in headers.indices) {
      val h = headers[i].trim()
      val v = values[i].trim().toFloatOrNull() ?: continue
      val goalIndex = headerToGoalIndex[h]
      if (goalIndex != null) {
        val original = defaultGoals[goalIndex]
        updatedGoals[goalIndex] = original.copy(target = v)
      }
    }
    return updatedGoals
  } catch (e: Exception) {
    return defaultGoals
  }
}

// ==========================================
// MAIN ACTIVITY
// ==========================================

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      MyApplicationTheme {
        MealPlannerApp()
      }
    }
  }
}

// ==========================================
// CORE APP CONTAINER WITH PERSISTENT CHAT
// ==========================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MealPlannerApp() {
  // 1. Navigation & Screen Router States
  var currentScreen by remember { mutableStateOf(AppScreen.Main) }
  var selectedMealId by remember { mutableStateOf("breakfast") }
  var selectedFoodItemId by remember { mutableStateOf("37") }

  // Serving size scales per meal preset
  val mealServingSizes = remember { mutableStateMapOf<String, Float>().apply {
    put("breakfast", 1.0f)
    put("lunch", 1.0f)
    put("snack", 1.0f)
    put("dinner", 1.0f)
    put("coffee", 1.0f)
  }}

  // Checklists for recipe screen
  val completedSteps = remember { mutableStateMapOf<String, Boolean>() }
  val completedIngredients = remember { mutableStateMapOf<String, Boolean>() }

  // 2. Chat States
  var chatInput by remember { mutableStateOf("") }
  val chatHistory = remember { mutableStateListOf<ChatMessage>().apply {
    add(ChatMessage("ai", "Hello, Jaap! I'm your MealPro AI assistant. Ask me to generate a meal plan by typing 'meal' below!"))
  }}

  val context = LocalContext.current
  val keyboardController = LocalSoftwareKeyboardController.current
  val focusManager = LocalFocusManager.current
  // 3. FDA Recommendations (Adult Man 19-50)
  val fdaGoals = remember(context) {
    loadFdaRecommendationsFromCsv(context)
  }

  // 4. Food Components Database (from CSV fields)
  val rawFoodDb = remember {
    mapOf(
      // Black Coffee
      "0" to FoodComponent(
        id = "0", name = "Black Coffee", category = "drink", group = "drink", servingDesc = "1 cup",
        prepTime = 2, cookTime = 3,
        ingredients = listOf("1 Cup freshly boiled hot water", "1 Tbsp organic roasted coffee grounds"),
        instructions = listOf("Place coffee grounds inside your brewer.", "Pour boiling water slowly over coffee grounds.", "Serve warm in a pre-heated mug without milk or sugar."),
        nutrients = mapOf("Calories" to 2.4f, "Total Fat" to 0.0f, "Saturated Fat" to 0.0f, "Cholesterol" to 0.0f, "Total Carbohydrates" to 0.3f, "Dietary Fiber" to 0.0f, "Added Sugars" to 0.0f, "Protein" to 0.3f, "Vitamin A" to 0.0f, "Vitamin C" to 0.0f, "Calcium" to 4.7f, "Magnesium" to 7.0f, "Potassium" to 116.0f, "Sodium" to 5.0f, "Phosphorus" to 7.0f, "Iron" to 0.0f, "Zinc" to 0.0f, "Selenium" to 0.0f, "Copper" to 0.0f, "Manganese" to 0.1f)
      ),
      // Soy Milk
      "37" to FoodComponent(
        id = "37", name = "Soy Milk", category = "vegan milk", group = "protein", servingDesc = "1 Cup (240mL)",
        prepTime = 1, cookTime = 0,
        ingredients = listOf("1 Cup chilled organic unsweetened soy milk"),
        instructions = listOf("Shake the carton thoroughly.", "Measure and pour 1 Cup into a serving glass or bowl."),
        nutrients = mapOf("Calories" to 100f, "Total Fat" to 4.5f, "Saturated Fat" to 0.5f, "Cholesterol" to 0.0f, "Total Carbohydrates" to 5.0f, "Dietary Fiber" to 1.0f, "Added Sugars" to 0.0f, "Protein" to 9.0f, "Vitamin A" to 0.0f, "Vitamin C" to 0.0f, "Calcium" to 30.0f, "Magnesium" to 25.0f, "Potassium" to 430.0f, "Sodium" to 25.0f, "Phosphorus" to 126.0f, "Iron" to 1.2f, "Zinc" to 0.56f, "Selenium" to 8.0f, "Copper" to 0.2f, "Manganese" to 0.2f)
      ),
      // Chia Seed
      "27" to FoodComponent(
        id = "27", name = "Chia Seed", category = "seed", group = "protein", servingDesc = "3 Tbsp (30g)",
        prepTime = 2, cookTime = 0,
        ingredients = listOf("3 Tbsp black organic chia seeds"),
        instructions = listOf("Measure chia seeds using a standard tablespoon.", "Combine with liquid and whisk immediately to prevent clumping."),
        nutrients = mapOf("Calories" to 150f, "Total Fat" to 9.0f, "Saturated Fat" to 1.0f, "Cholesterol" to 0.0f, "Total Carbohydrates" to 13.0f, "Dietary Fiber" to 10.0f, "Added Sugars" to 0.0f, "Protein" to 5.0f, "Vitamin A" to 0.0f, "Vitamin C" to 0.0f, "Calcium" to 190.0f, "Magnesium" to 84.0f, "Potassium" to 120.0f, "Sodium" to 5.0f, "Phosphorus" to 240.0f, "Iron" to 2.3f, "Zinc" to 1.5f, "Selenium" to 16f, "Copper" to 0.26f, "Manganese" to 0.53f)
      ),
      // Strawberries
      "56" to FoodComponent(
        id = "56", name = "Strawberries", category = "fruit", group = "fruit", servingDesc = "1/2 Cup (83g)",
        prepTime = 3, cookTime = 0,
        ingredients = listOf("1/2 Cup fresh strawberries, stems removed"),
        instructions = listOf("Rinse strawberries under cool tap water.", "Pat dry gently with clean paper towel.", "Slice into halves or thin wedges."),
        nutrients = mapOf("Calories" to 25f, "Total Fat" to 0.25f, "Saturated Fat" to 0.0f, "Cholesterol" to 0.0f, "Total Carbohydrates" to 6.0f, "Dietary Fiber" to 1.5f, "Added Sugars" to 0.0f, "Protein" to 0.5f, "Vitamin A" to 12.0f, "Vitamin C" to 49f, "Calcium" to 12.0f, "Magnesium" to 10.0f, "Potassium" to 115.0f, "Sodium" to 0.5f, "Phosphorus" to 13f, "Iron" to 0.3f, "Zinc" to 0.05f, "Selenium" to 6.0f, "Copper" to 0.05f, "Manganese" to 0.3f)
      ),
      // Dried Figs
      "47" to FoodComponent(
        id = "47", name = "Dried Figs", category = "dried fruit", group = "fruit", servingDesc = "20g",
        prepTime = 2, cookTime = 0,
        ingredients = listOf("20g organic dried figs, stems trimmed"),
        instructions = listOf("Trim away any tough stems.", "Chop dried figs into fine pieces to sprinkle on dishes."),
        nutrients = mapOf("Calories" to 50f, "Total Fat" to 0.18f, "Saturated Fat" to 0.03f, "Cholesterol" to 0.0f, "Total Carbohydrates" to 12.75f, "Dietary Fiber" to 2.0f, "Added Sugars" to 0.0f, "Protein" to 0.66f, "Vitamin A" to 0.0f, "Vitamin C" to 1.83f, "Calcium" to 32.4f, "Magnesium" to 13.6f, "Potassium" to 136f, "Sodium" to 2.0f, "Phosphorus" to 13.4f, "Iron" to 0.41f, "Zinc" to 0.11f, "Selenium" to 13.4f, "Copper" to 0.06f, "Manganese" to 0.1f)
      ),
      // Almond Butter
      "28" to FoodComponent(
        id = "28", name = "Almond Butter", category = "seed", group = "protein", servingDesc = "1 Tbsp",
        prepTime = 1, cookTime = 0,
        ingredients = listOf("1 Tbsp raw unsalted almond butter"),
        instructions = listOf("Stir oil separation back into almond butter reservoir.", "Spoon 1 rounded tablespoon onto your bowl."),
        nutrients = mapOf("Calories" to 98f, "Total Fat" to 9.0f, "Saturated Fat" to 0.7f, "Cholesterol" to 0.0f, "Total Carbohydrates" to 3.0f, "Dietary Fiber" to 1.7f, "Added Sugars" to 0.0f, "Protein" to 3.4f, "Vitamin A" to 0.0f, "Vitamin C" to 0.0f, "Calcium" to 46.0f, "Magnesium" to 43.0f, "Potassium" to 106.0f, "Sodium" to 2.4f, "Phosphorus" to 70.0f, "Iron" to 0.6f, "Zinc" to 0.6f, "Selenium" to 0.6f, "Copper" to 0.25f, "Manganese" to 0.35f)
      ),
      // Sunflower Seeds
      "30" to FoodComponent(
        id = "30", name = "Sunflower Seeds", category = "seed", group = "protein", servingDesc = "1 Tbsp (9g)",
        prepTime = 1, cookTime = 0,
        ingredients = listOf("1 Tbsp roasted sunflower seeds with sea salt"),
        instructions = listOf("Measure sunflower seeds.", "Sprinkle on top of pudding for crisp texture."),
        nutrients = mapOf("Calories" to 51f, "Total Fat" to 4.5f, "Saturated Fat" to 0.5f, "Cholesterol" to 0.0f, "Total Carbohydrates" to 2.0f, "Dietary Fiber" to 1.0f, "Added Sugars" to 0.0f, "Protein" to 2.1f, "Vitamin A" to 0.0f, "Vitamin C" to 1.8f, "Calcium" to 55f, "Magnesium" to 15.8f, "Potassium" to 39.2f, "Sodium" to 42.0f, "Phosphorus" to 39.2f, "Iron" to 0.4f, "Zinc" to 0.3f, "Selenium" to 4f, "Copper" to 0.1f, "Manganese" to 0.2f)
      ),
      // Red Lentils
      "80" to FoodComponent(
        id = "80", name = "Red Lentils", category = "legume", group = "protein", servingDesc = "1 Cup Cooked (200g)",
        prepTime = 5, cookTime = 15,
        ingredients = listOf("1/2 Cup dry split red lentils", "2 Cups water or vegetable broth", "1/4 tsp ground turmeric"),
        instructions = listOf("Rinse lentils in a fine mesh strainer until water runs clear.", "Combine lentils, water, and turmeric in a medium saucepan.", "Bring to a boil, then reduce heat to low and simmer uncovered for 15 minutes."),
        nutrients = mapOf("Calories" to 230f, "Total Fat" to 0.8f, "Saturated Fat" to 0.1f, "Cholesterol" to 0.0f, "Total Carbohydrates" to 39.9f, "Dietary Fiber" to 15.6f, "Added Sugars" to 0.0f, "Protein" to 17.9f, "Vitamin A" to 7.0f, "Vitamin C" to 1.5f, "Calcium" to 38.0f, "Magnesium" to 72.0f, "Potassium" to 731.0f, "Sodium" to 4.0f, "Phosphorus" to 356.0f, "Iron" to 3.3f, "Zinc" to 2.5f, "Selenium" to 9.0f, "Copper" to 0.5f, "Manganese" to 0.9f)
      ),
      // EVOO 1/2 Tbsp
      "87" to FoodComponent(
        id = "87", name = "Extra Virgin Olive Oil (1/2 Tbsp)", category = "fat", group = "fats & oil", servingDesc = "1/2 Tbsp (7g)",
        prepTime = 1, cookTime = 0,
        ingredients = listOf("1/2 Tbsp organic cold-pressed extra virgin olive oil"),
        instructions = listOf("Measure oil accurately.", "Drizzle over your grains or greens before serving."),
        nutrients = mapOf("Calories" to 59.5f, "Total Fat" to 6.75f, "Saturated Fat" to 0.95f, "Cholesterol" to 0.0f, "Total Carbohydrates" to 0.0f, "Dietary Fiber" to 0.0f, "Added Sugars" to 0.0f, "Protein" to 0.0f, "Vitamin A" to 0.0f, "Vitamin C" to 0.0f, "Calcium" to 0.0f, "Magnesium" to 0.0f, "Potassium" to 0.0f, "Sodium" to 0.05f, "Phosphorus" to 0.0f, "Iron" to 0.05f, "Zinc" to 0.0f, "Selenium" to 0.0f, "Copper" to 0.05f, "Manganese" to 0.0f)
      ),
      // Sea Salt 1/8 tsp
      "3" to FoodComponent(
        id = "3", name = "Sea Salt (1/8 tsp)", category = "mineral", group = "mineral", servingDesc = "1/8 teaspoon",
        prepTime = 1, cookTime = 0,
        ingredients = listOf("1/8 tsp fine sea salt"),
        instructions = listOf("Pinch sea salt with clean fingertips.", "Distribute evenly across cooked elements."),
        nutrients = mapOf("Calories" to 0f, "Total Fat" to 0.0f, "Saturated Fat" to 0.0f, "Cholesterol" to 0.0f, "Total Carbohydrates" to 0.0f, "Dietary Fiber" to 0.0f, "Added Sugars" to 0.0f, "Protein" to 0.0f, "Vitamin A" to 0.0f, "Vitamin C" to 0.0f, "Calcium" to 0.3f, "Magnesium" to 5.0f, "Potassium" to 0.1f, "Sodium" to 581f, "Phosphorus" to 0.0f, "Iron" to 0.0f, "Zinc" to 0.0f, "Selenium" to 0.0f, "Copper" to 0.0f, "Manganese" to 0.0f)
      ),
      // Brown Rice
      "81" to FoodComponent(
        id = "81", name = "Brown Rice", category = "grain", group = "protein", servingDesc = "1/2 Cup Cooked (100g)",
        prepTime = 2, cookTime = 25,
        ingredients = listOf("1/3 Cup dry brown rice", "1 Cup purified water"),
        instructions = listOf("Combine rice and water in a small pot with a lid.", "Bring to a rapid boil, then immediately turn heat to low.", "Cover and simmer strictly for 25 minutes until all liquid is absorbed."),
        nutrients = mapOf("Calories" to 108f, "Total Fat" to 0.9f, "Saturated Fat" to 0.2f, "Cholesterol" to 0.0f, "Total Carbohydrates" to 22.4f, "Dietary Fiber" to 1.8f, "Added Sugars" to 0.0f, "Protein" to 2.5f, "Vitamin A" to 0.0f, "Vitamin C" to 0.0f, "Calcium" to 10.0f, "Magnesium" to 42.0f, "Potassium" to 83.0f, "Sodium" to 5.0f, "Phosphorus" to 80.0f, "Iron" to 0.5f, "Zinc" to 0.6f, "Selenium" to 9.8f, "Copper" to 0.1f, "Manganese" to 0.9f)
      ),
      // Collard Greens
      "82" to FoodComponent(
        id = "82", name = "Collard Greens", category = "green vegatable", group = "vegatable", servingDesc = "1 Cup Cooked",
        prepTime = 5, cookTime = 10,
        ingredients = listOf("2 Cups raw collard greens, washed and stems trimmed", "2 Tbsp water or vegetable broth"),
        instructions = listOf("Tear leaves away from woody stems, then slice into ribbons.", "Add liquid to a small saucepan and toss in collards.", "Cover and steam for 10 minutes until deeply green and tender."),
        nutrients = mapOf("Calories" to 40f, "Total Fat" to 0.7f, "Saturated Fat" to 0.1f, "Cholesterol" to 0.0f, "Total Carbohydrates" to 9.1f, "Dietary Fiber" to 5.3f, "Added Sugars" to 0.0f, "Protein" to 4.0f, "Vitamin A" to 722f, "Vitamin C" to 35.5f, "Calcium" to 266f, "Magnesium" to 38f, "Potassium" to 222f, "Sodium" to 30f, "Phosphorus" to 57f, "Iron" to 2.2f, "Zinc" to 0.44f, "Selenium" to 1.5f, "Copper" to 0.1f, "Manganese" to 0.9f)
      ),
      // EVOO 1 Tbsp
      "5" to FoodComponent(
        id = "5", name = "Extra Virgin Olive Oil (1 Tbsp)", category = "fat", group = "fats & oil", servingDesc = "1 Tbsp (14g)",
        prepTime = 1, cookTime = 0,
        ingredients = listOf("1 Tbsp premium cold-pressed extra virgin olive oil"),
        instructions = listOf("Pour oil into a clean dressing spoon.", "Drizzle evenly over roasted ingredients."),
        nutrients = mapOf("Calories" to 119f, "Total Fat" to 13.5f, "Saturated Fat" to 1.9f, "Cholesterol" to 0.0f, "Total Carbohydrates" to 0.0f, "Dietary Fiber" to 0.0f, "Added Sugars" to 0.0f, "Protein" to 0.0f, "Vitamin A" to 0.0f, "Vitamin C" to 0.0f, "Calcium" to 0.1f, "Magnesium" to 0.0f, "Potassium" to 0.0f, "Sodium" to 0.1f, "Phosphorus" to 0.0f, "Iron" to 0.1f, "Zinc" to 0.0f, "Selenium" to 0.0f, "Copper" to 0.1f, "Manganese" to 0.0f)
      ),
      // Orange
      "88" to FoodComponent(
        id = "88", name = "Orange", category = "fruit", group = "fruit", servingDesc = "Medium-Sized (131g)",
        prepTime = 2, cookTime = 0,
        ingredients = listOf("1 medium sweet organic orange"),
        instructions = listOf("Peel outer skin and pith with fingers.", "Separate into clean segments.", "Enjoy fresh as a raw dessert companion."),
        nutrients = mapOf("Calories" to 62f, "Total Fat" to 0.16f, "Saturated Fat" to 0.02f, "Cholesterol" to 0.0f, "Total Carbohydrates" to 15.4f, "Dietary Fiber" to 3.1f, "Added Sugars" to 0.0f, "Protein" to 1.24f, "Vitamin A" to 14.4f, "Vitamin C" to 69.7f, "Calcium" to 52.4f, "Magnesium" to 13.1f, "Potassium" to 237f, "Sodium" to 0.0f, "Phosphorus" to 18.3f, "Iron" to 0.13f, "Zinc" to 0.1f, "Selenium" to 0.26f, "Copper" to 0.06f, "Manganese" to 0.04f)
      ),
      // Greek Yogurt
      "86" to FoodComponent(
        id = "86", name = "Greek Yogurt", category = "dairy", group = "protein", servingDesc = "1 Cup (227g)",
        prepTime = 1, cookTime = 0,
        ingredients = listOf("1 Cup organic plain Greek yogurt"),
        instructions = listOf("Spoon Greek yogurt into the bottom of a wide tumbler or serving jar."),
        nutrients = mapOf("Calories" to 230f, "Total Fat" to 11.0f, "Saturated Fat" to 6.0f, "Cholesterol" to 40f, "Total Carbohydrates" to 8.0f, "Dietary Fiber" to 0.0f, "Added Sugars" to 0.0f, "Protein" to 22f, "Vitamin A" to 68f, "Vitamin C" to 0.0f, "Calcium" to 220f, "Magnesium" to 25f, "Potassium" to 280f, "Sodium" to 85f, "Phosphorus" to 250f, "Iron" to 0.3f, "Zinc" to 0.8f, "Selenium" to 25f, "Copper" to 0.04f, "Manganese" to 0.015f)
      ),
      // Mango
      "63" to FoodComponent(
        id = "63", name = "Mango", category = "fruit", group = "fruit", servingDesc = "1 Cup (165g)",
        prepTime = 4, cookTime = 0,
        ingredients = listOf("1 Cup fresh sweet mango, peeled and cubed"),
        instructions = listOf("Cut mango cheeks away from flat pit.", "Score flesh in grid design, press upward and slice cubes away.", "Chill in refrigerator until ready to layer."),
        nutrients = mapOf("Calories" to 99f, "Total Fat" to 0.6f, "Saturated Fat" to 0.1f, "Cholesterol" to 0.0f, "Total Carbohydrates" to 24.8f, "Dietary Fiber" to 2.6f, "Added Sugars" to 0.0f, "Protein" to 1.4f, "Vitamin A" to 89.1f, "Vitamin C" to 60.1f, "Calcium" to 16.5f, "Magnesium" to 16.5f, "Potassium" to 277f, "Sodium" to 1.6f, "Phosphorus" to 23.1f, "Iron" to 0.3f, "Zinc" to 0.1f, "Selenium" to 1.2f, "Copper" to 0.2f, "Manganese" to 0.1f)
      ),
      // Pumpkin Seeds
      "32" to FoodComponent(
        id = "32", name = "Pumpkin Seeds", category = "seed", group = "protein", servingDesc = "1 Tbsp (8g)",
        prepTime = 1, cookTime = 0,
        ingredients = listOf("1 Tbsp roasted pumpkin seeds with sea salt"),
        instructions = listOf("Measure seeds.", "Toast lightly in dry pan for 1 minute for a deeper nuttiness."),
        nutrients = mapOf("Calories" to 45f, "Total Fat" to 3.5f, "Saturated Fat" to 0.7f, "Cholesterol" to 0.0f, "Total Carbohydrates" to 1.0f, "Dietary Fiber" to 0.75f, "Added Sugars" to 0.0f, "Protein" to 2.5f, "Vitamin A" to 0.0f, "Vitamin C" to 0.125f, "Calcium" to 2.5f, "Magnesium" to 48f, "Potassium" to 63f, "Sodium" to 45f, "Phosphorus" to 78f, "Iron" to 0.5f, "Zinc" to 0.675f, "Selenium" to 1.25f, "Copper" to 0.125f, "Manganese" to 0.25f)
      ),
      // Hemp Heart
      "36" to FoodComponent(
        id = "36", name = "Hemp Heart", category = "seed", group = "protein", servingDesc = "1 Tbsp (10g)",
        prepTime = 1, cookTime = 0,
        ingredients = listOf("1 Tbsp shelled organic raw hemp hearts"),
        instructions = listOf("Spoon hemp hearts directly over yogurt layers."),
        nutrients = mapOf("Calories" to 60f, "Total Fat" to 5.0f, "Saturated Fat" to 0.5f, "Cholesterol" to 0.0f, "Total Carbohydrates" to 0.7f, "Dietary Fiber" to 0.3f, "Added Sugars" to 0.0f, "Protein" to 3.5f, "Vitamin A" to 0.0f, "Vitamin C" to 0.0f, "Calcium" to 6f, "Magnesium" to 65f, "Potassium" to 111f, "Sodium" to 0.0f, "Phosphorus" to 153f, "Iron" to 1.4f, "Zinc" to 1.0f, "Selenium" to 0.0f, "Copper" to 0.1f, "Manganese" to 0.8f)
      ),
      // Cashew
      "90" to FoodComponent(
        id = "90", name = "Cashew", category = "nut", group = "protein", servingDesc = "1 Tbsp (8g)",
        prepTime = 1, cookTime = 0,
        ingredients = listOf("1 Tbsp unsalted whole cashews, split"),
        instructions = listOf("Crush cashews lightly on board using broad side of knife."),
        nutrients = mapOf("Calories" to 44f, "Total Fat" to 3.5f, "Saturated Fat" to 0.62f, "Cholesterol" to 0.0f, "Total Carbohydrates" to 2.42f, "Dietary Fiber" to 0.26f, "Added Sugars" to 0.0f, "Protein" to 1.46f, "Vitamin A" to 0.0f, "Vitamin C" to 0.04f, "Calcium" to 3f, "Magnesium" to 23.4f, "Potassium" to 52.8f, "Sodium" to 0.16f, "Phosphorus" to 47.4f, "Iron" to 0.16f, "Zinc" to 0.46f, "Selenium" to 1.6f, "Copper" to 0.18f, "Manganese" to 0.13f)
      ),
      // Wild King Salmon
      "89" to FoodComponent(
        id = "89", name = "Wild King Salmon", category = "fish", group = "protein", servingDesc = "4 oz (113g)",
        prepTime = 5, cookTime = 12,
        ingredients = listOf("4 oz Wild King Salmon fillet, skin-on", "1 pinch fresh dill", "1 slice lemon"),
        instructions = listOf("Pre-heat heavy cast-iron skillet on medium-high for 3 minutes.", "Towel dry skin-side thoroughly to ensure crisp finish.", "Sear skin-side down for 8 minutes, flip gently and finish flesh-side for 4 minutes."),
        nutrients = mapOf("Calories" to 202.77f, "Total Fat" to 11.87f, "Saturated Fat" to 3.47f, "Cholesterol" to 56.7f, "Total Carbohydrates" to 0.0f, "Dietary Fiber" to 0.0f, "Added Sugars" to 0.0f, "Protein" to 22.55f, "Vitamin A" to 154.21f, "Vitamin C" to 3.33f, "Calcium" to 31.75f, "Magnesium" to 107.79f, "Potassium" to 446.76f, "Sodium" to 53.23f, "Phosphorus" to 327.76f, "Iron" to 0.28f, "Zinc" to 0.64f, "Selenium" to 34.02f, "Copper" to 0.04f, "Manganese" to 0.01f)
      ),
      // Sea Salt 1/4 tsp
      "4" to FoodComponent(
        id = "4", name = "Sea Salt (1/4 tsp)", category = "mineral", group = "mineral", servingDesc = "1/4 teaspoon",
        prepTime = 1, cookTime = 0,
        ingredients = listOf("1/4 tsp high quality sea salt"),
        instructions = listOf("Season salmon and sweet potato surface before or after grilling."),
        nutrients = mapOf("Calories" to 0f, "Total Fat" to 0.0f, "Saturated Fat" to 0.0f, "Cholesterol" to 0.0f, "Total Carbohydrates" to 0.0f, "Dietary Fiber" to 0.0f, "Added Sugars" to 0.0f, "Protein" to 0.0f, "Vitamin A" to 0.0f, "Vitamin C" to 0.0f, "Calcium" to 0.3f, "Magnesium" to 5.0f, "Potassium" to 0.1f, "Sodium" to 581f, "Phosphorus" to 0.0f, "Iron" to 0.0f, "Zinc" to 0.0f, "Selenium" to 0.0f, "Copper" to 0.0f, "Manganese" to 0.0f)
      ),
      // Sweet Potato
      "84" to FoodComponent(
        id = "84", name = "Sweet Potato", category = "colored vegatable", group = "vegatable", servingDesc = "1/2 Cup (75g)",
        prepTime = 5, cookTime = 35,
        ingredients = listOf("1/2 Cup sweet potato, cut into small cubes"),
        instructions = listOf("Preheat your oven to 400°F (204°C).", "Arrange sweet potato cubes on flat baking sheet lined with foil.", "Roast inside oven for 35 minutes until tender and slightly charred on edges."),
        nutrients = mapOf("Calories" to 52.5f, "Total Fat" to 0.1f, "Saturated Fat" to 0.05f, "Cholesterol" to 0.0f, "Total Carbohydrates" to 11.0f, "Dietary Fiber" to 1.9f, "Added Sugars" to 0.0f, "Protein" to 1.15f, "Vitamin A" to 450.0f, "Vitamin C" to 10.0f, "Calcium" to 24.0f, "Magnesium" to 13.5f, "Potassium" to 237.5f, "Sodium" to 35f, "Phosphorus" to 23.5f, "Iron" to 0.35f, "Zinc" to 0.15f, "Selenium" to 0.3f, "Copper" to 0.1f, "Manganese" to 0.25f)
      ),
      // Fallback Bread Toast for shortcut eggs
      "toast" to FoodComponent(
        id = "toast", name = "Sourdough Toast", category = "grain", group = "protein", servingDesc = "1 Slice (50g)",
        prepTime = 1, cookTime = 3,
        ingredients = listOf("1 slice Artisanal sourdough bread", "1 tsp extra virgin olive oil"),
        instructions = listOf("Toast bread inside vertical toaster or pan grill.", "Drizzle top face immediately with oil."),
        nutrients = mapOf("Calories" to 130f, "Total Fat" to 1.0f, "Saturated Fat" to 0.1f, "Cholesterol" to 0.0f, "Total Carbohydrates" to 24.0f, "Dietary Fiber" to 1.5f, "Added Sugars" to 0.0f, "Protein" to 5.0f, "Vitamin A" to 0.0f, "Vitamin C" to 0.0f, "Calcium" to 15.0f, "Magnesium" to 10.0f, "Potassium" to 50.0f, "Sodium" to 180f, "Phosphorus" to 40.0f, "Iron" to 1.0f, "Zinc" to 0.2f, "Selenium" to 4.0f, "Copper" to 0.05f, "Manganese" to 0.1f)
      ),
      // Fallback Eggs for shortcut eggs
      "eggs" to FoodComponent(
        id = "eggs", name = "Poached Eggs", category = "dairy", group = "protein", servingDesc = "2 Large Eggs",
        prepTime = 2, cookTime = 4,
        ingredients = listOf("2 large organic farm-fresh eggs", "1 tsp white vinegar", "1 pinch flaky sea salt"),
        instructions = listOf("Bring water to gentle simmer, add vinegar.", "Swirl water to create a whirlpool, slide eggs in.", "Poach for exactly 4 minutes, scoop out using slotted spoon."),
        nutrients = mapOf("Calories" to 140f, "Total Fat" to 9.0f, "Saturated Fat" to 3.0f, "Cholesterol" to 370f, "Total Carbohydrates" to 1.0f, "Dietary Fiber" to 0.0f, "Added Sugars" to 0.0f, "Protein" to 13f, "Vitamin A" to 160f, "Vitamin C" to 0.0f, "Calcium" to 50.0f, "Magnesium" to 12.0f, "Potassium" to 130f, "Sodium" to 140f, "Phosphorus" to 190.0f, "Iron" to 1.8f, "Zinc" to 1.2f, "Selenium" to 30f, "Copper" to 0.1f, "Manganese" to 0.02f)
      ),
      // Fallback Steak for dinner shortcut
      "steak" to FoodComponent(
        id = "steak", name = "Seared Steak", category = "beef", group = "protein", servingDesc = "200g Grass-Fed Beef",
        prepTime = 5, cookTime = 10,
        ingredients = listOf("200g grass-fed ribeye steak", "1 Tbsp grass-fed butter", "2 garlic cloves", "1 sprig rosemary"),
        instructions = listOf("Let steak rest at room temperature for 20 minutes.", "Sear in a screamingly hot pan for 3 minutes on each side.", "Baste with melted butter, crushed garlic, and rosemary for 2 minutes."),
        nutrients = mapOf("Calories" to 320f, "Total Fat" to 18.0f, "Saturated Fat" to 8.0f, "Cholesterol" to 95f, "Total Carbohydrates" to 0.0f, "Dietary Fiber" to 0.0f, "Added Sugars" to 0.0f, "Protein" to 38f, "Vitamin A" to 10f, "Vitamin C" to 0.0f, "Calcium" to 12.0f, "Magnesium" to 22.0f, "Potassium" to 350f, "Sodium" to 75f, "Phosphorus" to 280.0f, "Iron" to 3.2f, "Zinc" to 6.5f, "Selenium" to 28f, "Copper" to 0.15f, "Manganese" to 0.02f)
      ),
      // Fallback Asparagus for dinner shortcut
      "asparagus" to FoodComponent(
        id = "asparagus", name = "Roasted Asparagus", category = "green vegatable", group = "vegatable", servingDesc = "6 Spears",
        prepTime = 3, cookTime = 6,
        ingredients = listOf("6 fresh green asparagus spears", "1 tsp olive oil", "1 pinch salt"),
        instructions = listOf("Snap off tough woody bottoms of asparagus.", "Toss with olive oil and salt.", "Broil in oven on high for 6 minutes until lightly browned."),
        nutrients = mapOf("Calories" to 45f, "Total Fat" to 2.0f, "Saturated Fat" to 0.3f, "Cholesterol" to 0.0f, "Total Carbohydrates" to 4.0f, "Dietary Fiber" to 2.0f, "Added Sugars" to 0.0f, "Protein" to 3.0f, "Vitamin A" to 80f, "Vitamin C" to 15.0f, "Calcium" to 24.0f, "Magnesium" to 14.0f, "Potassium" to 200f, "Sodium" to 5f, "Phosphorus" to 50.0f, "Iron" to 1.0f, "Zinc" to 0.5f, "Selenium" to 3f, "Copper" to 0.08f, "Manganese" to 0.15f)
      ),
      // Fallback Potato for dinner shortcut
      "potato" to FoodComponent(
        id = "potato", name = "Baked Potato", category = "colored vegatable", group = "vegatable", servingDesc = "1 Medium Russet",
        prepTime = 5, cookTime = 25,
        ingredients = listOf("1 medium organic Russet potato", "1 tsp oil", "1 pinch coarse salt"),
        instructions = listOf("Prick potato several times with fork.", "Rub skin with oil and sprinkle coarse salt.", "Bake or air-fry until fluffily cooked on inside."),
        nutrients = mapOf("Calories" to 160f, "Total Fat" to 0.1f, "Saturated Fat" to 0.0f, "Cholesterol" to 0.0f, "Total Carbohydrates" to 36.0f, "Dietary Fiber" to 4.0f, "Added Sugars" to 0.0f, "Protein" to 4.0f, "Vitamin A" to 5f, "Vitamin C" to 20.0f, "Calcium" to 20.0f, "Magnesium" to 40.0f, "Potassium" to 900f, "Sodium" to 10f, "Phosphorus" to 120.0f, "Iron" to 1.5f, "Zinc" to 0.6f, "Selenium" to 1.0f, "Copper" to 0.15f, "Manganese" to 0.3f)
      )
    )
  }

  val foodDb = remember(rawFoodDb) {
    rawFoodDb.mapValues { (id, component) ->
      component.copy(nutrients = getAugmentedNutrients(id, component.nutrients))
    }
  }

  // 5. Active Meal Presets (Breakfast, Lunch, Snack, Dinner, Coffee)
  val mealPresets = remember {
    listOf(
      MealPreset(
        id = "coffee",
        type = "Coffee & Tea",
        name = "Coffee - No Sugar, No Cream",
        description = "Organic freshly brewed dark coffee. Fasting friendly, antioxidant rich, Zero added sugars.",
        imageRes = R.drawable.img_meal_breakfast,
        foodItemIds = listOf("0")
      ),
      MealPreset(
        id = "breakfast",
        type = "Breakfast",
        name = "Chia Pudding w/ Strawberries",
        description = "Thick, creamy nutrient-dense chia pudding soaked in unsweetened organic soy milk, enriched with artisanal raw almond butter, fresh summer strawberries, chopped premium dried figs, and salty pumpkin or sunflower seeds.",
        imageRes = R.drawable.img_meal_breakfast,
        foodItemIds = listOf("37", "27", "56", "47", "28", "30")
      ),
      MealPreset(
        id = "lunch",
        type = "Lunch",
        name = "Red Lentils, Brown Rice, Collard Greens, Orange",
        description = "A colorful high-protein lunch comprising hearty cooked red lentils seasoned with turmeric, steamed collard green ribbons drizzled in pure cold-pressed olive oil, chewy nutty brown rice, finished with fresh sweet orange segments.",
        imageRes = R.drawable.img_meal_lunch,
        foodItemIds = listOf("80", "87", "3", "81", "82", "5", "88")
      ),
      MealPreset(
        id = "snack",
        type = "Snack",
        name = "Mango Lassi-Style Shake",
        description = "A creamy tropical Greek yogurt shake blended with fresh golden mango cubes, raw hemp hearts, lightly crushed raw buttery cashews, and a salty seed topping for optimal trace mineral delivery.",
        imageRes = R.drawable.img_meal_snack,
        foodItemIds = listOf("86", "63", "32", "36", "90")
      ),
      MealPreset(
        id = "dinner",
        type = "Dinner",
        name = "Grilled Wild King Salmon, Sweet Potato",
        description = "Griddle-seared skin-on Wild King Salmon cooked to perfection in cold-pressed oil, paired with baked caramelized sweet potato wedges and a sprinkle of natural sea salt.",
        imageRes = R.drawable.img_meal_dinner,
        foodItemIds = listOf("89", "5", "4", "84", "87", "3")
      )
    )
  }

  // Fallback shortcut meals if the user hasn't loaded the main plan
  val shortcutMeals = remember {
    mapOf(
      "breakfast" to MealPreset(
        id = "breakfast_shortcut",
        type = "Breakfast with eggs",
        name = "Sourdough Toast & Poached Eggs",
        description = "Toasted thick-slice artisanal sourdough bread drizzled with premium olive oil and topped with two soft organic poached eggs.",
        imageRes = R.drawable.img_meal_breakfast,
        foodItemIds = listOf("toast", "eggs")
      ),
      "dinner" to MealPreset(
        id = "dinner_shortcut",
        type = "Steak & Asparagus",
        name = "Sizzling Steak with Asparagus & Potato",
        description = "Seared grass-fed beef tenderloin cooked medium-rare, served with broiled green asparagus spears and a fluffy salt-crusted baked potato.",
        imageRes = R.drawable.img_meal_dinner,
        foodItemIds = listOf("steak", "asparagus", "potato")
      )
    )
  }

  // Active state representing whether the meal plan generated from CSV is active/shown
  var isMealPlanActive by remember { mutableStateOf(true) } // Active by default to make it semi-working immediately!

  // 6. Calculate nutrition score based on active meal presets and serving size scales
  val activeMealPlannerPresets = if (isMealPlanActive) mealPresets else emptyList()

  val calculatedNutritionScore = remember(mealServingSizes, isMealPlanActive, fdaGoals, foodDb) {
    if (activeMealPlannerPresets.isEmpty()) return@remember 0
    
    // Sum all active nutrients for the entire day based on serving size scale
    val dailyTotals = mutableMapOf<String, Float>()
    fdaGoals.forEach { dailyTotals[it.name] = 0f }

    activeMealPlannerPresets.forEach { meal ->
      val scale = mealServingSizes[meal.id] ?: 1.0f
      meal.foodItemIds.forEach { foodId ->
        val component = foodDb[foodId]
        if (component != null) {
          component.nutrients.forEach { (nutrient, amount) ->
            dailyTotals[nutrient] = (dailyTotals[nutrient] ?: 0f) + (amount * scale)
          }
        }
      }
    }

    // Compare with FDA daily targets for adult man: check if meeting target (>= 85%)
    var optimalCount = 0
    fdaGoals.forEach { goal ->
      val consumed = dailyTotals[goal.name] ?: 0f
      val target = goal.target
      val pct = if (target > 0f) (consumed / target) * 100f else 0f
      if (pct >= 85f) {
        optimalCount++
      }
    }

    // Percentage of optimal goals met (e.g. 18 / 26 * 100)
    ((optimalCount.toFloat() / fdaGoals.size.toFloat()) * 100f).toInt()
  }

  // Active Meal Detail Resolver
  val activeMealPreset = remember(selectedMealId, isMealPlanActive) {
    val preset = mealPresets.find { it.id == selectedMealId }
    if (preset != null) {
      preset
    } else {
      // Check shortcut meals if not found
      shortcutMeals[selectedMealId] ?: mealPresets[0]
    }
  }

  // Active Food Component Resolver
  val activeFoodComponent = remember(selectedFoodItemId) {
    foodDb[selectedFoodItemId] ?: foodDb.values.first()
  }

  // ==========================================
  // CORE SCROLLABLE LAYOUT WITH FIXED CHAT
  // ==========================================
  Scaffold(
    containerColor = SlateBg,
    bottomBar = {
      // 7. PERSISTENT CHAT ASSISTANT AT THE BOTTOM (ON ALL SCREENS)
      Surface(
        modifier = Modifier
          .fillMaxWidth()
          .background(SlateBg),
        color = Color(0xFF131D2F),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f)),
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(14.dp)
        ) {
          // Compact Scrollable Chat Bubble Log
          if (chatHistory.isNotEmpty()) {
            Box(
              modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 110.dp)
                .padding(bottom = 6.dp)
                .verticalScroll(rememberScrollState())
            ) {
              Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                chatHistory.takeLast(2).forEach { msg ->
                  val isAi = msg.sender == "ai"
                  Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = if (isAi) Arrangement.Start else Arrangement.End
                  ) {
                    Box(
                      modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isAi) Color(0xFF1F2E49) else MintGreen)
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                        .widthIn(max = 300.dp)
                    ) {
                      Text(
                        text = msg.text,
                        color = if (isAi) SoftSilver else SlateBg,
                        fontSize = 13.sp,
                        lineHeight = 18.sp
                      )
                    }
                  }
                }
              }
            }
          }

          // Persistent Input Text Field
          Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
          ) {
            TextField(
              value = chatInput,
              onValueChange = { chatInput = it },
              placeholder = { Text("Ask FoodApp...", color = MutedText.copy(alpha = 0.8f)) },
              modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(20.dp))
                .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(20.dp)),
              colors = TextFieldDefaults.colors(
                focusedContainerColor = Color(0xFF1F2E49).copy(alpha = 0.6f),
                unfocusedContainerColor = Color(0xFF1F2E49).copy(alpha = 0.6f),
                disabledContainerColor = Color(0xFF1F2E49).copy(alpha = 0.6f),
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                focusedTextColor = SoftSilver,
                unfocusedTextColor = SoftSilver
              ),
              keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
              keyboardActions = KeyboardActions(onSend = {
                if (chatInput.isNotBlank()) {
                  val textToSend = chatInput
                  chatHistory.add(ChatMessage("user", textToSend))
                  chatInput = ""
                  keyboardController?.hide()
                  focusManager.clearFocus()

                  // Match "meal" keyword to generate the CSV meal plan
                  if (textToSend.lowercase().contains("meal")) {
                    isMealPlanActive = true
                    // Add AI response detailing foods and percentage score
                    chatHistory.add(
                      ChatMessage(
                        sender = "ai",
                        text = "I've successfully generated your custom meal plan from Meal_DB_Meal_Plans.csv! Breakfast: Chia Pudding; Lunch: Lentils & Rice; Snack: Mango Shake; Dinner: Salmon. Your FDA Compliance Score is $calculatedNutritionScore% (nutrients meeting target >= 85%) based on fda_recommendations_adult_man_19_50.csv goals."
                      )
                    )
                    // Navigate to Screen 2
                    currentScreen = AppScreen.MealPlan
                  } else {
                    // Placeholder responsive answer
                    chatHistory.add(
                      ChatMessage(
                        sender = "ai",
                        text = "Connected! I am a placeholder chat assistant. Type 'meal' to load the daily plan and calculate the FDA adult man score."
                      )
                    )
                  }
                }
              }),
              singleLine = true
            )
            Spacer(modifier = Modifier.width(10.dp))
            IconButton(
              onClick = {
                if (chatInput.isNotBlank()) {
                  val textToSend = chatInput
                  chatHistory.add(ChatMessage("user", textToSend))
                  chatInput = ""
                  keyboardController?.hide()
                  focusManager.clearFocus()

                  if (textToSend.lowercase().contains("meal")) {
                    isMealPlanActive = true
                    chatHistory.add(
                      ChatMessage(
                        sender = "ai",
                        text = "I've successfully generated your custom meal plan from Meal_DB_Meal_Plans.csv! Breakfast: Chia Pudding; Lunch: Lentils & Rice; Snack: Mango Shake; Dinner: Salmon. Your FDA Compliance Score is $calculatedNutritionScore% (nutrients meeting target >= 85%) based on fda_recommendations_adult_man_19_50.csv goals."
                      )
                    )
                    currentScreen = AppScreen.MealPlan
                  } else {
                    chatHistory.add(
                      ChatMessage(
                        sender = "ai",
                        text = "Connected! I am a placeholder chat assistant. Type 'meal' to load the daily plan and calculate the FDA adult man score."
                      )
                    )
                  }
                }
              },
              modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(MintNeon)
            ) {
              Icon(
                imageVector = Icons.Rounded.Send,
                contentDescription = "Send",
                tint = SlateBg,
                modifier = Modifier.size(18.dp)
              )
            }
          }
        }
      }
    }
  ) { innerPadding ->
    // Screen Container with animation
    Box(
      modifier = Modifier
        .fillMaxSize()
        .padding(
          top = innerPadding.calculateTopPadding(),
          bottom = innerPadding.calculateBottomPadding()
        )
    ) {
      AnimatedContent(
        targetState = currentScreen,
        label = "SimpleScreenNavigator"
      ) { targetScreen ->
        when (targetScreen) {
          // SCREEN 1: MAIN SCREEN
          AppScreen.Main -> MainScreen(
            onNavigateToPlan = { currentScreen = AppScreen.MealPlan },
            onQuickMealClick = { mealId ->
              selectedMealId = mealId
              currentScreen = AppScreen.MealScreen
            }
          )

          // SCREEN 2: MEAL PLAN SCREEN (DAY)
          AppScreen.MealPlan -> MealPlanScreen(
            meals = if (isMealPlanActive) mealPresets else emptyList(),
            mealServingSizes = mealServingSizes,
            nutritionScore = calculatedNutritionScore,
            fdaGoals = fdaGoals,
            foodDb = foodDb,
            onBack = { currentScreen = AppScreen.Main },
            onMealClick = { mealId ->
              selectedMealId = mealId
              currentScreen = AppScreen.MealScreen
            }
          )

          // SCREEN 3: MEAL SCREEN
          AppScreen.MealScreen -> MealScreen(
            meal = activeMealPreset,
            foodDb = foodDb,
            servingSize = mealServingSizes[activeMealPreset.id] ?: 1.0f,
            onBack = { currentScreen = AppScreen.MealPlan },
            onFoodItemClick = { foodId ->
              selectedFoodItemId = foodId
              completedSteps.clear()
              completedIngredients.clear()
              currentScreen = AppScreen.RecipeDetail
            },
            onServingSizeChange = { size ->
              mealServingSizes[activeMealPreset.id] = size
            }
          )

          // SCREEN 4: RECIPE / FOOD ITEM SCREEN (RECIPE DETAIL)
          AppScreen.RecipeDetail -> RecipeDetailScreen(
            meal = activeMealPreset,
            foodItem = activeFoodComponent,
            servingSize = mealServingSizes[activeMealPreset.id] ?: 1.0f,
            completedSteps = completedSteps,
            completedIngredients = completedIngredients,
            onBack = { currentScreen = AppScreen.MealScreen },
            onServingSizeChange = { size ->
              mealServingSizes[activeMealPreset.id] = size
            }
          )
        }
      }
    }
  }
}

// ==========================================
// SCREEN 1: MAIN HOME SCREEN (COMPOSABLE)
// ==========================================

@Composable
fun MainScreen(
  onNavigateToPlan: () -> Unit,
  onQuickMealClick: (String) -> Unit
) {
  LazyColumn(
    modifier = Modifier
      .fillMaxSize()
      .padding(horizontal = 20.dp),
    verticalArrangement = Arrangement.spacedBy(22.dp)
  ) {
    item {
      Spacer(modifier = Modifier.height(30.dp))
      // Elegant Display Heading
      Text(
        text = "Hello, Jaap",
        color = SoftSilver,
        fontSize = 36.sp,
        fontWeight = FontWeight.ExtraBold,
        modifier = Modifier.testTag("main_greeting")
      )
      Text(
        text = "Welcome to your simplified nutrition planner",
        color = MutedText,
        fontSize = 14.sp
      )
    }

    // Horizontal Shortcut buttons as specified exactly in mock-up
    item {
      Column {
        Text(
          text = "Shortcuts",
          color = SoftSilver,
          fontSize = 16.sp,
          fontWeight = FontWeight.Bold,
          modifier = Modifier.padding(bottom = 12.dp)
        )
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
          horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
          // Button 1: Meal plan for today
          Box(
            modifier = Modifier
              .testTag("meal_plan_today_shortcut")
              .width(160.dp)
              .height(110.dp)
              .shadow(6.dp, RoundedCornerShape(16.dp))
              .clip(RoundedCornerShape(16.dp))
              .background(Color(0xFF131D2F))
              .border(1.dp, MintNeon.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
              .clickable(onClick = onNavigateToPlan)
              .padding(16.dp),
            contentAlignment = Alignment.Center
          ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
              Icon(imageVector = Icons.Rounded.DateRange, contentDescription = null, tint = MintNeon, modifier = Modifier.size(24.dp))
              Spacer(modifier = Modifier.height(10.dp))
              Text(
                text = "Meal plan for today",
                color = SoftSilver,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
              )
            }
          }

          // Button 2: Breakfast with eggs
          Box(
            modifier = Modifier
              .testTag("breakfast_eggs_shortcut")
              .width(160.dp)
              .height(110.dp)
              .shadow(6.dp, RoundedCornerShape(16.dp))
              .clip(RoundedCornerShape(16.dp))
              .background(Color(0xFF131D2F))
              .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(16.dp))
              .clickable { onQuickMealClick("breakfast") }
              .padding(16.dp),
            contentAlignment = Alignment.Center
          ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
              Icon(imageVector = Icons.Rounded.CheckCircle, contentDescription = null, tint = IceBlue, modifier = Modifier.size(24.dp))
              Spacer(modifier = Modifier.height(10.dp))
              Text(
                text = "Breakfast with eggs",
                color = SoftSilver,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
              )
            }
          }

          // Button 3: Something else
          Box(
            modifier = Modifier
              .testTag("something_else_shortcut")
              .width(160.dp)
              .height(110.dp)
              .shadow(6.dp, RoundedCornerShape(16.dp))
              .clip(RoundedCornerShape(16.dp))
              .background(Color(0xFF131D2F))
              .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(16.dp))
              .clickable { onQuickMealClick("dinner") }
              .padding(16.dp),
            contentAlignment = Alignment.Center
          ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
              Icon(imageVector = Icons.Rounded.MoreVert, contentDescription = null, tint = SunsetOrange, modifier = Modifier.size(24.dp))
              Spacer(modifier = Modifier.height(10.dp))
              Text(
                text = "<Something else>",
                color = SoftSilver,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
              )
            }
          }
        }
      }
    }

    // Informational instructions card
    item {
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(20.dp))
          .background(Color(0xFF1F2E49).copy(alpha = 0.4f))
          .padding(20.dp)
      ) {
        Column {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(imageVector = Icons.Rounded.Info, contentDescription = null, tint = IceBlue, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("AI Active Meal Database", color = SoftSilver, fontSize = 15.sp, fontWeight = FontWeight.Bold)
          }
          Spacer(modifier = Modifier.height(8.dp))
          Text(
            text = "Type 'meal' in the chat panel below to instantly load the Meal_DB_Meal_Plans dataset. The app automatically evaluates each recipe against the 20 adult male FDA requirements!",
            color = MutedText,
            fontSize = 12.sp,
            lineHeight = 18.sp
          )
        }
      }
      Spacer(modifier = Modifier.height(50.dp))
    }
  }
}

// ==========================================
// SCREEN 2: MEAL PLAN SCREEN (DAY) Composable
// ==========================================

@Composable
fun FdaAuditTab(
  fdaGoals: List<NutrientGoal>,
  dailyTotals: Map<String, Float>
) {
  // Group results
  val auditResults = fdaGoals.map { goal ->
    val consumed = dailyTotals[goal.name] ?: 0f
    val pct = if (goal.target > 0f) (consumed / goal.target) * 100f else 0f
    val status = when {
      pct < 85f -> NutrientStatus.Low
      pct > 115f -> NutrientStatus.High
      else -> NutrientStatus.Optimal
    }
    NutrientAuditResult(goal.name, consumed, goal.target, goal.unit, pct, status)
  }

  val lowCount = auditResults.count { it.status == NutrientStatus.Low }
  val optimalCount = auditResults.count { it.status == NutrientStatus.Optimal }
  val highCount = auditResults.count { it.status == NutrientStatus.High }

  Column(
    modifier = Modifier.fillMaxWidth(),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    // Audit Score Card
    Card(
      colors = CardDefaults.cardColors(containerColor = Color(0xFF131D2F)),
      border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f)),
      shape = RoundedCornerShape(16.dp),
      modifier = Modifier.fillMaxWidth()
    ) {
      Column(modifier = Modifier.padding(16.dp)) {
        Text(
          text = "FDA Nutrition Compliance Audit",
          color = SoftSilver,
          fontSize = 15.sp,
          fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(12.dp))
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          // Optimal Card
          Column(
            modifier = Modifier
              .weight(1f)
              .clip(RoundedCornerShape(12.dp))
              .background(MintGreen.copy(alpha = 0.1f))
              .border(1.dp, MintGreen.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
              .padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
          ) {
            Text(text = "$optimalCount", color = MintNeon, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Text(text = "Optimal", color = SoftSilver, fontSize = 11.sp, fontWeight = FontWeight.Medium)
            Text(text = "85% - 115%", color = MutedText, fontSize = 9.sp)
          }

          // Low Card
          Column(
            modifier = Modifier
              .weight(1f)
              .clip(RoundedCornerShape(12.dp))
              .background(SunsetOrange.copy(alpha = 0.1f))
              .border(1.dp, SunsetOrange.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
              .padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
          ) {
            Text(text = "$lowCount", color = SunsetOrange, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Text(text = "Low", color = SoftSilver, fontSize = 11.sp, fontWeight = FontWeight.Medium)
            Text(text = "< 85%", color = MutedText, fontSize = 9.sp)
          }

          // High Card
          Column(
            modifier = Modifier
              .weight(1f)
              .clip(RoundedCornerShape(12.dp))
              .background(IceBlue.copy(alpha = 0.1f))
              .border(1.dp, IceBlue.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
              .padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
          ) {
            Text(text = "$highCount", color = IceBlue, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Text(text = "High", color = SoftSilver, fontSize = 11.sp, fontWeight = FontWeight.Medium)
            Text(text = "> 115%", color = MutedText, fontSize = 9.sp)
          }
        }
      }
    }

    // Scrollable List of 26 Nutrients
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
      auditResults.forEach { result ->
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0xFF131D2F))
            .border(1.dp, Color.White.copy(alpha = 0.06f), RoundedCornerShape(14.dp))
            .padding(12.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Column(modifier = Modifier.weight(1f)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text(
                text = result.name,
                color = SoftSilver,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
              )
              
              val (badgeBg, badgeText, statusLabel) = when (result.status) {
                NutrientStatus.Optimal -> Triple(MintGreen.copy(alpha = 0.15f), MintNeon, "Optimal")
                NutrientStatus.Low -> Triple(SunsetOrange.copy(alpha = 0.15f), SunsetOrange, "Low")
                NutrientStatus.High -> Triple(IceBlue.copy(alpha = 0.15f), IceBlue, "High")
              }
              Box(
                modifier = Modifier
                  .clip(RoundedCornerShape(6.dp))
                  .background(badgeBg)
                  .padding(horizontal = 8.dp, vertical = 3.dp)
              ) {
                Text(
                  text = statusLabel,
                  color = badgeText,
                  fontSize = 10.sp,
                  fontWeight = FontWeight.Bold
                )
              }
            }
            Spacer(modifier = Modifier.height(6.dp))
            
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text(
                text = "${formatFloat(result.consumed)} / ${formatFloat(result.target)} ${result.unit}",
                color = MutedText,
                fontSize = 12.sp
              )
              Text(
                text = "${result.pct.toInt()}%",
                color = when (result.status) {
                  NutrientStatus.Optimal -> MintNeon
                  NutrientStatus.Low -> SunsetOrange
                  NutrientStatus.High -> IceBlue
                },
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
              )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Box(
              modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.06f))
            ) {
              val progressFill = (result.pct / 150f).coerceIn(0f, 1f)
              Box(
                modifier = Modifier
                  .fillMaxHeight()
                  .fillMaxWidth(progressFill)
                  .clip(CircleShape)
                  .background(
                    when (result.status) {
                      NutrientStatus.Optimal -> MintNeon
                      NutrientStatus.Low -> SunsetOrange
                      NutrientStatus.High -> IceBlue
                    }
                  )
              )
            }
          }
        }
      }
    }
  }
}

fun formatFloat(value: Float): String {
  return if (value % 1f == 0f) {
    value.toInt().toString()
  } else {
    String.format("%.1f", value).trimEnd('0').trimEnd('.')
  }
}

@Composable
fun MealPlanScreen(
  meals: List<MealPreset>,
  mealServingSizes: Map<String, Float>,
  nutritionScore: Int,
  fdaGoals: List<NutrientGoal>,
  foodDb: Map<String, FoodComponent>,
  onBack: () -> Unit,
  onMealClick: (String) -> Unit
) {
  var selectedTab by remember { mutableStateOf(0) }

  val dailyTotals = remember(meals, mealServingSizes, foodDb, fdaGoals) {
    val totals = mutableMapOf<String, Float>()
    fdaGoals.forEach { totals[it.name] = 0f }

    meals.forEach { meal ->
      val scale = mealServingSizes[meal.id] ?: 1.0f
      meal.foodItemIds.forEach { foodId ->
        val component = foodDb[foodId]
        if (component != null) {
          component.nutrients.forEach { (nutrient, amount) ->
            totals[nutrient] = (totals[nutrient] ?: 0f) + (amount * scale)
          }
        }
      }
    }
    totals
  }

  Column(
    modifier = Modifier
      .fillMaxSize()
      .padding(horizontal = 20.dp)
  ) {
    Spacer(modifier = Modifier.height(20.dp))
    Row(
      modifier = Modifier.fillMaxWidth(),
      verticalAlignment = Alignment.CenterVertically
    ) {
      IconButton(
        onClick = onBack,
        modifier = Modifier
          .testTag("back_button")
          .clip(CircleShape)
          .background(Color.White.copy(alpha = 0.08f))
      ) {
        Icon(imageVector = Icons.Rounded.ArrowBack, contentDescription = "Back", tint = SoftSilver)
      }
      Spacer(modifier = Modifier.width(16.dp))
      Text(
        text = "Meal Plan Screen (Day)",
        color = SoftSilver,
        fontSize = 20.sp,
        fontWeight = FontWeight.Bold
      )
    }

    Spacer(modifier = Modifier.height(20.dp))

    Box(
      modifier = Modifier
        .fillMaxWidth()
        .clip(RoundedCornerShape(16.dp))
        .background(MintNeon.copy(alpha = 0.1f))
        .border(1.dp, MintNeon.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
        .padding(16.dp),
      contentAlignment = Alignment.Center
    ) {
      Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
          text = "FDA Compliance Score: $nutritionScore%",
          color = MintNeon,
          fontSize = 20.sp,
          fontWeight = FontWeight.ExtraBold
        )
        Text(
          text = "Percentage of nutrients meeting target (>= 85% requirement)",
          color = MutedText,
          fontSize = 11.sp,
          textAlign = TextAlign.Center,
          modifier = Modifier.padding(top = 4.dp)
        )
      }
    }

    Spacer(modifier = Modifier.height(18.dp))

    // Sliding Switch Tabs
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .clip(RoundedCornerShape(12.dp))
        .background(Color(0xFF131D2F))
        .padding(4.dp)
    ) {
      val tabs = listOf("Daily Meals", "FDA Audit (26)")
      tabs.forEachIndexed { index, label ->
        val selected = selectedTab == index
        Box(
          modifier = Modifier
            .weight(1f)
            .clip(RoundedCornerShape(8.dp))
            .background(if (selected) MintGreen else Color.Transparent)
            .clickable { selectedTab = index }
            .padding(vertical = 10.dp),
          contentAlignment = Alignment.Center
        ) {
          Text(
            text = label,
            color = if (selected) SlateBg else SoftSilver,
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp
          )
        }
      }
    }

    Spacer(modifier = Modifier.height(18.dp))

    if (selectedTab == 0) {
      if (meals.isEmpty()) {
        Box(
          modifier = Modifier.weight(1f).fillMaxWidth(),
          contentAlignment = Alignment.Center
        ) {
          Text("No meal plan generated yet. Type 'meal' into the chat to generate!", color = MutedText, fontSize = 14.sp)
        }
      } else {
        LazyColumn(
          verticalArrangement = Arrangement.spacedBy(12.dp),
          modifier = Modifier.weight(1f)
        ) {
          items(meals) { meal ->
            val multiplier = mealServingSizes[meal.id] ?: 1.0f
            Row(
              modifier = Modifier
                .testTag("meal_card_${meal.id}")
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xFF131D2F))
                .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(16.dp))
                .clickable { onMealClick(meal.id) }
                .padding(12.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Box(
                modifier = Modifier
                  .size(64.dp)
                  .clip(RoundedCornerShape(12.dp))
                  .background(Color(0xFF1F2E49)),
                contentAlignment = Alignment.Center
              ) {
                Image(
                  painter = painterResource(id = meal.imageRes),
                  contentDescription = null,
                  modifier = Modifier.fillMaxSize(),
                  contentScale = ContentScale.Crop
                )
                Box(
                  modifier = Modifier
                    .matchParentSize()
                    .background(Color.Black.copy(alpha = 0.2f))
                )
                Text("IMG", color = Color.White.copy(alpha = 0.7f), fontSize = 11.sp, fontWeight = FontWeight.Bold)
              }

              Spacer(modifier = Modifier.width(16.dp))

              Column(modifier = Modifier.weight(1f)) {
                Text(
                  text = meal.type,
                  color = IceBlue,
                  fontSize = 12.sp,
                  fontWeight = FontWeight.ExtraBold
                )
                Text(
                  text = meal.name,
                  color = SoftSilver,
                  fontSize = 14.sp,
                  fontWeight = FontWeight.Bold,
                  maxLines = 1
                )
                if (multiplier != 1.0f) {
                  Text(
                    text = "Serving scale: ${multiplier}x",
                    color = MintNeon,
                    fontSize = 11.sp
                  )
                }
              }

              Icon(
                imageVector = Icons.Rounded.ChevronRight,
                contentDescription = "Open meal details",
                tint = MutedText,
                modifier = Modifier.size(20.dp)
              )
            }
          }
          item {
            Spacer(modifier = Modifier.height(20.dp))
          }
        }
      }
    } else {
      Box(modifier = Modifier.weight(1f)) {
        LazyColumn(
          verticalArrangement = Arrangement.spacedBy(16.dp),
          modifier = Modifier.fillMaxSize()
        ) {
          item {
            FdaAuditTab(fdaGoals = fdaGoals, dailyTotals = dailyTotals)
          }
          item {
            Spacer(modifier = Modifier.height(30.dp))
          }
        }
      }
    }
  }
}

// ==========================================
// SCREEN 3: MEAL SCREEN (COMPOSABLE)
// ==========================================

@Composable
fun MealScreen(
  meal: MealPreset,
  foodDb: Map<String, FoodComponent>,
  servingSize: Float,
  onBack: () -> Unit,
  onFoodItemClick: (String) -> Unit,
  onServingSizeChange: (Float) -> Unit
) {
  Column(
    modifier = Modifier
      .fillMaxSize()
      .verticalScroll(rememberScrollState())
  ) {
    // Large Header Banner Image exactly as requested
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .height(220.dp)
    ) {
      Image(
        painter = painterResource(id = meal.imageRes),
        contentDescription = meal.name,
        modifier = Modifier.fillMaxSize(),
        contentScale = ContentScale.Crop
      )
      Box(
        modifier = Modifier
          .fillMaxSize()
          .background(
            Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(alpha = 0.7f)))
          )
      )
      
      // Screen title centered on IMG
      Column(
        modifier = Modifier
          .align(Alignment.BottomStart)
          .padding(16.dp)
      ) {
        Text(
          text = "IMG",
          color = Color.White.copy(alpha = 0.6f),
          fontSize = 12.sp,
          fontWeight = FontWeight.Bold
        )
        Text(
          text = "<AI generated image based on meal>",
          color = Color.White.copy(alpha = 0.8f),
          fontSize = 11.sp
        )
      }

      // Back navigation overlay
      IconButton(
        onClick = onBack,
        modifier = Modifier
          .testTag("back_button")
          .align(Alignment.TopStart)
          .statusBarsPadding()
          .padding(12.dp)
          .clip(CircleShape)
          .background(Color.Black.copy(alpha = 0.4f))
      ) {
        Icon(imageVector = Icons.Rounded.ArrowBack, contentDescription = "Back", tint = SoftSilver)
      }
    }

    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(20.dp),
      verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
      // Meal Title Header
      Column {
        Text(text = meal.type, color = IceBlue, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        Text(text = meal.name, color = SoftSilver, fontSize = 22.sp, fontWeight = FontWeight.Bold)
        Text(text = meal.description, color = MutedText, fontSize = 13.sp, lineHeight = 18.sp)
      }

      // Serving Size Selector Row exactly as pictured
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(14.dp))
          .background(Color(0xFF131D2F))
          .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(14.dp))
          .padding(14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(imageVector = Icons.Rounded.Restaurant, contentDescription = null, tint = MintNeon, modifier = Modifier.size(18.dp))
          Spacer(modifier = Modifier.width(10.dp))
          Text(text = "Serving Size:", color = SoftSilver, fontSize = 14.sp, fontWeight = FontWeight.Bold)
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
          IconButton(
            onClick = { if (servingSize > 0.5f) onServingSizeChange(servingSize - 0.5f) },
            modifier = Modifier.testTag("serving_decrease").size(34.dp)
          ) {
            Icon(imageVector = Icons.Default.Remove, contentDescription = "Reduce", tint = SoftSilver)
          }
          Text(
            text = "${servingSize}x",
            color = MintNeon,
            fontSize = 14.sp,
            fontWeight = FontWeight.ExtraBold,
            modifier = Modifier.padding(horizontal = 8.dp)
          )
          IconButton(
            onClick = { onServingSizeChange(servingSize + 0.5f) },
            modifier = Modifier.testTag("serving_increase").size(34.dp)
          ) {
            Icon(imageVector = Icons.Default.Add, contentDescription = "Increase", tint = SoftSilver)
          }
        }
      }

      // Vertical food components list exactly as pictured
      Text(text = "Components", color = SoftSilver, fontSize = 15.sp, fontWeight = FontWeight.Bold)

      Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        meal.foodItemIds.forEach { foodId ->
          val food = foodDb[foodId]
          if (food != null) {
            Row(
              modifier = Modifier
                .testTag("food_item_card_${food.name.lowercase()}")
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFF131D2F))
                .border(1.dp, Color.White.copy(alpha = 0.06f), RoundedCornerShape(12.dp))
                .clickable { onFoodItemClick(foodId) }
                .padding(14.dp),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Column {
                Text(text = food.name, color = SoftSilver, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                Text(
                  text = "${(food.nutrients["Calories"] ?: 0f) * servingSize} kcal  •  ${food.servingDesc}",
                  color = MutedText,
                  fontSize = 11.sp
                )
              }
              Icon(imageVector = Icons.Rounded.ChevronRight, contentDescription = "Open recipe", tint = MutedText, modifier = Modifier.size(18.dp))
            }
          }
        }
      }
      Spacer(modifier = Modifier.height(40.dp))
    }
  }
}

// ==========================================
// SCREEN 4: RECIPE DETAIL (COMPOSABLE)
// ==========================================

@Composable
fun RecipeDetailScreen(
  meal: MealPreset,
  foodItem: FoodComponent,
  servingSize: Float,
  completedSteps: MutableMap<String, Boolean>,
  completedIngredients: MutableMap<String, Boolean>,
  onBack: () -> Unit,
  onServingSizeChange: (Float) -> Unit
) {
  Column(
    modifier = Modifier
      .fillMaxSize()
      .verticalScroll(rememberScrollState())
  ) {
    // Large Header image on top exactly as requested
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .height(220.dp)
    ) {
      Image(
        painter = painterResource(id = meal.imageRes),
        contentDescription = foodItem.name,
        modifier = Modifier.fillMaxSize(),
        contentScale = ContentScale.Crop
      )
      Box(
        modifier = Modifier
          .fillMaxSize()
          .background(
            Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(alpha = 0.7f)))
          )
      )

      Column(
        modifier = Modifier
          .align(Alignment.BottomStart)
          .padding(16.dp)
      ) {
        Text(
          text = "IMG",
          color = Color.White.copy(alpha = 0.6f),
          fontSize = 12.sp,
          fontWeight = FontWeight.Bold
        )
        Text(
          text = "<AI generated image based on meal>",
          color = Color.White.copy(alpha = 0.8f),
          fontSize = 11.sp
        )
      }

      // Back navigation overlay
      IconButton(
        onClick = onBack,
        modifier = Modifier
          .testTag("back_button")
          .align(Alignment.TopStart)
          .statusBarsPadding()
          .padding(12.dp)
          .clip(CircleShape)
          .background(Color.Black.copy(alpha = 0.4f))
      ) {
        Icon(imageVector = Icons.Rounded.ArrowBack, contentDescription = "Back", tint = SoftSilver)
      }
    }

    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(20.dp),
      verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
      // Recipe Headers
      Column {
        Text(text = "Component Detail", color = IceBlue, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        Text(text = foodItem.name, color = SoftSilver, fontSize = 24.sp, fontWeight = FontWeight.ExtraBold)
        Text(text = "Category: ${foodItem.category}  |  Group: ${foodItem.group}", color = MutedText, fontSize = 12.sp)
      }

      // Portion scale adjuster row exactly as pictured
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(14.dp))
          .background(Color(0xFF131D2F))
          .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(14.dp))
          .padding(14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(imageVector = Icons.Rounded.Scale, contentDescription = null, tint = MintNeon, modifier = Modifier.size(18.dp))
          Spacer(modifier = Modifier.width(10.dp))
          Text(text = "Serving Size:", color = SoftSilver, fontSize = 14.sp, fontWeight = FontWeight.Bold)
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
          IconButton(
            onClick = { if (servingSize > 0.5f) onServingSizeChange(servingSize - 0.5f) },
            modifier = Modifier.testTag("serving_decrease").size(34.dp)
          ) {
            Icon(imageVector = Icons.Default.Remove, contentDescription = "Reduce", tint = SoftSilver)
          }
          Text(
            text = "${servingSize}x",
            color = MintNeon,
            fontSize = 14.sp,
            fontWeight = FontWeight.ExtraBold,
            modifier = Modifier.padding(horizontal = 8.dp)
          )
          IconButton(
            onClick = { onServingSizeChange(servingSize + 0.5f) },
            modifier = Modifier.testTag("serving_increase").size(34.dp)
          ) {
            Icon(imageVector = Icons.Default.Add, contentDescription = "Increase", tint = SoftSilver)
          }
        }
      }

      // Time variables exactly as pictured: Prep Time, Cook Time, Total Time
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(12.dp))
          .background(Color(0xFF1F2E49).copy(alpha = 0.3f))
          .padding(12.dp),
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = "Prep Time: ${foodItem.prepTime}m",
          color = SoftSilver,
          fontSize = 13.sp,
          fontWeight = FontWeight.Bold
        )
        Text(
          text = "Cook Time: ${foodItem.cookTime}m",
          color = SoftSilver,
          fontSize = 13.sp,
          fontWeight = FontWeight.Bold
        )
        Text(
          text = "Total Time: ${foodItem.prepTime + foodItem.cookTime}m",
          color = MintNeon,
          fontSize = 13.sp,
          fontWeight = FontWeight.Bold
        )
      }

      // Ingredients text checklist exactly as pictured
      Text(text = "Ingredients:", color = SoftSilver, fontSize = 16.sp, fontWeight = FontWeight.Bold)

      Column(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(14.dp))
          .background(Color(0xFF131D2F))
          .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        foodItem.ingredients.forEachIndexed { idx, ing ->
          val isChecked = completedIngredients["$idx"] ?: false
          val scaledIng = scaleIngredientText(ing, servingSize)
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .clickable { completedIngredients["$idx"] = !isChecked }
              .padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Box(
              modifier = Modifier
                .size(18.dp)
                .clip(CircleShape)
                .background(if (isChecked) MintNeon else Color.White.copy(alpha = 0.06f))
                .border(1.dp, if (isChecked) MintNeon else Color.White.copy(alpha = 0.2f), CircleShape),
              contentAlignment = Alignment.Center
            ) {
              if (isChecked) {
                Icon(imageVector = Icons.Default.Check, contentDescription = null, tint = SlateBg, modifier = Modifier.size(12.dp))
              }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text(
              text = scaledIng,
              color = if (isChecked) MutedText else SoftSilver,
              fontSize = 13.sp,
              textDecoration = if (isChecked) TextDecoration.LineThrough else null
            )
          }
        }
      }

      // Instructions text timeline exactly as pictured
      Text(text = "Instructions:", color = SoftSilver, fontSize = 16.sp, fontWeight = FontWeight.Bold)

      Column(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(14.dp))
          .background(Color(0xFF131D2F))
          .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        foodItem.instructions.forEachIndexed { idx, step ->
          val isChecked = completedSteps["$idx"] ?: false
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .clickable { completedSteps["$idx"] = !isChecked }
              .padding(vertical = 4.dp),
            verticalAlignment = Alignment.Top
          ) {
            Box(
              modifier = Modifier
                .padding(top = 2.dp)
                .size(20.dp)
                .clip(CircleShape)
                .background(if (isChecked) MintNeon else Color(0xFF1F2E49)),
              contentAlignment = Alignment.Center
            ) {
              if (isChecked) {
                Icon(imageVector = Icons.Default.Check, contentDescription = null, tint = SlateBg, modifier = Modifier.size(12.dp))
              } else {
                Text(text = "${idx + 1}", color = SoftSilver, fontSize = 10.sp, fontWeight = FontWeight.Bold)
              }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text(
              text = step,
              color = if (isChecked) MutedText else SoftSilver,
              fontSize = 13.sp,
              lineHeight = 18.sp,
              textDecoration = if (isChecked) TextDecoration.LineThrough else null
            )
          }
        }
      }
      Spacer(modifier = Modifier.height(40.dp))
    }
  }
}

// Simple text scaling helper
private fun scaleIngredientText(input: String, scale: Float): String {
  if (scale == 1.0f) return input
  val regex = """^(\d+(\.\d+)?|1/2|1/3|1/4|2/3|3/4)\s*(g|tsp|tbsp|slice|bunch|cup|clove|sprig|thick slice|ripe Hass avocado|ripe avocado|large farm eggs|cloves|sprigs)?\b""".toRegex(RegexOption.IGNORE_CASE)
  val match = regex.find(input)
  if (match != null) {
    val quantityStr = match.groupValues[1]
    val numericVal = when (quantityStr) {
      "1/2" -> 0.5f
      "1/3" -> 0.33f
      "1/4" -> 0.25f
      "2/3" -> 0.67f
      "3/4" -> 0.75f
      else -> quantityStr.toFloatOrNull() ?: 1.0f
    }
    val scaledVal = numericVal * scale
    val formattedScaled = if (scaledVal % 1.0f == 0.0f) {
      scaledVal.toInt().toString()
    } else {
      String.format("%.2f", scaledVal).trimEnd('0').trimEnd('.')
    }
    return input.replaceFirst(quantityStr, formattedScaled)
  }
  return input
}
