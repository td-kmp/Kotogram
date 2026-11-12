# Spoiler dust: the fastest way to draw it on each target (2026-11-12)

Asked by the developer after the first native take (GL on a `SurfaceControl`, Android 13 and later): review it, find the fastest drawing for every target, keep the common API a modifier. What the code does now is in [design/designsystem.md](../design/designsystem.md#components); this file is the reasoning and the numbers.

Marks: **measured** (on this Mac, 2026-11-12), **read** (in the named source, the same day), **recalled** (AOSP's HWUI and Skia as remembered: their C++ isn't on this Mac, so check before building on it), **not checked**.

## What costs

A frame of dust has two costs: drawing about 0.35 speckles a square point, and whatever else the frame draws again because the dust changed. The second was the larger one on every platform looked at, so the fastest dust is the one that makes the least of its surroundings draw again. The first was larger than expected too: Skia took the speckles one oval at a time.

## Android

### Why Compose's dust made the whole window draw

- **Read** (ui-android 1.12.1, `GraphicsLayerOwnerLayer.invalidate`): a layer that has to be recorded again calls `ownerView.invalidate()`. `AndroidComposeView` then records its own display list again. **Recalled**: HWUI damages a node with a new display list over its whole bounds (`RenderNode::pushStagingDisplayListChanges` → `damageSelf`), which for that view is the window.
- **Measured**: with `debug.hwui.show_dirty_regions` the frame's damage was `[0, 0, 1080, 2400]`, all of the window, for one spoiler of 828 × 122 pixels.

### What HWUI offers instead

A `RenderNode` is retained: recorded again on its own, it damages only itself, and the frame draws only that rect (this is how a `View` that calls `invalidate()` has always been cheap). Two things stood between that and Compose:

- **The damage's bounds.** HWUI keeps damage inside a node only if the node clips to its bounds (**recalled**: `RenderProperties::getClipDamageToBounds`, `DamageAccumulator::applyRenderNodeTransform`). Compose's `GraphicsLayer` with `clip = true` always sets an outline and so `clipToOutline`, never `clipToBounds` (**read**: `GraphicsLayerV29.applyClip`, `AndroidGraphicsLayer.configureOutlineAndClip`). **Measured**: dust in a Compose `GraphicsLayer` recorded outside the draw pass still damaged the whole window. A platform `RenderNode` of our own, which clips to its bounds by default, damaged `[218, 540, 1046, 662]`: 3.9 % of the window.
- **A frame without Compose.** Something has to ask the window for a frame without invalidating `AndroidComposeView`. A 1 × 1 view in that view's `ViewOverlay` does: its `invalidate()` reaches `ViewRootImpl` through `onDescendantInvalidated`, which marks the ancestors dirty but not invalidated, so their display lists stay (**read**: `ViewGroup.onDescendantInvalidated` and `ViewOverlay.OverlayViewGroup.onDescendantInvalidated` in the SDK's sources of API 37; a parent with a software layer is invalidated in full there, which only loses the saving). Compose keeps a 1 × 1 view of its own in `AndroidComposeView` for frame-rate votes, so a helper view there has a precedent (**read**: `AndroidComposeView.frameRateCategoryView`).

So the dust stays in the scene, where its row's alpha, clips, transforms and whatever is drawn over it or blurred from it all apply, and a frame costs the dust's rect.

### Numbers

Emulator (`Resizable_Experimental`, Android 17, API 37, 1080 × 2400, 60 Hz, no window), release build with R8, a stand activity (not kept) with 16 `ChatListItem` rows and a two-line spoiler in row 2 (and row 9 or 3). HWUI was switched to `skiavk` (`debug.hwui.renderer`): the emulator's GL has no `EGL_EXT_buffer_age`, so `skiagl` can't draw part of a buffer there. Its Vulkan is lavapipe, a software GPU: GPU work shows as CPU of the emulator's process on the Mac, and every figure is good for comparing rows, not as a phone's. Ten seconds each, CPU per thread from `/proc/<pid>/task/*/stat`, as a share of one core.

| Dust | App | RenderThread | UI thread | GL thread | SurfaceFlinger | Emulator on the Mac |
|---|---|---|---|---|---|---|
| none | 0.1 | 0 | 0 | | 0.3 | 16 |
| before: Compose, points, 1 spoiler | 88.5 | 82.7 | 4.6 | | 8.3 | 825 |
| before: Compose, points, 2 spoilers | 89.9 | 87.3 | 2.2 | | 7.1 | 800 |
| before: `SurfaceControl` and GL, 1 spoiler | 12.4 | 0 | 0 | 12.1 | 4.2 | 539 |
| before: `SurfaceControl` and GL, 2 spoilers | 36.0 | 0 | 0 | 35.9 | 4.6 | 639 |
| own `RenderNode`, points, 1 spoiler | 54.1 | 52.3 | 1.6 | | 5.4 | 600 |
| sprites, whole window drawn, 1 spoiler | 35.3 | 33.2 | 1.9 | | 5.8 | 664 |
| **now: own `RenderNode`, sprites, 1 spoiler** | **17.7** | 16.2 | 1.2 | | 5.0 | 455 |
| **now, 2 spoilers, rows 2 and 3** | **22.6** | 20.8 | 1.7 | | 4.4 | 482 |
| **now, 2 spoilers, rows 2 and 9** | **32.1** | 30.2 | 1.8 | | 5.3 | 562 |

