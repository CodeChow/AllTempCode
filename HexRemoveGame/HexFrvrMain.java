
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;


public class HexFrvrMain extends Application {

	private void startGame(Pane pane)
	{
		Hex hex = new Hex(0);
		pane.setStyle("-fx-background-color: #20201e");
		HexBlockCreate hbc = new HexBlockCreate(3);
		hex.initizeHex(100, 85, 40, pane);
		for(int i = 0; i < 3; i++)
		{
			hbc.createBlock(i);
			hbc.drawBlock(i, 900, (i + 1) * 200 - 100, 30, pane);
		}
	}
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		Application.launch(args);
	}
	
	public void start(Stage primaryStage)
	{
		Pane pane = new Pane();
		startGame(pane);
		Scene scene = new Scene(pane, 1200, 750);
		primaryStage.setScene(scene);
		primaryStage.show();
	}
}
