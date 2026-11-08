import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.rememberWindowState
import com.xbot.kotogram.catalog.Catalog
import com.xbot.kotogram.core.designsystem.window.KotogramWindow
import dev.nucleusframework.application.nucleusApplication
import dev.nucleusframework.window.material.MaterialTitleBar

private const val TITLE = "Kotogram Catalog"
private val InitialWindowSize = DpSize(1100.dp, 760.dp)
private val MinimumWindowSize = DpSize(800.dp, 560.dp)

fun main(args: Array<String>) {
    nucleusApplication(args) {
        val systemDarkTheme = isSystemInDarkTheme()
        var darkTheme by remember {
            mutableStateOf(
                System.getProperty("catalog.dark")?.toBooleanStrictOrNull() ?: systemDarkTheme
            )
        }
        var laptop by remember {
            mutableStateOf(
                System.getProperty("catalog.mobile")?.toBooleanStrictOrNull()?.not() ?: true
            )
        }
        KotogramWindow(
            onCloseRequest = ::exitApplication,
            state = rememberWindowState(
                position = WindowPosition(Alignment.Center),
                size = InitialWindowSize
            ),
            title = TITLE,
            minimumSize = MinimumWindowSize,
            darkTheme = darkTheme,
            dynamicColor = false
        ) {
            MaterialTitleBar {
                Text(
                    text = TITLE,
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(horizontal = 12.dp)
                )
            }
            Catalog(
                darkTheme = darkTheme,
                onDarkThemeChange = { darkTheme = it },
                laptop = laptop,
                onLaptopChange = { laptop = it },
                startPage = System.getProperty("catalog.page")
            )
        }
    }
}
