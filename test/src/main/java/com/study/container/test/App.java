package com.study.container.test;

import com.study.bundle.first.api.BundleFirstApi;
import com.study.bundle.second.api.BundleSecondApi;
import com.study.container.api.ContainerServiceLoader;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;

/**
 * @author cangxing
 * @date 2017-04-16 19:31
 */
public class App {
    public static void main(String[] args) throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IllegalAccessException, InstantiationException, URISyntaxException, MalformedURLException {
        ContainerServiceLoader containerServiceLoader = ContainerServiceLoader.INSTANCE;
        containerServiceLoader.start();
        BundleFirstApi bundleFirstApi = containerServiceLoader.load(BundleFirstApi.class);
        BundleSecondApi bundleSecondApi = containerServiceLoader.load(BundleSecondApi.class);
        System.out.println(bundleFirstApi.createBundleFirst("1"));
        System.out.println(bundleSecondApi.createBundleSecond("2"));
    }
}
