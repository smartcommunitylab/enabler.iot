package it.smartcommunitylab.iotengine.storage;

import it.smartcommunitylab.iotengine.common.Utils;
import it.smartcommunitylab.iotengine.exception.EntityNotFoundException;
import it.smartcommunitylab.iotengine.exception.StorageException;
import it.smartcommunitylab.iotengine.model.DataSetConf;
import it.smartcommunitylab.iotengine.model.DomainConf;

import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

public class RepositoryManager {
	
	@Autowired
	private DataSetConfRepository datasetConfRepository;
	
	@Autowired
	private DomainConfRepository domainConfRepository;
	
	
	public DomainConf getDomainConf(String domain) {
		DomainConf conf = domainConfRepository.findByDomain(domain);
		return conf;
	}
	
	public DomainConf addDomainConf(DomainConf conf) throws StorageException, EntityNotFoundException {
		DomainConf domainConfDb = domainConfRepository.findByDomain(conf.getDomain());
		if(domainConfDb != null) {
			updateDomainConf(conf);
		} else {
			Date now = new Date();
			conf.setId(Utils.getUUID());
			conf.setCreationDate(now);
			conf.setLastUpdate(now);
			domainConfRepository.save(conf);
		}
		return conf;
	}
	
	public DomainConf updateDomainConf(DomainConf conf) throws StorageException, EntityNotFoundException {
		DomainConf confDb = domainConfRepository.findByDomain(conf.getDomain());
		if(confDb == null) {
			throw new EntityNotFoundException("entity not found");
		}
		Date now = new Date();
		confDb.setLastUpdate(now);
		confDb.setUser(conf.getUser());
		confDb.setSecret(conf.getSecret());
		confDb.setUserId(conf.getUserId());
		confDb.setApplicationId(conf.getApplicationId());
		confDb.setToken(conf.getToken());
		domainConfRepository.save(confDb);
		return confDb;
	}
	
	public DomainConf removeDomainConf(String domain) throws StorageException, EntityNotFoundException {
		DomainConf confDb = domainConfRepository.findByDomain(domain);
		if(confDb == null) {
			throw new EntityNotFoundException("entity not found");
		}
		List<DataSetConf> list = datasetConfRepository.findByDomain(domain);
		datasetConfRepository.delete(list);
		domainConfRepository.delete(confDb);
		return confDb;
	}
	
	public DataSetConf addDatasetConf(DataSetConf conf) throws StorageException, EntityNotFoundException {
		DataSetConf datasetConfDb = datasetConfRepository.findByDataset(conf.getDomain(), conf.getDataset());
		if(datasetConfDb != null) {
			updateDatasetConf(conf);
		} else {
			Date now = new Date();
			conf.setId(Utils.getUUID());
			conf.setCreationDate(now);
			conf.setLastUpdate(now);
			datasetConfRepository.save(conf);
		}
		return conf;
	}
	
	public DataSetConf updateDatasetConf(DataSetConf conf) throws StorageException, EntityNotFoundException {
		DataSetConf confDb = datasetConfRepository.findByDataset(conf.getDomain(), conf.getDataset());
		if(confDb == null) {
			throw new EntityNotFoundException("entity not found");
		}
		Date now = new Date();
		confDb.setLastUpdate(now);
		confDb.setApplicationId(conf.getApplicationId());
		confDb.setToken(conf.getToken());
		datasetConfRepository.save(confDb);
		return confDb;
	}
	
	public DataSetConf removeDatasetConf(String domain, String dataset) throws StorageException, EntityNotFoundException {
		DataSetConf confDb = datasetConfRepository.findByDataset(domain, dataset);
		if(confDb == null) {
			throw new EntityNotFoundException("entity not found");
		}
		datasetConfRepository.delete(confDb);
		return confDb;
	}

	public DataSetConf getDatasetConf(String domain, String dataset) {
		DataSetConf confDb = datasetConfRepository.findByDataset(domain, dataset);
		return confDb;
	}
	
	public List<DataSetConf> getAllDatasetConf() {
		return datasetConfRepository.findAll();
	}
	
	public List<DataSetConf> getDatasetConfByDomain(String domain) {
		return datasetConfRepository.findByDomain(domain);
	}

}
