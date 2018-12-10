package com.example.dominikjularic.customview

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.animation.AnimationUtils
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }
    /*
    fun translateAnimation(view: View){
        val customViewTranslateAnimation = AnimationUtils.loadAnimation(this,R.anim.translate_anim)
        view.startAnimation(customViewTranslateAnimation)
    }*/
}
