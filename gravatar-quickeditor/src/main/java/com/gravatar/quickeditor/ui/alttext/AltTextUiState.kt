package com.gravatar.quickeditor.ui.alttext

import java.net.URI

internal data class AltTextUiState(
    val avatarUrl: URI? = null,
    val isSaveButtonEnabled: Boolean,
    val isUpdating: Boolean,
    val altText: String = "",
)
