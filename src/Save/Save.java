package Save;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.sql.Time;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.ConcurrentLinkedQueue;

import Calculations.FrequencyWeighting;
import Calculations.MainCalculations;
import Calculations.MainCalculationsImpulse;

/**
 * Klasa odpowiedzialna za zapis danych do pliku o rozszerzeniu *.csv.
 * Zapis jest wykonyway w oddzielnym wątku i na bieżąco.
 * Dane są pobierane z kolejek FIFO.
 * @author Arkadiusz Kubiak
 */
public class Save implements Runnable {

    private Thread t;
    private ArrayList<ConcurrentLinkedQueue<double[]>> inputData;
    private int channelCount;
    private MainCalculations mc;
    private MainCalculationsImpulse mci;
    private boolean impulse;
    private FrequencyWeighting charakterystyka;

    /**
     * Konstruktor klasy zapisu dla stałych czasowych fast lub slow.
     * @param mc obiekt klasy MainCalculation
     * @param synchronisedQueue lista kolejek z napływającymi danymi
     * @param channelCount liczba kanałów
     * @param fast wartość określająca czy stała czasowa jest fast
     * @param charakterystyka typ krzywej korekcyjnej
     * @author Arkadiusz Kubiak
     */
    public Save(MainCalculations mc, ArrayList<ConcurrentLinkedQueue<double[]>> synchronisedQueue, int channelCount, boolean fast, FrequencyWeighting charakterystyka)
    {
        inputData = synchronisedQueue;
        this.mc = mc;
        this.channelCount = channelCount;
        impulse = false;
        this.charakterystyka = charakterystyka;
    }

    /**
     * Konstruktor klasy zapisu dla stałych czasowych impulse.
     * @param mci obiekt klasy MainCalculationImpulse
     * @param channelCount liczba kanałów
     * @author Arkadiusz Kubiak
     */
    public Save(MainCalculationsImpulse mci, int channelCount)
    {
        this.mci = mci;
        this.channelCount = channelCount;
        impulse = true;
    }

    /**
     * Metoda tworząca wątek.
     * @author Arkadiusz Kubiak
     */
    public void start(){
        if (t == null)
        {
            t = new Thread (this);
            t.start();
        }
    }

    /**
     * Wątek w pętli przetwarza napływające dane. W momencie zakończenia działania wątku klasy MainCalculations
     * lub MainCalculationsImpulse pętla jest przerywana, a wątek zapisuje reszte danych, po czym kończy się.
     * @author Arkadiusz Kubiak
     */
    @Override
    public void run() {
        int currentChannel = 0;
        if (!impulse) {
            try {
                PrintWriter zapis = new PrintWriter("zapis.csv");
                int lp = 1;
                zapis.print("Data i godzina;Kanal;");
                for (int i = 1;i<=33;i++)
                    zapis.print("Tercja " + i + ";");
                zapis.println("Charakterystyka;Poziom;Poziom chwilowy");
                while (mc.isRunning()) {
                    if (!inputData.get(currentChannel).isEmpty()) {
                        double[] currentData = inputData.get(currentChannel).poll();
                        zapis.print(GetTime() + ";" + (currentChannel + 1) + ";");
                        for (int i = 0; i < currentData.length; i++)
                            if (currentData[i] != -1000)
                                zapis.print(currentData[i] + "dB;");
                            else
                                zapis.print("Brak poziomu dla danej tercji;");
                        zapis.println(FW(charakterystyka) + ";" + mc.getLeqAll(currentChannel) + "dB;" + mc.getLevelLast(currentChannel) + "dB;");
                    }
                    currentChannel++;
                    if (currentChannel >= channelCount) currentChannel = 0;
                    Thread.yield();
                }
                //Po zakończeniu pracy wątku MainCalculations
                currentChannel = 0;
                boolean end = true;
                while (end) {
                    if (!inputData.get(currentChannel).isEmpty()) {
                        double[] currentData = inputData.get(currentChannel).poll();
                        zapis.print(GetTime() + ";" + (currentChannel + 1) + ";");
                        for (int i = 0; i < currentData.length; i++)
                            if (currentData[i] != -1000)
                                zapis.print(currentData[i] + "dB;");
                            else
                                zapis.print("Brak poziomu dla danej tercji;");
                        zapis.println(FW(charakterystyka) + ";" + mc.getLeqAll(currentChannel) + "dB;" + mc.getLevelLast(currentChannel) + "dB;");
                    }
                    currentChannel++;
                    if (currentChannel >= channelCount) {
                        end = false;
                    }
                }
                zapis.close();
            } catch (FileNotFoundException e) {
            }
        } else {
            //IMPULSE
            try {
                PrintWriter zapis = new PrintWriter("zapis.csv");
                zapis.println("Data i godzina;Kanal;Poziom;Poziom chwilowy");
                while(mci.isRunning()) {
                    t.sleep(1000);
                    for (int i = 0; i < channelCount; i++)
                        zapis.println(GetTime() + ";" + (1 + i) + ";" + mci.getLeqAll(i) + "dB;" + mci.getLevelLast(i) +  "dB");
                }
                zapis.close();
            } catch (FileNotFoundException e) {}
            catch (InterruptedException e) {}
        }
    }

