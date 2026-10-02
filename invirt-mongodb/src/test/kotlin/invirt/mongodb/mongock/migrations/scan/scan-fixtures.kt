@file:Suppress("DEPRECATION")

package invirt.mongodb.mongock.migrations.scan

import com.github.cloudyrock.mongock.ChangeLog
import io.mongock.api.annotations.ChangeUnit
import io.mongock.api.annotations.Execution
import io.mongock.api.annotations.RollbackExecution

/*
 * Classes that are only ever scanned, never run: one of each kind of type Mongock's package scan picks up, and one it
 * leaves out.
 */

@ChangeUnit(id = "scan-change-unit", order = "1")
open class ScanChangeUnit {

    @Execution
    fun execute() {
    }

    @RollbackExecution
    fun rollback() {
    }
}

/** Not annotated itself, but a subtype of a change unit. */
class ScanChangeUnitSubtype : ScanChangeUnit()

@ChangeLog(order = "2")
class ScanLegacyChangeLog

/** Neither annotated nor a subtype of an annotated type. */
class ScanPlainClass
