import 'dart:async';
import 'dart:typed_data';

import 'package:flutter/material.dart';
import 'package:next_synth_midi/next_synth_midi.dart';
import 'dart:async';

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

    Timer.periodic(
      Duration(seconds: 1),
      (Timer t) async {
        if (await NextSynthMidi.isDeviceListUpdated()) {
          await NextSynthMidi.rehashDeviceList();
          setState(() {
            _platformVersion = "device updated";
          });
        }
      },
    );
  }

  // Platform messages are asynchronous, so we initialize in an async method.
  Future<void> initPlatformState() async {
    int deviceCount = await NextSynthMidi.deviceCount;
    bool opened = false;
    print("deviceCount= ${deviceCount}");
    for (int i = 0; i < deviceCount; i++) {
      String name = await NextSynthMidi.getDeviceName(i);
      int inputs = await NextSynthMidi.getInputPortCount(i);
      int outputs = await NextSynthMidi.getOutputPortCount(i);
      print("name=${name} inputs=${inputs} outputs=${outputs}");
      for (int j = 0; j < inputs + outputs; j++) {
        int port = await NextSynthMidi.getPortNumber(i, j);
        bool isInput = await NextSynthMidi.isInputPort(i, j);
        bool isOutput = await NextSynthMidi.isOutputPort(i, j);
        print("[$j] port=${port} isInput=${isInput} isOutput=${isOutput}");
        if (isOutput && !opened) {
          opened = true;
          print("send");
          await NextSynthMidi.openPort(i, port, -1);
          await NextSynthMidi.waitForOpen(i, port, -1);
          await NextSynthMidi.send(
              i, port, Uint8List.fromList([0x90, 60, 127]), 0, 3);
          await Future.delayed(new Duration(seconds: 3));
          await NextSynthMidi.send(
              i, port, Uint8List.fromList([0x90, 60, 0]), 0, 3);
          var n = await NextSynthMidi.closePort(i, port, -1);
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
