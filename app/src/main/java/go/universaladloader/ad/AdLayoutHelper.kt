package go.universaladloader.ad

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import com.facebook.ads.NativeAdLayout
import com.google.android.gms.ads.formats.MediaView
import com.google.android.gms.ads.formats.UnifiedNativeAdView
import go.universaladloader.R
import golab.ad.AdType
import golab.ad.HybridAd

object AdLayoutHelper {
    fun prepareNativeLayout(context: Context, id: Int, hybridAd: HybridAd): Map<String, View> {
        if (hybridAd.adType == AdType.BANNER) {
            return emptyMap()
        }

        if (hybridAd.adSource == "admob") {
            if (hybridAd.adType == AdType.NATIVE)
                return prepareNativeAdMobLayout(context, id)
            else if (hybridAd.adType == AdType.NATIVE_BANNER)
                return prepareNativeAdMobBannerLayout(context, id)
        } else if (hybridAd.adSource == "fb") {
            if (hybridAd.adType == AdType.NATIVE_BANNER)
                return prepareNativeFBBannerLayout(context, id)
            else if (hybridAd.adType == AdType.NATIVE)
                return prepareNativeFBLayout(context, id)
        }

        return prepareNativeFBLayout(context, id)
    }

    private fun prepareNativeFBBannerLayout(context: Context, id: Int): Map<String, View> {
        val adView = LayoutInflater.from(context).inflate(id , null) as NativeAdLayout
        adView.tag = "fb_dynamic_view"

        val rootView: NativeAdLayout = adView
        val icon: com.facebook.ads.AdIconView? = rootView.findViewById(R.id.native_ad_icon)
        val title: TextView = rootView.findViewById(R.id.native_ad_title)
        val sponsor: TextView = rootView.findViewById(R.id.native_ad_sponsored_label)
        val socialContext: TextView = rootView.findViewById(R.id.native_ad_social_context)
        val cta: Button = rootView.findViewById(R.id.native_ad_call_to_action)
        val adChoiceContainer: LinearLayout = rootView.findViewById(R.id.ad_choices_container)

        val viewMap: MutableMap<String, View> = mutableMapOf()

        viewMap["root"] = adView
        viewMap["icon"] = icon as View
        viewMap["title"] = title as View
        viewMap["sponsor"] = sponsor as View
        viewMap["socialContext"] = socialContext as View
        viewMap["cta"] = cta as View
        viewMap["adChoiceContainer"] = adChoiceContainer as View

        return viewMap
    }

    private fun prepareNativeFBLayout(context: Context, id: Int): Map<String, View> {
        val adView = LayoutInflater.from(context).inflate(id , null) as NativeAdLayout
        adView.tag = "fb_dynamic_view"
        // val currentView = fbContainer.findViewWithTag<NativeAdLayout>("fb_dynamic_view")
        // fbContainer.removeView(currentView)

        val rootView: NativeAdLayout = adView
        val icon: com.facebook.ads.AdIconView? = rootView.findViewById(R.id.native_ad_icon)
        val title: TextView = rootView.findViewById(R.id.native_ad_title)
        val sponsor: TextView = rootView.findViewById(R.id.native_ad_sponsored_label)
        val mediaView: com.facebook.ads.MediaView = rootView.findViewById(R.id.native_ad_media)
        val socialContext: TextView = rootView.findViewById(R.id.native_ad_social_context)
        val adBody: TextView = rootView.findViewById(R.id.native_ad_body)
        val cta: Button = rootView.findViewById(R.id.native_ad_call_to_action)
        val adChoiceContainer: LinearLayout = rootView.findViewById(R.id.ad_choices_container)

        val viewMap: MutableMap<String, View> = mutableMapOf()

        viewMap["root"] = adView
        viewMap["icon"] = icon as View
        viewMap["title"] = title as View
        viewMap["adBody"] = adBody as View
        viewMap["sponsor"] = sponsor as View
        viewMap["mediaView"] = mediaView as View
        viewMap["socialContext"] = socialContext as View
        viewMap["cta"] = cta as View
        viewMap["adChoiceContainer"] = adChoiceContainer as View

        return viewMap
    }

    private fun prepareNativeAdMobBannerLayout(context: Context, id: Int): Map<String, View> {
        val adView = LayoutInflater.from(context).inflate(id , null) as UnifiedNativeAdView
        adView.tag = "admob_dynamic_view"

        //adView.mediaView = adView.findViewById<MediaView>(R.id.ad_media)
        //adView.priceView = adView.findViewById(R.id.ad_price)
        //adView.starRatingView = adView.findViewById(R.id.ad_stars)
        //adView.storeView = adView.findViewById(R.id.ad_store)
        adView.headlineView = adView.findViewById(R.id.native_ad_title)
        adView.bodyView = adView.findViewById(R.id.native_ad_social_context)
        adView.callToActionView = adView.findViewById(R.id.native_ad_call_to_action)
        adView.iconView = adView.findViewById(R.id.native_ad_icon)
        adView.advertiserView = adView.findViewById(R.id.native_ad_sponsored_label)

        val viewMap: MutableMap<String, View> = mutableMapOf()

        viewMap["root"] = adView

        return viewMap
    }

    private fun prepareNativeAdMobLayout(context: Context, id: Int): Map<String, View> {
        val adView = LayoutInflater.from(context).inflate(id, null) as UnifiedNativeAdView
        adView.tag = "admob_dynamic_view"

        adView.mediaView = adView.findViewById<MediaView>(R.id.ad_media)
        adView.headlineView = adView.findViewById(R.id.ad_headline)
        adView.bodyView = adView.findViewById(R.id.ad_body)
        adView.callToActionView = adView.findViewById(R.id.ad_call_to_action)
        adView.iconView = adView.findViewById(R.id.ad_app_icon)
        adView.priceView = adView.findViewById(R.id.ad_price)
        adView.starRatingView = adView.findViewById(R.id.ad_stars)
        adView.storeView = adView.findViewById(R.id.ad_store)
        adView.advertiserView = adView.findViewById(R.id.ad_advertiser)

        val viewMap: MutableMap<String, View> = mutableMapOf()

        viewMap["root"] = adView

        return viewMap
    }
}