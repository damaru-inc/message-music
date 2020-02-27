package com.damaru.messagemusic.midi;

import com.damaru.midi.InstrumentValue;
import com.damaru.midi.Midi;
import com.damaru.midi.MidiDeviceValue;
import com.damaru.music.ScaleFactory;
import com.damaru.music.ScaleTypeName;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import javax.sound.midi.MidiUnavailableException;
import java.util.Arrays;

public class MidiUtil {
    public static ObservableList<MidiDeviceValue> getMidiDevices() {
        ObservableList<MidiDeviceValue> ret = FXCollections.observableArrayList();
        ret.addAll(Midi.getReceivers());
        return ret;
    }

    public static ObservableList<InstrumentValue> getInstruments() throws MidiUnavailableException {
        ObservableList<InstrumentValue> ret = FXCollections.observableArrayList();
        ret.addAll(Midi.getInstruments());
        return ret;
    }

    public static ObservableList<ScaleTypeName> getScales() {
        ObservableList<ScaleTypeName> ret = FXCollections.observableArrayList();
        ret.addAll(ScaleTypeName.values());
        return ret;
    }
}
