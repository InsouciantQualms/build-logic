package {{packageName}}

class {{className}} {
    fun greet(name: String): String {
        return "Hello, $name!"
    }
}

fun main() {
    val app = {{className}}()
    println(app.greet("World"))
}