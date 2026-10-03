package invirt.mongodb

import com.mongodb.event.CommandListener
import com.mongodb.event.CommandStartedEvent
import invirt.mongo.test.testMongo
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldContain
import org.bson.Document
import java.util.concurrent.CopyOnWriteArrayList

class MongoClientSettingsTest : StringSpec() {

    private val commands = CopyOnWriteArrayList<String>()

    private val mongo = testMongo {
        addCommandListener(object : CommandListener {
            override fun commandStarted(event: CommandStartedEvent) {
                commands.add(event.commandName)
            }
        })
    }

    init {
        "configureClient applies to the client behind the database" {
            mongo.database.getCollection("settings-test", Document::class.java).insertOne(Document("a", 1))

            commands shouldContain "insert"
        }
    }
}
