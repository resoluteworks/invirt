package invirt.mongodb

class VersionConflictException(
    val documentId: String,
    val updateVersion: Long
) : Exception("Could not update document $documentId to $updateVersion")
