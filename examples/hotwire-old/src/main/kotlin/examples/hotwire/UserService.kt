package examples.hotwire

import invirt.utils.uuid7

data class User(
    val email: String,
    val name: String,
    val id: String = uuid7()
)

class UserService {

    private val users = listOf(
        User("estelle@example.com", "Estelle Rodgers"),
        User("alex@example.com", "Alexandra Rivers"),
        User("paula@example.com", "Paula Medrano")
    )
        .associateBy { it.id }
        .toMutableMap()

    fun allUsers(): List<User> = users.values.sortedBy { it.name }

    fun add(user: User) {
        users[user.id] = user
    }

    fun deleteUser(userId: String) {
        users.remove(userId)
    }

    fun getUser(id: String): User? = users[id]

    fun update(user: User): User {
        users[user.id] = user
        return user
    }

    fun update(userId: String, block: (User) -> User): User {
        val user = getUser(userId)!!
        return update(block(user))
    }
}
