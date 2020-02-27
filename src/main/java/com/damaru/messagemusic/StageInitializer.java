package com.damaru.messagemusic;

import com.solace.temperature.Temperature;
import com.solace.temperature.TemperatureDataChannel;
import com.solace.temperature.TemperatureMessage;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class StageInitializer implements ApplicationListener<FxApplication.StageReadyEvent> {

	private static final Logger log = LoggerFactory.getLogger(StageInitializer.class);
	private static final int WIDTH = 1280;
	private static final int HEIGHT = 960;
	private static final int NUM_GRAPHS = 4;
	private double[] values;

	@Value("classpath:/fxml/main.fxml")
	private Resource screenResource;

	private ApplicationContext applicationContext;

	@Autowired
	public StageInitializer(ApplicationContext applicationContext) {
		log.info("ctor");
		this.applicationContext = applicationContext;
	}

	@Autowired
	Controller controller;

	@Override
	public void onApplicationEvent(FxApplication.StageReadyEvent event) {
		Stage stage = event.getStage();
		values = new double[NUM_GRAPHS];

		runFromXml(stage);
	}

	private void runFromXml(Stage stage) {
		try {
			FXMLLoader fxmlLoader = new FXMLLoader(screenResource.getURL());
			fxmlLoader.setControllerFactory(aClass -> applicationContext.getBean(aClass));
			Parent parent = fxmlLoader.load();

			stage.setScene(new Scene(parent, WIDTH, HEIGHT));
			stage.setTitle("Visualizer");
			stage.show();
			controller.addPanels(4);
			log.info("--------- loaded " + screenResource.getURL());
		} catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException();
		}
	}

	public void update(int index, double value) {
		values[index] = value;
	}
}

