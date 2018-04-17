package sample;

import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Slider;
import javafx.scene.input.MouseEvent;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.util.Duration;

import java.io.File;
import java.io.FilenameFilter;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;


public class Controller implements Initializable {

    public static final List<String> supportedFileExtensions = Arrays.asList(".mp3", ".mp4");

    private MediaPlayer mediaPlayer;

    @FXML
    private MediaView mediaView;

    @FXML
    private Slider slider;
    @FXML
    private Slider seekSlider;

    @FXML
    private Button previous;
    @FXML
    private Button next;

    @FXML
    private void ButtonFileAction(ActionEvent actionEvent) {
        DirectoryChooser chooser = new DirectoryChooser();
        final File directory = chooser.showDialog(null);
        if (!directory.exists() || !directory.isDirectory()) {
            System.out.println("Cannot find audio source directory: " + directory + " please supply a directory as a command line argument");
            Platform.exit();
            return;
        }
        final List<MediaPlayer> players = new ArrayList<>();
        for (String file : directory.list(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                for (String ext : supportedFileExtensions) {
                    if (name.endsWith(ext)) {
                        return true;
                    }
                }

                return false;
            }
        })) players.add(createPlayer("file:///" + (directory + "\\" + file).replace("\\", "/").replaceAll(" ", "%20")));

        if (players.isEmpty()) {
            System.out.println("No file found in " + directory);
            Platform.exit();
            return;
        }

        mediaView.setMediaPlayer(players.get(0));

        for (int i = 0; i < players.size(); i++) {
            mediaPlayer = players.get(i);
            mediaPlayer.setOnEndOfMedia(new Runnable() {
                @Override
                public void run() {
                    mediaPlayer.stop();
                    mediaView.setMediaPlayer(mediaPlayer);
                    playResource();
                }
            });


        }

        seekSlider.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                currentPlayer().seek(Duration.seconds(seekSlider.getValue()));
            }
        });


        slider.valueProperty().addListener(new InvalidationListener() {
            @Override
            public void invalidated(Observable observable) {
                currentPlayer().setVolume(slider.getValue() / 100);

            }
        });


        previous.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                final MediaPlayer curPlayer = currentPlayer();
                curPlayer.stop();

                MediaPlayer previousPlayer = players.get((players.indexOf(curPlayer) - 1) % players.size());
                mediaView.setMediaPlayer(previousPlayer);
                playResource();
            }
        });

        next.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                final MediaPlayer curPlayer = currentPlayer();
                curPlayer.stop();

                MediaPlayer nextPlayer = players.get((players.indexOf(curPlayer) + 1) % players.size());
                mediaView.setMediaPlayer(nextPlayer);
                playResource();
            }
        });

        mediaView.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                if(mouseEvent.getClickCount() == 2) {

                    Window window = mediaView.getScene().getWindow();
                    boolean isFullScreen = ((Stage) window).isFullScreen();
                    ((Stage) window).setFullScreen(!isFullScreen);

                    window.widthProperty().addListener(new ChangeListener<Number>() {
                        @Override public void changed(ObservableValue<? extends Number> observableValue, Number oldSceneWidth, Number newSceneWidth) {
                            mediaView.setFitWidth((double) newSceneWidth);
                        }
                    });

                    window.heightProperty().addListener(new ChangeListener<Number>() {
                        @Override public void changed(ObservableValue<? extends Number> observableValue, Number oldSceneHeight, Number newSceneHeight) {
                            mediaView.setFitHeight((double) newSceneHeight);
                        }
                    });
                }
            }
        });

        playResource();

    }


    @FXML
    private void playVideo(ActionEvent actionEvent) {
        currentPlayer().play();
        currentPlayer().setRate(1);
    }

    @FXML
    private void pauseVideo(ActionEvent actionEvent) {
        currentPlayer().pause();
    }

    @FXML
    private void stopVideo(ActionEvent actionEvent) {
        currentPlayer().stop();
    }

    @FXML
    private void exitPlayer(ActionEvent actionEvent) {
        System.exit(0);
    }

    @FXML
    private void fastVideo(ActionEvent actionEvent) {
        currentPlayer().setRate(2);
    }

    @FXML
    private void slowVideo(ActionEvent actionEvent) {
        currentPlayer().setRate(0.5);
    }

    private MediaPlayer createPlayer(String mediaSource) {
        final Media media = new Media(mediaSource);
        final MediaPlayer player = new MediaPlayer(media);
        player.setOnError(new Runnable() {
            @Override
            public void run() {
                System.out.println("Media error occurred: " + player.getError());
            }
        });
        return player;
    }

    private MediaPlayer currentPlayer() {
        return mediaView.getMediaPlayer();
    }


    private void playResource() {
        currentPlayer().currentTimeProperty().addListener(new ChangeListener<Duration>() {
            @Override
            public void changed(ObservableValue<? extends Duration> observableValue, Duration duration, Duration t1) {
                seekSlider.setValue(t1.toSeconds());
            }
        });
        slider.setValue(currentPlayer().getVolume() * 100);

        currentPlayer().play();

    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

    }

}
