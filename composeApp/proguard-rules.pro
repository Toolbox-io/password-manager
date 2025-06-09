# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html
#

-keepattributes LineNumberTable,SourceFile

# Hide original package names for classes
-repackageclasses

# Remove all log calls
-assumenosideeffects class android.util.Log {
    public static int d(...);
    public static int w(...);
    public static int e(...);
    public static int wtf(...);
    public static int i(...);
    public static int v(...);
}
-assumenosideeffects class java.lang.System {
    public static final java.io.PrintStream out;
}