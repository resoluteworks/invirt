package invirt.utils

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

class NamesTest : StringSpec({

    "greetingName is the first token of the full name" {
        "Mara Ellison".greetingName("there") shouldBe "Mara"
        "Cher".greetingName("there") shouldBe "Cher"
        "Mary Jane Watson".greetingName("there") shouldBe "Mary"
        "Anne-Marie Dubois".greetingName("there") shouldBe "Anne-Marie"
        "Anne-Marie".greetingName("there") shouldBe "Anne-Marie"
        "Siobhan O'Brien".greetingName("there") shouldBe "Siobhan"
        "O'Brien".greetingName("there") shouldBe "O'Brien"
        "Tim Stuart-Buttle".greetingName("there") shouldBe "Tim"
        "Jan van der Berg".greetingName("there") shouldBe "Jan"
        "Rachel (member 1)".greetingName("there") shouldBe "Rachel"
        "Rachel Kim, PhD".greetingName("there") shouldBe "Rachel"
        "  Tomás   Reyes ".greetingName("there") shouldBe "Tomás"
        "Ольга Иванова".greetingName("there") shouldBe "Ольга"
        "Ελένη Παπαδοπούλου".greetingName("there") shouldBe "Ελένη"
        "Ayşe Yılmaz".greetingName("there") shouldBe "Ayşe"
        "陳大文".greetingName("there") shouldBe "陳大文"
        // Nothing counts characters, so a very long given name survives whole.
        "Wolfeschlegelsteinhausenbergerdorff Jr".greetingName("there") shouldBe "Wolfeschlegelsteinhausenbergerdorff"
    }

    "greetingName splits on any whitespace, not just spaces" {
        "\tMara\nEllison ".greetingName("there") shouldBe "Mara"
    }

    "greetingName falls back to the caller's fallback when the name is blank" {
        "".greetingName("there") shouldBe "there"
        "   ".greetingName("there") shouldBe "there"
        "\t\n".greetingName("there") shouldBe "there"
        "".greetingName("friend") shouldBe "friend"
    }

    "splitName cuts after the first token, so neither part is ever a fragment" {
        "Mara Ellison".splitName() shouldBe ("Mara" to "Ellison")
        "Cher".splitName() shouldBe ("Cher" to null)
        "Mary Jane Watson".splitName() shouldBe ("Mary" to "Jane Watson")
        "Tim Stuart-Buttle".splitName() shouldBe ("Tim" to "Stuart-Buttle")
        "Jan van der Berg".splitName() shouldBe ("Jan" to "van der Berg")
        "Rachel (member 1)".splitName() shouldBe ("Rachel" to "(member 1)")
        "Rachel Kim, PhD".splitName() shouldBe ("Rachel" to "Kim, PhD")
        "  Tomás   Reyes ".splitName() shouldBe ("Tomás" to "Reyes")
        "Ольга Иванова".splitName() shouldBe ("Ольга" to "Иванова")
    }

    "splitName yields two nulls for a blank name" {
        "".splitName() shouldBe (null to null)
        "   ".splitName() shouldBe (null to null)
        "\t\n".splitName() shouldBe (null to null)
    }
})