    /**
     * Metoda zamieniająca podaną wartość częstotliwości na pasmo tercji.
     * @param szukana wartość podanej częstotliwości
     * @return pasmo tercji lub -1, gdy podana częstotliwość nie znajduje się w tabeli pasm tercji
     * @author Arkadiusz Kubiak
     */
    private static double FiltrTercjowy(double szukana) {
        double[][] tablica =
                {
                        new double[]{1, 11.1, 14.0},
                        new double[]{2, 14.3, 18.0},
                        new double[]{3, 17.8, 22.4},
                        new double[]{4, 22.3, 28.1},
                        new double[]{5, 28.1, 35.4},
                        new double[]{6, 35.6, 44.9},
                        new double[]{7, 44.5, 56, 1},
                        new double[]{8, 56.1, 70.7},
                        new double[]{9, 71.3, 89, 8},
                        new double[]{10, 89.1, 112.2},
                        new double[]{11, 111.4, 140.3},
                        new double[]{12, 142.5, 179.6},
                        new double[]{13, 178, 2, 224.5},
                        new double[]{14, 222.7, 280.6},
                        new double[]{15, 280.6, 353.6},
                        new double[]{16, 356.4, 449.0},
                        new double[]{17, 445.4, 561.2},
                        new double[]{18, 561.3, 707.2},
                        new double[]{19, 712.7, 898.0},
                        new double[]{20, 890.9, 1122.5},
                        new double[]{21, 1113.6, 1403.1},
                        new double[]{22, 1425.4, 1795.9},
                        new double[]{23, 1781.8, 2244.9},
                        new double[]{24, 2227.2, 2806.2},
                        new double[]{25, 2806.3, 3535.8},
                        new double[]{26, 3563.6, 4489.8},
                        new double[]{27, 4454.5, 5612.3},
                        new double[]{28, 5612.7, 7071.5},
                        new double[]{29, 7127.2, 8979.7},
                        new double[]{30, 8909.0, 11224.6},
                        new double[]{31, 11136.2, 14030.8},
                        new double[]{32, 14254.4, 17959.4},
                        new double[]{33, 17818.0, 22449.2}
                };

        int lewo = 0, prawo = tablica.length - 1, srodek = 0;

        while (lewo <= prawo) {
            srodek = (lewo + prawo) / 2;
            if (tablica[srodek][1] <= szukana && tablica[srodek][2] >= szukana)
                return tablica[srodek][0];
            else if (tablica[srodek][1] < szukana)
                lewo = srodek + 1;
            else
                prawo = srodek - 1;
        }
        return -1;
    }


    /**
     * Metoda pobierająca datę i czas systemowy.
     * @return aktualna data i czas
     * @author Arkadiusz Kubiak
     */
    private static String GetTime() {
        Date currentDate = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
        String dateString = dateFormat.format(currentDate);
        return  dateString;
    }

    /**
     * Metoda zwracająca zmienną typu string z typem krzywej korekcyjnej
     * @return typ krzywej korekcyjnej
     * @author Arkadiusz Kubiak
     */
    private static String FW(FrequencyWeighting fWeighting){
        if (fWeighting == FrequencyWeighting.Z) return "Z";
        if (fWeighting == FrequencyWeighting.A) return "A";
        if (fWeighting == FrequencyWeighting.C) return "C";
        return "?";
    }
}



