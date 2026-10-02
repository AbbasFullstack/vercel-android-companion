package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.ui.components.formatRelativeTime
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    @Test
    fun `read app name from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Vercel Dashboard", appName)
    }

    @Test
    fun `formatRelativeTime formats properly`() {
        assertEquals("Never", formatRelativeTime(0L))
        val justNow = System.currentTimeMillis() - 10_000
        assertEquals("just now", formatRelativeTime(justNow))
        val hoursAgo = System.currentTimeMillis() - 7_200_000
        assertTrue(formatRelativeTime(hoursAgo).contains("h ago"))
    }
}
