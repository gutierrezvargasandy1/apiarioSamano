package com.ApiarioSamano.MicroServiceGeneradorCodigo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.ApiarioSamano")
public class MicroServiceGeneradorCodigoApplication {

	public static void main(String[] args) {
		SpringApplication.run(MicroServiceGeneradorCodigoApplication.class, args);
	}

}
