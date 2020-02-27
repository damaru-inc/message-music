package com.damaru.messagemusic;

import com.damaru.messagemusic.midi.MusicPlayer;
import com.damaru.messagemusic.midi.NoteOffHandler;
import com.damaru.messagemusic.midi.MidiUtil;
import com.damaru.midi.InstrumentValue;
import com.damaru.midi.Midi;
import com.damaru.midi.MidiDeviceValue;
import com.damaru.music.Note;
import com.damaru.music.Scale;
import com.damaru.music.ScaleFactory;
import com.damaru.music.ScaleTypeName;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Receiver;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.Future;


@Component
public class Controller {

	private static final Logger log = LoggerFactory.getLogger(Controller.class);
	int numPanels;

	@Value("classpath:/fxml/panel.fxml")
	private Resource panelResource;

	@Autowired
	ApplicationContext applicationContext;

	@Autowired
	MusicPlayer musicPlayer;

	@Autowired
	MusicModel musicModel;

	@FXML
	ToggleButton playButton;

	@FXML
	ComboBox<MidiDeviceValue> deviceCombo;

	@FXML
	ComboBox<ScaleTypeName> scaleCombo;

	@FXML
	public void initialize() {
		deviceCombo.setItems(MidiUtil.getMidiDevices());
		deviceCombo.getSelectionModel().select(0);

		scaleCombo.setItems(MidiUtil.getScales());
		scaleCombo.getSelectionModel().select(3);
	}

	public void addPanels(int numPanels) {
		for (int i = 0; i < numPanels; i++) {
			addPanel(i);
		}
	}
	public void addPanel(int id) {
		try {
			ChannelModel channelModel = new ChannelModel(id);
			musicModel.addChannelModel(channelModel);

			FXMLLoader fxmlLoader = new FXMLLoader(panelResource.getURL());
			fxmlLoader.setControllerFactory(aClass -> applicationContext.getBean(aClass));
			GridPane gridPane = (GridPane) fxmlLoader.load();
			List kids = gridPane.getChildren();

			Label label = (Label) kids.get(0);
			label.setText("Channel " + id);

			ComboBox<InstrumentValue> comboBox = (ComboBox) kids.get(2);
			comboBox.valueProperty().bindBidirectional(channelModel.instrumentValueProperty());
			comboBox.setOnAction(this::instrumentChanged);
			comboBox.setId("" + id);
			try {
				comboBox.setItems(MidiUtil.getInstruments());
			} catch (MidiUnavailableException e) {
				e.printStackTrace();
			}
			comboBox.getSelectionModel().select(0);
			log.info("--------- loaded " + gridPane);
			final Scene scene = deviceCombo.getScene();
			final BorderPane root = (BorderPane) scene.getRoot();
			final HBox box = (HBox) root.getCenter();
			box.getChildren().add(gridPane);
		} catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException();
		}
	}

	public void instrumentChanged(ActionEvent event) {
		ComboBox combo = (ComboBox) event.getSource();
		log.info("instrumentChanged: " + combo.getId());
		int index = Integer.valueOf(combo.getId());
		ChannelModel channelModel = musicModel.getChannelModel(index);
		log.info("Model: " + channelModel.getInstrumentValue());
		int instr = combo.getSelectionModel().getSelectedIndex();
		if (playButton.isSelected()) {
			musicPlayer.changeInstrument(index, instr);
		}
	}

	public void scaleChanged() {
		ScaleTypeName scaleTypeName = scaleCombo.getValue();
		log.info("scaleChanged: " + scaleTypeName);
		musicModel.setScaleTypeName(scaleTypeName);
	}

	public void togglePlay() {
		log.info("togglePlay: " + playButton.isSelected());
		if (playButton.isSelected()) {
			MidiDeviceValue midiDeviceValue = deviceCombo.getValue();
			MidiDevice currentDevice = midiDeviceValue.getMidiDevice();
			musicPlayer.start(currentDevice);
		} else {
			musicPlayer.stop();
		}
	}

}

//class ButtonEventHandler implements EventHandler<ActionEvent> {
//	private static final Logger log = LoggerFactory.getLogger(ButtonEventHandler.class);
//
//	List<ChannelModel> channelModels;
//
//	ButtonEventHandler(List<ChannelModel> channelModels) {
//		this.channelModels = channelModels;
//	}
//
//	@Override
//	public void handle(ActionEvent actionEvent) {
//		Button button = (Button) actionEvent.getSource();
//		log.info("Got event " + button.getId());
//		for (ChannelModel channelModel : channelModels) {
//			log.info("Instrument: " + channelModel.getInstrumentValue());
//		}
//	}
//}