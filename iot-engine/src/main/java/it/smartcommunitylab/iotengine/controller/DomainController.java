package it.smartcommunitylab.iotengine.controller;

import it.smartcommunitylab.iotengine.common.Utils;
import it.smartcommunitylab.iotengine.exception.EntityNotFoundException;
import it.smartcommunitylab.iotengine.exception.StorageException;
import it.smartcommunitylab.iotengine.exception.UnauthorizedException;
import it.smartcommunitylab.iotengine.model.DomainConf;
import it.smartcommunitylab.iotengine.raptor.RaptorManger;
import it.smartcommunitylab.iotengine.storage.RepositoryManager;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.RandomStringUtils;
import org.createnet.raptor.models.auth.User;
import org.createnet.raptor.sdk.Raptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
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

	@RequestMapping(value = "/api/domain/{domain}/conf", method = RequestMethod.GET)
	public @ResponseBody DomainConf getDomainConf (
			@PathVariable String domain,
			HttpServletRequest request) throws Exception {
		if(!checkRole("DSA_" + domain.toUpperCase(), request)) {
			throw new UnauthorizedException("Unauthorized Exception: role not valid");
		}
		domain = domain.toUpperCase();
		DomainConf result = getDomainConf(domain);
		if(logger.isInfoEnabled()) {
			logger.info(String.format("addDomainConf: %s", result.toString()));
		}
		return result;
	}
	
	@RequestMapping(value = "/api/domain/{domain}/conf", method = RequestMethod.DELETE)
	public @ResponseBody DomainConf deleteDomainConf (
			@PathVariable String domain,
			HttpServletRequest request) throws Exception {
		if(!checkRole("DSA_" + domain.toUpperCase(), request)) {
			throw new UnauthorizedException("Unauthorized Exception: role not valid");
		}
		domain = domain.toUpperCase();
		deleteDomainConf(domain);
		if(logger.isInfoEnabled()) {
			logger.info(String.format("deleteDomainConf: %s", domain));
		}
		return null;
	}
	
	private void deleteDomainConf(String domain) throws EntityNotFoundException, StorageException {
		DomainConf domainConf = dataManager.getDomainConf(domain);
		if(domainConf == null) {
			throw new EntityNotFoundException("domain not found");
		}
		raptorManager.deleteUser(domainConf.getUserId());
		dataManager.removeDomainConf(domain);
	}

	private DomainConf getDomainConf(String domain) throws StorageException, EntityNotFoundException {
		DomainConf domainConf = dataManager.getDomainConf(domain);
		if(domainConf == null) {
			String secret = RandomStringUtils.randomAlphanumeric(12);
			
			User userRaptor = raptorManager.addDomainUser(domain, secret);
			//App application = raptorManager.addUserToApplication(domain, userRaptor);
			Raptor raptor = raptorManager.getRaptorByUser(userRaptor.getUsername(), secret);
			String raptorToken = raptorManager.getRaptorToken(raptor, userRaptor.getUsername(), secret);
			
			DomainConf conf = new DomainConf();
			conf.setDomain(domain);
			conf.setUserId(userRaptor.getId());
			conf.setUser(userRaptor.getUsername());
			conf.setSecret(secret);
			//conf.setApplicationId(application.getId());
			conf.setToken(raptorToken);
			DomainConf result = dataManager.addDomainConf(conf);
			return result;
		} else {
			return domainConf;
		}
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
