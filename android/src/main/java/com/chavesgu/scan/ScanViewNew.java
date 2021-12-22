package com.chavesgu.scan;

import android.Manifest;
import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.Toast;

import com.google.zxing.DecodeHintType;
import com.google.zxing.qrcode.QRCodeReader;
import com.journeyapps.barcodescanner.BarcodeCallback;
import com.journeyapps.barcodescanner.BarcodeResult;
import com.journeyapps.barcodescanner.BarcodeView;
import com.journeyapps.barcodescanner.CameraPreview;
import com.journeyapps.barcodescanner.DefaultDecoderFactory;
import com.journeyapps.barcodescanner.Size;
import com.journeyapps.barcodescanner.SourceData;
import com.journeyapps.barcodescanner.camera.PreviewCallback;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.util.EnumMap;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding;
import io.flutter.plugin.common.PluginRegistry;

import static android.content.Context.VIBRATOR_SERVICE;
import static android.content.pm.PackageManager.PERMISSION_GRANTED;
import static java.lang.Math.min;

public class ScanViewNew extends BarcodeView implements PluginRegistry.RequestPermissionsResultListener {
    public interface CaptureListener {
        void onCapture(String text);
    }
    private CaptureListener captureListener;

    private String LOG_TAG = "scan";
    private int CAMERA_REQUEST_CODE = 6537;
    private Context context;
    private Activity activity;
    private ActivityPluginBinding activityPluginBinding;
    private Application.ActivityLifecycleCallbacks lifecycleCallback;

    private double vw;
    private double vh;
    private double scale = .7;

    private QrCodeAsyncTask task;

    public ScanViewNew(Context context, Activity activity, @NonNull ActivityPluginBinding activityPluginBinding, @Nullable Map<String, Object> args) {
        super(context, null);

        this.context = context;
        this.activity = activity;
        activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        this.activityPluginBinding = activityPluginBinding;
        activityPluginBinding.addRequestPermissionsResultListener(this);
        this.scale = (double) args.get("scale");

        checkPermission();
    }

    private void start() {
        addListenLifecycle();
        this.setDecoderFactory(new DefaultDecoderFactory(QRCodeDecoder.allFormats, QRCodeDecoder.HINTS, "utf-8", 2));
        this.decodeContinuous(new BarcodeCallback() {
            @Override
            public void barcodeResult(BarcodeResult result) {
                captureListener.onCapture(result.getText());
                Vibrator myVib = (Vibrator) context.getSystemService(VIBRATOR_SERVICE);
                if (myVib != null) {
                    if (Build.VERSION.SDK_INT >= 26) {
                        myVib.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE));
                    } else {
                        myVib.vibrate(50);
                    }
                }
            }
        });
        _resume();
//        final ScanViewNew _this = this;
//        this.getCameraInstance().requestPreview(new PreviewCallback() {
//            @Override
//            public void onPreview(SourceData sourceData) {
//                Log.i("scan test", "preview sourceData"+sourceData);
//                sourceData.setCropRect(getPreviewFramingRect());
//                Bitmap bmp = sourceData.getBitmap();
//                if (task != null && (task.getStatus() == AsyncTask.Status.RUNNING
//                        || task.getStatus() == AsyncTask.Status.PENDING)) return;
//                task = new QrCodeAsyncTask(_this);
//                task.execute(bmp);
//            }
//
//            @Override
//            public void onPreviewError(Exception e) {
//                Log.i("scan test", "preview error"+e.getLocalizedMessage());
//            }
//        });
    }

    private void checkPermission() {
        if (hasPermission()) {
            start();
        } else {
            String[] permissions = new String[1];
            permissions[0] = Manifest.permission.CAMERA;
            ActivityCompat.requestPermissions(activity, permissions, CAMERA_REQUEST_CODE);
        }
    }

    private boolean hasPermission() {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.M ||
                activity.checkSelfPermission(Manifest.permission.CAMERA) == PERMISSION_GRANTED;
    }

    private void addListenLifecycle() {
//        activity.getApplication().registerActivityLifecycleCallbacks(lifecycleCallback);
    }

    public void _resume() {
        this.resume();
    }
    public void _pause() {
        this.pause();
    }
    public void toggleTorchMode(boolean mode) {
        this.setTorch(mode);
    }
    public void setCaptureListener(CaptureListener captureListener) {
        this.captureListener = captureListener;
    }
    public void dispose() {
//        this.stopDecoding();
        _pause();
//        activity.getApplication().unregisterActivityLifecycleCallbacks(lifecycleCallback);
//        lifecycleCallback = null;
        if (task != null) {
            task.cancel(true);
            task = null;
        }
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        vw = getWidth();
        vh = getHeight();
        if (scale < 1.0) {
            int areaWidth = (int) (min(vw, vh) * scale);
            this.setFramingRectSize(new Size(areaWidth, areaWidth));
        } else {
            this.setFramingRectSize(new Size((int) vw, (int) vh));
        }
    }

    @Override
    public boolean onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == CAMERA_REQUEST_CODE && grantResults[0] == PERMISSION_GRANTED) {
            start();
            Log.i(LOG_TAG, "onRequestPermissionsResult: true");
            return true;
        }
        Log.i(LOG_TAG, "onRequestPermissionsResult: false");
        return false;
    }

    /**
     * AsyncTask 静态内部类，防止内存泄漏
     */
    static class QrCodeAsyncTask extends AsyncTask<Bitmap, Integer, String> {
        private final WeakReference<ScanViewNew> mWeakReference;
//        private final Bitmap bitmap;

        public QrCodeAsyncTask(ScanViewNew view) {
            mWeakReference = new WeakReference<>(view);
//            this.bitmap = bitmap;
        }

        @Override
        protected String doInBackground(Bitmap... params) {
            // 解析二维码/条码
            return QRCodeDecoder.decodeQRCode(mWeakReference.get().context, params[0]);
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            //识别出图片二维码/条码，内容为s
            ScanViewNew view = (ScanViewNew) mWeakReference.get();
            view.captureListener.onCapture(s);
            view.task.cancel(true);
            view.task = null;
            if (s!=null) {
                Vibrator myVib = (Vibrator) view.context.getSystemService(VIBRATOR_SERVICE);
                if (myVib != null) {
                    if (Build.VERSION.SDK_INT >= 26) {
                        myVib.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE));
                    } else {
                        myVib.vibrate(50);
                    }
                }
            }
        }
    }
}
