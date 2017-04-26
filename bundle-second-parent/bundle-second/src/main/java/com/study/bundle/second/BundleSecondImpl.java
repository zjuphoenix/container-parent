package com.study.bundle.second;

import com.study.bundle.second.api.BundleSecondApi;
import io.netty.util.Version;
import org.springframework.core.SpringVersion;

/**
 * @author cangxing
 * @date 2017-04-16 17:54
 */
public class BundleSecondImpl implements BundleSecondApi {
    public String createBundleSecond(String params) {
        System.out.println("BundleSecond:");
        System.out.print("test import: spring classloader:");
        System.out.println(SpringVersion.class.getClassLoader());
        System.out.println("spring version: "+SpringVersion.getVersion());
        System.out.print("test import: BundleSecondApi classloader:");
        System.out.println(BundleSecondApi.class.getClassLoader());

        System.out.print("test export: BundleSecondImpl classloader:");
        System.out.println(getClass().getClassLoader());

        System.out.print("test bundle class: io.netty.util.Version classloader:");
        System.out.println(Version.class.getClassLoader());
        String version = Version.identify(BundleSecondImpl.class.getClassLoader()).toString();
        System.out.print("bundle-second netty version:");
        System.out.println(version);
        return params;
    }
}
