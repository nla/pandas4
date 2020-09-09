package pandas.admin.render;

import org.apache.commons.pool2.BasePooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;
import org.apache.commons.pool2.impl.GenericObjectPool;

import java.io.IOException;
import java.io.UncheckedIOException;

public class BrowserPool extends GenericObjectPool<Browser> {
    public BrowserPool() {
        super(new Factory());
        setTestOnBorrow(true);
        setMinEvictableIdleTimeMillis(30000);
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
