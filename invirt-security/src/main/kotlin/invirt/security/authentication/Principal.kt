package invirt.security.authentication

import invirt.core.InvirtRequest
import org.http4k.core.Request
import org.http4k.core.with
import org.http4k.lens.RequestKey

internal val principalContextKey = RequestKey.optional<Principal>("currentPrincipal")

/**
 * An interface representing an authenticated entity.
 */
interface Principal {

    /**
     * A reference identifying the principal, containing its type and unique identifier.
     * This allows for retrieving the full principal details from a data source when needed or
     * when logging.
     */
    val ref: PrincipalRef
}

/**
 * A data class representing a reference to a principal, containing its type and unique identifier.
 */
data class PrincipalRef(val type: String, val id: String) {
    override fun toString(): String = "$type:$id"
}

fun Request.withPrincipal(principal: Principal): Request = this.with(principalContextKey of principal)

val Request.principal: Principal?
    get() {
        return if (this is InvirtRequest) principalContextKey(this.delegate) else principalContextKey(this)
    }

val Request.hasPrincipal: Boolean
    get() {
        return if (this is InvirtRequest) principalContextKey(this.delegate) != null else principalContextKey(this) != null
    }
