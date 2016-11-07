package Calculations;

/**
 * Klasa odpowiadająca za aplikacje filtrów tercjowych. Na wejściu przyjmujemy
 * tablicę mocy w dziedzinie częstotliwości, na wyjściu otrzymujemy tablicę
 * z poziomami podzielonymi na tercje. *
 *
 * @author Patryk Głażewski
 */
public class Filters {

    private final static double REF_POWER = 1;


    /**
     * Metoda przyjmująca tablicę mocy przydzielająca poziomy do konkretnych tercji
     *
     * i=0 dla (11.1, 14.0) Hz
     * i=1 dla (14.3, 18.0) Hz
     * i=2 dla (17.8, 22.4) Hz
     * i=3 dla (22.3, 28.1) Hz
     * i=4 dla (28.1, 35.4) Hz
     * i=5 dla (35.6, 44.9) Hz
     * i=6 dla (44.5, 56.1) Hz
     * i=7 dla (56.1, 70.7) Hz
     * i=8 dla (71.3, 89.8) Hz
     * i=9 dla (89.1, 112.2) Hz
     * i=10 dla (111.4, 140.3) Hz
     * i=11 dla (142.5, 179.6) Hz
     * i=12 dla (178, 2, 224.5) Hz
     * i=13 dla (222.7, 280.6) Hz
     * i=14 dla (280.6, 353.6) Hz
     * i=15 dla (356.4, 449.0) Hz
     * i=16 dla (445.4, 561.2) Hz
     * i=17 dla (561.3, 707.2) Hz
     * i=18 dla (712.7, 898.0) Hz
     * i=19 dla (890.9, 1122.5) Hz
     * i=20 dla (1113.6, 1403.1) Hz
     * i=21 dla (1425.4, 1795.9) Hz
     * i=22 dla (1781.8, 2244.9) Hz
     * i=23 dla (2227.2, 2806.2) Hz
     * i=24 dla (2806.3, 3535.8) Hz
     * i=25 dla (3563.6, 4489.8) Hz
     * i=26 dla (4454.5, 5612.3) Hz
     * i=27 dla (5612.7, 7071.5) Hz
     * i=28 dla (7127.2, 8979.7) Hz
     * i=29 dla (8909.0, 11224.6) Hz
     * i=30 dla (11136.2, 14030.8) Hz
     * i=31 dla (14254.4, 17959.4) Hz
     * i=32 dla (17818.0, 22449.2) Hz
     *
     *
     * @param powers tablica z mocami w dziedzinie częśtotliwości
     * @param calib offset kalibracji
     * @param fast true dla fast, false dla slow
     * @return tablica z poziomami podzielonymi na filtry tercjowe
     */
    public static double[] filter(double[] powers, double calib, boolean fast, int samplingRate){
        double[] filters = new double[33];
        double curFreq=0;
        double freqShift;

        if(fast)
            freqShift=samplingRate/8192.0;
        else
            freqShift=samplingRate/65536.0;

        for (int i = 0; i < powers.length; i++) {
            if(curFreq>=11.1&&curFreq<=14.0)
                filters[0]+=powers[i];
            if(curFreq>=14.3&&curFreq<=18.0)
                filters[1]+=powers[i];
            if(curFreq>=17.8&&curFreq<=22.4)
                filters[2]+=powers[i];
            if(curFreq>=22.3&&curFreq<=28.1)
                filters[3]+=powers[i];
            if(curFreq>=28.1&&curFreq<=35.4)
                filters[4]+=powers[i];
            if(curFreq>=35.6&&curFreq<=44.9)
                filters[5]+=powers[i];
            if(curFreq>=44.5&&curFreq<=56.1)
                filters[6]+=powers[i];
            if(curFreq>=56.1&&curFreq<=70.7)
                filters[7]+=powers[i];
            if(curFreq>=71.3&&curFreq<=89.8)
                filters[8]+=powers[i];
            if(curFreq>=89.1&&curFreq<=112.2)
                filters[9]+=powers[i];
            if(curFreq>=111.4&&curFreq<=140.3)
                filters[10]+=powers[i];
            if(curFreq>=142.5&&curFreq<=179.6)
                filters[11]+=powers[i];
            if(curFreq>=178.2&&curFreq<=224.5)
                filters[12]+=powers[i];
            if(curFreq>=222.7&&curFreq<=280.6)
                filters[13]+=powers[i];
            if(curFreq>=280.6&&curFreq<=353.6)
                filters[14]+=powers[i];
            if(curFreq>=356.4&&curFreq<=449.0)
                filters[15]+=powers[i];
            if(curFreq>=445.4&&curFreq<=561.2)
                filters[16]+=powers[i];
            if(curFreq>=561.3&&curFreq<=707.2)
                filters[17]+=powers[i];
            if(curFreq>=712.7&&curFreq<=898.0)
                filters[18]+=powers[i];
            if(curFreq>=890.9&&curFreq<=1122.5)
                filters[19]+=powers[i];
            if(curFreq>=1113.6&&curFreq<=1403.1)
                filters[20]+=powers[i];
            if(curFreq>=1425.4&&curFreq<=1795.9)
                filters[21]+=powers[i];
            if(curFreq>=1781.8&&curFreq<=2244.9)
                filters[22]+=powers[i];
            if(curFreq>=2227.2&&curFreq<=2806.2)
                filters[23]+=powers[i];
            if(curFreq>=2806.3&&curFreq<=3535.8)
                filters[24]+=powers[i];
            if(curFreq>=3563.6&&curFreq<=4489.8)
                filters[25]+=powers[i];
            if(curFreq>=4454.5&&curFreq<=5612.3)
                filters[26]+=powers[i];
            if(curFreq>=5612.7&&curFreq<=7071.5)
                filters[27]+=powers[i];
            if(curFreq>=7127.2&&curFreq<=8979.7)
                filters[28]+=powers[i];
            if(curFreq>=8909.0&&curFreq<=11224.6)
                filters[29]+=powers[i];
            if(curFreq>=11136.2&&curFreq<=14030.8)
                filters[30]+=powers[i];
            if(curFreq>=14254.4&&curFreq<=17959.4)
                filters[31]+=powers[i];
            if(curFreq>=17818.0&&curFreq<=22449.2)
                filters[32]+=powers[i];

            curFreq+=freqShift;
        }
        filters = changeToLevels(filters, calib);
        return filters;
    }

    private static double[] changeToLevels(double[] powerSums, double calib){

        for (int i = 0; i < powerSums.length ; i++) {
            if(powerSums[i]==0){
                powerSums[i]=-1000;                           //jeśli nie ma poziomu dla danych czestotliwosc, magic number to -1000 - DO UWZGLEDNIENIA W ZAPISIE
                continue;
            }
            powerSums[i] = PreparingFFT.Power_dB(powerSums[i],REF_POWER);
            powerSums[i] += calib;
        }
        return powerSums;
    }

}
