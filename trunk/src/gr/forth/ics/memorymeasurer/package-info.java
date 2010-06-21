/**
 * An object graph memory measurer.
 * 
 * <p>This utility depends on a java agent attachment to the target JVM. Specifically, this argument needs
 * to be passed to the JVM:
 * <pre>{@code
 * -javaagent:path/to/MonitoringTools.jar
 * }</pre>
 * 
 * <p> Example:
 * <pre>{@code
 *Map map = new HashMap();
 *map.put(new Integer(5), "testValue");
 * 
 *long memoryUsed = MemoryMeasurer.count(map);
 * }</pre>
 */
package gr.forth.ics.memorymeasurer;