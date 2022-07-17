package com.mk.bluetoothcontroller

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.ContentValues.TAG
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {

    private var mBluetoothAdapter:BluetoothAdapter? = null
    private lateinit var mPairedDevices: Set<BluetoothDevice>
    private val REQUEST_ENABLE_BLUETOOTH = 1
    private val REQUEST_BLUETOOTH = 101
    private val deviceID = "78:D8:5D:10:1D:D4"
    private var isConnected = false


    @SuppressLint("MissingPermission")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        permissionSetup()


        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        if(mBluetoothAdapter == null){
            Toast.makeText(applicationContext,R.string.does_not_support_bluetooth,
            Toast.LENGTH_SHORT).show()
            finish()
        }

        if(!mBluetoothAdapter!!.isEnabled){
            val enableBluetoothIntent =
                Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)

            startActivityForResult(enableBluetoothIntent, REQUEST_ENABLE_BLUETOOTH)
        }
        checkBluetoothIsConnected()
        val intent = Intent(this, ControlActivity::class.java)
        intent.putExtra(EXTRA_ADDRESS,deviceID)
        if(isConnected) startActivity(intent)
        else{
            Toast.makeText(this,"ابتدا به بلوتوث وصل شوید", Toast.LENGTH_SHORT)
                .show()
            val bluetoothSettings = Intent()
            bluetoothSettings.action = Settings.ACTION_BLUETOOTH_SETTINGS
            startActivity(bluetoothSettings)
        }

        connectButton.setOnClickListener {
            checkBluetoothIsConnected()
            if(isConnected) startActivity(intent)
            else{
                Toast.makeText(this,"ابتدا به بلوتوث وصل شوید", Toast.LENGTH_SHORT)
                    .show()
                val bluetoothSettings = Intent()
                bluetoothSettings.action = Settings.ACTION_BLUETOOTH_SETTINGS
                startActivity(bluetoothSettings)
            }

        }

    }

    @RequiresApi(Build.VERSION_CODES.S)
    private fun permissionSetup() {
        val blue = ContextCompat.checkSelfPermission(this,
        Manifest.permission.BLUETOOTH_CONNECT)
        if(blue != PackageManager.PERMISSION_GRANTED){
            makeRequest()
        }

        val scan = ContextCompat.checkSelfPermission(this,
            Manifest.permission.BLUETOOTH_SCAN)
        if(scan != PackageManager.PERMISSION_GRANTED){
            makeRequest()
        }
    }

    @RequiresApi(Build.VERSION_CODES.S)
    private fun makeRequest(){
        ActivityCompat.requestPermissions(this,
        arrayOf(Manifest.permission.BLUETOOTH,
            Manifest.permission.BLUETOOTH_CONNECT, Manifest.permission.BLUETOOTH_SCAN),
        REQUEST_BLUETOOTH )
    }

    companion object{
        const val EXTRA_ADDRESS:String = "Device_address"
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == REQUEST_ENABLE_BLUETOOTH){
            if(resultCode == Activity.RESULT_OK){
                if(mBluetoothAdapter!!.isEnabled){
                    Toast.makeText(applicationContext, R.string.bluetooth_opened
                    , Toast.LENGTH_SHORT).show()
                } else{
                    Toast.makeText(applicationContext, R.string.bluetooth_closed,
                    Toast.LENGTH_SHORT).show()
                }
            } else if(resultCode == Activity.RESULT_CANCELED){
                Toast.makeText(applicationContext, R.string.bluetooth_cancelled,
                Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when(requestCode){
            REQUEST_BLUETOOTH ->{
                if(grantResults.isEmpty() || grantResults[0] != PackageManager.PERMISSION_GRANTED){
                    Log.i(TAG, "Permission has been denied by user")
                } else{
                    Log.i(TAG, "Permission has been granted by user")
                }
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun checkBluetoothIsConnected(){
        if(!mBluetoothAdapter!!.isEnabled){
            val enableBluetoothIntent =
                Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)

            startActivityForResult(enableBluetoothIntent, REQUEST_ENABLE_BLUETOOTH)
        }
        mPairedDevices = mBluetoothAdapter!!.bondedDevices
        val list:ArrayList<BluetoothDevice> = ArrayList()
        if(mPairedDevices.isNotEmpty()){
            for(device:BluetoothDevice in mPairedDevices){
                list.add(device)
            }
        }
        val deviceMac: ArrayList<String> = ArrayList()
        for(item in list){
            deviceMac.add(item.address)
        }
        if(deviceMac.contains(deviceID))
            isConnected = true
        //isConnected = deviceMac.contains(deviceID)
    }

}