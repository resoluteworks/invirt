package invirt.core

import io.pebbletemplates.pebble.extension.Extension
import org.http4k.core.Request

class InvirtPebbleConfig(
    val classpathLocation: String = "webapp/views",
    val hotReloadDirectory: String = "src/main/resources/webapp/views",
    val extensions: List<Extension> = emptyList(),
    val globalVariables: Map<String, Any> = emptyMap(),
    val contextVariables: Map<String, (Request) -> Any?> = emptyMap()
)
