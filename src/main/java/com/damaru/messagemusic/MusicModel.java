package com.damaru.messagemusic;

import com.damaru.music.Note;
import com.damaru.music.Scale;
import com.damaru.music.ScaleTypeName;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class MusicModel {
    private List<ChannelModel> channelModels = new ArrayList();

    private ScaleTypeName scaleTypeName = ScaleTypeName.MAJOR;
    private Note keyNote = new Note(0);

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
}
