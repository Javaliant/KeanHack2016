/*
Author: Luigi Vincent
*/

import javafx.application.Application;
import javafx.beans.binding.Bindings;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.geometry.HPos;
import javafx.scene.Scene;
import javafx.scene.text.Text;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

public class AC extends Application {
	private final int MAX_TEMPERATURE = 84;
	private final int MIN_TEMPERATURE = 58;
	private int startingTemperature = TemperatureLog.getTemp();
	private Text temperatureText;
	private IntegerProperty temperature = new SimpleIntegerProperty(startingTemperature);
	public static void main(String[] args) {
		launch(args);
	}

	@Override
	public void start(Stage stage) {
		GridPane gridpane = new GridPane();
		gridpane.setHgap(5);
		gridpane.setVgap(5);

		temperatureText = new Text();
		temperatureText.setId("temperature-text");
		temperatureText.textProperty().bind(Bindings.concat("Current Temperature: ").concat(temperature));
		gridpane.add(temperatureText, 0, 0);
		GridPane.setRowSpan(temperatureText, 2);

		Button upButton = new Button("", new ImageView(new Image("assets/arrow_up.png")));
		upButton.setOnAction(e -> {
			temperature.setValue(Math.min(MAX_TEMPERATURE, temperature.getValue() + 1));
			if (temperature.getValue() == 72) {
				temperatureText.setStyle(Setting.WARM.style());
			} else if (temperature.getValue() == 78) {
				temperatureText.setStyle(Setting.HOT.style());
			} else if (temperature.getValue() == 67) {
				temperatureText.setStyle(Setting.NEUTRAL.style());
			} else if (temperature.getValue() == 62) {
				temperatureText.setStyle(Setting.COOL.style());
			}
		});
		gridpane.add(upButton, 1, 0);

		Button downButton = new Button("", new ImageView(new Image("assets/arrow_down.png")));
		downButton.setOnAction(e -> {
			temperature.setValue(Math.max(MIN_TEMPERATURE, temperature.getValue() - 1));
			if (temperature.getValue() == 77) {
				temperatureText.setStyle(Setting.WARM.style());
			} else if (temperature.getValue() == 71) {
				temperatureText.setStyle(Setting.NEUTRAL.style());
			} else if (temperature.getValue() == 66) {
				temperatureText.setStyle(Setting.COOL.style());
			} else if (temperature.getValue() == 61) {
				temperatureText.setStyle(Setting.COLD.style());
			}
		});
		gridpane.add(downButton, 1, 1);

		Label logoLabel = new Label("", new ImageView(new Image("assets/logo.png")));
		gridpane.add(logoLabel, 0, 2);
		GridPane.setHalignment(logoLabel, HPos.CENTER);
		GridPane.setColumnSpan(logoLabel, 2);

		Scene scene = new Scene(gridpane);
		scene.getStylesheets().add("assets/ac.css");
		stage.setScene(scene);
		stage.setTitle("Hack Kean 2016");
		stage.setResizable(false);
		stage.getIcons().add(new Image(getClass().getResourceAsStream("assets/cougar.png")));
		stage.setOnCloseRequest(e -> {
			TemperatureLog.save(temperature.getValue());
		});
		stage.show();
	}
}