package invirt.pebble

import org.http4k.core.Request

internal val requestThreadLocal = ThreadLocal<Request>()
val currentHttp4kRequest: Request? get() = requestThreadLocal.get()

//object RequestContext {
//    val requestThreadLocal = ThreadLocal<Request>()
//    val request: Request? get() = requestThreadLocal.get()
//
//    fun <R> with(request: Request, block: () -> R): R {
//        return requestThreadLocal.withValue(request, block)
//    }
//
//    fun replaceParam(param: String, newValue: Any): String {
//        return replaceParams(mapOf(param to newValue))
//    }
//
//    fun removeParams(params: Collection<String>): String {
//        return replaceParams(params.associateWith { "" })
//    }
//
//    fun removeParam(param: String): String {
//        return removeParams(listOf(param))
//    }
//
//    fun replaceParams(newParams: Map<String, Any>): String {
//        val queries = request!!.uri.queries()
//        val newQueries: List<Pair<String, Any?>> =
//            if (queries.isNotEmpty()) {
//                // Slightly overcomplicated, in order to maintain param order
//                val replacedParams = mutableSetOf<String>()
//                queries.map {
//                    val param = it.first
//                    if (newParams.containsKey(param)) {
//                        replacedParams.add(param)
//                        param to newParams[param]
//                    } else {
//                        it
//                    }
//                }.plus(newParams.filter { it.key !in replacedParams }.toList())
//            } else {
//                newParams.toList()
//            }
//        val requestParams =
//            newQueries
//                .map { it.first to it.second.toString() }
//                .filter { it.second.isNotBlank() }
//        return if (requestParams.isEmpty()) {
//            request!!.uri.path
//        } else {
//            request!!.uri.path + "?" + requestParams.toUrlFormEncoded()
//        }
//    }
//
//    fun addParamValue(param: String, newValue: Any, separator: String): String {
//        val existingParam = request!!.query(param)
//        return if (existingParam.isNullOrBlank()) {
//            replaceParam(param, newValue)
//        } else {
//            val newParamValue =
//                existingParam.split(",")
//                    .toSet()
//                    .plus(newValue.toString())
//                    .joinToString(separator)
//            replaceParam(param, newParamValue)
//        }
//    }
//
//    fun removeParamValue(
//        param: String,
//        newValue: Any,
//        separator: String,
//    ): String {
//        val existingParam = request!!.query(param)
//        if (existingParam.isNullOrBlank()) {
//            return request!!.uri.toString()
//        } else {
//            val newParamValue =
//                existingParam
//                    .split(separator)
//                    .minus(newValue.toString())
//                    .joinToString(separator)
//            return replaceParam(param, newParamValue)
//        }
//    }
//
//    fun replacePage(page: Page): String {
//        return replaceParams(
//            mapOf(
//                "from" to page.from,
//                "size" to page.size,
//            ),
//        )
//    }
//
//    fun revertSort(): String {
//        val sort = request!!.sort()
//        return if (sort == null) {
//            request!!.uri.toString()
//        } else {
//            replaceParam("sort", sort.revert().toString())
//        }
//    }
//}
