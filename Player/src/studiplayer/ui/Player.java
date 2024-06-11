package studiplayer.ui;

import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.control.ChoiceBox;
import javafx.scene.image.ImageView;
import javafx.scene.image.Image;
import javafx.scene.control.TitledPane;
import javafx.scene.control.ContentDisplay;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import javafx.scene.control.Label;
import java.io.File;
import javafx.application.Application;

import javafx.geometry.Pos;
import javafx.application.Platform;
import java.net.URL;
import javafx.event.ActionEvent;
import studiplayer.audio.SortCriterion;
import studiplayer.audio.AudioFile;
import javafx.geometry.Insets;
import javafx.scene.layout.HBox;
import javafx.scene.layout.BorderPane;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import studiplayer.audio.NotPlayableException;
import javafx.event.Event;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.scene.layout.GridPane;
import studiplayer.audio.PlayList;

public class Player extends Application
{
    public static final String DEFAULT_PLAYLIST = "playlists/DefaultPlayList.m3u";
    private static final String PLAYLIST_DIRECTORY = "";
    private static final String INITIAL_PLAY_TIME_LABEL = "00:00";
    private static final String NO_CURRENT_SONG = " - ";
    private PlayList playList;
    private boolean useCertPlayList = false;
    
    private Button playButton;
    private Button pauseButton;
    private Button stopButton;
    private Button nextButton;
    private Button filterButton;
    
    private Label playListLabel;
    private Label playTimeLabel;
    private Label currentSongLabel;
    
    private PlayerThread playerThread;
    private TimerThread timerThread;
    private ChoiceBox<SortCriterion> sortChoiceBox;
    private TextField searchTextField;
    private SongTable songTable;
    
    
	public	Player() {}
	
	public PlayList getPlayList() {return playList;}
	public void setPlayList(PlayList playList) {this.playList = playList;}
	public boolean getUseCertPlayList(){return this.useCertPlayList;}
	public void setUseCertPlayList(boolean useCertPlayList) {this.useCertPlayList = useCertPlayList;}
	
	
	public void playCurrentSong(ActionEvent e)
	{
		System.out.println("playing " + playList.currentAudioFile().toString());
		updateSongInfo(playList.currentAudioFile());
		updateButtonStates(false, true, true, true);
	    playerThread = new PlayerThread(playList.currentAudioFile());
	    playerThread.start();
	    timerThread = new TimerThread();
	    timerThread.start();
	}
	
	
	public void pauseCurrentSong(ActionEvent e)
	{
		System.out.println("Pausing " + playList.currentAudioFile().toString());
		playList.currentAudioFile().togglePause();
	}

	public void	stopCurrentSong(ActionEvent e)
	{
		Thread stopThread = new Thread() {public void run() {playList.currentAudioFile().stop();}};
		stopThread.run();
		playerThread.terminate();
		timerThread.terminate();
		playList.resetIterator();
		updateButtonStates(true, false, false, true);		
		Platform.runLater(() -> playTimeLabel.setText(INITIAL_PLAY_TIME_LABEL));
		System.out.println("Stopping " + playList.currentAudioFile().toString());
	}

	public void	switchCurrentSong(ActionEvent e)
	{
		Thread stopThread = new Thread()
		{public void run() {	playList.currentAudioFile().stop();}};
		stopThread.run();
		playerThread.terminate();
		timerThread.terminate();
        playList.nextSong();
        System.out.println("Switching " + playList.currentAudioFile().toString());
        playCurrentSong(e);
        Platform.runLater(() -> playTimeLabel.setText(INITIAL_PLAY_TIME_LABEL));
        updateSongInfo(playList.currentAudioFile());
	}
	
	public void addIteam() 
	{
		sortChoiceBox.getItems().add(SortCriterion.DEFAULT);
		sortChoiceBox.getItems().add(SortCriterion.AUTHOR);
		sortChoiceBox.getItems().add(SortCriterion.TITLE);
		sortChoiceBox.getItems().add(SortCriterion.ALBUM);
		sortChoiceBox.getItems().add(SortCriterion.DURATION);
		sortChoiceBox.setValue(SortCriterion.DEFAULT);
	}
	
