package invirt.utils.files

import java.io.File

fun workingDirectory(): File = File(System.getProperty("user.dir"))

fun tempDirectory(): File = File(System.getProperty("java.io.tmpdir"))
