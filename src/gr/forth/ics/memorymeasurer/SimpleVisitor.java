package gr.forth.ics.memorymeasurer;

import java.io.PrintStream;
import java.lang.reflect.Field;

/**
 * A visitor that prints simple messages on object graph traversal events.
 *
 * @author Andreou Dimitris, email: jim.andreou (at) gmail (dot) com
 */
public class SimpleVisitor implements ObjectGraphVisitor {
    private int identation;
    private final PrintStream out;

    /**
     * Constructs a SimpleVisitor that will print messages on object graph traversal events on the standard output.
     */
    public SimpleVisitor() {
        this(System.out);
    }

    /**
     * Constructs a SimpleVisitor that will print messages on object graph traversal events on the specified stream.
     */
    public SimpleVisitor(PrintStream out) {
        if (out == null) { throw new IllegalArgumentException(); }
        this.out = out;
    }

    public void begin(Object o, long shallowMemoryUsage) {
        print("Entering instance of: " + o.getClass() + ", shallow memory: " + shallowMemoryUsage);
        identation++;
    }

    public void end(Object o, long deepMemoryUsage) {
        identation--;
        print("Exiting instance of: " + o.getClass() + ", complete memory usage: " + deepMemoryUsage);
    }

    public void field(Field f, Object value) {
        if (value == null) {
            return;
        }
        print("Traversing field: " + f.getName() + " with value of class: " + value.getClass());
    }

    private void print(String s) {
        for (int i = 0; i < identation; i++) System.out.print(' ');
        out.println(s);
    }
}
