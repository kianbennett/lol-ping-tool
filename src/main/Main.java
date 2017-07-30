package main;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javafx.animation.Animation;
import javafx.animation.Interpolator;
import javafx.animation.RotateTransition;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Spinner;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.RowConstraints;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;

public class Main extends Application {
	
	class Region {
		public String name;
		public String ip;
		public Text text;
		public String details;
		public ImageView icon;
		public RotateTransition imageRotation;
		public Button button;
		public Runnable runnable;
		
		public Region(String name, String ip) {
			this.name = name;
			this.ip = ip;
		}
	}
	
	private Region[] regions = new Region[] {
		new Region("NA", "104.160.131.3"),
		new Region("EUW", "104.160.141.3"),
		new Region("EUNE", "104.160.142.3"),
		new Region("OCE", "104.160.156.3"),
		new Region("LAN", "104.160.136.3"),
	};
	
	private BorderPane borderPane;
	private Text textExternalIp;
	private Image imageLoading, imageError, imageReload, imageSettings;
	private GridPane contentGrid;
	private TextArea textArea;
	private Button buttonReload;
	private Stage stageOptions;
	private Spinner<Integer> requestCountSpinner;
	
	private Map<Region, Boolean> haveRegionsFinished = new HashMap<Region, Boolean>();
	
    @Override
    public void start(Stage primaryStage) {
		imageLoading = new Image(getClass().getResourceAsStream("loading.png"), 80, 80, true, true);
		imageError = new Image(getClass().getResourceAsStream("error.png"), 80, 80, true, true);
		imageReload = new Image(getClass().getResourceAsStream("reload.png"), 80, 80, true, true);
		imageSettings = new Image(getClass().getResourceAsStream("settings.png"), 80, 80, true, true);
		
//        loadingImageView = new ImageView(imageLoading);
//        loadingImageView.setFitHeight(60); 
//        loadingImageView.setFitWidth(60);
//        loadingImageView.setTranslateY(-10);
		
        borderPane = new BorderPane();
        borderPane.setCenter(contentGrid = buildCentre());
    	borderPane.setTop(buildTop());
    	borderPane.setBottom(buildBottom());

        Scene scene = new Scene(borderPane, 400, 480);
        primaryStage.setTitle("LoL Ping Tool");
        primaryStage.setScene(scene);
        primaryStage.show();
        
        stageOptions = buildOptionsMenu();
        
        Thread thread = new Thread(new Runnable() {
        	public void run() {
        		textExternalIp.setText("Getting external IP...");
        		String externalIp = null;
        		try {
        			URL url = new URL("http://checkip.amazonaws.com");
        			BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
        			externalIp = in.readLine();
        		} catch(Exception e) {
        			e.printStackTrace();
        		}
        		textExternalIp.setText(externalIp != null ? "Pinging from " + externalIp : "Cannot get external IP (are you connected to the internet?)");
        	}
        });
        thread.start();
        
        pingServers();
    }
    
    private void pingServers() {
    	buttonReload.setDisable(true);
    	textArea.setText("Pinging servers..." + System.lineSeparator());
    	
    	haveRegionsFinished.clear();
    	for(Region region : regions) {
    		haveRegionsFinished.put(region, false);
    	}
    	
    	for(int i = 0; i < regions.length; i++) {
			Region region = regions[i];
			int index = i;
			
			Platform.runLater(new Runnable() {
    			public void run() {
    				if(contentGrid.getChildren().contains(region.text)) {
    					contentGrid.getChildren().remove(region.text);
        				contentGrid.add(region.icon, 2, index);	
    				}
    			}
    		});
			
			region.icon.setImage(imageLoading);
			region.imageRotation.play();
			
			Thread thread = new Thread(region.runnable);
			thread.start();
		}
    }
    
    private GridPane buildCentre() {
    	GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(25, 25, 0, 25));
//        grid.setGridLinesVisible(true);
        
        ColumnConstraints col1 = new ColumnConstraints();
        ColumnConstraints col2 = new ColumnConstraints();
        ColumnConstraints col3 = new ColumnConstraints();
        ColumnConstraints col4 = new ColumnConstraints();
        col3.setHgrow(Priority.ALWAYS);
        
        grid.getColumnConstraints().addAll(col1, col2, col3, col4);
        
        RowConstraints row = new RowConstraints();
        row.setVgrow(Priority.NEVER);
        row.setMinHeight(30);
        grid.getRowConstraints().addAll(row, row, row, row, row);
        