Compose's dust with two spoilers dropped to 52 frames a second with 22 % of them janky; every other row held the display's 60 with none or under 1 %. The three rows marked "now" are the code as it is in the tree; the two between "before" and "now" were steps on the way.

Read from the table:
- The window's redraw was half of the old cost and the ovals the other half: either fix alone leaves 35 or 54 %.
- The GL thread is cheapest for one spoiler and loses with two: a surface a spoiler means an `eglMakeCurrent` and an `eglSwapBuffers` each, every frame.
- Two spoilers far apart cost more than two neighbours: HWUI keeps one damage rect a frame, so everything between them draws again (two neighbours damaged `[218, 540, 1046, 861]`, 10.3 % of the window). At worst that is the whole window, which is where Compose's dust started.

Also **measured**, on screenshots of the stand: the dust follows its row through a scroll (a three-second swipe); a translucent box drawn over the row tints the dust under it; a row whose alpha Compose animates takes its dust along (and the window draws whole again while that runs: 42.8 % with two such rows); in the background the process takes 0.0 % and draws no frames.

On the emulator's own `skiagl`, which has no buffer age and so draws every frame whole, the new dust still runs right: 46.5 % at 63 frames a second, where Compose's points took 95.5 % and reached 27.

**Not checked**: a real phone (the developer's SM-G990E wasn't connected), Android 10 to 16, a GL-backed HWUI that has buffer age, 120 Hz. After the last two changes (the points' path removed, the model moved into the frame's callback and run once a picture) the emulator gave the same: the same damage rect, 18.4 % for one spoiler, 22.9 and 32.0 for two. Also open on Android 15 and later with an adaptive refresh rate: a view votes for the display's rate when it draws, a small one for less than a large one (**recalled**), and the only view invalidated here is 1 × 1, so such a display may hold the dust at 60 Hz or under; `View.setRequestedFrameRate` (API 35) on that view would pin it if that shows.

### What was turned down

- **`SurfaceControl` layers with a GL thread** (the first native take). Fastest only for a single spoiler, and outside the scene: at rest the dust lay over anything Compose drew above the spoiler, ignored an ancestor's alpha and would not be blurred under a bar. Switching to Compose's dust whenever the window drew cost a state machine with four modes, a dust frame skipped out of every eight to probe for rest, Android 13, and a thread that waited up to 0.5 s for EGL at the first spoiler. Never seen on a phone.
- **A fragment shader** (AGSL, SkSL). A pixel would have to test every speckle that can reach it: within 10.55 points, about 120 of them at 0.35 a square point (**calculated**). The speckles are 1.1 points wide: drawing them as geometry touches a few pixels each. And in Compose a shader's time uniform is a layer drawn again, so the window's redraw stays.
- **`android.graphics.Mesh`** (Android 14, a vertex shader inside HWUI). Would move the model to the GPU; the model takes about 50 µs a frame on the JVM for 2000 speckles (measured earlier), so there is little to win, and it needs API 34.
- **A frame-rate cap.** Not done, at the developer's word. For the record: a speckle moves 10 points a second at most, 0.25 of a pixel a frame at 120 Hz on a 3× screen and 0.5 at 60.

## One draw call (every platform)

**Recalled** (Skia's `SkDraw::drawDevicePoints`, which Ganesh's `Device::drawPoints` falls back to for anything but a hairline): points with a width are drawn one by one, a `drawOval` each for round caps. 2000 speckles were 2000 ovals a frame, on every platform, whatever the batching by alpha did.

Now the speckles are quads over one small sprite of a dot, sent in one `drawVertices` with a colour a vertex (`extensions/CanvasExt.drawSprites`): one Skia op, the alpha exact for every speckle (the 16 levels are gone), and linear sampling moves a dot between pixels as `CAEmitterLayer` moves its sprite. **Measured** on Android with the whole window still drawn: RenderThread 33.2 % against 82.7.

- Compose's `ImageShader` samples the nearest pixel on Skia (**read**: `SkiaShader.skiko.kt`, `makeShader` with `SamplingMode.DEFAULT`), so the Skia sprite's paint is built with `SamplingMode.LINEAR` (`extensions/ImageBitmapExt.toSpritePaint`).
- A hardware canvas ignores `drawVertices` before Android 10 (**recalled**: Android's "Hardware acceleration" guide lists it from API 29, and N's `RecordingCanvas` drops the call), and there is no public `RenderNode` before it either. The first cut kept `drawPoints` and the window's redraw for Android 7 to 9, the slowest drawing for the slowest phones and never run on one. The developer raised the app's minimum to Android 10 instead the same night (5.5 % of devices are on 7 to 9 by Google's distribution data; Telegram's own client starts at Android 6), and that path is gone.

## What Telegram for Android does

