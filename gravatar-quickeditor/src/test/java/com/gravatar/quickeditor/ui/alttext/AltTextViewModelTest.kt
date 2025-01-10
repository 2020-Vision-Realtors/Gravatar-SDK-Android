package com.gravatar.quickeditor.ui.alttext

import app.cash.turbine.test
import com.gravatar.quickeditor.createAvatar
import com.gravatar.quickeditor.data.repository.AvatarRepository
import com.gravatar.quickeditor.ui.CoroutineTestRule
import com.gravatar.restapi.models.Avatar
import com.gravatar.services.GravatarResult
import io.mockk.coEvery
import io.mockk.mockk
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import java.net.URI

class AltTextViewModelTest {
    private val testDispatcher = StandardTestDispatcher()

    @get:Rule
    var coroutineTestRule = CoroutineTestRule(testDispatcher)

    private lateinit var viewModel: AltTextViewModel

    private val email = "testEmail"
    private val avatarId = "testAvatarId"
    private val altText = "Alternative Text"
    private val avatarUrl = URI("https://gravatar.com/avatar/test")
    private val avatarRepository = mockk<AvatarRepository>()
    private val avatarsSharedFlow = mockk<SharedFlow<List<Avatar>>>()

    @Test
    fun `given an avatar that can't be loaded when view model is initialized then  AvatarCantBeLoaded is sent`() =
        runTest {
            coEvery { avatarRepository.getAvatars(any()) } returns avatarsSharedFlow
            coEvery { avatarsSharedFlow.replayCache } returns listOf(emptyList())

            viewModel = AltTextViewModel(email, avatarId, avatarRepository)

            viewModel.actions.test {
                assertEquals(AltTextAction.AvatarCantBeLoaded, awaitItem())
            }
        }

    @Test
    fun `given some initial values when view model is initialized then uiState is correct`() = runTest {
        initWithAvatarStorage(createAvatar(id = avatarId, url = avatarUrl, altText = altText))

        viewModel.uiState.test {
            skipItems(1) // Skipping the initial value

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
        initWithAvatarStorage(createAvatar(id = avatarId, url = avatarUrl, altText = altText))

        val newAltText = "New Alternative Text"
        viewModel.onEvent(AltTextEvent.AvatarAltTextChange(newAltText))

        viewModel.uiState.test {
            skipItems(2) // Skipping the initial value and the value set in the init block

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
            initWithAvatarStorage(createAvatar(id = avatarId, url = avatarUrl, altText = altText))

            val newAltText = "New Alternative Text"
            coEvery {
                avatarRepository.updateAvatar(email = any(), avatarId = avatarId, rating = null, altText = newAltText)
            } returns GravatarResult.Success(mockk())

            viewModel.onEvent(AltTextEvent.AvatarAltTextChange(newAltText))

            viewModel.onEvent(AltTextEvent.AvatarAltTextSaveTapped)

            viewModel.uiState.test {
                skipItems(3) // Skipping the following values: initial, init block and alt text change event

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
        initWithAvatarStorage(createAvatar(id = avatarId, url = avatarUrl, altText = altText))

        val newAltText = "New Alternative Text"
        coEvery {
            avatarRepository.updateAvatar(email = any(), avatarId = avatarId, rating = null, altText = newAltText)
        } returns GravatarResult.Failure(mockk())

        viewModel.onEvent(AltTextEvent.AvatarAltTextChange(newAltText))

        viewModel.onEvent(AltTextEvent.AvatarAltTextSaveTapped)

        viewModel.uiState.test {
            skipItems(3) // Skipping the following values: initial, init block and alt text change event

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

    private fun initWithAvatarStorage(avatar: Avatar) {
        initWithAvatarStorage(listOf(avatar))
    }

    private fun initWithAvatarStorage(avatars: List<Avatar>) {
        coEvery { avatarRepository.getAvatars(any()) } returns avatarsSharedFlow
        coEvery { avatarsSharedFlow.replayCache } returns listOf(avatars)

        viewModel = AltTextViewModel(email, avatarId, avatarRepository)
    }
}
