package com.leliukh.labelmanager.features.label.presentation.viewmodel.editlabel

import androidx.lifecycle.SavedStateHandle
import com.leliukh.labelmanager.core.session.domain.repository.FakeSessionRepository
import com.leliukh.labelmanager.core.session.domain.repository.testSession
import com.leliukh.labelmanager.features.label.domain.interactor.LabelInteractor
import com.leliukh.labelmanager.features.label.domain.models.CachedLabel
import com.leliukh.labelmanager.features.label.domain.repository.FakeLabelRepository
import com.leliukh.labelmanager.features.label.domain.repository.testLabel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class EditLabelViewModelTest {

    private val labels = FakeLabelRepository().apply {
        cache.value = mapOf("label-1" to CachedLabel(testLabel(), now))
    }
    private val viewModel by lazy {
        EditLabelViewModel(
            labelInteractor = LabelInteractor(FakeSessionRepository(testSession()), labels),
            savedStateHandle = SavedStateHandle(),
        )
    }

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `the form starts from the cached label with save off`() {
        val state = viewModel.viewState.value

        assertEquals("Rhein Valley Records", state.name)
        assertEquals("DE", state.countryCode)
        assertFalse(state.isSaveEnabled)
    }

    @Test
    fun `a one-letter name is rejected`() {
        viewModel.handleUiAction(EditLabelViewAction.NameChanged("R"))

        assertTrue(viewModel.viewState.value.isNameInvalid)
        assertFalse(viewModel.viewState.value.isSaveEnabled)
    }

    @Test
    fun `a new country is saved and the screen closes`() = runTest {
        viewModel.handleUiAction(EditLabelViewAction.CountryChosen("AT"))

        viewModel.handleUiAction(EditLabelViewAction.Save)

        assertEquals(listOf("Rhein Valley Records" to "AT"), labels.updates)
        assertEquals(EditLabelViewEvent.Close, viewModel.uiEventsFlow.first())
    }
}
