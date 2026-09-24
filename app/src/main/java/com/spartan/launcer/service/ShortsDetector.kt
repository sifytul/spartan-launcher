package com.spartan.launcer.service

import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import com.spartan.launcer.data.ShortVideoPackages
import com.spartan.launcer.domain.blocking.AppBlocker

/**
 * Pure, unit-testable heuristic for whether a node snapshot is the YouTube
 * Shorts vertical player. Precision matters more than recall: a false positive
 * blocks a whole video session, so we only fire on signals specific to Shorts.
 */
object ShortsHeuristics {

    private val EXACT_SHORTS = Regex("^shorts$", RegexOption.IGNORE_CASE)

    fun matches(
        texts: Collection<String>,
        contentDescriptions: Collection<String>
    ): Boolean {
        val exactShorts = texts.any { EXACT_SHORTS.matches(it.trim()) } ||
            contentDescriptions.any { EXACT_SHORTS.matches(it.trim()) }
        if (exactShorts) return true
        // The swipe-to-next/previously a11y actions only exist on the Shorts
        // player, not on regular video or browse views.
        return contentDescriptions.any {
            it.contains("Next video on YouTube", ignoreCase = true) ||
                it.contains("Previous video on YouTube", ignoreCase = true)
        }
    }
}

/**
 * Listens for the accessibility events that reveal the Shorts player and
 * reports visibility to [AppBlocker], which enforces the "Shorts only" block.
 * The window hierarchy is only inspected at most every [RESCAN_INTERVAL_MS]
 * and never ~visited beyond [NODE_CAP] nodes; nothing is stored beyond the
 * current boolean.
 */
class ShortsDetector(private val appBlocker: AppBlocker) {

    private var lastScanMs = 0L
    private var visited = 0

    fun onAccessibilityEvent(event: AccessibilityEvent?, root: AccessibilityNodeInfo?) {
        val e = event ?: return
        val packageName = e.packageName?.toString()
        if (packageName != ShortVideoPackages.YOUTUBE) {
            // Leave of absence: Shorts can only be on screen while YouTube is.
            appBlocker.onShortsVisibility(false)
            return
        }
        if (!appBlocker.isShortsOnlyEnabled()) {
            appBlocker.onShortsVisibility(false)
            return
        }
        if (e.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED || shouldRescan()) {
            scan(root)
        }
    }

    private fun shouldRescan(): Boolean =
        System.currentTimeMillis() - lastScanMs > RESCAN_INTERVAL_MS

    private fun scan(root: AccessibilityNodeInfo?) {
        appBlocker.onShortsVisibility(false)
        if (root == null) return
        lastScanMs = System.currentTimeMillis()
        val texts = ArrayList<String>()
        val descriptions = ArrayList<String>()
        visited = 0
        walk(root, texts, descriptions)
        val visible = ShortsHeuristics.matches(texts, descriptions)
        root.recycle()
        appBlocker.onShortsVisibility(visible)
    }

    private fun walk(
        node: AccessibilityNodeInfo,
        texts: MutableList<String>,
        descriptions: MutableList<String>
    ) {
        if (visited >= NODE_CAP) return
        visited++
        node.text?.toString()?.let { texts += it }
        node.contentDescription?.toString()?.let { descriptions += it }
        for (i in 0 until node.childCount) {
            val child = node.getChild(i) ?: continue
            walk(child, texts, descriptions)
            child.recycle()
        }
    }

    companion object {
        private const val RESCAN_INTERVAL_MS = 1_500L
        private const val NODE_CAP = 400
    }
}