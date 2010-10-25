package memorymeasurer;

import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.base.Supplier;
import com.google.common.base.Throwables;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.HashMultiset;
import com.google.common.collect.ImmutableBiMap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.ImmutableMultiset;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSetMultimap;
import com.google.common.collect.ImmutableSortedMap;
import com.google.common.collect.ImmutableSortedSet;
import com.google.common.collect.MapMaker;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multiset;
import com.google.common.collect.TreeMultimap;
import com.google.common.collect.TreeMultiset;
import java.lang.reflect.Constructor;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.Delayed;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.TimeUnit;
import objectexplorer.MemoryMeasurer;
import objectexplorer.ObjectGraphMeasurer;
import objectexplorer.ObjectGraphMeasurer.Footprint;

public class ElementCostOfDataStructures {
    public static void main(String[] args) throws Exception {
        caption("Basic Lists, Sets, Maps");

        analyze(new CollectionPopulator(defaultSupplierFor(ArrayList.class)));
        analyze(new ImmutableListPopulator());

        analyze(new CollectionPopulator(defaultSupplierFor(HashSet.class)));
        analyze(new ImmutableSetPopulator());

        analyze(new CollectionPopulator(defaultSupplierFor(TreeSet.class), EntryFactories.COMPARABLE));
        analyze(new ImmutableSortedSetPopulator());

        analyze(new MapPopulator(defaultSupplierFor(HashMap.class)));
        analyze(new ImmutableMapPopulator());
        analyze(new MapPopulator(defaultSupplierFor(LinkedHashMap.class)));

        analyze(new MapPopulator(defaultSupplierFor(TreeMap.class), EntryFactories.COMPARABLE));
        analyze(new ImmutableSortedMapPopulator());

        caption("ConcurrentHashMap/MapMaker");

        analyze(new MapPopulator(defaultSupplierFor(ConcurrentHashMap.class)));
        analyze("MapMaker", new MapPopulator(new Supplier<Map>() { public Map get() { return
            new MapMaker().makeMap(); } }));
        analyze("MapMaker_Expires", new MapPopulator(new Supplier<Map>() { public Map get() { return
            new MapMaker().expiration(5, TimeUnit.DAYS).makeMap(); } }));
        analyze("MapMaker_Evicts", new MapPopulator(new Supplier<Map>() { public Map get() { return
            new MapMaker().maximumSize(1000000).makeMap(); } }));
        analyze("MapMaker_ExpiresEvicts", new MapPopulator(new Supplier<Map>() { public Map get() { return
            new MapMaker().maximumSize(1000000).expiration(3, TimeUnit.DAYS).makeMap(); } }));
        analyze("MapMaker_SoftKeys", new MapPopulator(new Supplier<Map>() { public Map get() { return
            new MapMaker().softKeys().makeMap(); } }));
        analyze("MapMaker_SoftValues", new MapPopulator(new Supplier<Map>() { public Map get() { return
            new MapMaker().softValues().makeMap(); } }));
        analyze("MapMaker_SoftKeysValues", new MapPopulator(new Supplier<Map>() { public Map get() { return
            new MapMaker().softKeys().softValues().makeMap(); } }));
        analyze("MapMaker_Evicts_SoftKeys", new MapPopulator(new Supplier<Map>() { public Map get() { return
            new MapMaker().maximumSize(1000000).softKeys().makeMap(); } }));
        analyze("MapMaker_Evicts_SoftValues", new MapPopulator(new Supplier<Map>() { public Map get() { return
            new MapMaker().maximumSize(1000000).softValues().makeMap(); } }));
        analyze("MapMaker_Evicts_SoftKeysValues", new MapPopulator(new Supplier<Map>() { public Map get() { return
            new MapMaker().maximumSize(1000000).softKeys().softValues().makeMap(); } }));
        analyze("MapMaker_Expires_SoftKeys", new MapPopulator(new Supplier<Map>() { public Map get() { return
            new MapMaker().expiration(3, TimeUnit.DAYS).softKeys().makeMap(); } }));
        analyze("MapMaker_Expires_SoftValues", new MapPopulator(new Supplier<Map>() { public Map get() { return
            new MapMaker().expiration(3, TimeUnit.DAYS).softValues().makeMap(); } }));
        analyze("MapMaker_Expires_SoftKeysValues", new MapPopulator(new Supplier<Map>() { public Map get() { return
            new MapMaker().expiration(3, TimeUnit.DAYS).softKeys().softValues().makeMap(); } }));
        analyze("MapMaker_ExpiresEvicts_SoftKeys", new MapPopulator(new Supplier<Map>() { public Map get() { return
            new MapMaker().maximumSize(1000000).expiration(3, TimeUnit.DAYS).softKeys().makeMap(); } }));
        analyze("MapMaker_ExpiresEvicts_SoftValues", new MapPopulator(new Supplier<Map>() { public Map get() { return
            new MapMaker().maximumSize(1000000).expiration(3, TimeUnit.DAYS).softValues().makeMap(); } }));
        analyze("MapMaker_ExpiresEvicts_SoftKeysValues", new MapPopulator(new Supplier<Map>() { public Map get() { return
            new MapMaker().maximumSize(1000000).expiration(3, TimeUnit.DAYS).softKeys().softValues().makeMap(); } }));

        caption("Multisets");

        analyze("HashMultiset_Worst", new MultisetPopulator_Worst(new Supplier<Multiset>() { public Multiset get() { return
            HashMultiset.create(); } }));
        analyze(new ImmutableSetMultimapPopulator_Worst());
        analyze("TreeMultiset_Worst", new MultisetPopulator_Worst(new Supplier<Multiset>() { public Multiset get() { return
            TreeMultiset.create(); } }, EntryFactories.COMPARABLE));

        analyze("HashMultiset_Best ", new MultisetPopulator_Best(new Supplier<Multiset>() { public Multiset get() { return
            HashMultiset.create(); } }));
        analyze(new ImmutableSetMultimapPopulator_Best());
        analyze("TreeMultiset_Best ", new MultisetPopulator_Best(new Supplier<Multiset>() { public Multiset get() { return
            TreeMultiset.create(); } }, EntryFactories.COMPARABLE));

        caption("Multimaps");

        analyze("HashMultimap_Worst", new MultimapPopulator_Worst(new Supplier<Multimap>() { public Multimap get() { return
            HashMultimap.create(); } }));
        analyze("TreeMultimap_Worst", new MultimapPopulator_Worst(new Supplier<Multimap>() { public Multimap get() { return
            TreeMultimap.create(); } }, EntryFactories.COMPARABLE));
        analyze(new ImmutableMultimapPopulator_Worst());
        analyze("ArrayListMultimap_Worst", new MultimapPopulator_Worst(new Supplier<Multimap>() { public Multimap get() { return
            ArrayListMultimap.create(); } }));
        analyze(new ImmutableListMultimapPopulator_Worst());

        analyze("HashMultimap_Best ", new MultimapPopulator_Best(new Supplier<Multimap>() { public Multimap get() { return
            HashMultimap.create(); } }));
        analyze("TreeMultimap_Best ", new MultimapPopulator_Best(new Supplier<Multimap>() { public Multimap get() { return
            TreeMultimap.create(); } }, EntryFactories.COMPARABLE));
        analyze(new ImmutableMultimapPopulator_Best());
        analyze("ArrayListMultimap_Best ", new MultimapPopulator_Best(new Supplier<Multimap>() { public Multimap get() { return
            ArrayListMultimap.create(); } }));
        analyze(new ImmutableListMultimapPopulator_Best());

        caption("BiMaps");

        analyze("HashBiMap", new MapPopulator(new Supplier<Map>() { public Map get() { return
            HashBiMap.create(); } }));
        analyze(new ImmutableBiMapPopulator());

        caption("Misc");

        analyze(new MapPopulator(defaultSupplierFor(WeakHashMap.class)));
        analyze(new CollectionPopulator(defaultSupplierFor(LinkedList.class)));
        analyze(new CollectionPopulator(defaultSupplierFor(ArrayDeque.class)));
        analyze(new CollectionPopulator(defaultSupplierFor(LinkedHashSet.class)));
        analyze(new CollectionPopulator(defaultSupplierFor(PriorityQueue.class), EntryFactories.COMPARABLE));
        analyze(new CollectionPopulator(defaultSupplierFor(PriorityBlockingQueue.class), EntryFactories.COMPARABLE));
        analyze(new CollectionPopulator(defaultSupplierFor(ConcurrentSkipListSet.class), EntryFactories.COMPARABLE));
        analyze(new CollectionPopulator(defaultSupplierFor(CopyOnWriteArrayList.class)));
        analyze(new CollectionPopulator(defaultSupplierFor(CopyOnWriteArraySet.class)));
        analyze(new CollectionPopulator(defaultSupplierFor(DelayQueue.class), EntryFactories.DELAYED));
        analyze(new CollectionPopulator(defaultSupplierFor(LinkedBlockingQueue.class)));
        analyze(new CollectionPopulator(defaultSupplierFor(LinkedBlockingDeque.class)));
    }

