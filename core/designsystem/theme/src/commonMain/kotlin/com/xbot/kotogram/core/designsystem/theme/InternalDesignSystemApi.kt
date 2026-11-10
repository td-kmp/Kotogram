package com.xbot.kotogram.core.designsystem.theme

@RequiresOptIn(message = "Only the design system's own modules use this.", level = RequiresOptIn.Level.ERROR)
@Retention(AnnotationRetention.BINARY)
@Target(AnnotationTarget.CLASS, AnnotationTarget.PROPERTY)
annotation class InternalDesignSystemApi
