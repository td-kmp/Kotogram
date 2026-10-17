import androidx.compose.ui.window.ComposeUIViewController
import com.xbot.kotogram.shared.App
import com.xbot.kotogram.shared.di.initKoin
import org.koin.mp.KoinPlatform
import platform.UIKit.UIViewController

// Called from Swift as Main_iosKt.MainViewController().
@Suppress("ktlint:standard:function-naming", "FunctionNaming")
fun MainViewController(): UIViewController {
    if (KoinPlatform.getKoinOrNull() == null) initKoin()
    return ComposeUIViewController { App() }
}
