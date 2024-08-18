package invirt.mongodb

class VersionedDocumentConflictException(
    val documentId: String,
    val updateVersion: Long
) : Exception("Could not update document $documentId to $updateVersion")
