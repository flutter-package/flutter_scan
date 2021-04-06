package com.chavesgu.scan;

import android.app.Activity;
import android.content.Context;

import java.util.Map;

import androidx.annotation.NonNull;
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding;
import io.flutter.plugin.common.BinaryMessenger;
import io.flutter.plugin.common.MessageCodec;
import io.flutter.plugin.common.PluginRegistry;
import io.flutter.plugin.common.StandardMessageCodec;
import io.flutter.plugin.platform.PlatformView;
import io.flutter.plugin.platform.PlatformViewFactory;

public class ScanViewFactory extends PlatformViewFactory {
    @NonNull private final BinaryMessenger messenger;
    @NonNull private final Context context;
    @NonNull private final Activity activity;
    private ActivityPluginBinding activityPluginBinding;

    ScanViewFactory(@NonNull BinaryMessenger messenger, @NonNull Context context, @NonNull Activity activity, @NonNull ActivityPluginBinding activityPluginBinding) {
        super(StandardMessageCodec.INSTANCE);
        this.messenger = messenger;
        this.context = context;
        this.activity = activity;
        this.activityPluginBinding = activityPluginBinding;
    }

    @Override
    public PlatformView create(Context context, int viewId, Object args) {
        final Map<String, Object> creationParams = (Map<String, Object>) args;
        return new ScanPlatformView(messenger, this.context, this.activity, this.activityPluginBinding, viewId, creationParams);
    }
}
