// ============================================================
// Token.java - Represents a single token from lexical analysis
// ============================================================
public class Token {
    public enum Type {
        KEYWORD,      // if, then
        IDENTIFIER,   // variable names like a, b, x
        NUMBER,       // numeric literals like 5, 10
        OPERATOR,     // >, <, >=, <=, ==, !=, =
        UNKNOWN       // anything unrecognized
    }

    private Type type;
    private String value;

    public Token(Type type, String value) {
        this.type = type;
        this.value = value;
    }

    public Type getType()   { return type; }
    public String getValue(){ return value; }

    @Override
    public String toString() {
        return String.format("  %-12s -> \"%s\"", type, value);
    }
}
