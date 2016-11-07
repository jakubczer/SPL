package Calculations;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.FileReader;
import java.io.FileNotFoundException;

/**
 * Klasa licząca wzmocnienia związane z charakterystyką mikrofonu. Z pliku .csv
 * podanego przez użytkownika pobiera charakterystyczne punkty i na ich
 * podstawie oblicza potrzebne wzmocnienia dla wszystkich częstotliwości które
 * nas interesują, za pomocą trzypunktowej interpolacji lagrange'a (wyszukując
 * dla każdej częstotliwości trzy najbliższe punkty z tych podanych przez
 * użytkownika). Metody statyczne - nie tworzyć obiektu.
 *
 * @author Patryk Głażewski
 */
public class Interpolation {

    /**
     * Główna metoda klasy, wywołuje wszystkie inne metody w odpowiedniej
     * kolejności.
     *
     * @param samplingRate częstotliwość próbkowania
     * @param fast wartość określająca czy stała czasowa jest fast
     * @param path ścieżka do pliku .csv z charakterystyką mikrofonu, podana
     * przez użytkownika
     * @return obliczone wzmocnienia związane z charakterystyką mikrofonu
     * @throws FileNotFoundException jeśli nie znaleziono pliku z charakterystyką
     * @throws IOException jeśli wystąpi błąd podczas odczytu
     * @throws IllegalArgumentException jeśli podany plik z charakterystyką nie jest według standardu
     */
    public static double[] lagInterp(int samplingRate, boolean fast, String path) throws FileNotFoundException, IOException {

        double fShift; //roznica pomiedzy kolejnymi czestotliwosciami
        double currentFreq = 0;
        int[] pts;
        double[] interpolated;
        double[] micCor = readCSVFile(path); //plik CSV: czestotliwosc,wzmocnienie - rekordy oddzielone znakiem nowej linii
        double[] frequencies = new double[micCor.length / 2];
        if (fast) {
            fShift = samplingRate/8192.0; //TODO sprawdzic czy poprawnie
            interpolated = new double[8192];
        } else {
            fShift = samplingRate/65536.0; //TODO sprawdzic czy poprawnie
            interpolated = new double[65536];
        }

        for (int i = 0; i < micCor.length; i = i + 2) {
            frequencies[i / 2] = micCor[i];
        }
        interpolated[0] = 1;
        for (int i = 0; i < interpolated.length/2; i++) {
            currentFreq += fShift;
            pts = findClosest(frequencies, currentFreq);
            interpolated[i] = evalInterp(currentFreq,
                    micCor[pts[0] * 2], micCor[pts[1] * 2], micCor[pts[2] * 2],
                    micCor[pts[0] * 2 + 1], micCor[pts[1] * 2 + 1], micCor[pts[2] * 2 + 1]);
            interpolated[i] = Math.pow(10, interpolated[i]/20);               //zamiana wzmocnienia na współczynnik amplitudy
        }
        for (int i = interpolated.length/2; i < interpolated.length; i++) {   //uwzględniamy okresowość widma
            interpolated[i] = interpolated[interpolated.length-i-1];
        }
        return interpolated;
    }

