package invirt.pebble.functions

import invirt.data.Page
import invirt.http4k.*
import invirt.pebble.currentHttp4kRequest

val replaceQueryFunction = pebbleFunction("replaceQuery", "name", "value") {
    currentHttp4kRequest!!.uri.replaceQuery(args["name"] as String to args["value"]!!)
}

val removeQueryValueFunction = pebbleFunction("removeQueryValue", "name", "value") {
    currentHttp4kRequest!!.uri.removeQueryValue(args["name"] as String, args["value"]!!)
}

val toggleQueryValueFunction = pebbleFunction("toggleQueryValue", "name", "value") {
    currentHttp4kRequest!!.uri.toggleQueryValue(args["name"] as String, args["value"]!!)
}

val replacePageFunction = pebbleFunction("replacePage", "page") {
    currentHttp4kRequest!!.uri.replacePage(args["page"] as Page)
}

val removeQueriesFunction = pebbleFunction("removeQueries", "names") {
    val namesArg = args["names"]!!
    val names = if (namesArg is Array<*>) {
        namesArg.toSet()
    } else if (namesArg is Collection<*>) {
        namesArg
    } else {
        throw IllegalArgumentException("Can't process names argument of type ${namesArg::class}")
    }
    currentHttp4kRequest!!.uri.removeQueries(names as Collection<String>)
}
