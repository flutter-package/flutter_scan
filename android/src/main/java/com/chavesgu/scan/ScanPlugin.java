package com.chavesgu.scan;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.DecodeHintType;
import com.google.zxing.LuminanceSource;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.NotFoundException;
import com.google.zxing.RGBLuminanceSource;
import com.google.zxing.common.GlobalHistogramBinarizer;
import com.google.zxing.common.HybridBinarizer;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import androidx.annotation.NonNull;

import io.flutter.Log;
import io.flutter.embedding.engine.plugins.FlutterPlugin;
import io.flutter.embedding.engine.plugins.activity.ActivityAware;
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;
import io.flutter.plugin.common.PluginRegistry.Registrar;

/** ScanPlugin */
public class ScanPlugin implements FlutterPlugin, MethodCallHandler, ActivityAware {
  private MethodChannel channel;
  private Activity activity;
  private FlutterPluginBinding flutterPluginBinding;

  public static void registerWith(Registrar registrar) {
    ScanPlugin instance = new ScanPlugin();
    final MethodChannel channel = new MethodChannel(registrar.messenger(), "chavesgu/scan");
    channel.setMethodCallHandler(instance);
    instance.channel = channel;
    instance.activity = registrar.activity();
    registrar.platformViewRegistry().registerViewFactory("chavesgu/scan_view", new ScanViewFactory(
            registrar.messenger(),
            registrar.context(),
            registrar.activity(),
            registrar
    ));
  }

  @Override
  public void onAttachedToEngine(@NonNull FlutterPluginBinding flutterPluginBinding) {
    this.flutterPluginBinding = flutterPluginBinding;
  }

  private void configChannel(ActivityPluginBinding binding) {
    activity = binding.getActivity();
    channel = new MethodChannel(flutterPluginBinding.getBinaryMessenger(), "chavesgu/scan");
    channel.setMethodCallHandler(this);
    flutterPluginBinding.getPlatformViewRegistry()
            .registerViewFactory("chavesgu/scan_view", new ScanViewFactory(
                    flutterPluginBinding.getBinaryMessenger(),
                    flutterPluginBinding.getApplicationContext(),
                    activity,
                    binding
            ));
  }

  @Override
  public void onAttachedToActivity(@NonNull ActivityPluginBinding binding) {
    configChannel(binding);
  }

  @Override
  public void onReattachedToActivityForConfigChanges(@NonNull ActivityPluginBinding binding) {
    configChannel(binding);
  }

  @Override
  public void onDetachedFromActivityForConfigChanges() {
  }
  @Override
  public void onDetachedFromEngine(@NonNull FlutterPluginBinding binding) {
    this.flutterPluginBinding = null;
  }

  @Override
  public void onDetachedFromActivity() {
    activity = null;
    channel.setMethodCallHandler(null);
  }

  @Override
  public void onMethodCall(@NonNull MethodCall call, @NonNull Result result) {
    if (call.method.equals("getPlatformVersion")) {
      result.success("Android " + android.os.Build.VERSION.RELEASE);
    } else if (call.method.equals("parse")) {
      result.success(getCodeFromImagePath((String) call.arguments));
    } else {
      result.notImplemented();
    }
  }

  private String getCodeFromImagePath(String path) {
    final Map<DecodeHintType, Object> HINTS = new EnumMap<>(DecodeHintType.class);
    List<BarcodeFormat> allFormats = new ArrayList<>();
    allFormats.add(BarcodeFormat.AZTEC);
    allFormats.add(BarcodeFormat.CODABAR);
    allFormats.add(BarcodeFormat.CODE_39);
    allFormats.add(BarcodeFormat.CODE_93);
    allFormats.add(BarcodeFormat.CODE_128);
    allFormats.add(BarcodeFormat.DATA_MATRIX);
    allFormats.add(BarcodeFormat.EAN_8);
    allFormats.add(BarcodeFormat.EAN_13);
    allFormats.add(BarcodeFormat.ITF);
    allFormats.add(BarcodeFormat.MAXICODE);
    allFormats.add(BarcodeFormat.PDF_417);
    allFormats.add(BarcodeFormat.QR_CODE);
    allFormats.add(BarcodeFormat.RSS_14);
    allFormats.add(BarcodeFormat.RSS_EXPANDED);
    allFormats.add(BarcodeFormat.UPC_A);
    allFormats.add(BarcodeFormat.UPC_E);
    allFormats.add(BarcodeFormat.UPC_EAN_EXTENSION);
    HINTS.put(DecodeHintType.TRY_HARDER, BarcodeFormat.QR_CODE);
    HINTS.put(DecodeHintType.POSSIBLE_FORMATS, allFormats);
    HINTS.put(DecodeHintType.CHARACTER_SET, "utf-8");

    BitmapFactory.Options options = new BitmapFactory.Options();
    options.inJustDecodeBounds = true;
    BitmapFactory.decodeFile(path, options);
    int sampleSize = options.outHeight / 400;
    if (sampleSize <= 0) {
      sampleSize = 1;
    }
    options.inSampleSize = sampleSize;
    options.inJustDecodeBounds = false;
    Bitmap bitmap = BitmapFactory.decodeFile(path, options);

    com.google.zxing.Result result = null;
    RGBLuminanceSource source = null;
    try {
      int width = bitmap.getWidth();
      int height = bitmap.getHeight();
      int[] pixels = new int[width * height];
      bitmap.getPixels(pixels, 0, width, 0, 0, width, height);
      source = new RGBLuminanceSource(width, height, pixels);
      result = new MultiFormatReader().decode(new BinaryBitmap(new HybridBinarizer(source)), HINTS);
      return result.getText();
    } catch (Exception e) {
      if (source != null) {
        try {
          result = new MultiFormatReader().decode(new BinaryBitmap(new GlobalHistogramBinarizer(source)), HINTS);
          return result.getText();
        } catch (Throwable e2) {
          MultiFormatReader multiFormatReader = new MultiFormatReader();
          try {
            LuminanceSource invertedSource = source.invert();
            BinaryBitmap binaryBitmap = new BinaryBitmap(new HybridBinarizer(invertedSource));

            result = multiFormatReader.decode(binaryBitmap, HINTS);
            return result.getText();
          } catch (NotFoundException exception) {
            e.printStackTrace();
            e2.printStackTrace();
            exception.printStackTrace();
            return null;
          } finally {
            multiFormatReader.reset();
          }
        }
      }
      return null;
    }
  }
}
