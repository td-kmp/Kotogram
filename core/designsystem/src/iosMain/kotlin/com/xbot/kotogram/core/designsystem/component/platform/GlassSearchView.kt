package com.xbot.kotogram.core.designsystem.component.platform

import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSAttributedString
import platform.Foundation.create
import platform.UIKit.NSFontAttributeName
import platform.UIKit.NSForegroundColorAttributeName
import platform.UIKit.NSLayoutConstraint
import platform.UIKit.UIAction
import platform.UIKit.UIButton
import platform.UIKit.UIButtonConfiguration
import platform.UIKit.UIColor
import platform.UIKit.UIControlEventEditingChanged
import platform.UIKit.UIControlEventEditingDidBegin
import platform.UIKit.UIControlEventEditingDidEndOnExit
import platform.UIKit.UICornerConfiguration
import platform.UIKit.UIFont
import platform.UIKit.UIGlassContainerEffect
import platform.UIKit.UIGlassEffect
import platform.UIKit.UIGlassEffectStyle
import platform.UIKit.UIImage
import platform.UIKit.UIReturnKeyType
import platform.UIKit.UISearchTextField
import platform.UIKit.UITextBorderStyle
import platform.UIKit.UITextFieldViewMode
import platform.UIKit.UIView
import platform.UIKit.UIVisualEffectView
import platform.UIKit.labelColor

// iOS 26's search in a toolbar: a glass capsule with UIKit's search field and, while searching, a glass cancel
// button. One glass container holds both, so they merge and part as the button comes and goes.
@OptIn(ExperimentalForeignApi::class)
internal class GlassSearchView(private val onTextChange: (String) -> Unit) {
    private val field = UISearchTextField()
    private val capsule = UIVisualEffectView(effect = capsuleEffect())
    private val cancel = UIButton.buttonWithConfiguration(
        configuration = cancelConfiguration(UIColor.labelColor),
        primaryAction = UIAction.actionWithHandler { cancel() }
    )
    val root = UIVisualEffectView(effect = UIGlassContainerEffect().apply { spacing = SPACING })
    private val capsuleToEdge = capsule.trailingAnchor.constraintEqualToAnchor(root.contentView.trailingAnchor)
    private val capsuleToCancel =
        capsule.trailingAnchor.constraintEqualToAnchor(cancel.leadingAnchor, constant = -SPACING)
    private var active = false

    init {
        field.borderStyle = UITextBorderStyle.UITextBorderStyleNone
        field.backgroundColor = UIColor.clearColor
        field.returnKeyType = UIReturnKeyType.UIReturnKeySearch
        field.clearButtonMode = UITextFieldViewMode.UITextFieldViewModeWhileEditing
        capsule.cornerConfiguration = UICornerConfiguration.capsuleConfiguration()
        cancel.alpha = 0.0
        cancel.hidden = true
        listOf<UIView>(field, capsule, cancel).forEach { it.translatesAutoresizingMaskIntoConstraints = false }
        capsule.contentView.addSubview(field)
        root.contentView.addSubview(capsule)
        root.contentView.addSubview(cancel)
        val content = root.contentView
        NSLayoutConstraint.activateConstraints(
            listOf(
                capsule.leadingAnchor.constraintEqualToAnchor(content.leadingAnchor),
                capsule.topAnchor.constraintEqualToAnchor(content.topAnchor),
                capsule.bottomAnchor.constraintEqualToAnchor(content.bottomAnchor),
                capsuleToEdge,
                cancel.trailingAnchor.constraintEqualToAnchor(content.trailingAnchor),
                cancel.topAnchor.constraintEqualToAnchor(content.topAnchor),
                cancel.bottomAnchor.constraintEqualToAnchor(content.bottomAnchor),
                cancel.widthAnchor.constraintEqualToAnchor(cancel.heightAnchor),
                field.leadingAnchor.constraintEqualToAnchor(capsule.contentView.leadingAnchor, constant = INSET),
                field.trailingAnchor.constraintEqualToAnchor(capsule.contentView.trailingAnchor, constant = -INSET),
                field.topAnchor.constraintEqualToAnchor(capsule.contentView.topAnchor),
                field.bottomAnchor.constraintEqualToAnchor(capsule.contentView.bottomAnchor)
            )
        )
        field.addAction(
            UIAction.actionWithHandler { onTextChange(field.text.orEmpty()) },
            forControlEvents = UIControlEventEditingChanged
        )
        field.addAction(
            UIAction.actionWithHandler { setActive(true) },
            forControlEvents = UIControlEventEditingDidBegin
        )
        // An action for this event is what makes the return key put the keyboard away.
        field.addAction(UIAction.actionWithHandler {}, forControlEvents = UIControlEventEditingDidEndOnExit)
    }

    @OptIn(BetaInteropApi::class)
    fun update(
        text: String,
        placeholder: String,
        font: UIFont,
        textColor: UIColor,
        placeholderColor: UIColor,
        tintColor: UIColor
    ) {
        if (field.text.orEmpty() != text) field.text = text
        field.font = font
        field.textColor = textColor
        field.tintColor = tintColor
        field.leftView?.tintColor = placeholderColor
        field.attributedPlaceholder = NSAttributedString.create(
            string = placeholder,
            attributes = mapOf<Any?, Any?>(
                NSFontAttributeName to font,
                NSForegroundColorAttributeName to placeholderColor
            )
        )
        cancel.configuration = cancelConfiguration(textColor)
        if (text.isNotEmpty()) setActive(true)
    }

    private fun cancel() {
        field.text = ""
        onTextChange("")
        field.resignFirstResponder()
        setActive(false)
    }

    private fun setActive(value: Boolean) {
        if (active == value) return
        active = value
        cancel.hidden = false
        UIView.animateWithDuration(
            ANIMATION_SECONDS,
            animations = {
                if (value) {
                    capsuleToEdge.active = false
                    capsuleToCancel.active = true
                } else {
                    capsuleToCancel.active = false
                    capsuleToEdge.active = true
                }
                cancel.alpha = if (value) 1.0 else 0.0
                root.layoutIfNeeded()
            },
            completion = { cancel.hidden = !active }
        )
    }
}

private fun capsuleEffect(): UIGlassEffect =
    UIGlassEffect.effectWithStyle(UIGlassEffectStyle.UIGlassEffectStyleRegular).apply { interactive = true }

private fun cancelConfiguration(color: UIColor): UIButtonConfiguration =
    UIButtonConfiguration.glassButtonConfiguration().apply {
        image = UIImage.systemImageNamed("xmark")
        baseForegroundColor = color
    }

private const val SPACING = 8.0

private const val INSET = 6.0

private const val ANIMATION_SECONDS = 0.3
