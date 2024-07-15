package invirt.http4k

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import org.http4k.core.Body
import org.http4k.core.Method
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.core.with
import org.http4k.lens.Validator
import org.http4k.lens.WebForm
import org.http4k.lens.webForm
import org.http4k.routing.bind
import org.http4k.routing.routes

class FormsTest : StringSpec() {

    init {

        "String.dotNotation" {
            "child[1]".dotNotation() shouldBe "child.1"
            "parent.child[name]".dotNotation() shouldBe "parent.child.name"
            "parent.child[1]".dotNotation() shouldBe "parent.child.1"
            "company[0].departments[HR].directors[1].name".dotNotation() shouldBe "company.0.departments.HR.directors.1.name"
        }

        "formFieldsToMapTree - basics" {
            formFieldsToMapTree(mapOf("name" to "John")) shouldBe mapOf("name" to "John")

            formFieldsToMapTree(mapOf("parent.child[name]" to "John")) shouldBe mapOf(
                "parent" to mapOf(
                    "child" to mapOf("name" to "John")
                )
            )

            formFieldsToMapTree(mapOf("parent.child.name" to "John")) shouldBe mapOf(
                "parent" to mapOf(
                    "child" to mapOf("name" to "John")
                )
            )

            formFieldsToMapTree(mapOf("parent[child].name" to "John")) shouldBe mapOf(
                "parent" to mapOf(
                    "child" to mapOf("name" to "John")
                )
            )
        }

        "formFieldsToMapTree - collection indices" {
            val expected = mapOf(
                "department" to "IT",
                "parent" to mapOf(
                    "company" to "ACME",
                    "children" to listOf(
                        mapOf("name" to "John"),
                        mapOf("name" to "Mary")
                    )
                )
            )
            formFieldsToMapTree(
                mapOf(
                    "department" to "IT",
                    "parent.company" to "ACME",
                    "parent.children[0].name" to "John",
                    "parent.children[1].name" to "Mary"
                )
            ) shouldBe expected

            formFieldsToMapTree(
                mapOf(
                    "department" to "IT",
                    "parent.company" to "ACME",
                    "parent.children[1].name" to "Mary",
                    "parent.children[0].name" to "John"
                )
            ) shouldBe expected
        }

        "formFieldsToMapTree - collection indices as root" {
            formFieldsToMapTree(
                mapOf(
                    "0" to "John",
                    "1" to "Mary"
                )
            ) shouldBe listOf("John", "Mary")

            // Check that index order is honoured
            formFieldsToMapTree(
                mapOf(
                    "1" to mapOf("child" to mapOf("name" to "Mary")),
                    "0" to mapOf("child" to mapOf("name" to "John"))
                )
            ) shouldBe listOf(
                mapOf("child" to mapOf("name" to "John")),
                mapOf("child" to mapOf("name" to "Mary"))
            )
        }

        "formFieldsToMapTree - collection values" {
            formFieldsToMapTree(
                mapOf(
                    "department" to "IT",
                    "parent.company" to "ACME",
                    "parent.children" to listOf("John", "Mary")
                )
            ) shouldBe mapOf(
                "department" to "IT",
                "parent" to mapOf(
                    "company" to "ACME",
                    "children" to listOf("John", "Mary")
                )
            )
        }

        "basic form" {
            data class Form(val name: String, val age: Int)
            post<Form>("name" to "John Smith", "age" to 45) shouldBe Form("John Smith", 45)
        }

        "collection fields submitted as multiple form values for the same field" {
            data class Form(val name: String, val roles: Set<String>)
            post<Form>("name" to "John Smith", "roles" to listOf("admin", "user")) shouldBe Form(
                "John Smith",
                setOf("admin", "user")
            )
            post<Form>("name" to "John Smith", "roles" to listOf("admin", "admin", "user")) shouldBe Form(
                "John Smith",
                setOf("admin", "user")
            )
        }

        "null as empty collection" {
            data class Form(val name: String, val roles: Set<String> = emptySet())
            post<Form>("name" to "John Smith") shouldBe Form(
                "John Smith",
                emptySet()
            )
        }

        "nested object" {
            data class Department(val id: String, val name: String)
            data class Company(val name: String, val department: Department)

            post<Company>(
                "name" to "Acme",
                "department.id" to "123",
                "department.name" to "HR"
            ) shouldBe Company("Acme", Department("123", "HR"))
        }

        "collection fields submitted using index references: [0], [1], etc" {
            data class Company(val names: List<String>)
            post<Company>(
                "names[0]" to "Acme",
                "names[1]" to "Acme Ltd"
            ) shouldBe Company(listOf("Acme", "Acme Ltd"))
        }

        "enums" {
            data class Form(val name: String, val type: TestFormEnum?)
            post<Form>("name" to "John Smith", "type" to "ONE") shouldBe Form("John Smith", TestFormEnum.ONE)
            post<Form>("name" to "John Smith", "type" to "") shouldBe Form("John Smith", null)
        }

        "map keys" {
            data class Company(val names: Map<String, String>)
            post<Company>(
                "names[one]" to "Acme",
                "names[two]" to "Acme Ltd"
            ) shouldBe Company(mapOf("one" to "Acme", "two" to "Acme Ltd"))
        }

        "nested objects, arrays, maps" {
            data class Employee(
                val fullName: String,
                val roles: Set<String>,
                val metadata: Map<String, String>
            )

            data class Department(
                val name: String,
                val employeeCount: Map<String, Int>,
                val employees: List<Employee>
            )

            data class Company(
                val name: String,
                val departments: List<Department>
            )

            post<Company>(
                "name" to "Acme",
                "departments[0].name" to "HR",
                "departments[1].name" to "Management",

                "departments[0].employeeCount[Junior]" to 10,
                "departments[0].employeeCount[Senior]" to 3,
                "departments[1].employeeCount[All]" to 47,

                "departments[0].employees[0].fullName" to "John Smith",
                "departments[0].employees[0].roles" to listOf("Admin", "Supervisor"),
                "departments[0].employees[0].metadata[address]" to "The Cinnamons",
                "departments[0].employees[0].metadata[city]" to "London",

                "departments[0].employees[1].fullName" to "Hannah Smith",
                "departments[0].employees[1].roles" to listOf("Assistant"),
                "departments[0].employees[1].metadata[address]" to "Some address",
                "departments[0].employees[1].metadata[city]" to "Liverpool",

                "departments[1].employees[0].fullName" to "Angela Jones",
                "departments[1].employees[0].roles" to listOf("Team lead", "supervisor"),
                "departments[1].employees[0].metadata[address]" to "Linton Travel Tavern",
                "departments[1].employees[0].metadata[city]" to "Norfolk"
            ) shouldBe Company(
                name = "Acme",
                departments = listOf(
                    Department(
                        "HR",
                        mapOf("Junior" to 10, "Senior" to 3),
                        listOf(
                            Employee(
                                "John Smith",
                                setOf("Admin", "Supervisor"),
                                mapOf("address" to "The Cinnamons", "city" to "London")
                            ),
                            Employee(
                                "Hannah Smith",
                                setOf("Assistant"),
                                mapOf("address" to "Some address", "city" to "Liverpool")
                            )
                        )
                    ),
                    Department(
                        "Management",
                        mapOf("All" to 47),
                        listOf(
                            Employee(
                                "Angela Jones",
                                setOf("Team lead", "supervisor"),
                                mapOf("address" to "Linton Travel Tavern", "city" to "Norfolk")
                            )
                        )
                    )
                )
            )
        }
    }

    private inline fun <reified F : Any> post(vararg data: Pair<String, Any>): F {
        return post(data.toMap())
    }

    private inline fun <reified F : Any> post(data: Map<String, Any>): F {
        lateinit var form: F
        val httpHandler = routes(
            "/test" bind Method.POST to {
                form = it.toForm<F>()
                Response(Status.OK)
            }
        )
        val fields = data.map { entry ->
            val value = entry.value
            entry.key to if (value is Collection<*>) {
                value.map { it.toString() }.toList()
            } else {
                listOf(value.toString())
            }
        }.toMap()

        val strictFormBody = Body.webForm(Validator.Ignore).toLens()
        httpHandler(Request(Method.POST, "/test").with(strictFormBody of WebForm(fields)))
        return form
    }
}

enum class TestFormEnum { ONE, TWO }
