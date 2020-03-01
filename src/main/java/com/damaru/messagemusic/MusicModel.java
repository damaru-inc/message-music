package com.damaru.messagemusic;

import com.damaru.music.Note;
import com.damaru.music.Scale;
import com.damaru.music.ScaleTypeName;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class MusicModel {
    private List<ChannelModel> channelModels = new ArrayList();

    private ScaleTypeName scaleTypeName = ScaleTypeName.MAJOR;
    private Note keyNote = new Note(0);
    private IntegerProperty lowInput = new SimpleIntegerProperty();
    private IntegerProperty highInput = new SimpleIntegerProperty();


    public List<ChannelModel> getChannelModels() {
        return channelModels;
    }

    public void addChannelModel(ChannelModel channelModel) {
        channelModel.setMusicModel(this);
        channelModels.add(channelModel);
    }

    public ChannelModel getChannelModel(int index) {
        return channelModels.get(index);
    }

    public ScaleTypeName getScaleTypeName() {
        return scaleTypeName;
    }

    public void setScaleTypeName(ScaleTypeName scaleTypeName) {
        this.scaleTypeName = scaleTypeName;
        reset();
    }

    public Note getKeyNote() {
        return keyNote;
    }

    public void setKeyNote(Note keyNote) {
        this.keyNote = keyNote;
    }

    public void reset() {
        for (ChannelModel model : channelModels) {
            model.setNoteList(null);
        }
    }

    public void setSolo(int id, Boolean on) {
        if (on) {
            boolean allOn = true;
            // If all other channels are on, turn them all off.
            for (ChannelModel cm : channelModels) {
                if (cm.getId() != id && cm.getSolo()) {
                    allOn = false;
                    break;
                }
            }

            if (allOn) {
                for (ChannelModel cm : channelModels) {
                    if (cm.getId() != id) {
                        cm.setMute(true);
                    }
                }
            }
        } else {
            // We're switching off the solo button. Unmute the others if they're all muted.
            boolean allOff = true;
            // If all other channels are on, turn them all off.
            for (ChannelModel cm : channelModels) {
                if (cm.getId() != id && !cm.getMute()) {
                    allOff = false;
                    break;
                }
            }

            if (allOff) {
                for (ChannelModel cm : channelModels) {
                    if (cm.getId() != id) {
                        cm.setMute(false);
                    }
                }
            }
        }
    }

    IntegerProperty lowInputProperty() { return lowInput; }
    IntegerProperty highInputProperty() { return highInput; }
    int getLowInput() { return lowInput.get(); }
    int getHighInput() { return highInput.get(); }

    public void flashLabel(int i) {
        ChannelModel channelModel = channelModels.get(i);
        channelModel.flashLabel();
    }
}
