package it.smartcommunitylab.iotengine.storage;

import it.smartcommunitylab.iotengine.model.DataSetConf;

import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

public interface DataSetConfRepository extends MongoRepository<DataSetConf, String> {
	List<DataSetConf> findAll(Sort sort);
	
	@Query(value="{domain:?0, dataset:?1}")
	DataSetConf findByDataset(String domain, String dataset);
	
}