    private static void caption(String caption) {
        System.out.println();
        System.out.println("========================================== " + caption
                        + " ==========================================");
        System.out.println();
    }

    static void analyze(Populator<?> populator) {
        analyze(populator.toString(), populator);
    }

    static void analyze(String caption, Populator<?> populator) {
        AvgEntryCost cost = averageEntryCost(populator, 16, 256 * 31);
        System.out.printf("%40s -- Bytes = %6.2f, Objects = %5.2f Refs = %5.2f Primitives = %s%n",
            caption, cost.bytes, cost.objects, cost.refs, cost.primitives);
    }

    static AvgEntryCost averageEntryCost(Populator<?> populator, int initialEntries, int entriesToAdd) {
        Preconditions.checkArgument(initialEntries >= 0, "initialEntries negative");
        Preconditions.checkArgument(entriesToAdd > 0, "entriesToAdd negative or zero");

        Predicate<Object> predicate = Predicates.not(Predicates.instanceOf(
                populator.getEntryType()));

        Object collection1 = populator.construct(initialEntries);
        Footprint footprint1 = ObjectGraphMeasurer.measure(collection1, predicate);
        long bytes1 = MemoryMeasurer.measureBytes(collection1, predicate);

        Object collection2 = populator.construct(initialEntries + entriesToAdd);
        Footprint footprint2 = ObjectGraphMeasurer.measure(collection2, predicate);
        long bytes2 = MemoryMeasurer.measureBytes(collection2, predicate);

        double objects = (footprint2.getObjects() - footprint1.getObjects()) / (double) entriesToAdd;
        double refs = (footprint2.getReferences() - footprint1.getReferences()) / (double) entriesToAdd;
        double bytes = (bytes2 - bytes1) / (double)entriesToAdd;

        Map<Class<?>, Double> primitives = Maps.newHashMap();
        for (Class<?> primitiveType : primitiveTypes) {
            int initial = footprint1.getPrimitives().count(primitiveType);
            int ending = footprint2.getPrimitives().count(primitiveType);
            if (initial != ending) {
                primitives.put(primitiveType, (ending - initial) / (double) entriesToAdd);
            }
        }

        return new AvgEntryCost(objects, refs, primitives, bytes);
    }

