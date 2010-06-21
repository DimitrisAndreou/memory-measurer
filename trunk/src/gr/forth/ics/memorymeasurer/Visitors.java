package gr.forth.ics.memorymeasurer;

import java.io.PrintStream;
import java.lang.reflect.Field;

/**
 * A static factory of {@link ObjectGraphVisitor visitors} that can be used with {@link MemoryMeasurer}.
 * 
 * @author andreou
 */
public class Visitors {
    private Visitors() { }
    
    /**
     * Returns a do-nothing ObjectGraphVisitor.
     * 
     * @return a do-nothing ObjectGraphVisitor
     */
    static ObjectGraphVisitor empty() {
        return new ObjectGraphVisitor() {
            public void begin(Object o, long shallowMemoryUsage) { }
            public void end(Object o, long deepMemoryUsage) { }
            public void field(Field f, Object value) { }
        };
    }
    
    /**
     * Returns a simple visitor that will print the object traversal to the standard output.
     * 
     * @return a simple visitor that will print the object traversal to the standard output
     */
    public static ObjectGraphVisitor simple() {
        return new SimpleVisitor();
    }
    
    /**
     * Returns a simple visitor that will print the object traversal to the specified stream.
     * 
     * @param out the stream to which to print the object traversal
     * @return a simple visitor that will print the object traversal to the specified stream
     */
    public static ObjectGraphVisitor simple(PrintStream out) {
        return new SimpleVisitor(out);
    }
    
    /**
     * Returns a simple visitor that will ignore objects in packages not included in the specified
     * list of acceptable packages, and will print the traversal of the others to the standard output.
     * 
     * @param acceptedPackages the packages of which the object traversal will be printed
     * (objects in other packages will be ignored)
     * @return a simple visitor that will ignore objects in packages not included in the specified
     * list of acceptable packages, and will print the traversal of the others to the standard output
     */
    public static ObjectGraphVisitor filtering(String ... acceptedPackages) {
        return new FilteringSimpleVisitor(acceptedPackages);
    }
    
    /**
     * Returns a simple visitor that will ignore objects in packages not included in the specified
     * list of acceptable packages, and will print the traversal of the others to the specified output.
     * 
     * @param out the stream to which to print the object traversal
     * @param acceptedPackages the packages of which the object traversal will be printed
     * (objects in other packages will be ignored)
     * @return a simple visitor that will ignore objects in packages not included in the specified
     * list of acceptable packages, and will print the traversal of the others to the specified output
     */
    public static ObjectGraphVisitor filtering(PrintStream out, String ... acceptedPackages) {
        return new FilteringSimpleVisitor(out, acceptedPackages);
    }
}
