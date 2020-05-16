package golab.ad

import android.content.res.Resources
import android.graphics.Color
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.facebook.ads.*
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.formats.UnifiedNativeAd
import com.google.android.gms.ads.formats.UnifiedNativeAdView

class HybridAd(val ad: Any?,
               val unitId: String,
               val adSource: String,
               var adType: AdType = AdType.NATIVE,
               var eventType: AdEventType = AdEventType.default) {

    private val TAG = "HybridAdTAG"
    private var adInstance: Any? = ad

    fun setType(adType: AdType) {
        this.adType = adType
    }

    fun onClicked() {
        Log.d(TAG, "onClicked ${unitId}")
    }

    fun onImpressed() {
        Log.d(TAG, "onImpressed ${unitId}")
    }

    fun setAdData(ad: Any?) {
        adInstance = ad
    }

    fun getAdData(): Any? {
        return adInstance
    }

    fun destroyAd(container: ViewGroup?) {
        (adInstance as? View)?.let {
            container?.removeView(it)
            container?.visibility = View.GONE
            (it as? AdView)?.apply {
                destroy()
            }

            (it as? com.facebook.ads.AdView)?.apply {
                destroy()
            }
        }

        (adInstance as? NativeBannerAd)?.apply {
            unregisterView()
            val currentView = container?.findViewWithTag<NativeAdLayout>("fb_dynamic_view")
            container?.removeView(currentView)
        }

        (adInstance as? NativeAd)?.apply {
            Log.d(TAG, "fb native ad destroyed")
            unregisterView()
            val currentView = container?.findViewWithTag<NativeAdLayout>("fb_dynamic_view")
            container?.removeView(currentView)
        }

        (adInstance as? InterstitialAd)?.apply {
            destroy()
        }
    }

    fun render(container: ViewGroup, viewMap: Map<String, View> = emptyMap()) {
        if (adType == AdType.BANNER) {
            renderBanner(container)
            eventType = AdEventType.used
        } else if (adType == AdType.NATIVE_BANNER) {
            renderNativeBanner(container, viewMap)
            eventType = AdEventType.used
        } else if (adType == AdType.NATIVE) {
            renderNative(container, viewMap)
            eventType = AdEventType.used
        } else if (adType == AdType.INTERSTITIAL) {
            renderInter()
            eventType = AdEventType.used
        }
    }

    private fun renderInter() {
        (adInstance as? InterstitialAd)?.let {
            if (it.isAdLoaded) {
                it.show()
            }
        }

        (adInstance as? com.google.android.gms.ads.InterstitialAd)?.let {
            if (it.isLoaded) {
                it.show()
            }
        }
    }

    private fun renderBanner(container: ViewGroup) {
        (adInstance as? View)?.let {
            container.addView(it)

            if (container is RelativeLayout) {
                val layoutParams = RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT)
                layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT, 1)
                it.layoutParams = layoutParams
            }
        }
    }

    private fun renderNative(container: ViewGroup, viewMap: Map<String, View>) {
        (adInstance as? NativeAd)?.let {
            renderFBNative(container, viewMap)
        }

        (adInstance as? UnifiedNativeAd)?.let {
            renderAdMobNative(container, viewMap)
        }
    }

    private fun renderFBNative(container: ViewGroup, viewMap: Map<String, View>) {
        val nativeAd = (adInstance as? NativeAd)?.apply {
        } ?: run {
            return
        }

        Log.d(TAG, "renderFBNative for ${nativeAd}")
        nativeAd.unregisterView()

        val rootView = viewMap["root"] as NativeAdLayout
        val prevView = container.findViewWithTag<NativeAdLayout>(rootView.tag)
        container.removeView(prevView)

        val title = viewMap["title"] as TextView
        val icon = viewMap["icon"] as AdIconView
        val sponsor = viewMap["sponsor"] as TextView
        val socialContext = viewMap["socialContext"] as TextView
        val adBody = viewMap["adBody"] as TextView
        val cta = viewMap["cta"] as Button
        val adChoiceContainer = viewMap["adChoiceContainer"] as LinearLayout
        val mediaView = viewMap["mediaView"] as MediaView

        rootView.visibility = View.VISIBLE
        title.text = nativeAd.advertiserName
        sponsor.text = nativeAd.sponsoredTranslation
        socialContext.text = nativeAd.adSocialContext
        adBody.text = nativeAd.adBodyText
        cta.text = nativeAd.adCallToAction

        val adOptions = AdOptionsView(title.context, nativeAd, rootView)
        adOptions.setIconColor(Color.CYAN)
        adOptions.setIconSizeDp(23)
        adChoiceContainer.addView(adOptions)

        nativeAd.registerViewForInteraction(rootView, mediaView, icon,
            mutableListOf(cta as View, socialContext as View, adBody as View, mediaView as View))

        container.addView(rootView)
        container.visibility = View.VISIBLE
    }

    private fun renderAdMobNative(container: ViewGroup, viewMap: Map<String, View>) {
        val nativeAd = (adInstance as? UnifiedNativeAd)?.apply {
        } ?: run {
            return
        }

        val adView = viewMap["root"] as UnifiedNativeAdView
        val prevView = container.findViewWithTag<UnifiedNativeAdView>(adView.tag)
        container.removeView(prevView)

        // The headline is guaranteed to be in every UnifiedNativeAd.
        (adView.headlineView as TextView).text = nativeAd.headline

        // These assets aren't guaranteed to be in every UnifiedNativeAd, so it's important to
        // check before trying to display them.
        if (nativeAd.body == null) {
            adView.bodyView.visibility = View.INVISIBLE
        } else {
            adView.bodyView.visibility = View.VISIBLE
            (adView.bodyView as TextView).text = nativeAd.body
        }

        if (nativeAd.callToAction == null) {
            adView.callToActionView.visibility = View.INVISIBLE
        } else {
            adView.callToActionView.visibility = View.VISIBLE
            (adView.callToActionView as Button).text = nativeAd.callToAction
        }

        if (nativeAd.icon == null) {
            adView.iconView.visibility = View.GONE
        } else {
            (adView.iconView as ImageView).setImageDrawable(
                nativeAd.icon.drawable
            )
            adView.iconView.visibility = View.VISIBLE
        }

        if (nativeAd.price == null) {
            adView.priceView.visibility = View.GONE
        } else {
            adView.priceView.visibility = View.VISIBLE
            (adView.priceView as TextView).text = nativeAd.price
        }

        if (nativeAd.store == null) {
            adView.storeView.visibility = View.GONE
        } else {
            adView.storeView.visibility = View.VISIBLE
            (adView.storeView as TextView).text = nativeAd.store
        }

        if (nativeAd.starRating == null) {
            adView.starRatingView.visibility = View.GONE
        } else {
            (adView.starRatingView as RatingBar).rating = nativeAd.starRating!!.toFloat()
            adView.starRatingView.visibility = View.VISIBLE
        }

        if (nativeAd.advertiser == null) {
            adView.advertiserView.visibility = View.GONE
        } else {
            (adView.advertiserView as TextView).text = nativeAd.advertiser
            adView.advertiserView.visibility = View.VISIBLE
        }

        // This method tells the Google Mobile Ads SDK that you have finished populating your
        // native ad view with this native ad. The SDK will populate the adView's MediaView
        // with the media content from this native ad.
        adView.setNativeAd(nativeAd)
        container.addView(adView)
        // adView.mediaView.viewTreeObserver.addOnGlobalLayoutListener(layoutUpdateListener)
        container.visibility = View.VISIBLE
    }

    private fun renderFBNativeBanner(container: ViewGroup, viewMap: Map<String, View>) {
        val nativeAd = (adInstance as? NativeBannerAd)?.apply {
        } ?: run {
            return
        }

        val rootView = viewMap["root"] as NativeAdLayout
        val prevView = container.findViewWithTag<NativeAdLayout>(rootView.tag)
        container.removeView(prevView)

        val title = viewMap["title"] as TextView
        val icon = viewMap["icon"] as AdIconView
        val sponsor = viewMap["sponsor"] as TextView
        val socialContext = viewMap["socialContext"] as TextView
        val cta = viewMap["cta"] as Button
        val adChoiceContainer = viewMap["adChoiceContainer"] as LinearLayout

        rootView.visibility = View.VISIBLE
        title.text = nativeAd.advertiserName
        sponsor.text = nativeAd.sponsoredTranslation
        socialContext.text = nativeAd.adSocialContext
        cta.text = nativeAd.adCallToAction

        adChoiceContainer.removeAllViews()
        val optionView = AdOptionsView(title.context, nativeAd, rootView)
        optionView.setIconColor(Color.CYAN)

        adChoiceContainer.addView(optionView)

        nativeAd.registerViewForInteraction(rootView, icon,
            mutableListOf(cta as View, socialContext as View, title as View, icon as View))

        if (container is RelativeLayout) {
            val layoutParams = RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                container.resources.dp2px(50f)
            )
            rootView.layoutParams = layoutParams
        }

        container.addView(rootView)
        val params = rootView.layoutParams
        params.width = ViewGroup.LayoutParams.MATCH_PARENT
        rootView.layoutParams = params
        container.visibility = View.VISIBLE
    }

    private fun renderAdMobNativeBanner(container: ViewGroup, viewMap: Map<String, View>) {
        val nativeAd = (adInstance as? UnifiedNativeAd)?.apply {
        } ?: run {
            return
        }

        val adView = viewMap["root"] as UnifiedNativeAdView
        val prevView = container.findViewWithTag<UnifiedNativeAdView>(adView.tag)
        container.removeView(prevView)

        // The headline is guaranteed to be in every UnifiedNativeAd.
        (adView.headlineView as TextView).text = nativeAd.headline

        // These assets aren't guaranteed to be in every UnifiedNativeAd, so it's important to
        // check before trying to display them.
        if (nativeAd.body == null) {
            adView.bodyView.visibility = View.INVISIBLE
        } else {
            adView.bodyView.visibility = View.VISIBLE
            (adView.bodyView as TextView).text = nativeAd.body
        }

        if (nativeAd.callToAction == null) {
            adView.callToActionView.visibility = View.INVISIBLE
        } else {
            adView.callToActionView.visibility = View.VISIBLE
            (adView.callToActionView as Button).text = nativeAd.callToAction
        }

        if (nativeAd.icon == null) {
            adView.iconView.visibility = View.GONE
        } else {
            (adView.iconView as ImageView).setImageDrawable(
                nativeAd.icon.drawable
            )
            adView.iconView.visibility = View.VISIBLE
        }

        if (nativeAd.advertiser == null) {
            adView.advertiserView.visibility = View.GONE
        } else {
            (adView.advertiserView as TextView).text = nativeAd.advertiser
            adView.advertiserView.visibility = View.VISIBLE
        }

        // This method tells the Google Mobile Ads SDK that you have finished populating your
        // native ad view with this native ad. The SDK will populate the adView's MediaView
        // with the media content from this native ad.
        adView.setNativeAd(nativeAd)
        container.addView(adView)
        // adView.mediaView.viewTreeObserver.addOnGlobalLayoutListener(layoutUpdateListener)
        container.visibility = View.VISIBLE
    }

    fun renderNativeBanner(container: ViewGroup, viewMap: Map<String, View>) {
        (adInstance as? NativeBannerAd)?.apply {
            renderFBNativeBanner(container, viewMap)
        }

        (adInstance as? UnifiedNativeAd)?.apply {
            renderAdMobNativeBanner(container, viewMap)
        }
    }
}

fun Resources.dp2px(dpValue: Float): Int {
    val scale = Resources.getSystem().displayMetrics.density
    return (dpValue * scale + 0.5f).toInt()
}