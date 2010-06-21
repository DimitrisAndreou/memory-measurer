package gr.forth.ics.memorymeasurer;

import java.io.PrintStream;
import java.lang.reflect.Field;

/**
 * A visitor that prints simple messages on object graph traversal events, only for objects belonging to
 * specified packages.
 * 
 * @author Andreou Dimitris, email: jim.andreou (at) gmail (dot) com
 */
public class FilteringSimpleVisitor extends SimpleVisitor {
    private final String[] acceptedPackages;
    
    /**
     * Constructs a FilteringSimpleVisitor that will only print messages to the standard output for objects belonging in one
     * of the specified packages
     * 
     * @param acceptedPackages the packages that define which objects are considered in the generated messages
     */
    public FilteringSimpleVisitor(String ... acceptedPackages) {
        super();
        this.acceptedPackages = acceptedPackages;
    }
    
    /**
     * Constructs a FilteringSimpleVisitor that will only print messages to the specified stream for objects belonging in one
     * of the specified packages
     * 
     * @param acceptedPackages the packages that define which objects are considered in the generated messages
     */
    public FilteringSimpleVisitor(PrintStream out, String ... acceptedPackages) {
        super(out);
        this.acceptedPackages = acceptedPackages;
    }
    
    public void begin(Object o, long shallowMemoryUsage) {
        if (accept(o)) {
            super.begin(o, shallowMemoryUsage);
        }
    }
    
    public void end(Object o, long deepMemoryUsage) {
        if (accept(o)) {
            super.end(o, deepMemoryUsage);
        }
    }
    
    public void field(Field f, Object value) {
        if (accept(value)) {
            super.field(f, value);
        }
    }
    
    private boolean accept(Object o) {
        if (o == null) {
            return true;
        }
        String clazz = o.getClass().getName();
        for (String acceptedPackage : acceptedPackages) {
            if (clazz.startsWith(acceptedPackage)) {
                return true;
            }
        }
        return false;
    }
}
