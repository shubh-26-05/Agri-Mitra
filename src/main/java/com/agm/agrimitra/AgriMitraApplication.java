package com.agm.agrimitra;

import com.agm.agrimitra.config.DotenvLoader;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class AgriMitraApplication {

	public static void main(String[] args) {
		DotenvLoader.load();
		SpringApplication.run(AgriMitraApplication.class, args);
	}

}
