package com.gravatar.quickeditor.data.storage

import com.gravatar.quickeditor.data.repository.EmailAvatars
import com.gravatar.quickeditor.ui.avatarpicker.copy
import com.gravatar.restapi.models.Avatar
import com.gravatar.types.Email
import kotlinx.coroutines.flow.MutableSharedFlow

internal class AvatarStorage {
    private val avatarsFlows = mutableMapOf<String, MutableSharedFlow<EmailAvatars>>()

    internal fun avatarsFlow(email: Email): MutableSharedFlow<EmailAvatars> =
        avatarsFlows.getOrPut(email.hash().toString()) { MutableSharedFlow(replay = 1) }

    internal suspend fun storeAvatars(emailAvatars: EmailAvatars, email: Email) {
        avatarsFlow(email).emit(emailAvatars)
    }

    internal suspend fun markAvatarAsSelected(email: Email, avatarId: String) {
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
    }

    internal suspend fun addAvatar(email: Email, avatar: Avatar) {
        avatarsFlow(email).let { avatarsFlow ->
            val emailAvatars = avatarsFlow.replayCache.lastOrNull()?.let {
                val avatars = buildList {
                    add(avatar)
                    it.avatars.filter { avatarToFilter ->
                        avatarToFilter.imageId != avatar.imageId
                    }.let { avatars ->
                        addAll(
                            if (avatar.selected == true) {
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
                    selectedAvatarId = if (avatar.selected == true) {
                        avatar.imageId
                    } else {
                        it.selectedAvatarId
                    },
                )
            }

            emailAvatars?.let { avatarsFlow.emit(it) }
        }
    }

    internal suspend fun deleteAvatar(email: Email, avatarId: String) {
        avatarsFlow(email).let { avatarsFlow ->
            val emailAvatars = avatarsFlow.replayCache.lastOrNull()?.let {
                it.copy(
                    avatars = it.avatars.filter { avatar ->
                        avatar.imageId != avatarId
                    },
                    selectedAvatarId = if (it.selectedAvatarId == avatarId) {
                        null
                    } else {
                        it.selectedAvatarId
                    },
                )
            }

            emailAvatars?.let { avatarsFlow.emit(it) }
        }
    }

    internal suspend fun updateAvatar(email: Email, avatarToUpdate: Avatar) {
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