    private static final ImmutableSet<Class<?>> primitiveTypes = ImmutableSet.<Class<?>>of(
            boolean.class, byte.class, char.class, short.class,
            int.class, float.class, long.class, double.class);

    private static class AvgEntryCost {
        final double objects;
        final double refs;
        final ImmutableMap<Class<?>, Double> primitives;
        final double bytes;
        AvgEntryCost(double objects, double refs, Map<Class<?>, Double> primitives, double bytes) {
            this.objects = objects;
            this.refs = refs;
            this.primitives = ImmutableMap.copyOf(primitives);
            this.bytes = bytes;
        }
    }

    private static class DefaultConstructorSupplier<C> implements Supplier<C> {
        private final Constructor<C> constructor;
        DefaultConstructorSupplier(Class<C> clazz) throws NoSuchMethodException {
            this.constructor = clazz.getConstructor();
        }
        public C get() {
            try {
                return constructor.newInstance();
            } catch (Exception e) {
                throw Throwables.propagate(e);
            }
        }

        @Override
        public String toString() {
            return constructor.getDeclaringClass().getSimpleName();
        }
    }

    static <C> DefaultConstructorSupplier<C> defaultSupplierFor(Class<C> clazz) throws NoSuchMethodException {
        return new DefaultConstructorSupplier(clazz);
    }
}

