package Calculations;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;


/**
 * Wątek przekazujący dane z bufora byte[] do kolejki double[] 
 * 
 * Wątek pobiera dane z kolejki Queue<byte[]> - dane odebrane z karty dzwiękowej. Konweruje je do postaci doubli na przedziale <-1,1>. 
 * I zapisuje do struktury danych AudioData (ArrayList<ConcurrentLinkedQueue<double[]>>) Rozdzielając odpowiednio na kanały.
 * 
 * @author Chomik
 *
 */
public class DataHandling extends Thread { 
	
		public Queue<byte[]> queue;
		public final AudioData data;
		public final int arraysize;
		public final int channels;
		public final int sampleSize;
		//public final int bufferSize;
		
		/**
		 * Konstruktor wątku przechwytującego dane z Queue<byte[]> queue (surowe dane z karty), przetwarza na double <-1,1> 
		 * i umieszcza je w klasie AudioData data.
		 * 
		 * @param queue - kolejka tablic byte[] z karty dzwiękowej
		 * @param data - instancja klasy AudioData w której mają być przechowywane dane
		 */
	    public DataHandling(Queue<byte[]> queue, AudioData data){
	        this.queue=queue;
	        this.data = data;
	        this.arraysize = data.arraysize;
	        this.channels = data.channels;
	        this.sampleSize = data.sampleSize;
	        //this.bufferSize = arraysize;
	    }
	    /**
	     * Rozpoczęcie wątku zbierania i konwesji danych.
	     */
	    public void Start(){ 
	        super.start();
	    }
	    /**
	     * Zakonczenie wątku konwersji
	     */
	    public void Stop(){
	        super.stop();
	    }
	    /**
	     * Pętla główna wątku pobiera dane z kolejki byte[] queue, pod warunkiem, że danych jest wystarczająca ilość do zapisu do tablicy.
	     * Gdy uzbiera się odpowiednia ilość danych (data.arraySize), zdejmuje z queue, konwertuje na double <-1,1>
	     * i zapisuje całą tablicę double[] do odpowiednich kolejek w data.Data.
	     */
	    @Override public void run()
		{
	    	//double max=0;
	    	//TODO @testerzy int amax = 1500;
	    	
			ArrayList<List<Double>> tmplist = new ArrayList<List<Double>>(); //
			while(true)
			{
				if(queue.size()>this.arraysize){			//sprawdzam, czy wystarszający bufor
					/*	//TODO @testerzytestowanie, czy bufor się opróżnia
					 
						if(queue.size()>amax){
						amax = queue.size();
						System.out.printf("max queue.size():%d\n",amax);
						//System.out.printf("queue.size():%d\tthis.arraysize:%d\n",queue.size(),this.arraysize);
					}
					*/
					
					for(int j=0;j<this.channels;j++)		//dla kazdego kanalu
					{
						List<Double> dlist = new ArrayList<Double>();
						tmplist.add(dlist);
					}
					for(int i=0;i<this.arraysize;i++){
						
						byte[] bytes;		
						double[] tmpdata;
						
						if((bytes=queue.poll())!=null){			//pobieram dane
							//konwersja na double
							 tmpdata = byteConverter.convertToDouble(bytes,this.channels,this.sampleSize,false);
						}else{
							//System.out.printf("Nie mozna pobrac danych z kolejki byte[] !\n");
							break;
							 //tmpdata = new double[this.channels];
						}
						
						for(int j=0;j<this.channels;j++)		//rozdzielenie doubli dla kazdego kanalu
						{
							tmplist.get(j).add(tmpdata[j]);
						}
					}
					
					//lista pelna, zapisuje do data
					for(int j=0;j<this.channels;j++)			//dla kazdego kanalu
					{
					    	/*
						if (tmplist.get(j).size() != this.arraysize){
							System.out.println("Blad rozmiaru listy!!!");
						}
						*/
						
						//zamiana listy na tablice
						double[] tmp = new double[tmplist.get(j).size()];
						for(int i = 0; i < tmplist.get(j).size(); i++) tmp[i] = tmplist.get(j).get(i);
						
						//dodanie tablicy do data
						data.Data.get(j).add(tmp);
						tmplist.get(j).clear();
					}

					
					for(int j=0;j<this.channels;j++)			//dla kazdego kanalu
					{
						//double[] doubleArray = data.Data.get(j).poll();	//zdejmowanie z AudioData
						//TODO dane zapisane do AudioData, mozna wywolackolejne funkcje dB, maxAmplitudy itp.
						
						
						
						/*//wypisywanie maxa amplitud:
						double tmp = todB.maxAmplitude(doubleArray);
						if(tmp>max){
							max = tmp;
							System.out.printf("max:%f\n",max);
							}
						*/
						
					}
					
				}else{
					
					try {
						//TODO @testerzy sprawdzic, na ile usypiac, i czy to nie przeszkadza!!! patrz max queue.size() ^^
						Thread.sleep(10);
						//50 za dużo
						//System.out.printf("sleep\n");
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					
				}
				
			}
		}
	}
