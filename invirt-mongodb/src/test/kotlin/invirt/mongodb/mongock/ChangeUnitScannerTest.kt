package invirt.mongodb.mongock

import invirt.mongodb.mongock.migrations.data.V1_Data
import invirt.mongodb.mongock.migrations.scan.ScanChangeUnit
import invirt.mongodb.mongock.migrations.scan.ScanChangeUnitSubtype
import invirt.mongodb.mongock.migrations.scan.ScanLegacyChangeLog
import invirt.mongodb.mongock.migrations.scan.ScanPlainClass
import invirt.mongodb.mongock.migrations.scan.nested.ScanNestedChangeUnit
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeSameInstanceAs
import java.net.URLClassLoader
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.CountDownLatch
import kotlin.concurrent.thread

class ChangeUnitScannerTest : StringSpec() {

    private val scanPackageChangeUnits = listOf(
        ScanChangeUnit::class.java,
        ScanChangeUnitSubtype::class.java,
        ScanLegacyChangeLog::class.java,
        ScanNestedChangeUnit::class.java
    )

    init {
        "finds the change units, legacy change logs and their subtypes in a package and its subpackages" {
            ChangeUnitScanner().changeUnits(SCAN_PACKAGE) shouldBe ChangeUnits(scanPackageChangeUnits, ChangeUnitDiscovery.SCANNED)
        }

        "reuses the scan of a package through the same context classloader" {
            val scanner = ChangeUnitScanner()
            val scanned = scanner.changeUnits(SCAN_PACKAGE)
            val cached = scanner.changeUnits(SCAN_PACKAGE)

            scanned.discovery shouldBe ChangeUnitDiscovery.SCANNED
            cached.discovery shouldBe ChangeUnitDiscovery.CACHED
            cached.classes shouldBeSameInstanceAs scanned.classes
        }

        "keeps a scan per package" {
            val scanner = ChangeUnitScanner()

            scanner.changeUnits(SCAN_PACKAGE).discovery shouldBe ChangeUnitDiscovery.SCANNED
            scanner.changeUnits(DATA_PACKAGE) shouldBe ChangeUnits(listOf(V1_Data::class.java), ChangeUnitDiscovery.SCANNED)
            scanner.changeUnits(SCAN_PACKAGE).discovery shouldBe ChangeUnitDiscovery.CACHED
            scanner.changeUnits(DATA_PACKAGE).discovery shouldBe ChangeUnitDiscovery.CACHED
        }

        "scans again through another context classloader, which replaces the scan it kept" {
            val scanner = ChangeUnitScanner()
            scanner.changeUnits(SCAN_PACKAGE).discovery shouldBe ChangeUnitDiscovery.SCANNED

            URLClassLoader(emptyArray(), Thread.currentThread().contextClassLoader).use { childClassLoader ->
                withContextClassLoader(childClassLoader) {
                    scanner.changeUnits(SCAN_PACKAGE) shouldBe ChangeUnits(scanPackageChangeUnits, ChangeUnitDiscovery.SCANNED)
                    scanner.changeUnits(SCAN_PACKAGE).discovery shouldBe ChangeUnitDiscovery.CACHED
                }
            }

            scanner.changeUnits(SCAN_PACKAGE).discovery shouldBe ChangeUnitDiscovery.SCANNED
            scanner.changeUnits(SCAN_PACKAGE).discovery shouldBe ChangeUnitDiscovery.CACHED
        }

        "leaves the scan to Mongock when the context classloader cannot resolve the change units, on every call" {
            val scanner = ChangeUnitScanner()
            withContextClassLoader(ClassLoader.getPlatformClassLoader()) {
                scanner.changeUnits(SCAN_PACKAGE) shouldBe ChangeUnits(scanPackageChangeUnits, ChangeUnitDiscovery.PACKAGE_SCAN)
                scanner.changeUnits(SCAN_PACKAGE) shouldBe ChangeUnits(scanPackageChangeUnits, ChangeUnitDiscovery.PACKAGE_SCAN)
            }
        }

        "leaves the scan to Mongock when there is no context classloader, through which Reflections sees no classpath" {
            withContextClassLoader(null) {
                ChangeUnitScanner().changeUnits(SCAN_PACKAGE) shouldBe ChangeUnits(emptyList(), ChangeUnitDiscovery.PACKAGE_SCAN)
            }
        }

        "leaves the scan to Mongock for a package without change units" {
            val scanner = ChangeUnitScanner()

            scanner.changeUnits(PACKAGE_WITHOUT_CHANGE_UNITS) shouldBe ChangeUnits(emptyList(), ChangeUnitDiscovery.PACKAGE_SCAN)
            scanner.changeUnits(PACKAGE_WITHOUT_CHANGE_UNITS) shouldBe ChangeUnits(emptyList(), ChangeUnitDiscovery.PACKAGE_SCAN)
        }

        "scans a package once when several threads ask for it at the same time" {
            val scanner = ChangeUnitScanner()
            val start = CountDownLatch(1)
            val discoveries = ConcurrentLinkedQueue<ChangeUnitDiscovery>()
            val threads = List(8) {
                thread {
                    start.await()
                    discoveries.add(scanner.changeUnits(SCAN_PACKAGE).discovery)
                }
            }

            start.countDown()
            threads.forEach { it.join() }

            discoveries.count { it == ChangeUnitDiscovery.SCANNED } shouldBe 1
            discoveries.count { it == ChangeUnitDiscovery.CACHED } shouldBe 7
        }

        "a class is loadable through the classloader that defined it" {
            ScanChangeUnit::class.java.isLoadableThrough(ScanChangeUnit::class.java.classLoader) shouldBe true
        }

        "a class is not loadable through a classloader that cannot find it" {
            ScanChangeUnit::class.java.isLoadableThrough(ClassLoader.getPlatformClassLoader()) shouldBe false
            ScanChangeUnit::class.java.isLoadableThrough(null) shouldBe false
        }

        "a class is not loadable through a classloader that resolves its name to another class" {
            val classesRoot = ScanPlainClass::class.java.protectionDomain.codeSource.location
            URLClassLoader(arrayOf(classesRoot), null).use { isolatedClassLoader ->
                Class.forName(ScanPlainClass::class.java.name, false, isolatedClassLoader).name shouldBe ScanPlainClass::class.java.name
                ScanPlainClass::class.java.isLoadableThrough(isolatedClassLoader) shouldBe false
            }
        }

        "a class is not loadable through a classloader that fails to link it" {
            val failingClassLoader = object : ClassLoader(null) {
                override fun loadClass(name: String, resolve: Boolean): Class<*> = throw NoClassDefFoundError(name)
            }
            ScanChangeUnit::class.java.isLoadableThrough(failingClassLoader) shouldBe false
        }
    }
}
