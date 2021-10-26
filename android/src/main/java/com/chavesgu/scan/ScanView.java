//package com.chavesgu.scan;
//
//import android.Manifest;
//import android.app.Activity;
//import android.app.Application;
//import android.content.Context;
//import android.content.pm.ActivityInfo;
//import android.content.pm.PackageManager;
//import android.graphics.Canvas;
//import android.graphics.Color;
//import android.graphics.Paint;
//import android.graphics.PointF;
//import android.hardware.Camera;
//
//import com.google.zxing.BinaryBitmap;
//import com.google.zxing.ChecksumException;
//import com.google.zxing.DecodeHintType;
//import com.google.zxing.FormatException;
//import com.google.zxing.LuminanceSource;
//import com.google.zxing.MultiFormatReader;
//import com.google.zxing.NotFoundException;
//import com.google.zxing.PlanarYUVLuminanceSource;
//import com.google.zxing.Result;
//import com.google.zxing.common.HybridBinarizer;
//import com.google.zxing.qrcode.QRCodeReader;
//import com.google.zxing.client.android.camera.CameraManager;
//
//import android.os.AsyncTask;
//import android.os.Build;
//import android.os.Bundle;
//import android.os.Handler;
//import android.os.Message;
//import android.os.Vibrator;
//import android.util.AttributeSet;
//import android.util.Log;
//import android.view.Surface;
//import android.view.SurfaceHolder;
//import android.view.SurfaceView;
//import android.view.View;
//import android.view.WindowManager;
//
//import java.io.IOException;
//import java.lang.ref.WeakReference;
//import java.util.Map;
//
//import androidx.annotation.NonNull;
//import androidx.annotation.Nullable;
//import androidx.annotation.RequiresApi;
//import androidx.core.app.ActivityCompat;
//import io.flutter.embedding.engine.plugins.activity.ActivityAware;
//import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding;
//import io.flutter.plugin.common.PluginRegistry;
//
//import static android.content.Context.VIBRATOR_SERVICE;
//import static android.content.pm.PackageManager.PERMISSION_GRANTED;
//import static android.hardware.Camera.getCameraInfo;
//import static java.lang.Math.min;
//
//public class ScanView extends SurfaceView implements SurfaceHolder.Callback, Camera.PreviewCallback,
//        PluginRegistry.RequestPermissionsResultListener {
//
//    public interface CaptureListener {
//        void onCapture(String text);
//    }
//    private CaptureListener captureListener;
//
//    private int CAMERA_REQUEST_CODE = 6537;
//    private String LOG_TAG = "scan";
//    private Context context;
//    private Activity activity;
//    private ActivityPluginBinding activityPluginBinding;
//    private SurfaceHolder surfaceHolder;
//
//    private double vw;
//    private double vh;
//    private double scale = .7;
//    private double areaX;
//    private double areaY;
//    private double areaWidth;
//    private int cameraOrientation;
//    private CameraManager cameraManager;
//    private QRCodeReader qrCodeReader;
//    private int previewWidth;
//    private int previewHeight;
//    private DecodeFrameTask decodeFrameTask;
//    private Map<DecodeHintType, Object> decodeHints;
//    private boolean exit = false;
//
//
//    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
//    public ScanView(Context context, Activity activity, @NonNull ActivityPluginBinding activityPluginBinding, @Nullable Map<String, Object> args) {
//        super(context, null);
//
//        Log.i(LOG_TAG, args.toString());
//
//        this.context = context;
//        this.activity = activity;
//        activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
//        this.activityPluginBinding = activityPluginBinding;
//        activityPluginBinding.addRequestPermissionsResultListener(this);
//        this.scale = (double) args.get("scale");
//
//        if (isInEditMode()) {
//            return;
//        }
//
//        setVisibility(View.INVISIBLE); // do not handle created before request permission
//        getHolder().addCallback(this);
//        checkPermission();
//    }
//
//    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
//    private void initCamera() {
//        if (checkCameraHardware()) {
////            cameraManager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
//            cameraManager = new CameraManager(getContext());
//            cameraManager.setPreviewCallback(this);
//            cameraManager.setPreviewCameraId(Camera.CameraInfo.CAMERA_FACING_BACK);
//
//            addListenLifecycle(activity);
//
//            setVisibility(View.VISIBLE); // handle created
//        } else {
//            throw new RuntimeException("Error: Camera not found");
//        }
//    }
//
//    @Override
//    public void surfaceCreated(SurfaceHolder surfaceHolder) {
//        Log.i(LOG_TAG, "surface created");
//
//        vw = getWidth();
//        vh = getHeight();
////        setBackgroundColor(0xff000000);
//        setZOrderMediaOverlay(true);
////        setWillNotDraw(false);
//
//        try {
//            cameraManager.openDriver(surfaceHolder, getWidth(), getHeight());
//        } catch (IOException | RuntimeException e) {
//            Log.i(LOG_TAG, "Can not openDriver: " + e.getMessage());
//            cameraManager.closeDriver();
//        }
//
//        try {
//            qrCodeReader = new QRCodeReader();
//            cameraManager.startPreview();
//        } catch (Exception e) {
//            Log.e(LOG_TAG, "Exception: " + e.getMessage());
//            cameraManager.closeDriver();
//        }
//    }
//
//    @Override
//    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int width, int height) {
//        Log.i(LOG_TAG, "surface changed");
//        vw = width;
//        vh = height;
//
//        areaWidth = min(vw, vh) * scale;
//        areaX = (vw - areaWidth) / 2;
//        areaY = (vh - areaWidth) / 2;
//
//        if (surfaceHolder.getSurface() == null) {
//            Log.e(LOG_TAG, "Error: preview surface does not exist");
//            return;
//        }
//
//        if (cameraManager.getPreviewSize() == null) {
//            Log.e(LOG_TAG, "Error: preview size does not exist");
//            return;
//        }
//
//
//        previewWidth = cameraManager.getPreviewSize().x;
//        previewHeight = cameraManager.getPreviewSize().y;
//        cameraManager.setManualFramingRect((int)areaWidth, (int)areaWidth);
//
////        Log.i(LOG_TAG, "preview:"+previewWidth+","+previewHeight);
////        Log.i(LOG_TAG, "view:"+vw+","+vh);
//
//        Log.i(LOG_TAG, "orientation:" + getCameraDisplayOrientation());
//        cameraOrientation = getCameraDisplayOrientation();
//
//        cameraManager.stopPreview();
//        cameraManager.setPreviewCallback(this);
//        cameraManager.startPreview();
//    }
//
//    @Override
//    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
//        Log.i(LOG_TAG, "surface destroyed");
//        cameraManager.setPreviewCallback(null);
//        cameraManager.stopPreview();
//        cameraManager.closeDriver();
//    }
//
//    @Override
//    public void onPreviewFrame(byte[] bytes, Camera camera) {
//        if (decodeFrameTask != null && (decodeFrameTask.getStatus() == AsyncTask.Status.RUNNING
//                || decodeFrameTask.getStatus() == AsyncTask.Status.PENDING)) return;
//
//        decodeFrameTask = new DecodeFrameTask(this, decodeHints);
//        decodeFrameTask.execute(bytes);
//    }
//
//    @Override
//    public void onDetachedFromWindow() {
//        super.onDetachedFromWindow();
//
//        if (decodeFrameTask != null) {
//            decodeFrameTask.cancel(true);
//            decodeFrameTask = null;
//        }
//    }
//
//    public void resume() {
//        exit = false;
//        cameraManager.setPreviewCallback(this);
//        cameraManager.startPreview();
//    }
//    public void pause() {
//        cameraManager.setPreviewCallback(null);
//        cameraManager.stopPreview();
//    }
//    public void toggleTorchMode(boolean mode) {
//        cameraManager.setTorchEnabled(mode);
//    }
//    public void setCaptureListener(CaptureListener captureListener) {
//        this.captureListener = captureListener;
//    }
//
//    private boolean checkCameraHardware() {
//        if (getContext().getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
//            // this device has a google.zxing.client.android.android.com.google.zxing.client.android.camera
//            return true;
//        } else if (getContext().getPackageManager()
//                .hasSystemFeature(PackageManager.FEATURE_CAMERA_FRONT)) {
//            // this device has a front google.zxing.client.android.android.com.google.zxing.client.android.camera
//            return true;
//        } else {
//            // this device has any google.zxing.client.android.android.com.google.zxing.client.android.camera
//            return getContext().getPackageManager().hasSystemFeature(
//                    PackageManager.FEATURE_CAMERA_ANY);
//        }
//    }
//
//    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
//    private void checkPermission() {
//        if (hasPermission()) {
//            initCamera();
//        } else {
//            String[] permissions = new String[1];
//            permissions[0] = Manifest.permission.CAMERA;
//            ActivityCompat.requestPermissions(activity, permissions, CAMERA_REQUEST_CODE);
//        }
//    }
//
//    private boolean hasPermission() {
//        return Build.VERSION.SDK_INT < Build.VERSION_CODES.M ||
//                activity.checkSelfPermission(Manifest.permission.CAMERA) == PERMISSION_GRANTED;
//    }
//
//    private void addListenLifecycle(Activity activity) {
//        final Activity _activity = activity;
//        _activity.getApplication().registerActivityLifecycleCallbacks(new Application.ActivityLifecycleCallbacks() {
//            @Override
//            public void onActivityCreated(@NonNull Activity activity, @Nullable Bundle bundle) {
//            }
//            @Override
//            public void onActivityStarted(@NonNull Activity activity) {
//            }
//
//            @Override
//            public void onActivityResumed(@NonNull Activity activity) {
//                if (activity == _activity) {
//                    Log.i(LOG_TAG, "activity resume");
//                    resume();
//                }
//            }
//
//            @Override
//            public void onActivityPaused(@NonNull Activity activity) {
//                if (activity == _activity) {
//                    Log.i(LOG_TAG, "activity pause");
//                    pause();
//                }
//            }
//
//            @Override
//            public void onActivityStopped(@NonNull Activity activity) {
//            }
//            @Override
//            public void onActivitySaveInstanceState(@NonNull Activity activity, @NonNull Bundle bundle) {
//            }
//            @Override
//            public void onActivityDestroyed(@NonNull Activity activity) {
//            }
//        });
//    }
//
//    private int getCameraDisplayOrientation() {
//        Camera.CameraInfo info = new Camera.CameraInfo();
//        getCameraInfo(cameraManager.getPreviewCameraId(), info);
//        WindowManager windowManager =
//                (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
//        int rotation = windowManager.getDefaultDisplay().getRotation();
//        int degrees = 0;
//        switch (rotation) {
//            case Surface.ROTATION_0:
//                degrees = 0;
//                break;
//            case Surface.ROTATION_90:
//                degrees = 90;
//                break;
//            case Surface.ROTATION_180:
//                degrees = 180;
//                break;
//            case Surface.ROTATION_270:
//                degrees = 270;
//                break;
//            default:
//                break;
//        }
//
//        return (info.orientation - degrees + 360) % 360;
//    }
//
//    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
//    @Override
//    public boolean onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
//        if (requestCode == CAMERA_REQUEST_CODE && grantResults[0] == PERMISSION_GRANTED) {
//            initCamera();
//            Log.i(LOG_TAG, "onRequestPermissionsResult: true");
//            return true;
//        }
//        Log.i(LOG_TAG, "onRequestPermissionsResult: false");
//        return false;
//    }
//
//
//    private static class DecodeFrameTask extends AsyncTask<byte[], Void, Result> {
//        private final WeakReference<ScanView> viewRef;
//        private final WeakReference<Map<DecodeHintType, Object>> hintsRef;
//
//        DecodeFrameTask(ScanView view, Map<DecodeHintType, Object> hints) {
//            viewRef = new WeakReference<>(view);
//            hintsRef = new WeakReference<>(hints);
//        }
//
//        @Override
//        protected Result doInBackground(byte[]... params) {
//            final ScanView scanView = viewRef.get();
//            if (scanView == null) return null;
//            Result result = null;
//
//            final PlanarYUVLuminanceSource source = scanView.cameraManager.buildLuminanceSource(
//                    params[0],
//                    (int) scanView.previewWidth,
//                    (int) scanView.previewHeight
//            );
////            final LuminanceSource cropSource = source.crop(
////                    cropImageX,
////                    cropImageY,
////                    cropImageWidth,
////                    cropImageHeight
////            );
//            final HybridBinarizer hybridBinarizer = new HybridBinarizer(source);
//            final BinaryBitmap bitmap = new BinaryBitmap(hybridBinarizer);
////            Log.i(scanView.LOG_TAG, "bitmap:"+bitmap.getWidth()+","+bitmap.getHeight());
//
//            try {
//                if (!scanView.exit) {
//                    result = scanView.qrCodeReader.decode(bitmap, hintsRef.get());
////                    Log.i(scanView.LOG_TAG, "result:"+result.getText());
//                }
//            } catch (FormatException e) {
//                Log.i(scanView.LOG_TAG, "FormatException:" + e.getMessage());
//            } catch (ChecksumException e) {
//                Log.i(scanView.LOG_TAG, "ChecksumException:" + e.getMessage());
//            } catch (NotFoundException e) {
////                return null;
//            } finally {
//                scanView.qrCodeReader.reset();
//            }
//            return result;
//        }
//
//        @Override
//        protected void onPostExecute(Result result) {
//            super.onPostExecute(result);
//
//            final ScanView scanView = viewRef.get();
//
//            if (scanView!=null && result!=null && scanView.captureListener!=null) {
//                scanView.exit = true;
//                scanView.captureListener.onCapture(result.getText());
////                scanView.pause();
//                Vibrator myVib = (Vibrator) scanView.context.getSystemService(VIBRATOR_SERVICE);
//                if (myVib != null) {
//                    myVib.vibrate(50);
//                }
//            }
//        }
//
//        private void calcCoordinate() {
//
//        }
//    }
//}
