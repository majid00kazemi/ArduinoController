package com.mk.bluetoothcontroller

import android.annotation.SuppressLint
import android.app.Activity
import android.app.ProgressDialog
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.MutableLiveData
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.android.synthetic.main.activity_control.*
import kotlinx.android.synthetic.main.activity_control.view.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.io.IOException
import kotlinx.coroutines.*
import java.util.*

class ControlActivity : AppCompatActivity() {

    private var liveData= MutableLiveData<String>()

    @SuppressLint("MissingPermission")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_control)

        mAddress = intent.getStringExtra(MainActivity.EXTRA_ADDRESS).toString()
        ConnectToDevice(this,findViewById(android.R.id.content),liveData).execute()

        openButton.setOnClickListener {
            sendCommand("1")
        }
        disconnectDeviceButton.setOnClickListener { disconnect() }

    }

    private fun sendCommand(input: String){
        if(mBluetoothSocket != null){
            try {
                mBluetoothSocket!!.outputStream.write(input.toByteArray())
            } catch (e: IOException){
                e.printStackTrace()
            }
        }
    }


     private fun disconnect(){
        if(mBluetoothSocket != null){
            try {
                mBluetoothSocket!!.close()
                mBluetoothSocket = null
                mIsConnected = false
            } catch (e: IOException){
                e.printStackTrace()
            }
        }
        finish()
    }

    private class ConnectToDevice(c: Context, rV: View, liveData: MutableLiveData<String>) : AsyncTask<Void, Void,
            String>(){

        private var connectSuccess: Boolean = true
        private val context: Context = c

        override fun onPreExecute() {
            super.onPreExecute()
            alertDialog = MaterialAlertDialogBuilder(context,R.style.AlertDialogCustom)
                .setTitle(context.getString(R.string.connecting_status))
                .setMessage(context.getString(R.string.connecting))
                .setCancelable(false)
                .create()
            alertDialog.show()

        }

        @SuppressLint("MissingPermission")
        override fun doInBackground(vararg p0: Void?): String? {
            try {
                if(mBluetoothSocket == null || !mIsConnected){
                    mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
                    var bluetoothDevice:BluetoothDevice = mBluetoothAdapter.getRemoteDevice(mAddress)
                    mUUID = bluetoothDevice.uuids[0].uuid
                    val device: BluetoothDevice =
                        mBluetoothAdapter.getRemoteDevice(mAddress)


                    mBluetoothSocket =
                        device.createInsecureRfcommSocketToServiceRecord(mUUID)
                    BluetoothAdapter.getDefaultAdapter().cancelDiscovery()

                    mBluetoothSocket!!.connect()
                    alertDialog.dismiss()

                }
            } catch (e: IOException){
                connectSuccess = false
                e.printStackTrace()
            }
            return null
        }

        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)
            if(!connectSuccess){
                Log.i("data","couldn't Connect!")
                Toast.makeText(context,"اتصال برقرار نشد!",Toast.LENGTH_SHORT).show()
                (context as Activity).finish()

            } else {
                mIsConnected = true
            }
            alertDialog.dismiss()


        }

    }


    companion object{
        @SuppressLint("MissingPermission")
        lateinit var mUUID:UUID
        var mBluetoothSocket: BluetoothSocket? = null
        lateinit var alertDialog: androidx.appcompat.app.AlertDialog
        lateinit var mBluetoothAdapter: BluetoothAdapter
        var mIsConnected: Boolean = false
        lateinit var mAddress: String
    }

    override fun onDestroy() {
        super.onDestroy()
        alertDialog.dismiss()
    }

    override fun onPause() {
        super.onPause()
        disconnect()
    }
}