        for(int i = 0; i < regions.length; i++) {
        	final int index = i;
        	Region region = regions[i];
        	grid.add(new Text(region.name), 0, i);
        	grid.add(new Text("(" + region.ip + ")"), 1, i);
        	
        	region.text = new Text("0 ms");
        	GridPane.setHalignment(region.text, HPos.RIGHT);
        	GridPane.setMargin(region.text, new Insets(0, 0, 0, 0));
        	region.text.setFont(Font.font("Tahoma", FontWeight.NORMAL, 16));
        	
        	region.icon = new ImageView(imageLoading);
        	region.icon.setFitHeight(30); 
        	region.icon.setFitWidth(30);
            GridPane.setHalignment(region.icon, HPos.RIGHT);
            
            region.imageRotation = new RotateTransition(new Duration(1500), region.icon);
            region.imageRotation.setByAngle(360);
            region.imageRotation.setCycleCount(Animation.INDEFINITE);
            region.imageRotation.setInterpolator(Interpolator.LINEAR);
            
            grid.add(region.icon, 2, i);
        	
        	region.button = new Button("...");
        	GridPane.setHalignment(region.button, HPos.RIGHT);
        	region.button.setStyle("-fx-background-color: -fx-outer-border, -fx-inner-border, -fx-body-color; -fx-background-insets: 0, 1, 2; -fx-background-radius: 5, 4, 3; ");
        	region.button.setDisable(true);
        	region.button.setOnAction(new EventHandler<ActionEvent>() {
        	    @Override public void handle(ActionEvent e) {
        	    	textArea.setText(regions[index].details);
        	    	textArea.positionCaret(textArea.getText().length());
        	    	textArea.setScrollTop(Double.MAX_VALUE);
        	    	textArea.deselect();
        	    	textArea.selectPositionCaret(textArea.getText().length() * 2);
        	    }
        	});
        	grid.add(region.button, 3, i);
        	
        	region.runnable = new Runnable() {
    			public void run() {
    				boolean success = false;
    				try {
                        String s = null;
                        List<String> commands = new ArrayList<String>();
                        commands.add("ping");
                        commands.add(region.ip);
                        commands.add(System.getProperty("os.name").contains("Windows") ? "-n" : "-c"); // Windows uses -n, mac & linux use -c
                        commands.add(requestCountSpinner.getValue().toString());
                        ProcessBuilder processbuilder = new ProcessBuilder(commands);
                        Process process = processbuilder.start();
                        BufferedReader stdInput = new BufferedReader(new InputStreamReader(process.getInputStream()));
                        region.details = "";
                        region.button.setDisable(true);
                        while ((s = stdInput.readLine()) != null) {
                        	s = s.trim();
                        	if(s.isEmpty()) continue;
                        	region.details += (region.details.isEmpty() ? "" : System.lineSeparator()) + s; // only precede the line with a separator after the first line
                        	
                        	String[] split = s.split(" ");
                        	
                        	int ping = 0;
                        	// Windows
                    		if(s.startsWith("Minimum")) {
                        		ping = Integer.parseInt(split[split.length - 1].replaceAll("ms", ""));
                        	}	
                    		// Mac || Linux
                    		if(s.startsWith("round-trip") || s.startsWith("rtt")) {
                    			ping = (int) Double.parseDouble(split[3].split("/")[1]);
                    		}
                    		region.text.setText(ping + " ms");
                        }
                        
                        success = true;
                    } catch (Exception e) {
                    	e.printStackTrace();
                    }
    				
    				final boolean s = success;
    				
    				Platform.runLater(new Runnable() {
	        			public void run() {
	        				if(s) {
	        					if(contentGrid.getChildren().contains(region.icon)) {
	        						contentGrid.getChildren().remove(region.icon);
	    	        				contentGrid.add(region.text, 2, index);	
	        					}
	        				} else {
	        					region.icon.setImage(imageError);
	        				}
	        				region.imageRotation.stop();
	        				region.icon.setRotate(0);
	        				
	        				region.button.setDisable(false);
	        				
	        				haveRegionsFinished.put(region, true);
	        				
	        				boolean finished = true;
	        				for(boolean value : haveRegionsFinished.values()) {
	        					if(!value) finished = false;
	        				}

	        				if(finished) {
	        					buttonReload.setDisable(false);
	        					textArea.appendText("Finished pinging all servers!");
	        				}
	        			}
	        		});
    			}
    		};
        }
        
        for(Node node : grid.getChildren()) {
        	if(node instanceof Text) {
        		((Text) node).setFont(Font.font("Tahoma", FontWeight.NORMAL, 16));
        	}
        }
        
        textArea = new TextArea("");
        textArea.setEditable(false);
        textArea.setStyle("-fx-background-color: -fx-outer-border, -fx-inner-border, -fx-body-color; -fx-background-insets: 0, 1, 2; -fx-background-radius: 5, 4, 3; ");
        GridPane.setMargin(textArea, new Insets(10, 0, 0, 0));
        
        grid.add(textArea, 0, regions.length, 4, 1);
        
