package pandas.browser;

import org.apache.commons.pool2.BasePooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultEvictionPolicy;
import org.apache.commons.pool2.impl.DefaultPooledObject;
import org.apache.commons.pool2.impl.EvictionConfig;
import org.apache.commons.pool2.impl.GenericObjectPool;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;

public class BrowserPool extends GenericObjectPool<Browser> {
    long maxLifetime = 10L * 60L * 1000L; // kill browser after 10 minutes

    public BrowserPool(BrowserProperties properties) {
        super(new Factory(properties));
        setTestOnBorrow(true);
        setMaxIdle(properties.limit());
        setMaxTotal(properties.limit());
        setDurationBetweenEvictionRuns(Duration.ofSeconds(30));
        setMinEvictableIdleDuration(Duration.ofSeconds(30));
        setEvictionPolicy(new DefaultEvictionPolicy<>() {
            @Override
            public boolean evict(EvictionConfig config, PooledObject<Browser> underTest, int idleCount) {
                Instant expiration = underTest.getCreateInstant().plusMillis(maxLifetime);
                if (Instant.now().isAfter(expiration)) {
                    return true;
                }
                return super.evict(config, underTest, idleCount);
            }
        });
    }

    private static class Factory extends BasePooledObjectFactory<Browser> {
        private final BrowserProperties properties;

        private Factory(BrowserProperties properties) {
            this.properties = properties;
        }

        @Override
        public boolean validateObject(PooledObject<Browser> p) {
            return p.getObject().alive();
        }

        @Override
        public void destroyObject(PooledObject<Browser> p) {
            p.getObject().close();
        }

        @Override
        public Browser create() throws IOException {
            return new Browser(properties);
        }

        @Override
        public PooledObject<Browser> wrap(Browser browser) {
            return new DefaultPooledObject<>(browser);
        }
    }
}
