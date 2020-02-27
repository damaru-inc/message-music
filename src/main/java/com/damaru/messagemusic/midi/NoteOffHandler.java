package com.damaru.messagemusic.midi;

import com.damaru.midi.Midi;
import com.damaru.music.Note;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Component;

import javax.sound.midi.Receiver;
import java.util.concurrent.Future;

@Component
public class NoteOffHandler {

    private static final Logger log = LoggerFactory.getLogger(NoteOffHandler.class);
    private long tick;

    public long getTick() {
        return tick;
    }

    @Async
    public Future<String> start(Receiver receiver, Note[] noteOffs, long[] noteOffTimes) throws Exception {
        tick = 0;
        log.info("playing");

        try {

            while (true) {

                for (int i = 0; i < noteOffs.length; i++) {
                    Note note = noteOffs[i];
                    long time = noteOffTimes[i];

                    if (note != null && time <= tick) {
                        Midi.sendNoteOffMessage(receiver, i, note.getNoteNum(), 0);
                        noteOffs[i] = null;
                    }
                }
                Thread.sleep(MusicPlayer.INTERVAL);
                tick    ++;
            }
        } catch (InterruptedException e) {
            log.info("interrupted....." + tick);
            for (int i = 0; i < noteOffs.length; i++) {
                Note note = noteOffs[i];

                if (note != null ) {
                    Midi.sendNoteOffMessage(receiver, i, note.getNoteNum(), 0);
                    noteOffs[i] = null;
                }
            }
            receiver.close();
        }

        return new AsyncResult<String>("finished " + tick);
    }

}
