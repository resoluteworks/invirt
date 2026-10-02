package invirt.mongodb.mongock

import com.github.cloudyrock.mongock.ChangeLog
import io.mongock.api.annotations.ChangeUnit
import org.reflections.Reflections
import java.util.concurrent.ConcurrentHashMap

/**
 * How [runMigrations] hands the change units of a migrations package to Mongock.
 */
internal enum class ChangeUnitDiscovery(val logValue: String) {

    /** This call scanned the package, and Mongock receives the classes the scan found. */
    SCANNED("scanned"),

    /** An earlier call scanned the package through the same context classloader, and Mongock receives its classes. */
    CACHED("cached"),

    /**
     * Mongock scans the package itself. This is the path for a package without change units, because Mongock rejects a
     * run that has neither classes nor a package to scan, and for classes the context classloader cannot resolve,
     * because Mongock resolves each explicit class by name through that classloader and reads a name it cannot
     * resolve as a package to scan, which finds nothing and skips the change unit without an error.
     */
    PACKAGE_SCAN("package-scan")
}

/**
 * The change units of a migrations package, and how [runMigrations] hands them to Mongock.
 */
internal data class ChangeUnits(
    val classes: List<Class<*>>,
    val discovery: ChangeUnitDiscovery
)

/**
 * Finds the change units of a migrations package with the queries Mongock runs for a scan package: the types in the
 * package and its subpackages annotated with [ChangeUnit] or the legacy [ChangeLog], plus their subtypes.
 *
 * A scan walks every file under each classpath root that holds the package, so a process that runs the same
 * migrations many times (a test suite booting an application per spec) pays for it on every run. A classloader's
 * classpath does not change, so each package keeps the result of its last scan together with the context classloader
 * it was scanned through, and a call through that same classloader reuses it. A call through another classloader scans
 * again and replaces the entry, which keeps one scan per package alive rather than one per classloader ever seen.
 */
internal class ChangeUnitScanner {

    private class Scan(
        val classLoader: ClassLoader?,
        val classes: List<Class<*>>
    )

    private val scansByPackage = ConcurrentHashMap<String, Scan>()

    /**
     * The change units of [packageName], as seen from the calling thread's context classloader.
     */
    fun changeUnits(packageName: String): ChangeUnits {
        val classLoader = Thread.currentThread().contextClassLoader
        var isScanned = false
        val scan = scansByPackage.compute(packageName) { _, previous ->
            if (previous != null && previous.classLoader === classLoader) {
                previous
            } else {
                isScanned = true
                Scan(classLoader, scan(packageName))
            }
        }!!
        val discovery = when {
            scan.classes.isEmpty() || scan.classes.any { !it.isLoadableThrough(classLoader) } -> ChangeUnitDiscovery.PACKAGE_SCAN
            isScanned -> ChangeUnitDiscovery.SCANNED
            else -> ChangeUnitDiscovery.CACHED
        }
        return ChangeUnits(scan.classes, discovery)
    }

    private fun scan(packageName: String): List<Class<*>> {
        val reflections = Reflections(listOf(packageName))

        @Suppress("DEPRECATION")
        val legacyChangeLogs = reflections.getTypesAnnotatedWith(ChangeLog::class.java)
        return (reflections.getTypesAnnotatedWith(ChangeUnit::class.java) + legacyChangeLogs).sortedBy { it.name }
    }
}

/**
 * Whether [classLoader] resolves this class's name to this exact class, which is the lookup Mongock makes for each
 * explicit migration class.
 */
internal fun Class<*>.isLoadableThrough(classLoader: ClassLoader?): Boolean =
    try {
        Class.forName(name, false, classLoader) === this
    } catch (e: ClassNotFoundException) {
        false
    } catch (e: LinkageError) {
        false
    }
