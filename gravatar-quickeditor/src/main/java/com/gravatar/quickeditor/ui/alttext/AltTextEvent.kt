package com.gravatar.quickeditor.ui.alttext

internal sealed class AltTextEvent {
    data class AvatarAltTextChange(val newAltText: String) : AltTextEvent()

    data object AvatarAltTextSaveTapped : AltTextEvent()
}
