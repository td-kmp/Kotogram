import SwiftUI

/// The iOS app is a thin shim: every screen and all state live in the Compose Multiplatform
/// `SharedUI` framework. Swift only owns the app lifecycle and the window it hands to Compose.
@main
struct KotogramApp: App {
    var body: some Scene {
        WindowGroup {
            ComposeView()
                .ignoresSafeArea(.all)
        }
    }
}
