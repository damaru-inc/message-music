package com.damaru.messagemusic;

import com.solace.temperature.TemperatureDataChannel;
import javafx.application.Application;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

import javax.annotation.PostConstruct;

@SpringBootApplication
@EnableScheduling
@EnableAsync
@ComponentScan({"com.solace", "com.damaru"})
public class MainApplication {

	private static final Logger log = LoggerFactory.getLogger(MainApplication.class);

	@Autowired
	TemperatureDataChannel temperatureDataChannel;

	@Autowired
	TemperatureListener temperatureListener;

	public static void main(String[] args) {
		Application.launch(FxApplication.class, args);
	}

	@PostConstruct
	private void init() {
		try {
			log.info("subscribing....");
			temperatureDataChannel.subscribe(temperatureListener);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}


}
