package objectexplorer;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.ImmutableList;
import java.lang.instrument.Instrumentation;
import java.util.EnumSet;
import objectexplorer.ObjectExplorer.Feature;

public class ObjectGraphMeasurer {
    public static class Stats {
        private final int objects;
        private final int references;
        private final int primitives;

        Stats(int objects, int references, int primitives) {
            this.objects = objects;
            this.references = references;
            this.primitives = primitives;
        }

        public int getObjects() {
            return objects;
        }

        public int getReferences() {
            return references;
        }

        public int getPrimitives() {
            return primitives;
        }

        @Override
        public String toString() {
            return Objects.toStringHelper(this)
                .add("Objects", objects)
                .add("References", references)
                .add("Primitives", primitives)
                .toString();
        }
    }

    public static Stats measure(Object o) {
        return measure(o, Predicates.alwaysTrue());
    }

    public static Stats measure(Object o, Predicate<Object> predicate) {
        Preconditions.checkNotNull(predicate, "predicate");

        Predicate<Chain> completePredicate = Predicates.and(ImmutableList.of(
            new ObjectExplorer.AtMostOncePredicate(),
            ObjectExplorer.notEnumFields,
            Predicates.compose(predicate, ObjectExplorer.chainToObject)
        ));

        return ObjectExplorer.exploreObject(o, new ObjectGraphVisitor(completePredicate),
                EnumSet.of(Feature.VISIT_PRIMITIVES, Feature.VISIT_NULL));
    }

    private static class ObjectGraphVisitor implements ObjectVisitor<Stats> {
        private int objects;
        private int references = -1; //to account for the root object, which has no reference leading to it
        private int primitives;
        private final Predicate<Chain> predicate;

        ObjectGraphVisitor(Predicate<Chain> predicate) {
            this.predicate = predicate;
        }

        public Traversal visit(Chain chain) {
            if (chain.isPrimitive() || chain.getValue() instanceof String) {
                primitives++;
                return Traversal.SKIP;
            } else {
                references++;
            }
            if (predicate.apply(chain) && chain.getValue() != null) {
                objects++;
                return Traversal.EXPLORE;
            }
            return Traversal.SKIP;
        }

        public Stats result() {
            return new Stats(objects, references, primitives);
        }
    }
}