	public void start(Stage stage) throws Exception
	{
		if (useCertPlayList)
			loadPlayList("playlists/playList.cert.m3u");
		else
		{
			final FileChooser fileChooser = new FileChooser();
			File file = fileChooser.showOpenDialog(stage);
			loadPlayList(Objects.isNull(file) ? null : file.getAbsolutePath());
		}
		BorderPane mainPane = new BorderPane();
		stage.setTitle("APA Player");
		Scene scene = new Scene(mainPane, 600, 400);

		// Filter block
		GridPane filterByFeatures = new GridPane();
		filterByFeatures.setVgap(4);
		filterByFeatures.setHgap(16);
		filterByFeatures.setPadding(new Insets(5, 5, 5, 5));

		filterByFeatures.add(new Label("Search text"), 0, 0);
		searchTextField = new TextField();
		filterByFeatures.add(searchTextField, 1, 0);

		filterByFeatures.add(new Label("Sort by"), 0, 1);
		sortChoiceBox = new ChoiceBox<SortCriterion>();
		addIteam();
		searchTextField.setMaxWidth(200);
		sortChoiceBox.setMaxWidth(200);
		filterByFeatures.add(sortChoiceBox, 1, 1);

		filterButton = new Button("Display");
		filterButton.setOnAction(this::filterSongs);
		filterByFeatures.add(filterButton, 2, 1);

		mainPane.setTop(new TitledPane("Filter", filterByFeatures));

		songTable = new SongTable(playList);
		songTable.setRowSelectionHandler(this::SelectSong);
		mainPane.setCenter(songTable);

		VBox bottomPean = new VBox();

		GridPane infoBlock = new GridPane();
		infoBlock.setVgap(10);
		infoBlock.setHgap(10);
		infoBlock.setPadding(new Insets(5, 5, 5, 5));

		infoBlock.add(new Label("Playlist"), 0, 0);
		playListLabel = new Label(playList.getPath());
		infoBlock.add(playListLabel, 1, 0);

		infoBlock.add(new Label("Current Song"), 0, 1);
		if (Objects.isNull(playList.currentAudioFile()))
			currentSongLabel = new Label(NO_CURRENT_SONG);
		else
			currentSongLabel = new Label(playList.currentAudioFile().toString());
		infoBlock.add(currentSongLabel, 1, 1);

		infoBlock.add(new Label("Playtime"), 0, 2);
		if (Objects.isNull(playList.currentAudioFile()))
			playTimeLabel = new Label(INITIAL_PLAY_TIME_LABEL);
		else
			playTimeLabel = new Label(playList.currentAudioFile().formatPosition());
		infoBlock.add(playTimeLabel, 1, 2);
		bottomPean.getChildren().add(infoBlock);

		HBox buttons = new HBox();
		buttons.setAlignment(Pos.BOTTOM_CENTER);
		buttons.setPadding(new Insets(5, 5, 10, 5));

		playButton = createButton("play.jpg");
		pauseButton = createButton("pause.jpg");
		stopButton = createButton("stop.jpg");
		nextButton = createButton("next.jpg");
		stopButton.setDisable(true);
		pauseButton.setDisable(true);

		playButton.setOnAction(this::playCurrentSong);
		pauseButton.setOnAction(this::pauseCurrentSong);
		stopButton.setOnAction(this::stopCurrentSong);
		nextButton.setOnAction(this::switchCurrentSong);

		buttons.getChildren().add(pauseButton);
		buttons.getChildren().add(stopButton);
		buttons.getChildren().add(playButton);
		buttons.getChildren().add(nextButton);

		bottomPean.getChildren().add(buttons);

		mainPane.setBottom(bottomPean);

		stage.setScene(scene);
		stage.show();

	}
	private void SelectSong(Event e) 
	{
		if (!Objects.isNull(playList.currentAudioFile()))
			playList.currentAudioFile().stop();
		playList.jumpToAudioFile(songTable.getSelectionModel().getSelectedItem().getAudioFile());
		updateSongInfo(playList.currentAudioFile());
		updateButtonStates(true, true, true, true);
	}
	
