package com.damaru.messagemusic;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ConfigurableApplicationContext;

public class FxApplication extends Application {
	private ConfigurableApplicationContext applicationContext;
	private static final Logger log = LoggerFactory.getLogger(FxApplication.class);
	@Override
	public void start(Stage stage) {
		log.info("start...");
		applicationContext.publishEvent(new StageReadyEvent(stage));
	}
	@Override
	public void stop() {
		applicationContext.close();
		Platform.exit();
	}

	@Override
	public void init() {
		log.info("init");
		applicationContext = new SpringApplicationBuilder(MainApplication.class).run();
		for (String beanName : applicationContext.getBeanDefinitionNames()) {
			log.info(beanName);
		}
	}

	static class StageReadyEvent extends ApplicationEvent {
		public StageReadyEvent(Stage stage) {
			super(stage);
		}

		public Stage getStage() {
			return ((Stage) getSource());
		}
	}
}
