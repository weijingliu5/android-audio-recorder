package com.innosage.androidagentictemplate

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.GrantPermissionRule
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import android.Manifest

@RunWith(AndroidJUnit4::class)
class AppLaunchTest {

    @get:Rule
    val activityRule = ActivityScenarioRule(MainActivity::class.java)

    @get:Rule
    val grantPermissionRule: GrantPermissionRule = GrantPermissionRule.grant(
        android.Manifest.permission.RECORD_AUDIO,
        android.Manifest.permission.POST_NOTIFICATIONS
    )

    @Test
    fun testAppLaunchAndStartRecording() {
        // 1. Verify that the initial status text is displayed
        onView(withId(R.id.statusText))
            .check(matches(isDisplayed()))
            .check(matches(withText("Ready to record")))

        // 2. Click the Start Recording button
        onView(withId(R.id.recordButton))
            .check(matches(isDisplayed()))
            .perform(click())

        // 3. Verify that the status text updates correctly
        onView(withId(R.id.statusText))
            .check(matches(withText("Service Started: Recording 24/7")))
    }
}
