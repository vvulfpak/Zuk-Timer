package com.sjonsen.application;

import java.awt.event.ActionListener;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.Timer;
import javax.swing.UIManager;

import org.jnativehook.GlobalScreen;
import org.jnativehook.dispatcher.SwingDispatchService;
import org.jnativehook.keyboard.NativeKeyEvent;
import org.jnativehook.keyboard.NativeKeyListener;


/**
 * @author Sjonsen
 * Jan 25, 2017
 * Ugly fast code, may refactor in the future
 */
public class Controller implements Initializable, NativeKeyListener {

    @FXML
    private Label timerField;

    @FXML
    private ImageView setupButton;

    @FXML
    private Label topText;

    String dTop;

    int stage = 0;
    String pr = "space", add = "escape";//default keys

    Stage window;

    boolean onTop = false;

    int COUNT = 210;
    int minutes;
    int seconds;
    int tempcount;
    Timer timer;
    InputStream is, bufferedIn;
    AudioInputStream audioInputStream;
    FloatControl gainControl;
    Clip clip;

    Tooltip tt;

    BufferedWriter bw;
    String fileResult = "";
    String path;


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            path = System.getProperty("user.home") + File.separator + "Documents";
            path += File.separator + "ZukTimer";
            File dir = new File(path);

            if (dir.exists()) {
                readFile(path);
            } else if (dir.createNewFile()) {
                bw = new BufferedWriter(new FileWriter(path));
                bw.write(pr);
                bw.write("\n");
                bw.write(add);
                bw.close();
            } else {
                System.out.println(dir + " was not created");
            }

            is = getClass().getResourceAsStream("beep1.wav");
            bufferedIn = new BufferedInputStream(is);
            audioInputStream = AudioSystem.getAudioInputStream(bufferedIn);
            clip = AudioSystem.getClip();
            clip.open(audioInputStream);
            gainControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
            gainControl.setValue(-15.0f);
            dTop = topText.getText();
            tt = new Tooltip();
            tt.setText("Pause/resume: " + pr + "\nAdd time: " + add);
            Tooltip.install(setupButton, tt);
        } catch (UnsupportedAudioFileException | IOException e2) {
            e2.printStackTrace();
        } catch (LineUnavailableException e1) {
            e1.printStackTrace();
        }
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            GlobalScreen.setEventDispatcher(new SwingDispatchService());
            GlobalScreen.registerNativeHook();
            GlobalScreen.addNativeKeyListener(this);
            Logger logger = Logger.getLogger(GlobalScreen.class.getPackage().getName());
            logger.setLevel(Level.WARNING);
            logger.setUseParentHandlers(false);


            tempcount = COUNT;
            minutes = (tempcount % 3600) / 60;
            seconds = tempcount % 60;


        } catch (Exception e) {

        }
        timerField.setOnMouseClicked(new EventHandler<MouseEvent>(){

            @Override
            public void handle(MouseEvent event) {
                reset();

            }

        });

        timer = new Timer(1000, new ActionListener() {

            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                if (clip != null)
                clip.setFramePosition(0);
                minutes = (tempcount % 3600) / 60;
                seconds = tempcount % 60;
                tempcount--;
                if (tempcount <= 5 && tempcount >= 0){
                    clip.start();
                } else if (tempcount <= 0){
                    tempcount = COUNT;
                }
                updateUI();
            }
        });

        setupButton.setOnMouseClicked(new EventHandler<MouseEvent>(){

            @Override
            public void handle(MouseEvent event) {
                setupButtons();
            }
        });
    }

    public void readFile(String path){
        try (BufferedReader br = new BufferedReader(new FileReader(path))){
            String line;

            while ((line = br.readLine()) != null){
                fileResult += line;
                fileResult += ":";
            }
            System.out.println(fileResult);
        } catch (IOException e){
            e.printStackTrace();
        }

        String[] splits = fileResult.trim().split(":");
        pr = splits[0];//1
        add = splits[1];//2
    }

    public void saveButtons(String path){
        try {
            bw = new BufferedWriter(new FileWriter(path));
            bw.write(pr);
            bw.write("\n");
            bw.write(add);
            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setupButtons(){
        topText.setText("Press pause/resume key...");
        stage = 1;

    }

    public void updateUI(){
        minutes = (tempcount % 3600) / 60;
        seconds = tempcount % 60;
        Thread timerThread = new Thread(new Runnable(){
            public void run(){
                Platform.runLater(new Runnable(){

                    @Override
                    public void run() {
                        timerField.setText(String.format(Locale.ENGLISH, "%02d:%02d", minutes, seconds));
                        switch(stage){
                            case 2:
                                topText.setText("Press add time key...");
                                break;
                            case 3:
                                topText.setText(dTop);
                                saveButtons(path);
                                tt.setText("Pause/resume: " + pr + "\nAdd time: " + add);
                                stage = 0;
                                break;
                        }
                    }

                });
            }
        });
        timerThread.start();
    }



    public void reset(){
        timer.stop();
        COUNT = 210;
        tempcount = COUNT;
        minutes = (tempcount % 3600) / 60;
        seconds = tempcount % 60;
        updateUI();
    }

    @Override
    public void nativeKeyPressed(NativeKeyEvent e) {
        String input = NativeKeyEvent.getKeyText(e.getKeyCode());

        if (stage == 1){
            pr = input.toLowerCase();
            stage = 2;
            updateUI();
            return;
        }else if (stage == 2){
            add = input.toLowerCase();
            stage = 0;
            stage = 3;
            updateUI();
            return;
        }

        if (input.toLowerCase().equalsIgnoreCase(pr)){
            if (timer.isRunning() == true){
                timer.stop();
            }else {
                timer.start();
            }
        }else if (input.toLowerCase().equalsIgnoreCase(add)){
            tempcount += 105;
            updateUI();
        }
    }

    @Override
    public void nativeKeyReleased(NativeKeyEvent arg0) {

    }

    @Override
    public void nativeKeyTyped(NativeKeyEvent arg0) {

    }

    public Controller(){}

    public void setStage(Stage primaryStage) {
        window = primaryStage;

    }



}

