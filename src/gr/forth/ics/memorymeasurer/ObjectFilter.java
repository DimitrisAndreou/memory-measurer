package gr.forth.ics.memorymeasurer;

/**
 * An object filter, that is used by {@link MemoryMeasurer} to control object graph traversals.
 * 
 * @author Andreou Dimitris, email: jim.andreou (at) gmail (dot) com
 */
public interface ObjectFilter {
    
    /**
     * Returns true to indicate that the specified object is to be explored.
     * 
     * @param o the object to be explored
     * @return true to indicate that the specified object is to be explored
     */
    boolean explore(Object o);
}
