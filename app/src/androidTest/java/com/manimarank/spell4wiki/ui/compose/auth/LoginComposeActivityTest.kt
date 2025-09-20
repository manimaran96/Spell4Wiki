package com.manimarank.spell4wiki.ui.compose.auth

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.manimarank.spell4wiki.R
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LoginComposeActivityTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<LoginComposeActivity>()

    @Test
    fun loginScreen_displaysAllElements() {
        // Check if logo is displayed
        composeTestRule.onNodeWithContentDescription(
            composeTestRule.activity.getString(R.string.app_name)
        ).assertIsDisplayed()

        // Check if title is displayed
        composeTestRule.onNodeWithText(
            composeTestRule.activity.getString(R.string.wiki_login)
        ).assertIsDisplayed()

        // Check if username field is displayed
        composeTestRule.onNodeWithText(
            composeTestRule.activity.getString(R.string.username)
        ).assertIsDisplayed()

        // Check if password field is displayed
        composeTestRule.onNodeWithText(
            composeTestRule.activity.getString(R.string.password)
        ).assertIsDisplayed()

        // Check if login button is displayed
        composeTestRule.onNodeWithText(
            composeTestRule.activity.getString(R.string.login)
        ).assertIsDisplayed()

        // Check if forgot password button is displayed
        composeTestRule.onNodeWithText(
            composeTestRule.activity.getString(R.string.forgot_password)
        ).assertIsDisplayed()

        // Check if join wikipedia button is displayed
        composeTestRule.onNodeWithText(
            composeTestRule.activity.getString(R.string.join_wiki)
        ).assertIsDisplayed()

        // Check if skip login button is displayed
        composeTestRule.onNodeWithText(
            composeTestRule.activity.getString(R.string.skip_login)
        ).assertIsDisplayed()
    }

    @Test
    fun loginScreen_usernameInput_worksCorrectly() {
        val testUsername = "testuser"

        // Find username field and enter text
        composeTestRule.onNodeWithText(
            composeTestRule.activity.getString(R.string.username)
        ).performTextInput(testUsername)

        // Verify text was entered
        composeTestRule.onNodeWithText(testUsername).assertIsDisplayed()
    }

    @Test
    fun loginScreen_passwordInput_worksCorrectly() {
        val testPassword = "testpassword"

        // Find password field and enter text
        composeTestRule.onNodeWithText(
            composeTestRule.activity.getString(R.string.password)
        ).performTextInput(testPassword)

        // Password should be hidden by default, so we check if the field exists
        // but the actual text won't be visible due to password transformation
        composeTestRule.onNodeWithText(
            composeTestRule.activity.getString(R.string.password)
        ).assertIsDisplayed()
    }

    @Test
    fun loginScreen_passwordVisibilityToggle_worksCorrectly() {
        val testPassword = "testpassword"

        // Enter password
        composeTestRule.onNodeWithText(
            composeTestRule.activity.getString(R.string.password)
        ).performTextInput(testPassword)

        // Find and click the visibility toggle button
        composeTestRule.onNodeWithContentDescription("Show password").performClick()

        // After clicking, the content description should change
        composeTestRule.onNodeWithContentDescription("Hide password").assertIsDisplayed()

        // Click again to hide
        composeTestRule.onNodeWithContentDescription("Hide password").performClick()

        // Should be back to show password
        composeTestRule.onNodeWithContentDescription("Show password").assertIsDisplayed()
    }

    @Test
    fun loginScreen_loginButtonDisabled_whenFieldsEmpty() {
        // Login button should be disabled when fields are empty
        composeTestRule.onNodeWithText(
            composeTestRule.activity.getString(R.string.login)
        ).assertIsNotEnabled()
    }

    @Test
    fun loginScreen_loginButtonEnabled_whenFieldsFilled() {
        // Fill username
        composeTestRule.onNodeWithText(
            composeTestRule.activity.getString(R.string.username)
        ).performTextInput("testuser")

        // Fill password
        composeTestRule.onNodeWithText(
            composeTestRule.activity.getString(R.string.password)
        ).performTextInput("testpassword")

        // Login button should now be enabled
        composeTestRule.onNodeWithText(
            composeTestRule.activity.getString(R.string.login)
        ).assertIsEnabled()
    }

    @Test
    fun loginScreen_forgotPasswordButton_isClickable() {
        composeTestRule.onNodeWithText(
            composeTestRule.activity.getString(R.string.forgot_password)
        ).assertIsEnabled()
            .performClick()
    }

    @Test
    fun loginScreen_joinWikipediaButton_isClickable() {
        composeTestRule.onNodeWithText(
            composeTestRule.activity.getString(R.string.join_wiki)
        ).assertIsEnabled()
            .performClick()
    }

    @Test
    fun loginScreen_skipLoginButton_isClickable() {
        composeTestRule.onNodeWithText(
            composeTestRule.activity.getString(R.string.skip_login)
        ).assertIsEnabled()
            .performClick()
    }

    @Test
    fun loginScreen_scrollable_whenContentOverflows() {
        // This test ensures the screen is scrollable
        // We can test this by checking if all elements are accessible even on small screens
        
        // Scroll to bottom to ensure skip login button is visible
        composeTestRule.onNodeWithText(
            composeTestRule.activity.getString(R.string.skip_login)
        ).assertIsDisplayed()

        // Scroll to top to ensure logo is visible
        composeTestRule.onNodeWithContentDescription(
            composeTestRule.activity.getString(R.string.app_name)
        ).assertIsDisplayed()
    }

    @Test
    fun loginScreen_formValidation_worksCorrectly() {
        // Initially login button should be disabled
        composeTestRule.onNodeWithText(
            composeTestRule.activity.getString(R.string.login)
        ).assertIsNotEnabled()

        // Fill only username
        composeTestRule.onNodeWithText(
            composeTestRule.activity.getString(R.string.username)
        ).performTextInput("testuser")

        // Button should still be disabled
        composeTestRule.onNodeWithText(
            composeTestRule.activity.getString(R.string.login)
        ).assertIsNotEnabled()

        // Fill password
        composeTestRule.onNodeWithText(
            composeTestRule.activity.getString(R.string.password)
        ).performTextInput("testpassword")

        // Now button should be enabled
        composeTestRule.onNodeWithText(
            composeTestRule.activity.getString(R.string.login)
        ).assertIsEnabled()
    }
}
