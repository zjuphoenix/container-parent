package com.study.container;

import java.util.concurrent.ConcurrentHashMap;

/**
 * @author cangxing
 * @date 2017-04-16 00:55
 */
public class BundleExportClassManager {
    private final ConcurrentHashMap<String, Class> cachedClasses;

    public BundleExportClassManager() {
        this.cachedClasses = new ConcurrentHashMap<>();
    }

    public Class getClass(String fullClassName) {
        Class result = null;
        if (fullClassName != null)
            result = cachedClasses.get(fullClassName);
        return result;
    }

    public Class putIfAbsent(String moduleName, Class clazz) {
        if (moduleName == null || clazz == null)
            return null;
        Class oldClazz = cachedClasses.putIfAbsent(clazz.getName(), clazz);
        return oldClazz;
    }
}
