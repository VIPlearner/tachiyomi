package eu.kanade.tachiyomi.ui.deeplink

sealed class DeepLinkType {
    data class Search(val query: String) : DeepLinkType()
    data class OpenManga(val sourceId: Long, val mangaUrl: String) : DeepLinkType()
    data class OpenChapter(val sourceId: Long, val mangaUrl: String, val chapter: Long) : DeepLinkType()
}
