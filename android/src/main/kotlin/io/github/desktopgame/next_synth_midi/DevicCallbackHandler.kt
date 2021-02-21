package io.github.desktopgame.next_synth_midi

import android.media.midi.MidiDeviceInfo
import android.media.midi.MidiDeviceStatus
import android.media.midi.MidiManager
import android.media.midi.MidiManager.DeviceCallback
import android.os.Build
import androidx.annotation.RequiresApi

@RequiresApi(Build.VERSION_CODES.M)
class DeviceCallbackHandler : MidiManager.DeviceCallback() {
    public var dirty: Boolean = false;
    public var addedDevices: Int = 0;
    public var removedDevices: Int = 0;

    // MidiManager.DeviceCallback

    override fun onDeviceAdded(device: MidiDeviceInfo) {
        dirty = true;
        addedDevices++;
    }
    override fun onDeviceRemoved(device: MidiDeviceInfo) {
        dirty = true;
        removedDevices--;
    }
    override fun onDeviceStatusChanged(device: MidiDeviceStatus) {
    }

    public fun rehash() {
        this.dirty = false;
        this.addedDevices = 0;
        this.removedDevices = 0;
    }
}
