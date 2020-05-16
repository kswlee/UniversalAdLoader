package golab.ad

data class AdDesc(val source: String,
                  val type: String,
                  val unitId: String)

object AdUnitIdMapper {
    fun adMobId(id: String, type: AdType): String {
        return "admob|${getTypeString(type)}|${id}"
    }

    fun fbId(id: String, type: AdType): String {
        return "fb|${getTypeString(type)}|${id}"
    }

    fun moPubId(id: String, type: AdType): String {
        return "mopub|${getTypeString(type)}|${id}"
    }

    fun toAdDesc(compoundId: String): AdDesc {
        val tokens = compoundId.split("|")
        return AdDesc(tokens[0], tokens[1], tokens[2])
    }

    private fun getTypeString(type: AdType): String {
        return when(type) {
            AdType.BANNER -> "banner"
            AdType.NATIVE -> "native"
            AdType.NATIVE_BANNER -> "nativebanner"
            AdType.INTERSTITIAL -> "interstitial"
        }
    }
}