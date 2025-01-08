package com.gravatar.quickeditor.data.storage

import com.gravatar.quickeditor.ui.avatarpicker.copy
import com.gravatar.restapi.models.Avatar
import com.gravatar.types.Email
import kotlinx.coroutines.flow.MutableSharedFlow

internal class AvatarStorage {
    private val avatarsFlows = mutableMapOf<String, MutableSharedFlow<List<Avatar>>>()

    internal fun avatarsFlow(email: Email): MutableSharedFlow<List<Avatar>> =
        avatarsFlows.getOrPut(email.hash().toString()) { MutableSharedFlow(replay = 1) }

    internal suspend fun storeAvatars(avatars: List<Avatar>, email: Email) {
        avatarsFlow(email).emit(avatars)
    }

    internal suspend fun markAvatarAsSelected(email: Email, avatarId: String) {
        avatarsFlow(email).let { avatarsFlow ->
            val avatars = avatarsFlow.replayCache.lastOrNull()?.let {
                it.map { avatar ->
                    if (avatar.imageId == avatarId) {
                        avatar.copy(selected = true)
                    } else {
                        avatar.copy(selected = false)
                    }
                }
            }

            avatars?.let { avatarsFlow.emit(it) }
        }
    }

    internal suspend fun addAvatar(email: Email, avatar: Avatar) {
        avatarsFlow(email).let { avatarsFlow ->
            val avatars = avatarsFlow.replayCache.lastOrNull()?.let {
                buildList {
                    add(avatar)
                    it.filter { avatarToFilter ->
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
            }

            avatars?.let { avatarsFlow.emit(it) }
        }
    }

    internal suspend fun deleteAvatar(email: Email, avatarId: String) {
        avatarsFlow(email).let { avatarsFlow ->
            val avatars = avatarsFlow.replayCache.lastOrNull()?.let {
                it.filter { avatar ->
                    avatar.imageId != avatarId
                }
            }

            avatars?.let { avatarsFlow.emit(it) }
        }
    }

    internal suspend fun updateAvatar(email: Email, avatarToUpdate: Avatar) {
        avatarsFlow(email).let { avatarsFlow ->
            val avatars = avatarsFlow.replayCache.lastOrNull()?.let {
                it.map { avatar ->
                    if (avatar.imageId == avatarToUpdate.imageId) {
                        avatarToUpdate
                    } else {
                        avatar
                    }
                }
            }

            avatars?.let { avatarsFlow.emit(it) }
        }
    }
}
