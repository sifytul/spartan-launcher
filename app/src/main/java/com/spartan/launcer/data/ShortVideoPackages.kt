package com.spartan.launcer.data

/**
 * Default short-form video apps. There is no platform API to separate a
 * vertical-feed tab (YouTube Shorts, Instagram Reels, Snapchat Spotlight)
 * from its host app, so blocking a package here covers the whole app.
 *
 * This is the default set shown on first run; users can curate their own
 * list in Settings, which is then persisted and takes precedence.
 */
object ShortVideoPackages {

    const val YOUTUBE = "com.google.android.youtube"

    val DEFAULT_DISTRACTOR_PACKAGES: Set<String> = setOf(
        "com.ss.android.ugc.aweme", // TikTok (China)
        "com.zhiliaoapp.musically", // TikTok (international)
        "com.ss.android.ugc.trill", // TikTok (regional builds)
        "com.instagram.android", // Instagram / Reels
        "com.snapchat.android", // Snapchat / Spotlight
        YOUTUBE, // YouTube incl. Shorts
        "com.kuaishou.nebula", // Kwai
        "com.smile.gifmaker", // Kuaishou (China)
        "com.facebook.katana", // Facebook / Reels
        "com.twitter.android", // X / videos
        "com.reddit.frontpage" // Reddit / videos
    )
}