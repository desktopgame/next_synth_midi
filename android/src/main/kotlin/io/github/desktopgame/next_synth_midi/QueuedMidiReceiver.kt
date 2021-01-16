package io.github.desktopgame.next_synth_midi


import android.media.midi.MidiReceiver
import android.os.Build
import androidx.annotation.RequiresApi

@RequiresApi(Build.VERSION_CODES.M)
class QueuedMidiReceiver(val deviceIndex: Int, val portNumber: Int) : MidiReceiver() {
    private val storage = ArrayList<ByteArray>()

    val hasEvent: Boolean get() = storage.isNotEmpty()

    override fun onSend(p0: ByteArray?, p1: Int, p2: Int, p3: Long) {
        if (p0 != null) {
            storage.add(p0)
        };
    }

    fun pop(): ByteArray? {
        if(storage.isNotEmpty()) {
            val temp = storage[storage.size-1]
            storage.removeAt(storage.size-1)
            return temp
        }
        return null
    }
}