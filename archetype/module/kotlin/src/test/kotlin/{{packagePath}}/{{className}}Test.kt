package {{packageName}}

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

class {{className}}Test : StringSpec({
    "greet should return greeting message" {
        val app = {{className}}()
        app.greet("Test") shouldBe "Hello, Test!"
    }
})