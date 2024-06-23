package invirt.data.geo

data class GeoLocation(
    val lng: Double,
    val lat: Double,
    val lngLat: List<Double> = listOf(lng, lat)
) {

    companion object {
        fun normalised(lng: Double, lat: Double): GeoLocation {
            return GeoLocation(
                lng = lng.coerceAtMost(180.0).coerceAtLeast(-180.0),
                lat = lat.coerceAtMost(90.0).coerceAtLeast(-90.0)
            )
        }
    }
}
