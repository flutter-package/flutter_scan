# scan

[![scan](https://img.shields.io/badge/pub-1.6.0-orange)](https://pub.dev/packages/scan)

scan qrcode & barcode in widget tree.

decode qrcode & barcode image from path.

> if you want to generate qrcode image, you should use [qr_flutter](https://pub.dev/packages/qr_flutter)

### Features

- use `ScanView` in widget tree to show scan view.
- custom identifiable area.
- decode qrcode from image path by `Scan.parse`.

### prepare

##### ios
info.list
```
<key>NSCameraUsageDescription</key>
<string>Your Description</string>

<key>io.flutter.embedded_views_preview</key>
<string>YES</string>
```
##### android
```xml
<uses-permission android:name="android.permission.CAMERA" />

<application>
  <meta-data
    android:name="flutterEmbedding"
    android:value="2" />
</application>
```

```yaml
scan: ^newest
```
```dart
import 'package:scan/scan.dart';
```

### Usage

- show scan view in widget tree
```dart
ScanController controller = ScanController();
String qrcode = 'Unknown';

Container(
  width: 250, // custom wrap size
  height: 250,
  child: ScanView(
    controller: controller,
// custom scan area, if set to 1.0, will scan full area
    scanAreaScale: .7,
    scanLineColor: Colors.green.shade400,
    onCapture: (data) {
      // do something
    },
  ),
),
```
- you can use `controller.resume()` and `controller.pause()` resume/pause camera

```dart
controller.resume();
controller.pause();
```
- get qrcode string from image path
```dart
String result = await Scan.parse(imagePath);
```
- toggle flash light
```dart
controller.toggleTorchMode();
```
### proguard-rules
```
-ignorewarnings
-keepattributes *Annotation*
-keepattributes Exceptions
-keepattributes InnerClasses
-keepattributes Signature
-keepattributes SourceFile,LineNumberTable
-keep class com.huawei.hianalytics.**{*;}
-keep class com.huawei.updatesdk.**{*;}
-keep class com.huawei.hms.**{*;}
```

# License
MIT License





