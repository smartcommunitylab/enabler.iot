package it.smartcommunitylab.iotengine.storage;

import it.smartcommunitylab.iotengine.model.DomainConf;

import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

public interface DomainConfRepository extends MongoRepository<DomainConf, String> {
	List<DomainConf> findAll(Sort sort);
	
	@Query(value="{domain:?0}")
	DomainConf findByDomain(String domain);
	
}
