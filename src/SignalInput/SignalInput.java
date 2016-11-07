package SignalInput;

import CalibrationPKG.Calibration;

import javax.sound.sampled.*;
import java.util.Queue;

/**
 * Klasa pobierająca dane z karty dźwiękowej.
 * Dane zapisywane są do kolejki bajtów jako tablice z wartością dla każdego kanału.
 *
 * @author Piotr Janiszewski
 */
public class SignalInput implements Runnable {

    private Thread t;
    public Queue<byte[]> kolejka;

    public TargetDataLine getTargetLine() {
        return targetLine;
    }

    public void setTargetLine(TargetDataLine targetLine) {
        this.targetLine = targetLine;
    }

    private TargetDataLine targetLine;
    private float sampleRate;
    private int sampleSizeInBits;
    private int channels;
    private boolean bigEndian;
    private volatile boolean running = true;


    /**
     * Konstruktor klasy SignalInput
     * @param kolejka kolejka do której zapisywane są tablice z danymi
     * @param sampleRate częstotliwość pobierania próbek
     * @param sampleSizeInBits wielkość próbki, podawana jako ilość bitów
     * @param channels liczba kanałów
     * @param bigEndian kolejność bajtów w próbce, true - big endian
     */
    public SignalInput(Queue<byte[]> kolejka, float sampleRate, int sampleSizeInBits, int channels, boolean bigEndian){
        this.kolejka=kolejka;
        this.sampleRate=sampleRate;
        this.sampleSizeInBits=sampleSizeInBits;
        this.channels=channels;
        this.bigEndian=bigEndian;
        if(Calibration.isNeedNewTargetData()) this.targetLine = createTargetDataLine();
        else this.targetLine = Calibration.getTargetDataLine();
    }

    /**
     * Metoda tworząca wątek.
     */
    public void Start(){
        if (t == null)
        {
            try {
                if(!targetLine.isOpen()) targetLine.open();
            } catch (LineUnavailableException e) {
                e.printStackTrace();
            }
            targetLine.start();
            t = new Thread (this);
            running=true;
            t.start ();
        }
    }

    /**
     * Metoda zatrzymująca wątek.
     */
    public  void Stop(){
        targetLine.stop();
        targetLine.close();
        running=false;
        t=null;
    }

    /**
     * Wątek w pętli pobiera dane z karty dźwiękowej i zapisuje je do kolejki.
     * Dane pobierane są jako tablica w której znajdują się wartości dla każdego kanału
     */
    @Override
    public void run() {
        while(running)
        {
            byte[] data = new byte[channels * sampleSizeInBits / 8];
            targetLine.read(data, 0, data.length);
            kolejka.add(data);
            Thread.yield();
        }
    }

    /**
     * Metoda tworząca TargetDataLine
     */
    private TargetDataLine createTargetDataLine(){
        AudioFormat format1 = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, sampleRate, sampleSizeInBits, channels, (sampleSizeInBits/8)*channels , sampleRate, bigEndian);
        DataLine.Info info = new DataLine.Info(TargetDataLine.class, format1);

        TargetDataLine	targetDataLine = null;
        try
        {
            targetDataLine = (TargetDataLine) AudioSystem.getLine(info);
            targetDataLine.open(format1);
        }
        catch (LineUnavailableException e)
        {
            e.printStackTrace();
            System.exit(1);
        }
        return targetDataLine;
    }
}
