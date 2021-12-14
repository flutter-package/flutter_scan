import 'dart:io';

import 'package:flutter/material.dart';
import 'dart:async';

import 'package:flutter/services.dart';
import 'package:scan/scan.dart';
import 'package:images_picker/images_picker.dart';
import 'package:scan_example/scan.dart';

void main() {
  runApp(MyApp());
}

class MyApp extends StatefulWidget {
  @override
  _MyAppState createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> {
  String _platformVersion = 'Unknown';

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
      initialRoute: '/',
      routes: {
        '/': (context) => Scaffold(
              appBar: AppBar(
                title: const Text('Plugin example app'),
              ),
              body: Column(
                children: [
                  Text('Running on: $_platformVersion\n'),
                  Wrap(
                    children: [
                      ElevatedButton(
                        child: Text("parse from image"),
                        onPressed: () async {
                          List<Media>? res = await ImagesPicker.pick();
                          if (res != null) {
                            String? str = await Scan.parse(res[0].path);
                            if (str != null) {
                              setState(() {
                                qrcode = str;
                              });
                            }
                          }
                        },
                      ),
                      ElevatedButton(
                        child: Text('go scan page'),
                        onPressed: () {
                          Navigator.push(context,
                              MaterialPageRoute(builder: (_) {
                            return ScanPage();
                          }));
                        },
                      ),
                    ],
                  ),
                  Text('scan result is $qrcode'),
                ],
              ),
            ),
      },
    );
  }
}
