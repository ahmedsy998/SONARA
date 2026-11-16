package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("SONARA", appName)
  }

  @Test
  fun `arabic locale resolves arabic resources and rtl direction`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val arCtx = com.example.ui.util.localizedForHelper(context, com.example.model.AppLanguage.ARABIC)
    assertEquals("الرئيسية", arCtx.getString(R.string.nav_home))
    assertEquals(15, com.example.model.SonaraThemeId.entries.size)
  }
}
