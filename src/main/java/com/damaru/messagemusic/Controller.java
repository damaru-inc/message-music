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
import javafx.application.Platform;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import org.controlsfx.control.RangeSlider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiUnavailableException;
import java.io.IOException;
import java.util.*;

@Component
public class Controller {

	private static final Logger log = LoggerFactory.getLogger(Controller.class);
	double lowValue = Double.MAX_VALUE;
	double highValue = Double.MIN_VALUE;

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
	Label lowValueLabel;

	@FXML
	Label highValueLabel;

	private DoubleProperty lowestValue = new SimpleDoubleProperty();
	private DoubleProperty highestValue = new SimpleDoubleProperty();

	@FXML
	public void initialize() {
		deviceCombo.setItems(MidiUtil.getMidiDevices());
		deviceCombo.getSelectionModel().select(0);

		scaleCombo.setItems(MidiUtil.getScales());
		scaleCombo.getSelectionModel().select(3);
	}

	public void setupAdditionalComponents() {
		final Scene scene = deviceCombo.getScene();
		final BorderPane root = (BorderPane) scene.getRoot();
		final HBox box = (HBox) root.getTop();

		final RangeSlider hSlider = new RangeSlider(-10, 50, 0, 40);
		hSlider.setOrientation(Orientation.HORIZONTAL);
		hSlider.setShowTickMarks(true);
		hSlider.setShowTickLabels(true);
		hSlider.setBlockIncrement(10);
		hSlider.setMajorTickUnit(10);
		hSlider.lowValueProperty().bindBidirectional(musicModel.lowInputProperty());
		hSlider.highValueProperty().bindBidirectional(musicModel.highInputProperty());
		hSlider.setPrefWidth(500.0);
		HBox.setMargin(hSlider, new Insets(20, 20, 20, 20));
		hSlider.setHighValue(40);
		hSlider.setLowValue(0);
		box.getChildren().add(hSlider);

	}

	public void addPanels(int numPanels) {
		for (int i = 0; i < numPanels; i++) {
			addPanel(i);
		}
	}
	public void addPanel(int id) {
		try {

			// kids:
			// 0: channel label
			// 1: instrument combobox
			// 2: hbox with solo and mute buttons
			// 3: hbox with value and note labels
			ChannelModel channelModel = new ChannelModel(id);
			musicModel.addChannelModel(channelModel);

			FXMLLoader fxmlLoader = new FXMLLoader(panelResource.getURL());
			fxmlLoader.setControllerFactory(aClass -> applicationContext.getBean(aClass));
			GridPane gridPane = (GridPane) fxmlLoader.load();
			List kids = gridPane.getChildren();


			Label label = (Label) kids.get(0);
			label.setText("Channel " + id);

			ComboBox<InstrumentValue> comboBox = (ComboBox) kids.get(1);
			comboBox.valueProperty().bindBidirectional(channelModel.instrumentValueProperty());
			comboBox.setOnAction(this::instrumentChanged);
			comboBox.setId("" + id);
			try {
				comboBox.setItems(MidiUtil.getInstruments());
			} catch (MidiUnavailableException e) {
				e.printStackTrace();
			}
			comboBox.getSelectionModel().select(0);

			HBox hbox = (HBox) kids.get(2);
			List hboxKids = hbox.getChildren();

			ToggleButton soloButton = (ToggleButton) hboxKids.get(0);
			soloButton.selectedProperty().bindBidirectional(channelModel.soloProperty());
			channelModel.setSoloButton(soloButton);

			ToggleButton muteButton = (ToggleButton) hboxKids.get(1);
			muteButton.selectedProperty().bindBidirectional(channelModel.muteProperty());
			channelModel.setMuteButton(muteButton);

			hbox = (HBox) kids.get(3);
			hboxKids = hbox.getChildren();
			Label valueLabel = (Label) hboxKids.get(0);
			valueLabel.textProperty().bindBidirectional(channelModel.valueProperty());
			Label noteLabel = (Label) hboxKids.get(1);
			noteLabel.textProperty().bindBidirectional(channelModel.noteProperty());

			final RangeSlider hSlider = new RangeSlider(0, 127, 48, 72);
			hSlider.setId("" + id);
			hSlider.setOrientation(Orientation.VERTICAL);
			hSlider.setShowTickMarks(true);
			hSlider.setShowTickLabels(true);
			hSlider.setBlockIncrement(10);
			hSlider.lowValueProperty().bindBidirectional(channelModel.lowNoteProperty());
			hSlider.highValueProperty().bindBidirectional(channelModel.highNoteProperty());
			gridPane.add(hSlider, 0, 4);
			gridPane.setHalignment(hSlider, HPos.CENTER);
			GridPane.setMargin(hSlider, new Insets(20, 0, 20, 0));

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

	public void setValue(Double temperature) {
		if (temperature > highValue) {
			highValue = temperature;

			Platform.runLater(new Runnable(){
				@Override
				public void run() {
					highValueLabel.setText(String.format("%2.1f", highValue));
				}
			});
		}

		if (temperature < lowValue) {
			lowValue = temperature;
			Platform.runLater(new Runnable(){
				@Override
				public void run() {
					lowValueLabel.setText(String.format("%2.1f", lowValue));
				}
			});

		}
	}
}
