package eu.kanade.tachiyomi.ui.deeplink

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import eu.kanade.presentation.components.AppBar
import eu.kanade.presentation.util.Screen
import eu.kanade.tachiyomi.R
import eu.kanade.tachiyomi.ui.browse.source.globalsearch.GlobalSearchScreen
import eu.kanade.tachiyomi.ui.manga.MangaScreen
import eu.kanade.tachiyomi.ui.reader.ReaderActivity
import tachiyomi.presentation.core.components.material.Scaffold
import tachiyomi.presentation.core.screens.LoadingScreen

class DeepLinkScreen(
    val deepLinkType: DeepLinkType
) : Screen() {

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val context = LocalContext.current

        val screenModel = rememberScreenModel {
            DeepLinkScreenModel(deepLinkType = deepLinkType)
        }
        val state by screenModel.state.collectAsState()
        Scaffold(
            topBar = { scrollBehavior ->
                AppBar(
                    title = stringResource(R.string.action_search_hint),
                    navigateUp = navigator::pop,
                    scrollBehavior = scrollBehavior,
                )
            },
        ) { contentPadding ->
            when (state) {
                is DeepLinkScreenModel.State.Loading -> {
                    LoadingScreen(Modifier.padding(contentPadding))
                }
                is DeepLinkScreenModel.State.NoResults -> {
                    when(deepLinkType){
                        is DeepLinkType.Search -> navigator.replace(GlobalSearchScreen(deepLinkType.query))
                        is DeepLinkType.OpenChapter -> navigator.pop()
                        is DeepLinkType.OpenManga -> navigator.pop()
                    }
                }
                is DeepLinkScreenModel.State.Result -> {
                    val chapterId = (state as? DeepLinkScreenModel.State.Result)?.chapterId
                    val mangaId = (state as DeepLinkScreenModel.State.Result).manga.id
                    if(chapterId == null){
                        navigator.replace(
                            MangaScreen(
                                mangaId,
                                true,
                            ),
                        )
                    } else{
                        navigator.pop()
                        ReaderActivity.newIntent(context, mangaId, chapterId).also {
                            context.startActivity(it)
                        }
                    }
                }
            }
        }
    }
}
