package com.gravatar.quickeditor.ui.alttext

import com.gravatar.quickeditor.ui.gravatarScreenshotTest
import com.gravatar.restapi.models.Avatar
import com.gravatar.ui.GravatarTheme
import com.gravatar.uitestutils.RoborazziTest
import org.junit.Test
import org.robolectric.annotation.Config
import java.net.URI

class AltTextSectionTest : RoborazziTest() {
    @Test
    fun altTextSectionLoaded() = gravatarScreenshotTest {
        GravatarTheme {
            AltTextSection(
                avatar = Avatar {
                    imageUrl = URI.create("https://gravatar.com/avatar/test")
                    imageId = "1"
                    rating = Avatar.Rating.G
                    altText = "alt"
                    updatedDate = ""
                },
                onEvent = { },
            )
        }
    }

    @Config(qualifiers = "+night")
    @Test
    fun altTextSectionLoadedDark() = gravatarScreenshotTest {
        GravatarTheme {
            AltTextSection(
                avatar = Avatar {
                    imageUrl = URI.create("https://gravatar.com/avatar/test")
                    imageId = "1"
                    rating = Avatar.Rating.G
                    altText = "alt"
                    updatedDate = ""
                },
                onEvent = { },
            )
        }
    }

    @Test
    fun emptyAltTextSectionLoaded() = gravatarScreenshotTest {
        GravatarTheme {
            AltTextSection(
                avatar = Avatar {
                    imageUrl = URI.create("https://gravatar.com/avatar/test")
                    imageId = "1"
                    rating = Avatar.Rating.G
                    altText = "alt"
                    updatedDate = ""
                },
                onEvent = { },
            )
        }
    }

    @Config(qualifiers = "+night")
    @Test
    fun emptyAltTextSectionLoadedDark() = gravatarScreenshotTest {
        GravatarTheme {
            AltTextSection(
                avatar = Avatar {
                    imageUrl = URI.create("https://gravatar.com/avatar/test")
                    imageId = "1"
                    rating = Avatar.Rating.G
                    altText = "alt"
                    updatedDate = ""
                },
                onEvent = { },
            )
        }
    }
}
