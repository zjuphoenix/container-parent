package com.study.container.api;

import java.io.File;
import java.io.FilenameFilter;
import java.lang.reflect.*;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.ServiceLoader;
import java.util.concurrent.atomic.AtomicInteger;
import static com.study.container.api.lifecycle.LifeCycleStatus.*;

/**
 * @author cangxing
 * @date 2017-04-16 09:45
 */
public class ContainerServiceLoader {
    public static final ContainerServiceLoader INSTANCE = new ContainerServiceLoader();
    private AtomicInteger status = new AtomicInteger(0);
    private Object classLoaderManager;
    private URLClassLoader containerClassLoader;
    private Object containerClassLoaderWrapper;
    //private ConcurrentHashMap<Class<?>, Object> cachedInstances = new ConcurrentHashMap<>();
    private ContainerServiceLoader() {
    }

    public void start() throws URISyntaxException, MalformedURLException, ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        if (status.compareAndSet(INIT, STARTING)) {
            File containerDir = new File(ClassLoader.getSystemClassLoader().getResource("container").toURI());
            containerClassLoader = getContainerLoader(containerDir);
            //classLoaderManager = new ClassLoaderManager(getClass().getClassLoader());
            Class<?> classLoaderManagerClass = containerClassLoader.loadClass("com.study.container.ClassLoaderManager");
            Constructor constructor = classLoaderManagerClass.getDeclaredConstructor(ClassLoader.class);
            classLoaderManager = constructor.newInstance(getClass().getClassLoader());
            //classLoaderManager.initBundles();
            Method method = classLoaderManagerClass.getMethod("initBundles");
            method.invoke(classLoaderManager);
            //classLoaderManager.getContainerClassLoader()
            method = classLoaderManagerClass.getMethod("getContainerClassLoader");
            containerClassLoaderWrapper = method.invoke(classLoaderManager);
            status.set(STARTED);
        }
    }

    public <T> T load(Class<T> api) throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        if (status.get() != STARTED) {
            throw new RuntimeException("ClassLoaderManager not started!");
        }
        //Class<T> clazz = (Class<T>)classLoaderManager.getContainerClassLoader().loadClass(api.getName());
        Class<?> containerClassLoaderClass = containerClassLoader.loadClass("com.study.container.ContainerClassLoader");
        Method method = containerClassLoaderClass.getMethod("loadClass", String.class);
        Class<T> clazz = (Class<T>) method.invoke(containerClassLoaderWrapper, api.getName());
        ServiceLoader<T> serviceLoader = ServiceLoader.load(clazz, (ClassLoader)containerClassLoaderWrapper);
        final T res = serviceLoader.iterator().next();
        /*ClassLoader apiClassLoader = api.getClassLoader();
        Class impClass = res.getClass();
        ClassLoader imlClassLoader = impClass.getClassLoader();
        Class interfaceClass = impClass.getInterfaces()[0];
        ClassLoader interfaceClassLoader = interfaceClass.getClassLoader();*/
        return (T) Proxy.newProxyInstance(api.getClassLoader(), new Class<?>[]{api}, new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                return res.getClass().getMethod(method.getName(), method.getParameterTypes()).invoke(res, args);
            }
        });
    }

    private URLClassLoader getContainerLoader(File containerDir)
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
        return new URLClassLoader(urls);
    }

}
