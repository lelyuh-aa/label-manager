package com.leliukh.labelmanager.features.profile.presentation.viewmodel

import com.leliukh.labelmanager.core.session.domain.repository.FakeSessionRepository
import com.leliukh.labelmanager.core.session.domain.repository.testSession
import com.leliukh.labelmanager.core.settings.domain.models.ThemeMode
import com.leliukh.labelmanager.core.settings.domain.repository.FakeSettingsRepository
import com.leliukh.labelmanager.features.auth.domain.interactor.AuthInteractor
import com.leliukh.labelmanager.features.auth.domain.repository.FakeAuthRepository
import com.leliukh.labelmanager.features.label.domain.interactor.LabelInteractor
import com.leliukh.labelmanager.features.label.domain.models.CachedLabel
import com.leliukh.labelmanager.features.label.domain.repository.FakeLabelRepository
import com.leliukh.labelmanager.features.label.domain.repository.testLabel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ProfileViewModelTest {

    private val sessions = FakeSessionRepository(testSession())
    private val labels = FakeLabelRepository().apply {
        cache.value = mapOf("label-1" to CachedLabel(testLabel(), now))
    }
    private val settings = FakeSettingsRepository()
    private val viewModel by lazy {
        ProfileViewModel(
            sessionRepository = sessions,
            labelInteractor = LabelInteractor(sessions, labels),
            settingsRepository = settings,
            authInteractor = AuthInteractor(FakeAuthRepository(), sessions, labels),
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
    fun `the account and the label are shown`() = runTest {
        val state = stateOf(viewModel)

        assertEquals("manager@rheinvalley.fm", state.email)
        assertEquals("Rhein Valley Records", state.labelName)
        assertEquals(16, state.artistCount)
    }

    @Test
    fun `the chosen palette is saved`() = runTest {
        viewModel.handleUiAction(ProfileViewAction.ThemeChosen(ThemeMode.Dark))

        assertEquals(ThemeMode.Dark, settings.settings.value.themeMode)
    }

    @Test
    fun `signing out asks first and then clears the session and the label`() = runTest {
        viewModel.handleUiAction(ProfileViewAction.SignOutClicked)
        assertTrue(stateOf(viewModel).isSignOutConfirmOpen)

        viewModel.handleUiAction(ProfileViewAction.ConfirmSignOut)

        assertNull(sessions.state.value)
        assertTrue(labels.cache.value.isEmpty())
    }

    private fun TestScope.stateOf(viewModel: ProfileViewModel): ProfileViewState {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.viewState.collect {}
        }
        return viewModel.viewState.value
    }
}
