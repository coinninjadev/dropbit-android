package app.dropbit.twitter.ui.demo

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import app.dropbit.twitter.R
import app.dropbit.twitter.ui.login.TwitterLoginActivity

class TwitterAuthenticationActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_twitter_authentication)
        findViewById<Button>(R.id.login)?.setOnClickListener { startActivity(Intent(this, TwitterLoginActivity::class.java)) }
    }

}
