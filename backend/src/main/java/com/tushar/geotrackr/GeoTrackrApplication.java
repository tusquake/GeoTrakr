package com.tushar.geotrackr;

import jakarta.annotation.PostConstruct;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class GeoTrackrApplication {

	public static void main(String[] args) {
		SpringApplication.run(GeoTrackrApplication.class, args);
	}

	@PostConstruct
	public void fixRenderDatabaseUrl() {
		String dburl = System.getenv("DATABASE_URL");
		if (dburl != null && dburl.startsWith("postgresql://")) {
			String jdbcUrl = dburl.replace("postgresql://", "jdbc:postgresql://");
			System.setProperty("spring.datasource.url", jdbcUrl);
		}
	}
}