package Calculations;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import javax.sound.sampled.AudioFormat;


/**
 * Klasa przechowująca przetworzone dane z karty dzwiekowej.
 *
 * Uwaga podczas uruchamiania watku pobierajacego sygnal z karty, nalezy ustawic timestamp System.currentTimeMillis()!!!
 *
 * zmienna timeWeighting to wilekosc (w milisekundach) stalej czasowej typowo: 32ms, 125ms,1000ms. Powysza zmienna definiuje dlługość tablic double[]
 * @author Chomik
 *
 */
public class AudioData {
	public ArrayList<ConcurrentLinkedQueue<double[]>> Data;	//Lista synchronizowanych kolejek, po jednej kolejce na kanał
	public long timeStamp;		//sygnatura czasowa, należy przypasać gdy nastąpi rozpoczęcie pobierania sygnału
	public final int sampleRate;	//ilość próbek na sekundę
	public final int channels;	//liczba kanałów
	public final int sampleSize;	//ilość bitów próbki
	public final int arraysize;	//rozmiar tablicy doubli (zależny od stałej czasowej)


	/**
	 * Konstruktor klasy AudioData, przechowującej tablice doubli[] <-1,1> dla każdego kanału.
	 *
	 * Podczas uruchamiania pobierania syganłu należy ustawić timeStamp = System.currentTimeMillis()
	 * Do obiektu Data należy odwoływać się bezpośrednio.
	 *
	 * @param sampleFrequency - ilość próbek na sekundę
	 * @param channels - liczba kanałów
	 * @param sampleSize - ilość bitów próbki
	 * @param timeWeighting - stała czasowa w ms (32/125/1000ms)
	 */
	public AudioData(int sampleFrequency,int channels, int sampleSize,int timeWeighting){
		this.sampleRate = sampleFrequency;
		this.channels = channels;
		this.timeStamp = 0;	//trzeba przypisać podczas uruchomienia !!!
		this.sampleSize = sampleSize;
		this.arraysize = timeWeighting*this.sampleRate/1000;
		this.Data = new ArrayList<>();
		for(int i=0;i<channels;i++){
		    ConcurrentLinkedQueue<double[]> kolejka = new ConcurrentLinkedQueue<>();
		    this.Data.add(kolejka);
		}
	}
	/**
	 * Konstruktor klasy AudioData, przechowującej tablice doubli[] <-1,1> dla każdego kanału.
	 *
	 * Podczas uruchamiania pobierania syganłu należy ustawić timeStamp = System.currentTimeMillis()
	 * Do obiektu Data należy odwoływać się bezpośrednio.
	 *
	 * @param format - AudioFormat w jakim pobierane były dane
	 * @param timeWeighting - stała czasowa w ms (32/125/1000ms)
	 */
	public AudioData(AudioFormat format,int timeWeighting){
		this.sampleRate = ((int)format.getSampleRate());
		this.channels = format.getChannels();
		this.timeStamp = 0;	//trzeba przypisać podczas uruchomienia !!!
		this.sampleSize = format.getSampleSizeInBits();
		this.arraysize = timeWeighting*this.sampleRate/1000;
		this.Data = new ArrayList<>();
		for(int i=0;i<channels;i++){
		    ConcurrentLinkedQueue<double[]> kolejka = new ConcurrentLinkedQueue<>();
		    this.Data.add(kolejka);
		}
	}
}
