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
        System.out.println("test import: spring classloader:");
        System.out.println(SpringVersion.class.getClassLoader());
        System.out.println("spring version: "+SpringVersion.getVersion());

        System.out.println("test export: BundleSecondImpl classloader:");
        System.out.println(getClass().getClassLoader());

        System.out.println("test bundle class: io.netty.util.Version classloader:");
        System.out.println(Version.class.getClassLoader());
        String version = Version.identify(BundleSecondImpl.class.getClassLoader()).toString();
        System.out.println(version);
        return params;
    }
}
