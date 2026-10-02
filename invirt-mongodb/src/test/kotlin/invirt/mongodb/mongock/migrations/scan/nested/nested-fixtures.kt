package invirt.mongodb.mongock.migrations.scan.nested

import io.mongock.api.annotations.ChangeUnit
import io.mongock.api.annotations.Execution
import io.mongock.api.annotations.RollbackExecution

/** A change unit in a subpackage of the scanned package. */
@ChangeUnit(id = "scan-nested-change-unit", order = "3")
class ScanNestedChangeUnit {

    @Execution
    fun execute() {
    }

    @RollbackExecution
    fun rollback() {
    }
}
