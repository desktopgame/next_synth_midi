package io.github.desktopgame.next_synth_midi

import android.Manifest
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.content.pm.PackageManager
import android.media.midi.MidiDeviceInfo
import android.media.midi.MidiManager
import android.os.Build
import androidx.annotation.NonNull
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat

import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.embedding.engine.plugins.activity.ActivityAware
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result
import io.flutter.plugin.common.PluginRegistry.Registrar

/** NextSynthMidiPlugin */
class NextSynthMidiPlugin: FlutterPlugin, MethodCallHandler, ActivityAware {
  private lateinit var context: Context
  private lateinit var activity: Activity
  private lateinit var portManager: PortManager;
  private lateinit var bPortManager: PortManager;

  /// The MethodChannel that will the communication between Flutter and native Android
  ///
  /// This local reference serves to register the plugin with the Flutter Engine and unregister it
  /// when the Flutter Engine is detached from the Activity
  private lateinit var channel : MethodChannel

  override fun onAttachedToEngine(@NonNull flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
    channel = MethodChannel(flutterPluginBinding.binaryMessenger, "next_synth_midi")
    channel.setMethodCallHandler(this)
    context = flutterPluginBinding.applicationContext
  }

  @RequiresApi(Build.VERSION_CODES.M)
  override fun onMethodCall(@NonNull call: MethodCall, @NonNull result: Result) {
    val midiManager = context.getSystemService(Context.MIDI_SERVICE) as MidiManager
    val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    val args = (call.arguments as HashMap<String, *>)
    if(!this::portManager.isInitialized) {
      this.portManager = PortManager(midiManager);
    }
    if(!this::bPortManager.isInitialized) {
      this.bPortManager = PortManager(midiManager);
    }
    // isSupported
    if (call.method == "isSupported") {
      result.success(context.packageManager.hasSystemFeature(PackageManager.FEATURE_MIDI))
    // getDeviceCount()
    } else if(call.method == "getDeviceCount") {
      result.success(midiManager.devices.size);
    // getDeviceName(deviceIndex)
    } else if(call.method == "getDeviceName") {
      val deviceIndex = args["deviceIndex"] as Int;
      if(deviceIndex < 0 || deviceIndex >= midiManager.devices.size) {
        result.error("", "getDeviceName: index is out of range.", null);
      } else {
        val props = midiManager.devices[deviceIndex].properties;
        if(props.containsKey(MidiDeviceInfo.PROPERTY_NAME)) {
          result.success(props.get(MidiDeviceInfo.PROPERTY_NAME));
        } else {
          result.success("");
        }
      }
    // getInputPortCount(deviceIndex)
    } else if(call.method == "getInputPortCount") {
      val deviceIndex = args["deviceIndex"] as Int;
      if(deviceIndex < 0 || deviceIndex >= midiManager.devices.size) {
        result.error("", "getInputPortCount: index is out of range.", null);
      } else {
        result.success(midiManager.devices[deviceIndex].inputPortCount);
      }
    // getOutputPortCount(deviceIndex)
    } else if(call.method == "getOutputPortCount") {
      val deviceIndex = args["deviceIndex"] as Int;
      if (deviceIndex < 0 || deviceIndex >= midiManager.devices.size) {
        result.error("", "getOutputPortCount: index is out of range.", null);
      } else {
        result.success(midiManager.devices[deviceIndex].outputPortCount);
      }
    // getPortNumber(deviceIndex, portIndex)
    } else if(call.method == "getPortNumber") {
      val deviceIndex = args["deviceIndex"] as Int;
      val portIndex = args["portIndex"] as Int;
      if ((deviceIndex < 0 || deviceIndex >= midiManager.devices.size) ||
              (portIndex < 0 || portIndex >= midiManager.devices[deviceIndex].ports.size)) {
        result.error("", "getPortNumber: index is out of range.", null);
      } else {
        result.success(midiManager.devices[deviceIndex].ports[portIndex].portNumber);
      }
    // isInputPort(deviceIndex, portIndex)
    } else if(call.method == "isInputPort") {
      val deviceIndex = args["deviceIndex"] as Int;
      val portIndex = args["portIndex"] as Int;
      if ((deviceIndex < 0 || deviceIndex >= midiManager.devices.size) ||
              (portIndex < 0 || portIndex >= midiManager.devices[deviceIndex].ports.size)) {
        result.error("", "getOutputPortCount: index is out of range.", null);
      } else {
        result.success(midiManager.devices[deviceIndex].ports[portIndex].type == MidiDeviceInfo.PortInfo.TYPE_INPUT);
      }
    // isOutputPort(deviceIndex, portIndex)
    } else if(call.method == "isOutputPort") {
      val deviceIndex = args["deviceIndex"] as Int;
      val portIndex = args["portIndex"] as Int;
      if ((deviceIndex < 0 || deviceIndex >= midiManager.devices.size) ||
              (portIndex < 0 || portIndex >= midiManager.devices[deviceIndex].ports.size)) {
        result.error("", "isOutputPort: index is out of range.", null);
      } else {
        result.success(midiManager.devices[deviceIndex].ports[portIndex].type == MidiDeviceInfo.PortInfo.TYPE_OUTPUT);
      }
    // waitForOpen(deviceIndex, inputPort, outputPort)
    } else if(call.method == "waitForOpen") {
      val deviceIndex = args["deviceIndex"] as Int;
      val inputPort = args["inputPort"] as Int;
      val outputPort = args["outputPort"] as Int;
      portManager.waitForOpen(result, deviceIndex, inputPort, outputPort, 2000);
    // openPort(deviceIndex, inputPort, outputPort)
    } else if(call.method == "openPort") {
      val deviceIndex = args["deviceIndex"] as Int;
      val inputPort = args["inputPort"] as Int;
      val outputPort = args["outputPort"] as Int;
      portManager.open(deviceIndex, inputPort, outputPort);
      result.success(null);
    // closePort(deviceIndex, inputPort, outputPort)
    } else if(call.method == "closePort") {
      val deviceIndex = args["deviceIndex"] as Int;
      val inputPort = args["inputPort"] as Int;
      val outputPort = args["outputPort"] as Int;
      result.success(portManager.close(deviceIndex, inputPort, outputPort));
    // send(deviceIndex, inputPort, data, offset, size)
    } else if(call.method == "send") {
      val deviceIndex = args["deviceIndex"] as Int;
      val inputPort = args["inputPort"] as Int;
      val data = args["data"] as ByteArray;
      val offset = args["offset"] as Int;
      val size = args["size"] as Int;
      if (portManager.send(deviceIndex, inputPort, data, offset, size)) {
        result.success(null);
      } else {
        result.error("", "send: port is not found", null);
      }
    // receive(deviceIndex, outputPort)
    } else if(call.method == "receive") {
      val deviceIndex = args["deviceIndex"] as Int;
      val outputPort = args["outputPort"] as Int;
      val dt = portManager.receive(deviceIndex, outputPort);
      if (dt != null) {
        result.success(dt);
      } else {
        result.error("", "receive: port is not found", null);
      }
    // canReceive(deviceIndex, outputPort)
    } else if(call.method == "canReceive") {
      val deviceIndex = args["deviceIndex"] as Int;
      val outputPort = args["outputPort"] as Int;
      result.success(portManager.canReceive(deviceIndex, outputPort));
    // getBluetoothDeviceCount()
    } else if(call.method == "getBluetoothDeviceCount") {
      // 権限のチェック
      if (ContextCompat.checkSelfPermission(
                      context,
                      Manifest.permission.BLUETOOTH)
              != PackageManager.PERMISSION_GRANTED) {
        result.error("", "getBluetoothDeviceCount: permission is denied.", null);
        return;
      }
      if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled) {
        result.error("", "getBluetoothDeviceCount: bluetooth is inactive.", null);
        return;
      }
      result.success(bluetoothAdapter.bondedDevices.size);
    // getBluetoothDeviceName(bluetoothDeviceIndex)
    } else if(call.method == "getBluetoothDeviceName") {
      // 権限のチェック
      if (ContextCompat.checkSelfPermission(
                      context,
                      Manifest.permission.BLUETOOTH)
              != PackageManager.PERMISSION_GRANTED) {
        result.error("", "getBluetoothDeviceName: permission is denied.", null);
        return;
      }
      if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled) {
        result.error("", "getBluetoothDeviceName: bluetooth is inactive.", null);
        return;
      }
      val bluetoothDeviceIndex = args["bluetoothDeviceIndex"] as Int;
      if(bluetoothDeviceIndex < 0 || bluetoothDeviceIndex >= bluetoothAdapter.bondedDevices.size) {
        result.error("", "getBluetoothDeviceName: index is out of range.", null);
      } else {
        result.success(bluetoothAdapter.bondedDevices.toList()[bluetoothDeviceIndex].name);
      }
    // waitForOpenBluetooth(deviceIndex, inputPort, outputPort)
    } else if(call.method == "waitForOpenBluetooth") {
      val deviceIndex = args["id"] as Int;
      val inputPort = args["inputPort"] as Int;
      val outputPort = args["outputPort"] as Int;
      bPortManager.waitForOpen(result, deviceIndex, inputPort, outputPort, 2000);
    // openBluetoothPort(bluetoothDeviceIndex)
    } else if(call.method == "openBluetoothPort") {
      // 権限のチェック
      if (ContextCompat.checkSelfPermission(
                      context,
                      Manifest.permission.BLUETOOTH)
              != PackageManager.PERMISSION_GRANTED) {
        result.error("", "openBluetoothPort: permission is denied.", null);
        return;
      }
      if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled) {
        result.error("", "openBluetoothPort: bluetooth is inactive.", null);
        return;
      }
      val bluetoothDeviceIndex = args["bluetoothDeviceIndex"] as Int;
      val inputPort = args["inputPort"] as Int;
      val outputPort = args["outputPort"] as Int;
      result.success(bPortManager.openBluetooth(bluetoothAdapter.bondedDevices.toList()[bluetoothDeviceIndex], inputPort, outputPort));
    // closeBluetoothPort(bluetoothDeviceIndex)
    } else if(call.method == "closeBluetoothPort") {
      // 権限のチェック
      if (ContextCompat.checkSelfPermission(
                      context,
                      Manifest.permission.BLUETOOTH)
              != PackageManager.PERMISSION_GRANTED) {
        result.error("", "closeBluetoothPort: permission is denied.", null);
        return;
      }
      if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled) {
        result.error("", "closeBluetoothPort: bluetooth is inactive.", null);
        return;
      }
      val id = args["id"] as Int;
      val inputPort = args["inputPort"] as Int;
      val outputPort = args["outputPort"] as Int;
      result.success(bPortManager.closeBluetooth(id, inputPort, outputPort));

    // sendBluetooth(deviceIndex, inputPort, data, offset, size)
    } else if(call.method == "sendBluetooth") {
      val deviceIndex = args["id"] as Int;
      val inputPort = args["inputPort"] as Int;
      val data = args["data"] as ByteArray;
      val offset = args["offset"] as Int;
      val size = args["size"] as Int;
      if (bPortManager.send(deviceIndex, inputPort, data, offset, size)) {
        result.success(null);
      } else {
        result.error("", "sendBluetooth: port is not found", null);
      }
    // receiveBluetooth(deviceIndex, outputPort)
    } else if(call.method == "receiveBluetooth") {
      val deviceIndex = args["id"] as Int;
      val outputPort = args["outputPort"] as Int;
      val dt = bPortManager.receive(deviceIndex, outputPort);
      if (dt != null) {
        result.success(dt);
      } else {
        result.error("", "receiveBluetooth: port is not found", null);
      }
    // canReceiveBluetooth(deviceIndex, outputPort)
    } else if(call.method == "canReceiveBluetooth") {
      val deviceIndex = args["id"] as Int;
      val outputPort = args["outputPort"] as Int;
      result.success(bPortManager.canReceive(deviceIndex, outputPort));
    } else {
      result.notImplemented()
    }
  }

  override fun onDetachedFromEngine(@NonNull binding: FlutterPlugin.FlutterPluginBinding) {
    channel.setMethodCallHandler(null)
  }

  // ActivityAware

  override fun onDetachedFromActivity() {
  }

  override fun onReattachedToActivityForConfigChanges(binding: ActivityPluginBinding) {
  }

  override fun onAttachedToActivity(binding: ActivityPluginBinding) {
    activity = binding.activity;
  }

  override fun onDetachedFromActivityForConfigChanges() {
  }
}
