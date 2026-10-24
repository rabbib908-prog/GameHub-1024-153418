package com.gamehub.app

import android.app.Activity
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView

class MainActivity : Activity() {
    private lateinit var root: FrameLayout
    private var current: View? = null
    private var onBack: (() -> Unit)? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        root = FrameLayout(this)
        root.setBackgroundColor(0xFF10152B.toInt())
        setContentView(root)
        home()
    }

    override fun onPause() {
        super.onPause()
        (current as? Pausable)?.pauseGame()
    }

    override fun onResume() {
        super.onResume()
        (current as? Pausable)?.resumeGame()
    }

    override fun onBackPressed() {
        val b = onBack
        if (b != null) b() else super.onBackPressed()
    }

    private fun dp(v: Int): Int = (v * resources.displayMetrics.density).toInt()

    private fun rounded(color: Int): GradientDrawable {
        val g = GradientDrawable()
        g.setColor(color)
        g.cornerRadius = dp(16).toFloat()
        return g
    }

    private fun show(v: View, back: (() -> Unit)?) {
        (current as? Pausable)?.pauseGame()
        root.removeAllViews()
        current = v
        onBack = back
        root.addView(v, FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT))
    }

    private fun menu(title: String, sub: String, items: List<Pair<String, () -> Unit>>, back: (() -> Unit)?) {
        val col = LinearLayout(this)
        col.orientation = LinearLayout.VERTICAL
        col.gravity = Gravity.CENTER
        col.setPadding(dp(28), dp(28), dp(28), dp(28))
        val t = TextView(this)
        t.text = title
        t.textSize = 30f
        t.setTextColor(0xFFFFFFFF.toInt())
        t.gravity = Gravity.CENTER
        t.typeface = Typeface.DEFAULT_BOLD
        col.addView(t)
        if (sub.isNotEmpty()) {
            val s = TextView(this)
            s.text = sub
            s.textSize = 16f
            s.setTextColor(0xFFB0BEC5.toInt())
            s.gravity = Gravity.CENTER
            s.setPadding(0, dp(6), 0, dp(10))
            col.addView(s)
        }
        for ((label, action) in items) {
            val btn = Button(this)
            btn.text = label
            btn.textSize = 18f
            btn.setAllCaps(false)
            btn.setTextColor(0xFFFFFFFF.toInt())
            btn.background = rounded(0xFF3D5AFE.toInt())
            btn.setPadding(0, dp(14), 0, dp(14))
            btn.setOnClickListener { action() }
            val lp = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            lp.topMargin = dp(14)
            col.addView(btn, lp)
        }
        show(col, back)
    }

    private fun home() {
        menu(
            "🎮 গেম হাব", "একা বা দুইজনে খেলুন",
            listOf(
                "👤 ১ প্লেয়ার" to { onePlayer() },
                "👥 ২ প্লেয়ার" to { twoPlayer() }
            ),
            null
        )
    }

    private fun onePlayer() {
        menu(
            "👤 ১ প্লেয়ার", "একা একা খেলুন",
            listOf(
                "🐦 উড়ন্ত পাখি" to { show(FlapView(this)) { onePlayer() } },
                "🐍 সাপ" to { show(SnakeView(this)) { onePlayer() } },
                "🧠 স্মৃতি মিল" to { show(MemoryView(this)) { onePlayer() } }
            ),
            { home() }
        )
    }

    private fun twoPlayer() {
        menu(
            "👥 ২ প্লেয়ার", "বন্ধু বা বটের সাথে খেলুন",
            listOf(
                "❌⭕ টিক ট্যাক টো" to { modeMenu("টিক ট্যাক টো") { m, d -> TicTacToeView(this, m, d) } },
                "🔴🟡 চার মিলাও" to { modeMenu("চার মিলাও") { m, d -> Connect4View(this, m, d) } }
            ),
            { home() }
        )
    }

    private fun modeMenu(name: String, make: (Int, Int) -> View) {
        menu(
            name, "কীভাবে খেলবেন?",
            listOf(
                "🤝 বন্ধুর সাথে খেলুন" to { show(make(MODE_FRIEND, 0)) { modeMenu(name, make) } },
                "🤖 বটের সাথে খেলুন" to { diffMenu(name, make) }
            ),
            { twoPlayer() }
        )
    }

    private fun diffMenu(name: String, make: (Int, Int) -> View) {
        menu(
            "$name - বট", "কঠিনতা বেছে নিন",
            listOf(
                "😊 সহজ" to { show(make(MODE_BOT, 1)) { modeMenu(name, make) } },
                "😐 মাঝারি" to { show(make(MODE_BOT, 2)) { modeMenu(name, make) } },
                "😈 কঠিন" to { show(make(MODE_BOT, 3)) { modeMenu(name, make) } }
            ),
            { modeMenu(name, make) }
        )
    }
}
