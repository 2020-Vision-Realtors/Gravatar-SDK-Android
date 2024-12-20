package com.gravatar.quickeditor.ui.alttext

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import coil.compose.AsyncImage
import com.gravatar.quickeditor.R
import com.gravatar.quickeditor.ui.avatarpicker.AltTextPageUiState
import com.gravatar.quickeditor.ui.avatarpicker.AvatarPickerAction
import com.gravatar.quickeditor.ui.avatarpicker.AvatarPickerEvent
import com.gravatar.quickeditor.ui.avatarpicker.AvatarPickerViewModel
import com.gravatar.quickeditor.ui.avatarpicker.AvatarUpdateType
import com.gravatar.quickeditor.ui.avatarpicker.errorStringRes
import com.gravatar.quickeditor.ui.components.QEButton
import com.gravatar.quickeditor.ui.components.QESectionTitle
import com.gravatar.quickeditor.ui.extensions.QESnackbarHost
import com.gravatar.quickeditor.ui.extensions.SnackbarType
import com.gravatar.quickeditor.ui.extensions.showQESnackbar
import com.gravatar.restapi.models.Avatar
import com.gravatar.ui.GravatarTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.URI
import java.net.URL

@Composable
internal fun AltTextPage(onBackPressed: () -> Unit, viewModel: AvatarPickerViewModel, modifier: Modifier = Modifier) {
    BackHandler {
        onBackPressed()
    }

    val context = LocalContext.current
    val snackState = remember { SnackbarHostState() }
    val state by viewModel.uiState.collectAsState()
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        withContext(Dispatchers.Main.immediate) {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.actions.collect { action ->
                    when {
                        action is AvatarPickerAction.AvatarUpdated && action.type == AvatarUpdateType.ALT_TEXT -> {
                            onBackPressed()
                        }

                        action is AvatarPickerAction.AvatarUpdateFailed && action.type == AvatarUpdateType.ALT_TEXT -> {
                            scope.launch {
                                snackState.showQESnackbar(
                                    message = context.getString(action.type.errorStringRes),
                                    snackbarType = SnackbarType.Error,
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    GravatarTheme {
        Box(modifier = modifier.wrapContentSize()) {
            state.altTextPageUiState?.let { altTextState ->
                AltTextPage(
                    altTextState = altTextState,
                    onEvent = viewModel::onEvent,
                )
                QESnackbarHost(
                    modifier = Modifier
                        .align(Alignment.BottomStart),
                    hostState = snackState,
                )
            }
        }
    }
}

@Composable
internal fun AltTextPage(
    altTextState: AltTextPageUiState,
    onEvent: (AvatarPickerEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(modifier = modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.surfaceContainerHighest,
                    shape = RoundedCornerShape(8.dp),
                )
                .padding(16.dp),
        ) {
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    QESectionTitle(
                        title = stringResource(R.string.gravatar_qe_avatar_alt_text_section_title),
                        modifier = Modifier,
                    )
                    Text(
                        text = stringResource(id = R.string.gravatar_qe_avatar_alt_text_section_what_is),
                        color = MaterialTheme.colorScheme.primary,
                    )
                }

                Row(
                    verticalAlignment = Alignment.Top,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                ) {
                    val cornerRadius = 8.dp
                    val avatarSize = 96.dp
                    val sizePx = with(LocalDensity.current) { avatarSize.roundToPx() }
                    AsyncImage(
                        model = altTextState.avatar.imageUrlWithSize(sizePx),
                        contentDescription = stringResource(
                            id = R.string.gravatar_qe_selectable_avatar_content_description,
                        ),
                        modifier = Modifier
                            .size(96.dp)
                            .border(
                                1.dp,
                                MaterialTheme.colorScheme.surfaceDim,
                                RoundedCornerShape(cornerRadius),
                            )
                            .clip(RoundedCornerShape(cornerRadius)),
                    )
                    BasicTextField(
                        value = altTextState.altText,
                        onValueChange = {
                            onEvent(AvatarPickerEvent.AvatarAltTextChange(it))
                        },
                        textStyle = LocalTextStyle.current.copy(color = MaterialTheme.colorScheme.onSurface),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 16.dp, top = 8.dp, bottom = 8.dp),
                    )
                }
                QEButton(
                    buttonText = stringResource(R.string.gravatar_qe_avatar_alt_text_save_button),
                    onClick = { onEvent(AvatarPickerEvent.AvatarAltTextSaveTapped) },
                    modifier = Modifier.padding(top = 16.dp),
                    enabled = altTextState.isSaveButtonEnabled,
                    loading = altTextState.isUpdating,
                )
            }
        }
    }
}

private fun Avatar.imageUrlWithSize(sizePx: Int) = imageUrl.toURL()?.let { url ->
    URL(url.protocol, url.host, url.path.plus("?size=$sizePx"))
}.toString()

@Composable
@Preview(showBackground = true)
private fun AltTextPagePreview() {
    GravatarTheme {
        AltTextPage(
            altTextState = AltTextPageUiState(
                avatar = Avatar {
                    imageUrl = URI.create("https://gravatar.com/avatar/test")
                    imageId = "1"
                    rating = Avatar.Rating.G
                    altText = "alt"
                    updatedDate = ""
                },
                isUpdating = false,
                altText = "alt",
                isSaveButtonEnabled = true,
            ),
            onEvent = { },
        )
    }
}

@Composable
@Preview(showBackground = true)
private fun AltTextPageEmptyAltTextPreview() {
    GravatarTheme {
        AltTextPage(
            altTextState = AltTextPageUiState(
                avatar = Avatar {
                    imageUrl = URI.create("https://gravatar.com/avatar/test")
                    imageId = "1"
                    rating = Avatar.Rating.G
                    altText = ""
                    updatedDate = ""
                },
                isUpdating = false,
                altText = "",
                isSaveButtonEnabled = true,
            ),
            onEvent = { },
        )
    }
}
