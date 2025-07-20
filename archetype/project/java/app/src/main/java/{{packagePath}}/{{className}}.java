package {{packageName}};

public class {{className}} {
    public String greet(String name) {
        return "Hello, " + name + "!";
    }
    
    public static void main(String[] args) {
        {{className}} app = new {{className}}();
        System.out.println(app.greet("World"));
    }
}