package com.gravatar.quickeditor.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
internal fun QEPage(topBar: @Composable () -> Unit, content: @Composable () -> Unit, modifier: Modifier = Modifier) {
    Column(modifier) {
        topBar()
        content()
    }
}
