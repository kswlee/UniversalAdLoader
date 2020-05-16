package go.universaladloader.ad

import golab.ad.AdType
import golab.ad.AdUnitIdMapper

object AdUnits {
    private val ADMOB_BANNER_MAIN = AdUnitIdMapper.adMobId("ca-app-pub-3940256099942544/6300978111", AdType.BANNER)
    private val ADMOB_NATIVEBANNER_MAIN = AdUnitIdMapper.adMobId("ca-app-pub-3940256099942544/2247696110", AdType.NATIVE_BANNER)

    private val FB_NATIVEBANNER_MAIN = AdUnitIdMapper.fbId("YOUR_PLACEMENT_ID", AdType.NATIVE_BANNER)
    private val FB_BANNER_MAIN = AdUnitIdMapper.fbId("YOUR_PLACEMENT_ID", AdType.BANNER)

    private val MOPUB_BANNER_MAIN = AdUnitIdMapper.moPubId("9dd8e74bcf204ad6a16782240cb458af", AdType.BANNER)

    val MAIN_BANNER_UNITS = listOf(ADMOB_BANNER_MAIN, FB_NATIVEBANNER_MAIN, ADMOB_NATIVEBANNER_MAIN, FB_BANNER_MAIN, MOPUB_BANNER_MAIN)
}