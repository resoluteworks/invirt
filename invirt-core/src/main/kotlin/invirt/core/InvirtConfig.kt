package invirt.core

import invirt.core.config.developmentMode
import io.pebbletemplates.pebble.extension.Extension
import org.http4k.config.Environment

data class InvirtConfig(
    val developmentMode: Boolean = Environment.ENV.developmentMode,
    val pebble: InvirtPebbleConfig = InvirtPebbleConfig()
)

data class InvirtPebbleConfig(
    val classpathLocation: String = "webapp/views",
    val hotReloadDirectory: String = "src/main/resources/webapp/views",
    val extensions: List<Extension> = emptyList(),
    val globalVariables: Map<String, Any> = emptyMap()
)
