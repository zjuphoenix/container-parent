package com.study.container.test;

import com.study.bundle.first.api.BundleFirstApi;
import com.study.bundle.second.api.BundleSecondApi;
import com.study.container.api.ContainerServiceLoader;
import io.netty.util.Version;
import org.springframework.core.SpringVersion;

import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;

/**
 * @author cangxing
 * @date 2017-04-16 19:31
 */
public class App {
    public static void main(String[] args) throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IllegalAccessException, InstantiationException, URISyntaxException, MalformedURLException {
        System.out.println("App:");
        System.out.print("test spring classloader:");
        System.out.println(SpringVersion.class.getClassLoader());
        System.out.println("spring version: "+SpringVersion.getVersion());
        System.out.print("test app class: io.netty.util.Version classloader:");
        System.out.println(Version.class.getClassLoader());
        String version = Version.identify(App.class.getClassLoader()).toString();
        System.out.print("app netty version:");
        System.out.println(version);
        ContainerServiceLoader containerServiceLoader = ContainerServiceLoader.INSTANCE;
        containerServiceLoader.start();
        BundleFirstApi bundleFirstApi = containerServiceLoader.load(BundleFirstApi.class);
        BundleSecondApi bundleSecondApi = containerServiceLoader.load(BundleSecondApi.class);
        System.out.println(bundleFirstApi.createBundleFirst("1"));
        System.out.println(bundleSecondApi.createBundleSecond("2"));
    }
}
