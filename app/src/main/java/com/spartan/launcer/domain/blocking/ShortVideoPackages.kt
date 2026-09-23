package com.spartan.launcer.domain.blocking

/**
 * Known short-form video apps. There is no platform API to separate a
 * vertical-feed tab (YouTube Shorts, Instagram Reels, Snapchat Spotlight)
 * from its host app, so enabling the "Block short videos" preset blocks the
 * whole host app.
 */
object ShortVideoPackages {

    val DISTRACTOR_PACKAGES: Set<String> = setOf(
        "com.ss.android.ugc.aweme", // TikTok (China)
        "com.zhiliaoapp.musically", // TikTok (international)
        "com.ss.android.ugc.trill", // TikTok (regional builds)
        "com.instagram.android", // Instagram / Reels
        "com.snapchat.android", // Snapchat / Spotlight
        "com.google.android.youtube", // YouTube incl. Shorts
        "com.kuaishou.nebula", // Kwai
        "com.smile.gifmaker" // Kuaishou (China)
    )
}