    private void filterSongs(ActionEvent e)
    {
		playList.setSearch(searchTextField.getText().toString());
		playList.setSortCriterion(sortChoiceBox.getValue());

        Platform.runLater(() -> {
			songTable.refreshSongs();
			if (Objects.isNull(playList.getIterator()))
				playList.resetIterator();
			else
				playList.getIterator().setInx(0);
		});
		Thread stopThread = new Thread() 
		{
			public void run() {playList.currentAudioFile().stop();}
		};
		stopThread.run();
		playerThread.terminate();
		timerThread.terminate();
    }

    private void updateSongInfo(AudioFile song)
    {
    	Platform.runLater(() -> {
    		if (song == null)
    		{
    			playTimeLabel.setText(INITIAL_PLAY_TIME_LABEL);
    			currentSongLabel.setText(NO_CURRENT_SONG);
    		}
    		else
    		{
    			playTimeLabel.setText(song.formatDuration());
    			currentSongLabel.setText(song.toString());
    		}
    	});
    }
    
    
	public void loadPlayList(String pathname)
    {
        if (pathname == null || pathname.isEmpty())
		    playList = new PlayList(PLAYLIST_DIRECTORY + DEFAULT_PLAYLIST);
		else
		    playList = new PlayList(pathname);
        Platform.runLater(() -> {
        	AudioFile a = playList.currentAudioFile();
        	if (a != null)
        		playListLabel.setText(a.getPathname());
        	else
        		playListLabel.setText(PLAYLIST_DIRECTORY + DEFAULT_PLAYLIST);
			songTable.refreshSongs();
			if (Objects.isNull(playList.getIterator()))
				playList.resetIterator();
			else
				playList.getIterator().setInx(0);
		});
    }
	
	
	private class PlayerThread extends Thread
	{
		private AtomicBoolean stopped = new AtomicBoolean(false);
		private AudioFile audioFile;
		public void terminate() {stopped.set(true);}

		public PlayerThread(AudioFile file)
		{
			super();
			audioFile = file;
		}

		public void run() 
		{
			try
			{
				audioFile.play();
			}
			catch (NotPlayableException e)
			{
				e.printStackTrace();
			}
			while (!stopped.get()) {}
		}
	}

	private class TimerThread extends Thread 
	{
		private AtomicBoolean stopped = new AtomicBoolean(false);

		public void terminate() {stopped.set(true);}
		public void run() 
		{
			while (!stopped.get()) 
			{
				if (Objects.isNull(playList.currentAudioFile()))
					break;
				else 
					Platform.runLater(() -> {
						if (!stopped.get())
							playTimeLabel.setText(playList.currentAudioFile().formatPosition());
					});
				try 
				{
					Thread.sleep(1000);
				}
				catch (InterruptedException e) 
				{
					break;
				}
			}
		}

	}
	
    private void updateButtonStates(boolean play, boolean pause, boolean stop, boolean next) 
    {
    	Platform.runLater(() -> {
			playButton.setDisable(!play);
			pauseButton.setDisable(!pause);
			stopButton.setDisable(!stop);
			nextButton.setDisable(!next);
		});
    }

    private Button createButton(String iconfile) 
    {
    	Button button = null;
    	try 
    	{
    		URL url = getClass().getResource("/icons/" + iconfile);
    		Image icon = new Image(url.toString());
    		ImageView imageView = new ImageView(icon);
    		imageView.setFitHeight(20);
    		imageView.setFitWidth(20);
    		button = new Button("", imageView);
    		button.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
    		button.setStyle("-fx-background-color: #fff;");
    	} 
    	catch (Exception e)
    	{
    		System.out.println("Image " + "icons/" + iconfile + " not found!");
    		System.exit(-1);
    	}
    	return button;
    }
	
	public void main(String[] args)
	{
		launch(args);
	}
}

