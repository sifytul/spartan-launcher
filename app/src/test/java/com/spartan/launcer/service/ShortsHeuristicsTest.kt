package com.spartan.launcer.service

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ShortsHeuristicsTest {

    @Test
    fun exactShortsLabelHits() {
        assertTrue(ShortsHeuristics.matches(texts = listOf("Shorts"), contentDescriptions = emptyList()))
        assertTrue(ShortsHeuristics.matches(texts = emptyList(), contentDescriptions = listOf("shorts")))
        assertTrue(ShortsHeuristics.matches(texts = listOf("  Shorts  "), contentDescriptions = emptyList()))
    }

    @Test
    fun shortWordInsideLongLabelDoesNotHit() {
        assertFalse(ShortsHeuristics.matches(texts = listOf("Watch short films"), contentDescriptions = emptyList()))
        assertFalse(ShortsHeuristics.matches(texts = emptyList(), contentDescriptions = listOf("My shorts playlist")))
    }

    @Test
    fun shortsPlayerNavigationDescriptionsHit() {
        assertTrue(
            ShortsHeuristics.matches(
                texts = emptyList(),
                contentDescriptions = listOf("Next video on YouTube")
            )
        )
        assertTrue(
            ShortsHeuristics.matches(
                texts = emptyList(),
                contentDescriptions = listOf("Previous video on YouTube")
            )
        )
    }

    @Test
    fun regularVideoControlDoesNotHit() {
        assertFalse(
            ShortsHeuristics.matches(
                texts = emptyList(),
                contentDescriptions = listOf("Play video", "Like this video", "Share")
            )
        )
        assertFalse(ShortsHeuristics.matches(texts = emptyList(), contentDescriptions = emptyList()))
    }
}