package invirt.utils.files

import invirt.utils.uuid7
import io.github.oshai.kotlinlogging.KotlinLogging
import java.io.Closeable
import java.io.File
import java.io.IOException

private val log = KotlinLogging.logger {}

class TempDir(parentDirectory: File = tempDirectory()) : Closeable {

    val directory = createRootDir(parentDirectory)

    init {
        log.atInfo {
            message = "Creating temporary directory"
            payload = mapOf("directory" to directory.absolutePath)
        }
    }

    fun newFile(extension: String? = null): File {
        val ext = extension?.let { "." + extension.removePrefix(".") } ?: ""
        return File(directory, uuid7() + ext)
    }

    fun newDirectory(): File {
        val dir = File(directory, uuid7())
        if (!dir.mkdirs()) {
            throw IllegalStateException("Could not create directory $dir")
        }
        return dir
    }

    override fun close() {
        val deleted = try {
            directory.deleteRecursively()
            true
        } catch (e: IOException) {
            log.atError {
                message = "Could not delete temporary directory"
                payload = mapOf("directory" to directory.absolutePath)
                cause = e
            }
            false
        }

        if (!deleted) {
            log.atError {
                message = "Could not delete temporary directory"
                payload = mapOf("directory" to directory.absolutePath)
            }
        } else {
            log.atInfo {
                message = "Deleted temporary directory"
                payload = mapOf("directory" to directory.absolutePath)
            }
        }
    }

    private fun createRootDir(workingDirectory: File): File {
        val tempFile = File(workingDirectory, uuid7())
        if (!tempFile.mkdirs()) {
            throw IllegalStateException("Could not create temp dir $tempFile")
        }
        return tempFile
    }
}

fun <T> withTempDir(
    currentDirectory: File = tempDirectory(),
    block: (TempDir) -> T
): T {
    return TempDir(currentDirectory).use(block)
}
