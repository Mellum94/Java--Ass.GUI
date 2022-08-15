import javafx.application.Application;
import javafx.scene.input.*;
import javafx.event.ActionEvent;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.Scene;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import java.util.Optional;
import javafx.event.EventHandler;
import java.io.File;
import javafx.stage.FileChooser;
import java.io.FileNotFoundException;
import javafx.scene.control.Alert.*;
import javafx.scene.control.TextArea;
import java.util.ArrayList;
import java.util.List;

public class LabyrintGUI extends Application{
	protected GUIrute[][] ruter;
	private Labyrint labyrint;
	protected File fil;
	protected BorderPane root;
	protected Stage stage;
	protected VBox topp;
	protected boolean LABBYREADDYTOGO = false;
	protected List<String> losninger;
	protected HBox panel = new HBox(50);
	protected HBox nesteOgForrige = new HBox(50);

	protected int naavaerendeLosning;
	protected int antallLosninger;

	protected int[] startRute= new int[2];

	public void los(int rad, int kol){
		startRute[0] = rad; startRute[1] = kol;
		System.out.println("Prøver å løse fra (" + kol + "," + rad + ")" );
		final long start = System.currentTimeMillis();
		losninger = labyrint.finnUtveiFra(kol+1, rad+1);
		antallLosninger = losninger.size();
		naavaerendeLosning = 0;
		visLosning(0);
			
		
		settStatusBar();
		settNesteOgForrige();
		stage.sizeToScene();


		
		final long slutt = System.currentTimeMillis();
		System.out.println("" + (double)(slutt-start)/1000 + " sekunder");
	}
	public void settNesteOgForrige(){
		nesteOgForrige.getChildren().clear();
		Button forrige = new Button("Forrige");
		forrige.setOnAction(new EventHandler<ActionEvent>() {
			public void handle(ActionEvent event)
			{
				if (naavaerendeLosning>0){
					visLosning(--naavaerendeLosning);
					settStatusBar();
				}
			}
		});

		Button neste = new Button("Neste");
		neste.setOnAction(new EventHandler<ActionEvent>() {
			public void handle(ActionEvent event)
			{
				if ((naavaerendeLosning+1)<antallLosninger){
					visLosning(++naavaerendeLosning);
					settStatusBar();
				}
			}
		});
		nesteOgForrige.getChildren().add(forrige);
		nesteOgForrige.getChildren().add(neste);

	}
	public void settStatusBar()
	{
		panel.getChildren().clear();
		TextArea tekst = new TextArea("Løsninger: " + losninger.size());
		tekst.setMaxHeight(10);
		tekst.setMaxWidth(130);
		tekst.setBackground(Background.EMPTY);
		panel.getChildren().add(tekst);

		tekst = new TextArea("Nåværende løsning: " + (naavaerendeLosning+1));
		tekst.setMaxHeight(10);
		tekst.setMaxWidth(190);
		tekst.setBackground(Background.EMPTY);
		panel.getChildren().add(tekst);
	}
		
	public void visLosning(int nr)
	{
		if (!losninger.isEmpty()){
			boolean[][] tabell = losningStringTilTabell(losninger.get(nr), labyrint.kolonner, labyrint.rader);
			for (int radCount = 0; radCount<labyrint.rader;radCount++){
				for (int kolCount=0;kolCount < labyrint.kolonner;kolCount++){
					ruter[radCount][kolCount].fjernGronn();
					if (tabell[radCount][kolCount]){
						ruter[radCount][kolCount].settGronn();
					}
				}
			}
			try{
				ruter[startRute[0]][startRute[1]].setBackground(new Background(
					new BackgroundFill(Color.RED, null, null)));
			}
			catch(Exception e) {}
		}
	
	}

	static boolean[][] losningStringTilTabell(String losningString, int	bredde, int	hoyde) {
		boolean[][] losning = new boolean[hoyde][bredde];
		java.util.regex.Pattern p = java.util.regex.Pattern.compile("\\(([0-9]+),([0-9]+)\\)");
		java.util.regex.Matcher m = p.matcher(losningString.replaceAll("\\s",""));
		while(m.find()) {
			int x = Integer.parseInt(m.group(1))-1;
			int	y = Integer.parseInt(m.group(2))-1;
			losning[y][x] = true;
		}
		return losning;
	}

