package com.gravatar.quickeditor.data.storage

import app.cash.turbine.test
import com.gravatar.quickeditor.createAvatar
import com.gravatar.quickeditor.data.repository.EmailAvatars
import com.gravatar.types.Email
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class AvatarStorageTest {
    private lateinit var avatarStorage: AvatarStorage

    @Before
    fun setUp() {
        avatarStorage = AvatarStorage()
    }

    @Test
    fun `given avatars when storeAvatars then avatarsFlow emits a avatars`() = runTest {
        val email = Email("email")

        // Given
        val emailAvatars = EmailAvatars(
            avatars = listOf(createAvatar(id = "imageId", isSelected = true)),
            selectedAvatarId = "imageId",
        )

        // When
        avatarStorage.storeAvatars(emailAvatars, email)

        // Then
        avatarStorage.avatarsFlow(email).test {
            assertEquals(emailAvatars, awaitItem())
        }
    }

    @Test
    fun `given avatarId when markAvatarAsSelected then avatarsFlow emits a avatars with selected avatar`() = runTest {
        val email = Email("email")

        // Given
        val emailAvatars = EmailAvatars(
            avatars = listOf(
                createAvatar("otherAvatar", isSelected = true),
                createAvatar(id = "imageId", isSelected = false),
            ),
            selectedAvatarId = "otherAvatar",
        )
        avatarStorage.storeAvatars(emailAvatars, email)

        // When
        avatarStorage.markAvatarAsSelected(email, "imageId")

        // Then
        avatarStorage.avatarsFlow(email).test {
            assertEquals(
                emailAvatars.copy(
                    avatars = listOf(
                        createAvatar("otherAvatar", isSelected = false),
                        createAvatar(id = "imageId", isSelected = true),
                    ),
                    selectedAvatarId = "imageId",
                ),
                awaitItem(),
            )
        }
    }

    @Test
    fun `given avatar when addAvatar then avatarsFlow emits a avatars with updated avatar`() = runTest {
        val email = Email("email")

        // Given
        val emailAvatars = EmailAvatars(
            avatars = listOf(
                createAvatar("otherAvatar", isSelected = true),
                createAvatar("imageId", isSelected = false),
            ),
            selectedAvatarId = "imageId",
        )
        avatarStorage.storeAvatars(emailAvatars, email)

        // When
        val updatedAvatar = createAvatar("imageId", isSelected = true)
        avatarStorage.addAvatar(email, updatedAvatar)

        // Then
        avatarStorage.avatarsFlow(email).test {
            assertEquals(
                emailAvatars.copy(
                    avatars = listOf(updatedAvatar, createAvatar("otherAvatar", isSelected = false)),
                    selectedAvatarId = "imageId",
                ),
                awaitItem(),
            )
        }
    }

    @Test
    fun `given avatarId when deleteAvatar then avatarsFlow emits a avatars without deleted avatar`() = runTest {
        val email = Email("email")

        // Given
        val emailAvatars = EmailAvatars(
            avatars = listOf(
                createAvatar("otherAvatar", isSelected = false),
                createAvatar("imageId", isSelected = true),
            ),
            selectedAvatarId = "imageId",
        )
        avatarStorage.storeAvatars(emailAvatars, email)

        // When
        avatarStorage.deleteAvatar(email, "imageId")

        // Then
        avatarStorage.avatarsFlow(email).test {
            assertEquals(
                emailAvatars.copy(
                    avatars = listOf(createAvatar("otherAvatar", isSelected = false)),
                    selectedAvatarId = null,
                ),
                awaitItem(),
            )
        }
    }

    @Test
    fun `given avatar when updateAvatar then avatarsFlow emits a avatars with updated avatar`() = runTest {
        val email = Email("email")

        // Given
        val emailAvatars = EmailAvatars(
            avatars = listOf(createAvatar("otherAvatar"), createAvatar("imageId", altText = "altText")),
            selectedAvatarId = null,
        )
        avatarStorage.storeAvatars(emailAvatars, email)

        // When
        val updatedAvatar = createAvatar("imageId", altText = "newAltText")
        avatarStorage.updateAvatar(email, updatedAvatar)

        // Then
        avatarStorage.avatarsFlow(email).test {
            assertEquals(
                emailAvatars.copy(
                    avatars = listOf(createAvatar("otherAvatar"), updatedAvatar),
                    selectedAvatarId = null,
                ),
                awaitItem(),
            )
        }
    }
}
