---
sidebar_position: 4
---

# Geo

Lightweight value types for geographic coordinates, used by
[`DataFilter.Field.WithinGeoBounds`](/docs/api/invirt-data/data-filter#datafilterfield) and the
[MongoDB integration](/docs/api/invirt-mongodb/filters).

## GeoLocation
```kotlin
data class GeoLocation(
    val lng: Double,
    val lat: Double,
    val lngLat: List<Double> = listOf(lng, lat)
)
```

`GeoLocation.normalised(lng, lat)` clamps the longitude to `[-180, 180]` and the latitude to `[-90, 90]`.

## GeoBoundingBox
A rectangle expressed as south-west and north-east corners.

```kotlin
data class GeoBoundingBox(
    val southWest: GeoLocation,
    val northEast: GeoLocation
) {
    val points: List<GeoLocation>          // clockwise rectangle corners
    fun toLngLatString(): String           // "swLng,swLat,neLng,neLat"
    fun toLatLngString(): String           // "swLat,swLng,neLat,neLng"
    fun pad(delta: Double): GeoBoundingBox

    companion object {
        operator fun invoke(bbox: List<Double>): GeoBoundingBox
        operator fun invoke(vararg bbox: Double): GeoBoundingBox
        fun fromLngLatString(bbox: String): GeoBoundingBox
        fun fromLatLngString(bbox: String): GeoBoundingBox
    }
}
```
