package invirt.utils

import com.github.f4b6a3.uuid.UuidCreator

fun uuid7(): String {
    return UuidCreator.getTimeOrderedEpoch()
        .toString()
        .replace("-", "")
}
