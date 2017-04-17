package com.study.container;


import com.study.container.exception.FrameworkException;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ServiceLoader;
import java.util.concurrent.atomic.AtomicInteger;
import static com.study.container.lifecycle.LifeCycleStatus.INIT;
import static com.study.container.lifecycle.LifeCycleStatus.STARTED;
import static com.study.container.lifecycle.LifeCycleStatus.STARTING;

/**
 * @author cangxing
 * @date 2017-04-16 09:45
 */
@Deprecated
public class ContainerServiceLoaderWrapper {
    public static final ContainerServiceLoaderWrapper INSTANCE = new ContainerServiceLoaderWrapper();
    private AtomicInteger status = new AtomicInteger(0);
    private ClassLoaderManager classLoaderManager;
    //private ConcurrentHashMap<Class<?>, Object> cachedInstances = new ConcurrentHashMap<>();
    private ContainerServiceLoaderWrapper() {
    }

    public void start(){
        if (status.compareAndSet(INIT, STARTING)) {
            classLoaderManager = new ClassLoaderManager(getClass().getClassLoader());
            classLoaderManager.initBundles();
            status.set(STARTED);
        }
    }

    public <T> T load(Class<T> api) throws ClassNotFoundException {
        if (status.get() != STARTED) {
            throw new FrameworkException("ClassLoaderManager not started!");
        }
        Class<T> clazz = (Class<T>)classLoaderManager.getContainerClassLoader().loadClass(api.getName());
        ServiceLoader<T> serviceLoader = ServiceLoader.load(clazz, classLoaderManager.getContainerClassLoader());
        T res = serviceLoader.iterator().next();
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

    public ClassLoaderManager getClassLoaderManager() {
        return classLoaderManager;
    }
}
