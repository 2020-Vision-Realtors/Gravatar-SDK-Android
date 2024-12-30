package com.gravatar.quickeditor.ui.avatarpicker

import android.net.Uri
import androidx.annotation.StringRes
import com.gravatar.quickeditor.R
import com.gravatar.restapi.models.Avatar
import com.gravatar.types.Email
import java.io.File

internal sealed class AvatarPickerAction {
    data object AvatarSelected : AvatarPickerAction()

    data class LaunchImageCropper(val imageUri: Uri, val tempFile: File) : AvatarPickerAction()

    data object AvatarSelectionFailed : AvatarPickerAction()

    data object InvokeAuthFailed : AvatarPickerAction()

    data object AvatarDownloadStarted : AvatarPickerAction()

    data object DownloadManagerNotAvailable : AvatarPickerAction()

    data class AvatarDeletionFailed(val avatarId: String) : AvatarPickerAction()

    data class AvatarUpdated(val type: AvatarUpdateType) : AvatarPickerAction()

    data class AvatarUpdateFailed(val type: AvatarUpdateType) : AvatarPickerAction()

    data class LaunchAvatarAltText(val email: Email, val avatar: Avatar) : AvatarPickerAction()
}

internal enum class AvatarUpdateType {
    RATING,
    ALT_TEXT,
}

internal val AvatarUpdateType.successStringRes: Int
    @StringRes get() = when (this) {
        AvatarUpdateType.RATING -> R.string.gravatar_qe_avatar_picker_rating_update_success
        AvatarUpdateType.ALT_TEXT -> R.string.gravatar_qe_avatar_picker_alt_text_update_success
    }

internal val AvatarUpdateType.errorStringRes: Int
    @StringRes get() = when (this) {
        AvatarUpdateType.RATING -> R.string.gravatar_qe_avatar_picker_rating_update_error
        AvatarUpdateType.ALT_TEXT -> R.string.gravatar_qe_avatar_picker_alt_text_update_error
    }
