package objectexplorer;

import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.ImmutableList;
import java.lang.instrument.Instrumentation;

public class MemoryMeasurer {
    private static final Instrumentation instrumentation = InstrumentationGrabber.instrumentation();

    /*
     * The bare minimum memory footprint of an enum value, measured empirically.
     * This should be subtracted for any enum value encountered, since it
     * is static in nature.
     */
    private static final long costOfBareEnumConstant = instrumentation.getObjectSize(DummyEnum.CONSTANT);

    private enum DummyEnum {
        CONSTANT;
    }

    public static long measureBytes(Object o) {
        return measureBytes(o, Predicates.alwaysTrue());
    }

    public static long measureBytes(Object o, Predicate<Object> predicate) {
        Preconditions.checkNotNull(predicate, "predicate");

        Predicate<Chain> completePredicate = Predicates.and(ImmutableList.of(
            new ObjectExplorer.AtMostOncePredicate(),
            ObjectExplorer.notEnumFields,
            Predicates.compose(predicate, ObjectExplorer.chainToObject)
        ));

        return ObjectExplorer.exploreObject(o, new MemoryMeasurerVisitor(completePredicate));
    }

    private static class MemoryMeasurerVisitor implements ObjectVisitor<Long> {
        private long memory;
        private final Predicate<Chain> predicate;

        MemoryMeasurerVisitor(Predicate<Chain> predicate) {
            this.predicate = predicate;
        }

        public Traversal visit(Chain chain) {
            if (predicate.apply(chain)) {
                Object o = chain.getValue();
                memory += instrumentation.getObjectSize(o);
                if (Enum.class.isAssignableFrom(o.getClass())) {
                    memory -= costOfBareEnumConstant;
                }
                return Traversal.EXPLORE;
            }
            return Traversal.SKIP;
        }

        public Long result() {
            return memory;
        }
    }
}
