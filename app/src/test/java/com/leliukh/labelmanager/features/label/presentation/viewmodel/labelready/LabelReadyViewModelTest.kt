package com.leliukh.labelmanager.features.label.presentation.viewmodel.labelready

import com.leliukh.labelmanager.core.network.domain.ApiError
import com.leliukh.labelmanager.core.network.domain.ApiResult
import com.leliukh.labelmanager.features.label.domain.repository.FakeLabelRepository
import com.leliukh.labelmanager.features.label.domain.repository.testLabel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.util.Locale

@OptIn(ExperimentalCoroutinesApi::class)
class LabelReadyViewModelTest {

    private val defaultLocale = Locale.getDefault()

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        Locale.setDefault(Locale.US)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        Locale.setDefault(defaultLocale)
    }

    @Test
    fun `the new label is summarised by genre`() {
        val viewModel = LabelReadyViewModel(FakeLabelRepository(ApiResult.Success(label)))

        val state = viewModel.viewState.value
        assertFalse(state.isLoading)
        assertEquals("Rhein Valley Records", state.labelName)
        assertEquals("Germany", state.countryName)
        assertEquals(3_000_000, state.budget)
        assertEquals(listOf(4, 4, 4, 4), state.genres.map { it.count })
    }

    @Test
    fun `a failed load can be retried`() {
        val repository = FakeLabelRepository(ApiResult.Failure(ApiError.Network))
        val viewModel = LabelReadyViewModel(repository)
        assertTrue(viewModel.viewState.value.isError)

        repository.result = ApiResult.Success(label)
        viewModel.handleUiAction(LabelReadyViewAction.Retry)

        assertFalse(viewModel.viewState.value.isError)
        assertEquals(16, viewModel.viewState.value.artistCount)
    }

    private val label = testLabel()
}
