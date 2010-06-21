package gr.forth.ics.memorymeasurer;

import java.lang.reflect.Field;

/**
 * A object graph traversal visitor.
 * 
 * @author Andreou Dimitris, email: jim.andreou (at) gmail (dot) com
 */
public interface ObjectGraphVisitor {
    /**
     * Called by {@link MemoryMeasurer} when an object is first encountered.
     * 
     * @param o a newly discovered object (will not be null)
     * @param shallowMemoryUsage the shallow memory usage of this object's fields, i.e. not taking
     * recursively into account the memory usage of other objects accessible through the visited object's fields
     */
    void begin(Object o, long shallowMemoryUsage);
    
    /**
     * Called for every explored field of a visited object. These calls for the fields of an objects are nested
     * between a call to {@link #begin(Object, long)} and a call to {@link #end(Object, long)}.
     * 
     * @param f a field of a visited object (will not be null)
     * @param value the value of the field
     */
    void field(Field f, Object value);
    
    /**
     * Called by {@link MemoryMeasurer} when an object is fully (and recursively) explored, along with the
     * accumulated memory usage of it.
     * 
     * @param o a newly discovered object (will not be null)
     * @param deepMemoryUsage the accumulated memory usage of this object's fields, i.e. by also taking
     * recursively into account the memory usage of other objects accessible through the visited object's fields
     */
    void end(Object o, long deepMemoryUsage);
}
