package com.study.container;

import com.study.container.config.Config;
import com.study.container.exception.FrameworkException;
import com.study.container.util.ConfigFileUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * @author cangxing
 * @date 2017-04-16 08:47
 */
public class ClassLoaderManager {
    private static final Logger logger = LoggerFactory.getLogger(ClassLoaderManager.class);

    public ClassLoaderManager(ClassLoader bizClassLoader) {
        this.bizClassLoader = bizClassLoader;
        systemClassLoader = ClassLoader.getSystemClassLoader();
        ClassLoader classLoader = String.class.getClassLoader();
        if (classLoader == null)
            for (classLoader = systemClassLoader; classLoader.getParent() != null; classLoader = classLoader.getParent())
                ;
        extClassLoader = classLoader;
        containerClassLoader = new ContainerClassLoader((URLClassLoader) getClass().getClassLoader());
        this.bundleExportClassManager = new BundleExportClassManager();
        containerClassLoader.setBundleExportClassManager(this.bundleExportClassManager);
        /*try {
            File containerDir = new File(systemClassLoader.getResource("container").toURI());
            containerClassLoader = getContainerLoader(containerDir);
            this.bundleExportClassManager = new BundleExportClassManager();
            containerClassLoader.setBundleExportClassManager(this.bundleExportClassManager);
        } catch (MalformedURLException e) {
            logger.error("init container classloader error!", e);
        } catch (URISyntaxException e) {
            logger.error("container lib error!", e);
        }*/
    }

    public void initBundles(){
        //部署每个中间件plugin，创建每个中间件的classloader,把每个中间件暴露出的类解析到SharedClassService
        File bundlesRoot = new File(bizClassLoader.getResource("container/bundles").getFile());
        File[] bundles = bundlesRoot.listFiles();
        for (File bundle : bundles) {
            if (bundle.isDirectory()) {
                deployBundle(bundle);
            }
        }
    }

    private BundleClassLoader createBundleClassLoader(String bundleName, URL[] repository, List<String> importPackageList) throws FrameworkException {
        BundleClassLoader bundleClassLoader = new BundleClassLoader(bundleName, repository);
        bundleClassLoader.setExtClassLoader(extClassLoader);
        bundleClassLoader.setImportPackages(importPackageList);
        bundleClassLoader.setContainerClassLoader(containerClassLoader);
        //sharedClassService：中间件库暴露给业务的api
        bundleClassLoader.setBundleExportClassManager(bundleExportClassManager);
        bundleClassLoader.setBizClassLoader(bizClassLoader);
        bundleClassLoader.setSystemClassLoader(systemClassLoader);
        return bundleClassLoader;
    }

    /*private ContainerClassLoader getContainerLoader(File containerDir)
            throws MalformedURLException {
        ArrayList list = new ArrayList();
        // there are pandora related jars in containerDir, which is 'deploy/container.sar'
        // deploy/container.sar/lib contains container related libs
        File lib = new File(containerDir, "lib");
        File[] containerLibs = lib.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.endsWith(".jar");
            }
        });
        for (File jarFile : containerLibs) {
            list.add(jarFile.toURI().toURL());
        }

        URL urls[] = (URL[]) list.toArray(new URL[list.size()]);
        return new ContainerClassLoader(urls);
    }*/

    private void deployBundle(File bundleFile) {
        String bundleName = bundleFile.getName();
        String bundlePath = bundleFile.getAbsolutePath();
        File jarsPath = new File(ConfigFileUtil.middlewareLib(bundlePath));
        File[] jars = jarsPath.listFiles();
        //中间件的每个依赖的jar
        URL[] urls = new URL[jars.length];
        for (int i = 0; i < jars.length; i++) {
            try {
                urls[i] = jars[i].toURI().toURL();
            } catch (MalformedURLException e) {
                throw new FrameworkException(e);
            }
        }
        //解析bundle配置文件，解析导出和引入的包
        try {
            Config config = ConfigFileUtil.toJson(ConfigFileUtil.middlewareConfigPath(bundlePath));
            //构建每个中间件单独的classloader
            BundleClassLoader classLoader = createBundleClassLoader(bundleName, urls, config.getImportPackages());
            List<String> exportJars = config.getExportJars();
            for (String exportJar : exportJars) {
                File exportJarFile = new File(ConfigFileUtil.middlewareExportJar(bundlePath, exportJar));
                try {
                    JarFile jarFile = new JarFile(exportJarFile);
                    Enumeration entries = jarFile.entries();
                    do {
                        if (!entries.hasMoreElements())
                            break;
                        JarEntry entry = (JarEntry) entries.nextElement();
                        //解析中间件的导出api包中的class
                        if (entry.getName().endsWith(".class")) {
                            //System.out.println(entry.getName());
                            String className = convertClassName(entry);
                            try {
                                Class clazz = classLoader.loadClass(className);
                                bundleExportClassManager.putIfAbsent(bundleName, clazz);
                            } catch (Throwable t) {
                                throw new FrameworkException(t);
                            }
                        }
                    } while (true);
                } catch (IOException e) {
                    logger.error("read export jars error!", e);
                }
            }
        } catch (FileNotFoundException e) {
            logger.error("config.json not found!", e);
        } catch (IOException e) {
            logger.error("read config.json error!", e);
        }
    }

    //把class文件名转换为类全限定名，如将com/zju/middleware/MiddlewareApi.class转换为com.zju.middleware.MiddlewareApi
    private String convertClassName(JarEntry entry) {
        if (entry.isDirectory())
            return null;
        String entryName = entry.getName();
        if (!entryName.endsWith(".class"))
            return null;
        if (entryName.charAt(0) == '/')
            entryName = entryName.substring(1);
        entryName = entryName.replace("/", ".");
        return entryName.substring(0, entryName.length() - 6);
    }

    public ContainerClassLoader getContainerClassLoader() {
        return containerClassLoader;
    }

    private ClassLoader systemClassLoader;
    private ClassLoader extClassLoader;
    private ContainerClassLoader containerClassLoader;
    private ClassLoader bizClassLoader;
    private BundleExportClassManager bundleExportClassManager;
}
