package Interface;

import Audio.AudioInfo;
import CalibrationPKG.Calibration;
import CalibrationPKG.CalibrationRunnable;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.text.DecimalFormat;

public class CalibrationView extends JDialog {
    private JPanel contentPane;
    private JButton buttonCancel;
    private JProgressBar maxPB;
    private JSpinner spinner1;
    private JButton startCalibrationBtn;
    private JTextField calibratorVal;
    private JLabel wynikLabel;
    private JLabel maximumValue;
    private JProgressBar calibPB;
    private JLabel wynLabel;
    private static JProgressBar calibRef;
    public static boolean clicked = false;
    public boolean isRunning = false;

    private Calibration calibration;

    public volatile CalibrationPKG.CalibrationRunnable calibrationRunnable;

    /**
     * Konstruktor klasy CalibrationView
	 * @author Mateusz Tracz
     */
    public CalibrationView() {
        calibration = new Calibration();
        setContentPane(contentPane);
        setModal(true);
        setTitle("Kalibracja");


        // wybieranie kanału
        String[] channels = AudioInfo.getChannels();
        SpinnerListModel channelmodel = new SpinnerListModel(channels);
        spinner1.setModel(channelmodel);

        startCalibrationBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                new Thread()
                {
                    public void run() {
                        startCalibration(Integer.parseInt((String) spinner1.getValue()) - 1);
                    }
                }.start();
            }
        });

        buttonCancel.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        });

        // call onCancel() when cross is clicked
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });


        // wyswietlanie maksimum
        createCalibrationThread();
        calibrationRunnable.startThread();

        setSize(500, 300);
        setVisible(true);

    }

    public static JProgressBar getCalibRef() {
        return calibRef;
    }

    public static boolean isClicked() {
        return clicked;
    }

    /**
     * Funkcja tworząca wątek kalibracyjny.
     *
     * W wątku tym w pętli liczona jest maksymalna wartość jaką może jeszcze przyjąć karta.
     *
	 * @author Mateusz Tracz
     */
    private void createCalibrationThread(){
        CalibrationPKG.CalibrationRunnable newCalibrationRunnable = new CalibrationRunnable(
                calibratorVal,
                spinner1,
                calibRef,
                calibPB,
                calibration,
                maximumValue,
                maxPB);

        calibrationRunnable = newCalibrationRunnable;
    }
    /**
     * Funkcja startująca po wciśnięciu klawisza Kalibruj.
     *
     * Dokonuje jednokrotnej kalibracji zadanego kanału.
     *
     * @param channel Numer kanału do kalibracji
	 * @author Mateusz Tracz
     */
    private void startCalibration(final int channel) {
        //zabezpieczenie przed ponownym kliknieciem
        if (isRunning) return;
        isRunning = true;

        calibrationRunnable.stopThread();
        calibration.signalInput.Stop();

        int calibratorValue = Integer.parseInt(calibratorVal.getText());

        // żeby nie zmieniać metody calibrate
        CalibrationView.clicked = true;
        calibRef = calibPB;

        Calibration.setCalibratorValue(calibratorValue);
        calibration.calibrate(channel);

        DecimalFormat df = new DecimalFormat("#.0");
        String output = df.format(calibration.getCalibrationOffset(channel)) + " dB";

        wynikLabel.setText(output);

        // wznowienie wątku, t.Start() nie jest dozwolone przez Javę po zastopowaniu
        createCalibrationThread();
        calibrationRunnable.startThread();

        isRunning = false;
    }

    /**
     * Funkcja odpowiedzialna za zamknięcie okna kalibracji
     *
     * Nie jest wykonywana gdy kalibracja jest w trakcie.
	 * @author Mateusz Tracz
     */
    private void onCancel() {
        if(isRunning) return;

        clicked = true;
        calibrationRunnable.stopThread();
        calibration.signalInput.Stop();


        dispose();
    }


}
