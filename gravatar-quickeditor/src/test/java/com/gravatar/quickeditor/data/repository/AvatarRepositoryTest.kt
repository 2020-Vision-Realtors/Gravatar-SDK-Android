package com.gravatar.quickeditor.data.repository

import android.net.Uri
import androidx.core.net.toFile
import com.gravatar.quickeditor.data.models.QuickEditorError
import com.gravatar.quickeditor.data.storage.AvatarStorage
import com.gravatar.quickeditor.data.storage.DataStoreTokenStorage
import com.gravatar.quickeditor.ui.CoroutineTestRule
import com.gravatar.quickeditor.ui.avatarpicker.EmailAvatars
import com.gravatar.restapi.models.Avatar
import com.gravatar.services.AvatarService
import com.gravatar.services.ErrorType
import com.gravatar.services.GravatarResult
import com.gravatar.types.Email
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.io.File
import java.net.URI

class AvatarRepositoryTest {
    private val testDispatcher = StandardTestDispatcher()

    @get:Rule
    var coroutinesTestRule = CoroutineTestRule(testDispatcher)

    private val avatarService = mockk<AvatarService>()
    private val tokenStorage = mockk<DataStoreTokenStorage>()
    private val avatarStorage = mockk<AvatarStorage>()

    private lateinit var avatarRepository: AvatarRepository

    private val email = Email("email")

    private companion object {
        const val DEFAULT_TOKEN = "token"
    }

    @Before
    fun setUp() {
        avatarRepository = AvatarRepository(
            avatarService = avatarService,
            tokenStorage = tokenStorage,
            avatarStorage = avatarStorage,
            dispatcher = testDispatcher,
        )
    }

    @Test
    fun `given email when token not found then TokenNotFound result`() = runTest {
        coEvery { tokenStorage.getToken(any()) } returns null

        val result = avatarRepository.getAvatars(email)

        assertEquals(GravatarResult.Failure<EmailAvatars, QuickEditorError>(QuickEditorError.TokenNotFound), result)
        coVerify(exactly = 0) { avatarStorage.storeAvatars(any(), any()) }
    }

    @Test
    fun `given token stored when get avatars fails then Failure result`() = runTest {
        coEvery { tokenStorage.getToken(any()) } returns DEFAULT_TOKEN
        coEvery { avatarService.retrieveCatching(any(), any()) } returns GravatarResult.Failure(ErrorType.Server)

        val result = avatarRepository.getAvatars(email)

        assertEquals(
            GravatarResult.Failure<List<Avatar>, QuickEditorError>(QuickEditorError.Request(ErrorType.Server)),
            result,
        )
        coVerify(exactly = 0) { avatarStorage.storeAvatars(any(), any()) }
    }

    @Test
    fun `given token stored when get avatars succeed then Success result`() = runTest {
        val imageId = "2"
        val avatar = createAvatar(imageId, isSelected = true)
        coEvery { tokenStorage.getToken(any()) } returns DEFAULT_TOKEN
        coEvery { avatarService.retrieveCatching(any(), any()) } returns GravatarResult.Success(listOf(avatar))
        coEvery { avatarStorage.storeAvatars(any(), any()) } returns Unit

        val result = avatarRepository.getAvatars(email)
        val expectedAvatars = listOf(avatar)

        assertEquals(
            GravatarResult.Success<List<Avatar>, QuickEditorError>(expectedAvatars),
            result,
        )
        coVerify { avatarStorage.storeAvatars(expectedAvatars, email) }
    }

    @Test
    fun `given email stored when token not found then TokenNotFound result`() = runTest {
        coEvery { tokenStorage.getToken(any()) } returns null

        val result = avatarRepository.selectAvatar(email, "avatarId")

        assertEquals(GravatarResult.Failure<String, QuickEditorError>(QuickEditorError.TokenNotFound), result)
        coVerify(exactly = 0) { avatarStorage.markAvatarAsSelected(any(), any()) }
    }

    @Test
    fun `given token stored when avatar selected fails then Failure result`() = runTest {
        val avatar = createAvatar("avatarId")
        initAvatarsFlowForEmail(email)
        coEvery {
            avatarService.setAvatarCatching(any(), any(), any())
        } returns GravatarResult.Failure(ErrorType.Unknown())
        coEvery { avatarStorage.markAvatarAsSelected(any(), any()) } returns Unit

        val result = avatarRepository.selectAvatar(email, avatar.imageId)

        assertEquals(
            GravatarResult.Failure<String, QuickEditorError>(QuickEditorError.Request(ErrorType.Unknown())),
            result,
        )
        coVerify(exactly = 0) { avatarStorage.markAvatarAsSelected(email, avatar.imageId) }
    }

