package tachiyomi.domain.chapter.interactor

import logcat.LogPriority
import tachiyomi.core.util.system.logcat
import tachiyomi.domain.chapter.model.Chapter
import tachiyomi.domain.chapter.repository.ChapterRepository

class GetChapterByUrlAndMangaId(
    private val chapterRepository: ChapterRepository,
) {

    suspend fun await(url: String, sourceId: Long): Chapter? {
        return try {
            chapterRepository.getChapterByUrlAndMangaId(url, sourceId)
        } catch (e: Exception) {
            logcat(LogPriority.ERROR, e)
            null
        }
    }
}
