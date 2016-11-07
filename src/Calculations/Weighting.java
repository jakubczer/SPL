package Calculations;

/**
 * Klasa uwzględniająca charakterystykę krzywej korekcyjnej A lub C. 
 * Metody wykorzystują realizację funkcyjną krzywych korekcyjnych.
 * Metody statyczne - nie tworzyć obiektu.
 * @author Patryk Głażewski
 */
public class Weighting {

    private static double[] weightA;
    private static double[] weightC;

    /**
     * Liczy wzmocnienia dla charakterystyki.
     * Metoda odpalana tylko raz w trakcie działania programu.
     * @param fast wartość określająca czy stała czasowa jest fast
     * @param samplingRate częstotliwość próbkowania
     * @param fw rodzaj krzywej charakterystyki (A lub C)
     */
    public static void countWeighting(boolean fast, int samplingRate, FrequencyWeighting fw){
        double fShift, funct, offset;
        double curFreq = 0;
        double[] x;
        if (fast) {
            fShift = samplingRate/8192.0;
            x= new double[8192];
        } else {
            fShift = samplingRate/65536.0;
            x= new double[65536];
        }
        x[0]=1;
        for (int i = 1; i < x.length/2; i++) {
            curFreq += fShift;
            if(fw==FrequencyWeighting.A) {
                funct = helpWeightA(curFreq);
            }
            else {
                funct = helpWeightC(curFreq);
            }
            x[i] = funct;
        }


        for (int i = x.length/2; i < x.length; i++) {
            x[i] = x[x.length-i-1];
        }

        if(fw==FrequencyWeighting.A)
            weightA =x;
        else
            weightC =x;
    }
    /**
     * Uwzględnia charakterystykę korekcyjną A.
     * @param x amplituda w dziedzinie częstotliwości
     * @return amplituda z uwzględnioną charakterystyką korekcyjną A
     */
    public static double[] WeightingA(double[] x) {

        for (int i = 0; i < x.length; i++) {
            x[i] *= weightA[i];
        }
        return x;
    }
/**
 * Uwzględnia charakterystykę korekcyjną C.
 * @param x amplituda w dziedzinie częstotliwości
 * @return amplituda z uwzględnioną charakterystyką korekcyjną C
 */
    public static double[] WeightingC(double[] x) {

        for (int i = 0; i < x.length; i++) {
            x[i] *= weightC[i];
        }
        return x;
    }

    /**
     * Metoda pomocnicza dla charakterystyki A
     * @param curFreq częstotliwość
     * @return waga dla danej częstotliwości
     */
    private static double helpWeightA(double curFreq){
        double funct = (Math.pow(12200, 2) * Math.pow(curFreq, 4))
                / ((Math.pow(curFreq, 2) + Math.pow(20.6, 2))
                * Math.sqrt(
                (Math.pow(curFreq, 2) + Math.pow(107.7, 2))
                        * (Math.pow(curFreq, 2) + Math.pow(727.9, 2))
        )
                * (Math.pow(curFreq, 2) + Math.pow(12200, 2)));
        funct *= Math.pow(10, 0.1);
        //offset = 2 + 20 * Math.log10(funct);
        return funct;
    }
    /**
     * Metoda pomocnicza dla charakterystyki A
     * @param curFreq częstotliwość
     * @return waga dla danej częstotliwości
     */
    private static double helpWeightC(double curFreq){

        double funct = (Math.pow(12200, 2) * Math.pow(curFreq, 2))
                / ((Math.pow(curFreq, 2) + Math.pow(20.6, 2))
                * (Math.pow(curFreq, 2) + Math.pow(12200, 2)));
        //offset = 0.06 + 20 * Math.log10(funct);
        return funct;

    }
}