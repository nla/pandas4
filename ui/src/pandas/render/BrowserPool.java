package pandas.render;

import org.apache.commons.pool2.BasePooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultEvictionPolicy;
import org.apache.commons.pool2.impl.DefaultPooledObject;
import org.apache.commons.pool2.impl.EvictionConfig;
import org.apache.commons.pool2.impl.GenericObjectPool;

import java.io.IOException;
import java.io.UncheckedIOException;

public class BrowserPool extends GenericObjectPool<Browser> {
    long maxLifetime = 10L * 60L * 1000L; // kill browser after 10 minutes

    public BrowserPool() {
        super(new Factory());
        setTestOnBorrow(true);
        setTimeBetweenEvictionRunsMillis(30000);
        setMinEvictableIdleTimeMillis(30000);
        setEvictionPolicy(new DefaultEvictionPolicy<>() {
            @Override
            public boolean evict(EvictionConfig config, PooledObject<Browser> underTest, int idleCount) {
                if (System.currentTimeMillis() - underTest.getCreateTime() > maxLifetime) {
                    return true;
                }
                return super.evict(config, underTest, idleCount);
            }
        });
    }

    private static class Factory extends BasePooledObjectFactory<Browser> {
        @Override
        public boolean validateObject(PooledObject<Browser> p) {
            return p.getObject().alive();
        }

        @Override
        public void destroyObject(PooledObject<Browser> p) {
            p.getObject().close();
        }

        @Override
        public Browser create() {
            try {
                return new Browser();
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }

        @Override
        public PooledObject<Browser> wrap(Browser browser) {
            return new DefaultPooledObject<>(browser);
        }
    }
}
