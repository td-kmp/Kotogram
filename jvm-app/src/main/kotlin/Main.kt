import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.xbot.kotogram.ui.App
import com.xbot.kotogram.ui.KotogramTheme
import com.xbot.kotogram.ui.di.closeTelegramClient
import com.xbot.kotogram.ui.di.initKoin
import dev.nucleusframework.application.nucleusApplication
import dev.nucleusframework.window.material.MaterialDecoratedWindow
import dev.nucleusframework.window.material.MaterialTitleBar
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeoutOrNull

fun main(args: Array<String>) {
    initKoin()
    // Covers window close, Cmd+Q and SIGTERM alike.
    Runtime.getRuntime().addShutdownHook(
        Thread { runBlocking { withTimeoutOrNull(TDLIB_CLOSE_TIMEOUT) { closeTelegramClient() } } }
    )
    nucleusApplication(args) {
        KotogramTheme {
            MaterialDecoratedWindow(
                onCloseRequest = ::exitApplication,
                title = "Kotogram"
            ) {
                MaterialTitleBar {
                    Text(
                        text = "Kotogram",
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .padding(horizontal = 12.dp)
                    )
                }
                App()
            }
        }
    }
}

private val TDLIB_CLOSE_TIMEOUT = 3.seconds