	@Override
	public void start(Stage stageIn){
		root = new BorderPane();
		this.stage = stageIn;
		panel.setMinHeight(50);
		panel.setBackground(Background.EMPTY);
		topp = new VBox(lagToppBoks(), panel, nesteOgForrige);
		root.setTop(topp);
		Pane a = new Pane();
		a.setMinHeight(25);
		root.setBottom(a);
		

		stage.setScene(new Scene(root));
		stage.setTitle("Labyrintløser");
		stage.show();
	}
	
	private HBox lagToppBoks() {
		TextField filFelt = new TextField();
		Button velgFilKnapp = new Button("Velg fil...");
		velgFilKnapp.setOnAction(new FilVelger(filFelt));

		Button lastInnKnapp = new Button("Last inn");
		lastInnKnapp.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				try {
					labyrint = Labyrint.lesFraFil(fil);
					labyrint.settMinimalUtskrift();
					GridPane rutenett = new GridPane();
					ruter = new GUIrute[labyrint.rader][labyrint.kolonner];

					for (int rad = 0; rad<labyrint.rader;rad++){
						for (int kol=0;kol < labyrint.kolonner;kol++){
							ruter[rad][kol] = new GUIrute(labyrint.array[rad][kol] instanceof HvitRute, rad,kol);
							rutenett.add(ruter[rad][kol], kol, rad);
						}
					}
					root.setCenter(rutenett);
					stage.sizeToScene();

							} catch(FileNotFoundException e) {}
						}
					});
		
		return new HBox(50,velgFilKnapp, filFelt, lastInnKnapp);
	}
	private class FilVelger implements EventHandler<ActionEvent> {
		TextField filFelt;
		public FilVelger(TextField filFelt){
			this.filFelt = filFelt;
		}

		@Override
		public void handle(ActionEvent event){
			panel.getChildren().clear();
			nesteOgForrige.getChildren().clear();
			FileChooser fileChooser = new FileChooser();
			fileChooser.setTitle("Velg fil");
			File selectedFile = fileChooser.showOpenDialog(null);
			fil = selectedFile;
			System.out.println("Fil ble satt inn");
			if (selectedFile != null){
				filFelt.setText(selectedFile.getPath());
			}
		}
		
	}

	private class GUIrute extends Pane {
		boolean erHvit;
		boolean erGronn;
		private int rad;
		private int kolonne;

		public GUIrute(boolean erHvitIn, int radIn, int kolonneIn){
			this.erHvit = erHvitIn;
			this.rad = radIn;
			this.kolonne = kolonneIn;
			if (erHvit){
				setBackground(new Background(
				new BackgroundFill(Color.WHITE, null, null)));
			}
			else {
				setBackground(new Background(
					new BackgroundFill(Color.BLACK, null, null)));
			}

			setBorder(new Border(new BorderStroke(Color.BLACK, BorderStrokeStyle.SOLID,
				null, new BorderWidths(1))));

			setMinWidth(10);
			setMinHeight(10);

			addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
				@Override
				public void handle(MouseEvent event)
				{
					if(erHvit){
						los(rad, kolonne);
					}
					else{
						Alert alert = new Alert(AlertType.NONE, "Du kan ikke gå fra en svart rute, din tufs", 
							ButtonType.YES);
							alert.showAndWait();
							if (alert.getResult() == ButtonType.YES) {
								alert.close();
							}
					}
				}
			});

		}
		public void settGronn(){
			setBackground(new Background(
				new BackgroundFill(Color.GREEN, null, null)));
			erGronn = true;
		}
		public void fjernGronn(){
			if (erGronn){
				erGronn = false;
				if (erHvit){
					setBackground(new Background(
				new BackgroundFill(Color.WHITE, null, null)));
				}
				else{
					setBackground(new Background(
				new BackgroundFill(Color.BLACK, null, null)));
				}
			}
		}
		public void oppdaterFarge(){
			if (erHvit) {
				setBackground(new Background(
					new BackgroundFill(Color.WHITE, null, null)));
			}
			else {
				setBackground(new Background(
					new BackgroundFill(Color.BLACK, null, null)));
			}
		}
	}
}