		return grid;
    }
    
    private StackPane buildTop() {
    	StackPane stackTop = new StackPane();
    	stackTop.setPadding(new Insets(20, 20, 20, 20));
    	
    	Text title = new Text("LoL Ping Tool");
        title.setFont(Font.font("Tahoma", FontWeight.NORMAL, 20));
        stackTop.getChildren().add(title);
        
		textExternalIp = new Text();
        textExternalIp.setTranslateY(30);
        stackTop.getChildren().add(textExternalIp);
        
        ImageView imageView = new ImageView(imageReload);
        imageView.setFitWidth(30);
        imageView.setFitHeight(30);
        buttonReload = new Button("", imageView);
        buttonReload.setStyle(
            "-fx-max-width: 40px; " +
            "-fx-max-height: 40px;" +
            "-fx-background-color: -fx-outer-border, -fx-inner-border, -fx-body-color; -fx-background-insets: 0, 1, 2; -fx-background-radius: 5, 4, 3;"
        );
        StackPane.setAlignment(buttonReload, Pos.CENTER_RIGHT);
        buttonReload.setDisable(true);
        buttonReload.setOnAction(new EventHandler<ActionEvent>() {
    	    @Override public void handle(ActionEvent e) {
    	    	pingServers();
    	    }
    	});
        stackTop.getChildren().add(buttonReload);
        
        ImageView imageViewSettings = new ImageView(imageSettings);
        imageViewSettings.setFitWidth(30);
        imageViewSettings.setFitHeight(30);
        Button buttonSettings = new Button("", imageViewSettings);
        buttonSettings.setStyle(
            "-fx-max-width: 40px; " +
            "-fx-max-height: 40px;" +
            "-fx-background-color: -fx-outer-border, -fx-inner-border, -fx-body-color; -fx-background-insets: 0, 1, 2; -fx-background-radius: 5, 4, 3;"
        );
        StackPane.setAlignment(buttonSettings, Pos.CENTER_LEFT);
        buttonSettings.setOnAction(new EventHandler<ActionEvent>() {
    	    @Override public void handle(ActionEvent e) {
    	    	stageOptions.show();
    	    }
    	});
        stackTop.getChildren().add(buttonSettings);
        
        return stackTop;
    }
    
    private StackPane buildBottom() {
    	StackPane stackBottom = new StackPane();
        stackBottom.setPadding(new Insets(10));
        
        Text version = new Text("Version 0.1");
        Text author = new Text("Kian Bennett 2017");
        StackPane.setAlignment(version, Pos.BOTTOM_LEFT);
        StackPane.setAlignment(author, Pos.BOTTOM_RIGHT);
        stackBottom.getChildren().add(version);
        stackBottom.getChildren().add(author);
        
        return stackBottom;
    }
    
    private Stage buildOptionsMenu() {
    	GridPane grid = new GridPane();
        grid.setHgap(40);
        grid.setVgap(10);
        grid.setPadding(new Insets(10, 0, 0, 0));
        grid.setAlignment(Pos.TOP_CENTER);
//        grid.setGridLinesVisible(true);
        
        grid.add(new Text("Request Count: "), 0, 0);
        
        Text textTip = new Text("(Higher count = slower but more accurate)");
        GridPane.setHalignment(textTip, HPos.CENTER);
        grid.add(textTip, 0, 1, 2, 1);
        
        requestCountSpinner = new Spinner<Integer>(1, 10, 2);
        requestCountSpinner.setStyle(
            "-fx-max-width: 60px; " +
    		"-fx-background-color: -fx-outer-border, -fx-inner-border, -fx-body-color; -fx-background-insets: 0, 1, 2; -fx-background-radius: 5, 4, 3; "
		);
        GridPane.setHalignment(requestCountSpinner, HPos.RIGHT);
        grid.add(requestCountSpinner, 1, 0);
        
        Button button = new Button("Close");
    	button.setStyle("-fx-background-color: -fx-outer-border, -fx-inner-border, -fx-body-color; -fx-background-insets: 0, 1, 2; -fx-background-radius: 5, 4, 3; ");
    	button.setOnAction(new EventHandler<ActionEvent>() {
    	    @Override public void handle(ActionEvent e) {
    	    	stageOptions.hide();
    	    }
    	});
    	GridPane.setHalignment(button, HPos.CENTER);
    	GridPane.setColumnSpan(button, 2);
    	GridPane.setMargin(button, new Insets(20, 0, 0, 0));
    	grid.add(button, 0, 2);
    	
    	Stage stage = new Stage();
    	stage.initModality(Modality.WINDOW_MODAL);
    	stage.initOwner(contentGrid.getScene().getWindow());
    	stage.setTitle("Settings");
    	stage.setResizable(false);
    	stage.setScene(new Scene(grid, 250, 120));
    	
		return stage;
    }
    
    public static void main(String[] args) {
        launch(args);
    }
}