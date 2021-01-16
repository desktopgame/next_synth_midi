
import 'dart:async';

import 'package:flutter/services.dart';

class NextSynthMidi {
  static const MethodChannel _channel =
      const MethodChannel('next_synth_midi');

  static Future<String> get platformVersion async {
    final String version = await _channel.invokeMethod('getPlatformVersion');
    return version;
  }
}
