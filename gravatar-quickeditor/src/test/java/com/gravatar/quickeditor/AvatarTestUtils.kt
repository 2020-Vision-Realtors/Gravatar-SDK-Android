package com.gravatar.quickeditor

import com.gravatar.restapi.models.Avatar
import java.net.URI

internal fun createAvatar(
    id: String,
    isSelected: Boolean? = null,
    rating: Avatar.Rating = Avatar.Rating.G,
    altText: String = "alt",
) = Avatar {
    imageUrl = URI.create("https://gravatar.com/avatar/test")
    imageId = id
    this.rating = rating
    this.altText = altText
    updatedDate = ""
    selected = isSelected
}
