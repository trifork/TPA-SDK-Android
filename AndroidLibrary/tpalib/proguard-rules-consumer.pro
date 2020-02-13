-keep class io.tpa.tpalib.lifecycle.TpaNoCheck
-keep class io.tpa.tpalib.lifecycle.ActivityEventListener
-keep class io.tpa.tpalib.lifecycle.ApplicationEventListener
-keep class io.tpa.tpalib.UpdateAvailableCallback
-keep class io.tpa.tpalib.CheckUpdateMonitor
-keep class io.tpa.tpalib.protobuf.** { *; }

-dontwarn **ActivityLifecycleCallbacks, io.tpa.tpalib.android.R*, io.tpa.tpalib.CheckUpdateMonitor*