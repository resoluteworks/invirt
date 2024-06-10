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
}
