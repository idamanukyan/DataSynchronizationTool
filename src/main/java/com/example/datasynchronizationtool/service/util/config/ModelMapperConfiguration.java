package com.example.datasynchronizationtool.service.util.config;

import org.hibernate.collection.spi.PersistentCollection;
import org.modelmapper.Condition;
import org.modelmapper.ModelMapper;
import org.modelmapper.spi.MappingContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ModelMapperConfiguration {


    @Bean
    public ModelMapper getModelMapper() {
        ModelMapper modelMapper = new ModelMapper();
   /* modelMapper.getConfiguration()
      .setCollectionsMergeEnabled(true)
      .setFieldAccessLevel(org.modelmapper.config.Configuration.AccessLevel.PRIVATE);
    return modelMapper;*/

        modelMapper.getConfiguration().setPropertyCondition(new Condition<Object, Object>() {
            public boolean applies(MappingContext<Object, Object> context) {
                //if the object is a PersistentCollection could be not initialized
                //in case of lazy strategy, in this case the object will not be mapped:
                return (!(context.getSource() instanceof PersistentCollection)
                        || ((PersistentCollection) context.getSource()).wasInitialized());
            }
        });
        return modelMapper;
    }

}


