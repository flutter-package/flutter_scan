import 'dart:io';

import 'package:flutter/material.dart';
import 'dart:async';

import 'package:flutter/services.dart';
import 'package:scan/scan.dart';
import 'package:images_picker/images_picker.dart';

void main() {
  runApp(MyApp());
}

class MyApp extends StatefulWidget {
  @override
  _MyAppState createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> {
  String _platformVersion = 'Unknown';

  ScanController controller = ScanController();
  String qrcode = 'Unknown';

  @override
  void initState() {
    super.initState();
    initPlatformState();
  }

  Future<void> initPlatformState() async {
    String platformVersion;
    try {
      platformVersion = await Scan.platformVersion;
    } on PlatformException {
      platformVersion = 'Failed to get platform version.';
    }
    if (!mounted) return;

    setState(() {
      _platformVersion = platformVersion;
    });
  }

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      home: Scaffold(
        appBar: AppBar(
          title: const Text('Plugin example app'),
        ),
        body: Column(
          children: [
            Text('Running on: $_platformVersion\n'),
            Wrap(
              children: [
                ElevatedButton(
                  child: Text("toggleTorchMode"),
                  onPressed: () {
                    controller.toggleTorchMode();
                  },
                ),
                ElevatedButton(
                  child: Text("pause"),
                  onPressed: () {
                    controller.pause();
                  },
                ),
                ElevatedButton(
                  child: Text("resume"),
                  onPressed: () {
                    controller.resume();
                  },
                ),
                ElevatedButton(
                  child: Text("parse from image"),
                  onPressed: () async {
                    List<Media>? res = await ImagesPicker.pick();
                    if (res != null) {
                      String? str = await Scan.parse(res[0].path!);
                      if (str != null) {
                        setState(() {
                          qrcode = str;
                        });
                      }
                    }
                  },
                ),
              ],
            ),
            Container(
              width: 220,
              height: 400,
              child: ScanView(
                controller: controller,
                scanAreaScale: .7,
                scanLineColor: Colors.green.shade400,
                onCapture: (data) {
                  setState(() {
                    qrcode = data;
                  });
                },
              ),
            ),
            Text('scan result is $qrcode'),
          ],
        ),
      ),
    );
  }
}