//package studiplayer.ui;
//
//import javafx.scene.control.Button;
//import javafx.scene.control.ChoiceBox;
//import javafx.scene.control.ContentDisplay;
//import javafx.scene.control.Label;
//import javafx.scene.control.TextField;
//import javafx.scene.control.TitledPane;
//import javafx.scene.image.Image;
//import javafx.scene.image.ImageView;
//
//import java.io.File;
//import java.net.URL;
//import java.util.Objects;
//import java.util.concurrent.atomic.AtomicBoolean;
//
//import javafx.application.Application;
//import javafx.application.Platform;
//import javafx.event.ActionEvent;
//import javafx.event.Event;
//import javafx.geometry.Insets;
//import javafx.geometry.Pos;
//import javafx.scene.Scene;
//import javafx.scene.layout.BorderPane;
//import javafx.scene.layout.GridPane;
//import javafx.scene.layout.HBox;
//import javafx.scene.layout.VBox;
//import javafx.stage.FileChooser;
//import javafx.stage.Stage;
//import studiplayer.audio.AudioFile;
//import studiplayer.audio.NotPlayableException;
//import studiplayer.audio.PlayList;
//import studiplayer.audio.SortCriterion;
//
//public class Player extends Application {
//	public static final String DEFAULT_PLAYLIST = "playlists/DefaultPlayList.m3u";
//	private static final String PLAYLIST_DIRECTORY = "";
//	private static final String INITIAL_PLAY_TIME_LABEL = "00:00";
//	private static final String NO_CURRENT_SONG = " - ";
//
//	boolean useCertPlayList = true;
//	private PlayList playList = new PlayList();
//
//	private Button playButton;
//	private Button pauseButton;
//	private Button stopButton;
//	private Button nextButton;
//	private Label playListLabel;
//	private Label playTimeLabel;
//	private Label currentSongLabel;
//	private ChoiceBox<SortCriterion> sortChoiceBox;
//	private TextField searchTextField;
//	private Button filterButton;
//	private SongTable songTable;
//
//	private PlayerThread playerThread;
//	private TimerThread timerThread;
//
//	public Player() {
//	}
//
//	public static void main(String[] args) {
//		launch();
//	}
//
//	@Override
//	public void start(Stage stage) throws Exception {
//		BorderPane pane = new BorderPane();
//		stage.setTitle("APA Player");
//
//		// Filter block
//		GridPane filterAccordionContent = new GridPane();
//		filterAccordionContent.setVgap(4);
//		filterAccordionContent.setHgap(16);
//		filterAccordionContent.setPadding(new Insets(5, 5, 5, 5));
//
//		filterAccordionContent.add(new Label("Search text"), 0, 0);
//		searchTextField = new TextField();
//		filterAccordionContent.add(searchTextField, 1, 0);
//
//		filterAccordionContent.add(new Label("Sort by"), 0, 1);
//		sortChoiceBox = new ChoiceBox<SortCriterion>();
//		sortChoiceBox.getItems().add(SortCriterion.DEFAULT);
//		sortChoiceBox.getItems().add(SortCriterion.AUTHOR);
//		sortChoiceBox.getItems().add(SortCriterion.TITLE);
//		sortChoiceBox.getItems().add(SortCriterion.ALBUM);
//		sortChoiceBox.getItems().add(SortCriterion.DURATION);
//		sortChoiceBox.setValue(SortCriterion.DEFAULT);
//		filterAccordionContent.add(sortChoiceBox, 1, 1);
//
//		filterButton = new Button("Display");
//		filterButton.setOnAction(this::onClickFilter);
//		filterAccordionContent.add(filterButton, 2, 1);
//
//		TitledPane filterAccordion = new TitledPane("Filter", filterAccordionContent);
//		pane.setTop(filterAccordion);
//
//		// Center Block
//		songTable = new SongTable(playList);
//		songTable.setRowSelectionHandler(this::onSelectSong);
//		pane.setCenter(songTable);
//
//		// Bottom Block
//		VBox bottomBlock = new VBox();
//
//		// Bottom block info
//		GridPane infoBlock = new GridPane();
//		infoBlock.setVgap(4);
//		infoBlock.setHgap(16);
//		infoBlock.setPadding(new Insets(5, 5, 5, 5));
//
//		infoBlock.add(new Label("Playlist"), 0, 0);
//		playListLabel = new Label(playList.getPath());
//		infoBlock.add(playListLabel, 1, 0);
//
//		infoBlock.add(new Label("Current Song"), 0, 1);
//		currentSongLabel = new Label(getCurrentSongTitle());
//		infoBlock.add(currentSongLabel, 1, 1);
//
//		infoBlock.add(new Label("Playtime"), 0, 2);
//		playTimeLabel = new Label(getCurrentSongPlayTime());
//		infoBlock.add(playTimeLabel, 1, 2);
//		bottomBlock.getChildren().add(infoBlock);
//
//		// Bottom buttons
//		HBox buttons = new HBox();
//		buttons.setAlignment(Pos.BOTTOM_CENTER);
//		buttons.setPadding(new Insets(5, 5, 10, 5));
//
//		playButton = createIconButton("play.jpg");
//		playButton.setOnAction(this::onClickPlay);
//		buttons.getChildren().add(playButton);
//
//		pauseButton = createIconButton("pause.jpg");
//		pauseButton.setDisable(true);
//		pauseButton.setOnAction(this::onClickPause);
//		buttons.getChildren().add(pauseButton);
//
//		stopButton = createIconButton("stop.jpg");
//		stopButton.setDisable(true);
//		stopButton.setOnAction(this::onClickStop);
//		buttons.getChildren().add(stopButton);
//
//		nextButton = createIconButton("next.jpg");
//		nextButton.setOnAction(this::onClickNext);
//		buttons.getChildren().add(nextButton);
//
//		bottomBlock.getChildren().add(buttons);
//
//		pane.setBottom(bottomBlock);
//
//		Scene scene = new Scene(pane, 600, 400);
//		stage.setScene(scene);
//		stage.show();
//
//		if (useCertPlayList) {
//			loadPlayList("playlists/playList.cert.m3u");
//		} else {
//			final FileChooser fileChooser = new FileChooser();
//			File file = fileChooser.showOpenDialog(stage);
//			String playListPathname = Objects.isNull(file) ? null : file.getAbsolutePath();
//			loadPlayList(playListPathname);
//		}
//	}
//
//	private class PlayerThread extends Thread {
//		private AtomicBoolean stopped = new AtomicBoolean(false);
//		private AudioFile audioFile;
//
//		public PlayerThread(AudioFile file) {
//			super();
//			audioFile = file;
//		}
//
//		public void run() {
//			try {
//				audioFile.play();
//			} catch (NotPlayableException e) {
//				e.printStackTrace();
//			}
//			while (!stopped.get()) {
//			}
//		}
//
//		public void terminate() {
//			stopped.set(true);
//		}
//	}
//
//	private class TimerThread extends Thread {
//		private AtomicBoolean stopped = new AtomicBoolean(false);
//
//		public void run() {
//
//			while (!stopped.get()) {
//				try {
//					var currentAudioFile = playList.currentAudioFile();
//					if (Objects.isNull(currentAudioFile)) {
//						break;
//					} else {
//						Platform.runLater(() -> {
//							if (!stopped.get()) {
//								playTimeLabel.setText(currentAudioFile.formatPosition());
//							}
//						});
//					}
//					sleep(1000);
//				} catch (InterruptedException e) {
//					e.printStackTrace();
//					break;
//				}
//			}
//
//		}
//
//		public void terminate() {
//			stopped.set(true);
//		}
//	}
//
//	private void playCurrentSong() {
//		var currentAudioFile = playList.currentAudioFile();
//		setSongInfo(currentAudioFile);
//		setActiveButtons(false, true, true, true);
//		playerThread = new PlayerThread(currentAudioFile);
//		playerThread.start();
//		timerThread = new TimerThread();
//		timerThread.start();
//		logAction("Playing", currentAudioFile);
//	}
//
//	private void stopCurrentSong() {
//		var stopThread = new Thread() {
//			public void run() {
//				playList.currentAudioFile().stop();
//			}
//		};
//		stopThread.run();
//		playerThread.terminate();
//		timerThread.terminate();
//	}
//
//	private void pauseCurrentSong() {
//		var currentAudioFile = playList.currentAudioFile();
//		currentAudioFile.togglePause();
//		logAction("Toggling pause", currentAudioFile);
//	}
//
//	private void onClickFilter(ActionEvent e) {
//		var search = searchTextField.getText().toString();
//		playList.setSearch(search);
//		playList.setSortCriterion(sortChoiceBox.getValue());
//		Platform.runLater(() -> {
//			songTable.refreshSongs();
//			if (Objects.isNull(playList.getIterator()))
//				playList.resetIterator();
//			else
//				playList.getIterator().setInx(0);
//		});
//		stopCurrentSong();
//	}
//
//	private void onClickPlay(ActionEvent e) {
//		playCurrentSong();
//	}
//
//	private void onClickPause(ActionEvent e) {
//		pauseCurrentSong();
//	}
//
//	private void onClickStop(ActionEvent e) {
//		stopCurrentSong();
//		setActiveButtons(true, false, false, true);
//		Platform.runLater(() -> playTimeLabel.setText(INITIAL_PLAY_TIME_LABEL));
//		logAction("Stopping", playList.currentAudioFile());
//	}
//
//	private void onClickNext(ActionEvent e) {
//		stopCurrentSong();
//		playList.nextSong();
////		System.out.println(playList.getCurrent());
//
//		logAction("Switching to next audio file", playList.currentAudioFile());
//		playCurrentSong();
//	}
//
//	private void onSelectSong(Event e) {
//		Song selectedSong = songTable.getSelectionModel().getSelectedItem();
//		var currentAudioFile = playList.currentAudioFile();
//		if (!Objects.isNull(currentAudioFile)) {
//			currentAudioFile.stop();
//		}
//
//		playList.jumpToAudioFile(selectedSong.getAudioFile());
//		setSongInfo(playList.currentAudioFile());
//		setActiveButtons(true, true, true, true);
//		logAction("Selecting audio file", currentAudioFile);
//	}
//
//	private void setSongInfo(AudioFile file) {
//		Platform.runLater(() -> {
//			if (Objects.isNull(file)) {
//				currentSongLabel.setText(NO_CURRENT_SONG);
//				playTimeLabel.setText(INITIAL_PLAY_TIME_LABEL);
//			} else {
//				currentSongLabel.setText(file.toString());
//				playTimeLabel.setText(file.formatPosition());
//			}
//		});
//	}
//
//	private void setActiveButtons(boolean playButtonState, boolean pauseButtonState, boolean stopButtonState,
//			boolean nextButtonState) {
//		Platform.runLater(() -> {
//			playButton.setDisable(!playButtonState);
//			pauseButton.setDisable(!pauseButtonState);
//			stopButton.setDisable(!stopButtonState);
//			nextButton.setDisable(!nextButtonState);
//		});
//	}
//
//	private void logAction(String action, AudioFile file) {
//		System.out.printf("%s: %s \n", action, file.toString());
//	}
//
//	public void setUseCertPlayList(boolean useCertPlayList) {
//		this.useCertPlayList = useCertPlayList;
//	}
//
//	public void loadPlayList(String pathname) throws NotPlayableException {
//		if (Objects.isNull(pathname)) {
//			playList.loadFromM3U(PLAYLIST_DIRECTORY + DEFAULT_PLAYLIST);
//		} else {
//			playList.loadFromM3U(pathname);
//		}
//		Platform.runLater(() -> {
//			playListLabel.setText(playList.getPath());
//			songTable.refreshSongs();
//			if (Objects.isNull(playList.getIterator()))
//				playList.resetIterator();
//			else
//				playList.getIterator().setInx(0);
//		});
//	}
//
//	private String getCurrentSongTitle() {
//		var currentSong = playList.currentAudioFile();
//		if (Objects.isNull(currentSong)) {
//			return Player.NO_CURRENT_SONG;
//		} else {
//			return currentSong.toString();
//		}
//	}
//
//	private String getCurrentSongPlayTime() {
//		var currentSong = playList.currentAudioFile();
//		if (Objects.isNull(currentSong)) {
//			return Player.INITIAL_PLAY_TIME_LABEL;
//		} else {
//			return currentSong.formatPosition();
//		}
//	}
//
//	private Button createIconButton(String iconfile) {
//		Button button = null;
//		try {
//			URL url = getClass().getResource("/icons/" + iconfile);
//			Image icon = new Image(url.toString());
//			ImageView imageView = new ImageView(icon);
//			imageView.setFitHeight(20);
//			imageView.setFitWidth(20);
//			button = new Button("", imageView);
//			button.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
//			button.setStyle("-fx-background-color: #fff;");
//		} catch (Exception e) {
//			System.out.println("Image " + "icons/" + iconfile + " not found!");
//			System.exit(-1);
//		}
//		return button;
//	}
//}
