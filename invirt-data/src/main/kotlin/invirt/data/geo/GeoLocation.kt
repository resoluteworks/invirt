package invirt.data.geo

data class GeoLocation(
    val lng: Double,
    val lat: Double,
    val lngLat: List<Double> = listOf(lng, lat)
)
