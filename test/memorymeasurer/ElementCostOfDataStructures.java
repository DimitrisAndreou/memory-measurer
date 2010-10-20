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
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.MapMaker;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
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
        ImmutableList.Builder<Analyzer> builder = ImmutableList.builder();
        builder.add(Analyzer.createCollection("ArrayList", defaultSupplierFor(ArrayList.class)));
        builder.add(Analyzer.createCollection("LinkedList", defaultSupplierFor(LinkedList.class)));
        builder.add(Analyzer.createCollection("ArrayDeque", defaultSupplierFor(ArrayDeque.class)));
        builder.add(Analyzer.createCollection("HashSet", defaultSupplierFor(HashSet.class)));
        builder.add(Analyzer.createCollection("LinkedHashSet", defaultSupplierFor(LinkedHashSet.class)));
        builder.add(Analyzer.createCollection("PriorityQueue", defaultSupplierFor(PriorityQueue.class), Analyzer.ElementType.COMPARABLE));
        builder.add(Analyzer.createCollection("PriorityBlockingQueue", defaultSupplierFor(PriorityBlockingQueue.class), Analyzer.ElementType.DELAYED));
        builder.add(Analyzer.createCollection("TreeSet", defaultSupplierFor(TreeSet.class), Analyzer.ElementType.COMPARABLE));
        builder.add(Analyzer.createCollection("ConcurrentSkipListSet", defaultSupplierFor(ConcurrentSkipListSet.class), Analyzer.ElementType.COMPARABLE));
        builder.add(Analyzer.createCollection("CopyOnWriteArrayList", defaultSupplierFor(CopyOnWriteArrayList.class)));
        builder.add(Analyzer.createCollection("CopyOnWriteArraySet", defaultSupplierFor(CopyOnWriteArraySet.class)));
        builder.add(Analyzer.createCollection("DelayQueue", defaultSupplierFor(DelayQueue.class), Analyzer.ElementType.DELAYED));
        builder.add(Analyzer.createCollection("LinkedBlockingQueue", defaultSupplierFor(LinkedBlockingQueue.class)));
        builder.add(Analyzer.createCollection("LinkedBlockingDeque", defaultSupplierFor(LinkedBlockingDeque.class)));
        builder.add(Analyzer.createMap("HashMap", defaultSupplierFor(HashMap.class)));
        builder.add(Analyzer.createMap("LinkedHashMap", defaultSupplierFor(LinkedHashMap.class)));
        builder.add(Analyzer.createMap("TreeMap", defaultSupplierFor(TreeMap.class), Analyzer.ElementType.COMPARABLE));
        builder.add(Analyzer.createMap("WeakHashMap", defaultSupplierFor(WeakHashMap.class)));
        builder.add(Analyzer.createMap("ConcurrentHashMap", defaultSupplierFor(ConcurrentHashMap.class)));
        builder.add(Analyzer.createMap("MapMaker", new Supplier<Map>() { public Map get() { return
            new MapMaker().makeMap(); } }));
        builder.add(Analyzer.createMap("MapMaker_Expires", new Supplier<Map>() { public Map get() { return
            new MapMaker().expiration(5, TimeUnit.DAYS).makeMap(); } }));
        builder.add(Analyzer.createMap("MapMaker_Evicts", new Supplier<Map>() { public Map get() { return
            new MapMaker().maximumSize(1000000).makeMap(); } }));
        builder.add(Analyzer.createMap("MapMaker_Expires", new Supplier<Map>() { public Map get() { return
            new MapMaker().expiration(5, TimeUnit.DAYS).makeMap(); } }));
        builder.add(Analyzer.createMap("MapMaker_Expires", new Supplier<Map>() { public Map get() { return
            new MapMaker().expiration(5, TimeUnit.DAYS).makeMap(); } }));
        builder.add(Analyzer.createMap("MapMaker_Expires", new Supplier<Map>() { public Map get() { return
            new MapMaker().expiration(5, TimeUnit.DAYS).makeMap(); } }));
        builder.add(Analyzer.createMap("MapMaker_ExpiresEvicts", new Supplier<Map>() { public Map get() { return
            new MapMaker().maximumSize(1000000).expiration(3, TimeUnit.DAYS).makeMap(); } }));
        builder.add(Analyzer.createMap("MapMaker_SoftKeys", new Supplier<Map>() { public Map get() { return
            new MapMaker().softKeys().makeMap(); } }));
        builder.add(Analyzer.createMap("MapMaker_SoftValues", new Supplier<Map>() { public Map get() { return
            new MapMaker().softValues().makeMap(); } }));
        builder.add(Analyzer.createMap("MapMaker_SoftKeysValues", new Supplier<Map>() { public Map get() { return
            new MapMaker().softKeys().softValues().makeMap(); } }));
        builder.add(Analyzer.createMap("MapMaker_Evicts_SoftKeys", new Supplier<Map>() { public Map get() { return
            new MapMaker().maximumSize(1000000).softKeys().makeMap(); } }));
        builder.add(Analyzer.createMap("MapMaker_Evicts_SoftValues", new Supplier<Map>() { public Map get() { return
            new MapMaker().maximumSize(1000000).softValues().makeMap(); } }));
        builder.add(Analyzer.createMap("MapMaker_Evicts_SoftKeysValues", new Supplier<Map>() { public Map get() { return
            new MapMaker().maximumSize(1000000).softKeys().softValues().makeMap(); } }));
        builder.add(Analyzer.createMap("MapMaker_Expires_SoftKeys", new Supplier<Map>() { public Map get() { return
            new MapMaker().expiration(3, TimeUnit.DAYS).softKeys().makeMap(); } }));
        builder.add(Analyzer.createMap("MapMaker_Expires_SoftValues", new Supplier<Map>() { public Map get() { return
            new MapMaker().expiration(3, TimeUnit.DAYS).softValues().makeMap(); } }));
        builder.add(Analyzer.createMap("MapMaker_Expires_SoftKeysValues", new Supplier<Map>() { public Map get() { return
            new MapMaker().expiration(3, TimeUnit.DAYS).softKeys().softValues().makeMap(); } }));
        builder.add(Analyzer.createMap("MapMaker_ExpiresEvicts_SoftKeys", new Supplier<Map>() { public Map get() { return
            new MapMaker().maximumSize(1000000).expiration(3, TimeUnit.DAYS).softKeys().makeMap(); } }));
        builder.add(Analyzer.createMap("MapMaker_ExpiresEvicts_SoftValues", new Supplier<Map>() { public Map get() { return
            new MapMaker().maximumSize(1000000).expiration(3, TimeUnit.DAYS).softValues().makeMap(); } }));
        builder.add(Analyzer.createMap("MapMaker_ExpiresEvicts_SoftKeysValues", new Supplier<Map>() { public Map get() { return
            new MapMaker().maximumSize(1000000).expiration(3, TimeUnit.DAYS).softKeys().softValues().makeMap(); } }));
        builder.add(Analyzer.createCollection("HashMultiset (frequency=1)", new Supplier<Collection>() { public Collection get() { return
            HashMultiset.create(); } }));
        builder.add(Analyzer.createCollection("TreeMultiset (frequency=1)", new Supplier<Collection>() { public Collection get() { return
            TreeMultiset.create(); } }, Analyzer.ElementType.COMPARABLE));
        builder.add(Analyzer.createMap("HashBiMap", new Supplier<Map>() { public Map get() { return
            HashBiMap.create(); } }));
        builder.add(Analyzer.createMultimapWorst("HashMultimap (Worst)", new Supplier<Multimap>() { public Multimap get() { return
            HashMultimap.create(); } }));
        builder.add(Analyzer.createMultimapBest("HashMultimap (Best)", new Supplier<Multimap>() { public Multimap get() { return
            HashMultimap.create(); } }));
        builder.add(Analyzer.createMultimapWorst("ArrayListMultimap (Worst)", new Supplier<Multimap>() { public Multimap get() { return
            ArrayListMultimap.create(); } }));
        builder.add(Analyzer.createMultimapBest("ArrayListMultimap (Best)", new Supplier<Multimap>() { public Multimap get() { return
            ArrayListMultimap.create(); } }));
        builder.add(Analyzer.createMultimapWorst("TreeMultimap (Worst)", new Supplier<Multimap>() { public Multimap get() { return
            TreeMultimap.create(); } }, Analyzer.ElementType.COMPARABLE));
        builder.add(Analyzer.createMultimapBest("TreeMultimap (Best)", new Supplier<Multimap>() { public Multimap get() { return
            TreeMultimap.create(); } }, Analyzer.ElementType.COMPARABLE));

        //immutable collections

        ImmutableList<Analyzer> analyzers = builder.build();
        for (Analyzer analyzer : analyzers) {
            AvgEntryCost cost = analyzer.averageEntryCost(16, 1024 * 16);
            System.out.printf("%40s -- Bytes = %6.2f, Objects = %5.2f Refs = %5.2f Primitives = %s%n",
                    analyzer.getDescription(), cost.bytes, cost.objects, cost.refs, cost.primitives);
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
    }

    static <C> DefaultConstructorSupplier<C> defaultSupplierFor(Class<C> clazz) throws NoSuchMethodException {
        return new DefaultConstructorSupplier(clazz);
    }
}

