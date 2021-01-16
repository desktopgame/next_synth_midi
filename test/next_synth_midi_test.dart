import 'package:flutter/services.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:next_synth_midi/next_synth_midi.dart';

void main() {
  const MethodChannel channel = MethodChannel('next_synth_midi');

  TestWidgetsFlutterBinding.ensureInitialized();

  setUp(() {
    channel.setMockMethodCallHandler((MethodCall methodCall) async {
      return '42';
    });
  });

  tearDown(() {
    channel.setMockMethodCallHandler(null);
  });

  test('getPlatformVersion', () async {
    expect(await NextSynthMidi.platformVersion, '42');
  });
}
