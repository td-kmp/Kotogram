import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.ComposeViewport
import com.xbot.kotogram.core.designsystem.KotogramTheme
import com.xbot.kotogram.shared.App
import com.xbot.kotogram.shared.di.initKoin
import kotlin.js.ExperimentalWasmJsInterop
import kotlinx.browser.document

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    whenTheOnlyTab(
        onAcquired = {
            initKoin()
            ComposeViewport(document.body!!) { App() }
        },
        onBusy = {
            ComposeViewport(document.body!!) {
                KotogramTheme {
                    Surface(Modifier.fillMaxSize()) {
                        Box(contentAlignment = Alignment.Center) {
                            Text("Kotogram is already open in another tab")
                        }
                    }
                }
            }
        }
    )
}

// Two workers on one IndexedDB would erase each other's files, so only one tab runs TDLib.
@OptIn(ExperimentalWasmJsInterop::class)
@Suppress("UnusedParameter") // Used by the JavaScript below.
private fun whenTheOnlyTab(onAcquired: () -> Unit, onBusy: () -> Unit): Unit = js(
    """navigator.locks.request("kotogram-tdlib", { ifAvailable: true }, (lock) => {
        if (lock) { onAcquired(); return new Promise(() => {}); }
        onBusy();
    })"""
)
