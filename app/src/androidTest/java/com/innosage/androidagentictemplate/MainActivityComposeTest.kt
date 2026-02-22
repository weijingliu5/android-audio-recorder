package com.innosage.androidagentictemplate

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.GrantPermissionRule
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MainActivityComposeTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @get:Rule
    val grantPermissionRule: GrantPermissionRule = GrantPermissionRule.grant(
        android.Manifest.permission.RECORD_AUDIO
    )

    @Test
    fun testInitialState() {
        composeTestRule.onNodeWithText("Ready").assertExists()
        composeTestRule.onNodeWithText("Start Recording").assertExists()
        composeTestRule.onNodeWithText("Play Last").assertExists()
    }

    @Test
    fun testRecordingStateToggle() {
        composeTestRule.onNodeWithText("Start Recording").performClick()
        composeTestRule.onNodeWithText("Recording...").assertExists()
        composeTestRule.onNodeWithText("Stop Recording").assertExists()

        composeTestRule.onNodeWithText("Stop Recording").performClick()
        composeTestRule.onNodeWithText("Ready").assertExists()
        composeTestRule.onNodeWithText("Start Recording").assertExists()
    }

    @Test
    fun testPlaybackStateToggle() {
        // Record something first
        composeTestRule.onNodeWithText("Start Recording").performClick()
        composeTestRule.onNodeWithText("Stop Recording").performClick()

        // Start Playback
        composeTestRule.onNodeWithText("Play Last").performClick()
        composeTestRule.onNodeWithText("Playing...").assertExists()
        composeTestRule.onNodeWithText("Stop Playback").assertExists()

        // Stop Playback
        composeTestRule.onNodeWithText("Stop Playback").performClick()
        composeTestRule.onNodeWithText("Ready").assertExists()
        composeTestRule.onNodeWithText("Play Last").assertExists()
    }
}
