package SignalInput;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import java.io.File;
import java.io.IOException;
import java.util.Queue;

/**
 * Klasa pobierająca dane z pliku wav
 * Dane zapisywane są do kolejki bajtów jako tablice z wartością dla każdego kanału w czasie rzeczywistym
 * czyli tak jakby dane były pobierane z karty dźwiękowej
 * plik jest zapętlony
 * @author Piotr Janiszewski
 */
public class AudioFromFile implements  Runnable {
    private  Thread t;
    private Queue<byte[]> kolejka;
    private AudioInputStream audioInputStream;
    private File soundFile;

    /**
     * Konstruktor klasy AudioFromFile
     * @param kolejka kolejeka do której zapisywane są tablice z danymi
     * @param filePath ścieżka dostępu do pliku
     */
    public  AudioFromFile(Queue<byte[]> kolejka, String filePath){
        this.soundFile = new File(filePath);
        this.audioInputStream = createAudioInputStream();
        this.kolejka=kolejka;
    }

    /**
     * Metoda tworząca wątek.
     */
    public void Start(){
        if (t == null)
        {
            t = new Thread (this);
            t.start ();
        }
    }

    /**
     * Metoda zatrzymująca wątek.
     */
    public  void Stop(){
        t.stop();
    }

    /**
     * Wątek w pętli pobiera dane z pliku i zapisuje je do kolejki.
     * Dane pobierane są jako tablica w której znajdują się wartości dla każdego kanału
     * plik jest zapętlony
     */
    @Override
    public void run() {
        long przeczytaneDane=0;
        long time = System.nanoTime();
        float odstep = 1000000000 / audioInputStream.getFormat().getSampleRate();
        int tabLength =audioInputStream.getFormat().getChannels()*audioInputStream.getFormat().getSampleSizeInBits()/8;

        while(true) {
            int koniecPliku = 0;
            while (koniecPliku != -1) {
                if (przeczytaneDane * odstep <= System.nanoTime() - time) {
                    try {
                        byte[] abData = new byte[tabLength];
                        koniecPliku = audioInputStream.read(abData, 0, abData.length);
                        this.kolejka.add(abData);
                        przeczytaneDane++;
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                Thread.yield();
            }
            this.audioInputStream=createAudioInputStream();
        }
    }

    /**
     * Metoda tworząca AudioInputStream
     */
    private AudioInputStream createAudioInputStream(){
        AudioInputStream audioInputStream = null;
        try {
            audioInputStream = AudioSystem.getAudioInputStream(soundFile);
        }
        catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
        return audioInputStream;
    }
}
