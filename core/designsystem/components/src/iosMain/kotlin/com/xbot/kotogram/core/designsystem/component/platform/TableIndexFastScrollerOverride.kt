package com.xbot.kotogram.core.designsystem.component.platform

import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.unit.dp
import com.xbot.kotogram.core.designsystem.component.FastScrollerOverride
import com.xbot.kotogram.core.designsystem.component.FastScrollerOverrideScope
import com.xbot.kotogram.core.designsystem.extensions.toUIColor
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.convert
import kotlinx.coroutines.launch
import platform.CoreGraphics.CGRectMake
import platform.Foundation.NSIndexPath
import platform.UIKit.UIColor
import platform.UIKit.UIScrollViewContentInsetAdjustmentBehavior
import platform.UIKit.UITableView
import platform.UIKit.UITableViewCell
import platform.UIKit.UITableViewCellSeparatorStyle
import platform.UIKit.UITableViewDataSourceProtocol
import platform.UIKit.UITableViewStyle
import platform.darwin.NSInteger
import platform.darwin.NSObject

// Hack: UIKit draws its section index only inside a table, so an empty table that shows nothing else carries it.
internal object TableIndexFastScrollerOverride : FastScrollerOverride {
    @OptIn(ExperimentalForeignApi::class)
    @Composable
    override fun FastScrollerOverrideScope.FastScroller() {
        val scope = rememberCoroutineScope()
        val currentSections by rememberUpdatedState(sections)
        val currentState by rememberUpdatedState(state)
        val source = remember {
            SectionIndexSource { index ->
                currentSections.getOrNull(index)?.let { section ->
                    scope.launch { currentState.scrollToItem(section.index) }
                }
            }
        }
        val titles = remember(sections) { sections.map { it.title } }
        val tint = style.indexColor.toUIColor()
        OverlayView(
            factory = {
                UITableView(CGRectMake(0.0, 0.0, 0.0, 0.0), UITableViewStyle.UITableViewStylePlain).also { table ->
                    table.backgroundColor = UIColor.clearColor
                    table.separatorStyle = UITableViewCellSeparatorStyle.UITableViewCellSeparatorStyleNone
                    table.scrollEnabled = false
                    table.contentInsetAdjustmentBehavior =
                        UIScrollViewContentInsetAdjustmentBehavior.UIScrollViewContentInsetAdjustmentNever
                    table.sectionIndexBackgroundColor = UIColor.clearColor
                    table.dataSource = source
                }
            },
            properties = GlassProperties,
            modifier = modifier.width(INDEX_WIDTH),
            update = { table ->
                table.sectionIndexColor = tint
                if (source.titles != titles) {
                    source.titles = titles
                    table.reloadData()
                }
            },
        )
    }
}

@OptIn(ExperimentalForeignApi::class)
private class SectionIndexSource(
    private val onSection: (Int) -> Unit,
) : NSObject(),
    UITableViewDataSourceProtocol {
    var titles: List<String> = []

    override fun numberOfSectionsInTableView(tableView: UITableView): NSInteger = titles.size.convert()

    override fun tableView(
        tableView: UITableView,
        numberOfRowsInSection: NSInteger,
    ): NSInteger = 0

    override fun tableView(
        tableView: UITableView,
        cellForRowAtIndexPath: NSIndexPath,
    ): UITableViewCell = UITableViewCell()

    override fun sectionIndexTitlesForTableView(tableView: UITableView): List<*>? = titles

    override fun tableView(
        tableView: UITableView,
        sectionForSectionIndexTitle: String,
        atIndex: NSInteger,
    ): NSInteger {
        onSection(atIndex.toInt())
        return atIndex
    }
}

private val INDEX_WIDTH = 15.dp
