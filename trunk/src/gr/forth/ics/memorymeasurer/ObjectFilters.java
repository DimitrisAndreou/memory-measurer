package gr.forth.ics.memorymeasurer;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * A static factory of {@link ObjectFilter object filters} that can be used with {@link MemoryMeasurer}.
 * 
 * @author Andreou Dimitris, email: jim.andreou (at) gmail (dot) com
 */
public class ObjectFilters {
    private ObjectFilters() { }
    
    /**
     * Returns a filter that accepts every object.
     * @return a filter that accepts every object
     */
    public static ObjectFilter allowAny() {
        return new ObjectFilter() {
            public boolean explore(Object o) {
                return true;
            }
        };
    }
    
    /**
     * Returns a filter that forbids the traversal of exact instances of one of the specified types. In other words, an object
     * {@code obj} will be traversed if its {@code obj.getClass()} is not included in the specified types.
     * 
     * @param types the types, exact instances of which to forbid
     * @return a filter that forbids objects belonging exactly in one of the specified types
     */
    public static ObjectFilter stopExactlyAt(Class<?> ... types) {
        return stopExactlyAt(new HashSet<Class<?>>(Arrays.asList(types)));
    }
    
    /**
     * Returns a filter that forbids the traversal of exact instances of one of the specified types. In other words, an object
     * {@code obj} will be traversed if its {@code obj.getClass()} is not included in the specified types. Instances of
     * subclasses of the specified types are allowed (unless their exact type is also explicitly forbidden).
     * 
     * @param types the types, exact instances of which to forbid
     * @return a filter that forbids objects belonging exactly in one of the specified types
     */
    public static ObjectFilter stopExactlyAt(final Collection<Class<?>> types) {
        if (types == null) {
            throw new NullPointerException();
        }
        return new ObjectFilter() {
            public boolean explore(Object o) {
                return !types.contains(o.getClass());
            }
        };
    }
    
    /**
     * Returns a filter that forbids the traversal of (direct or indirect) instances of one of the specified types. 
     * 
     * @param types the types, direct or indirect instances of which to forbid 
     * @return a filter that forbids objects belonging exactly in one of the specified types
     */
    public static ObjectFilter stopAtAnyInstanceOf(Class<?> ... types) {
        return stopAtAnyInstanceOf(Arrays.asList(types));
    }
    
    /**
     * Returns a filter that forbids the traversal of (direct or indirect) instances of one of the specified types. 
     * 
     * @param types the types, direct or indirect instances of which to forbid 
     * @return a filter that forbids objects belonging exactly in one of the specified types
     */
    public static ObjectFilter stopAtAnyInstanceOf(final Collection<Class<?>> types) {
        return new ObjectFilter() {
            public boolean explore(Object o) {
                Class clazz = o.getClass();
                for (Class<?> type : types) {
                    if (type.isAssignableFrom(clazz)) {
                        return false;
                    }
                }
                return true;
            }
        };
    }

    /**
     * Returns a filter that forbids the traversal of the specified objects.
     *
     * @param instances the objects which to avoid traversing
     * @return a filter that forbids the traversal of the specified objects
     */
    public static ObjectFilter stopAtTheseInstances(Object... instances) {
        return stopAtTheseInstances(new HashSet<Object>(Arrays.asList(instances)));
    }
    
    /**
     * Returns a filter that forbids the traversal of the specified objects.
     *
     * @param instances the objects which to avoid traversing
     * @return a filter that forbids the traversal of the specified objects
     */
    public static ObjectFilter stopAtTheseInstances(final Collection<Object> instances) {
        final Set<Object> set = new HashSet<Object>();
        for (Object o : instances) set.add(o);
        return new ObjectFilter() {
            public boolean explore(Object o) {
                return !set.contains(o);
            }
        };
    }

    /**
     * Returns the negation of a filter. That is, the returned filter will only allow objects not allowed by the specified
     * filter, and will forbid objects allowed by the specified filter.
     * 
     * @param filter the filter to negate
     * @return the negation of a filter
     */
    public static ObjectFilter dont(final ObjectFilter filter) {
        if (filter == null) {
            throw new NullPointerException();
        }
        return new ObjectFilter() {
            public boolean explore(Object o) {
                return !filter.explore(o);
            }
        };
    }
}
