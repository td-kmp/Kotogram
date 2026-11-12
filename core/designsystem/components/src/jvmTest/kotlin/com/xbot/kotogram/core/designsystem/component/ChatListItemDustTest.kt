@file:OptIn(ExperimentalComposeUiApi::class, ExperimentalComposeRuntimeApi::class, ComposeToolingApi::class)

package com.xbot.kotogram.core.designsystem.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.ExperimentalComposeRuntimeApi
import androidx.compose.runtime.RecomposeScope
import androidx.compose.runtime.currentComposer
import androidx.compose.runtime.tooling.ComposeToolingApi
import androidx.compose.runtime.tooling.CompositionObserver
import androidx.compose.runtime.tooling.ObservableComposition
import androidx.compose.runtime.tooling.setObserver
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.ImageComposeScene
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.layout.layout
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.unit.Density
import com.xbot.kotogram.core.designsystem.KotogramTheme
import org.jetbrains.skia.Image
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

class ChatListItemDustTest {
    private class Counts {
        var layouts = 0
        var draws = 0
        var recomposed = 0
    }

    private fun scene(
        counts: Counts,
        text: AnnotatedString,
    ) = ImageComposeScene(WIDTH, HEIGHT, Density(DENSITY)) {
        val composition = currentComposer.composition
        DisposableEffect(composition) {
            val handle = composition.setObserver(RecomposeCounter { counts.recomposed++ })
            onDispose { handle?.dispose() }
        }
        KotogramTheme(darkTheme = false, dynamicColor = false) {
            Box(
                Modifier
                    .layout { measurable, constraints ->
                        counts.layouts++
                        val placeable = measurable.measure(constraints)
                        layout(placeable.width, placeable.height) { placeable.place(0, 0) }
                    }.drawWithContent {
                        counts.draws++
                        drawContent()
                    },
            ) { Row(text) }
        }
    }

    @Test
    fun `a frame of dust neither recomposes, lays out nor redraws the row`() {
        val counts = Counts()
        val scene = scene(counts, spoilerText())
        try {
            repeat(SETTLE_FRAMES) { scene.render(it * FRAME_NANOS) }
            val before = listOf(counts.recomposed, counts.layouts, counts.draws)
            val first = scene.render(SETTLE_FRAMES * FRAME_NANOS).pixels()
            var last = first
            for (frame in 1..FRAMES) last = scene.render((SETTLE_FRAMES + frame) * FRAME_NANOS).pixels()

            assertEquals(before, listOf(counts.recomposed, counts.layouts, counts.draws))
            assertFalse(first.contentEquals(last), "the dust didn't move")
        } finally {
            scene.close()
        }
    }
}

@Composable
private fun Row(text: AnnotatedString) {
    ChatListItem(
        avatar = {
            ChatAvatar { blur -> Avatar(null, "P", 2, Modifier.fillMaxSize(), blur = blur) }
        },
        title = { Title(text = "Puzzles") },
        date = { Date("21:13") },
        text = { Message(text = text) },
        modifier = Modifier.fillMaxWidth(),
    )
}

private fun spoilerText(): AnnotatedString = buildAnnotatedString {
    append("The answer is ")
    val start = length
    append("forty-two, and the rest of this long message hides under the spoiler as well")
    addStringAnnotation(SPOILER_ANNOTATION, "", start, length)
}

private class RecomposeCounter(
    private val onEnter: () -> Unit,
) : CompositionObserver {
    override fun onBeginComposition(composition: ObservableComposition) = Unit

    override fun onScopeEnter(scope: RecomposeScope) = onEnter()

    override fun onReadInScope(
        scope: RecomposeScope,
        value: Any,
    ) = Unit

    override fun onScopeExit(scope: RecomposeScope) = Unit

    override fun onEndComposition(composition: ObservableComposition) = Unit

    override fun onScopeInvalidated(
        scope: RecomposeScope,
        value: Any?,
    ) = Unit

    override fun onScopeDisposed(scope: RecomposeScope) = Unit
}

private fun Image.pixels(): ByteArray = encodeToData()!!.bytes

private const val WIDTH = 1170
private const val HEIGHT = 240
private const val DENSITY = 3f
private const val FRAME_NANOS = 16_666_667L
private const val SETTLE_FRAMES = 30
private const val FRAMES = 60
