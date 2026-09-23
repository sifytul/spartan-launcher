# Project-specific R8/ProGuard rules.
#
# Compose, coroutines, DataStore and the AndroidX libraries ship their own
# consumer rules; manifest-declared components are kept automatically.
# Add explicit rules here only if an optimization drops something at runtime.

# Keep accessibility/notification service classes (referenced by name in
# system settings) and their methods the system may call.
-keep class com.spartan.launcer.service.SpartanAccessibilityService { *; }
-keep class com.spartan.launcer.service.NotificationFilterService { *; }