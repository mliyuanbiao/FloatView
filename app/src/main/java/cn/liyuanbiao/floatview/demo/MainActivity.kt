package cn.liyuanbiao.floatview.demo

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import cn.liyuanbiao.floatview.FloatView
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        text.setOnClickListener {
            FloatView.get("float")?.apply {
                if (isShow) {
                    hide()
                } else {
                    show()
                }
            }
        }
    }
}
