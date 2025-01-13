package com.gravatar.quickeditor.data.repository

import android.net.Uri
import androidx.core.net.toFile
import com.gravatar.quickeditor.data.models.QuickEditorError
import com.gravatar.quickeditor.data.storage.AvatarStorage
import com.gravatar.quickeditor.data.storage.TokenStorage
import com.gravatar.restapi.models.Avatar
import com.gravatar.services.AvatarService
import com.gravatar.services.GravatarResult
import com.gravatar.types.Email
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.withContext

internal class AvatarRepository(
    private val avatarService: AvatarService,
    private val tokenStorage: TokenStorage,
    private val avatarStorage: AvatarStorage,
    private val dispatcher: CoroutineDispatcher,
) {
    fun getAvatars(email: Email) = avatarStorage.avatarsFlow(email).asSharedFlow()

    fun getAvatar(email: Email, avatarId: String): Avatar? =
        getAvatars(email).replayCache.firstOrNull()?.firstOrNull { it.imageId == avatarId }

    suspend fun refreshAvatars(email: Email): GravatarResult<List<Avatar>, QuickEditorError> = withContext(dispatcher) {
        val token = tokenStorage.getToken(email.hash().toString())
        token?.let {
            when (val avatarsResult = avatarService.retrieveCatching(token, email.hash())) {
                is GravatarResult.Success -> {
                    avatarStorage.storeAvatars(avatarsResult.value, email)
                    GravatarResult.Success(avatarsResult.value)
                }

                is GravatarResult.Failure -> {
                    GravatarResult.Failure(QuickEditorError.Request(avatarsResult.error))
                }
            }
        } ?: GravatarResult.Failure(QuickEditorError.TokenNotFound)
    }

    suspend fun selectAvatar(email: Email, avatarId: String): GravatarResult<Unit, QuickEditorError> = withContext(
        dispatcher,
    ) {
        val token = tokenStorage.getToken(email.hash().toString())
        token?.let {
            when (val result = avatarService.setAvatarCatching(email.hash().toString(), avatarId, token)) {
                is GravatarResult.Success -> {
                    avatarStorage.selectAvatar(avatarId = avatarId, email = email)
                    GravatarResult.Success(Unit)
                }

                is GravatarResult.Failure -> GravatarResult.Failure(QuickEditorError.Request(result.error))
            }
        } ?: GravatarResult.Failure(QuickEditorError.TokenNotFound)
    }

    suspend fun uploadAvatar(email: Email, avatarUri: Uri): GravatarResult<Avatar, QuickEditorError> =
        withContext(dispatcher) {
            val hash = email.hash()
            val token = tokenStorage.getToken(hash.toString())
            token?.let {
                when (
                    val result = avatarService.uploadCatching(avatarUri.toFile(), token, hash)
                ) {
                    is GravatarResult.Success -> {
                        avatarStorage.addAvatar(email, result.value)
                        GravatarResult.Success(result.value)
                    }

                    is GravatarResult.Failure -> GravatarResult.Failure(QuickEditorError.Request(result.error))
                }
            } ?: GravatarResult.Failure(QuickEditorError.TokenNotFound)
        }

    suspend fun deleteAvatar(email: Email, avatarId: String): GravatarResult<Unit, QuickEditorError> = withContext(
        dispatcher,
    ) {
        val token = tokenStorage.getToken(email.hash().toString())
        token?.let {
            when (val result = avatarService.deleteAvatarCatching(avatarId, token)) {
                is GravatarResult.Success -> {
                    avatarStorage.deleteAvatar(email, avatarId)
                    GravatarResult.Success(Unit)
                }
                is GravatarResult.Failure -> GravatarResult.Failure(QuickEditorError.Request(result.error))
            }
        } ?: GravatarResult.Failure(QuickEditorError.TokenNotFound)
    }

    suspend fun updateAvatar(
        email: Email,
        avatarId: String,
        rating: Avatar.Rating? = null,
        altText: String? = null,
    ): GravatarResult<Avatar, QuickEditorError> = withContext(dispatcher) {
        val token = tokenStorage.getToken(email.hash().toString())
        token?.let {
            when (val result = avatarService.updateAvatarCatching(avatarId, token, rating, altText)) {
                is GravatarResult.Success -> {
                    avatarStorage.updateAvatar(email, result.value)
                    GravatarResult.Success(result.value)
                }

                is GravatarResult.Failure -> GravatarResult.Failure(QuickEditorError.Request(result.error))
            }
        } ?: GravatarResult.Failure(QuickEditorError.TokenNotFound)
    }
}
