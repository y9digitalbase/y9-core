package net.risesoft.log.repository;

import java.util.List;

import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import net.risesoft.log.entity.Y9logMapping;

public interface Y9logMappingRepository extends ElasticsearchRepository<Y9logMapping, String> {

    public List<Y9logMapping> findByModularName(String modularName);
}
