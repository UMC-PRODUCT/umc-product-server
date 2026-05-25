package com.umc.product.global.observability;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.observability.tracing")
public class ObservabilityTracingProperties {

    private boolean enabled = true;
    private boolean useCaseSpans = true;
    private boolean adapterSpans = true;
    private boolean dbSpans = true;
    private boolean includeSql = false;
    private int maxSqlLength = 500;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isUseCaseSpans() {
        return useCaseSpans;
    }

    public void setUseCaseSpans(boolean useCaseSpans) {
        this.useCaseSpans = useCaseSpans;
    }

    public boolean isAdapterSpans() {
        return adapterSpans;
    }

    public void setAdapterSpans(boolean adapterSpans) {
        this.adapterSpans = adapterSpans;
    }

    public boolean isDbSpans() {
        return dbSpans;
    }

    public void setDbSpans(boolean dbSpans) {
        this.dbSpans = dbSpans;
    }

    public boolean isIncludeSql() {
        return includeSql;
    }

    public void setIncludeSql(boolean includeSql) {
        this.includeSql = includeSql;
    }

    public int getMaxSqlLength() {
        return maxSqlLength;
    }

    public void setMaxSqlLength(int maxSqlLength) {
        this.maxSqlLength = maxSqlLength;
    }
}
