package com.okbug.platform.config;

import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.BeanPropertyWriter;
import com.fasterxml.jackson.databind.ser.BeanSerializerModifier;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Only serialize Long/long fields named 'id' as strings to avoid JS precision loss,
 * without affecting other numeric fields like ApiResult.code.
 */
@Configuration
public class JacksonIdSerializerConfig {

    @Bean
    public Module idLongFieldToStringModule() {
        SimpleModule module = new SimpleModule();
        module.setSerializerModifier(new BeanSerializerModifier() {
            @Override
            public List<BeanPropertyWriter> changeProperties(SerializationConfig config,
                                                             BeanDescription beanDesc,
                                                             List<BeanPropertyWriter> beanProperties) {
                for (BeanPropertyWriter writer : beanProperties) {
                    if (isIdLongProperty(writer)) {
                        writer.assignSerializer(ToStringSerializer.instance);
                    }
                }
                return beanProperties;
            }

            private boolean isIdLongProperty(BeanPropertyWriter writer) {
                String name = writer.getName();
                Class<?> rawType = writer.getType().getRawClass();
                if (rawType != Long.class && rawType != long.class) {
                    return false;
                }
                // Serialize any Long field named exactly 'id' or ending with 'Id' (e.g. teamId, userId) as String
                return "id".equals(name) || (name != null && name.endsWith("Id"));
            }
        });
        return module;
    }
}


