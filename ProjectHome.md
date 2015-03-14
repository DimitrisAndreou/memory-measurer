# Introduction #

## [Javadocs](http://memory-measurer.googlecode.com/svn/trunk/dist/javadoc/index.html) ##

A small tool that is very handy when e.g. you design data structures and want to see how much memory each one uses. To do this, it uses a simple reflection-based object-traversing framework ([ObjectExplorer](http://memory-measurer.googlecode.com/svn/trunk/dist/javadoc/objectexplorer/ObjectExplorer.html)). On it, it builds two facilities:

  * [MemoryMeasurer](http://memory-measurer.googlecode.com/svn/trunk/dist/javadoc/objectexplorer/MemoryMeasurer.html), which can estimate the memory footprint of an object graph _in bytes_. This requires installing a javaagent when running the JVM, e.g. by passing `-javaagent:path/to/object-explorer.jar`.

  * [ObjectGraphMeasurer](http://memory-measurer.googlecode.com/svn/trunk/dist/javadoc/objectexplorer/ObjectGraphMeasurer.html) does not need a javaagent, and can also give a much more qualitative measurement than MemoryMeasurer - it counts the number of objects, references, and primitives (of each kind) that an object graph entails.

Also of interest is the synergy with this project (of yours truly) : [JBenchy](http://code.google.com/p/jbenchy/)

Put together, they allow you to easily and systematically run and analyze benchmarks regarding data structures.

## How to use ##

An extremely simple example:

```
long memory = MemoryMeasurer.measureBytes(new HashMap());
```

or

```
Footprint footprint = ObjectGraphMeasurer.measure(new HashMap());
```