package go.universaladloader

import android.os.Bundle
import android.util.Log
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import com.google.android.gms.ads.MobileAds
import go.universaladloader.ad.AdLayoutHelper
import go.universaladloader.ad.AdUnits
import go.universaladloader.extension.disposeTo
import golab.ad.AdEventType
import golab.ad.AdLoader
import golab.ad.HybridAd
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.subscribeBy

import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*

class MainActivity : AppCompatActivity() {
    companion object {
        private const val TAG = "MainActivityTAG"
    }

    private val compositeDisposable = CompositeDisposable()
    private var activeBannerAd: HybridAd? = null
        set(value) {
            field?.destroyAd(adContainer)
            field = value
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        MobileAds.initialize(this)
        initAdBanner()
    }

    override fun onDestroy() {
        super.onDestroy()
        activeBannerAd = null
    }

    private fun initAdBanner() {
        AdLoader
            .loadByOrder(this, AdUnits.MAIN_BANNER_UNITS)
            .filter {
                it.eventType == AdEventType.loaded
            }
            .take(1)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy(onNext = {
                activeBannerAd = it

                val nativeLayout = if (it.adSource == "fb") R.layout.native_ad_banner_fb_view else R.layout.native_ad_banner_admob_view
                val nativeBannerViewMap = AdLayoutHelper.prepareNativeLayout(this, nativeLayout, it)

                it.render(adContainer, nativeBannerViewMap)
                adContainer.visibility = View.VISIBLE
            }, onError = {
                it.printStackTrace()
                Log.e(TAG, "$it")
            })
            .disposeTo(compositeDisposable)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }
}
