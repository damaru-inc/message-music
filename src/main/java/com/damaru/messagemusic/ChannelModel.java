package com.damaru.messagemusic;

import com.damaru.midi.InstrumentValue;
import com.damaru.music.Note;
import com.damaru.music.ScaleFactory;
import javafx.beans.property.*;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.ToggleButton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class ChannelModel {

    private static final Logger log = LoggerFactory.getLogger(ChannelModel.class);

    private MusicModel musicModel;

    private int id;
    private double lowValue = 0.0;
    private double highValue = 35.0;
    private List<Note> noteList;
    private ObjectProperty<InstrumentValue> instrumentValueProp = new SimpleObjectProperty<>();
    private IntegerProperty lowNote = new SimpleIntegerProperty();
    private IntegerProperty highNote = new SimpleIntegerProperty();
    private BooleanProperty solo = new SimpleBooleanProperty();
    private BooleanProperty mute = new SimpleBooleanProperty();
    private ToggleButton soloButton;
    private ToggleButton muteButton;
    private static String MUTE_ON_STYLE = "-fx-background-color: red";
    private static String MUTE_OFF_STYLE = "-fx-background-color: pink";
    private static String SOLO_ON_STYLE = "-fx-background-color: green";
    private static String SOLO_OFF_STYLE = "-fx-background-color: lightgreen";

    private ChangeListener<Number> noteChangeListener = new ChangeListener<>() {
        @Override
        public void changed(ObservableValue<? extends Number> observableValue, Number oldNum, Number newNum) {
            //log.info("Low note changed from " + oldNum + " to " + newNum);
            noteList = null;
        }
    };

    private ChangeListener<Boolean> soloChangeListener = new ChangeListener<>() {
        @Override
        public void changed(ObservableValue<? extends Boolean> observableValue, Boolean oldVal, Boolean newVal) {
            log.info("Solo button note changed from " + oldVal + " to " + newVal + " observable: " + observableValue);
            soloButton.setStyle(newVal ? SOLO_ON_STYLE : SOLO_OFF_STYLE);
            if (newVal) setMute(false);
            musicModel.setSolo(id, newVal);
        }
    };

    private ChangeListener<Boolean> muteChangeListener = new ChangeListener<>() {
        @Override
        public void changed(ObservableValue<? extends Boolean> observableValue, Boolean oldVal, Boolean newVal) {
            log.info("Mute button changed from " + oldVal + " to " + newVal + " observable: " + observableValue);
            muteButton.setStyle(newVal ? MUTE_ON_STYLE : MUTE_OFF_STYLE);
            if (newVal) setSolo(false);
        }
    };

    public ChannelModel(int id) {
        this.id = id;
        lowNote.set(48);
        highNote.set(72);
        lowNote.addListener(noteChangeListener);
        highNote.addListener(noteChangeListener);
        solo.addListener(soloChangeListener);
        mute.addListener(muteChangeListener);
    }

    public Note getNote(double value) {
        if (noteList == null) {
            noteList = ScaleFactory.generateNoteList(musicModel.getScaleTypeName(), musicModel.getKeyNote(), lowNote.intValue(), highNote.intValue());
        }

        if (value < lowValue) {
            log.warn("Value is out of range: {} low: {} high: {}", value, lowValue, highValue);
            value = lowValue;
        }
        if (value > highValue) {
            log.warn("Value is out of range: {} low: {} high: {}", value, lowValue, highValue);
            value = highValue;
        }

        int numNotes = noteList.size();
        double percent = (value - lowValue) / (highValue - lowValue);
        int index = (int) (percent * (numNotes - 1));
        log.debug("value: {} percent: {} index: {} numNotes {}", value, percent, index, numNotes);
        if (index >= numNotes) {
            index = numNotes - 1;
        }
        return noteList.get(index);
    }

    public int getId() { return id; }

    public ObjectProperty<InstrumentValue> instrumentValueProperty() {
        return instrumentValueProp;
    }

    public InstrumentValue getInstrumentValue() {
        return instrumentValueProp.get();
    }

    public void setInstrumentValue(InstrumentValue instrumentValue) {
        instrumentValueProp.setValue(instrumentValue);
        log.info("Set instrument on " + id + " to " + instrumentValue);
    }

    public int getLowNote() {
        return lowNote.get();
    }

    public IntegerProperty lowNoteProperty() {
        return lowNote;
    }

    public void setLowNote(int lowNote) {
        this.lowNote.set(lowNote);
    }

    public int getHighNote() {
        return highNote.get();
    }

    public IntegerProperty highNoteProperty() {
        return highNote;
    }

    public void setHighNote(int highNote) {
        this.highNote.set(highNote);
    }

    public double getLowValue() {
        return lowValue;
    }

    public void setLowValue(double lowValue) {
        this.lowValue = lowValue;
    }

    public double getHighValue() {
        return highValue;
    }

    public void setHighValue(double highValue) {
        this.highValue = highValue;
    }

    public MusicModel getMusicModel() {
        return musicModel;
    }

    public void setMusicModel(MusicModel musicModel) {
        this.musicModel = musicModel;
    }

    public List<Note> getNoteList() {
        return noteList;
    }

    public void setNoteList(List<Note> noteList) {
        this.noteList = noteList;
    }

    public BooleanProperty soloProperty() { return solo; }

    public void setSolo(boolean solo) {
        this.solo.set(solo);
        log.info("setSolo {} {}", id, solo);
    }

    public boolean getSolo() { return solo.get(); }

    public BooleanProperty muteProperty() { return mute; }

    public void setMute(boolean mute) {
        this.mute.set(mute);
        log.info("setMute {} {}", id, solo);
    }

    public boolean getMute() { return mute.get(); }

    public void setSoloButton(ToggleButton soloButton) {
        this.soloButton = soloButton;
    }

    public void setMuteButton(ToggleButton muteButton) {
        this.muteButton = muteButton;
    }
}
