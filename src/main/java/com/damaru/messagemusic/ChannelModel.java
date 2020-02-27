package com.damaru.messagemusic;

import com.damaru.midi.InstrumentValue;
import com.damaru.music.Note;
import com.damaru.music.ScaleFactory;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class ChannelModel {

    private static final Logger log = LoggerFactory.getLogger(ChannelModel.class);

    private MusicModel musicModel;

    private int id;
    private double lowValue = 15.0;
    private double highValue = 35.0;
    private int lowNoteNum = 36;
    private int highNoteNum = 84;
    private List<Note> noteList;

    private ObjectProperty<InstrumentValue> instrumentValueProp = new SimpleObjectProperty<>();

    public ChannelModel(int id) {
        this.id = id;
    }

    public Note getNote(double value) {
        if (noteList == null) {
            noteList = ScaleFactory.generateNoteList(musicModel.getScaleTypeName(), musicModel.getKeyNote(), lowNoteNum, highNoteNum);
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

    public int getLowNoteNum() {
        return lowNoteNum;
    }

    public void setLowNoteNum(int lowNoteNum) {
        this.lowNoteNum = lowNoteNum;
    }

    public int getHighNoteNum() {
        return highNoteNum;
    }

    public void setHighNoteNum(int highNoteNum) {
        this.highNoteNum = highNoteNum;
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
}