interface Populator<C> {
    Class<?> getEntryType();

    C construct(int entries);
}

abstract class AbstractPopulator<C> implements Populator<C> {
    private final EntryFactory entryFactory;
    AbstractPopulator() { this(EntryFactories.REGULAR); }
    AbstractPopulator(EntryFactory entryFactory) {
        this.entryFactory = entryFactory;
    }

    protected Object newEntry() {
        return entryFactory.get();
    }

    public Class<?> getEntryType() {
        return entryFactory.getEntryType();
    }
}

abstract class MutablePopulator<C> extends AbstractPopulator<C> {
    private final Supplier<? extends C> factory;

    MutablePopulator(Supplier<? extends C> factory) {
        this(factory, EntryFactories.REGULAR);
    }

    MutablePopulator(Supplier<? extends C> factory, EntryFactory entryFactory) {
        super(entryFactory);
        this.factory = factory;
    }

    protected abstract void addEntry(C target);

    public C construct(int entries) {
        C collection = factory.get();
        for (int i = 0; i < entries; i++) {
            addEntry(collection);
        }
        return collection;
    }

    @Override
    public String toString() {
        return factory.toString();
    }
}

class MapPopulator extends MutablePopulator<Map> {
    MapPopulator(Supplier<? extends Map> mapFactory) {
        super(mapFactory);
    }

    MapPopulator(Supplier<? extends Map> mapFactory, EntryFactory entryFactory) {
        super(mapFactory, entryFactory);
    }

    public void addEntry(Map map) {
        map.put(newEntry(), newEntry());
    }
}

class CollectionPopulator extends MutablePopulator<Collection> {
    CollectionPopulator(Supplier<? extends Collection> collectionFactory) {
        super(collectionFactory);
    }

    CollectionPopulator(Supplier<? extends Collection> collectionFactory, EntryFactory entryFactory) {
        super(collectionFactory, entryFactory);
    }

    public void addEntry(Collection collection) {
        collection.add(newEntry());
    }
}

class MultimapPopulator_Worst extends MutablePopulator<Multimap> {
    MultimapPopulator_Worst(Supplier<? extends Multimap> multimapFactory) {
        super(multimapFactory);
    }
    MultimapPopulator_Worst(Supplier<? extends Multimap> multimapFactory, EntryFactory entryFactory) {
        super(multimapFactory, entryFactory);
    }

    public void addEntry(Multimap multimap) {
        multimap.put(newEntry(), newEntry());
    }
}

class MultimapPopulator_Best extends MutablePopulator<Multimap> {
    MultimapPopulator_Best(Supplier<? extends Multimap> multimapFactory) {
        super(multimapFactory);
    }
    MultimapPopulator_Best(Supplier<? extends Multimap> multimapFactory, EntryFactory entryFactory) {
        super(multimapFactory, entryFactory);
    }

    private final Object key = newEntry();
    public void addEntry(Multimap multimap) {
        multimap.put(key, newEntry());
    }
}

class MultisetPopulator_Worst extends MutablePopulator<Multiset> {
    MultisetPopulator_Worst(Supplier<? extends Multiset> multisetFactory) {
        super(multisetFactory);
    }
    MultisetPopulator_Worst(Supplier<? extends Multiset> multisetFactory, EntryFactory entryFactory) {
        super(multisetFactory, entryFactory);
    }

