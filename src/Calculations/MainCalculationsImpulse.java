package Calculations;


import java.util.ArrayList;
import java.util.concurrent.ConcurrentLinkedQueue;

/**Klasa odpowiedzialna za obliczenia dla stałej czasowej impulse.
 * Obliczenia są wykonywane w oddzielnym wątku i na bieżąco. 
 * Dane są pobierane z kolejek FIFO.
 * Dane są poddawane tylko zamianie na poziom i naniesieniu kalibracji, a
 * następnie są zapisywane do obiektu klasy LevelEq.
 * @author Patryk Głażewski
 */
public class MainCalculationsImpulse implements Runnable {

    private Thread t;
    private volatile boolean stopThread = false;
    private ArrayList<ConcurrentLinkedQueue<double[]>> inputData;
    

    //wzmocnienia z kalibracji na każdy z kanałów
    private ArrayList<Double> calibAmps;
    
    //obiekt z równoważnymi poziomami dźwięku  
    private LevelEq Leq;    

    private int channelCount;    
    /**
     * Konstruktor klasy obliczeń.
     * @param synchronisedQueue lista kolejek z napływającymi danymi
     * @param calibAmps lista z offsetami związanymi z kalibracją
     * @param channelCount liczba kanałów
     */
    public MainCalculationsImpulse(ArrayList<ConcurrentLinkedQueue<double[]>> synchronisedQueue, 
                                   ArrayList<Double> calibAmps, int channelCount)
    {
        inputData = synchronisedQueue;        
        this.channelCount = channelCount;       
        this.calibAmps = calibAmps;
        
        Leq = new LevelEq(this.channelCount);

        // do danych gotowych używamy obiektu Leq - bezpośrednio z niego pobieramy poziomy        
    }
    
    /**
     * Zwraca poziom równoważny dla x ostatnich ramek.
     * @param x liczba ramek z których pobieramy poziom równoważny
     * @param channelNr numer kanału
     * @return poziom równoważny
     */
    public double getLeqFromLastX(int x, int channelNr){
        return Leq.countLeq(x, channelNr);
    }
    /**
     * Zwraca poziom równoważny z całych dotychczasowych danych.
     * @param channelNr numer kanału
     * @return poziom równoważny
     */
    public double getLeqAll(int channelNr){
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
    public void start(){
        if (t == null)
        {
            t = new Thread (this);
            t.start ();
        }
    }
    
    /**
     * Metoda zatrzymująca wątek.
     * @author Patryk Głażewski
     */
    public void stop() {
        if(t!=null&&t.isAlive())
            stopThread = true;
    }    
    
    /**
     * Metoda sprawdza czy wątek żyje.
     * @return wartość logiczna, określająca czy wątek żyje
     * @author Patryk Głażewski
     */
    public boolean isRunning(){
        return t.isAlive();
    }

     /**
     * Wątek w pętli przetwarza napływające dane. W momencie gdy program chce zabić
     * wątek, liczy do końca dane i kończy działanie.
     */
    @Override
    public void run() {

        int currentChannel = 0;
        while (!stopThread)
        {
            if (!inputData.get(currentChannel).isEmpty())
            {
                double[] currentData = inputData.get(currentChannel).poll();                               
                Leq.addLevelRaw(currentData, calibAmps.get(currentChannel), currentChannel);            
                                
            }            
            currentChannel++;
            if (currentChannel >= channelCount) currentChannel = 0;

            // gdyby nie było tego wywłaszczenia wątek zajmowałby 100% czasu procesora nawet gdy FIFO byłoby puste
            Thread.yield();
        }
        //w chwili gdy "stopujemy", liczymy reszte danych ktore zdazylo pobrac
        currentChannel = 0;
        while (true) {
            while (!inputData.get(currentChannel).isEmpty()) {
                double[] currentData = inputData.get(currentChannel).poll();                               
                Leq.addLevelRaw(currentData, calibAmps.get(currentChannel), currentChannel);
            }
            currentChannel++;
            if (currentChannel >= channelCount) {
                break;
            }
        }
    }    
    
   
}
