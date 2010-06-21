package gr.forth.ics.memorymeasurer;

import java.lang.instrument.Instrumentation;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.IdentityHashMap;
import java.util.LinkedList;
import java.util.Map;

/**
 * A utility that can estimate occupied heap memory (in bytes) of an object,
 * and all its transitive closure.
 * <p>
 * This argument needs to be passed to the VM for this to work:
 * 
 * -javaagent:path/to/MonitoringTools.jar
 * 
 * @see #count(Object)
 * @author Andreou Dimitris, email: jim.andreou (at) gmail (dot) com
 */
public class MemoryMeasurer {
    static Instrumentation instrumentation;
    
    /**
     * Returns whether the memory measurer is correctly initialized and can be used.
     * @return whether the memory measurer is correctly initialized and can be used.
     */
    public static boolean isOperational() {
        return instrumentation != null;
    }
    
    private static long measure(Object o) {
        return instrumentation.getObjectSize(o);
    }
    
    static void init(Instrumentation inst) {
        if (instrumentation != null) {
            throw new IllegalStateException("Already initialized");
        }
        instrumentation = inst;
    }
    
    /**
     * Returns the estimation of memory footprint of the object graph rooted at the specified object.
     * 
     * @param o the root of the object graph to be measured
     * @return the estimation of memory footprint of the object graph rooted at the specified object
     */
    public static long count(Object o) {
        return count(o, ObjectFilters.allowAny(), Visitors.empty());
    }
    
    /**
     * Returns the estimation of memory footprint of the object graph rooted at the specified object, and
     * also accepts a visitor that will be notified for every graph traversal event.
     * 
     * @param o the root of the object graph to be measured
     * @param visitor a visitor that will be notified for every graph traversal event
     * @return the estimation of memory footprint of the object graph rooted at the specified object
     */
    public static long count(Object o, ObjectGraphVisitor visitor) {
        return count(o, ObjectFilters.allowAny(), visitor);
    }
    
    /**
     * Returns the estimation of memory footprint of the object graph rooted at the specified object, 
     * filtering out some objects as per the specified object filter.
     * 
     * @param o the root of the object graph to be measured
     * @param objectFilter a filter that controls whether to traverse a specific object or not
     * @return the estimation of memory footprint of the traversed object graph rooted at the specified object
     */
    public static long count(Object o, ObjectFilter objectFilter) {
        return count(o, objectFilter, Visitors.empty());
    }
    
    /**
     * Returns the estimation of memory footprint of the object graph rooted at the specified object, 
     * filtering out some objects as per the specified object filter,
     * and also accepts a visitor that will be notified for every graph traversal event.
     * 
     * @param o the root of the object graph to be measured
     * @param objectFilter a filter that controls whether to traverse a specific object or not
     * @param visitor a visitor that will be notified for every graph traversal event
     * @return the estimation of memory footprint of the traversed object graph rooted at the specified object
     */
    public static long count(Object o, ObjectFilter objectFilter, ObjectGraphVisitor visitor) {
        if (!isOperational()) {
            throw new IllegalStateException("No instrumentation available (has the VM started with the appropriate agent?)");
        }
        if (objectFilter == null) {
            objectFilter = ObjectFilters.allowAny();
        }
        return measureRecursively(o, new IdentityHashMap<Object, Object>(), objectFilter, visitor);
    }
    
    
    private static long measureRecursively(Object root, Map<Object, Object> visited,
            ObjectFilter objectFilter, ObjectGraphVisitor visitor) {
        LinkedList<Node> stack = new LinkedList<Node>();
        Node rootNode = new Node(root);
        stack.addLast(rootNode);
        while (!stack.isEmpty()) {
            Node node = stack.removeLast();
            Object o = node.object();
            try {
                if (o == null ||
                        o instanceof Class ||
                        visited.containsKey(o)) {
                    continue;
                }
                if (!objectFilter.explore(o)) {
                    continue;
                }
                Class<?> c = o.getClass();
                node.addMemoryUsage(measure(o));
                if (Enum.class.isAssignableFrom(c)) {
                    node.addMemoryUsage(-measure(DummyEnum.CONSTANT)); //subtract the constant cost of an empty enum
                }
                visitor.begin(o, node.memoryUsage());
                visited.put(o, null);
                //instead of simply getFields(), use getDeclaredFields() for each class in the hierarcy,
                //as getFields() does not return inaccessible fields
                if (c.isArray()) {
                    int len = Array.getLength(o);
                    Class<?> ct = c.getComponentType();
                    if (!ct.isPrimitive()) {
                        for (int i = 0; i < len; i++) {
                            stack.addLast(new Node(node, Array.get(o, i)));
                        }
                    } // arrays of primitive type are already measured
                } else {
                    do {
                        if (Enum.class.equals(c)) {
                            break;
                        }
                        Field[] fields = c.getDeclaredFields();
                        AccessibleObject.setAccessible(fields, true);
                        for (Field f : fields) {
                            if (f.getType().isPrimitive() || Modifier.isStatic(f.getModifiers())) {
                                continue; //only non-static object references need to be traversed
                            }
                            try {
                                Object fieldValue = f.get(o);
                                visitor.field(f, fieldValue);
                                stack.addLast(new Node(node, fieldValue));
                            } catch (Exception e) {
                                throw new RuntimeException(e);
                            }
                        }
                        c = c.getSuperclass();
                    } while (c != null);
                }
            } finally {
                node.tryToFinish(visitor);
            }
        }
        return rootNode.memoryUsage();
    }

    private static class Node {
        private final Node parent;
        private int waitingFor;
        private long memoryUsage;
        private Object object;

        Node(Object object) {
            this.parent = null;
            this.object = object;
        }

        Node(Node parent, Object object) {
            this.parent = parent;
            this.object = object;
            parent.waitingFor++;
        }

        void addMemoryUsage(long memoryUsage) {
            this.memoryUsage += memoryUsage;
        }

        void tryToFinish(ObjectGraphVisitor visitor) {
            if (waitingFor > 0) {
                return; //wait for kids to finish this node
            }
            //non-recursive implementation
            Node current = this;
            while (current.parent != null) {
                visitor.end(current.object, current.memoryUsage);
                current.parent.waitingFor--;
                current.parent.addMemoryUsage(current.memoryUsage);
                if (current.parent.waitingFor == 0) {
                    current = current.parent;
                } else {
                    break;
                }
            }
        }

        boolean hasKids() {
            return waitingFor > 0;
        }

        Object object() {
            return object;
        }

        long memoryUsage() {
            return memoryUsage;
        }
    }

    private enum DummyEnum {
        CONSTANT;
    }
}
