package golab.ad

import android.content.Context
import android.util.Log
import android.view.View
import android.view.ViewGroup
import com.facebook.ads.*
import com.facebook.ads.AdSize
import com.google.android.gms.ads.*
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.InterstitialAd
import com.google.android.gms.ads.formats.NativeAdOptions
import com.mopub.mobileads.MoPubErrorCode
import com.mopub.mobileads.MoPubView
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import java.lang.Error


object AdLoader {
    private val TAG = "AdLoaderTAG"

    fun loadByOrder(context: Context, unitIds: List<String>): Observable<HybridAd> {
        val observables = toObservableCandidates(context, unitIds).toMutableList()
        val consumer = createConsumerObservable(observables)

        return consumer
            .flatMap {
                it
            }
            .doOnError {
                Log.d(TAG, "doOnError ${it}")
                it.printStackTrace()
            }
            .map {
                if (it.eventType == AdEventType.failed) {
                    Log.d(TAG, "failed ${it.unitId}")
                    throw Error("ad load failed")
                }

                it
            }
            .retry(unitIds.size.toLong())
            .map {
                if (it.eventType == AdEventType.flow_failed) {
                    Log.d(TAG, "flow failed ${it.unitId}")
                    throw Error("no ad availabe")
                }

                it
            }
    }

    private fun toObservableCandidates(context: Context, unitIds: List<String>): List<Observable<HybridAd>> {
        var partsList = unitIds.map {
            AdUnitIdMapper.toAdDesc(it)
        }

        return partsList.map {
            if (it.source == "admob") {
                if (it.type == "native") {
                    return@map createAdMobNativeRequestObservable(context, it.unitId)
                } else if (it.type == "banner") {
                    return@map createAdMobBannerRequestObservable(context, it.unitId)
                } else if (it.type == "nativebanner") {
                    return@map createAdMobNativeBannerRequestObservable(context, it.unitId)
                } else if (it.type == "interstitial") {
                    return@map createAdMobInterRequestObservable(context, it.unitId)
                }
            } else if (it.source == "fb") {
                if (it.type == "native") {
                    return@map createFBNativeRequestObservable(context, it.unitId)
                } else if (it.type == "banner") {
                    return@map createFBBannerRequestObservable(context, it.unitId)
                } else if (it.type == "nativebanner") {
                    return@map createFBNativeBannerRequestObservable(context, it.unitId)
                } else if (it.type == "interstitial") {
                    return@map createFBInterRequestObservable(context, it.unitId)
                }
            } else if (it.source == "mopub") {
                if (it.type == "banner") {
                    return@map createMoPubBannerRequestObservable(context, it.unitId)
                }
            }

            createAdMobNativeRequestObservable(context, it.unitId)
        }
    }

    private fun createConsumerObservable(observables: List<Observable<HybridAd>>): Observable<Observable<HybridAd>> {
        val mutableList = observables.toMutableList()
        return Observable.create<Observable<HybridAd>> { emitter ->
            Log.d(TAG, "consuming mutableList size : ${mutableList.size}")
            if (mutableList.isEmpty()) {
                val emptyAd = HybridAd(null, "", "", AdType.NATIVE, AdEventType.flow_failed)
                emitter.onNext( Observable.just(emptyAd) )
            } else {
                val candidate = mutableList.removeAt(0)
                emitter.onNext(candidate)
            }
        }
    }

