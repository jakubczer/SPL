package Interface;


import Audio.AudioInfo;
import Calculations.*;
import CalibrationPKG.Calibration;
import SignalInput.SignalInput;
import Save.*;

import javax.sound.sampled.AudioFormat;
import javax.swing.*;
import java.awt.*;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;

public class Measurement implements Runnable {

    private Thread t;
    private boolean stopMeasurment;

    private SignalInput signalInput;
    private DataHandling dataHandling;
    private MainCalculations mainCalculations;
    private MainCalculationsImpulse mainCalculationsImpulse;
    private Save save;

    private JLabel stateText;
    private JList leqList;

    private boolean maxValues;
    private ArrayList<Double> maxList;

    /** Zmienne parametrów pomiaru */
    private int sampleRate;
    private FrequencyWeighting fWeighting;
    private WindowFunction wFunction;
    private String sampleWeighting;
    private double[] interpolated;

    /**
     *  Konstruktor klasy pomiaru.
     * @param stateText Pole tekstowe do wypisywania komunikatów o pomiarze
     * @param leqList   Lista do wypisywania Leq dla kolejnych dostępnych kanałów
     */
    public Measurement(JLabel stateText, JList leqList, boolean maxValues) {
        this.stateText = stateText;
        this.leqList = leqList;
        this.maxValues=maxValues;
        stopMeasurment=true;
    }

    /**
     *  Metoda rozpoczynająca pomiar.
     *  Wszystkie wartości podawane do metody MUSZĄ być sprawdzone przed ich przekazaniem.
     *
     *  @param sampleRate Częstotliwość próbkowania sygnału podczas pomiaru
     *  @param fWeighting Wybrana przez użytkownika krzywa korekcyjna
     *  @param wFunction  Wybrane przez użytkownika okno czasowe
     *  @param sampleWeighting Wybrany czas pobierania próbek
     */
    public void start(int sampleRate, FrequencyWeighting fWeighting, WindowFunction wFunction, String sampleWeighting, double[] interpolated){
        if(stopMeasurment) {
            if (t == null) {
                t = new Thread(this);
                t.start();
            }

            this.sampleRate = sampleRate;
            this.fWeighting = fWeighting;
            this.wFunction = wFunction;
            this.sampleWeighting = sampleWeighting;
            this.interpolated = interpolated;

            stopMeasurment = false;
        }
    }

    public boolean isRunning(){return !stopMeasurment;}

    /**
     * Metoda używana do zatrzymania wykonywania pomiaru
     */
    public void stop(){
        if(t!=null && t.isAlive())
            stopMeasurment=true;
    }

    /**
     * Zadanie wykonywane w wątku. Jego celem jest inicjalizacja obiektów potrzebnych do wykonania pomiaru oraz
     * uruchomienie stałego pomiaru aż do sygnalizacji jego zatrzymania. Metoda wypisuje wyniki w interfejsie.
     */
    @Override
    public void run() {
        String[] channels = AudioInfo.getChannels();
        ArrayList<Double> channelsOffset= new ArrayList<>();
        DefaultListModel<String> leqListModel= new DefaultListModel<>();

        for(int i=0; i<channels.length; i++){
            try {
                channelsOffset.add(Calibration.getCalibrationOffset(i));
            } catch (NullPointerException ne){
                stateText.setText("Brak kalibracji kanału: "+(i+1));
                return;
            }
        }

        int timeWeighting=0;

        switch (sampleWeighting){
            case "Impulse":
                timeWeighting=32;
                break;
            case "Fast":
                timeWeighting=125;
                break;
            case "Slow":
                timeWeighting=1000;
                break;
        }

        AudioFormat audioFormat = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, sampleRate, 16, AudioInfo.getChannels().length, 4, sampleRate, false);

        maxList= new ArrayList<>();

        for(int i=0; i<AudioInfo.getChannels().length; i++){
            maxList.add(0.0);
        }

        AudioData audioData= new AudioData(audioFormat, timeWeighting);
        Queue<byte[]> dataQueue= new LinkedList<>();
        signalInput = new SignalInput(
                dataQueue,
                audioFormat.getSampleRate(),
                audioFormat.getSampleSizeInBits(),
                audioFormat.getChannels(),
                audioFormat.isBigEndian());

        audioData.timeStamp = System.currentTimeMillis();
        dataHandling = new DataHandling(dataQueue, audioData);

        signalInput.Start();
        dataHandling.Start();

        if(sampleWeighting.equals("Impulse")){
            mainCalculationsImpulse= new MainCalculationsImpulse(audioData.Data, channelsOffset,channels.length);
            mainCalculationsImpulse.start();
            save = new Save(mainCalculationsImpulse, channels.length);
            
        } else {
            boolean sW= sampleWeighting.equals("Fast");
            mainCalculations = new MainCalculations(audioData.Data, fWeighting, wFunction,
                    interpolated, channels.length, sW, channelsOffset, sampleRate);

            mainCalculations.start();
            save = new Save(mainCalculations, mainCalculations.getReadyData(), channels.length, sW, fWeighting);
        }
        save.start();

        stateText.setText("Start");
        for (int i=0; i<channels.length; i++){
            leqListModel.add(i, String.valueOf(i));
        }

        leqList.setModel(leqListModel);
        DecimalFormat df = new DecimalFormat("#.0");

        while (!stopMeasurment){
            for(int i=0; i<channels.length; i++){
                double leq;
                double tmp;

                if(sampleWeighting.equals("Impulse")) {
                    leq = mainCalculationsImpulse.getLeqAll(i);
                    tmp = mainCalculationsImpulse.getLevelLast(i);
                } else {
                    leq = mainCalculations.getLeqAll(i);
                    tmp = mainCalculations.getLevelLast(i);
                }

                if(leq>maxList.get(i))
                    maxList.set(i, leq);

                if(maxValues)
                    leqListModel.set(i, "ch " + (i + 1) + ": " + df.format(maxList.get(i))+" dB | " + df.format(tmp)+" dB");
                else
                    leqListModel.set(i, "ch " + (i + 1) + ": " + df.format(leq)+" dB | " + df.format(tmp)+" dB");
            }



            try {
                Thread.sleep(100);
            } catch(InterruptedException ex) {
                Thread.currentThread().interrupt();
            }
        }

        signalInput.Stop();
        dataHandling.Stop();

        if(sampleWeighting.equals("Impulse")){
            mainCalculationsImpulse.stop();
        } else {
            mainCalculations.stop();
        }

        stateText.setText("Stop");

        stopMeasurment=false;
    }

    public void SwitchMaxAct(boolean maxValues){
        this.maxValues=maxValues;
    }
}