    public void addEntry(Multiset multiset) {
        multiset.add(newEntry());
    }
}

class MultisetPopulator_Best extends MutablePopulator<Multiset> {
    MultisetPopulator_Best(Supplier<? extends Multiset> multisetFactory) {
        super(multisetFactory);
    }
    MultisetPopulator_Best(Supplier<? extends Multiset> multisetFactory, EntryFactory entryFactory) {
        super(multisetFactory, entryFactory);
    }

    private final Object key = newEntry();
    public void addEntry(Multiset multiset) {
        multiset.add(key);
    }
}

/** Immutable classes */

class ImmutableListPopulator extends AbstractPopulator<ImmutableList> {
    public ImmutableList construct(int entries) {
        ImmutableList.Builder builder = ImmutableList.builder();
        for (int i = 0; i < entries; i++) {
            builder.add(newEntry());
        }
        return builder.build();
    }

    @Override
    public String toString() {
        return "ImmutableList";
    }
}

class ImmutableSetPopulator extends AbstractPopulator<ImmutableSet> {
    public ImmutableSet construct(int entries) {
        ImmutableSet.Builder builder = ImmutableSet.builder();
        for (int i = 0; i < entries; i++) {
            builder.add(newEntry());
        }
        return builder.build();
    }

    @Override
    public String toString() {
        return "ImmutableSet";
    }
}

class ImmutableMapPopulator extends AbstractPopulator<ImmutableMap> {
    public ImmutableMap construct(int entries) {
        ImmutableMap.Builder builder = ImmutableMap.builder();
        for (int i = 0; i < entries; i++) {
            builder.put(newEntry(), newEntry());
        }
        return builder.build();
    }

    @Override
    public String toString() {
        return "ImmutableMap";
    }
}

class ImmutableSortedSetPopulator extends AbstractPopulator<ImmutableSortedSet> {
    ImmutableSortedSetPopulator() {
        super(EntryFactories.COMPARABLE);
    }

    public ImmutableSortedSet construct(int entries) {
        ImmutableSortedSet.Builder builder = ImmutableSortedSet.<Comparable>naturalOrder();
        for (int i = 0; i < entries; i++) {
            builder.add(newEntry());
        }
        return builder.build();
    }

    @Override
    public String toString() {
        return "ImmutableSortedSet";
    }
}

class ImmutableSortedMapPopulator extends AbstractPopulator<ImmutableSortedMap> {
    ImmutableSortedMapPopulator() {
        super(EntryFactories.COMPARABLE);
    }

    public ImmutableSortedMap construct(int entries) {
        ImmutableSortedMap.Builder builder = ImmutableSortedMap.<Comparable, Object>naturalOrder();
        for (int i = 0; i < entries; i++) {
            builder.put(newEntry(), newEntry());
        }
        return builder.build();
    }

    @Override
    public String toString() {
        return "ImmutableSortedMap";
    }
}

class ImmutableBiMapPopulator extends AbstractPopulator<ImmutableBiMap> {
    public ImmutableBiMap construct(int entries) {
        ImmutableBiMap.Builder builder = ImmutableBiMap.builder();
        for (int i = 0; i < entries; i++) {
            builder.put(newEntry(), newEntry());
        }
        return builder.build();
    }

    @Override
    public String toString() {
        return "ImmutableBiMap";
    }
}

class ImmutableMultimapPopulator_Worst extends AbstractPopulator<ImmutableMultimap> {
    public ImmutableMultimap construct(int entries) {
        ImmutableMultimap.Builder builder = ImmutableMultimap.builder();
        for (int i = 0; i < entries; i++) {
            builder.put(newEntry(), newEntry());
        }
        return builder.build();
    }

    @Override
    public String toString() {
        return "ImmutableMultimap_Worst";
    }
}

