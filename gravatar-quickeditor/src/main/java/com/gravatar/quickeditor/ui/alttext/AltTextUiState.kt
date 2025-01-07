package com.gravatar.quickeditor.ui.alttext

internal data class AltTextUiState(
    val avatarUrl: String,
    val isSaveButtonEnabled: Boolean,
    val isUpdating: Boolean,
    val altText: String,
)
