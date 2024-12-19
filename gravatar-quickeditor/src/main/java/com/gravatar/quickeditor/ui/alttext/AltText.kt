package com.gravatar.quickeditor.ui.alttext

import androidx.activity.compose.BackHandler
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
internal fun AltText(onBackPressed: () -> Unit, modifier: Modifier = Modifier) {
    BackHandler {
        onBackPressed()
    }

    Surface(modifier) { Text("Alt Text Section", modifier = modifier) }
}