interface Populator<C> {
    Class<?> entryType();
    void addEntry(C target);
}

class Analyzer {
    private final String description;
    private final SupplierAndPopulator<?> supplierAndPopulator;
    
    Analyzer(String description, SupplierAndPopulator<?> supplierAndPopulator) {
        this.description = description;
        this.supplierAndPopulator = supplierAndPopulator;
    }

    static <C> Analyzer newAnalyzer(String description,
            Supplier<C> supplier, Populator<? super C> populator) {
        return new Analyzer(description, new SupplierAndPopulator<C>(supplier, populator));
    }

    private static class Element { }

    private static class ComparableElement extends Element implements Comparable {
        public int compareTo(Object o) { return 1; }
    }

    private static class DelayedElement extends Element implements Delayed {
        public long getDelay(TimeUnit unit) { return 0; }
        public int compareTo(Delayed o) { return 1; }
    }

    enum ElementType {
        REGULAR {
            Class<?> getElementClass() { return Element.class; }
            Object createElement() { return new Element(); }
        },
        COMPARABLE {
            Class<?> getElementClass() { return ComparableElement.class; }
            Object createElement() { return new ComparableElement(); }
        },
        DELAYED {
            Class<?> getElementClass() { return DelayedElement.class; }
            Object createElement() { return new DelayedElement(); }
        };

