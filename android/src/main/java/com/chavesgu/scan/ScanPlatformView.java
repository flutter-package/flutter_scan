package com.chavesgu.scan;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.util.Log;
import android.view.View;

import java.util.Map;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding;
import io.flutter.plugin.common.BinaryMessenger;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.PluginRegistry;
import io.flutter.plugin.platform.PlatformView;

public class ScanPlatformView implements PlatformView, MethodChannel.MethodCallHandler, ScanViewNew.CaptureListener {
    private MethodChannel channel;
    private Context context;
    private Activity activity;
    private ActivityPluginBinding activityPluginBinding;
    private ParentView parentView;
//    private ScanView scanView;
    private ScanViewNew scanViewNew;
    private ScanDrawView scanDrawView;
    private boolean flashlight;

    ScanPlatformView(@NonNull BinaryMessenger messenger, @NonNull Context context, @NonNull Activity activity, ActivityPluginBinding activityPluginBinding, int viewId, @Nullable Map<String, Object> args) {
        channel = new MethodChannel(messenger, "chavesgu/scan/method_"+viewId);
        channel.setMethodCallHandler(this);
        this.context = context;
        this.activity = activity;
        this.activityPluginBinding = activityPluginBinding;
        initForBinding(args);
    }

    private void initForBinding(Map<String, Object> args) {
        this.scanViewNew = new ScanViewNew(context, activity, activityPluginBinding,  args);
        this.scanViewNew.setCaptureListener(this);

        this.scanDrawView = new ScanDrawView(context, activity, args);

        this.parentView = new ParentView(context);
        this.parentView.addView(this.scanViewNew);
        this.parentView.addView(this.scanDrawView);
    }

    @Override
    public View getView() {
        return this.parentView;
    }

    @Override
    public void dispose() {
        this.scanViewNew.dispose();
    }

    @Override
    public void onMethodCall(@NonNull MethodCall call, @NonNull MethodChannel.Result result) {
        if (call.method.equals("resume")) {
            resume();
        } else if (call.method.equals("pause")) {
            pause();
        } else if (call.method.equals("toggleTorchMode")) {
            toggleTorchMode();
        }
    }

    private void resume() {
        this.scanViewNew.resume();
        this.scanDrawView.resume();
    }
    private void pause() {
        this.scanViewNew.pause();
        this.scanDrawView.pause();
    }
    private void toggleTorchMode() {
        this.scanViewNew.toggleTorchMode(!flashlight);
        flashlight = !flashlight;
    }

    @Override
    public void onCapture(String text) {
        channel.invokeMethod("onCaptured", text);
        pause();
    }
}
