package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
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
    assertEquals("Klinik Romana", appName)
  }

  @Test
  fun `verify clinic repository initial state and workflow`() {
    val repository = com.example.data.ClinicRepository.getInstance()
    val state = repository.state.value
    // Verify pre-populated queues across 5 roles
    assertTrue(state.queue.isNotEmpty())
    assertTrue(state.medicines.isNotEmpty())
    assertTrue(state.opnameRecords.isNotEmpty())
  }
}
