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

        statusSwitch.setOnCheckedChangeListener {checked ->
            if(checked){
                sendCommand("1")
                reciveMessage(mBluetoothSocket!!,findViewById(android.R.id.content),liveData).execute()
            }

            if(!checked){
                sendCommand("0")
                reciveMessage(mBluetoothSocket!!,findViewById(android.R.id.content),liveData).execute()
            }
        }
        disconnectDeviceButton.setOnClickListener { disconnect() }

        liveData.observe(this, androidx.lifecycle.Observer {
            messageR.text = liveData.value!!.trim()
            var message = messageR.text.toString()
            if(messageR.text.toString().equals("On")) messageR.text = "روشن"
            else if(message == "Off") messageR.text = "خاموش"
            else if(message == "1") {
                messageR.text = "روشن"
                statusSwitch.setChecked(true)
            }
            else if(message == "2"){
                messageR.text = "خاموش"
                statusSwitch.setChecked(false)
            }


        })

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

    private class reciveMessage(bluetoothSocket: BluetoothSocket,rV: View, liveData: MutableLiveData<String>) : AsyncTask<Void,Void,String>(){

        private val rootView: View = rV
        var bluetoothInput = mBluetoothSocket!!.inputStream
        var buffer = ByteArray(1024)
        var bytes: Int = 0
        private  lateinit var  readMessage:String
        private var liveData1: MutableLiveData<String> = liveData

        override fun doInBackground(vararg p0: Void?): String? {
            try {
                Log.i("LOGTAG", Thread.currentThread().name)

                if(mBluetoothSocket!= null)
                {
                    try {
                        while (true){
                            try {
                                bytes = bluetoothInput.read(buffer)
                                readMessage = String(buffer,0,bytes)
                                liveData1.postValue(readMessage)
                                Log.i("TAG",readMessage)
                            } catch (e: IOException){
                                e.printStackTrace()
                                break
                            }
                        }
                    } catch (e:IOException){
                        e.printStackTrace()
                    }

                }
            }catch (e: IOException){
                e.printStackTrace()
            }
            return null
        }

        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)
        }

    }

    private class ConnectToDevice(c: Context, rV: View, liveData: MutableLiveData<String>) : AsyncTask<Void, Void,
            String>(){

        private var connectSuccess: Boolean = true
        private val context: Context = c
        private val rootView: View = rV
        private var liveData1: MutableLiveData<String> = liveData


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
                    // status read
                    alertDialog.dismiss()
                    if(mBluetoothSocket != null){
                        try {
                            mBluetoothSocket!!.outputStream.write("8".toByteArray())
                        } catch (e: IOException){
                            e.printStackTrace()
                        }
                    }

                    if(mBluetoothSocket!= null)
                    {
                        try {
                                try {
                                    val bluetoothInput = mBluetoothSocket!!.inputStream
                                    val buffer = ByteArray(1024)
                                    var bytes: Int
                                    bytes = bluetoothInput.read(buffer)
                                    val readMessage = String(buffer,0,bytes)
                                    liveData1.postValue(readMessage)

                                } catch (e: IOException){
                                    e.printStackTrace()
                                }

                        } catch (e:IOException){
                            e.printStackTrace()
                        }

                    }

                    ///end status read



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

