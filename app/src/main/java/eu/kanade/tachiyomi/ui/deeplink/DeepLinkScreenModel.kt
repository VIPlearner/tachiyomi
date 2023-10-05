package eu.kanade.tachiyomi.ui.deeplink

import androidx.compose.runtime.Immutable
import cafe.adriel.voyager.core.model.StateScreenModel
import cafe.adriel.voyager.core.model.coroutineScope
import eu.kanade.domain.manga.model.toDomainManga
import eu.kanade.tachiyomi.source.online.HttpSource
import eu.kanade.tachiyomi.source.online.ResolvableSource
import kotlinx.coroutines.flow.update
import tachiyomi.core.util.lang.launchIO
import tachiyomi.domain.manga.interactor.NetworkToLocalManga
import tachiyomi.domain.manga.model.Manga
import tachiyomi.domain.source.service.SourceManager
import uy.kohesive.injekt.Injekt
import uy.kohesive.injekt.api.get

class DeepLinkScreenModel(
    deepLinkType: DeepLinkType = DeepLinkType.Search(""),
    private val sourceManager: SourceManager = Injekt.get(),
    private val networkToLocalManga: NetworkToLocalManga = Injekt.get()
) : StateScreenModel<DeepLinkScreenModel.State>(State.Loading) {

    init {
        coroutineScope.launchIO {
            when(deepLinkType){
                is DeepLinkType.Search -> {
                    val manga = sourceManager.getCatalogueSources()
                        .filterIsInstance<ResolvableSource>()
                        .filter { it.canResolveUri(deepLinkType.query) }
                        .firstNotNullOfOrNull { it.getManga(deepLinkType.query)?.toDomainManga(it.id) }

                    mutableState.update {
                        if (manga == null) {
                            State.NoResults
                        } else {
                            State.Result(manga)
                        }
                    }
                }

                is DeepLinkType.OpenChapter -> {
                    val networkManga = (sourceManager.getOrStub(deepLinkType.sourceId) as? HttpSource)?.getMangaDetailsFromUrl(deepLinkType.mangaUrl)
                    mutableState.update {
                        if (networkManga == null) {
                            State.NoResults
                        } else {
                            val manga = networkToLocalManga.await(networkManga.toDomainManga(deepLinkType.sourceId))
                            State.Result(manga, deepLinkType.chapter)
                        }
                    }
                }
                is DeepLinkType.OpenManga -> {
                    val networkManga = (sourceManager.getOrStub(deepLinkType.sourceId) as? HttpSource)?.getMangaDetailsFromUrl(deepLinkType.mangaUrl)
                    mutableState.update {
                        if (networkManga == null) {
                            State.NoResults
                        } else {
                            val manga = networkToLocalManga.await(networkManga.toDomainManga(deepLinkType.sourceId))
                            State.Result(manga)
                        }
                    }
                }
            }
        }
    }

    sealed interface State {
        @Immutable
        data object Loading : State

        @Immutable
        data object NoResults : State

        @Immutable
        data class Result(
            val manga: Manga,
            val chapterId: Long? = null
        ) : State
    }
}
