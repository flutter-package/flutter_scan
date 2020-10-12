//
//  ScanPlatformView.swift
//  scan
//
//  Created by chaves on 2020/10/9.
//

import Flutter
import UIKit

public class ScanPlatformView: NSObject,FlutterPlatformView{
  
  var scanView: ScanView?;
  var viewId: Int64!;
  var channel: FlutterMethodChannel?;
  
  init(_ frame:CGRect, viewId:Int64, args: Any?,registrar: FlutterPluginRegistrar){
    super.init();
    
    self.scanView = ScanView(frame, viewId: viewId, args: args, registrar: registrar);
    self.viewId = viewId;
  }
  
  public func view() -> UIView {
    return self.scanView!;
  }
}

extension UIColor {
    convenience init(hex: String, alpha: CGFloat) {
        let v = hex.map { String($0) } + Array(repeating: "0", count: max(6 - hex.count, 0))
        let r = CGFloat(Int(v[0] + v[1], radix: 16) ?? 0) / 255.0
        let g = CGFloat(Int(v[2] + v[3], radix: 16) ?? 0) / 255.0
        let b = CGFloat(Int(v[4] + v[5], radix: 16) ?? 0) / 255.0
        self.init(red: r, green: g, blue: b, alpha: alpha)
    }

    convenience init(hex: String) {
        self.init(hex: hex, alpha: 1.0)
    }
}
