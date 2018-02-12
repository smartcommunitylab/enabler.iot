package it.smartcommunitylab.iotengine.raptor;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;

import org.createnet.raptor.models.app.App;
import org.createnet.raptor.models.app.AppRole;
import org.createnet.raptor.models.auth.Token;
import org.createnet.raptor.models.auth.User;
import org.createnet.raptor.sdk.PageResponse;
import org.createnet.raptor.sdk.Raptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

public class RaptorManger {
	@Autowired
	@Value("${raptor.endpoint}")
	private String endpoint;
	
	@Autowired
	@Value("${raptor.adminUser}")
	private String adminUser;
	
	@Autowired
	@Value("${raptor.adminPassword}")
	private String adminPassword;
	
	@Autowired
	@Value("${raptor.userMail}")
	private String userMail;
	
	@Autowired
	@Value("${raptor.domain.prefix}")
	private String domainPrefix;
	
	private ObjectMapper fullMapper;
	private Raptor raptorAdmin;
	
	public static String domainUserRole = "sc_domain_user";
	
	public RaptorManger() {
		fullMapper = new ObjectMapper();
		fullMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		fullMapper.configure(DeserializationFeature.READ_ENUMS_USING_TO_STRING, true);
		fullMapper.configure(SerializationFeature.WRITE_ENUMS_USING_TO_STRING, true);
		fullMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
		fullMapper.configure(SerializationFeature.INDENT_OUTPUT, false);
	}
	
	@PostConstruct
	public void init() {
		raptorAdmin = new Raptor(endpoint, adminUser, adminPassword);
		raptorAdmin.Auth().login();
	}
	
	public User addDomainUser(String user, String secret) {
		User userRaptor = new User();
		userRaptor.setUsername(domainPrefix + user);
		userRaptor.setPassword(secret);
		userRaptor.setEmail(user + userMail);
		userRaptor.setEnabled(true);
		userRaptor.setOwnerId(raptorAdmin.Auth().getUser().getId());
		User userDomain = raptorAdmin.Admin().User().create(userRaptor);
		return userDomain;
	}
	
	public App addUserToApplication(String domain, User domainUser) {
		App app = null;
		String appName = domainPrefix + domain;
		PageResponse<App> pageResponse = raptorAdmin.App().list();
		for(App appTemp : pageResponse.getContent()) {
			if(appTemp.getName().equals(appName)) {
				app = pageResponse.getContent().get(0);
				app.addUser(domainUser, domainUserRole);
				app = raptorAdmin.App().update(app);
				return app;
			}
		}
		List<String> permissions = new ArrayList<String>();
		permissions.add("admin_own_device");
		permissions.add("admin_own_stream");

		List<AppRole> roles = new ArrayList<AppRole>();
		AppRole appRole = new AppRole(domainUserRole, permissions);
		roles.add(appRole);
		
		app = new App(appName, raptorAdmin.Auth().getUser());
		app.setEnabled(true);
		app.setRoles(roles);
		app = raptorAdmin.App().create(app);
		
		app.addUser(domainUser, domainUserRole);
		app = raptorAdmin.App().update(app);
		return app;
	}
	
	public App addDatasetApplication(String domain, String dataset, 
			User domainUser, String domainAppId) {
		App app = null;
		String appName = domainPrefix + domain + "-" + dataset;
		//AppQuery appQuery = new AppQuery();
		//appQuery.name.match(domain);
		PageResponse<App> pageResponse = raptorAdmin.App().list();
		for(App appTemp : pageResponse.getContent()) {
			if(appTemp.getName().equals(appName)) {
				app = pageResponse.getContent().get(0);
				app.addUser(domainUser, domainUserRole);
				app = raptorAdmin.App().update(app);
				return app;
			}
		}
		List<String> permissions = new ArrayList<String>();
		permissions.add("admin_own_device");
		permissions.add("admin_own_stream");

		List<AppRole> roles = new ArrayList<AppRole>();
		AppRole appRole = new AppRole(domainUserRole, permissions);
		roles.add(appRole);

		app = new App(appName, raptorAdmin.Auth().getUser());
		app.setEnabled(true);
		app.setRoles(roles);
		app.setDomain(domainAppId);
		app = raptorAdmin.App().create(app);
		
		app.addUser(domainUser, domainUserRole);
		app = raptorAdmin.App().update(app);
		return app;
	}

	public String getRaptorToken(Raptor raptor, String user, String secret) {
		Token token = new Token(user, secret);
		token.setExpires(0L);
		Token newToken = raptor.Admin().Token().create(token);
		return newToken.getToken();
	}

	public Raptor getRaptorByUser(String user, String secret) {
		Raptor raptor = new Raptor(endpoint, user, secret);
		raptor.Auth().login();
		return raptor;
	}

	public Raptor getRaptorByToken(String token) {
		Raptor raptor = new Raptor(endpoint, token);
		raptor.Auth().login();
		return raptor;
	}

	public void deleteUser(String userId) {
		User user = raptorAdmin.Admin().User().get(userId);
		raptorAdmin.Admin().User().delete(user);
	}
	
	public User getUserById(String userId) {
		return raptorAdmin.Admin().User().get(userId);
	}

}
