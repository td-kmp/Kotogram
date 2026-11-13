package com.xbot.kotogram.feature.connection.impl.platform

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import platform.Foundation.NSNotificationCenter
import platform.Foundation.NSOperationQueue
import platform.UIKit.UIApplication
import platform.UIKit.UIApplicationDidBecomeActiveNotification
import platform.UIKit.UIApplicationDidEnterBackgroundNotification
import platform.UIKit.UIApplicationState
import platform.UIKit.UIApplicationWillResignActiveNotification

@Composable
actual fun isAppInForeground(): Boolean {
    var active by remember {
        mutableStateOf(UIApplication.sharedApplication.applicationState == UIApplicationState.UIApplicationStateActive)
    }
    DisposableEffect(Unit) {
        val center = NSNotificationCenter.defaultCenter
        val changes = [
            UIApplicationDidBecomeActiveNotification to true,
            UIApplicationWillResignActiveNotification to false,
            UIApplicationDidEnterBackgroundNotification to false,
        ]
        val observers = changes.map { (name, value) ->
            center.addObserverForName(name, null, NSOperationQueue.mainQueue) { active = value }
        }
        onDispose { observers.forEach(center::removeObserver) }
    }
    return active
}
