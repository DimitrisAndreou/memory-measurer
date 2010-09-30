package objectexplorer;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Lists;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Collections;
import java.util.Deque;
import java.util.EnumSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Set;
import javax.annotation.Nonnull;
import objectexplorer.Chain.FieldChain;
import objectexplorer.ObjectVisitor.Traversal;

public class ObjectExplorer {
    private ObjectExplorer() { }

    public static <T> T exploreObject(Object objectGraphRoot, ObjectVisitor<T> visitor) {
        return exploreObject(objectGraphRoot, visitor, EnumSet.noneOf(Feature.class));
    }

    public static <T> T exploreObject(Object objectGraphRoot, 
            ObjectVisitor<T> visitor, EnumSet<Feature> features) {
        Deque<Chain> stack = new ArrayDeque<Chain>(32);
        if (objectGraphRoot != null) stack.push(Chain.root(objectGraphRoot));

        while (!stack.isEmpty()) {
            Chain chain = stack.pop();
            //the only place where the return value of visit() is considered
            Traversal traversal = visitor.visit(chain);
            switch (traversal) {
                case SKIP: continue;
                case EXPLORE: break;
            }

            //only nonnull values pushed in the stack
            @Nonnull Object value = chain.getValue();
            Class<?> valueClass = value.getClass();
            if (valueClass.isArray()) {
                boolean isPrimitive = valueClass.getComponentType().isPrimitive();
                for (int i = Array.getLength(value) - 1; i >= 0; i--) {
                    Object childValue = Array.get(value, i);
                    if (isPrimitive) {
                        if (features.contains(Feature.VISIT_PRIMITIVES))
                            visitor.visit(chain.appendArrayIndex(i, childValue));
                        continue;
                    }
                    if (childValue == null) {
                        if (features.contains(Feature.VISIT_NULL))
                            visitor.visit(chain.appendArrayIndex(i, childValue));
                        continue;
                    }
                    stack.push(chain.appendArrayIndex(i, childValue));
                }
            } else {
                for (Field field : getAllFields(value)) {
                    if (Modifier.isStatic(field.getModifiers())) continue;
                    Object childValue = null;
                    try {
                        childValue = field.get(value);
                    } catch (Exception e) {
                        throw new AssertionError(e);
                    }
                    if (childValue == null) {
                        if (features.contains(Feature.VISIT_NULL))
                            visitor.visit(chain.appendField(field, childValue));
                        continue;
                    }
                    boolean isPrimitive = field.getType().isPrimitive();
                    Chain extendedChain = chain.appendField(field, childValue);
                    if (isPrimitive) {
                        if (features.contains(Feature.VISIT_PRIMITIVES))
                            visitor.visit(extendedChain);
                        continue;
                    } else {
                        stack.push(extendedChain);
                    }
                }
            }
        }
        return visitor.result();
    }

    static class AtMostOncePredicate implements Predicate<Chain> {
        private final Set<Object> interner = Collections.newSetFromMap(
                new IdentityHashMap<Object, Boolean>());

        public boolean apply(Chain chain) {
            Object o = chain.getValue();
            return o instanceof Class || interner.add(o);
        }
    };

    static final Predicate<Chain> notEnumFields = new Predicate<Chain>(){
        public boolean apply(Chain chain) {
            return !(chain instanceof FieldChain) ||
                    ((FieldChain)chain).getField().getDeclaringClass() != Enum.class;
        }
    };

    static final Function<Chain, Object> chainToObject =
            new Function<Chain, Object>() {
        public Object apply(Chain chain) {
            return chain.getValue();
        }
    };

    private static Iterable<Field> getAllFields(Object o) {
        List<Field> fields = Lists.newArrayListWithCapacity(8);
        Class<?> clazz = o.getClass();
        while (clazz != null) {
            fields.addAll(Arrays.asList(clazz.getDeclaredFields()));
            clazz = clazz.getSuperclass();
        }

        //all together so there is only one security check
        AccessibleObject.setAccessible(fields.toArray(new AccessibleObject[fields.size()]), true);
        return fields;
    }

    public enum Feature {
        VISIT_NULL,
        VISIT_PRIMITIVES
    }
}
