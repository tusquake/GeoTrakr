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

			// Handle missing .render.com suffix if needed
			if (!jdbcUrl.contains(".render.com")) {
				jdbcUrl = jdbcUrl.replace(
						"dpg-d3l749j3fgac73abrvo0-a/",
						"dpg-d3l749j3fgac73abrvo0-a.oregon-postgres.render.com/"
				);
			}

			System.setProperty("spring.datasource.url", jdbcUrl);
			System.out.println("✅ Converted DATABASE_URL → " + jdbcUrl);
		}
	}
}