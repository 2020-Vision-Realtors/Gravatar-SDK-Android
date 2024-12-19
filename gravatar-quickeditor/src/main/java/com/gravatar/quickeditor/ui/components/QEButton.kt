package com.gravatar.quickeditor.ui.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.gravatar.quickeditor.R
import com.gravatar.ui.GravatarTheme

@Composable
internal fun QEButton(
    buttonText: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    loading: Boolean = false,
) {
    Button(
        modifier = modifier
            .fillMaxWidth()
            .size(50.dp),
        onClick = onClick,
        shape = RoundedCornerShape(4.dp),
        contentPadding = if (loading) PaddingValues(8.dp) else PaddingValues(horizontal = 14.dp),
        enabled = enabled && !loading,
    ) {
        if (loading) {
            CircularProgressIndicator(Modifier.size(32.dp))
        } else {
            Text(
                text = buttonText,
                style = MaterialTheme.typography.titleMedium,
            )
        }
    }
}

@Preview
@Composable
private fun QEButtonPreview() {
    GravatarTheme {
        Surface {
            QEButton(
                modifier = Modifier.padding(20.dp),
                buttonText = stringResource(id = R.string.gravatar_qe_avatar_picker_upload_image),
                onClick = { },
                enabled = true,
            )
        }
    }
}

@Preview
@Composable
private fun QEButtonLoadingPreview() {
    GravatarTheme {
        Surface {
            QEButton(
                modifier = Modifier.padding(20.dp),
                buttonText = stringResource(id = R.string.gravatar_qe_avatar_picker_upload_image),
                onClick = { },
                enabled = false,
                loading = true,
            )
        }
    }
}
