package objectexplorer;

public interface ObjectVisitor<T> {
    //if value is primitive or null, return is ignored
    Traversal visit(Chain chain);
    T result();

    enum Traversal {
        EXPLORE,
        SKIP
    }
}
