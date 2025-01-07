package com.gravatar.quickeditor.ui.alttext

import app.cash.turbine.test
import com.gravatar.quickeditor.data.repository.AvatarRepository
import com.gravatar.quickeditor.ui.CoroutineTestRule
import com.gravatar.services.GravatarResult
import io.mockk.coEvery
import io.mockk.mockk
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class AltTextViewModelTest {
    private val testDispatcher = StandardTestDispatcher()

    @get:Rule
    var coroutineTestRule = CoroutineTestRule(testDispatcher)

    private lateinit var viewModel: AltTextViewModel

    private val email = "testEmail"
    private val avatarId = "testAvatarId"
    private val altText = "Alternative Text"
    private val avatarUrl = "https://gravatar.com/avatar/test"
    private val avatarRepository = mockk<AvatarRepository>()

    @Before
    fun setUp() {
        viewModel = AltTextViewModel(
            email = email,
            avatarId = avatarId,
            altText = altText,
            avatarUrl = avatarUrl,
            avatarRepository = avatarRepository,
        )
    }

    @Test
    fun `given some initial values when view model is initialized then uiState is correct`() = runTest {
        viewModel.uiState.test {
            val altTextUiState = AltTextUiState(
                avatarUrl = avatarUrl,
                isSaveButtonEnabled = false,
                isUpdating = false,
                altText = altText,
            )

            assertEquals(altTextUiState, awaitItem())
        }
    }

    @Test
    fun `given an alt text change event when onEvent is called then uiState is updated with new alt text`() = runTest {
        val newAltText = "New Alternative Text"
        viewModel.onEvent(AltTextEvent.AvatarAltTextChange(newAltText))

        viewModel.uiState.test {
            expectMostRecentItem()

            val altTextUiState = AltTextUiState(
                avatarUrl = avatarUrl,
                isSaveButtonEnabled = true,
                isUpdating = false,
                altText = newAltText,
            )

            assertEquals(altTextUiState, awaitItem())
        }
    }

    @Test
    fun `given an alt text save tapped event when alt text is successfully updated then uiState is updated`() =
        runTest {
            val newAltText = "New Alternative Text"
            coEvery {
                avatarRepository.updateAvatar(email = any(), avatarId = avatarId, rating = null, altText = newAltText)
            } returns GravatarResult.Success(mockk())

            viewModel.onEvent(AltTextEvent.AvatarAltTextChange(newAltText))

            viewModel.onEvent(AltTextEvent.AvatarAltTextSaveTapped)

            viewModel.uiState.test {
                skipItems(2)

                var altTextUiState = AltTextUiState(
                    avatarUrl = avatarUrl,
                    isSaveButtonEnabled = false,
                    isUpdating = true,
                    altText = newAltText,
                )

                assertEquals(altTextUiState, awaitItem())

                altTextUiState = altTextUiState.copy(
                    isSaveButtonEnabled = true,
                    isUpdating = false,
                )

                assertEquals(altTextUiState, awaitItem())
            }

            viewModel.actions.test {
                assertEquals(AltTextAction.AltTextUpdated, awaitItem())
            }
        }

    @Test
    fun `given an alt text save tapped event when alt text update fails then uiState is updated`() = runTest {
        val newAltText = "New Alternative Text"
        coEvery {
            avatarRepository.updateAvatar(email = any(), avatarId = avatarId, rating = null, altText = newAltText)
        } returns GravatarResult.Failure(mockk())

        viewModel.onEvent(AltTextEvent.AvatarAltTextChange(newAltText))

        viewModel.onEvent(AltTextEvent.AvatarAltTextSaveTapped)

        viewModel.uiState.test {
            skipItems(2)

            var altTextUiState = AltTextUiState(
                avatarUrl = avatarUrl,
                isSaveButtonEnabled = false,
                isUpdating = true,
                altText = newAltText,
            )

            assertEquals(altTextUiState, awaitItem())

            altTextUiState = altTextUiState.copy(
                isSaveButtonEnabled = true,
                isUpdating = false,
            )

            assertEquals(altTextUiState, awaitItem())
        }

        viewModel.actions.test {
            assertEquals(AltTextAction.AltTextUpdateFailed, awaitItem())
        }
    }
}
