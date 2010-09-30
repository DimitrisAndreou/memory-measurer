package objectexplorer;

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import java.lang.reflect.Field;
import java.util.ArrayDeque;
import java.util.Collections;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class Chain {
    private final Object value;
    private final Chain parent;

    Chain(Chain parent, Object value) {
        this.parent = parent;
        this.value = value;
    }

    static Chain root(Object value) {
        return new Chain(null, value);
    }

    FieldChain appendField(Field field, Object value) {
        return new FieldChain(this, Preconditions.checkNotNull(field), value);
    }

    ArrayIndexChain appendArrayIndex(int arrayIndex, Object value) {
        return new ArrayIndexChain(this, arrayIndex, value);
    }

    public boolean hasParent() {
        return parent != null;
    }

    public @Nonnull Chain getParent() {
        Preconditions.checkState(parent != null, "This is the root value, it has no parent");
        return parent;
    }

    public @Nullable Object getValue() {
        return value;
    }

    public boolean isThroughField() {
        return false;
    }

    public boolean isThroughArrayIndex() {
        return false;
    }

    public boolean isPrimitive() {
        return false;
    }

    Object getRoot() {
        Chain current = this;
        while (current.hasParent()) {
            current = current.getParent();
        }
        return current.getValue();
    }

    Deque<Chain> reverse() {
        Deque<Chain> reverseChain = new ArrayDeque<Chain>(8);
        Chain current = this;
        reverseChain.addFirst(current);
        while (current.hasParent()) {
            current = current.getParent();
            reverseChain.addFirst(current);
        }
        return reverseChain;
    }

    @Override public String toString() {
        StringBuilder sb = new StringBuilder(32);

        Iterator<Chain> it = reverse().iterator();
        sb.append(it.next().getValue());
        while (it.hasNext()) {
            sb.append("->");
            Chain current = it.next();
            if (current.isThroughField()) {
                sb.append(((FieldChain)current).getField().getName());
            } else if (current.isThroughArrayIndex()) {
                sb.append("[").append(((ArrayIndexChain)current).getArrayIndex()).append("]");
            }
        }
        return sb.toString();
    }

    public static class FieldChain extends Chain {
        private final Field field;

        FieldChain(Chain parent, Field referringField, Object value) {
            super(parent, value);
            this.field = referringField;
        }

        @Override
        public boolean isThroughField() {
            return true;
        }

        @Override
        public boolean isThroughArrayIndex() {
            return false;
        }

        @Override
        public boolean isPrimitive() {
            return field.getType().isPrimitive();
        }

        public Field getField() {
            return field;
        }
    }

    public static class ArrayIndexChain extends Chain {
        private final int index;

        ArrayIndexChain(Chain parent, int index, Object value) {
            super(parent, value);
            this.index = index;
        }

        @Override
        public boolean isThroughField() {
            return false;
        }

        @Override
        public boolean isThroughArrayIndex() {
            return true;
        }

        @Override
        public boolean isPrimitive() {
            return getParent().getValue().getClass().getComponentType().isPrimitive();
        }

        public int getArrayIndex() {
            return index;
        }
    }
}
