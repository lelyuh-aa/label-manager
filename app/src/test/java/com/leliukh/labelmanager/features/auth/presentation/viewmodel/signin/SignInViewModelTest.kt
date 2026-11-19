package com.leliukh.labelmanager.features.auth.presentation.viewmodel.signin

import androidx.lifecycle.SavedStateHandle
import com.leliukh.labelmanager.R
import com.leliukh.labelmanager.contract.dto.error.ErrorCodeDto
import com.leliukh.labelmanager.core.network.domain.ApiError
import com.leliukh.labelmanager.core.network.domain.ApiResult
import com.leliukh.labelmanager.core.session.domain.repository.FakeSessionRepository
import com.leliukh.labelmanager.features.auth.domain.interactor.AuthInteractor
import com.leliukh.labelmanager.features.auth.domain.repository.FakeAuthRepository
import com.leliukh.labelmanager.features.label.domain.repository.FakeLabelRepository
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
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SignInViewModelTest {

    private val repository = FakeAuthRepository()
    private val interactor =
        AuthInteractor(repository, FakeSessionRepository(), FakeLabelRepository())
    private val savedState = SavedStateHandle()

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `an invalid email is flagged only after leaving the field`() {
        val viewModel = SignInViewModel(interactor, savedState)

        viewModel.handleUiAction(SignInViewAction.EmailChanged("manager@rheinvalley"))
        assertNull(viewModel.viewState.value.emailErrorRes)

        viewModel.handleUiAction(SignInViewAction.EmailFocusLost)
        assertEquals(R.string.auth_email_invalid, viewModel.viewState.value.emailErrorRes)

        viewModel.handleUiAction(SignInViewAction.EmailChanged("manager@rheinvalley.fm"))
        assertNull(viewModel.viewState.value.emailErrorRes)
    }

    @Test
    fun `a new email error waits until the field is left again`() {
        val viewModel = SignInViewModel(interactor, savedState)
        viewModel.handleUiAction(SignInViewAction.EmailChanged("manager@rheinvalley.fm"))
        viewModel.handleUiAction(SignInViewAction.EmailFocusLost)

        viewModel.handleUiAction(SignInViewAction.EmailChanged("manager@rheinvalley"))
        assertNull(viewModel.viewState.value.emailErrorRes)

        viewModel.handleUiAction(SignInViewAction.EmailFocusLost)
        assertEquals(R.string.auth_email_invalid, viewModel.viewState.value.emailErrorRes)
    }

    @Test
    fun `a successful sign-in clears the password and opens home`() = runTest {
        val viewModel = filledViewModel()

        viewModel.handleUiAction(SignInViewAction.Submit)

        assertEquals(listOf("manager@rheinvalley.fm" to "backstage2026"), repository.signIns)
        assertEquals("", viewModel.viewState.value.password)
        assertEquals(SignInViewEvent.OpenHome, viewModel.uiEventsFlow.first())
    }

    @Test
    fun `wrong credentials show the banner and keep the form`() {
        repository.result = ApiResult.Failure(
            ApiError.Server(ErrorCodeDto.INVALID_CREDENTIALS, "Invalid credentials", 401),
        )
        val viewModel = filledViewModel()

        viewModel.handleUiAction(SignInViewAction.Submit)

        val state = viewModel.viewState.value
        assertEquals(R.string.auth_error_credentials_title, state.message?.titleRes)
        assertEquals("backstage2026", state.password)
        assertFalse(state.isSubmitting)
        assertTrue(state.isSubmitEnabled)
    }

    @Test
    fun `the email survives process death, the password does not`() {
        filledViewModel()

        val restored = SignInViewModel(interactor, savedState)

        assertEquals("manager@rheinvalley.fm", restored.viewState.value.email)
        assertEquals("", restored.viewState.value.password)
        assertFalse(savedState.keys().any { savedState.get<Any>(it) == "backstage2026" })
    }

    private fun filledViewModel() = SignInViewModel(interactor, savedState).apply {
        handleUiAction(SignInViewAction.EmailChanged("manager@rheinvalley.fm"))
        handleUiAction(SignInViewAction.PasswordChanged("backstage2026"))
    }
}
