package invirt.core

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.http4k.core.Body
import org.http4k.core.Request
import org.http4k.lens.BiDiBodyLens
import org.http4k.lens.Validator
import org.http4k.lens.WebForm
import org.http4k.lens.webForm
import kotlin.reflect.KClass

private val bodyFormLens: BiDiBodyLens<WebForm> = Body.webForm(Validator.Ignore).toLens()
private val formsObjectMapper: ObjectMapper = jacksonObjectMapper()
    .registerModule(JavaTimeModule())
    .enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
    .enable(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_AS_NULL)
    .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)

inline fun <reified T : Any> Request.toForm(): T = this.toForm(T::class)

/**
 * This function converts a form POSTed with 'application/x-www-form-urlencoded' to an object.
 *
 * It supports arrays, maps and nested objects. For example:
 *      parent.children[0].name = John
 *      departments[HR].employees[0].age = 32
 */
fun <T : Any> Request.toForm(formClass: KClass<T>): T {
    val formTree = formFieldsToMapTree(bodyFormLens(this).fields)

    // Use this tree of maps to create a Json structure
    val tree = formsObjectMapper.valueToTree<JsonNode>(formTree)!!

    // Convert this Json structure to a Pojo
    return formsObjectMapper.treeToValue(tree, formClass.java)
}

/**
 * Converts the given map fields into a tree (maps of maps), which can be used to represent an object model (similar to a JSON structure).
 *
 * Firstly, field names are transformed so that square brackets are replaced with a dot notation:
 *  - parent.child[name] becomes parent.child.name
 *  - parent.child[0] becomes parent.child.0
 *
 * Then a map is created for each "level" in the hierarchy. For example
 *  parent.child.name=John
 * becomes
 *  mapOf("parent" to mapOf("child" to mapOf("name" to "John")))
 *
 *  This can then be used as a proxy for a JSON object model
 *
 */
internal fun formFieldsToMapTree(fields: Map<String, Any>): Any {
    val dotNotationFields = fields
        .map { entry ->
            val value = entry.value

            // The value can be a list a form is submitted like name=John&name=Smith which produces a mapOf("name" to listOf("John", "Smith")
            // But when it's a list with one element, we assume that it's a single value rather than a collection. We safeguard against cases
            // when the field is a collection and only one value was sent with DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY
            val element = if (value is Collection<*> && value.size == 1) value.first()!! else value

            entry.key.dotNotation() to element
        }
        .toMap()

    val tree = mutableMapOf<String, Any>()

    // Create the tree based on dot notation in the key names
    dotNotationFields.forEach { (key, values) ->
        if (key.contains('.')) {
            // We have something like parent.child.name=John. So "name" is for "child", and we need to find
            // or create the chain of maps from ROOT/parent/child, and add "John" to that map

            // This gives us a list of keys that can be navigated from the root of the tree to the map where we need to add
            // the "name" field. For example [parent, child].
            val parentPath = key.substringBeforeLast('.').split('.')

            // Starting from the root, we traverse the chain [parent, child] to find the right map/container (also create any missing ones)
            var container = tree
            for (currentKey in parentPath) {
                container.putIfAbsent(currentKey, mutableMapOf<String, Any>())
                container = container[currentKey] as MutableMap<String, Any>
            }

            // Add ("name" -> "John")
            container[key.substringAfterLast('.')] = values
        } else {
            // This is a field directly on the root object, like name=John, so we don't need to traverse anything
            tree[key] = values
        }
    }

    // We now have to replace array entries like parent.children.0, parent.children.1, etc, with a collection.
    // At the moment, the tree contains
    //     mapOf("children" to mapOf("0" to "John", "1" to "Mary))
    // and we want this to be
    //     mapOf("children" to listOf("John", "Mary")
    return tree.indexKeysToCollections()
}

@Suppress("UNCHECKED_CAST")
internal fun Any.indexKeysToCollections(): Any {
    return if (this is Map<*, *>) {
        val index = keys.firstOrNull()?.let { it as? String }?.toIntOrNull()
        return if (index != null) {
            // We have numeric keys (like 0, 1, 2), which means all keys on this level are indices (it will/should fail otherwise)
            entries.sortedBy { (it.key as String).toInt() }
                .map { it.value!!.indexKeysToCollections() }
                .toList()
        } else {
            // We have non-numeric keys (like firstName, lastName, children)
            map { it.key to it.value!!.indexKeysToCollections() }.toMap()
        }
    } else {
        this
    }
}

internal fun String.dotNotation(): String = replace("[", ".").replace("]", "")
