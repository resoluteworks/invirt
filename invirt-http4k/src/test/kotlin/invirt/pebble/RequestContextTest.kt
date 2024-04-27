//package invirt.pebble
//
//import invirt.data.Page
//import invirt.http4k.GET
//import io.kotest.core.spec.style.StringSpec
//import io.kotest.matchers.shouldBe
//import org.http4k.core.Method
//import org.http4k.core.Request
//import org.http4k.core.Response
//import org.http4k.core.Status
//import org.http4k.core.then
//import org.http4k.kotest.shouldHaveStatus
//import org.http4k.routing.routes
//
//class RequestContextTest : StringSpec() {
//    init {
//
//        "replaceParam" {
//            testReplaceParam("/test", "q", "John", "/test?q=John")
//            testReplaceParam("/test", "q", "John Smith", "/test?q=John+Smith")
//            testReplaceParam("/test?q=nothing", "q", "John Smith", "/test?q=John+Smith")
//            testReplaceParam("/test?q=nothing", "from", "10", "/test?q=nothing&from=10")
//            testReplaceParam("/test?q=john&from=0&size=10", "q", "smith", "/test?q=smith&from=0&size=10")
//            testReplaceParam("/test?q=john&from=0&size=10", "from", "10", "/test?q=john&from=10&size=10")
//            testReplaceParam("/test?q=john&from=0&size=10", "size", "20", "/test?q=john&from=0&size=20")
//            testReplaceParam("/test?q=john&from=0&size=10", "size", "", "/test?q=john&from=0")
//        }
//
//        "removeParam" {
//            testRemoveParam("/test?q=john&from=0&size=10", "size", "/test?q=john&from=0")
//            testRemoveParam("/test?q=john&from=0&size=10", "from", "/test?q=john&size=10")
//        }
//
//        "removeParams" {
//            testRemoveParams("/test?q=john&from=0&size=10", listOf("size"), "/test?q=john&from=0")
//            testRemoveParams("/test?q=john&from=0&size=10", listOf("from", "size"), "/test?q=john")
//            testRemoveParams("/test?q=john&from=0&size=10", listOf("q", "from", "size"), "/test")
//            testRemoveParams("/test?q=john&from=0&size=10", listOf("nothing", "here"), "/test?q=john&from=0&size=10")
//        }
//
//        "addParamValue" {
//            testAddParamValue("/test", "q", "John", "/test?q=John")
//            testAddParamValue("/test?type=entity", "type", "individual", "/test?type=entity%2Cindividual")
//            testAddParamValue("/test?q=john&from=0&size=10", "type", "individual", "/test?q=john&from=0&size=10&type=individual")
//            testAddParamValue(
//                "/test?q=john&from=0&size=10&type=individual,entity",
//                "type",
//                "entity",
//                "/test?q=john&from=0&size=10&type=individual%2Centity"
//            )
//            testAddParamValue(
//                "/test?q=john&from=0&size=10&type=individual,entity",
//                "type",
//                "unknown",
//                "/test?q=john&from=0&size=10&type=individual%2Centity%2Cunknown"
//            )
//            testAddParamValue("/test?type=ONLINE_EXHIBITION", "type", "ONLINE_EXHIBITION", "/test?type=ONLINE_EXHIBITION")
//        }
//
//        "removeParamValue" {
//            testRemoveParamValue("/test", "q", "John", "/test")
//            testRemoveParamValue("/test?type=entity", "age", "20", "/test?type=entity")
//            testRemoveParamValue("/test?type=entity", "type", "entity", "/test")
//            testRemoveParamValue(
//                "/test?q=john&from=0&size=10&type=individual,entity",
//                "type",
//                "unknown",
//                "/test?q=john&from=0&size=10&type=individual%2Centity"
//            )
//            testRemoveParamValue(
//                "/test?q=john&from=0&size=10&type=individual,entity",
//                "type",
//                "entity",
//                "/test?q=john&from=0&size=10&type=individual"
//            )
//        }
//
//        "replacePage" {
//            testReplacePage("/test", Page(0, 10), "/test?from=0&size=10")
//            testReplacePage("/test?q=john", Page(100, 10), "/test?q=john&from=100&size=10")
//            testReplacePage("/test?q=john&from=0&size=10", Page(30, 10), "/test?q=john&from=30&size=10")
//        }
//
//        "revertSort" {
//            testRevertSort("/test", "/test")
//            testRevertSort("/test?q=john&from=0&size=10&sort=name:ASC", "/test?q=john&from=0&size=10&sort=name%3ADESC")
//            testRevertSort("/test?q=john&from=0&size=10&sort=name", "/test?q=john&from=0&size=10&sort=name%3ADESC")
//            testRevertSort("/test?q=john&sort=name:DESC&from=0&size=10", "/test?q=john&sort=name%3AASC&from=0&size=10")
//        }
//    }
//
//    private fun testReplaceParam(request: String, param: String, paramValue: String, expected: String) {
//        val handler = invirtPebbleFilter.then(
//            routes(
//                "/test" GET {
//                    RequestContext.replaceParam(param, paramValue) shouldBe expected
//                    Response(Status.OK)
//                }
//            )
//        )
//        handler(Request(Method.GET, request)) shouldHaveStatus Status.OK
//    }
//
//    private fun testAddParamValue(request: String, param: String, paramValue: String, expected: String) {
//        val handler = invirtPebbleFilter.then(
//            routes(
//                "/test" GET {
//                    RequestContext.addParamValue(param, paramValue, ",") shouldBe expected
//                    Response(Status.OK)
//                }
//            )
//        )
//        handler(Request(Method.GET, request)) shouldHaveStatus Status.OK
//    }
//
//    private fun testRemoveParamValue(request: String, param: String, paramValue: String, expected: String) {
//        val handler = invirtPebbleFilter.then(
//            routes(
//                "/test" GET {
//                    RequestContext.removeParamValue(param, paramValue, ",") shouldBe expected
//                    Response(Status.OK)
//                }
//            )
//        )
//        handler(Request(Method.GET, request)) shouldHaveStatus Status.OK
//    }
//
//    private fun testRemoveParams(request: String, params: Collection<String>, expected: String) {
//        val handler = invirtPebbleFilter.then(
//            routes(
//                "/test" GET {
//                    RequestContext.removeParams(params) shouldBe expected
//                    Response(Status.OK)
//                }
//            )
//        )
//        handler(Request(Method.GET, request)) shouldHaveStatus Status.OK
//    }
//
//    private fun testRemoveParam(request: String, param: String, expected: String) {
//        val handler = invirtPebbleFilter.then(
//            routes(
//                "/test" GET {
//                    RequestContext.removeParam(param) shouldBe expected
//                    Response(Status.OK)
//                }
//            )
//        )
//        handler(Request(Method.GET, request)) shouldHaveStatus Status.OK
//    }
//
//    private fun testReplacePage(request: String, page: Page, expected: String) {
//        val handler = invirtPebbleFilter.then(
//            routes(
//                "/test" GET {
//                    RequestContext.replacePage(page) shouldBe expected
//                    Response(Status.OK)
//                }
//            )
//        )
//        handler(Request(Method.GET, request)) shouldHaveStatus Status.OK
//    }
//
//    private fun testRevertSort(request: String, expected: String) {
//        val handler = invirtPebbleFilter.then(
//            routes(
//                "/test" GET {
//                    RequestContext.revertSort() shouldBe expected
//                    Response(Status.OK)
//                }
//            )
//        )
//        handler(Request(Method.GET, request)) shouldHaveStatus Status.OK
//    }
//}
