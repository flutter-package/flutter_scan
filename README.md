# scan

flutter widget to scan qrcode customly.

Get qrcode from image.

### Features

- use `ScanView` in widget tree to show scan view.
- custom identifiable area.
- get qrcode string from image path by `Scan.parse`.

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

# License
MIT License





