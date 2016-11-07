package Calculations;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Klasa "wrapper" dla listy zawierającej gotowe dane i obliczająca poziomy
 * równoważne.
 *
 * @author Patryk Głażewski
 */
public class LevelEq {

    private final List<List<Double>> levels = new ArrayList<List<Double>>();
    //suma wszystkich poziomów do tej pory
    private final double[] levelsSum;
    private final int[] levelsCounters;
    private final Object lock1 = new Object(); //lock do zapisu/odczytu z tablic
    //moc odniesienia 1
    private final double REF_POWER = 1;

    public LevelEq(int channelCount) {

        levelsSum = new double[channelCount];
        levelsCounters = new int[channelCount];
        for (int i = 0; i < channelCount; i++) {
            levels.add(Collections.synchronizedList(new ArrayList<Double>()));
        }
    }

    /**
     * Metoda dodająca poziom do listy z ramki poziomów w dziedzinie
     * częstotliwości.
     *
     * @param data ramka poziomów w dziedzinie częstotliwości
     * @param channelNr numer kanału
     */
    public void addLevel(double data, int channelNr, double calib) {
        double level = PreparingFFT.Power_dB(data, REF_POWER);
        level += calib;
        levels.get(channelNr).add(level);
        synchronized (lock1) {
            levelsSum[channelNr] += level;
            levelsCounters[channelNr] += 1;
        }
    }

    /**
     * Metoda dodająca poziom do listy z ramki nieprzetworzonych danych i
     * uwzględniająca kalibrację.
     *
     * @param data nieprzetworzony sygnał
     * @param calib offset wynikający z kalibracji
     * @param channelNr numer kanału
     */
    public void addLevelRaw(double[] data, double calib, int channelNr) {
        double output = PreparingFFT.Power_dB(PreparingFFT.Power(data), REF_POWER);
        levels.get(channelNr).add(output + calib);
        synchronized (lock1) {
            levelsSum[channelNr] += (output + calib);
            levelsCounters[channelNr] += 1;
        }
    }

    /**
     * Liczy poziom równoważny z x ostatnich "ramek". Nie jest używana w programie!
     *
     * @param x liczba ramek z których chcemy obliczyć poziom równoważny
     * @param channelNr numer kanału
     * @return poziom równoważny
     */
    public double countLeq(int x, int channelNr) {
        if (levels.get(channelNr).isEmpty()) {
            return 0;
        }
        double sum = 0;
        int i = 0;
        try {
            for (; i < x; i++) {
                sum += levels.get(channelNr).get(levels.get(channelNr).size() - i - 1);
            }
        } catch (IndexOutOfBoundsException e) {
        }
        //jak rzuci wyjątkiem - tzn, że koniec listy
        return sum / i;
    }

    /**
     * Zwraca poziom równoważny z całych dotychczasowych danych.
     *
     * @param channelNr numer kanału
     * @return poziom równoważny
     */
    public double getLevelAll(int channelNr) {
        if (levels.get(channelNr).isEmpty()) {
            return 0;
        }
        synchronized (lock1) {
            return levelsSum[channelNr] / levelsCounters[channelNr];
        }
    }

    /**
     * Zwraca poziom chwilowy (z ostatniej ramki).
     * @param channelNr numer kanału
     * @return poziom chwilowy
     */
    public double getLevelLast (int channelNr){
        if(levels.get(channelNr).isEmpty())
            return 0;
        return levels.get(channelNr).get(levels.get(channelNr).size()-1);
    }
}
