package com.example.bluetooth

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.OutputStream
import java.io.PrintWriter
import java.net.InetSocketAddress
import java.net.Socket
import java.util.UUID

sealed class ConnectionState {
    object Disconnected : ConnectionState()
    object Connecting : ConnectionState()
    data class Connected(val name: String, val address: String, val type: String) : ConnectionState()
    data class Error(val message: String) : ConnectionState()
}

enum class ConnectionType {
    BLUETOOTH,
    WIFI
}

class PCConnectionManager(private val context: Context) {

    private val ioScope = CoroutineScope(Dispatchers.IO)
    private val uuid: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB") // Standard SPP UUID

    private val _connectionState = MutableStateFlow<ConnectionState>(ConnectionState.Disconnected)
    val connectionState: StateFlow<ConnectionState> = _connectionState

    private var bluetoothSocket: BluetoothSocket? = null
    private var wifiSocket: Socket? = null
    private var outputStream: OutputStream? = null
    private var writer: PrintWriter? = null

    val bluetoothAdapter: BluetoothAdapter? by lazy {
        val manager = context.getSystemService(Context.BLUETOOTH_SERVICE) as? android.bluetooth.BluetoothManager
        manager?.adapter ?: BluetoothAdapter.getDefaultAdapter()
    }

    /**
     * Get list of paired bluetooth devices.
     */
    @SuppressLint("MissingPermission")
    fun getPairedDevices(): List<BluetoothDevice> {
        val adapter = bluetoothAdapter ?: return emptyList()
        return try {
            if (adapter.isEnabled) {
                adapter.bondedDevices.toList()
            } else {
                emptyList()
            }
        } catch (e: SecurityException) {
            Log.e("PCConnectionManager", "Missing BLUETOOTH_CONNECT permission", e)
            emptyList()
        }
    }

    /**
     * Connect to PC via Bluetooth RFCOMM
     */
    @SuppressLint("MissingPermission")
    fun connectBluetooth(device: BluetoothDevice) {
        disconnect()
        _connectionState.value = ConnectionState.Connecting

        ioScope.launch {
            try {
                _connectionState.value = ConnectionState.Connecting
                val socket = device.createRfcommSocketToServiceRecord(uuid)
                bluetoothSocket = socket

                // Cancel discovery if active (good practice for RFCOMM stability)
                try {
                    bluetoothAdapter?.cancelDiscovery()
                } catch (e: SecurityException) {
                    // Ignore permission exception
                }

                socket.connect()
                outputStream = socket.outputStream
                writer = PrintWriter(outputStream!!, true)

                withContext(Dispatchers.Main) {
                    _connectionState.value = ConnectionState.Connected(
                        name = device.name ?: "Unknown PC",
                        address = device.address,
                        type = "Bluetooth"
                    )
                }
                Log.d("PCConnectionManager", "Connected via Bluetooth to ${device.name}")
            } catch (e: Exception) {
                Log.e("PCConnectionManager", "Bluetooth connection failed", e)
                withContext(Dispatchers.Main) {
                    _connectionState.value = ConnectionState.Error("Bluetooth connection failed: ${e.localizedMessage}")
                }
                disconnect()
            }
        }
    }

    /**
     * Connect to PC via Local Wi-Fi (IP and Port)
     */
    fun connectWifi(ip: String, port: Int = 8000) {
        disconnect()
        _connectionState.value = ConnectionState.Connecting

        ioScope.launch {
            try {
                val socket = Socket()
                socket.connect(InetSocketAddress(ip, port), 4000) // 4 sec timeout
                wifiSocket = socket
                outputStream = socket.getOutputStream()
                writer = PrintWriter(outputStream!!, true)

                withContext(Dispatchers.Main) {
                    _connectionState.value = ConnectionState.Connected(
                        name = "Desktop via Local LAN",
                        address = "$ip:$port",
                        type = "Wi-Fi"
                    )
                }
                Log.d("PCConnectionManager", "Connected via Wi-Fi to $ip:$port")
            } catch (e: Exception) {
                Log.e("PCConnectionManager", "Wi-Fi connection failed", e)
                withContext(Dispatchers.Main) {
                    _connectionState.value = ConnectionState.Error("Wi-Fi connection failed: ${e.localizedMessage}")
                }
                disconnect()
            }
        }
    }

    /**
     * Sends a direct control payload command to the PC
     */
    fun sendCommand(cmd: String) {
        val currentWriter = writer
        if (currentWriter != null && _connectionState.value is ConnectionState.Connected) {
            ioScope.launch {
                try {
                    currentWriter.println(cmd)
                    currentWriter.flush()
                } catch (e: Exception) {
                    Log.e("PCConnectionManager", "Failed to send command", e)
                    // Auto-disconnect on connection break
                    withContext(Dispatchers.Main) {
                        _connectionState.value = ConnectionState.Error("Stream connection broken.")
                    }
                    disconnect()
                }
            }
        }
    }

    /**
     * Cleans up and disconnects active sockets
     */
    fun disconnect() {
        ioScope.launch {
            try { writer?.close() } catch (e: Exception) {}
            try { outputStream?.close() } catch (e: Exception) {}
            try { bluetoothSocket?.close() } catch (e: Exception) {}
            try { wifiSocket?.close() } catch (e: Exception) {}

            writer = null
            outputStream = null
            bluetoothSocket = null
            wifiSocket = null

            withContext(Dispatchers.Main) {
                if (_connectionState.value !is ConnectionState.Error) {
                    _connectionState.value = ConnectionState.Disconnected
                }
            }
        }
    }
}
