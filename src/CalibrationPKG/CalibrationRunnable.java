package CalibrationPKG;


import Interface.CalibrationView;

import javax.swing.*;
import java.awt.*;
import java.text.DecimalFormat;

/**
 * Klasa pomocnicza przy tworzeniu wątku używanego przy kalibracji
 * @author  Mateusz Tracz
 */
public class CalibrationRunnable implements Runnable{

    public volatile Thread blinker;
    private volatile JTextField calibratorVal;
    private volatile JSpinner spinner1;
    private volatile JProgressBar calibRef;
    private volatile JProgressBar calibPB;
    private volatile Calibration calibration;
    private volatile JLabel maximumValue;
    private volatile JProgressBar maxPB;

    /**
     * Konstruktor klasy CalibrationRunnable
     * @author Mateusz Tracz
     */
    public CalibrationRunnable(JTextField calibratorVal, JSpinner spinner1, JProgressBar calibRef, JProgressBar calibPB, Calibration calibration, JLabel maximumValue, JProgressBar maxPB) {
        this.calibratorVal = calibratorVal;
        this.spinner1 = spinner1;
        this.calibRef = calibRef;
        this.calibPB = calibPB;
        this.calibration = calibration;
        this.maximumValue = maximumValue;
        this.maxPB = maxPB;
    }

    /**
     * Funkcja startująca wątek
     */
    public void startThread() {
        blinker = new Thread(this);
        blinker.start();
    }

    /**
     * Funkcja zatrzymująca wątek
     */
    public void stopThread() {
        blinker.stop();
        blinker = null;
    }

    /**
     * Główna pętla wątku
     */
    @Override
    public void run() {
        Thread thread = Thread.currentThread();
        while (blinker == thread) {

            int calibratorValue = 0;
            if (!calibratorVal.getText().equals("")) calibratorValue = Integer.parseInt(calibratorVal.getText());

            int channel = Integer.parseInt((String) spinner1.getValue()) - 1;

            // żeby nie zmieniać metody calibrate
            CalibrationView.clicked = false;
            calibRef = calibPB;

            Calibration.setCalibratorValue(calibratorValue);
            calibration.calibrate(channel);

            DecimalFormat df = new DecimalFormat("#.0");
            String output = df.format(calibration.getMaximumAvailableInput(channel)) + " dB";
            maximumValue.setText(output);

            int maxVal = (int) calibration.getMaximumAvailableInput(channel);
            if (maxVal > 100) {
                maxPB.setValue(100);
                maxPB.setForeground(new Color(0, 255, 0));
            }
            else {
                maxPB.setValue(maxVal);
                if (maxVal < 50) maxPB.setForeground(new Color(255, maxVal*255/50,0));
                else maxPB.setForeground(new Color(255 - (maxVal-50)*255/50,255,0));
            }

            Thread.yield();
        }
}}