    @Test
    fun `given token stored when avatar selected succeeds then Success result`() = runTest {
        coEvery { avatarService.setAvatarCatching(any(), any(), any()) } returns GravatarResult.Success(Unit)
        coEvery { avatarStorage.markAvatarAsSelected(email = email, avatarId = "avatarId") } returns Unit
        initAvatarsFlowForEmail(email)

        val result = avatarRepository.selectAvatar(email, "avatarId")

        assertEquals(GravatarResult.Success<Unit, QuickEditorError>(Unit), result)
        coVerify { avatarStorage.markAvatarAsSelected(email, "avatarId") }
    }

    @Test
    fun `given token not stored when avatar upload then Failure result`() = runTest {
        val uri = mockk<Uri>()
        coEvery { tokenStorage.getToken(any()) } returns null
        coEvery {
            avatarService.uploadCatching(any(), any(), any(), any())
        } returns GravatarResult.Success(createAvatar("1"))

        val result = avatarRepository.uploadAvatar(email, uri)

        assertEquals(GravatarResult.Failure<Unit, QuickEditorError>(QuickEditorError.TokenNotFound), result)
        coVerify(exactly = 0) { avatarStorage.addAvatar(any(), any()) }
    }

    @Test
    fun `given token stored when avatar upload succeeds then Success result`() = runTest {
        val avatar = createAvatar("2")
        mockkStatic("androidx.core.net.UriKt")
        val file = mockk<File>()
        val uri = mockk<Uri> {
            every { toFile() } returns file
        }
        coEvery { avatarService.uploadCatching(any(), any(), any(), any()) } returns GravatarResult.Success(avatar)
        coEvery { avatarStorage.addAvatar(email, avatar) } returns Unit
        initAvatarsFlowForEmail(email)

        val result = avatarRepository.uploadAvatar(email, uri)

        assertEquals(GravatarResult.Success<Avatar, QuickEditorError>(avatar), result)
        coVerify { avatarStorage.addAvatar(email, avatar) }
    }

    @Test
    fun `given token stored when avatar upload fails then Failure result`() = runTest {
        mockkStatic("androidx.core.net.UriKt")
        val file = mockk<File>()
        val uri = mockk<Uri> {
            every { toFile() } returns file
        }
        coEvery { tokenStorage.getToken(any()) } returns "token"
        coEvery {
            avatarService.uploadCatching(any(), any(), any(), any())
        } returns GravatarResult.Failure(ErrorType.Server)

        val result = avatarRepository.uploadAvatar(email, uri)

        assertEquals(GravatarResult.Failure<Unit, QuickEditorError>(QuickEditorError.Request(ErrorType.Server)), result)
        coVerify(exactly = 0) { avatarStorage.addAvatar(any(), any()) }
    }

    @Test
    fun `given token stored when avatar delete succeeds then Success result`() = runTest {
        val imageId = "imageId"
        coEvery {
            avatarService.deleteAvatarCatching(avatarId = "imageId", oauthToken = DEFAULT_TOKEN)
        } returns GravatarResult.Success(Unit)
        coEvery { avatarStorage.deleteAvatar(email, imageId) } returns Unit
        initAvatarsFlowForEmail(email)

        val result = avatarRepository.deleteAvatar(email, imageId)

        assertEquals(GravatarResult.Success<Unit, QuickEditorError>(Unit), result)
        coVerify { avatarStorage.deleteAvatar(email, imageId) }
    }

    @Test
    fun `given token stored when selected avatar delete succeeds then Success result`() = runTest {
        val imageId = "imageId"
        coEvery {
            avatarService.deleteAvatarCatching(avatarId = "imageId", oauthToken = DEFAULT_TOKEN)
        } returns GravatarResult.Success(Unit)
        coEvery { avatarStorage.deleteAvatar(email, imageId) } returns Unit
        initAvatarsFlowForEmail(email)

        val result = avatarRepository.deleteAvatar(email, imageId)

        assertEquals(GravatarResult.Success<Unit, QuickEditorError>(Unit), result)
        coVerify { avatarStorage.deleteAvatar(email, imageId) }
    }

