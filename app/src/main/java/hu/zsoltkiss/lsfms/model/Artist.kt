package hu.zsoltkiss.lsfms.model

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class Artist(
    @Expose @SerializedName("name") val name: String,
    @Expose @SerializedName("mbid") val mbid: String,
    @Expose @SerializedName("url") val url: String,
    @Expose @SerializedName("image_small") val imageSmall: String,
    @Expose @SerializedName("image") val image: String)

data class ArtistMatches(@Expose @SerializedName("artist") val artists: List<Artist>)
data class SearchResults(@Expose @SerializedName("results") val matches: ArtistMatches)


class SimpleArtist(
    val name: String,
    val listeners: Int,
    val mediumImage: String
)


