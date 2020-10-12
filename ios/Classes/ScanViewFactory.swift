//
//  ScanViewFactory.swift
//  scan
//
//  Created by chaves on 2020/10/9.
//

import Flutter
import UIKit

public class ScanViewFactory: NSObject, FlutterPlatformViewFactory {
  var registrar: FlutterPluginRegistrar!;
  
  @objc public init(registrar: FlutterPluginRegistrar?) {
    super.init();
    self.registrar = registrar;
  }
  
  public func create(withFrame frame: CGRect, viewIdentifier viewId: Int64, arguments args: Any?) -> FlutterPlatformView {
    return ScanPlatformView(frame, viewId:viewId, args:args, registrar: registrar );
  }
  
  public func createArgsCodec() -> FlutterMessageCodec & NSObjectProtocol {
    return FlutterStandardMessageCodec.sharedInstance()
  }
}
