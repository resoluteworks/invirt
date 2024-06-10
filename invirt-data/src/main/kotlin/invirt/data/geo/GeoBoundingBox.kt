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

    companion object {
        operator fun invoke(bbox: List<Double>): GeoBoundingBox {
            if (bbox.size != 4) {
                throw IllegalArgumentException("Bounding box coordinates array must be of size 4")
            }
            return GeoBoundingBox(
                southWest = GeoLocation(lng = bbox[0], lat = bbox[1]),
                northEast = GeoLocation(lng = bbox[2], lat = bbox[3])
            )
        }

        operator fun invoke(vararg bbox: Double): GeoBoundingBox = GeoBoundingBox(bbox.toList())
        operator fun invoke(bbox: String): GeoBoundingBox = GeoBoundingBox(bbox.split(",").map { it.toDouble() })
    }
}