class ImmutableMultimapPopulator_Best extends AbstractPopulator<ImmutableMultimap> {
    public ImmutableMultimap construct(int entries) {
        ImmutableMultimap.Builder builder = ImmutableMultimap.builder();
        Object key = newEntry();
        for (int i = 0; i < entries; i++) {
            builder.put(key, newEntry());
        }
        return builder.build();
    }

    @Override
    public String toString() {
        return "ImmutableMultimap_Best ";
    }
}

class ImmutableListMultimapPopulator_Worst extends AbstractPopulator<ImmutableListMultimap> {
    public ImmutableListMultimap construct(int entries) {
        ImmutableListMultimap.Builder builder = ImmutableListMultimap.builder();
        for (int i = 0; i < entries; i++) {
            builder.put(newEntry(), newEntry());
        }
        return builder.build();
    }

    @Override
    public String toString() {
        return "ImmutableListMultimap_Worst";
    }
}

class ImmutableListMultimapPopulator_Best extends AbstractPopulator<ImmutableListMultimap> {
    public ImmutableListMultimap construct(int entries) {
        ImmutableListMultimap.Builder builder = ImmutableListMultimap.builder();
        Object key = newEntry();
        for (int i = 0; i < entries; i++) {
            builder.put(key, newEntry());
        }
        return builder.build();
    }

    @Override
    public String toString() {
        return "ImmutableListMultimap_Best ";
    }
}

class ImmutableSetMultimapPopulator_Worst extends AbstractPopulator<ImmutableSetMultimap> {
    public ImmutableSetMultimap construct(int entries) {
        ImmutableSetMultimap.Builder builder = ImmutableSetMultimap.builder();
        for (int i = 0; i < entries; i++) {
            builder.put(newEntry(), newEntry());
        }
        return builder.build();
    }

    @Override
    public String toString() {
        return "ImmutableSetMultimap_Worst";
    }
}

class ImmutableSetMultimapPopulator_Best extends AbstractPopulator<ImmutableSetMultimap> {
    public ImmutableSetMultimap construct(int entries) {
        ImmutableSetMultimap.Builder builder = ImmutableSetMultimap.builder();
        Object key = newEntry();
        for (int i = 0; i < entries; i++) {
            builder.put(key, newEntry());
        }
        return builder.build();
    }

    @Override
    public String toString() {
        return "ImmutableSetMultimap_Best ";
    }
}

class ImmutableMultisetPopulator_Worst extends AbstractPopulator<ImmutableMultiset> {
    public ImmutableMultiset construct(int entries) {
        ImmutableMultiset.Builder builder = ImmutableMultiset.builder();
        for (int i = 0; i < entries; i++) {
            builder.add(newEntry());
        }
        return builder.build();
    }

    @Override
    public String toString() {
        return "ImmutableMultiset_Worst";
    }
}

class ImmutableMultisetPopulator_Best extends AbstractPopulator<ImmutableMultiset> {
    public ImmutableMultiset construct(int entries) {
        ImmutableMultiset.Builder builder = ImmutableMultiset.builder();
        Object key = newEntry();
        for (int i = 0; i < entries; i++) {
            builder.add(key);
        }
        return builder.build();
    }

    @Override
    public String toString() {
        return "ImmutableMultiset_Best ";
    }
}


interface EntryFactory extends Supplier {
    Class<?> getEntryType();
}

enum EntryFactories implements EntryFactory {
    REGULAR {
        public Class<?> getEntryType() { return Element.class; }
        public Object get() { return new Element(); }
    },
    COMPARABLE {
        public Class<?> getEntryType() { return ComparableElement.class; }
        public Object get() { return new ComparableElement(); }
    },
    DELAYED {
        public Class<?> getEntryType() { return DelayedElement.class; }
        public Object get() { return new DelayedElement(); }
    };
}

class Element { }

class ComparableElement extends Element implements Comparable {
    public int compareTo(Object o) { return 1; }
}

class DelayedElement extends Element implements Delayed {
    public long getDelay(TimeUnit unit) { return 0; }
    public int compareTo(Delayed o) { return 1; }
}
