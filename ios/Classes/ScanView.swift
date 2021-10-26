//
//  ScanView.swift
//  scan
//
//  Created by chaves on 2020/10/9.
//

import UIKit
import AVFoundation
import Flutter



public class ScanView: UIView,AVCaptureMetadataOutputObjectsDelegate,FlutterPlugin {
  public static func register(with registrar: FlutterPluginRegistrar) {
    //
  }
  
  var loaded: Bool = false;
  var queue: DispatchQueue?;
  var session: AVCaptureSession?;
  var isSessionRun: Bool = false;
  var captureLayer: AVCaptureVideoPreviewLayer?;
  var metadataOutput: AVCaptureMetadataOutput?;
  var scanShapeLayer: CAShapeLayer?;
  var scanColor: UIColor!;
  var needDelScanLine: Bool = false;
  var transparentScanLine: Bool = false;
  var channel: FlutterMethodChannel?;
  
  var _bounds: CGRect = CGRect();
  var vw:CGFloat = 0;
  var vh:CGFloat = 0;
  var scale:CGFloat = 0.7;
  
  init(_ frame:CGRect, viewId:Int64, args: Any?,registrar: FlutterPluginRegistrar) {
    super.init(frame: frame);
    self.queue = DispatchQueue.init(label: "com.chavesgu.scan", attributes: .concurrent);
    self.session = AVCaptureSession();
    self.channel = FlutterMethodChannel(name: "chavesgu/scan/method_\(viewId)", binaryMessenger: registrar.messenger());
    registrar.addMethodCallDelegate(self, channel: self.channel!);
//    registrar.addApplicationDelegate(self);
    
    let params = args as! NSDictionary;
    self.scale = params["scale"] as! CGFloat;
    let r = params["r"] as! CGFloat;
    let g = params["g"] as! CGFloat;
    let b = params["b"] as! CGFloat;
    let a = params["a"] as! CGFloat;
    self.transparentScanLine = a == 0.0;
    self.scanColor = UIColor(red: r / 255.0, green: g / 255.0, blue: b / 255.0, alpha: a);
    
    let layer = AVCaptureVideoPreviewLayer(session: self.session!);
    self.captureLayer = layer;
    layer.name = "capture";
    layer.backgroundColor = UIColor.black.cgColor;
    layer.videoGravity = .resizeAspectFill;
    self.layer.addSublayer(layer);
    
    NotificationCenter.default.addObserver(self, selector: #selector(sessionDidStart), name: .AVCaptureSessionDidStartRunning, object: nil);
    
    NotificationCenter.default.addObserver(self, selector: #selector(sessionDidStop), name: .AVCaptureSessionDidStopRunning, object: nil);
  }
  
  private func load() {
    self.loaded = true;
    // 获取相机权限
    AVCaptureDevice.requestAccess(for: .video) { (bool) in
      if (bool) {
        self.configSession();
      }
    }
  }
  
  private func configSession() {
    do {
//      self.session!.beginConfiguration();
      // add input
      var defaultVideoDevice: AVCaptureDevice?;
      if let cameraDevice =  AVCaptureDevice.default(for: .video)  {
        defaultVideoDevice = cameraDevice
      }
      guard let videoDevice = defaultVideoDevice else {
        print("Default video device is unavailable.")
        return
      }
      let videoDeviceInput = try AVCaptureDeviceInput(device: videoDevice);
      if self.session!.canAddInput(videoDeviceInput) {
        self.session!.addInput(videoDeviceInput);
      }
      
      // add output
      let metadataOutput = AVCaptureMetadataOutput();
      self.metadataOutput = metadataOutput;
      if self.session!.canAddOutput(metadataOutput) {
        self.session!.addOutput(metadataOutput);
        metadataOutput.setMetadataObjectsDelegate(self, queue: .main);
        metadataOutput.metadataObjectTypes = [.aztec, .code128, .code39, .code39Mod43, .code93, .dataMatrix, .ean13, .ean8, .interleaved2of5, .itf14, .pdf417, .qr];
      } else {
          print("Could not add photo output to the session")
          return
      }
      
      self.session!.sessionPreset = AVCaptureSession.Preset.high;
      self.setScanArea();
//      self.session!.commitConfiguration();
      self.session!.startRunning();
//      self.queue!.async {
//        self.session!.startRunning();
//      }
    } catch {
      print("Couldn't create video device input: \(error)")
    }
  }
  
  // 设置扫描区域
  private func setScanArea() {
    let scale:CGFloat = self.scale;
    let areaWidth = min(self.vw, self.vh) * scale;
    let x = (self.vw - areaWidth) / 2;
    let y = (self.vh - areaWidth) / 2;
    if let output = self.metadataOutput,let captureLayer = self.captureLayer {
      let originRect = CGRect(x: x, y: y, width: areaWidth, height: areaWidth);
      let rect = captureLayer.metadataOutputRectConverted(fromLayerRect: originRect);
      output.rectOfInterest = rect;
    }
  }
  
  @objc func sessionDidStart() {
    self.isSessionRun = true;
    if self.vw>0, self.scanShapeLayer==nil{
      let areaWidth = min(self.vw, self.vh) * self.scale;
      self.drawScanLine(areaWidth: areaWidth);
      self.needDelScanLine = false;
    }
  }
  
  @objc func sessionDidStop() {
    self.isSessionRun = false;
    if let scanShapeLayer = self.scanShapeLayer {
      self.needDelScanLine = true;
      scanShapeLayer.removeAllAnimations();
      scanShapeLayer.removeFromSuperlayer();
      self.scanShapeLayer = nil;
    }
  }
  
  private func drawScanArea() {
    for subLayer in self.layer.sublayers! {
      if subLayer.name != "capture" {
        subLayer.removeFromSuperlayer();
      }
    }
    
    let scale:CGFloat = self.scale;
    let areaWidth = min(self.vw, self.vh) * scale;
    let x = (self.vw - areaWidth) / 2;
    let y = (self.vh - areaWidth) / 2;
    let shortWidth:CGFloat = areaWidth * 0.1;
    let joinWidth:CGFloat = 0.5;
    
    if scale < 1 {
      // 绘制遮罩
      let rectPath = UIBezierPath(rect: CGRect(x: 0, y: 0, width: self.vw, height: self.vh));
      let clipPath = UIBezierPath(rect: CGRect(x: x-joinWidth, y: y-joinWidth, width: areaWidth+joinWidth*2, height: areaWidth+joinWidth*2));
      rectPath.append(clipPath);
      rectPath.usesEvenOddFillRule = true;
      let mask = CAShapeLayer();
      mask.frame = self.bounds;
      mask.path = rectPath.cgPath;
      mask.fillRule = .evenOdd;
      mask.fillColor = UIColor.init(red: 0.0, green: 0.0, blue: 0.0, alpha: 0.5).cgColor;
      
      self.layer.insertSublayer(mask, above: self.captureLayer);
      // 绘制四个角
      let path = UIBezierPath();
      path.move(to: CGPoint(x: x + joinWidth, y: y));
      path.addLine(to: CGPoint(x: shortWidth + x, y: y));
      
      path.move(to: CGPoint(x: x, y: y + joinWidth));
      path.addLine(to: CGPoint(x: x, y: y + shortWidth));
      
      path.move(to: CGPoint(x: x + areaWidth - joinWidth, y: y));
      path.addLine(to: CGPoint(x: x + areaWidth - shortWidth, y: y));
      
      path.move(to: CGPoint(x: x + areaWidth, y: y + joinWidth));
      path.addLine(to: CGPoint(x: x + areaWidth, y: y + shortWidth));
      
      path.move(to: CGPoint(x: x, y: y + areaWidth - joinWidth));
      path.addLine(to: CGPoint(x: x, y: y + areaWidth - shortWidth));
      
      path.move(to: CGPoint(x: x + joinWidth, y: y + areaWidth));
      path.addLine(to: CGPoint(x: x + shortWidth, y: y + areaWidth));
      
      path.move(to: CGPoint(x: x + areaWidth - joinWidth, y: y + areaWidth));
      path.addLine(to: CGPoint(x: x + areaWidth - shortWidth, y: y + areaWidth));
      
      path.move(to: CGPoint(x: x + areaWidth, y: y + areaWidth - joinWidth));
      path.addLine(to: CGPoint(x: x + areaWidth, y: y + areaWidth - shortWidth));

      
      let shapeLayer = CAShapeLayer();
      shapeLayer.frame = self.bounds;
      shapeLayer.path = path.cgPath;
      shapeLayer.lineCap = .round;
      shapeLayer.lineJoin = .round;
      shapeLayer.strokeColor = self.scanColor.cgColor;
      shapeLayer.lineWidth = 2.0;

      self.layer.insertSublayer(shapeLayer, above: self.captureLayer);
    }
    
    // 绘制扫描线
    if !self.needDelScanLine && !self.transparentScanLine {
      self.drawScanLine(areaWidth: areaWidth);
    }
  }
  
  private func drawScanLine(areaWidth: CGFloat) {
    // 绘制扫描线
    let scanPath = UIBezierPath();
    let scanLineWidth = areaWidth * 0.8;
    let scanLineX = (self.vw - scanLineWidth) / 2;
    let scanLineY = (self.vh - scanLineWidth) / 2;
    scanPath.move(to: CGPoint(x: scanLineX, y: scanLineY));
    scanPath.addLine(to: CGPoint(x: scanLineX + scanLineWidth, y: scanLineY));
    let scanShapeLayer = CAShapeLayer();
    scanShapeLayer.frame = self._bounds;
    scanShapeLayer.path = scanPath.cgPath;
    scanShapeLayer.strokeColor = self.scanColor.withAlphaComponent(0.9).cgColor;
    scanShapeLayer.lineWidth = 2.0;
    
    // 扫描线动画
    let animationGroup = CAAnimationGroup();
    let scanPositionAnimation = CABasicAnimation();
    scanPositionAnimation.keyPath = "transform.translation.y";
    scanPositionAnimation.byValue = scanLineWidth;
    scanPositionAnimation.duration = CFTimeInterval(areaWidth/175*1.5);
    
    let scanOpacityAnimation = CABasicAnimation();
    scanOpacityAnimation.keyPath = "opacity";
    scanOpacityAnimation.fromValue = 1;
    scanOpacityAnimation.toValue = 0;
    scanOpacityAnimation.duration = CFTimeInterval(areaWidth/175*0.5);
    scanOpacityAnimation.beginTime = CFTimeInterval(areaWidth/175*1);
    
    animationGroup.animations = [scanPositionAnimation, scanOpacityAnimation];
    animationGroup.repeatCount = MAXFLOAT;
    animationGroup.duration = CFTimeInterval(areaWidth/175*1.5);
    animationGroup.isRemovedOnCompletion = false;
    scanShapeLayer.add(animationGroup, forKey: nil);
    
    self.scanShapeLayer = scanShapeLayer;
    self.layer.insertSublayer(scanShapeLayer, above: self.captureLayer);
  }
  
  public override func layoutSubviews() {
    super.layoutSubviews();
    self.captureLayer?.frame = self.bounds;
    self._bounds = self.bounds;
    self.vw = self.bounds.width;
    self.vh = self.bounds.height;
    self.drawScanArea();
    if !self.loaded {
      self.load();
    }
  }
  
  public override func removeFromSuperview() {
    // clear
    self.session?.stopRunning();
    NotificationCenter.default.removeObserver(self);
    self.loaded = false;
    self.session = nil;
    self.queue = nil;
    super.removeFromSuperview();
  }
  
  required init?(coder: NSCoder) {
    fatalError("init(coder:) has not been implemented")
  }
  
  // 扫码结果
  public func metadataOutput(_ output: AVCaptureMetadataOutput, didOutput metadataObjects: [AVMetadataObject], from connection: AVCaptureConnection) {
    self.session!.stopRunning();
    if let metadataObject = metadataObjects.first {
      guard let readableObject = metadataObject as? AVMetadataMachineReadableCodeObject else { return }
      guard let stringValue = readableObject.stringValue else { return }
      if #available(iOS 10.0, *) {
        UIImpactFeedbackGenerator(style: .medium).impactOccurred();
      }
      self.channel!.invokeMethod("onCaptured", arguments: stringValue);
    }
  }
  
  public func handle(_ call: FlutterMethodCall, result: @escaping FlutterResult) {
    if call.method=="resume" {
      self.resume();
    } else if call.method=="pause" {
      self.pause();
    } else if call.method=="toggleTorchMode" {
      self.toggleTorchMode();
    }
  }
  
  private func resume() {
    if !self.isSessionRun {
      self.session?.startRunning();
    }
  }
  
  private func pause() {
    if self.isSessionRun {
      self.session?.stopRunning();
    }
  }
  
  private func toggleTorchMode() {
    guard let device = AVCaptureDevice.default(for: .video) else { return }
    guard device.hasTorch else { return }
    do {
      try device.lockForConfiguration();
      
      if (device.torchMode == AVCaptureDevice.TorchMode.on) {
        device.torchMode = AVCaptureDevice.TorchMode.off;
      } else {
        do {
          try device.setTorchModeOn(level: 1.0);
        } catch {
          print(error);
        }
      }
        
      device.unlockForConfiguration();
    } catch {
      print(error);
    }
  }
}
