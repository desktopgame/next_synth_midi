import 'dart:async';
import 'dart:typed_data';

import 'package:flutter/material.dart';
import 'package:next_synth_midi/next_synth_midi.dart';

void main() {
  runApp(MyApp());
}

class MyApp extends StatefulWidget {
  @override
  _MyAppState createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> {
  String _platformVersion = 'Unknown';

  @override
  void initState() {
    super.initState();
    initPlatformState();
  }

  // Platform messages are asynchronous, so we initialize in an async method.
  Future<void> initPlatformState() async {
    int deviceCount = await Midik.deviceCount;
    bool opened = false;
    print("deviceCount= ${deviceCount}");
    for (int i = 0; i < deviceCount; i++) {
      String name = await Midik.getDeviceName(i);
      int inputs = await Midik.getInputPortCount(i);
      int outputs = await Midik.getOutputPortCount(i);
      print("name=${name} inputs=${inputs} outputs=${outputs}");
      for (int j = 0; j < inputs + outputs; j++) {
        int port = await Midik.getPortNumber(i, j);
        bool isInput = await Midik.isInputPort(i, j);
        bool isOutput = await Midik.isOutputPort(i, j);
        print("[$j] port=${port} isInput=${isInput} isOutput=${isOutput}");
        if (isOutput && !opened) {
          opened = true;
          print("send");
          await Midik.openPort(i, port, -1);
          await Midik.waitForOpen(i, port, -1);
          await Midik.send(i, port, Uint8List.fromList([0x90, 60, 127]), 0, 3);
          await Future.delayed(new Duration(seconds: 3));
          await Midik.send(i, port, Uint8List.fromList([0x90, 60, 0]), 0, 3);
          var n = await Midik.closePort(i, port, -1);
          print("send! $n");
        }
      }
    }
  }

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      home: Scaffold(
        appBar: AppBar(
          title: const Text('Plugin example app'),
        ),
        body: Center(
          child: Text('Running on: $_platformVersion\n'),
        ),
      ),
    );
  }
}
