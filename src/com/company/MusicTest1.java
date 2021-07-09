package com.company;

import javax.sound.midi.*;
import java.util.Scanner;

public class MusicTest1 {
    public void play (int instrument, int note)
    {
        try {
            Sequencer player = MidiSystem.getSequencer();
            player.open();
            Sequence seq = new Sequence(Sequence.PPQ, 4);
            Track track = seq.createTrack();


            ShortMessage first = new ShortMessage();
            first.setMessage(192, 1, instrument, 0);
            MidiEvent changeInstrument = new MidiEvent(first, 1);
            track.add(changeInstrument);

            ShortMessage a = new ShortMessage();
            a.setMessage(144, 1, note, 100);
            MidiEvent noteOn = new MidiEvent(a, 1);
            track.add(noteOn);

            ShortMessage b = new ShortMessage();
            b.setMessage(128, 1, note, 100);
            MidiEvent noteOff = new MidiEvent(b, 16);
            track.add(noteOff);

            player.setSequence(seq);
            player.start();

        } catch (Exception ex) {
            System.out.println("Fail");
        }
    }

    public static void main(String[] args) {
        MusicTest1 mt = new MusicTest1();
        Scanner in = new Scanner(System.in);

        for(int i = 0; i < 100; i++) mt.play((int) (Math.random() * 128), (int) (Math.random() * 128));
    }
}
