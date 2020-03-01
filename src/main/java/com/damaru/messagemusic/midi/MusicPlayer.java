package com.damaru.messagemusic.midi;

import com.damaru.messagemusic.ChannelModel;
import com.damaru.messagemusic.Controller;
import com.damaru.messagemusic.MusicModel;
import com.damaru.midi.Midi;
import com.damaru.music.Note;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.sound.midi.MidiDevice;
import javax.sound.midi.Receiver;
import java.util.List;
import java.util.concurrent.Future;

@Component
public class MusicPlayer {
    private static final Logger log = LoggerFactory.getLogger(MusicPlayer.class);
    private Receiver currentReceiver;
    public static final int INTERVAL = 20; // milliseconds sleep time
    private static final int LOOPS_PER_SECOND = 1000 / INTERVAL;
    private static final int DURATION = LOOPS_PER_SECOND * 10;
    private static final int FLASH_DURATION = LOOPS_PER_SECOND;


    @Autowired
    NoteOffHandler noteOffHandler;

    @Autowired
    MusicModel musicModel;
    private Note[] currentNotes;
    private Note[] noteOffs;
    private long[] noteOffTimes;
    private long[] labelFlashTimes;
    private Future<String> playFuture;
    private boolean running;

    public void start(MidiDevice currentDevice) {
        try {
            currentDevice.open();
            currentReceiver = currentDevice.getReceiver();
            List<ChannelModel> channelModels = musicModel.getChannelModels();
            int numChannels = channelModels.size();
            for (int channel = 0; channel < numChannels; channel++) {
                ChannelModel channelModel = channelModels.get(channel);
                try {
                    Midi.sendProgramChangeMessage(currentReceiver, channel, channelModel.getInstrumentValue().getProgram());
                } catch (Exception e) {
                    e.printStackTrace();
                    currentDevice.close();
                    return;
                }
            }

            noteOffs = new Note[numChannels];
            noteOffTimes = new long[numChannels];
            currentNotes = new Note[numChannels];
            labelFlashTimes = new long[numChannels];
            playFuture = noteOffHandler.start(this, currentReceiver, noteOffs, noteOffTimes, labelFlashTimes);
            running = true;
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void stop() {
        if (playFuture != null && !playFuture.isDone()) {
            playFuture.cancel(true);
        }
        running = false;
    }

    public void setValue(int channel, double value) {
        if (running) {
            int numChannels = musicModel.getChannelModels().size();
            if (channel >= numChannels) {
                log.warn("Got a channel that's higher than expected. Num channels: {} channel: {}", numChannels, channel);
            } else {
                ChannelModel channelModel = musicModel.getChannelModel(channel);
                if (channelModel.getMute()) {
                    return;
                }
                Note note = channelModel.getNote(value);
                int num = note.getNoteNum();
                Note prevNote = currentNotes[channel];

                if (prevNote == null || num != prevNote.getNoteNum()) {
                    try {
                        if (prevNote != null) {
                            Midi.sendNoteOffMessage(currentReceiver, channel, prevNote.getNoteNum(), 127);
                        }
                        currentNotes[channel] = note;
                        Midi.sendNoteOnMessage(currentReceiver, channel, note.getNoteNum(), 127);
                        long tick = noteOffHandler.getTick();
                        long noteOffTick = tick + DURATION;
                        log.debug("new note channel {} num {} tick {} noteOff {}", channel, note.getNoteNum(), tick, noteOffTick);
                        synchronized (this) {
                            noteOffs[channel] = note;
                            noteOffTimes[channel] = noteOffTick;
                            labelFlashTimes[channel] = tick + FLASH_DURATION;
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    public void changeInstrument(int channel, int instrument) {
        try {
            Midi.sendProgramChangeMessage(currentReceiver, channel, instrument);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void flashLabel(int i) {
        musicModel.flashLabel(i);
    }
}