    /**
     * Czyta plik .csv i zwraca częstotliwości wraz z odpowiadającymi im
     * wzmocnieniami. W pliku rekordy powinny być zapisane w następujący sposób:
     * częstotliwość,wzmocnienie oraz być oddzielone znakami nowej linii. Plik
     * musi zawierać nagłówek "charakterystyka mikrofonu".
     *
     * @param path ścieżka do pliku .csv, plik ma zawierać nagłówek
     * "charakterystyka mikrofonu" w pierwszej linii
     * @return częstotliwości na parzystych indeksach oraz odpowiadające im
     * wzmocnienia na nieparzystych
     * @throws FileNotFoundException jeśli nie znaleziono pliku
     * @throws IOException jeśli wystąpi błąd podczas odczytu
     * @throws IllegalArgumentException jeśli plik nie jest według standardu
     */
    private static double[] readCSVFile(String path) throws FileNotFoundException, IOException, IllegalArgumentException {

        BufferedReader br = null;
        double[] micCorrection;
        int counter = 0;
        String[] line;
        String naglowek = "charakterystyka mikrofonu";        
        try {            
            br = new BufferedReader(new FileReader(path));   
            //sprawdzamy czy naglowek jest poprawny
            if (!(br.readLine().trim().equalsIgnoreCase(naglowek))) {
                throw new IllegalArgumentException();
            }
            //liczymy ilosc linijek
            while (br.readLine() != null) {
                counter++;
            }
        } catch (FileNotFoundException e) {
            if (br != null) {
                br.close();
            }
            throw e;
        } catch (IOException e) {
            if (br != null) {
                br.close();
            }
            throw e;
        }
        br.close();
        //jesli plik jest pusty (oprocz naglowka), lub ma mniej niz 3 rekordy to tez rzucamy wyjatek
        if(counter<3)
            throw new IllegalArgumentException();
        micCorrection = new double[counter * 2];
        try {
            br = new BufferedReader(new FileReader(path));
            br.readLine(); //omijamy nagłówek
            for (int i = 0; i < counter; i++) {
                line = br.readLine().trim().split(",");
                micCorrection[2 * i] = Double.parseDouble(line[0]); //czestotliwosc
                micCorrection[2 * i + 1] = Double.parseDouble(line[1]); //wzmocnienie
            }
        } catch (FileNotFoundException e) {
            if (br != null) {
                br.close();
            }
            throw e;
        } catch (IOException e) {
            if (br != null) {
                br.close();
            }
            throw e;
        } catch (Exception e) {       //jeśli rzuci wyjątkiem innym niż związanym z I/O (np. ParseException) tzn, że plik nie jest według standardu
            if (br != null) {
                br.close();
            }
            throw new IllegalArgumentException();
        }
        br.close();
        return micCorrection;
    }

    /**
     * Metoda znajdująca 3 najbliższe wartości do zadanej w tablicy double.
     * Wykorzystuje metodę checkTwo
     *
     * @param freqs POSORTOWANA tablica częstotliwości
     * @param x szukana wartość
     * @return tablica z indeksami 3 najbliższych wartości     *
     */
    private static int[] findClosest(double[] freqs, double x) { //zwraca tablice indeksow w tablicy czestotliwosci
        //freqs MUSI BYC POSORTOWANE (zakladamy ze w csv bedzie)
        int[] j = new int[3];
        j[0] = 0;
        double min = Math.abs(freqs[0] - x);
        for (int i = 0; i < freqs.length; i++) { //znajdujemy najmniejszy element
            if (Math.abs(freqs[i] - x) < min) {
                j[0] = i;
                min = Math.abs(freqs[i] - x);
            }
        }
        j[1] = checkTwo(freqs, j[0] - 1, j[0] + 1, x);      //sprawdzamy elementy obok niego  
        if (j[1] == (j[0] + 1)) {
            j[2] = checkTwo(freqs, j[0] - 1, j[0] + 2, x);
        } else {
            j[2] = checkTwo(freqs, j[0] - 2, j[0] + 1, x);
        }
        return j;

    }

    /**
     * Metoda sprawdza która z dwóch wartości w tablicy jest bliżej do zadanej
     * wartości. Potrzebna do metody findClosest
     *
     * @param freqs tablica z częstotliwościami
     * @param i1 indeks pierwszej wartości porównywanej
     * @param i2 indeks drugiej wartości porównywanej
     * @param x wartość zadana
     * @return indeks wartości bliższej do x     *
     */
    private static int checkTwo(double[] freqs, int i1, int i2, double x) {
        //porownuje odleglosci 2 elementow od zadanej wartosci i sprawdza czy nie wychodzimy poza zakres
        //i1 to dolny indeks, i2 gorny
        if (i2 >= freqs.length) {
            return i1;
        }
        if (i1 < 0) {
            return i2;
        }
        if (Math.abs(freqs[i1] - x) > Math.abs(freqs[i2] - x)) {
            return i2;
        }
        return i1;
    }

    /**
     * Trzypunktowa interpolacja Lagrange'a.
     *
     * @param x argument dla której interpolujemy wartość
     * @param x1 argument nr 1
     * @param x2 argument nr 2
     * @param x3 argument nr 3
     * @param y1 f(x1)
     * @param y2 f(x2)
     * @param y3 f(x3)
     * @return f(x)
     */
    private static double evalInterp(double x, double x1, double x2, double x3, double y1, double y2, double y3) { //trzypunktowa

        double sum = ((x - x2) * (x - x3) * y1) / ((x1 - x2) * (x1 - x3));
        sum += ((x - x1) * (x - x3) * y2) / ((x2 - x1) * (x2 - x3));
        sum += ((x - x1) * (x - x2) * y3) / ((x3 - x1) * (x3 - x2));
        return sum;
    }

}
