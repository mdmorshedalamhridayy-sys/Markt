package com.example

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import com.example.data.Product
import com.example.ui.ProductCard
import com.example.ui.theme.MyApplicationTheme
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = RobolectricDeviceQualifiers.Pixel8, sdk = [36])
class GreetingScreenshotTest {

  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun greeting_screenshot() {
    val sampleProduct = Product(
      name = " elite Photo Editor",
      description = "Full featured creative suite with smart presets, layers, and visual adjustments.",
      price = 4.99,
      category = "Regular",
      visualType = 0,
      popularity = 95,
      isNewArrival = false,
      downloadCount = 12500,
      ratingAvg = 4.8f
    )

    composeTestRule.setContent {
      MyApplicationTheme {
        ProductCard(
          product = sampleProduct,
          onClick = {},
          onAddToCart = {},
          isInCart = false
        )
      }
    }

    composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/greeting.png")
  }
}
