package net.hang321.pomrescue;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toMap;

@Service
public class RescueService {

	private static Logger logger = LoggerFactory.getLogger(RescueService.class);

	void rescue() {
		Map<String, String> sha1map = parseSha1File();

		logger.info("sha1map: {}", sha1map);
	}

	/**
	 * parse sha1 file generated in *nix env
	 *
	 * @return a map with artifactId-version-type as key, sha1 as value
	 */
	private Map<String, String> parseSha1File() {

		Map<String, String> map = new TreeMap<>();
		try {
			Path path = Paths.get(getClass().getClassLoader().getResource("jar-sha1sums.txt").toURI());
			Stream<String> lines = Files.lines(path);

			map = lines.map(str -> str.split("  "))
					.collect(toMap(str -> str[1].trim(), str -> str[0]));

			lines.close();
		} catch (URISyntaxException | IOException e) {
			e.printStackTrace();
		}
		return map;
	}
}
