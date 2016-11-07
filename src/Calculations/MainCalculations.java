package Calculations;

import java.util.ArrayList;
//import java.util.List;
//import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Klasa odpowiedzialna za obliczenia dla stałych czasowych fast lub slow.
 * Obliczenia są wykonywane w oddzielnym wątku i na bieżąco. Dane są pobierane z
 * kolejek FIFO. Obliczenia są wykonywane w następującej kolejności: -
 * przygotowanie do FFT - FFT - wyciągnięcie amplitudy z transformaty -
 * naniesienie korekcji mikrofonu - naniesienie korekcji krzywej charakterystyki
 * - zamiana danych na poziomy w dziedzinie częstotliwości - uwzględnienie
 * kalibracji - zapisanie "ramki" danych do kolejki FIFO oraz poziomu do obiektu
 * klasy LevelEq
 * @author Patryk Głażewski, Mateusz Tracz
 */
public class MainCalculations implements Runnable {

    private Thread t;
    private volatile boolean stopThread = false;

    private ArrayList<ConcurrentLinkedQueue<double[]>> inputData;

    //wzmocnienia z kalibracji na każdy z kanałów
    private ArrayList<Double> calibAmps;

    // tablica kolejek, każdy kanał ma swoją kolejkę
    private ArrayList<ConcurrentLinkedQueue<double[]>> readyData = new ArrayList<>();
    //obiekt z równoważnymi poziomami dźwięku  
    private LevelEq Leq;

    private FrequencyWeighting chosenWeighting;
    private WindowFunction chosenWindow;

    private double[] microphoneEqData;

    private int channelCount;
    private int samplingRate;

    private boolean fast;
    //moc odniesienia
    public static final double REF_POWER = 1;

    /**
     * Konstruktor klasy obliczeń.
     *
     * @param synchronisedQueue lista kolejek z napływającymi danymi
     * @param frequencyWeighting wybór krzywej korekcyjnej
     * @param windowFunction wybór okna czasowego
     * @param microphoneEqData tablica ze wzmocnieniami wynikającymi z korekcji
     * mikrofonu
     * @param channelCount liczba kanałów
     * @param fast wartość określająca czy stała czasowa jest fast
     * @param calibAmps lista z offsetami związanymi z kalibracją
     */
    public MainCalculations(ArrayList<ConcurrentLinkedQueue<double[]>> synchronisedQueue,
            FrequencyWeighting frequencyWeighting,
            WindowFunction windowFunction,
            double[] microphoneEqData,
            int channelCount, boolean fast,
            ArrayList<Double> calibAmps, int samplingRate) {
        inputData = synchronisedQueue;

        chosenWeighting = frequencyWeighting;
        chosenWindow = windowFunction;

        this.microphoneEqData = microphoneEqData;
        this.channelCount = channelCount;
        this.fast = fast;
        this.calibAmps = calibAmps;
        this.samplingRate = samplingRate;
        Leq = new LevelEq(this.channelCount);
        
        // do danych gotowych
        for (int i = 0; i < channelCount; i++) {
            readyData.add(new ConcurrentLinkedQueue<double[]>());
        }
    }

    /**
     * Zwraca pierwszą ramkę z kolejki gotowych danych dla konkretnego kanału.
     *
     * @param channelNumber numer aktualnego kanału
     * @return ramka z przetworzonymi danymi dla danego kanału
     */
    public double[] getFirstReadyData(int channelNumber) {
        return getReadyData().get(channelNumber).poll();
    }    
    /**      
     * @return lista z kolejkami z gotowymi danymi
     */
    public ArrayList<ConcurrentLinkedQueue<double[]>> getReadyData() {
        return readyData;
    }

    /**
     * Zwraca poziom równoważny dla x ostatnich ramek dla danego kanału.
     *
     * @param x liczba ramek z których pobieramy poziom równoważny
     * @param channelNr numer kanału
     * @return poziom równoważny
     */
    public double getLeqFromLastX(int x, int channelNr) {
        return Leq.countLeq(x, channelNr);
    }

    /**
     * Zwraca poziom równoważny z całych dotychczasowych danych dla danego kanału.
     *
     * @param channelNr numer kanału
     * @return poziom równoważny
     */
    public double getLeqAll(int channelNr) {
        return Leq.getLevelAll(channelNr);
    }

