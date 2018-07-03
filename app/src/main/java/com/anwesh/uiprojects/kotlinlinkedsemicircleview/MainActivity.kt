package com.anwesh.uiprojects.kotlinlinkedsemicircleview

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.anwesh.uiprojects.linkedsemicircleview.LinkedSemiCircleView

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        LinkedSemiCircleView.create(this)
    }
}
