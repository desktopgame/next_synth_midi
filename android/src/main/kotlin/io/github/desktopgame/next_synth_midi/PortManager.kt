package io.github.desktopgame.next_synth_midi


import android.bluetooth.BluetoothDevice
import android.media.midi.MidiInputPort
import android.media.midi.MidiManager
import android.media.midi.MidiOutputPort
import android.os.Build
import android.os.Handler
import android.os.Looper
import androidx.annotation.RequiresApi
import io.flutter.plugin.common.MethodChannel

class PortManager(private val midiManager: MidiManager) {
    private val inputConnectioons = ArrayList<PortConnection<MidiInputPort>>()
    private val outputConnectioons = ArrayList<PortConnection<MidiOutputPort>>()
    private val receivers = ArrayList<QueuedMidiReceiver>()
    private var uniqueIndex = 0;
    private val mutex = Object();

    @RequiresApi(Build.VERSION_CODES.M)
    fun open(deviceIndex: Int, inputPort: Int, outputPort: Int) {
        midiManager.openDevice(midiManager.devices[deviceIndex], { device ->
            synchronized(mutex) {
                if (inputPort >= 0) {
                    val ip = device.openInputPort(inputPort)
                    inputConnectioons.add(PortConnection(deviceIndex, inputPort, ip));
                }
                if (outputPort >= 0) {
                    val op = device.openOutputPort(outputPort)
                    outputConnectioons.add(PortConnection(deviceIndex, outputPort, op));
                    val receiver = QueuedMidiReceiver(deviceIndex, outputPort)
                    receivers.add(receiver);
                    op.connect(receiver);
                }
            }
        }, Handler(Looper.getMainLooper()))
    }

    @RequiresApi(Build.VERSION_CODES.M)
    fun close(deviceIndex: Int, inputPort: Int, outputPort: Int): Int {
        synchronized(mutex) {
            val oldPorts = inputConnectioons.size + outputConnectioons.size;
            inputConnectioons
                    .filter { e -> e.deviceIndex == deviceIndex && e.portNumber == inputPort }
                    .forEach { e -> e.close() }
            inputConnectioons.removeAll { e -> e.deviceIndex == deviceIndex && e.portNumber == inputPort }

            outputConnectioons
                    .filter { e -> e.deviceIndex == deviceIndex && e.portNumber == outputPort }
                    .forEach { e ->
                        receivers.filter { x -> x.deviceIndex == deviceIndex && x.portNumber == outputPort }
                                .forEach { x -> e.port.disconnect(x) }
                        receivers.removeAll() { x -> x.deviceIndex == deviceIndex && x.portNumber == outputPort }
                        e.close()
                    }
            outputConnectioons.removeAll { e -> e.deviceIndex == deviceIndex && e.portNumber == outputPort }

            val newPorts = inputConnectioons.size + outputConnectioons.size;
            return oldPorts - newPorts;
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    fun openBluetooth(bluetoothDevice: BluetoothDevice, inputPort: Int, outputPort: Int): Int {
        val id = uniqueIndex++;
        midiManager.openBluetoothDevice(bluetoothDevice, { device ->
            synchronized(mutex) {
                if (inputPort >= 0) {
                    val ip = device.openInputPort(inputPort)
                    inputConnectioons.add(PortConnection(id, inputPort, ip));
                }
                if (outputPort >= 0) {
                    val op = device.openOutputPort(outputPort)
                    outputConnectioons.add(PortConnection(id, outputPort, op));
                    val receiver = QueuedMidiReceiver(id, outputPort)
                    receivers.add(receiver);
                    op.connect(receiver);
                }
            }
        }, Handler(Looper.getMainLooper()))
        return id;
    }

    @RequiresApi(Build.VERSION_CODES.M)
    fun closeBluetooth(id: Int, inputPort: Int, outputPort: Int): Int {
        synchronized(mutex) {
            val oldPorts = inputConnectioons.size + outputConnectioons.size;
            inputConnectioons
                    .filter { e -> e.deviceIndex == id && e.portNumber == inputPort }
                    .forEach { e -> e.close() }
            inputConnectioons.removeAll { e -> e.deviceIndex == id && e.portNumber == inputPort }

            outputConnectioons
                    .filter { e -> e.deviceIndex == id && e.portNumber == outputPort }
                    .forEach { e ->
                        receivers.filter { x -> x.deviceIndex == id && x.portNumber == outputPort }
                                .forEach { x -> e.port.disconnect(x) }
                        receivers.removeAll() { x -> x.deviceIndex == id && x.portNumber == outputPort }
                        e.close()
                    }
            outputConnectioons.removeAll { e -> e.deviceIndex == id && e.portNumber == outputPort }

            val newPorts = inputConnectioons.size + outputConnectioons.size;
            return oldPorts - newPorts;
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    fun send(deviceIndex: Int, inputPort: Int, data: ByteArray, offset: Int, size: Int): Boolean {
        val bytes = ArrayList<Byte>()
        for(b in data) {
            bytes.add(b as Byte);
        }
        val conn = inputConnectioons.firstOrNull { e -> e.deviceIndex == deviceIndex && e.portNumber == inputPort }
        if (conn != null) {
            val ko = ByteArray(bytes.size)
            for (i in 0 until bytes.size) {
                ko[i] = bytes[i]
            }
            conn.port.send(ko, offset, size);
            return true;
        } else {
            return false;
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    fun receive(deviceIndex: Int, outputPort: Int): ByteArray? {
        val receiver = receivers.firstOrNull { e -> e.deviceIndex == deviceIndex && e.portNumber == outputPort };
        if (receiver != null) {
            val dt = receiver.pop();
            if (dt != null) {
                return dt;
            } else {
                return null;
            }
        } else {
            return null;
        }
    }

    fun canReceive(deviceIndex: Int, outputPort: Int): Boolean {
        val receiver = receivers.firstOrNull { e -> e.deviceIndex == deviceIndex && e.portNumber == outputPort };
        if (receiver != null) {;
            return receiver.hasEvent
        } else {
            return false;
        }
    }

    fun waitForOpen(result: MethodChannel.Result, deviceIndex: Int, inputPort: Int, outputPort: Int, timeout: Int) {

        val h = Handler(Looper.getMainLooper());
        Thread {
            var elapsed = 0L;
            var b = true;
            while(b) {
                if(elapsed > timeout.toLong()) {
                    h.post{
                        result.error("", "waitForOpen: timeout", null);
                    }
                    break;
                }
                synchronized(mutex) {
                    val isize = if (inputPort >= 0)  inputConnectioons
                            .filter { e -> e.deviceIndex == deviceIndex && e.portNumber == inputPort }
                            .size else -1;
                    val osize = if (outputPort >= 0)  outputConnectioons
                            .filter { e -> e.deviceIndex == deviceIndex && e.portNumber == outputPort }
                            .size else -1;
                    val iOk = isize == -1 || isize > 0;
                    val oOk = osize == -1 || osize > 0;
                    if(iOk && oOk) {
                        b = false;
                        h.post {
                            result.success(null);
                        }
                    }
                }
                Thread.sleep(100);
                elapsed += 100;
            }
        }.start();
    }
}