    @Test
    fun `given token stored when avatar delete fails then Failure result`() = runTest {
        val imageId = "imageId"
        coEvery { tokenStorage.getToken(any()) } returns "token"
        coEvery {
            avatarService.deleteAvatarCatching(
                avatarId = "imageId",
                oauthToken = "token",
            )
        } returns GravatarResult.Failure(ErrorType.Server)

        val result = avatarRepository.deleteAvatar(email, imageId)

        assertEquals(GravatarResult.Failure<Unit, QuickEditorError>(QuickEditorError.Request(ErrorType.Server)), result)
        coVerify(exactly = 0) { avatarStorage.deleteAvatar(any(), any()) }
    }

    @Test
    fun `given no token when deleteAvatar is called then TokenNotFound result`() = runTest {
        val imageId = "imageId"
        coEvery { tokenStorage.getToken(any()) } returns null

        val result = avatarRepository.deleteAvatar(email, imageId)

        assertEquals(GravatarResult.Failure<Unit, QuickEditorError>(QuickEditorError.TokenNotFound), result)
        coVerify(exactly = 0) { avatarStorage.deleteAvatar(any(), any()) }
    }

    @Test
    fun `given no token found when updateAvatar is called then TokenNotFound result`() = runTest {
        coEvery { tokenStorage.getToken(any()) } returns null

        val result = avatarRepository.updateAvatar(email, "avatarId", Avatar.Rating.PG, "New Alt Text")

        assertEquals(GravatarResult.Failure<Unit, QuickEditorError>(QuickEditorError.TokenNotFound), result)
        coVerify(exactly = 0) { avatarStorage.updateAvatar(any(), any()) }
    }

    @Test
    fun `given token stored when update avatar fails then Failure result`() = runTest {
        coEvery {
            avatarService.updateAvatarCatching(any(), any(), any(), any())
        } returns GravatarResult.Failure(ErrorType.Server)
        initAvatarsFlowForEmail(email)

        val result = avatarRepository.updateAvatar(email, "avatarId", Avatar.Rating.PG, "New Alt Text")

        assertEquals(
            GravatarResult.Failure<Unit, QuickEditorError>(QuickEditorError.Request(ErrorType.Server)),
            result,
        )
        coVerify(exactly = 0) { avatarStorage.updateAvatar(any(), any()) }
    }

    @Test
    fun `given token stored when update avatar succeeds then Success result`() = runTest {
        initAvatarsFlowForEmail(email)
        val updatedAvatar = createAvatar(id = "avatarId", rating = Avatar.Rating.PG, altText = "New Alt Text")
        coEvery {
            avatarService.updateAvatarCatching(
                avatarId = updatedAvatar.imageId,
                oauthToken = DEFAULT_TOKEN,
                avatarRating = updatedAvatar.rating,
                altText = updatedAvatar.altText,
            )
        } returns GravatarResult.Success(updatedAvatar)
        coEvery { avatarStorage.updateAvatar(email, updatedAvatar) } returns Unit

        val result = avatarRepository.updateAvatar(
            email,
            updatedAvatar.imageId,
            updatedAvatar.rating,
            updatedAvatar.altText,
        )

        assertEquals(GravatarResult.Success<Avatar, QuickEditorError>(updatedAvatar), result)
        coVerify { avatarStorage.updateAvatar(email, updatedAvatar) }
    }

    private suspend fun initAvatarsFlowForEmail(email: Email, avatars: List<Avatar> = listOf()) {
        coEvery { tokenStorage.getToken(any()) } returns DEFAULT_TOKEN
        coEvery { avatarService.retrieveCatching(any(), any()) } returns GravatarResult.Success(avatars)
        coEvery { avatarStorage.storeAvatars(any(), any()) } returns Unit

        avatarRepository.getAvatars(email)
    }

    private fun createAvatar(
        id: String,
        isSelected: Boolean = false,
        rating: Avatar.Rating = Avatar.Rating.G,
        altText: String = "alt",
    ) = Avatar {
        imageUrl = URI.create("https://gravatar.com/avatar/test")
        imageId = id
        this.rating = rating
        this.altText = altText
        updatedDate = ""
        selected = isSelected
    }
}
