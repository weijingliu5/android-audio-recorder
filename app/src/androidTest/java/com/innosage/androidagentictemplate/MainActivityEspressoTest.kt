package com.innosage.androidagentictemplate

import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.rule.GrantPermissionRule
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MainActivityEspressoTest {

    @get:Rule
    val activityRule = ActivityScenarioRule(MainActivity::class.java)

    @get:Rule
    val grantPermissionRule: GrantPermissionRule = GrantPermissionRule.grant(
        android.Manifest.permission.RECORD_AUDIO
    )

    @Test
    fun testInitialState() {
        onView(withId(R.id.statusText)).check(matches(withText("Ready")))
        onView(withId(R.id.recordButton)).check(matches(withText("Start Recording")))
        onView(withId(R.id.playButton)).check(matches(withText("Play Last")))
    }

    @Test
    fun testRecordingStateToggle() {
        // Start Recording
        onView(withId(R.id.recordButton)).perform(click())
        onView(withId(R.id.statusText)).check(matches(withText("Recording...")))
        onView(withId(R.id.recordButton)).check(matches(withText("Stop Recording")))

        // Stop Recording
        onView(withId(R.id.recordButton)).perform(click())
        onView(withId(R.id.statusText)).check(matches(withText("Ready")))
        onView(withId(R.id.recordButton)).check(matches(withText("Start Recording")))
    }

    @Test
    fun testPlaybackStateToggle() {
        // Record something first
        onView(withId(R.id.recordButton)).perform(click())
        onView(withId(R.id.recordButton)).perform(click())

        // Start Playback
        onView(withId(R.id.playButton)).perform(click())
        onView(withId(R.id.statusText)).check(matches(withText("Playing...")))
        onView(withId(R.id.playButton)).check(matches(withText("Stop Playback")))

        // Stop Playback
        onView(withId(R.id.playButton)).perform(click())
        onView(withId(R.id.statusText)).check(matches(withText("Ready")))
        onView(withId(R.id.playButton)).check(matches(withText("Play Last")))
    }
}
