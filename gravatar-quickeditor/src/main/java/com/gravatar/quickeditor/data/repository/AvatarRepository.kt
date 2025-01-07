package com.gravatar.quickeditor.data.repository

import android.net.Uri
import androidx.core.net.toFile
import com.gravatar.quickeditor.data.models.QuickEditorError
import com.gravatar.quickeditor.data.storage.TokenStorage
import com.gravatar.quickeditor.ui.avatarpicker.copy
import com.gravatar.restapi.models.Avatar
import com.gravatar.services.AvatarService
import com.gravatar.services.GravatarResult
import com.gravatar.types.Email
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.withContext

internal class AvatarRepository(
    private val avatarService: AvatarService,
    private val tokenStorage: TokenStorage,
    private val dispatcher: CoroutineDispatcher,
) {
    fun getAvatarsFlow(email: Email) = avatarsFlow(email).asSharedFlow()

    private val avatarsFlows = mutableMapOf<String, MutableSharedFlow<EmailAvatars>>()

    private fun avatarsFlow(email: Email): MutableSharedFlow<EmailAvatars> =
        avatarsFlows.getOrPut(email.hash().toString()) { MutableSharedFlow(replay = 1) }

    suspend fun getAvatars(email: Email): GravatarResult<EmailAvatars, QuickEditorError> = withContext(dispatcher) {
        val token = tokenStorage.getToken(email.hash().toString())
        token?.let {
            when (val avatarsResult = avatarService.retrieveCatching(token, email.hash())) {
                is GravatarResult.Success -> {
                    val emailAvatars = avatarsResult.value.let { avatars ->
                        EmailAvatars(
                            avatars,
                            avatars.firstOrNull { it.selected == true }?.imageId,
                        )
                    }
                    avatarsFlow(email).emit(emailAvatars)
                    GravatarResult.Success(emailAvatars)
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
                    avatarsFlow(email).let { avatarsFlow ->
                        val emailAvatars = avatarsFlow.replayCache.lastOrNull()?.let {
                            it.copy(
                                avatars = it.avatars.map { avatar ->
                                    if (avatar.imageId == avatarId) {
                                        avatar.copy(selected = true)
                                    } else {
                                        avatar.copy(selected = false)
                                    }
                                },
                                selectedAvatarId = avatarId,
                            )
                        }

                        emailAvatars?.let { avatarsFlow.emit(it) }
                    }
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
                        avatarsFlow(email).let { avatarsFlow ->
                            val emailAvatars = avatarsFlow.replayCache.lastOrNull()?.let {
                                val avatars = buildList {
                                    add(result.value)
                                    it.avatars.filter { avatar ->
                                        avatar.imageId != result.value.imageId
                                    }.let { avatars ->
                                        addAll(
                                            if (result.value.selected == true) {
                                                avatars.map { avatar ->
                                                    avatar.copy(selected = false)
                                                }
                                            } else {
                                                avatars
                                            },
                                        )
                                    }
                                }
                                it.copy(
                                    avatars = avatars,
                                    selectedAvatarId = if (result.value.selected == true) {
                                        result.value.imageId
                                    } else {
                                        it.selectedAvatarId
                                    },
                                )
                            }

                            emailAvatars?.let { avatarsFlow.emit(it) }
                        }

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
                is GravatarResult.Success -> GravatarResult.Success(Unit)
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
                    updateAvatarFlow(email, result.value)
                    GravatarResult.Success(result.value)
                }

                is GravatarResult.Failure -> GravatarResult.Failure(QuickEditorError.Request(result.error))
            }
        } ?: GravatarResult.Failure(QuickEditorError.TokenNotFound)
    }

    private suspend fun updateAvatarFlow(email: Email, avatarToUpdate: Avatar) {
        avatarsFlow(email).let { avatarsFlow ->
            val emailAvatars = avatarsFlow.replayCache.lastOrNull()?.let {
                it.copy(
                    avatars = it.avatars.map { avatar ->
                        if (avatar.imageId == avatarToUpdate.imageId) {
                            avatarToUpdate
                        } else {
                            avatar
                        }
                    },
                )
            }

            emailAvatars?.let { avatarsFlow.emit(it) }
        }
    }
}

internal data class EmailAvatars(
    val avatars: List<Avatar>,
    val selectedAvatarId: String?,
)