        abstract Class<?> getElementClass();
        abstract Object createElement();
    }

    //the purpose of this class is to not unnecessarily expose the parameter type in the outer class interface
    static class SupplierAndPopulator<C> {
        final Supplier<? extends C> supplier;
        final Populator<? super C> populator;

        SupplierAndPopulator(Supplier<? extends C> supplier, Populator<? super C> populator) {
            this.supplier = supplier;
            this.populator = populator;
        }

        class Population {
            C target = supplier.get();

            void addEntry() {
                populator.addEntry(target);
            }
        }
    }

    AvgEntryCost averageEntryCost(int initialEntries, int entriesToAdd) {
        Preconditions.checkArgument(initialEntries >= 0, "initialEntries negative");
        Preconditions.checkArgument(entriesToAdd > 0, "entriesToAdd negative or zero");
        SupplierAndPopulator.Population population = supplierAndPopulator.new Population();
        for (int i = 1; i <= initialEntries; i++) {
            population.addEntry();
        }
        Predicate<Object> predicate = Predicates.not(Predicates.instanceOf(
                supplierAndPopulator.populator.entryType()));
        Footprint initialCost = ObjectGraphMeasurer.measure(population.target, predicate);
        long bytes1 = MemoryMeasurer.measureBytes(population.target, predicate);
        for (int i = 0; i < entriesToAdd; i++) {
            population.addEntry();
        }
        Footprint finalCost = ObjectGraphMeasurer.measure(population.target, predicate);
        long bytes2 = MemoryMeasurer.measureBytes(population.target, predicate);
        double objects = (finalCost.getObjects() - initialCost.getObjects()) / (double) entriesToAdd;
        double refs = (finalCost.getReferences() - initialCost.getReferences()) / (double) entriesToAdd;
        double bytes = (bytes2 - bytes1) / (double)entriesToAdd;

        Map<Class<?>, Double> primitives = Maps.newHashMap();
        for (Class<?> primitiveType : primitiveTypes) {
            int initial = initialCost.getPrimitives().count(primitiveType);
            int ending = finalCost.getPrimitives().count(primitiveType);
            if (initial != ending) {
                primitives.put(primitiveType, (ending - initial) / (double) entriesToAdd);
            }
        }

        return new AvgEntryCost(objects, refs, primitives, bytes);
    }