**Read** in its sources (13.2.2, `ui/Components/spoilers`), because "Telegram draws spoilers with a `TextureView`" is half of it:

- **Text** (`SpoilerEffect`, `SpoilerEffectBitmapFactory`): no `TextureView`. One shared tile, an `ALPHA_8` bitmap 100 to 150 dp square. A background queue moves a hundred small particle systems and draws them into it with `drawPoints` in three levels of alpha, at most once in 32 ms; every spoiler is then one `drawRect` with a repeating `BitmapShader`, invalidated at 60 frames a second at most.
- **Media** (`SpoilerEffect2`): one `TextureView` a screen, up to 1280 pixels square, never drawn itself (its container's `drawChild` returns false). A GL thread moves 500 to 10000 particles with transform feedback; every cell draws that texture into its own canvas with `textureView.draw(canvas)` and is invalidated every frame.
- Both stay cheap for one reason: a cell is a `View`, and a `View` that invalidates itself damages only itself. That is the property the `RenderNode` gives back to Compose. What goes inside it, vertices, a tile or a GL texture, is a second and independent choice; a texture from a GL thread starts to pay with ten times the speckles, which is media, not text.

## iOS and macOS

- **Read** (Compose Multiplatform 1.12.1, `MetalRedrawer.ios.kt`): a frame records the whole scene into one picture and draws that picture into the next drawable. Nothing is retained between frames, so dust in the scene costs a frame of the whole screen however little of it changed. That frame is cheaper now by the ovals.
- The only way out of the scene's frame is a layer the system draws by itself: `CAEmitterLayer`, as Telegram's `InvisibleInkDustNode`, costs the app nothing while it runs. It has the limits the `SurfaceControl` take had, since it is not in the scene: nothing Compose draws later covers an overlay, an ancestor's alpha doesn't reach it, and it follows a scroll a frame late unless Compose presents with the transaction. **Read** in the same file: with interop views that moved, the frame is presented with `presentsWithTransaction` and the main thread waits in `waitUntilScheduled`.
- **Measured** on the simulator (iPhone 18 Pro, iOS 27.2, a debug framework, so Kotlin's share is inflated; CPU of the app's process over ten seconds, the same stand of 16 rows): no spoiler 0.2 % of a core; one spoiler 13.7, 15.8 and 20.7 % with sprites against 16.6 and 27.0 with points (separate runs, and that noisy); two spoilers 18.7 against 28.0. The same stand with a `CAEmitterLayer` laid over the idle scene (2000 speckles a second, one-second lives, a sublayer of the Compose view at `zPosition` 1000; not Telegram's look, only its load): the app 0.4 %, and the simulator's render side (`backboardd`, `SimRenderServer`, `SimMetalHost` together) 23 % against 49 for the scene's dust and 1 for a screen at rest. So an emitter takes the app out of the frame, as expected, and roughly halves what the system does; a phone's render server draws on the GPU, so its share there is **not checked**.
- **Where an iOS frame goes** (**measured**: `sample` of the same stand for eight seconds, debug, main thread 11 % busy). 6.7 % is Core Animation committing the frame's surface, nearly all of it IOSurface calls that are XPC in the simulator. 4.2 % is the redrawer: Compose's frame and, inside it, the dust's model at 2.1 %. The render thread takes 4.3 %, half of it the simulator's Metal driver talking to the host. So the scene's raster is not what a frame costs: the scene records only the dust's node again (**read**: Skia's `GraphicsLayer` is Skiko's retained `RenderNode`, `SkiaGraphicsLayer.skiko.kt`), and the rest is what presenting any frame costs. The model ran twice a frame on this path, in the frame's callback and again in the draw; it now runs once, inside the picture: 20.7 % became 16.9 and 17.2 on the simulator. Nothing else in the scene's path is ours to cut: fewer frames or no frames is what is left.
- **Not checked**: a release build on an iPhone at 120 Hz. The simulator says the emitter is worth having for a screen at rest; the phone has to say how much the scene's dust costs there before the switching is written. An emitter should then be a layer for a resting screen only, with the scene's dust for everything that moves, fades or is covered: the lesson of the Android take.
- macOS is the same engine and the same choice, through `appkit` and Nucleus's `NativeView`. A desktop GPU draws a 700 × 550 window in well under a millisecond (**not checked** here), so nothing is planned.

## Windows, Linux and the web

The scene's dust, with the one draw call. Skiko draws the window's picture whole on each (**not checked** today; same engine as iOS). On the web a second `<canvas>` over Compose's, drawing points with WebGL 2, would be the layer outside the scene; the same limits apply and nothing asks for it yet.

## The seam

Common code owns the model and the modifier. A platform answers one question: can a picture drawn in this node be replaced between the scene's draws?

- `Modifier.invisibleInk(color) { areas }`: the API, a modifier, unchanged.
- `component/platform/FrameLayer`: `draw` puts a picture into the node, `update` replaces it without the node being drawn again and says whether it could. `SceneFrameLayer` (common) can't: the node is invalidated every frame. Android answers with a `RenderNode`.
- An emitter on iOS would not come through this seam: its picture isn't the scene's. It would be a second, resting renderer beside it.
