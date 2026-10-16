import androidx.compose.ui.window.ComposeUIViewController
import com.xbot.kotogram.ui.App
import platform.UIKit.UIViewController

// Called from Swift as Main_iosKt.MainViewController().
@Suppress("ktlint:standard:function-naming", "FunctionNaming")
fun MainViewController(): UIViewController = ComposeUIViewController { App() }
