package CalibrationPKG;

import Audio.AudioInfo;
import Calculations.*;
import Interface.CalibrationView;
import SignalInput.SignalInput;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.TargetDataLine;
import javax.swing.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Klasa odpowiedzialna za kalibrację.
 * Po wykonaniu metody calibrate przesunięcie kalibracyjne przechowywane jest w strukturze ConcurrentHashMap.
 *
 * @author  Mateusz Tracz
 */
public class Calibration{

    // różnica
    public static double getCalibrationOffset(int channelNumber) {
        return calibrationOffsets.get(channelNumber);
    }

    private static ConcurrentHashMap<Integer, Double> calibrationOffsets = new ConcurrentHashMap<>();

    public static void setTargetDataLine(TargetDataLine targetDataLine) {
        Calibration.targetDataLine = targetDataLine;
    }

    public static boolean isNeedNewTargetData() {
        return needNewTargetData;
    }

    public static void setNeedNewTargetData(boolean needNewTargetData) {
        Calibration.needNewTargetData = needNewTargetData;
    }

    public double getMaximumAvailableInput(int channelNumber) {
        return maximumAvailableInput.get(channelNumber);
    }

    private static ConcurrentHashMap<Integer, Double> maximumAvailableInput = new ConcurrentHashMap<>();

    public static double getCalibratorValue() {
        return calibratorValue;
    }

    public static void setCalibratorValue(double calibratorValue) {
        Calibration.calibratorValue = calibratorValue;
    }

    // wartość wzorcowa z kalibratora
    private static double calibratorValue = 0;

    public SignalInput signalInput;

    public static TargetDataLine getTargetDataLine() {
        return targetDataLine;
    }

    private static TargetDataLine targetDataLine;
    private static boolean needNewTargetData;


    /**
     * Funkcja do pobierania ostatnich danych jakie pobrano z obiektu SignalInput
     *
	 * @author Mateusz Tracz
     * @param channel Numer kanału z którego chcę pobrać dane
     * @return Wartość poziomu dla kanału channel
     */
    public Double[] getLastRawInput(int channel) {
        AudioFormat audioFormat = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED,
                                                  44100,
                                                  16,
                                                  AudioInfo.getChannels().length,
                                                  4,
                                                  44100,
                                                  false);

        AudioData audioData = new AudioData(audioFormat,125);
        Queue<byte[]> queue = new LinkedList<>();

        if (targetDataLine == null) {
            needNewTargetData = true;
        }
        else needNewTargetData = false;

        signalInput = new SignalInput(
                queue,
                audioFormat.getSampleRate(),
                audioFormat.getSampleSizeInBits(),
                audioFormat.getChannels(),
                audioFormat.isBigEndian());
        targetDataLine = signalInput.getTargetLine();



        audioData.timeStamp = System.currentTimeMillis();
        DataHandling dataHandling = new DataHandling(queue, audioData);

        signalInput.Start();
        dataHandling.Start();


        //zbieramy dane przez 2 sekundy
        try {
            if (CalibrationView.isClicked()){
                JProgressBar progressBar = CalibrationView.getCalibRef();
                for(int i = 0; i < 100; i++)
                {
                    Thread.sleep(20);
                    progressBar.setValue(i+1);
                }
            }
            else Thread.sleep(2000);
        } catch(InterruptedException ex) {
            Thread.currentThread().interrupt();
        }

        signalInput.Stop();
        dataHandling.Stop();

        ArrayList<Double> ret = new ArrayList<>();
        while(!audioData.Data.get(channel).isEmpty()){
            double[] data = audioData.Data.get(channel).poll();
            for (int i = 0; i < data.length; i++){
                ret.add(data[i]);
            }
        }
        return ret.toArray(new Double[ret.size()]);
    }

    /**
     * Funkcja ROZPOCZYNAJĄCA kablibrację. Wartość kalibracji (różnica między wartością podawaną
     * przez kalibrator a odczytaną) jest po zakończeniu zapisywana w zmienne lokalnej. Co jakiś czas musi być ustawiany postęp.
     *
	 * @author Mateusz Tracz
     * @param channel Numer kanału do kalibracji
     */
    public void calibrate(int channel) {
        Double[] input = getLastRawInput(channel);

        Double currentPower = PreparingFFT.Power(input);

        Double currentDecibels = PreparingFFT.Power_dB(currentPower, MainCalculations.REF_POWER);

        calibrationOffsets.put(channel, calibratorValue - currentDecibels);
        maximumAvailableInput.put(channel, currentDecibels * -1);
    }
}
