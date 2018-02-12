package it.smartcommunitilab.iotengine.test;

import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.createnet.raptor.models.data.RecordSet;
import org.createnet.raptor.models.data.ResultSet;
import org.createnet.raptor.models.objects.Channel;
import org.createnet.raptor.models.objects.Device;
import org.createnet.raptor.models.objects.Stream;
import org.createnet.raptor.sdk.PageResponse;
import org.createnet.raptor.sdk.Raptor;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;


@RunWith(SpringRunner.class)
@TestPropertySource("classpath:application.properties")
public class TestDevice {
	@Value("${raptor.endpoint}")
	private String endpoint;
	
	@Value("${raptor.adminUser}")
	private String adminUser;
	
	@Value("${raptor.adminPassword}")
	private String adminPassword;
	
	
	Raptor raptor = null;
	SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
	
	private Raptor getRaptor() {
		if(raptor == null) {
			raptor = new Raptor(endpoint, token);
			//raptor = new Raptor(endpoint, adminUser, adminPassword);
			raptor.Auth().login();
		}
		return raptor;
	}
	
	private Stream getStream(String streamName) {
		PageResponse<Device> pageResponse = getRaptor().Inventory().list();
		for(Device device : pageResponse.getContent()) {
			System.out.println("device:" + device.name());
			if(device.getName().equals("SC-IOT-test_device")) {
				for(Stream stream : device.streams().values()) {
					System.out.println("stream:" + stream.getName());
					if(stream.getName().equals(streamName)) {
						for(Channel channel : stream.channels().values()) {
							System.out.println("channel:" + channel.getName() + " - " + channel.getType());
						}
						return stream;
					}
				}				
			}
		}
		return null;
	}
	
	@Test
	public void addDevice() {
		Device dev = new Device();
		dev.name("SC-IOT-test_device").description("info about");
		//dev.domain(appId);
		dev.addStream("stream-test", "ownerId", "string");
		dev.addStream("stream-test", "routeId", "string");
		dev.addStream("stream-test", "wsnNodeId", "string");
		dev.addStream("stream-test", "passengerId", "string");
		dev.addStream("stream-test", "eventType", "number");
		dev.addStream("stream-test", "latitude", "number");
		dev.addStream("stream-test", "longitude", "number");
		dev.addStream("stream-test", "accuracy", "number");
		dev.validate();
		dev = getRaptor().Inventory().create(dev);
		System.out.println("device:" + dev.getId());
	}
	
	@Test
	public void testWriteEvents() throws Exception {
		List<TestEvent> events = readEvents();
		Stream stream = getStream("stream-test");
		for(TestEvent event : events) {
			RecordSet record = new RecordSet(stream);
			record.channel("ownerId", event.getOwnerId());
			record.channel("routeId", event.getRouteId());
			record.channel("wsnNodeId", event.getWsnNodeId());
			record.channel("passengerId", event.getPassengerId());
			record.channel("eventType", event.getEventType());
			record.channel("latitude", event.getLatitude());
			record.channel("longitude", event.getLongitude());
			record.channel("accuracy", event.getAccuracy());
			record.timestamp(event.getTimestamp());
			getRaptor().Stream().push(record);
		}
	}
	
	@Test
	public void testReadEvents() {
		Stream stream = getStream("stream-test");
		ResultSet results = getRaptor().Stream().pull(stream, 0, 1000);
		Iterator<RecordSet> iterator = results.iterator();
		while(iterator.hasNext()) {
			RecordSet recordSet = iterator.next();
			System.out.println(String.format("event: %s %s", recordSet.getTimestamp(), 
					recordSet.getChannels().get("eventType")));
		}
	}
	
	private List<TestEvent> readEvents() throws IOException {
		List<TestEvent> result = new ArrayList<TestEvent>();
		FileReader fileReader = new FileReader("C:\\Users\\micnori\\Documents\\Progetti\\climb\\console\\game\\events.json");
		ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		JsonFactory jsonFactory = new JsonFactory();
		jsonFactory.setCodec(objectMapper);
		JsonParser jp = jsonFactory.createParser(fileReader);
		JsonToken current;
		current = jp.nextToken();
		TypeReference<Map<String, Object>> typeRef = new TypeReference<Map<String, Object>>() {};
		if (current != JsonToken.START_ARRAY) {
			System.out.println("Error: records should be an array: skipping.");
    }
		int count = 1;
		while (jp.nextToken() != JsonToken.END_ARRAY) {
			try {
				Map<String, Object> entity = jp.readValueAs(typeRef);
				//System.out.println(count + ":" + entity);
				TestEvent testEvent = new TestEvent();
				testEvent.setOwnerId((String) entity.get("ownerId"));
				testEvent.setRouteId((String) entity.get("routeId"));
				testEvent.setWsnNodeId((String) entity.get("wsnNodeId"));
				testEvent.setPassengerId(getPassengerId(entity));
				testEvent.setTimestamp(getTimestamp(entity));
				testEvent.setEventType((int) entity.get("eventType"));
				testEvent.setLatitude(getLatitude(entity));
				testEvent.setLongitude(getLongitude(entity));
				testEvent.setAccuracy(getAccuracy(entity));
				result.add(testEvent);
				count++;
			} catch (Exception e) {
				System.err.println(e.getMessage());
			}
		}
		return result;
	}
	
	@SuppressWarnings("unchecked")
	private double getLatitude(Map<String, Object> entity) {
		Map<String, Object> payload = (Map<String, Object>) entity.get("payload");
		if(payload.containsKey("latitude")) {
			return (double) payload.get("latitude");
		} else {
			return -1;
		}
	}
	
	@SuppressWarnings("unchecked")
	private double getLongitude(Map<String, Object> entity) {
		Map<String, Object> payload = (Map<String, Object>) entity.get("payload");
		if(payload.containsKey("longitude")) {
			return (double) payload.get("longitude");
		} else {
			return -1;
		}
	}
	
	@SuppressWarnings("unchecked")
	private double getAccuracy(Map<String, Object> entity) {
		Map<String, Object> payload = (Map<String, Object>) entity.get("payload");
		Number number = (Number) payload.get("accuracy");
		if(number != null) {
			return number.doubleValue();
		} else {
			return -1;
		}
	}
	
	@SuppressWarnings("unchecked")
	private String getPassengerId(Map<String, Object> entity) {
		Map<String, Object> payload = (Map<String, Object>) entity.get("payload");
		if(payload.containsKey("passengerId")) {
			return (String) payload.get("passengerId");
		} else {
			return null;
		}
	}
	
	@SuppressWarnings("unchecked")
	private long getTimestamp(Map<String, Object> entity) throws ParseException {
		Map<String, Object> timestamp = (Map<String, Object>) entity.get("timestamp");
		String date = (String) timestamp.get("$date");
		return sdf.parse(date).getTime();
	}
}
