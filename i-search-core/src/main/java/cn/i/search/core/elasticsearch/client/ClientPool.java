package cn.i.search.core.elasticsearch.client;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.elasticsearch.client.Client;

public class ClientPool {
	private static Map<String, Client> pool = new ConcurrentHashMap<>();

	private ClientPool() {
	};

	public static Client getClient(String key) {
		return pool.get(key);
	}

	public static void putClient(String key, Client client) {
		pool.put(key, client);
	}

}