    private fun createMoPubBannerRequestObservable(context: Context, unitId: String): Observable<HybridAd> {
        return Observable.create<HybridAd> { emitter ->
            Log.d(TAG, "createMoPubBannerRequestObservable unitId ${unitId}")
            val mopubView = MoPubView(context)
            mopubView.adUnitId = unitId
            mopubView.adSize = MoPubView.MoPubAdSize.HEIGHT_50
            val hybridAd = HybridAd(mopubView, unitId, "mopub", AdType.BANNER, AdEventType.default)
            mopubView.bannerAdListener = object: MoPubView.BannerAdListener {
                override fun onBannerExpanded(banner: MoPubView?) {
                    hybridAd.onImpressed()
                }

                override fun onBannerLoaded(banner: MoPubView?) {
                    Log.d(TAG,"loadMoPubAd loaded")
                    hybridAd.eventType = AdEventType.loaded
                    emitter.onNext(hybridAd)
                }

                override fun onBannerCollapsed(banner: MoPubView?) {
                }

                override fun onBannerFailed(banner: MoPubView?, errorCode: MoPubErrorCode?) {
                    Log.d(TAG,"loadMoPubAd failed")
                    hybridAd.eventType = AdEventType.failed
                    emitter.onNext(hybridAd)
                }

                override fun onBannerClicked(banner: MoPubView?) {
                    hybridAd.onClicked()
                }
            }

            mopubView.loadAd(MoPubView.MoPubAdSize.HEIGHT_90)
        }.subscribeOn(AndroidSchedulers.mainThread())
    }

    private fun createAdMobBannerRequestObservable(context: Context, unitId: String): Observable<HybridAd> {
        return Observable.create<HybridAd> { emitter ->
            val adMobAdView = AdView(context)
            val hybridAd = HybridAd(adMobAdView, unitId, "admob", AdType.BANNER, AdEventType.default)
            adMobAdView.adUnitId = unitId
            adMobAdView.adSize = com.google.android.gms.ads.AdSize.BANNER
            adMobAdView.adListener = object: com.google.android.gms.ads.AdListener() {
                override fun onAdFailedToLoad(var1: Int) {
                    Log.d(TAG, "onAdFailedToLoad")
                    hybridAd.eventType = AdEventType.failed
                    emitter.onNext(hybridAd)
                }

                override fun onAdLoaded() {
                    Log.d(TAG, "onAdLoaded")
                    hybridAd.eventType = AdEventType.loaded
                    emitter.onNext(hybridAd)
                }

                override fun onAdOpened() {
                    super.onAdOpened()
                    Log.d(TAG, "onAdOpened")
                }

                override fun onAdLeftApplication() {
                    super.onAdLeftApplication()
                    Log.d(TAG, "onAdLeftApplication")
                    hybridAd.onClicked()
                }

                override fun onAdClicked() {
                    hybridAd.onClicked()
                }

                override fun onAdImpression() {
                    hybridAd.onImpressed()
                }
            }

            adMobAdView.loadAd(AdRequest.Builder().build())
        }.subscribeOn(AndroidSchedulers.mainThread())
    }

    private fun createFBBannerRequestObservable(context: Context, unitId: String): Observable<HybridAd> {
        return Observable.create<HybridAd> { emitter ->
            Log.d(TAG, "createFBBannerRequestObservable unitId ${unitId}")
            val fbAdView = com.facebook.ads.AdView(context, unitId, AdSize.BANNER_HEIGHT_50)
            val hybridAd = HybridAd(fbAdView, unitId, "fb", AdType.BANNER, AdEventType.default)
            fbAdView.setAdListener(object: com.facebook.ads.AdListener {
                override fun onAdClicked(p0: Ad?) {
                    hybridAd.onClicked()
                }
                override fun onLoggingImpression(p0: Ad?) {
                    hybridAd.onImpressed()
                }

                override fun onError(p0: Ad?, p1: AdError?) {
                    Log.d(TAG, "onError ${unitId}")
                    hybridAd.eventType = AdEventType.failed
                    emitter.onNext(hybridAd)
                }

                override fun onAdLoaded(p0: Ad?) {
                    Log.d(TAG, "onAdLoaded ${unitId}")
                    hybridAd.eventType = AdEventType.loaded
                    emitter.onNext(hybridAd)
                }
            })

            Log.d(TAG, "going to load unitId ${unitId}")
            fbAdView.loadAd()
        }.subscribeOn(AndroidSchedulers.mainThread())
    }

