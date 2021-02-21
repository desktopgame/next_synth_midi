import 'dart:async';
import 'dart:typed_data';

import 'package:flutter/services.dart';

class NextSynthMidi {
  static MethodChannel _impl;
  static MethodChannel get _channel {
    if (_impl == null) {
      _impl = MethodChannel('next_synth_midi');
    }
    return _impl;
  }

  static Future<bool> get isSupported async {
    return await _channel.invokeMethod('isSupported', {});
  }

  static Future<int> get deviceCount async {
    return await _channel.invokeMethod('getDeviceCount', {});
  }

  static Future<String> getDeviceName(int deviceIndex) async {
    return await _channel
        .invokeMethod("getDeviceName", {"deviceIndex": deviceIndex});
  }

  static Future<int> getInputPortCount(int deviceIndex) async {
    return await _channel
        .invokeMethod('getInputPortCount', {"deviceIndex": deviceIndex});
  }

  static Future<int> getOutputPortCount(int deviceIndex) async {
    return await _channel
        .invokeMethod('getOutputPortCount', {"deviceIndex": deviceIndex});
  }

  static Future<int> getPortNumber(int deviceIndex, int portIndex) async {
    return await _channel.invokeMethod(
        'getPortNumber', {"deviceIndex": deviceIndex, "portIndex": portIndex});
  }

  static Future<bool> isInputPort(int deviceIndex, int portIndex) async {
    return await _channel.invokeMethod(
        'isInputPort', {"deviceIndex": deviceIndex, "portIndex": portIndex});
  }

  static Future<bool> isOutputPort(int deviceIndex, int portIndex) async {
    return await _channel.invokeMethod(
        'isOutputPort', {"deviceIndex": deviceIndex, "portIndex": portIndex});
  }

  static Future<void> waitForOpen(
      int deviceIndex, int inputPort, int outputPort) async {
    await _channel.invokeMethod('waitForOpen', {
      "deviceIndex": deviceIndex,
      "inputPort": inputPort,
      "outputPort": outputPort
    });
  }

  static Future<void> openPort(
      int deviceIndex, int inputPort, int outputPort) async {
    return await _channel.invokeMethod('openPort', {
      "deviceIndex": deviceIndex,
      "inputPort": inputPort,
      "outputPort": outputPort
    });
  }

  static Future<int> closePort(
      int deviceIndex, int inputPort, int outputPort) async {
    return await _channel.invokeMethod('closePort', {
      "deviceIndex": deviceIndex,
      "inputPort": inputPort,
      "outputPort": outputPort
    });
  }

  static Future<void> send(int deviceIndex, int inputPort, Uint8List data,
      int offset, int size) async {
    await _channel.invokeMethod('send', {
      "deviceIndex": deviceIndex,
      "inputPort": inputPort,
      "data": data,
      "offset": offset,
      "size": size
    });
  }

  static Future<Uint8List> receive(int deviceIndex, int outputPort) async {
    return await _channel.invokeMethod(
        'receive', {"deviceIndex": deviceIndex, "outputPort": outputPort});
  }

  static Future<bool> canReceive(int deviceIndex, int outputPort) async {
    return await _channel.invokeMethod(
        'canReceive', {"deviceIndex": deviceIndex, "outputPort": outputPort});
  }

  static Future<int> get bluetoothDeviceCount async {
    return await _channel.invokeMethod("getBluetoothDeviceCount", {});
  }

  static Future<String> getBluetoothDeviceName(int bluetoothDeviceIndex) async {
    return await _channel.invokeMethod("getBluetoothDeviceName",
        {"bluetoothDeviceIndex": bluetoothDeviceIndex});
  }

  static Future<void> waitForOpenBluetooth(
      int deviceIndex, int inputPort, int outputPort) async {
    await _channel.invokeMethod('waitForOpenBluetooth',
        {"id": deviceIndex, "inputPort": inputPort, "outputPort": outputPort});
  }

  static Future<int> openBluetoothPort(
      int bluetoothDeviceIndex, int inputPort, int outputPort) async {
    return await _channel.invokeMethod("openBluetoothPort", {
      "bluetoothDeviceIndex": bluetoothDeviceIndex,
      "inputPort": inputPort,
      "outputPort": outputPort
    });
  }

  static Future<int> closeBluetoothPort(
      int id, int inputPort, int outputPort) async {
    return await _channel.invokeMethod("closeBluetoothPort",
        {"id": id, "inputPort": inputPort, "outputPort": outputPort});
  }

  static Future<void> sendBluetooth(
      int id, int inputPort, Uint8List data, int offset, int size) async {
    return await _channel.invokeMethod("sendBluetooth", {
      "id": id,
      "inputPort": inputPort,
      "data": data,
      "offset": offset,
      "size": size,
    });
  }

  static Future<Uint8List> receiveBluetooth(int id, int outputPort) async {
    return await _channel
        .invokeMethod('receiveBluetooth', {"id": id, "outputPort": outputPort});
  }

  static Future<bool> canReceiveBluetooth(int id, int outputPort) async {
    return await _channel.invokeMethod(
        'canReceiveBluetooth', {"id": id, "outputPort": outputPort});
  }

  static Future<bool> isDeviceListUpdated() async {
    return await _channel.invokeMethod("isDeviceListUpdated", {});
  }

  static Future<void> rehashDeviceList() async {
    return await _channel.invokeMethod("rehashDeviceList", {});
  }

  static Future<void> registerDeviceCallback() async {
    return await _channel.invokeMethod("registerDeviceCallback", {});
  }

  static Future<void> unregisterDeviceCallback() async {
    return await _channel.invokeMethod("unregisterDeviceCallback", {});
  }
}