    private static final ImmutableSet<Class<?>> primitiveTypes = ImmutableSet.<Class<?>>of(
            boolean.class, byte.class, char.class, short.class,
            int.class, float.class, long.class, double.class);

    String getDescription() {
        return description;
    }

    static Analyzer createMap(String description, Supplier<? extends Map> supplier) {
        return createMap(description, supplier, ElementType.REGULAR);
    }
    static Analyzer createMap(String description, Supplier<? extends Map> supplier,
            final ElementType elementType) {
        return new Analyzer(description, new SupplierAndPopulator<Map>(supplier, new Populator<Map>() {
            public Class<?> entryType() { return elementType.getElementClass(); }
            public void addEntry(Map target) {
                Object x = elementType.createElement();
                target.put(x, x);
            }
        }));
    }

    static Analyzer createCollection(String description, Supplier<? extends Collection> supplier) {
        return createCollection(description, supplier, ElementType.REGULAR);
    }
    static Analyzer createCollection(String description, Supplier<? extends Collection> supplier,
            final ElementType elementType) {
        return new Analyzer(description, new SupplierAndPopulator<Collection>(supplier, new Populator<Collection>() {
            public Class<?> entryType() { return elementType.getElementClass(); }
            public void addEntry(Collection target) { target.add(elementType.createElement()); }
        }));
    }

    static Analyzer createMultimapWorst(String description, Supplier<? extends Multimap> supplier) {
        return createMultimapWorst(description, supplier, ElementType.REGULAR);
    }
    static Analyzer createMultimapWorst(String description, Supplier<? extends Multimap> supplier,
            final ElementType elementType) {
        return new Analyzer(description, new SupplierAndPopulator<Multimap>(supplier, new Populator<Multimap>() {
            public Class<?> entryType() { return elementType.getElementClass(); }
            public void addEntry(Multimap target) {
                Object x = elementType.createElement();
                target.put(x, x);
            }
        }));
    }

    static Analyzer createMultimapBest(String description, Supplier<? extends Multimap> supplier) {
        return createMultimapBest(description, supplier, ElementType.REGULAR);
    }
    static Analyzer createMultimapBest(String description, Supplier<? extends Multimap> supplier,
            final ElementType elementType) {
        return new Analyzer(description, new SupplierAndPopulator<Multimap>(supplier, new Populator<Multimap>() {
            public Class<?> entryType() { return elementType.getElementClass(); }

            final Object key = elementType.createElement();
            public void addEntry(Multimap target) {
                target.put(key, elementType.createElement());
            }
        }));
    }
}

class AvgEntryCost {
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