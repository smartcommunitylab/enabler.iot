package it.smartcommunitylab.iotengine.storage;

import it.smartcommunitylab.iotengine.model.DatasetConf;

import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

public interface DatasetConfRepository extends MongoRepository<DatasetConf, String> {
	List<DatasetConf> findAll(Sort sort);
	
	@Query(value="{domain:?0, dataset:?1}")
	DatasetConf findByDataset(String domain, String dataset);
	
	@Query(value="{domain:?0}")
	List<DatasetConf> findByDomain(String domain);
}
