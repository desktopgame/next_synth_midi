# next_synth_midi

MIDI機能を使用するためのネイティブライブラリ

## example
MIDI端末を列挙して、最初に見つかった端末にノートオン,ノートオフを送信する
````.dart
Future<void> sendMidiMessage() async {
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
        await NextSynthMidi.openPort(i, port, -1);
        await NextSynthMidi.waitForOpen(i, port, -1);
        await NextSynthMidi.send(i, port, Uint8List.fromList([0x90, 60, 127]), 0, 3);
        await Future.delayed(new Duration(seconds: 3));
        await NextSynthMidi.send(i, port, Uint8List.fromList([0x90, 60, 0]), 0, 3);
        var n = await NextSynthMidi.closePort(i, port, -1);
        print("send! $n");
      }
    }
  }
}
````


