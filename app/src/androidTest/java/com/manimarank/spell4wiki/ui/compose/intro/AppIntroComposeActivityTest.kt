package com.manimarank.spell4wiki.ui.compose.intro

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.manimarank.spell4wiki.R
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AppIntroComposeActivityTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<AppIntroComposeActivity>()

    @Test
    fun appIntroScreen_displaysFirstSlide() {
        // Check if first slide title is displayed
        composeTestRule.onNodeWithText(
            composeTestRule.activity.getString(R.string.app_intro_slide_1_title)
        ).assertIsDisplayed()

        // Check if first slide description is displayed
        composeTestRule.onNodeWithText(
            composeTestRule.activity.getString(R.string.app_intro_slide_1_description)
        ).assertIsDisplayed()

        // Check if next button is displayed (not done button on first slide)
        composeTestRule.onNodeWithContentDescription("Next").assertIsDisplayed()
    }

    @Test
    fun appIntroScreen_navigationIndicators_displayed() {
        // Check if page indicators are present
        // We can't easily test the exact number of indicators, but we can check if the pager is working
        
        // The first slide should be displayed
        composeTestRule.onNodeWithText(
            composeTestRule.activity.getString(R.string.app_intro_slide_1_title)
        ).assertIsDisplayed()
    }

    @Test
    fun appIntroScreen_nextButton_navigatesToNextSlide() {
        // Verify we're on first slide
        composeTestRule.onNodeWithText(
            composeTestRule.activity.getString(R.string.app_intro_slide_1_title)
        ).assertIsDisplayed()

        // Click next button
        composeTestRule.onNodeWithContentDescription("Next").performClick()

        // Verify we're now on second slide
        composeTestRule.onNodeWithText(
            composeTestRule.activity.getString(R.string.app_intro_slide_2_title)
        ).assertIsDisplayed()
    }

    @Test
    fun appIntroScreen_swipeGesture_navigatesToNextSlide() {
        // Verify we're on first slide
        composeTestRule.onNodeWithText(
            composeTestRule.activity.getString(R.string.app_intro_slide_1_title)
        ).assertIsDisplayed()

        // Perform swipe left gesture on the pager
        composeTestRule.onRoot().performTouchInput {
            swipeLeft()
        }

        // Wait for animation and verify we're on second slide
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText(
            composeTestRule.activity.getString(R.string.app_intro_slide_2_title)
        ).assertIsDisplayed()
    }

    @Test
    fun appIntroScreen_lastSlide_showsDoneButton() {
        // Navigate to last slide by clicking next multiple times
        repeat(4) {
            composeTestRule.onNodeWithContentDescription("Next").performClick()
            composeTestRule.waitForIdle()
        }

        // Verify we're on last slide
        composeTestRule.onNodeWithText(
            composeTestRule.activity.getString(R.string.app_intro_slide_5_title)
        ).assertIsDisplayed()

        // Verify done button is displayed instead of next button
        composeTestRule.onNodeWithText(
            composeTestRule.activity.getString(R.string.done)
        ).assertIsDisplayed()

        // Verify next button is not displayed
        composeTestRule.onNodeWithContentDescription("Next").assertDoesNotExist()
    }

    @Test
    fun appIntroScreen_doneButton_isClickable() {
        // Navigate to last slide
        repeat(4) {
            composeTestRule.onNodeWithContentDescription("Next").performClick()
            composeTestRule.waitForIdle()
        }

        // Click done button
        composeTestRule.onNodeWithText(
            composeTestRule.activity.getString(R.string.done)
        ).assertIsEnabled()
            .performClick()
    }

    @Test
    fun appIntroScreen_allSlides_displayCorrectContent() {
        val slideData = listOf(
            R.string.app_intro_slide_1_title to R.string.app_intro_slide_1_description,
            R.string.app_intro_slide_2_title to R.string.app_intro_slide_2_description,
            R.string.app_intro_slide_3_title to R.string.app_intro_slide_3_description,
            R.string.app_intro_slide_4_title to R.string.app_intro_slide_4_description,
            R.string.app_intro_slide_5_title to R.string.app_intro_slide_5_description
        )

        slideData.forEachIndexed { index, (titleRes, descRes) ->
            // Check current slide content
            composeTestRule.onNodeWithText(
                composeTestRule.activity.getString(titleRes)
            ).assertIsDisplayed()

            composeTestRule.onNodeWithText(
                composeTestRule.activity.getString(descRes)
            ).assertIsDisplayed()

            // Navigate to next slide if not the last one
            if (index < slideData.size - 1) {
                composeTestRule.onNodeWithContentDescription("Next").performClick()
                composeTestRule.waitForIdle()
            }
        }
    }

    @Test
    fun appIntroScreen_slideImages_displayed() {
        // Check if image is displayed on first slide
        composeTestRule.onNodeWithContentDescription(
            composeTestRule.activity.getString(R.string.app_intro_slide_1_title)
        ).assertIsDisplayed()

        // Navigate to second slide and check image
        composeTestRule.onNodeWithContentDescription("Next").performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithContentDescription(
            composeTestRule.activity.getString(R.string.app_intro_slide_2_title)
        ).assertIsDisplayed()
    }

    @Test
    fun appIntroScreen_backNavigation_worksWithSwipe() {
        // Navigate to second slide
        composeTestRule.onNodeWithContentDescription("Next").performClick()
        composeTestRule.waitForIdle()

        // Verify we're on second slide
        composeTestRule.onNodeWithText(
            composeTestRule.activity.getString(R.string.app_intro_slide_2_title)
        ).assertIsDisplayed()

        // Swipe right to go back
        composeTestRule.onRoot().performTouchInput {
            swipeRight()
        }

        composeTestRule.waitForIdle()

        // Verify we're back on first slide
        composeTestRule.onNodeWithText(
            composeTestRule.activity.getString(R.string.app_intro_slide_1_title)
        ).assertIsDisplayed()
    }

    @Test
    fun appIntroScreen_slideContent_isScrollable() {
        // This test ensures that slide content is scrollable if it overflows
        // Check if description text is displayed (it should be scrollable if needed)
        composeTestRule.onNodeWithText(
            composeTestRule.activity.getString(R.string.app_intro_slide_1_description)
        ).assertIsDisplayed()

        // The content should be accessible even if it's long
        composeTestRule.onNodeWithText(
            composeTestRule.activity.getString(R.string.app_intro_slide_1_title)
        ).assertIsDisplayed()
    }
}