    private fun createAdMobNativeBannerRequestObservable(context: Context, unitId: String): Observable<HybridAd> {
        return Observable.create<HybridAd> { emitter ->
            Log.d(TAG, "createAdMobNativeBannerRequestObservable unitId ${unitId}")
            val builder = AdLoader.Builder(context, unitId)

            val hybridAd = HybridAd(null, unitId, "admob", AdType.NATIVE_BANNER, AdEventType.default)
            builder.forUnifiedNativeAd { unifiedNativeAd ->
                Log.d(TAG, "ad loaded ${unifiedNativeAd}")
                hybridAd.setAdData(unifiedNativeAd)
                hybridAd.eventType = AdEventType.loaded
                emitter.onNext(hybridAd)
            }

            val videoOptions = VideoOptions.Builder().setStartMuted(true).build()
            val adOptions = NativeAdOptions.Builder().setVideoOptions(videoOptions).build()

            builder.withNativeAdOptions(adOptions)

            val adLoader = builder.withAdListener(object : AdListener() {
                override fun onAdClicked() {
                    hybridAd.onClicked()
                }

                override fun onAdImpression() {
                    hybridAd.onImpressed()
                }

                override fun onAdFailedToLoad(errorCode: Int) {
                    Log.d(TAG, "admob onAdFailedToLoad ${errorCode}")
                    hybridAd.setAdData(null)
                    hybridAd.eventType = AdEventType.failed
                    emitter.onNext(hybridAd)
                }
            }).build()

            Log.d(TAG, "load for ${unitId} build")
            adLoader.loadAd(AdRequest.Builder().build())
        }
    }

    private fun createAdMobNativeRequestObservable(context: Context, unitId: String): Observable<HybridAd> {
        return Observable.create<HybridAd> { emitter ->
            Log.d(TAG, "createAdMobNativeRequestObservable unitId ${unitId}")
            val builder = AdLoader.Builder(context, unitId)

            val hybridAd = HybridAd(null, unitId, "admob", AdType.NATIVE, AdEventType.default)
            builder.forUnifiedNativeAd { unifiedNativeAd ->
                Log.d(TAG, "ad loaded ${unifiedNativeAd}")
                hybridAd.setAdData(unifiedNativeAd)
                hybridAd.eventType = AdEventType.loaded
                emitter.onNext(hybridAd)
            }

            val videoOptions = VideoOptions.Builder().setStartMuted(true).build()
            val adOptions = NativeAdOptions.Builder().setVideoOptions(videoOptions).build()

            builder.withNativeAdOptions(adOptions)

            val adLoader = builder.withAdListener(object : AdListener() {
                override fun onAdClicked() {
                    hybridAd.onClicked()
                }

                override fun onAdImpression() {
                    hybridAd.onImpressed()
                }

                override fun onAdFailedToLoad(errorCode: Int) {
                    hybridAd.setAdData(null)
                    hybridAd.eventType = AdEventType.failed
                    emitter.onNext(hybridAd)
                }
            }).build()

            Log.d(TAG, "load for ${unitId} build")
            adLoader.loadAd(AdRequest.Builder().build())
        }
    }

    private fun createFBNativeRequestObservable(context: Context, unitId: String): Observable<HybridAd> {
        return Observable.create<HybridAd> { emitter ->
            Log.d(TAG, "createFBNativeRequestObservable unitId ${unitId}")
            val nativeAd = NativeAd(context, unitId)
            val hybridAd = HybridAd(nativeAd, unitId, "fb", AdType.NATIVE, AdEventType.default)
            nativeAd.setAdListener(object: NativeAdListener {
                override fun onAdClicked(p0: Ad?) {
                    hybridAd.onClicked()
                }

                override fun onMediaDownloaded(p0: Ad?) {
                }

                override fun onError(p0: Ad?, p1: AdError?) {
                    hybridAd.eventType = AdEventType.failed
                    emitter.onNext(hybridAd)
                }

                override fun onAdLoaded(p0: Ad?) {
                    hybridAd.eventType = AdEventType.loaded
                    emitter.onNext(hybridAd)
                }

                override fun onLoggingImpression(p0: Ad?) {
                    hybridAd.onImpressed()
                }
            })

            nativeAd.loadAd()
        }
        .subscribeOn(AndroidSchedulers.mainThread())
    }

