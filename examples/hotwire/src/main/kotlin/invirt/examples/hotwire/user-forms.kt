package invirt.examples.hotwire

import invirt.http4k.ViewResponse
import io.validk.ValidObject
import io.validk.Validation
import io.validk.email
import io.validk.minLength
import kotlin.reflect.KProperty1

class AddUserForm(
    val email: String?,
    val name: String?
) : ValidObject<AddUserForm>, ViewResponse("add-user") {

    fun createUser() = User(email = email!!, name = name!!)

    override val validation = Validation {
        AddUserForm::email.validEmail(this)
        AddUserForm::name.validName(this)
    }
}

data class EditUserForm(
    val email: String?,
    val name: String?,
    val userId: String? = null
) : ValidObject<EditUserForm>, ViewResponse("edit-user") {

    constructor(user: User) : this(
        email = user.email,
        name = user.name,
        userId = user.id
    )

    fun update(user: User) = user.copy(email = email!!, name = name!!)

    fun withUserId(userId: String): EditUserForm {
        return this.copy(userId = userId)
    }

    override val validation = Validation {
        EditUserForm::email.validEmail(this)
        EditUserForm::name.validName(this)
    }
}

fun <T> KProperty1<T, String?>.validEmail(validation: Validation<T>) {
    validation.apply {
        this@validEmail.notNullOrBlank("Email is required") {
            email() message "Not a valid email"
        }
    }
}

fun <T> KProperty1<T, String?>.validName(validation: Validation<T>) {
    validation.apply {
        this@validName.notNullOrBlank("Name is required") {
            minLength(5) message "Name must be at least 5 characters"
        }
    }
}
