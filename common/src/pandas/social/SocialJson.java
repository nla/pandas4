package pandas.social;

import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.DeserializationConfig;
import com.fasterxml.jackson.databind.deser.SettableBeanProperty;
import com.fasterxml.jackson.databind.deser.ValueInstantiator;
import com.fasterxml.jackson.databind.deser.ValueInstantiators;
import com.fasterxml.jackson.databind.deser.std.StdValueInstantiator;
import com.fasterxml.jackson.databind.introspect.BeanPropertyDefinition;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class SocialJson {

    public static final JsonMapper mapper = JsonMapper.builder()
            .addModule(new JavaTimeModule())
            .addModule(new RecordNamingWorkaround())
            .build();

    // https://github.com/FasterXML/jackson-databind/issues/2992
    private static class RecordNamingWorkaround extends SimpleModule {
        @Override
        public void setupModule(SetupContext context) {
            context.addValueInstantiators(new ValueInstantiators.Base() {
                public ValueInstantiator findValueInstantiator(DeserializationConfig config, BeanDescription beanDesc,
                                                               ValueInstantiator defaultInstantiator) {
                    if (!beanDesc.getBeanClass().isRecord()
                            || !(defaultInstantiator instanceof StdValueInstantiator)
                            || !defaultInstantiator.canCreateFromObjectWith()) {
                        return defaultInstantiator;
                    }
                    Map<String, BeanPropertyDefinition> map = new HashMap<>();
                    for (BeanPropertyDefinition beanPropertyDefinition : beanDesc.findProperties()) {
                        map.put(beanPropertyDefinition.getInternalName(), beanPropertyDefinition);
                    }
                    SettableBeanProperty[] renamedConstructorArgs = Arrays.stream(defaultInstantiator.getFromObjectArguments(config))
                            .map(p -> {
                                BeanPropertyDefinition prop = map.get(p.getName());
                                return prop != null ? p.withName(prop.getFullName()) : p;
                            })
                            .toArray(SettableBeanProperty[]::new);

                    return new PatchedValueInstantiator((StdValueInstantiator) defaultInstantiator, renamedConstructorArgs);
                }
            });
            super.setupModule(context);
        }

        private static class PatchedValueInstantiator extends StdValueInstantiator {
            protected PatchedValueInstantiator(StdValueInstantiator src, SettableBeanProperty[] constructorArguments) {
                super(src);
                _constructorArguments = constructorArguments;
            }
        }
    }
}
