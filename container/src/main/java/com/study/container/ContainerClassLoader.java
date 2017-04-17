package com.study.container;

import com.study.container.exception.FrameworkException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.net.URLClassLoader;

/**
 * @author cangxing
 * @date 2017-04-15 23:45
 */
public class ContainerClassLoader extends ClassLoader {

    private static final Logger logger = LoggerFactory.getLogger(ContainerClassLoader.class);

    private ClassLoader containerClassLoader;
    private BundleExportClassManager bundleExportClassManager;

    public ContainerClassLoader(URLClassLoader containerClassLoader) {
        this.containerClassLoader = containerClassLoader;
    }

    public synchronized Class loadClass(String name) throws ClassNotFoundException {
        //先尝试从bundles中暴露的类中加载，进入该加载器后的类由特定bundle的类加载器接管后续的类加载
        Class clazz = loadClassFromExportApi(name);
        if (clazz != null) {
            debugClassLoaded(clazz, "loadClassFromExportApi", name);
            return clazz;
        }
        //如果要加载的类不是中间件暴露的类，那么从容器URLClassLoader加载
        clazz = loadClassByContainerURLClassLoader(name);
        if (clazz != null) {
            debugClassLoaded(clazz, "loadClassFromContainerLib", name);
            return clazz;
        }
        throw new FrameworkException((new StringBuilder()).append("ContainerClassLoader").append(" can not load class {").append(name).append("} after all phase.").toString());
    }

    Class loadClassByContainerURLClassLoader(String name)
            throws FrameworkException {
        debugClassLoading("loadClassByContainerURLClassLoader", name);
        try {
            return containerClassLoader.loadClass(name);
        } catch (ClassNotFoundException ex) {
        } catch (Throwable t) {
            throwClassLoadError(name, "loadClassByContainerURLClassLoader", t);
        }
        return null;
    }

    Class loadClassFromExportApi(String name)
            throws FrameworkException {
        debugClassLoading("loadClassFromExportApi", name);
        return bundleExportClassManager.getClass(name);
    }

    private void throwClassLoadError(String className, String phase, Throwable t)
            throws FrameworkException {
        throw new FrameworkException(new StringBuilder("Error when ContainerClassLoader load class ").append(className).append(" at ").append(phase).append(" phase.").toString(), t);
    }

    private void debugClassLoading(String phase, String className) {
        if (logger.isDebugEnabled())
            logger.debug(new StringBuilder().append("ContainerClassLoader try load class {} at ").append(phase).toString(), new Object[]{
                    className
            });
    }

    private void debugClassLoaded(Class clazz, String phase, String className) {
        String position = "unknown";
        if (clazz.getProtectionDomain() != null && clazz.getProtectionDomain().getCodeSource() != null && clazz.getProtectionDomain().getCodeSource().getLocation() != null)
            position = clazz.getProtectionDomain().getCodeSource().getLocation().toString();
        if (logger.isDebugEnabled())
            logger.debug("ContainerClassLoader loaded class: {} @ {} at {} phase", new Object[]{
                    className, position, phase
            });
    }

    public void setBundleExportClassManager(BundleExportClassManager bundleExportClassManager) {
        this.bundleExportClassManager = bundleExportClassManager;
    }
}
