package com.damaru.messagemusic;

import com.damaru.midi.InstrumentValue;
import com.damaru.music.Note;
import com.damaru.music.ScaleFactory;
import javafx.application.Platform;
import javafx.beans.property.*;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class ChannelModel {

    private static final Logger log = LoggerFactory.getLogger(ChannelModel.class);

    private MusicModel musicModel;

    private int id;
    private List<Note> noteList;
    private Note prevNote;
    private ObjectProperty<InstrumentValue> instrumentValueProp = new SimpleObjectProperty<>();
    private IntegerProperty lowNote = new SimpleIntegerProperty();
    private IntegerProperty highNote = new SimpleIntegerProperty();
    private BooleanProperty solo = new SimpleBooleanProperty();
    private BooleanProperty mute = new SimpleBooleanProperty();
    private StringProperty value = new SimpleStringProperty();
    private StringProperty note = new SimpleStringProperty();
    private ToggleButton soloButton;
    private ToggleButton muteButton;
    private Label valueLabel;
    private Label noteLabel;
    private static String MUTE_ON_STYLE = "-fx-background-color: red";
    private static String MUTE_OFF_STYLE = "-fx-background-color: pink";
    private static String SOLO_ON_STYLE = "-fx-background-color: green";
    private static String SOLO_OFF_STYLE = "-fx-background-color: lightgreen";
    private Font normalFont;
    private Font boldFont;

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

    public Note getNote(double val) {
        if (noteList == null) {
            noteList = ScaleFactory.generateNoteList(musicModel.getScaleTypeName(), musicModel.getKeyNote(), lowNote.intValue(), highNote.intValue());
        }

        double lowValue = musicModel.getLowInput();
        double highValue = musicModel.getHighInput();

        if (val < lowValue) {
            log.warn("Value is out of range: {} low: {} high: {}", val, lowValue, highValue);
            val = lowValue;
        }
        if (val > highValue) {
            log.warn("Value is out of range: {} low: {} high: {}", val, lowValue, highValue);
            val = highValue;
        }

        int numNotes = noteList.size();
        double percent = (val - lowValue) / (highValue - lowValue);
        int index = (int) (percent * (numNotes - 1));
        log.debug("value: {} percent: {} index: {} numNotes {}", val, percent, index, numNotes);
        if (index >= numNotes) {
            index = numNotes - 1;
        }
        Note ret = noteList.get(index);
        boolean newNote = false;

        if (prevNote == null || ret.getNoteNum() != prevNote.getNoteNum()) {
            // visually notify the user by making the value/note labels bold.
            prevNote = ret;
            newNote = true;
        }

        final double v = val;
        final boolean n = newNote;

        Platform.runLater(new Runnable(){
            @Override
            public void run() {
                value.set(String.format("%2.1f", v));

                if (n) {
                    note.set(ret.getName());
                    valueLabel.setFont(boldFont);
                    noteLabel.setFont(boldFont);
                }
            }
        });


        return ret;
    }

    public int getId() { return id; }

    public ObjectProperty<InstrumentValue> instrumentValueProperty() {
        return instrumentValueProp;
    }

    public InstrumentValue getInstrumentValue() {
        return instrumentValueProp.get();
    }

    public IntegerProperty lowNoteProperty() {
        return lowNote;
    }

    public IntegerProperty highNoteProperty() {
        return highNote;
    }

    public void setMusicModel(MusicModel musicModel) {
        this.musicModel = musicModel;
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

    public StringProperty valueProperty() { return value; }
    public StringProperty noteProperty() { return note; }

    public void flashLabel() {
        valueLabel.setFont(normalFont);
        noteLabel.setFont(normalFont);
    }

    public void setValueLabel(Label valueLabel) {
        this.valueLabel = valueLabel;
        Font f = valueLabel.getFont();
        normalFont = Font.font(f.getFamily(), FontWeight.NORMAL, f.getSize());
        boldFont = Font.font(f.getFamily(), FontWeight.BOLD, f.getSize());
    }

    public void setNoteLabel(Label noteLabel) {
        this.noteLabel = noteLabel;
    }
}
