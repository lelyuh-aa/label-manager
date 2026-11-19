package com.leliukh.labelmanager.features.auth.presentation.viewmodel.signup

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
class SignUpViewModelTest {

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
    fun `password rules are ticked off as the password is typed`() {
        val viewModel = SignUpViewModel(interactor, savedState)

        viewModel.handleUiAction(SignUpViewAction.PasswordChanged("backstage"))

        val met = viewModel.viewState.value.passwordRules.map { it.isMet }
        assertEquals(listOf(true, true, false), met)
    }

    @Test
    fun `a mismatch is shown as soon as the repeat cannot match and blocks submit`() {
        val viewModel = filledViewModel(repeat = "backstage8")

        val state = viewModel.viewState.value
        assertEquals(R.string.auth_password_mismatch, state.repeatPasswordErrorRes)
        assertFalse(state.isSubmitEnabled)
    }

    @Test
    fun `an unfinished repeat is flagged only after leaving the field`() {
        val viewModel = filledViewModel(repeat = "back")
        assertNull(viewModel.viewState.value.repeatPasswordErrorRes)

        viewModel.handleUiAction(SignUpViewAction.RepeatPasswordFocusLost)
        assertEquals(R.string.auth_password_mismatch, viewModel.viewState.value.repeatPasswordErrorRes)
    }

    @Test
    fun `a valid form signs up, clears both passwords and opens the label`() = runTest {
        val viewModel = filledViewModel(repeat = "backstage7")
        assertTrue(viewModel.viewState.value.isSubmitEnabled)

        viewModel.handleUiAction(SignUpViewAction.Submit)

        assertEquals(listOf("manager@rheinvalley.fm" to "backstage7"), repository.signUps)
        assertEquals("", viewModel.viewState.value.password)
        assertEquals("", viewModel.viewState.value.repeatPassword)
        assertEquals(SignUpViewEvent.OpenLabelReady, viewModel.uiEventsFlow.first())
    }

    @Test
    fun `a taken email is shown on the field until the email changes`() {
        repository.result = ApiResult.Failure(
            ApiError.Server(ErrorCodeDto.EMAIL_TAKEN, "Email taken", 409),
        )
        val viewModel = filledViewModel(repeat = "backstage7")

        viewModel.handleUiAction(SignUpViewAction.Submit)
        assertEquals(R.string.auth_email_taken, viewModel.viewState.value.emailErrorRes)
        assertFalse(viewModel.viewState.value.isSubmitEnabled)

        viewModel.handleUiAction(SignUpViewAction.EmailChanged("producer@rheinvalley.fm"))
        assertNull(viewModel.viewState.value.emailErrorRes)
        assertTrue(viewModel.viewState.value.isSubmitEnabled)
    }

    private fun filledViewModel(repeat: String) = SignUpViewModel(interactor, savedState).apply {
        handleUiAction(SignUpViewAction.EmailChanged("manager@rheinvalley.fm"))
        handleUiAction(SignUpViewAction.PasswordChanged("backstage7"))
        handleUiAction(SignUpViewAction.RepeatPasswordChanged(repeat))
    }
}