    /**
     * Zwraca poziom chwilowy dla danego kanału (z ostatniej ramki).
     * @param channelNr numer kanału
     * @return poziom chwilowy
     */
    public double getLevelLast(int channelNr) {return Leq.getLevelLast(channelNr);}

    /**
     * Metoda tworząca wątek.
     */
    public void start() {
        if (t == null) {
            t = new Thread(this);
            t.start();
        }
    }

    /**
     * Metoda zatrzymująca wątek.
     *
     * @author Patryk Głażewski
     */
    public void stop() {
        if (t != null && t.isAlive()) {
            stopThread = true;
        }
    }

    /**
     * Metoda sprawdza czy wątek żyje.
     *
     * @return wartość logiczna, określająca czy wątek żyje
     * @author Patryk Głażewski
     */
    public boolean isRunning() {
        return t.isAlive();
    }

    /**
     * Wątek w pętli przetwarza napływające dane. W momencie gdy program chce
     * zabić wątek, liczy do końca dane i kończy działanie.
     */
    @Override
    public void run() {

        if(chosenWeighting!=FrequencyWeighting.Z)
            Weighting.countWeighting(fast,samplingRate,chosenWeighting);
        int currentChannel = 0;
        while (!stopThread) {
            if (!inputData.get(currentChannel).isEmpty()) {
                calcLogic(currentChannel);
            }

            currentChannel++;
            if (currentChannel >= channelCount) {
                currentChannel = 0;
            }

            // gdyby nie było tego wywłaszczenia wątek zajmowałby 100% czasu procesora nawet gdy FIFO byłoby puste
            Thread.yield();
        }
        //w chwili gdy "stopujemy", liczymy reszte danych ktore zdazylo pobrac
        currentChannel = 0;
        while (true) {
            while (!inputData.get(currentChannel).isEmpty()) {
                calcLogic(currentChannel);
            }
            currentChannel++;
            if (currentChannel >= channelCount) {
                break;
            }
        }
    }

    /**
     * Funkcja wyliczająca amplitudę z próbki danych
     *
     * @return policzona amplituda
     * @param inputData tablica danych typu zespolonego
     * @author  Mateusz Tracz
     */
    public static double[] extractAmplitude(Complex[] inputData) {
        double[] result = new double[inputData.length];

        for (int i = 0; i < result.length; i++) {
            result[i] = inputData[i].abs();
        }

        return result;
    }

    /**
     * Funkcja nakładająca korekcję mikrofonu na dane
     *
     * @return dane po nałożeniu korekcji
     * @param inputData tablica danych typu double
     * @author  Mateusz Tracz
     */
    private double[] microphoneEqualisation(double[] inputData) {
        for (int i = 0; i < inputData.length; i++) {
            inputData[i] *= microphoneEqData[i];
        }

        return inputData;
    }

    private double[] powerFromAmplitude(double[] inputData){
        double[] outputData = new double[inputData.length];
        for (int i = 0; i < inputData.length; i++) {
            outputData[i] = inputData[i]*inputData[i];
        }
        return outputData;
    }

    private double sumOfPowers(double[] powersArray){
        double sum = 0;
        for (Double d: powersArray) {
            sum +=d;
        }
        return sum;
    }

    /**
     * Metoda zawierająca główną logikę obliczeń
     *
     * @param currentChannel numer atkualnego kanału wejściowego
     */
    private void calcLogic(int currentChannel) {
        double[] currentData = inputData.get(currentChannel).poll();
        int origLength = currentData.length;
        Complex[] preparedData = PreparingFFT.DataPreparingForFFT(currentData, chosenWindow);
        Complex[] afterFFT = FFT.fft(preparedData);
        double[] amplitudeData = extractAmplitude(afterFFT);
        if (chosenWeighting == FrequencyWeighting.A) //krzywa A
        {
            amplitudeData = Weighting.WeightingA(amplitudeData);
        }
        if (chosenWeighting == FrequencyWeighting.C) //krzywa C
        {
            amplitudeData = Weighting.WeightingC(amplitudeData);
        }
        double[] weightedData = microphoneEqualisation(amplitudeData);
        double[] powerData = powerFromAmplitude(weightedData);
        double powerSum = sumOfPowers(powerData)*((double)powerData.length/(double)origLength);
        double[] filters = Filters.filter(powerData, calibAmps.get(currentChannel), fast, samplingRate);
        getReadyData().get(currentChannel).add(filters);
        Leq.addLevel(powerSum, currentChannel, calibAmps.get(currentChannel));
    }


}
