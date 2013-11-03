package org.gamehost.jtrek.javatrek;

import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryPoolMXBean;
import java.util.List;

/**
 * User: Jay
 * Date: 11/2/13
 * Time: 2:02 PM
 */
public class TrekMemoryUsageReporter {
    public static void logMemory() {
        ManagementFactory.getMemoryMXBean().getHeapMemoryUsage();
        TrekLog.logMemory("Heap", ManagementFactory.getMemoryMXBean().getHeapMemoryUsage());
        TrekLog.logMemory("NonHeap", ManagementFactory.getMemoryMXBean().getNonHeapMemoryUsage());
    }

    public static void logMemoryBeans() {
        List<MemoryPoolMXBean> beans = ManagementFactory.getMemoryPoolMXBeans();
        for (MemoryPoolMXBean bean: beans) {
            TrekLog.logMemory(bean.getName(), bean.getUsage());
        }

        for (GarbageCollectorMXBean bean: ManagementFactory.getGarbageCollectorMXBeans()) {
            TrekLog.logMemory(bean.getName(), bean.getCollectionCount(), bean.getCollectionTime());
        }
    }
}
