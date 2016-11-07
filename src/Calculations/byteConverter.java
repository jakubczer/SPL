package Calculations;


/**
 * Klasa zamieniająca bajty na double
 * Wszystkie metody statycze - nie tworzyć obiektu
 *
 * @author Piotr Janiszewski
 */
public class byteConverter {

    /**
     * Metoda zamieniająca 2 bajty w wartość double
     * @param buffer tablica bajtów
     * @param byteOffset miejsce z którego należy pobrać pierwszy bajt
     * @param bigEndian kolejność bajtów w próbce, true - big endian
     * @return wyliczonona wartość double
     */
    private static double bytesToDouble16(byte[] buffer, int byteOffset, boolean bigEndian) {
        if (bigEndian) return ((buffer[byteOffset]<<8) | (buffer[byteOffset+1] & 0xFF));
        else return ((buffer[byteOffset+1]<<8) | (buffer[byteOffset] & 0xFF));
    }

    /**
     * Metoda zamieniająca 3 bajty w wartość double
     * @param buffer tablica bajtów
     * @param byteOffset miejsce z którego należy pobrać pierwszy bajt
     * @param bigEndian kolejność bajtów w próbce, true - big endian
     * @return wyliczonona wartość double
     */
    private static double bytesToDouble24(byte[] buffer, int byteOffset, boolean bigEndian) {
        if (bigEndian) return ((buffer[byteOffset]<<16) | ((buffer[byteOffset+1] & 0xFF)<<8) | (buffer[byteOffset+2] & 0xFF));
        else return ((buffer[byteOffset+2]<<16) | ((buffer[byteOffset+1] & 0xFF)<<8) | (buffer[byteOffset] & 0xFF));
    }

    /**
     * Metoda zamieniająca 4 bajty w wartość double
     * @param buffer tablica bajtów
     * @param byteOffset miejsce z którego należy pobrać pierwszy bajt
     * @param bigEndian kolejność bajtów w próbce, true - big endian
     * @return wyliczona wartość double
     */
    private static double bytesToDouble32(byte[] buffer, int byteOffset, boolean bigEndian) {
        if(bigEndian) return ((buffer[byteOffset]<<24) | ((buffer[byteOffset+1] & 0xFF)<<16) | ((buffer[byteOffset+2] & 0xFF)<<8) | (buffer[byteOffset+3] & 0xFF));
        else return ((buffer[byteOffset+3]<<24) | ((buffer[byteOffset+2] & 0xFF)<<16) | ((buffer[byteOffset+1] & 0xFF)<<8) | (buffer[byteOffset] & 0xFF));
    }

    /**
     * Metoda zamienia tablicę bajtów z wartościami dla wszystkich kanałów na tablicę double
     * @param input tablica bajtów
     * @param channels liczba kanałów
     * @param sampleSize liczba bitów w próbce
     * @return extracted amplitude
     * @param bigEndian kolejność bajtów w próbce, true - big endian
     * @return tablica double z wartościami dla wszystkich kanałów
     */
    public  static double[] convertToDouble(byte[] input, int channels, int sampleSize,boolean bigEndian) {
        double[] output=new double[channels];
        int offset=0;

        if(sampleSize==16) {
            for (int i = 0; i < channels; i++) {
                output[i] = bytesToDouble16(input, offset, bigEndian)/32767.0;
                if ( output[i]<-1) output[i]=-1;
                offset += 2;
            }
        }

        else if (sampleSize == 24){
            for (int i = 0; i < channels; i++) {
                output[i] = bytesToDouble24(input, offset, bigEndian)/8388607.0;
                if ( output[i]<-1) output[i]=-1;
                offset += 3;
            }
        }

        else if (sampleSize == 32) {
            for (int i = 0; i < channels; i++) {
                output[i] = bytesToDouble32(input, offset, bigEndian)/2147483647.0;
                if ( output[i]<-1) output[i]=-1;
                offset += 4;
            }
        }
        return output;
    }
}
