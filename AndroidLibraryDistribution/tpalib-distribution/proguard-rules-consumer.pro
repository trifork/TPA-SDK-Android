-keep class io.tpa.tpalib.distribution.UpdateAvailableCallback
-keep class io.tpa.tpalib.distribution.CheckUpdateMonitor
-keepclassmembers class io.tpa.tpalib.distribution.CheckUpdateMonitor {
    public <init>(...);
    public void onActivityCreated(android.os.Bundle);
    public void onActivitySaveInstanceState(android.app.Activity, android.os.Bundle);
    public void onActivityResumed(android.app.Activity);
    public void checkForUpdates(android.app.Activity);
    public void checkForUpdateAvailable(android.app.Activity, io.tpa.tpalib.distribution.UpdateAvailableCallback);
}