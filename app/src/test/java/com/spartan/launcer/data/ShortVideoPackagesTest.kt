package com.spartan.launcer.data

import org.junit.Assert.assertTrue
import org.junit.Test

class ShortVideoPackagesTest {

    @Test
    fun defaultListIncludesMajorShortVideoApps() {
        val defaults = ShortVideoPackages.DEFAULT_DISTRACTOR_PACKAGES
        assertTrue(defaults.contains("com.zhiliaoapp.musically"))
        assertTrue(defaults.contains("com.instagram.android"))
        assertTrue(defaults.contains("com.snapchat.android"))
        assertTrue(defaults.contains(ShortVideoPackages.YOUTUBE))
    }

    @Test
    fun defaultListNowCoversFeedApps() {
        val defaults = ShortVideoPackages.DEFAULT_DISTRACTOR_PACKAGES
        assertTrue(defaults.contains("com.facebook.katana"))
        assertTrue(defaults.contains("com.twitter.android"))
        assertTrue(defaults.contains("com.reddit.frontpage"))
    }

    @Test
    fun defaultListIsNonEmpty() {
        assertTrue(ShortVideoPackages.DEFAULT_DISTRACTOR_PACKAGES.isNotEmpty())
    }
}