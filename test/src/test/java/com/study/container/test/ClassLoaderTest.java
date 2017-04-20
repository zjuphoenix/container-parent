package com.study.container.test;


import org.junit.Test;

import java.io.File;
import java.io.FilenameFilter;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;

/**
 * @author cangxing
 * @date 2017-04-17 14:12
 */
public class ClassLoaderTest {
    @Test
    public void test() throws MalformedURLException, ClassNotFoundException {
        //Class.forName("com.study.container.test.App");
        File dir = new File(getClass().getClassLoader().getResource("container").getFile());
        URLClassLoader classLoader = getContainerLoader(dir);
        Class<?> clazz = classLoader.loadClass("java.util.ArrayList");
        System.out.println(clazz);
        clazz = ArrayList.class;
        System.out.println(clazz);
        clazz = classLoader.loadClass("com.study.container.util.StringUtils");
        System.out.println(clazz);
        //clazz = classLoader.loadClass("com.study.container.test.App");
        //System.out.println(clazz);
        clazz = classLoader.loadClass("io.netty.util.Version");
        System.out.println(clazz);
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
