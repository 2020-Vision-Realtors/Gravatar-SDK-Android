package com.gravatar.quickeditor.ui.alttext

import com.gravatar.quickeditor.ui.gravatarScreenshotTest
import com.gravatar.ui.GravatarTheme
import com.gravatar.uitestutils.RoborazziTest
import org.junit.Test
import org.robolectric.annotation.Config

class AltTextPageTest : RoborazziTest() {
    @Test
    fun altTextPageLoaded() = gravatarScreenshotTest {
        GravatarTheme {
            AltTextPage(
                altTextState = AltTextUiState(
                    avatarUrl = "https://gravatar.com/avatar/test",
                    isUpdating = false,
                    altText = "alt",
                    isSaveButtonEnabled = false,
                ),
                onEvent = { },
            )
        }
    }

    @Config(qualifiers = "+night")
    @Test
    fun altTextPageLoadedDark() = gravatarScreenshotTest {
        GravatarTheme {
            AltTextPage(
                altTextState = AltTextUiState(
                    avatarUrl = "https://gravatar.com/avatar/test",
                    isUpdating = false,
                    altText = "alt",
                    isSaveButtonEnabled = false,
                ),
                onEvent = { },
            )
        }
    }

    @Test
    fun emptyAltTextPageLoaded() = gravatarScreenshotTest {
        GravatarTheme {
            AltTextPage(
                altTextState = AltTextUiState(
                    avatarUrl = "https://gravatar.com/avatar/test",
                    isUpdating = false,
                    altText = "New alt text",
                    isSaveButtonEnabled = true,
                ),
                onEvent = { },
            )
        }
    }

    @Config(qualifiers = "+night")
    @Test
    fun emptyAltTextPageLoadedDark() = gravatarScreenshotTest {
        GravatarTheme {
            AltTextPage(
                altTextState = AltTextUiState(
                    avatarUrl = "https://gravatar.com/avatar/test",
                    isUpdating = false,
                    altText = "New alt text",
                    isSaveButtonEnabled = true,
                ),
                onEvent = { },
            )
        }
    }
}
