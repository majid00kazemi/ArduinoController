package com.mk.bluetoothcontroller

import android.app.Application
import com.google.android.material.color.DynamicColors

class BluetoothController: Application() {

    override fun onCreate() {
        super.onCreate()
        // Apply dynamic color
        //DynamicColors.applyToActivitiesIfAvailable(this)

    }
}