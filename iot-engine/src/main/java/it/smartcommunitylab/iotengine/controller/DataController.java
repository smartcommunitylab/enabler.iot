package it.smartcommunitylab.iotengine.controller;

import it.smartcommunitylab.iotengine.common.Utils;
import it.smartcommunitylab.iotengine.raptor.RaptorManger;
import it.smartcommunitylab.iotengine.exception.EntityNotFoundException;
import it.smartcommunitylab.iotengine.exception.StorageException;
import it.smartcommunitylab.iotengine.exception.UnauthorizedException;
import it.smartcommunitylab.iotengine.model.DataSetConf;
import it.smartcommunitylab.iotengine.storage.RepositoryManager;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

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
public class DataController extends AuthController {
	private static final transient Logger logger = LoggerFactory.getLogger(DataController.class);
	
	@Autowired
	private RepositoryManager dataManager;
	
	@Autowired
	private RaptorManger raptorManager;

	@RequestMapping(value = "/api/data/{domain}/{dataset}/token", method = RequestMethod.GET)
	public @ResponseBody String getToken(
			@PathVariable String domain,
			@PathVariable String dataset,
			HttpServletRequest request) throws Exception {
		if(!checkRole("iot_" + domain.toLowerCase(), request)) {
			throw new UnauthorizedException("Unauthorized Exception: role not valid");
		}
		DataSetConf conf = dataManager.getDataSetConf(domain, dataset);
		if(logger.isInfoEnabled()) {
			logger.info(String.format("getToken: %s %s", domain, dataset));
		}
		return conf.getToken();
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
