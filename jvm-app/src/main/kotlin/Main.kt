import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.rememberWindowState
import com.xbot.kotogram.core.designsystem.window.KotogramWindow
import com.xbot.kotogram.shared.App
import com.xbot.kotogram.shared.di.closeAccounts
import com.xbot.kotogram.shared.di.initKoin
import dev.nucleusframework.application.nucleusApplication
import dev.nucleusframework.window.material.MaterialTitleBar
import kotlinx.coroutines.runBlocking
import org.koin.core.context.stopKoin

// As Telegram for macOS: 700×550 at first launch, never smaller than 380×550, no maximum.
private val InitialWindowSize = DpSize(700.dp, 550.dp)
private val MinimumWindowSize = DpSize(380.dp, 550.dp)

fun main(args: Array<String>) {
    initKoin()
    Runtime.getRuntime().addShutdownHook(
        Thread {
            runBlocking { closeAccounts() }
            stopKoin()
        }
    )
    nucleusApplication(args) {
        KotogramWindow(
            onCloseRequest = ::exitApplication,
            state = rememberWindowState(
                position = WindowPosition(Alignment.Center),
                size = InitialWindowSize
            ),
            title = "Kotogram",
            minimumSize = MinimumWindowSize
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
