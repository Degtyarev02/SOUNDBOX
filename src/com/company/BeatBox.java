package com.company;

import javax.sound.midi.*;
import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.Socket;
import java.security.spec.ECField;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.Vector;

public class BeatBox {
    JPanel mainPanel;
    JList incomingList;
    ArrayList<JCheckBox> checkBoxArrayList;
    Sequencer sequencer;
    Sequence sequence;
    Sequence mySequence = null;
    Track track;
    JFrame mainFrame;
    String userName;
    ObjectInputStream in;
    ObjectOutputStream out;
    JTextField userMessage;
    Vector<String> listVector = new Vector<>();
    int nextNum;
    HashMap<String, boolean[]> outherSeqsMap = new HashMap<>();

    String[] instrumentNames = {"Bass Drum", "Closed Hi-Hat",
            "Open Hi-Hat", "Acoustic Snare", "Crash Cymbal", "Hand Clap",
            "High Tom", "Hi Bongo", "Maracas", "Whistle", "Low Conga",
            "Cowbell", "VibraSlap", "Low-mid Tom", "High Agogo",
            "Open Hi Conga"};
    int[] instruments = {35, 42, 46, 38, 49, 39, 50, 60, 70, 72, 64, 56, 58, 47, 67, 63};
    
    public static void main(String[] args) {
        new BeatBox().startUp("args[0]");
    }

    public void startUp(String name) {
        userName = name;
        try {
            Socket socket = new Socket("192.168.0.104", 4244);
            out = new ObjectOutputStream(socket.getOutputStream());
            in = new ObjectInputStream(socket.getInputStream());
            Thread remote = new Thread(new RemoteReader());
            remote.start();
        } catch (Exception exception) {
            exception.printStackTrace();
        }
        setUpMIDI();
        buildGUI();

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

        JButton sendIt = new JButton("Send");
        sendIt.addActionListener(new MysSendListener());
        buttonBox.add(sendIt);

        userMessage = new JTextField();
        buttonBox.add(userMessage);

        incomingList = new JList();
        incomingList.addListSelectionListener(new MyListSelectionListener());
        incomingList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane theList = new JScrollPane(incomingList);
        buttonBox.add(theList);
        incomingList.setListData(listVector);


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

        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return midiEvent;
    }

    private class SerializeListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            boolean[] chekBoxState = new boolean[256];
            for (int i = 0; i < 256; i++) {
                JCheckBox check = (JCheckBox) checkBoxArrayList.get(i);
                if (check.isSelected()) {
                    chekBoxState[i] = true;
                } else {
                    chekBoxState[i] = false;
                }
            }
            try {
                FileOutputStream fileOutputStream = new FileOutputStream(new File("CheckBox.ser"));
                ObjectOutputStream os = new ObjectOutputStream(fileOutputStream);
                os.writeObject(chekBoxState);
            } catch (Exception exception) {
                exception.printStackTrace();
            }

        }
    }

    private class RestoreListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {

            boolean[] checkBoxState = null;
            try {
                FileInputStream inputStream = new FileInputStream(new File("CheckBox.ser"));
                ObjectInputStream os = new ObjectInputStream(inputStream);
                checkBoxState = (boolean[]) os.readObject();
            } catch (Exception exception) {
                exception.printStackTrace();
            }
            for (int i = 0; i < 256; i++) {
                JCheckBox checkBox = (JCheckBox) checkBoxArrayList.get(i);
                if (checkBoxState[i]) {
                    checkBox.setSelected(true);
                } else {
                    checkBox.setSelected(false);
                }
            }
        }
    }

    private class MysSendListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            boolean[] chekBoxState = new boolean[256];
            for (int i = 0; i < 256; i++) {
                JCheckBox check = (JCheckBox) checkBoxArrayList.get(i);
                if (check.isSelected()) {
                    chekBoxState[i] = true;
                }
            }
            String messageToSend = null;
            try {
                out.writeObject(userName + nextNum++ + ": " + userMessage.getText());
                out.writeObject(chekBoxState);
            } catch (Exception exception) {
                exception.printStackTrace();
                System.out.println("Couldn't send to server");
            }
            userMessage.setText("");
        }
    }

    private class MyListSelectionListener implements javax.swing.event.ListSelectionListener {
        @Override
        public void valueChanged(ListSelectionEvent le) {
            if (!le.getValueIsAdjusting()) {
                String selected = (String) incomingList.getSelectedValue();
                if (selected != null) {
                    boolean[] selectedState = (boolean[]) outherSeqsMap.get(selected);
                    changeSequence(selectedState);
                    sequencer.stop();
                    buildTrackAndStart();
                }
            }
        }
    }

    private class RemoteReader implements Runnable {
        boolean[] checkbox = null;
        Object obj = null;

        @Override
        public void run() {
            try {
                while ((obj = in.readObject()) != null) {
                    System.out.println("Got an obj");
                    System.out.println(obj.getClass());
                    String nameToShow = (String) obj;
                    checkbox = (boolean[]) in.readObject();
                    outherSeqsMap.put(nameToShow, checkbox);
                    listVector.add(nameToShow);
                    incomingList.setListData(listVector);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    public class MyPlayMineListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            if (mySequence != null) {
                sequence = mySequence;
            }
        }
    }

    public void changeSequence(boolean[] checkboxState) {
        for (int i = 0; i < 256; i++) {
            JCheckBox checkBox = (JCheckBox) checkBoxArrayList.get(i);
            if (checkboxState[i]) {
                checkBox.setSelected(true);
            } else checkBox.setSelected(false);

        }
    }
}
