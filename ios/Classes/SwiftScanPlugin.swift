import Flutter
import UIKit

public class SwiftScanPlugin: NSObject, FlutterPlugin {
  public static func register(with registrar: FlutterPluginRegistrar) {
    let channel = FlutterMethodChannel(name: "chavesgu/scan", binaryMessenger: registrar.messenger())
    let instance = SwiftScanPlugin()
    registrar.addMethodCallDelegate(instance, channel: channel)
    
    registrar.register(ScanViewFactory(registrar: registrar), withId: "chavesgu/scan_view");
  }

  public func handle(_ call: FlutterMethodCall, result: @escaping FlutterResult) {
    if call.method=="getPlatformVersion" {
      result("iOS " + UIDevice.current.systemVersion)
    } else if call.method=="parse" {
      let path = call.arguments as! String;
      if let features = self.detectQRCode(UIImage.init(contentsOfFile: path)), !features.isEmpty {
        for case let row as CIQRCodeFeature in features{
          result(row.messageString);
        }
      } else {
        result(nil);
      }
    }
  }
  
  private func detectQRCode(_ image: UIImage?) -> [CIFeature]? {
    if let image = image, let ciImage = CIImage.init(image: image){
      var options: [String: Any];
      let context = CIContext();
      options = [CIDetectorAccuracy: CIDetectorAccuracyHigh];
      let qrDetector = CIDetector(ofType: CIDetectorTypeQRCode, context: context, options: options);
      if ciImage.properties.keys.contains((kCGImagePropertyOrientation as String)){
        options = [CIDetectorImageOrientation: ciImage.properties[(kCGImagePropertyOrientation as String)] ?? 1];
      } else {
        options = [CIDetectorImageOrientation: 1];
      }
      let features = qrDetector?.features(in: ciImage, options: options);
      return features;
    }
    return nil
  }
}
