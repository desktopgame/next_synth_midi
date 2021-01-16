package io.github.desktopgame.next_synth_midi

import java.io.Closeable

class PortConnection<T : Closeable> : Closeable {
    var deviceIndex: Int
        get
        private set
    var portNumber: Int
        get
        private set
    var port: T
        get
        private set

    constructor(deviceIndex: Int, portNumber: Int, port: T) {
        this.deviceIndex = deviceIndex;
        this.portNumber = portNumber;
        this.port = port;
    }

    override fun close() {
        port.close()
    }
}