package com.gravatar.quickeditor.data.storage

import app.cash.turbine.test
import com.gravatar.quickeditor.createAvatar
import com.gravatar.quickeditor.ui.avatarpicker.copy
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
    fun `given avatars when storeAvatars then avatarsFlow emits avatars`() = runTest {
        val email = Email("email")

        // Given
        val avatars = listOf(createAvatar(id = "imageId", isSelected = true))

        // When
        avatarStorage.storeAvatars(avatars, email)

        // Then
        avatarStorage.avatarsFlow(email).test {
            assertEquals(avatars, awaitItem())
        }
    }

    @Test
    fun `given avatarId when selectAvatar then avatarsFlow emits avatars with selected avatar`() = runTest {
        val email = Email("email")

        // Given
        val avatars = listOf(
            createAvatar("otherAvatar", isSelected = true),
            createAvatar(id = "imageId", isSelected = false),
        )
        avatarStorage.storeAvatars(avatars, email)

        // When
        avatarStorage.selectAvatar(avatarId = "imageId", email = email)

        // Then
        avatarStorage.avatarsFlow(email).test {
            assertEquals(
                listOf(
                    createAvatar("otherAvatar", isSelected = false),
                    createAvatar(id = "imageId", isSelected = true),
                ),
                awaitItem(),
            )
        }
    }

    @Test
    fun `given avatar when addAvatar then avatarsFlow emits avatars`() = runTest {
        val email = Email("email")

        // Given
        val avatars = listOf(
            createAvatar("otherAvatar", isSelected = true),
            createAvatar("imageId", isSelected = false),
        )
        avatarStorage.storeAvatars(avatars, email)

        // When
        val updatedAvatar = createAvatar("imageId", isSelected = true)
        avatarStorage.addAvatar(email, updatedAvatar)

        // Then
        avatarStorage.avatarsFlow(email).test {
            assertEquals(
                listOf(updatedAvatar, createAvatar("otherAvatar", isSelected = false)),
                awaitItem(),
            )
        }
    }

    @Test
    fun `given avatarId when deleteAvatar then avatarsFlow emits avatars without deleted avatar`() = runTest {
        val email = Email("email")

        // Given
        val avatars = listOf(
            createAvatar("otherAvatar", isSelected = false),
            createAvatar("imageId", isSelected = true),
        )
        avatarStorage.storeAvatars(avatars, email)

        // When
        avatarStorage.deleteAvatar(email, "imageId")

        // Then
        avatarStorage.avatarsFlow(email).test {
            assertEquals(
                listOf(createAvatar("otherAvatar", isSelected = false)),
                awaitItem(),
            )
        }
    }

    @Test
    fun `given avatar without selected info when updateAvatar then emits avatars with updated avatar`() = runTest {
        val email = Email("email")

        // Given
        val avatars = listOf(
            createAvatar("otherAvatar"),
            createAvatar("imageId", altText = "altText", isSelected = true),
        )
        avatarStorage.storeAvatars(avatars, email)

        // When - Updated avatar without selected info
        val updatedAvatar = createAvatar("imageId", altText = "newAltText")
        avatarStorage.updateAvatar(email, updatedAvatar)

        // Then - Updated avatar should keep the selected value from the previous stored avatar
        avatarStorage.avatarsFlow(email).test {
            assertEquals(
                listOf(createAvatar("otherAvatar"), updatedAvatar.copy(selected = true)),
                awaitItem(),
            )
        }
    }

    @Test
    fun `given avatar with selected info when updateAvatar then emits avatars with updated avatar`() = runTest {
        val email = Email("email")

        // Given
        val avatars = listOf(
            createAvatar("otherAvatar"),
            createAvatar("imageId", altText = "altText", isSelected = false),
        )
        avatarStorage.storeAvatars(avatars, email)

        // When - Updated avatar with selected info
        val updatedAvatar = createAvatar("imageId", altText = "newAltText", isSelected = true)
        avatarStorage.updateAvatar(email, updatedAvatar)

        // Then - Updated avatar should keep the selected value from the updated avatar
        avatarStorage.avatarsFlow(email).test {
            assertEquals(
                listOf(createAvatar("otherAvatar"), updatedAvatar),
                awaitItem(),
            )
        }
    }
}
