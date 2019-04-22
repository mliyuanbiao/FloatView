package cn.liyuanbiao.floatview.demo

import android.app.Application
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.Toast
import cn.liyuanbiao.floatview.FloatView

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        FloatView.with(this, "float") {
            return@with ImageButton(it).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
                setBackgroundResource(R.mipmap.ic_launcher_round)
                setOnClickListener {
                    Toast.makeText(this@App, "我被点击了", Toast.LENGTH_SHORT).show()
                }
            }
        }.setActive(true).setLocation(0.8f, 0.7f, FloatView.LOC_TYPE_SCREEN).show()
    }
}