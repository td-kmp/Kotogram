package com.xbot.kotogram.core.designsystem.appkit.extensions

import kotlinx.cinterop.ptr
import kotlinx.cinterop.reinterpret
import platform.AppKit.NSBitmapImageFileType
import platform.AppKit.NSBitmapImageRep
import platform.AppKit.NSImageCompressionFactor
import platform.AppKit.representationUsingType
import platform.CoreFoundation.CFDictionaryCreateMutable
import platform.CoreFoundation.CFDictionaryRef
import platform.CoreFoundation.CFDictionarySetValue
import platform.CoreFoundation.CFRelease
import platform.CoreFoundation.kCFBooleanTrue
import platform.CoreFoundation.kCFTypeDictionaryKeyCallBacks
import platform.CoreFoundation.kCFTypeDictionaryValueCallBacks
import platform.CoreGraphics.CGImageRef
import platform.CoreGraphics.CGImageRelease
import platform.Foundation.CFBridgingRetain
import platform.Foundation.NSNumber
import platform.Foundation.NSURL
import platform.ImageIO.CGImageSourceCreateThumbnailAtIndex
import platform.ImageIO.CGImageSourceCreateWithURL
import platform.ImageIO.kCGImageSourceCreateThumbnailFromImageAlways
import platform.ImageIO.kCGImageSourceCreateThumbnailWithTransform
import platform.ImageIO.kCGImageSourceThumbnailMaxPixelSize

internal fun NSURL.readJpeg(
    maxSide: Int,
    quality: Double,
): ByteArray? {
    val image = readDownsampled(maxSide) ?: return null
    val jpeg = NSBitmapImageRep(cGImage = image).representationUsingType(
        storageType = NSBitmapImageFileType.NSBitmapImageFileTypeJPEG,
        properties = mapOf<Any?, Any?>(NSImageCompressionFactor to quality),
    )
    CGImageRelease(image)
    return jpeg?.toByteArray()
}

// Workaround: Image I/O scales while it decodes and applies the EXIF orientation only in its thumbnail call.
private fun NSURL.readDownsampled(maxSide: Int): CGImageRef? {
    val url = CFBridgingRetain(this)
    val source = CGImageSourceCreateWithURL(url?.reinterpret(), null)
    CFRelease(url)
    source ?: return null
    val options = thumbnailOptions(maxSide)
    val image = CGImageSourceCreateThumbnailAtIndex(source, 0u, options)
    CFRelease(options)
    CFRelease(source)
    return image
}

private fun thumbnailOptions(maxSide: Int): CFDictionaryRef? {
    val side = CFBridgingRetain(NSNumber(int = maxSide))
    val options = CFDictionaryCreateMutable(
        allocator = null,
        capacity = 0,
        keyCallBacks = kCFTypeDictionaryKeyCallBacks.ptr,
        valueCallBacks = kCFTypeDictionaryValueCallBacks.ptr,
    )
    CFDictionarySetValue(options, kCGImageSourceCreateThumbnailFromImageAlways, kCFBooleanTrue)
    CFDictionarySetValue(options, kCGImageSourceCreateThumbnailWithTransform, kCFBooleanTrue)
    CFDictionarySetValue(options, kCGImageSourceThumbnailMaxPixelSize, side)
    CFRelease(side)
    return options
}
