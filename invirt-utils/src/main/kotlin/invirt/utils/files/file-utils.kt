package invirt.utils.files

import java.io.File

fun workingDirectory(): File {
    return File(System.getProperty("user.dir"))
}

fun tempDirectory(): File {
    return File(System.getProperty("java.io.tmpdir"))
}
