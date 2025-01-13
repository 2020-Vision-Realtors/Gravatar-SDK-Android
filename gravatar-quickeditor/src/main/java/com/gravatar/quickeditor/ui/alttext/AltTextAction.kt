package com.gravatar.quickeditor.ui.alttext

internal sealed class AltTextAction {
    data object AltTextUpdated : AltTextAction()

    data object AltTextUpdateFailed : AltTextAction()

    data object AvatarCantBeLoaded : AltTextAction()
}
