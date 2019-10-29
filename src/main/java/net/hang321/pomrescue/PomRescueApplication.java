package net.hang321.pomrescue;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class PomRescueApplication implements CommandLineRunner {

	@Autowired
	private RescueService rescueService;

	public static void main(String[] args) {
		SpringApplication.run(PomRescueApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		rescueService.rescue();
		System.exit(0);
	}
}
