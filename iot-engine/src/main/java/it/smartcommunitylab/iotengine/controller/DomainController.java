package it.smartcommunitylab.iotengine.controller;

import it.smartcommunitylab.iotengine.common.Utils;
import it.smartcommunitylab.iotengine.raptor.RaptorManger;
import it.smartcommunitylab.iotengine.exception.EntityNotFoundException;
import it.smartcommunitylab.iotengine.exception.StorageException;
import it.smartcommunitylab.iotengine.exception.UnauthorizedException;
import it.smartcommunitylab.iotengine.model.DataSetConf;
import it.smartcommunitylab.iotengine.storage.RepositoryManager;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.RandomStringUtils;
import org.createnet.raptor.models.auth.User;
import org.createnet.raptor.models.objects.Device;
import org.createnet.raptor.models.query.DeviceQuery;
import org.createnet.raptor.sdk.Raptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

@Controller
public class DomainController extends AuthController {
	private static final transient Logger logger = LoggerFactory.getLogger(DomainController.class);
	
	@Autowired
	private RepositoryManager dataManager;
	
	@Autowired
	private RaptorManger raptorManager;

	@RequestMapping(value = "/api/domain/{domain}/{dataset}/token", method = RequestMethod.GET)
	public @ResponseBody String getToken (
			@PathVariable String domain,
			@PathVariable String dataset,
			HttpServletRequest request) throws Exception {
		if(!checkRole("DSA_" + domain.toUpperCase(), request)) {
			throw new UnauthorizedException("Unauthorized Exception: role not valid");
		}
		DataSetConf conf = dataManager.getDataSetConf(domain, dataset);
		if(conf == null) {
			conf = addDataSetConf(domain, dataset);
		}
		String result = conf.getToken();
		if(logger.isInfoEnabled()) {
			logger.info(String.format("getToken: %s", result));
		}
		return result;
	}
	
	@RequestMapping(value = "/api/domain/{domain}/{dataset}/conf", method = RequestMethod.POST)
	public @ResponseBody DataSetConf addDataSetConf (
			@PathVariable String domain,
			@PathVariable String dataset,
			HttpServletRequest request) throws Exception {
		if(!checkRole("DSA_" + domain.toUpperCase(), request)) {
			throw new UnauthorizedException("Unauthorized Exception: role not valid");
		}
		DataSetConf result = addDataSetConf(domain, dataset);
		if(logger.isInfoEnabled()) {
			logger.info(String.format("addDataSetConf: %s", result.toString()));
		}
		return result;
	}

	private DataSetConf addDataSetConf(String domain, String dataset) throws StorageException,
			EntityNotFoundException {
		DataSetConf conf = new DataSetConf();
		conf.setDomain(domain);
		conf.setDataset(dataset);
		String user = domain + "-" + dataset;
		String secret = RandomStringUtils.randomAlphanumeric(12);
		conf.setUser(user);
		conf.setSecret(secret);
		DataSetConf result = dataManager.addDataSetConf(conf);
		User userRaptor = raptorManager.addUser(user, secret);
		conf.setUserId(userRaptor.getUuid());
		Raptor raptor = raptorManager.getRaptorByUser(user, secret);
		String raptorToken = raptorManager.getRaptorToken(raptor, user, secret);
		conf.setToken(raptorToken);
		dataManager.updateDataSetConf(conf);
		return result;
	}
	
	@RequestMapping(value = "/api/domain/{domain}/{dataset}/conf", method = RequestMethod.DELETE)
	public @ResponseBody DataSetConf deleteDataSetConf (
			@PathVariable String domain,
			@PathVariable String dataset,
			HttpServletRequest request) throws Exception {
		if(!checkRole("DSA_" + domain.toUpperCase(), request)) {
			throw new UnauthorizedException("Unauthorized Exception: role not valid");
		}
		DataSetConf result = dataManager.removeDataSetConf(domain, dataset);
		String user = result.getUser();
		String secret = result.getSecret();
		Raptor raptor = raptorManager.getRaptorByUser(user, secret);
		raptorManager.deleteDevices(raptor);
		raptorManager.deleteTokens(raptor);
		raptorManager.deleteUser(raptor, result.getUserId());
		if(logger.isInfoEnabled()) {
			logger.info(String.format("deleteDataSetConf: %s", result.toString()));
		}
		return result;
	}

	@RequestMapping(value = "/api/domain/{domain}/{dataset}/device", method = RequestMethod.POST)
	public @ResponseBody Device addDevice(
			@PathVariable String domain,
			@PathVariable String dataset,
			@RequestBody Device device,
			HttpServletRequest request) throws Exception {
		if(!checkRole("DSA_" + domain.toUpperCase(), request)) {
			throw new UnauthorizedException("Unauthorized Exception: role not valid");
		}
		DataSetConf conf = dataManager.getDataSetConf(domain, dataset);
		String user = conf.getUser();
		String secret = conf.getSecret();
		Raptor raptor = raptorManager.getRaptorByUser(user, secret);
		Device result = raptor.Inventory().create(device);
		conf.getDevices().add(result.getId());
		dataManager.updateDataSetConf(conf);
		if(logger.isInfoEnabled()) {
			logger.info(String.format("addDevice: %s - %s - %s", domain, dataset, result.getId()));
		}
		return result;
	}
	
	@RequestMapping(value = "/api/domain/{domain}/{dataset}/device/{deviceId}", method = RequestMethod.DELETE)
	public @ResponseBody Device deleteDevice(
			@PathVariable String domain,
			@PathVariable String dataset,
			@PathVariable String deviceId,
			HttpServletRequest request) throws Exception {
		if(!checkRole("DSA_" + domain.toUpperCase(), request)) {
			throw new UnauthorizedException("Unauthorized Exception: role not valid");
		}
		DataSetConf conf = dataManager.getDataSetConf(domain, dataset);
		String user = conf.getUser();
		String secret = conf.getSecret();
		Raptor raptor = raptorManager.getRaptorByUser(user, secret);
		DeviceQuery query = new DeviceQuery();
		query.deviceId(deviceId);
		List<Device> list = raptor.Inventory().search(query);
		Device result = null;
		if(list.size() > 0) {
			result = list.get(0);
			raptor.Inventory().delete(result);
		} else {
			throw new EntityNotFoundException("entity not found");
		}
		conf.getDevices().remove(deviceId);
		dataManager.updateDataSetConf(conf);
		if(logger.isInfoEnabled()) {
			logger.info(String.format("deleteDevice: %s - %s - %s", domain, dataset, result.getId()));
		}
		return result;
	}
	
	@ExceptionHandler({EntityNotFoundException.class, StorageException.class})
	@ResponseStatus(value=HttpStatus.BAD_REQUEST)
	@ResponseBody
	public Map<String,String> handleEntityNotFoundError(HttpServletRequest request, Exception exception) {
		logger.error(exception.getMessage());
		return Utils.handleError(exception);
	}
	
	@ExceptionHandler(UnauthorizedException.class)
	@ResponseStatus(value=HttpStatus.FORBIDDEN)
	@ResponseBody
	public Map<String,String> handleUnauthorizedError(HttpServletRequest request, Exception exception) {
		logger.error(exception.getMessage());
		return Utils.handleError(exception);
	}
	
	@ExceptionHandler(Exception.class)
	@ResponseStatus(value=HttpStatus.INTERNAL_SERVER_ERROR)
	@ResponseBody
	public Map<String,String> handleGenericError(HttpServletRequest request, Exception exception) {
		logger.error(exception.getMessage());
		return Utils.handleError(exception);
	}		
}
