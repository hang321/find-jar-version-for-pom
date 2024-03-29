package net.hang321.pomrescue;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Writer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toMap;

@Service
public class RescueService {

	private static Logger logger = LoggerFactory.getLogger(RescueService.class);

	void rescue() {
		Map<String, String> sha1map = parseSha1File();
		logger.debug("sha1map: {}", sha1map);

		ObjectMapper mapper = new ObjectMapper();
		RestTemplate restTemplate = new RestTemplate();
		String mavenUrl = "http://search.maven.org/solrsearch/select?q=1:\"";
		String mavenUrlSuffix = "\"&rows=20&wt=json";

		Model model = new Model();
		List<Dependency> dependencies = new ArrayList<>();

		// set optional info here
		model.setModelVersion("4.0.0");
		model.setGroupId("net.hang321");
		model.setArtifactId("pom-rescue");
		model.setVersion("1.0.0-SNAPSHOT");


		for (Map.Entry<String, String> entry : sha1map.entrySet()) {

			ResponseEntity<String> response
					= restTemplate.getForEntity(mavenUrl + entry.getValue() + mavenUrlSuffix, String.class);

			logger.trace("{}", response.getBody());

			JsonNode root = null;
			try {
				root = mapper.readTree(response.getBody());
				JsonNode numFound = root.path("response").path("numFound");
				if (numFound.intValue() == 1) {
					JsonNode docNode = root.path("response").path("docs").get(0);
					// logger.debug("id: {}", docNode.get("id"));

					String groupId = docNode.get("g").asText();
					String artifactId = docNode.get("a").asText();
					String version =docNode.get("v").asText();
					logger.info("{}:{}:{}", groupId, artifactId, version);

					Dependency dependency = new Dependency();
					dependency.setGroupId(groupId);
					dependency.setArtifactId(artifactId);
					dependency.setVersion(version);

					dependencies.add(dependency);
				}

			} catch (IOException e) {
				e.printStackTrace();
			}

			model.setDependencies(dependencies);
			writeMavenModel(model);
		}
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

	private void writeMavenModel(Model model) {

		MavenXpp3Writer writer = new MavenXpp3Writer();
		String baseDir = getClass().getProtectionDomain().getCodeSource().getLocation().getFile();

		try {
			writer.write(new FileOutputStream(new File(baseDir, "/new-pom.xml")), model);

		} catch (IOException e) {
			e.printStackTrace();
		}

	}
}
