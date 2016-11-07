package Interface;

import Calculations.FrequencyWeighting;
import Calculations.Interpolation;
import Calculations.WindowFunction;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

public class MainInterface extends JFrame {

    private JPanel MainViewField;
    private JButton startButton;
    private JButton stopButton;
    private JButton calibrateButton;
    private JButton micEqButton;
    private JList sampleRateList;
    private JList fWeightingList;
    private JList windowFunctionList;
    private JList sampleWeightingList;
    private JLabel stateText;
    private JList leqList;
    private JLabel micEqFileLabel;
    private JButton max_act;
    private JLabel resultsLabel;

    private Measurement measurement;

    private File micEqFile;

    private volatile boolean isRunning;

    private boolean maxValues;

    private double[] interpolated;

    /**
     * Ustawienie listenera przycisku kalibracji
     */
    public MainInterface() {

        maxValues=false;

        calibrateButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                if (isRunning) return;
                CalibrationView dialog = new CalibrationView();
            }
        });

        startButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                //zabezpieczenie przed ponownym wciśnieciem Start
                if (isRunning) return;



                int sampleRate;
                FrequencyWeighting frequencyWeighting=null;
                WindowFunction wFunction=null;

                if(sampleWeightingList.getSelectedIndex()<0){
                    stateText.setText("Wybierz stałą czasową");
                    return;
                }

                switch (sampleRateList.getSelectedIndex()) {
                    case 0:
                        sampleRate = 44100;
                        break;
                    case 1:
                        sampleRate = 48000;
                        break;
                    default:
                        stateText.setText("Wybierz częstotliwość próbkowania");
                        return;
                }

                if(!(sampleWeightingList.getSelectedValue()).equals("Impulse")) {
                    switch (fWeightingList.getSelectedIndex()) {
                        case 0:
                            frequencyWeighting = FrequencyWeighting.A;
                            resultsLabel.setText("<html> L<sub>Aeq</sub> </html>");
                            break;
                        case 1:
                            frequencyWeighting = FrequencyWeighting.C;
                            resultsLabel.setText("<html> L<sub>Ceq</sub> </html>");
                            break;
                        case 2:
                            frequencyWeighting = FrequencyWeighting.Z;
                            resultsLabel.setText("<html> L<sub>Zeq</sub> </html>");
                            break;
                        default:
                            stateText.setText("Wybierz krzywą charakterystyki");
                            return;
                    }

                    switch (windowFunctionList.getSelectedIndex()) {
                        case 0:
                            wFunction = WindowFunction.Rectangle;
                            break;
                        case 1:
                            wFunction = WindowFunction.Bartlett;
                            break;
                        case 2:
                            wFunction = WindowFunction.Hanning;
                            break;
                        case 3:
                            wFunction = WindowFunction.Hamming;
                            break;
                        case 4:
                            wFunction = WindowFunction.Blackman;
                            break;
                        default:
                            stateText.setText("Wybierz okno czasowe");
                            return;
                    }
                }

                if(!(sampleWeightingList.getSelectedValue()).equals("Impulse")&&micEqFile==null){
                    stateText.setText("Wybierz plik z charakterystyką mikrofonu");
                    return;
                }

                if(!(sampleWeightingList.getSelectedValue()).equals("Impulse")&&micEqFile!=null){
                    try{
                        boolean fast = ((String)sampleWeightingList.getSelectedValue()).equals("Fast");
                        interpolated = Interpolation.lagInterp(sampleRate, fast, micEqFile.getPath());
                    }
                    catch(IllegalArgumentException e){
                        stateText.setText("Plik niezgodny ze standardem");
                        return;
                    }
                    catch(FileNotFoundException e){
                        stateText.setText("Brak pliku o podanej ścieżce");
                        return;
                    }
                    catch(IOException e){
                        stateText.setText("Wystąpił błąd podczas czytania pliku");
                        return;
                    }
                }

                measurement=new Measurement(stateText, leqList, maxValues);
                measurement.start(sampleRate, frequencyWeighting, wFunction, (String)sampleWeightingList.getSelectedValue(), interpolated);

                isRunning = true;
            }
        });
        stopButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                isRunning = false;
                if (measurement != null) measurement.stop();
            }
        });
        micEqButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                final JFileChooser fc = new JFileChooser();
                if (fc.showOpenDialog(MainViewField) == JFileChooser.APPROVE_OPTION) {
                    micEqFile= fc.getSelectedFile();

                    micEqFileLabel.setText(micEqFile.getName());
                } else {
                    micEqFileLabel.setText("Brak");
                }
            }
        });

        max_act.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                maxValues=!maxValues;
                measurement.SwitchMaxAct(maxValues);

                if(maxValues){
                    max_act.setText("Przełącz na wartości aktualne");
                    resultsLabel.setText("Max:");
                } else {
                    max_act.setText("Przełącz na wartości maxymalne");


                    switch (fWeightingList.getSelectedIndex()) {
                        case 0:
                            resultsLabel.setText("<html> L<sub>Aeq</sub> </html>");
                            break;
                        case 1:
                            resultsLabel.setText("<html> L<sub>Ceq</sub> </html>");
                            break;
                        case 2:
                            resultsLabel.setText("<html> L<sub>Zeq</sub> </html>");
                            break;
                        default:
                            resultsLabel.setText("<html> L<sub>xy</sub> </html>");
                    }
                }
            }
        });
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("MainView");
        frame.setContentPane(new MainInterface().MainViewField);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 600);
        frame.setVisible(true);
    }
}

