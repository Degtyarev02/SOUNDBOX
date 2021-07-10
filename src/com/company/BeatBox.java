package com.company;

import javax.sound.midi.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Base64;

public class BeatBox {
    JPanel mainPanel;
    ArrayList<JCheckBox> checkBoxArrayList;
    Sequencer sequencer;
    Sequence sequence;
    Track track;
    JFrame mainFrame;

    String[] instrumentNames = {"Bass Drum", "Closed Hi-Hat",
            "Open Hi-Hat", "Acoustic Snare", "Crash Cymbal", "Hand Clap",
            "High Tom", "Hi Bongo", "Maracas", "Whistle", "Low Conga",
            "Cowbell", "VibraSlap", "Low-mid Tom", "High Agogo",
            "Open Hi Conga"};
    int[] instruments = {35, 42, 46, 38, 49, 39, 50, 60, 70, 72, 64, 56, 58, 47, 67, 63};

    public static void main(String[] args) {
        new BeatBox().buildGUI();
    }

    private void buildGUI() {
        mainFrame = new JFrame("Beat Boxer");
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        BorderLayout layout = new BorderLayout();
        JPanel background = new JPanel(layout);
        background.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        checkBoxArrayList = new ArrayList<JCheckBox>();
        Box buttonBox = new Box(BoxLayout.Y_AXIS);


        JButton start = new JButton("Start");
        start.addActionListener(new MyStartListener());
        buttonBox.add(start);

        JButton stop = new JButton("Stop");
        stop.addActionListener(new MyStopListener());
        buttonBox.add(stop);

        JButton upTempo = new JButton("Tempo up");
        upTempo.addActionListener(new MyUpTempoListener());
        buttonBox.add(upTempo);

        JButton downTempo = new JButton("Tempo down");
        downTempo.addActionListener(new MyDownTempoListener());
        buttonBox.add(downTempo);

        Box nameBox = new Box(BoxLayout.Y_AXIS);
        for (int i = 0; i < 16; i++) {
            nameBox.add(new Label(instrumentNames[i]));
        }

        background.add(BorderLayout.WEST, nameBox);
        background.add(BorderLayout.EAST, buttonBox);

        mainFrame.getContentPane().add(background);

        GridLayout gridLayout = new GridLayout(16, 16);
        gridLayout.setVgap(1);
        gridLayout.setHgap(2);
        mainPanel = new JPanel(gridLayout);
        background.add(BorderLayout.CENTER, mainPanel);

        for (int i = 0; i < 256; i++) {
            JCheckBox checkBox = new JCheckBox();
            checkBox.setSelected(false);
            checkBoxArrayList.add(checkBox);
            mainPanel.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
            mainPanel.add(checkBox);
        }

        setUpMIDI();

        mainFrame.setBounds(50, 50, 300, 300);
        mainFrame.pack();
        mainFrame.setVisible(true);

    }

    private void setUpMIDI() {

        try {
            sequencer = MidiSystem.getSequencer();
            sequencer.open();
            sequence = new Sequence(Sequence.PPQ, 4);
            track = sequence.createTrack();
            sequencer.setTempoInBPM(120);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void buildTrackAndStart() {
        int[] trackList = null;

        sequence.deleteTrack(track);
        track = sequence.createTrack();

        for (int i = 0; i < 16; i++) {
            trackList = new int[16];
            int key = instruments[i];

            for (int j = 0; j < 16; j++) {
                JCheckBox jCheckBox = (JCheckBox) checkBoxArrayList.get(j + (16 * i));
                if (jCheckBox.isSelected()) {
                    trackList[j] = key;
                } else {
                    trackList[j] = 0;
                }
            }
            makeTracks(trackList);
            track.add(MakeMidiEvent(176, 1, 127, 0, 16));
        }

        track.add(MakeMidiEvent(192, 9, 1, 0, 15));
        try {
            sequencer.setSequence(sequence);
            sequencer.setLoopCount(sequencer.LOOP_CONTINUOUSLY);
            sequencer.start();
            sequencer.setTempoInBPM(120);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }


    private void makeTracks(int[] trackList) {
        for (int i = 0; i < 16; i++) {
            int key = trackList[i];
            if (key != 0) {
                track.add(MakeMidiEvent(144, 9, key, 100, i));
                track.add(MakeMidiEvent(128, 9, key, 100, i + 1));
            }
        }
    }

    private class MyStartListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            buildTrackAndStart();
        }
    }

    private class MyStopListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            sequencer.stop();
        }
    }

    private class MyUpTempoListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            float tempoFactor = sequencer.getTempoFactor();
            sequencer.setTempoFactor((float) (tempoFactor * 1.05));
        }
    }

    private class MyDownTempoListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            float tempoFactor = sequencer.getTempoFactor();
            sequencer.setTempoFactor((float) (tempoFactor * .95));
        }
    }

    public static MidiEvent MakeMidiEvent(int command, int channel, int one, int two, int tick) {
        MidiEvent midiEvent = null;
        try {
            ShortMessage message = new ShortMessage();
            message.setMessage(command, channel, one, two);
            midiEvent = new MidiEvent(message, tick);

        } catch (Exception ex) {ex.printStackTrace();}

        return midiEvent;
    }
}
