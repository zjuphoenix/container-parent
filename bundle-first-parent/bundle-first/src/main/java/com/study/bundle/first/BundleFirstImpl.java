package com.study.bundle.first;

import com.study.bundle.first.api.BundleFirstApi;
import io.netty.util.Version;
import org.springframework.core.SpringVersion;

/**
 * @author cangxing
 * @date 2017-04-16 17:46
 */
public class BundleFirstImpl implements BundleFirstApi {
    public String createBundleFirst(String params) {
        System.out.println("BundleFirst:");
        System.out.println(SpringVersion.getVersion());
        System.out.println(Version.class.getClassLoader());
        String version = Version.identify(BundleFirstImpl.class.getClassLoader()).toString();
        System.out.println(version);
        return params;
    }
}
