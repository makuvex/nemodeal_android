package com.jungbae.nemodeal.view

import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import coil.api.load
import com.google.android.gms.ads.VideoController
import com.google.android.gms.ads.formats.MediaView
import com.google.android.gms.ads.formats.UnifiedNativeAd
import com.google.android.gms.ads.formats.UnifiedNativeAdView
import com.jungbae.nemodeal.CommonApplication
import com.jungbae.nemodeal.R
import com.jungbae.nemodeal.True
import com.jungbae.nemodeal.network.HotDealInfo
import io.reactivex.subjects.PublishSubject
import kotlinx.android.synthetic.main.home_card_row.view.*
import kotlinx.android.synthetic.main.native_ad_item.view.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*


class HomeCardHolder(inflater: LayoutInflater, parent: ViewGroup):
    RecyclerView.ViewHolder(inflater.inflate(R.layout.home_card_row, parent, false)) {

    var data: HotDealInfo? = null
    var inflater = inflater

    fun bind(data: HotDealInfo, selectSubject: PublishSubject<HotDealInfo>) {
        this.data = data
        updateUI(data)

        if(data.adUser.True()) {
            MainScope().async {
                //it.enableCustomClickGesture()
                with(itemView) {

                    data.adItem?.let {

                        val adView = inflater.inflate(R.layout.ad_unified, null) as UnifiedNativeAdView

                        populateUnifiedNativeAdView(it, adView)
                        ad_frame.removeAllViews()
                        ad_frame.addView(adView)
                    }
                }
            }
        } else {

        }

        itemView.setOnClickListener {
            selectSubject.onNext(data)
        }
    }

    private fun populateUnifiedNativeAdView(nativeAd: UnifiedNativeAd, adView: UnifiedNativeAdView) {
        adView.mediaView = adView.findViewById<MediaView>(R.id.ad_media)
        adView.headlineView = adView.findViewById(R.id.ad_headline)
        adView.bodyView = adView.findViewById(R.id.ad_body)
//        adView.callToActionView = adView.findViewById(R.id.ad_call_to_action)
        adView.iconView = adView.findViewById(R.id.ad_app_icon)
//        adView.priceView = adView.findViewById(R.id.ad_price)
//        adView.starRatingView = adView.findViewById(R.id.ad_stars)
//        adView.storeView = adView.findViewById(R.id.ad_store)
//        adView.advertiserView = adView.findViewById(R.id.ad_advertiser)

        // The headline and media content are guaranteed to be in every UnifiedNativeAd.
        (adView.headlineView as TextView).text = nativeAd.headline
        adView.mediaView.setMediaContent(nativeAd.mediaContent)

        // These assets aren't guaranteed to be in every UnifiedNativeAd, so it's important to
        // check before trying to display them.

        adView.bodyView.visibility = nativeAd.body?.let {
            (adView.bodyView as TextView).text = nativeAd.body
            View.VISIBLE
        } ?: View.INVISIBLE

        adView.iconView.visibility = nativeAd.icon?.let {
            (adView.iconView as ImageView).setImageDrawable(nativeAd.icon.drawable)
            View.VISIBLE
        } ?: View.GONE


//        if (nativeAd.price == null) {
//            adView.priceView.visibility = View.INVISIBLE
//        } else {
//            adView.priceView.visibility = View.VISIBLE
//            (adView.priceView as TextView).text = nativeAd.price
//        }

//        if (nativeAd.store == null) {
//            adView.storeView.visibility = View.INVISIBLE
//        } else {
//            adView.storeView.visibility = View.VISIBLE
//            (adView.storeView as TextView).text = nativeAd.store
//        }

//        if (nativeAd.starRating == null) {
//            adView.starRatingView.visibility = View.INVISIBLE
//        } else {
//            (adView.starRatingView as RatingBar).rating = nativeAd.starRating!!.toFloat()
//            adView.starRatingView.visibility = View.VISIBLE
//        }
//
//        if (nativeAd.advertiser == null) {
//            adView.advertiserView.visibility = View.INVISIBLE
//        } else {
//            (adView.advertiserView as TextView).text = nativeAd.advertiser
//            adView.advertiserView.visibility = View.VISIBLE
//        }

        // This method tells the Google Mobile Ads SDK that you have finished populating your
        // native ad view with this native ad.
        adView.setNativeAd(nativeAd)
    }

    private fun updateUI(data: HotDealInfo) {
        with(itemView) {
            itemView.site_icon.load(data.siteIcon)
            itemView.category.text = "[" + data.category + "]"
            itemView.title.text = data.title

            itemView.thumbnail.visibility = if (data.thumbnail == null) View.GONE else View.VISIBLE
            data.thumbnail?.let {
                GlobalScope.launch {
                    itemView.thumbnail.load(data.thumbnail)
                }
                itemView.thumbnail.clipToOutline = true
            }

            itemView.like_count.text = data.recommend.toString()

            if (data.decommend.toString() == "0") {
                itemView.unlike_count.visibility = View.GONE
                itemView.unlike_image.visibility = View.GONE
            } else {
                itemView.unlike_count.visibility = View.VISIBLE
                itemView.unlike_image.visibility = View.VISIBLE
            }
            itemView.unlike_count.text = data.decommend.toString()
            itemView.reg_date.text = ""
            itemView.invalid_item.visibility = if (data.articleEnd.True()) View.VISIBLE else View.GONE

            val seperated = data.regDate.split(" ")
            if (isToday(seperated[0])) {
                if (seperated.size > 1) {
                    val time = seperated[1].split(":")
                    itemView.reg_date.text = time[0] + ":" + time[1]
                }
            } else {
                itemView.reg_date.text = seperated[0].substring(5)
            }
        }
    }

    private fun isToday(date: String): Boolean {
        val format = if(date.contains(".")) "yyyy.MM.dd" else "yyyy-MM-dd"
        val current = System.currentTimeMillis()
        val sdfNow = SimpleDateFormat(format)
        val today = sdfNow.format(current)

        return date == today
    }
}