package com.study.container;

import com.study.container.exception.FrameworkException;
import com.study.container.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Enumeration;
import java.util.List;

/**
 * @author cangxing
 * @date 2017-04-15 23:38
 */
public class BundleClassLoader extends URLClassLoader {
    private static final Logger logger = LoggerFactory.getLogger(BundleClassLoader.class);

    public BundleClassLoader(String bundleName, URL urls[]) {
        super(urls, null);
        this.bundleName = bundleName;
    }

    /**
        container:
            lib:
            bundles:
                bundle1:
                    lib:
                    config.json
                bundle2:
                    lib:
                    config.json
     */

    protected synchronized Class loadClass(String name, boolean resolve)
            throws ClassNotFoundException {
        if (StringUtils.isEmpty(name))
            throw new FrameworkException("class name is blank.");
        //如果该类加载过，那么直接返回
        Class clazz = loadClassFromCache(name);
        if (clazz != null) {
            debugClassLoaded(clazz, "loadClassFromCache", name);
            return clazz;
        }
        //扩展类和根加载器加载,$JAVA_HOME/jre/lib/ext/*,$JAVA_HOME/jre/lib/rt.jar
        clazz = loadClassFromJDK(name);
        if (clazz != null) {
            debugClassLoaded(clazz, "loadClassFromJDK", name);
            return clazz;
        }
        //加载容器类,${classpath}/container/container/lib
        clazz = loadClassFromContainerLib(name);
        if (clazz != null) {
            debugClassLoaded(clazz, "loadClassFromContainerLib", name);
            return clazz;
        }
        //中间件暴露出的api类,定义在${classpath}/container/bundles/${bundle}/config.json中
        clazz = loadClassFromExportApi(name);
        if (clazz != null) {
            debugClassLoaded(clazz, "loadClassFromExportApi", name);
            return clazz;
        }
        //即便是中间件，可能也会依赖spring、servlet这些类库，这些类库定义在${classpath}/container/bundles/${bundle}/config.json中，然后由系统类加载器加载
        clazz = loadClassFromImport(name);
        if (clazz != null) {
            debugClassLoaded(clazz, "loadClassFromImport", name);
            return clazz;
        }
        //从bundle的lib中加载,${classpath}/container/bundles/${bundle}/lib中的jar
        clazz = loadClassFromBundleLib(name);
        if (clazz != null) {
            debugClassLoaded(clazz, "loadClassFromBundleLib", name);
            return clazz;
        }
        //从业务类加载器加载
        clazz = loadClassFromBiz(name);
        if (clazz != null) {
            debugClassLoaded(clazz, "loadClassFromBiz", name);
            return clazz;
        }

        //系统类加载器加载
        clazz = loadClassFromSystemClassLoader(name);
        if (clazz != null) {
            debugClassLoaded(clazz, "loadClassFromSystemClassLoader", name);
            if (resolve) {
                if (logger.isDebugEnabled())
                    logger.debug("Bundle-Loader {} resolve class: {}", new Object[]{
                            bundleName, name
                    });
                resolveClass(clazz);
            }
            return clazz;
        }
        throw new FrameworkException((new StringBuilder()).append("[Bundle-Loader] ").append(bundleName).append(": can not load class {").append(name).append("} after all phase.").toString());
    }

    public URL getResource(String name) {
        URL url = super.getResource(name);
        if (url == null) {
            if (systemClassLoader != null) {
                url = systemClassLoader.getResource(name);
                if (url != null)
                    return url;
            }
        } else {
            return url;
        }
        return null;
    }

    public Enumeration getResources(String name)
            throws IOException {
        Enumeration urls = super.getResources(name);
        if (urls != null && urls.hasMoreElements())
            return urls;
        if (systemClassLoader != null) {
            urls = systemClassLoader.getResources(name);
            if (urls != null && urls.hasMoreElements())
                return urls;
        }
        return urls;
    }

    public String toString() {
        return (new StringBuilder()).append(bundleName).append("'s BundleClassLoader").toString();
    }

    Class loadClassFromImport(String name)
            throws FrameworkException {
        if (importPackages != null && systemClassLoader != null) {
            debugClassLoading("loadClassFromImport", name);
            for (String packageName : importPackages) {
                if (StringUtils.isNotEmpty(packageName) && name.startsWith(packageName))
                    try {
                        return systemClassLoader.loadClass(name);
                    } catch (ClassNotFoundException ex) {
                    } catch (Throwable t) {
                        throwClassLoadError(name, "loadClassFromImport", t);
                    }
            }
        }
        return null;
    }

    Class loadClassFromExportApi(String name)
            throws FrameworkException {
        debugClassLoading("loadClassFromExportApi", name);
        return bundleExportClassManager.getClass(name);
    }

