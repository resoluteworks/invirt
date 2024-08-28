package invirt.data.geo

data class GeoBoundingBox(
    val southWest: GeoLocation,
    val northEast: GeoLocation
) {

    /**
     * Clockwise list of points on this geo bounds rectangle
     */
    val points: List<GeoLocation> = listOf(
        southWest,
        GeoLocation(southWest.lng, northEast.lat),
        northEast,
        GeoLocation(northEast.lng, southWest.lat)
    )

    fun toLngLatString(): String = "${southWest.lng},${southWest.lat},${northEast.lng},${northEast.lat}"
    fun toLatLngString(): String = "${southWest.lat},${southWest.lng},${northEast.lat},${northEast.lng}"

    fun pad(delta: Double): GeoBoundingBox = GeoBoundingBox(
        southWest = GeoLocation.normalised(lng = southWest.lng + delta, lat = southWest.lat + delta),
        northEast = GeoLocation.normalised(lng = northEast.lng - delta, lat = northEast.lat - delta)
    )

    companion object {
        operator fun invoke(bbox: List<Double>): GeoBoundingBox {
            if (bbox.size != 4) {
                throw IllegalArgumentException("Bounding box coordinates array must be of size 4")
            }
            return GeoBoundingBox(
                southWest = GeoLocation.normalised(lng = bbox[0], lat = bbox[1]),
                northEast = GeoLocation.normalised(lng = bbox[2], lat = bbox[3])
            )
        }

        operator fun invoke(vararg bbox: Double): GeoBoundingBox = GeoBoundingBox(bbox.toList())

        fun fromLngLatString(bbox: String): GeoBoundingBox = GeoBoundingBox(bbox.split(",").map { it.toDouble() })

        fun fromLatLngString(bbox: String): GeoBoundingBox {
            val elements = bbox.split(",").map { it.toDouble() }
            if (elements.size != 4) {
                throw IllegalArgumentException("Bounding box coordinates array must be of size 4")
            }
            return GeoBoundingBox(elements[1], elements[0], elements[3], elements[2])
        }
    }
}
