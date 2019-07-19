package com.coinninja.coinkeeper.view.widget

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatImageView
import com.coinninja.coinkeeper.CoinKeeperApplication
import com.coinninja.coinkeeper.R
import com.coinninja.coinkeeper.twitter.MyTwitterProfile
import com.coinninja.coinkeeper.util.image.CircleTransform
import com.squareup.picasso.Picasso
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch


class DropbitMeImageView : AppCompatImageView {

    constructor(context: Context) : super(context) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs, 0) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init()
    }


    private lateinit var circleTransform: CircleTransform
    private lateinit var myTwitterProfile: MyTwitterProfile

    fun init() {
        circleTransform = CoinKeeperApplication.appComponent.provideCircleTransform()
        myTwitterProfile = CoinKeeperApplication.appComponent.provideMyTwitter()
        setImageResource(R.drawable.ic_dropbit_me)
        GlobalScope.launch(Dispatchers.Main) {
            myTwitterProfile.loadMyProfile()?.let {
                val picasso = Picasso.get()
                picasso.invalidate(it.profileImage)
                picasso.load(it.profileImage)
                        .transform(circleTransform).into(this@DropbitMeImageView)
            }
        }
    }
}