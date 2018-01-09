package it.smartcommunitylab.iotengine.storage;

import it.smartcommunitylab.iotengine.common.Utils;
import it.smartcommunitylab.iotengine.exception.EntityNotFoundException;
import it.smartcommunitylab.iotengine.exception.StorageException;
import it.smartcommunitylab.iotengine.model.DataSetConf;

import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

public class RepositoryManager {
	
	@Autowired
	private DataSetConfRepository dataSetConfRepository;
	
	public DataSetConf addDataSetConf(DataSetConf conf) throws StorageException, EntityNotFoundException {
		DataSetConf dataSetConfDb = dataSetConfRepository.findByDataset(conf.getDomain(), conf.getDataset());
		if(dataSetConfDb != null) {
			updateDataSetConf(conf);
		} else {
			Date now = new Date();
			conf.setId(Utils.getUUID());
			conf.setCreationDate(now);
			conf.setLastUpdate(now);
			dataSetConfRepository.save(conf);
		}
		return conf;
	}
	
	public DataSetConf updateDataSetConf(DataSetConf conf) throws StorageException, EntityNotFoundException {
		DataSetConf confDb = dataSetConfRepository.findByDataset(conf.getDomain(), conf.getDataset());
		if(confDb == null) {
			throw new EntityNotFoundException("entity not found");
		}
		Date now = new Date();
		confDb.setLastUpdate(now);
		confDb.setDevices(conf.getDevices());
		confDb.setUser(conf.getUser());
		confDb.setSecret(conf.getSecret());
		confDb.setUserId(conf.getUserId());
		confDb.setToken(conf.getToken());
		dataSetConfRepository.save(confDb);
		return confDb;
	}
	
	public DataSetConf removeDataSetConf(String domain, String dataset) throws StorageException, EntityNotFoundException {
		DataSetConf confDb = dataSetConfRepository.findByDataset(domain, dataset);
		if(confDb == null) {
			throw new EntityNotFoundException("entity not found");
		}
		dataSetConfRepository.delete(confDb);
		return confDb;
	}

	public DataSetConf getDataSetConf(String domain, String dataset) {
		DataSetConf confDb = dataSetConfRepository.findByDataset(domain, dataset);
		return confDb;
	}

	public List<DataSetConf> getAllDataSetConf() {
		return dataSetConfRepository.findAll();
	}

}
