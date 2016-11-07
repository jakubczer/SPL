package Audio;


import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Line;
import javax.sound.sampled.Mixer;

/**
 * Klasa dostarcza informacje o karcie dźwiękowej
 *
 * @author  Mateusz Tracz
 */
public class AudioInfo {
    /**
     * Metoda odpytuje kartę dźwiękową o liczbę dostępnych kanałów
     *
     *
     * Kanały pobierane są z karty dźwiękowej ustawionej jako domyślna.
     *
     * @return Tablica kanałów licząc od 1
     * @author  Mateusz Tracz
     */
    public static String[] getChannels(){
        Mixer.Info[] mixInfos = AudioSystem.getMixerInfo();

        // pobranie mixera wybranego jako domyślny
        Mixer mixer = AudioSystem.getMixer(null);

        Line.Info[] channels = mixer.getSourceLineInfo();

        String[] ret = new String[channels.length];

        // w tablicy są numery kanałów, można zmienić na nazwy
        for (int i = 0; i < ret.length; i++) {
            ret[i] = new String(String.valueOf(i+1));
        }


        return ret;
    }
}
