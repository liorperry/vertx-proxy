package io.vertx.example.web.proxy.locator;

import java.util.Collections;
import java.util.Set;

public class ServiceVersion {
    private String name;
    private String version;
    private Set<String> supportedVersion;

    public ServiceVersion(String name,String version, Set<String> supportedVersion) {
        this.name = name;
        this.version = version;
        this.supportedVersion = supportedVersion;
    }

    public ServiceVersion(String name, String version) {
        this(name,version,Collections.singleton(version));
    }

    public String getName() {
        return name;
    }

    public String getVersion() {
        return version;
    }

    public Set<String> getSupportedVersion() {
        return Collections.unmodifiableSet(supportedVersion);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ServiceVersion that = (ServiceVersion) o;

        if (!name.equals(that.name)) return false;
        return version.equals(that.version);

    }

    @Override
    public int hashCode() {
        int result = name.hashCode();
        result = 31 * result + version.hashCode();
        return result;
    }
}