    private fun createFBNativeBannerRequestObservable(context: Context, unitId: String): Observable<HybridAd> {
        return Observable.create<HybridAd> { emitter ->
            Log.d(TAG, "createFBNativeBannerRequestObservable unitId ${unitId}")
            val nativeBannerAd = NativeBannerAd(context, unitId)
            val hybridAd = HybridAd(nativeBannerAd, unitId, "fb", AdType.NATIVE_BANNER, AdEventType.default)
            nativeBannerAd.setAdListener(object: NativeAdListener {
                override fun onAdClicked(p0: Ad?) {
                    hybridAd.onClicked()
                }

                override fun onMediaDownloaded(p0: Ad?) {
                }

                override fun onError(p0: Ad?, p1: AdError?) {
                    Log.d(TAG, "createFBNativeBannerRequestObservable onError unitId ${unitId}")
                    hybridAd.eventType = AdEventType.failed
                    emitter.onNext(hybridAd)
                }

                override fun onAdLoaded(p0: Ad?) {
                    Log.d(TAG, "createFBNativeBannerRequestObservable onAdLoaded unitId ${unitId}")
                    hybridAd.eventType = AdEventType.loaded
                    emitter.onNext(hybridAd)
                }

                override fun onLoggingImpression(p0: Ad?) {
                    hybridAd.onImpressed()
                }
            })

            nativeBannerAd.loadAd()
        }
        .subscribeOn(AndroidSchedulers.mainThread())
    }

    private fun createAdMobInterRequestObservable(context: Context, unitId: String): Observable<HybridAd> {
        return Observable.create<HybridAd> { emitter ->
            Log.d(TAG, "createAdMobInterRequestObservable unitId ${unitId}")
            MobileAds.setAppMuted(true)
            val interAd = InterstitialAd(context)
            val hybridAd = HybridAd(interAd, unitId, "fb", AdType.INTERSTITIAL, AdEventType.default)

            interAd.adListener = object : AdListener() {
                override fun onAdLoaded() {
                    hybridAd.eventType = AdEventType.loaded
                    emitter.onNext(hybridAd)
                }

                override fun onAdFailedToLoad(var1: Int) {
                    hybridAd.eventType = AdEventType.failed
                    emitter.onNext(hybridAd)
                }

                override fun onAdLeftApplication() {
                    hybridAd.onClicked()
                }

                override fun onAdOpened() {
                    hybridAd.onImpressed()
                }

                override fun onAdClicked() {
                    hybridAd.onClicked()
                }

                override fun onAdImpression() {
                    hybridAd.onImpressed()
                }
            }

            interAd.adUnitId = unitId
            interAd.loadAd(AdRequest.Builder().build())
        }
        .subscribeOn(AndroidSchedulers.mainThread())
    }

    private fun createFBInterRequestObservable(context: Context, unitId: String): Observable<HybridAd> {
        return Observable.create<HybridAd> { emitter ->
            Log.d(TAG, "createFBInterRequestObservable unitId ${unitId}")
            val interAd = com.facebook.ads.InterstitialAd(context, unitId)
            val hybridAd = HybridAd(interAd, unitId, "fb", AdType.INTERSTITIAL, AdEventType.default)

            interAd.setAdListener(object: InterstitialAdListener {
                override fun onInterstitialDisplayed(p0: Ad?) {
                }

                override fun onAdClicked(p0: Ad?) {
                    hybridAd.onClicked()
                }

                override fun onInterstitialDismissed(p0: Ad?) {
                }

                override fun onError(p0: Ad?, p1: AdError?) {
                    hybridAd.eventType = AdEventType.failed
                    emitter.onNext(hybridAd)
                }

                override fun onAdLoaded(p0: Ad?) {
                    hybridAd.eventType = AdEventType.loaded
                    emitter.onNext(hybridAd)
                }

                override fun onLoggingImpression(p0: Ad?) {
                    hybridAd.onImpressed()
                }

            })

            interAd.loadAd()
        }
        .subscribeOn(AndroidSchedulers.mainThread())
    }
}