package it.smartcommunitylab.iotengine.raptor;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.createnet.raptor.models.acl.PermissionUtil;
import org.createnet.raptor.models.acl.Permissions;
import org.createnet.raptor.models.auth.Token;
import org.createnet.raptor.models.auth.User;
import org.createnet.raptor.models.objects.Device;
import org.createnet.raptor.sdk.Raptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Map;

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
	
	private ObjectMapper fullMapper;
	private Raptor raptorAdmin;
	
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

	public User addUser(String user, String secret) {
		User userRaptor = raptorAdmin.Admin().User().create(user, secret, "test@test.raptor.local");
		return userRaptor;
	}

	public Device addDevice(Raptor raptor, String name, String description, Map<String, Object> properties) {
		Device dev = new Device();
		dev.name(name).description(description);
		dev.properties().putAll(properties);
		dev.validate();
		raptor.Inventory().create(dev);
		return dev;
	}

	public String getRaptorToken(Raptor raptor, String user, String secret) {
		Token token = raptor.Admin().Token().create(new Token(user, secret));
		List<String> permissions = PermissionUtil.asList(Permissions.pull, Permissions.push);
		raptor.Admin().Token().Permission().set(token, permissions);
		return token.getToken();
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

	public void deleteDevices(Raptor raptor) {
		for(Device device : raptor.Inventory().list()) {
			raptor.Inventory().delete(device);
		}
	}

	public void deleteTokens(Raptor raptor) {
		for(Token token : raptor.Admin().Token().list()) {
			//??? raptor.Admin().Token().
		}
	}

	public void deleteUser(Raptor raptor, String userId) {
		User user = raptor.Admin().User().get(userId);
		raptor.Admin().User().delete(user);
	}

}