    Class loadClassFromBundleLib(String name)
            throws FrameworkException {
        debugClassLoading("loadClassFromBundleLib", name);
        try {
            //通过调用findClass避免双亲委派规则，loadClass会走双亲委派
            return findClass(name);
        } catch (ClassNotFoundException ex) {
        } catch (Throwable t) {
            throwClassLoadError(name, "loadClassFromBundleLib", t);
        }
        return null;
    }

    Class loadClassFromBiz(String name)
            throws FrameworkException {
        if (bizClassLoader != null) {
            debugClassLoading("loadClassFromBiz", name);
            try {
                return bizClassLoader.loadClass(name);
            } catch (ClassNotFoundException e) {
            } catch (Throwable t) {
                throwClassLoadError(name, "loadClassFromBiz", t);
            }
        }
        return null;
    }

    Class loadClassFromJDK(String name)
            throws FrameworkException {
        if (extClassLoader != null) {
            debugClassLoading("loadClassFromJDK", name);
            try {
                return extClassLoader.loadClass(name);
            } catch (ClassNotFoundException e) {
            } catch (Throwable t) {
                throwClassLoadError(name, "loadClassFromJDK", t);
            }
        }
        return null;
    }

    //加载container的类
    Class loadClassFromContainerLib(String name)
            throws FrameworkException {
        if (containerClassLoader != null && name.startsWith("com.study.container")) {
            debugClassLoading("resolveContainerClass", name);
            try {
                return containerClassLoader.loadClass(name);
            } catch (ClassNotFoundException e) {
            } catch (Throwable t) {
                throwClassLoadError(name, "resolveContainerClass", t);
            }
        }
        return null;
    }

    Class loadClassFromSystemClassLoader(String name)
            throws FrameworkException {
        if (systemClassLoader != null) {
            debugClassLoading("loadClassFromSystemClassLoader", name);
            try {
                return systemClassLoader.loadClass(name);
            } catch (ClassNotFoundException e) {
            } catch (Throwable t) {
                throwClassLoadError(name, "loadClassFromSystemClassLoader", t);
            }
        }
        return null;
    }

    Class loadClassFromCache(String name)
            throws FrameworkException {
        debugClassLoading("loadClassFromCache", name);
        try {
            return findLoadedClass(name);
        } catch (Throwable t) {
            throwClassLoadError(name, "loadClassFromCache", t);
        }
        return null;
    }

    private void throwClassLoadError(String className, String phase, Throwable t)
            throws FrameworkException {
        throw new FrameworkException((new StringBuilder()).append("Error when BundleClassLoader[").append(bundleName).append("] load class ").append(className).append(" at ").append(phase).append(" phase.").toString(), t);
    }

    private void debugClassLoading(String phase, String className) {
        if (logger.isDebugEnabled())
            logger.debug(new StringBuilder("BundleClassLoader {} try load {} at ").append(phase).toString(), new Object[]{
                    bundleName, className
            });
    }

    private void debugClassLoaded(Class clazz, String phase, String className) {
        String position = "unknown";
        if (clazz.getProtectionDomain() != null && clazz.getProtectionDomain().getCodeSource() != null && clazz.getProtectionDomain().getCodeSource().getLocation() != null)
            position = clazz.getProtectionDomain().getCodeSource().getLocation().toString();
        if (logger.isDebugEnabled())
            logger.debug("BundleClassLoader {} loaded class: {} @ {} at {} phase", new Object[]{
                    bundleName, className, position, phase
            });
    }

    public String getBundleName() {
        return bundleName;
    }

    public void setImportPackages(List<String> importPackages) {
        this.importPackages = importPackages;
    }

    public void setExtClassLoader(ClassLoader extClassLoader) {
        this.extClassLoader = extClassLoader;
    }

    public void setContainerClassLoader(ContainerClassLoader containerClassLoader) {
        this.containerClassLoader = containerClassLoader;
    }

    public void setBizClassLoader(ClassLoader bizClassLoader) {
        this.bizClassLoader = bizClassLoader;
    }

    public void setSystemClassLoader(ClassLoader systemClassLoader) {
        this.systemClassLoader = systemClassLoader;
    }

    public void setBundleExportClassManager(BundleExportClassManager bundleExportClassManager) {
        this.bundleExportClassManager = bundleExportClassManager;
    }

    private String bundleName;
    private List<String> importPackages;
    private ClassLoader extClassLoader;
    private ContainerClassLoader containerClassLoader;
    private ClassLoader systemClassLoader;
    private ClassLoader bizClassLoader;
    private BundleExportClassManager bundleExportClassManager;
}
