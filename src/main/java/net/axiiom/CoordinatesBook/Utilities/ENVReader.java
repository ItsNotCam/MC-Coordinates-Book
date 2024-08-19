package net.axiiom.CoordinatesBook.Utilities;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;

public class ENVReader {
	final HashMap<String,String> envHash = new HashMap<>();

	public ENVReader() {
		InputStream inputStream = getClass().getClassLoader().getResourceAsStream(".env");
		try (BufferedReader br = new BufferedReader(new InputStreamReader(inputStream))) {
			String line;
			while ((line = br.readLine()) != null) {
				String[] keyVal = line.split("=");
				envHash.put(keyVal[0], keyVal[1]);
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public String get(String key) {
		return envHash.getOrDefault(key, null);
	}
}
