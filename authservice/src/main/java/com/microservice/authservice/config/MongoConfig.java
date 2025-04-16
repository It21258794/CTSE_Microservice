package com.microservice.authservice.config;

import com.microservice.authservice.model.User;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.IndexOperations;
import org.springframework.data.mongodb.core.index.IndexResolver;
import org.springframework.data.mongodb.core.index.MongoPersistentEntityIndexResolver;
import org.springframework.data.mongodb.core.mapping.MongoMappingContext;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import java.util.List;

@Configuration
public class MongoConfig {

    @Bean
    public boolean ensureIndexes(MongoTemplate mongoTemplate) {
        mongoTemplate.indexOps(User.class).dropAllIndexes();

        List<User> users = mongoTemplate.findAll(User.class);
        users.forEach(user -> {
            Query query = new Query(Criteria.where("email").is(user.getEmail()));
            List<User> duplicates = mongoTemplate.find(query, User.class);
            if (duplicates.size() > 1) {
                for (int i = 1; i < duplicates.size(); i++) {
                    mongoTemplate.remove(duplicates.get(i));
                }
            }
        });

        MongoMappingContext mappingContext = (MongoMappingContext) mongoTemplate.getConverter().getMappingContext();
        IndexResolver resolver = new MongoPersistentEntityIndexResolver(mappingContext);
        IndexOperations indexOps = mongoTemplate.indexOps(User.class);
        resolver.resolveIndexFor(User.class).forEach(indexOps::ensureIndex);
        
        return true;
    }
} 