package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.nyndialer.domain.Contact
import com.example.nyndialer.domain.T9Engine
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
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("nyndialer", appName)
  }

  @Test
  fun `t9 engine matches name correctly`() {
    val contacts = listOf(
      Contact(id = "1", name = "Mom", phoneNumber = "555-0199"),
      Contact(id = "2", name = "Alex Rivera", phoneNumber = "555-0123")
    )
    // 666 -> M O M
    val momResults = T9Engine.search(contacts, "666")
    assertEquals(1, momResults.size)
    assertEquals("Mom", momResults[0].contact.name)

    // 253 -> A L E
    val alexResults = T9Engine.search(contacts, "253")
    assertEquals(1, alexResults.size)
    assertEquals("Alex Rivera", alexResults[0].contact.name)
  }
}
