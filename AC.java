/* Author: Luigi Vincent
To simulate an Air Conditioner
*/

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.geometry.HPos;
import javafx.scene.Scene;
import javafx.scene.text.Text;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import java.util.concurrent.CountDownLatch;

public class AC extends Application {
	private final int MAX_TEMPERATURE = 84;
	private final int MIN_TEMPERATURE = 58;
	private int startingTemperature = TemperatureLog.getTemp();
	private Text temperatureText;
	private IntegerProperty temperature = new SimpleIntegerProperty(startingTemperature);
	private Button upButton;
	private Button downButton;
	private Stage stage;

	public static void main(String[] args) {
		launch(args);
	}

	@Override
	public void start(Stage blah) {
		stage = new Stage();
		Service<Void> service = new Service<Void>() {
	        @Override
	        protected Task<Void> createTask() {
	            return new Task<Void>() {           
	                @Override
	                protected Void call() throws Exception {
	                    //Background work
	                    int port = 6102;
	                    try(ServerSocket server = new ServerSocket(port)) {
							System.out.println("Server online.");
							while(true) {
								new Thread(new ClientHandler(server.accept())).start();
							}
						} catch (IOException ioe) {
							ioe.printStackTrace();
						}

	                    final CountDownLatch latch = new CountDownLatch(1);
	                    Platform.runLater(new Runnable() {                          
	                        @Override
	                        public void run() {
	                            try{
	                                //FX Stuff done here
	                            }finally{
	                                latch.countDown();
	                            }
	                        }
	                    });
	                    latch.await();                      
	                    //Keep with the background work
	                    return null;
	                }	
	            };
	        }
	    };
    	service.start();

		GridPane gridpane = new GridPane();
		gridpane.setHgap(5);
		gridpane.setVgap(5);

		temperatureText = new Text();
		temperatureText.setId("temperature-text");
		temperatureText.textProperty().bind(Bindings.concat("Current Temperature: ").concat(temperature));
		initTemperatureStyle();
		gridpane.add(temperatureText, 0, 0);
		GridPane.setRowSpan(temperatureText, 2);

		upButton = new Button("", new ImageView(new Image("assets/arrow_up.png")));
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

		downButton = new Button("", new ImageView(new Image("assets/arrow_down.png")));
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
		stage.getIcons().add(new Image(getClass().getResourceAsStream("assets/cougar.png")));
		stage.setOnCloseRequest(e -> {
			TemperatureLog.save(temperature.getValue());
		});
		Platform.setImplicitExit(false);
	}

	private class ClientHandler implements Runnable {
		Socket socket;
		boolean active = false;

		ClientHandler(Socket socket) {
			this.socket = socket;
			System.out.println("Connection success!");
		}

		@Override
		public void run() {
			try(
				BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				PrintWriter out = new PrintWriter(socket.getOutputStream(), true)
			) {
				out.println(temperature.getValue());
				System.out.println("Sending value: " + temperature.getValue());
				while (true) {
					String input = in.readLine();
					if (input == null || input.trim().isEmpty()) {
	                    continue;
	                }
	                if (input.equals("up")) {
	                	Platform.runLater(new Runnable() {                          
	                        @Override
	                        public void run() {
	                        	if (active) {
	                        		upButton.fire();
	                        	}
	                        }
	                    });
	                } else if (input.equals("down")) {
	                	Platform.runLater(new Runnable() {                          
	                        @Override
	                        public void run() {
	                        	if (active) {
	                        		downButton.fire();
	                        	}
	                        }
	                    });
	                } else if (input.equals("power")) {
	                	Platform.runLater(new Runnable() {                          
	                        @Override
	                        public void run() {
	                        	if (!active) {
	                        		stage.show();
	                        	} else {
	                        		stage.hide();
	                        	}
	                        	active = !active;
	                        }
	                    });
	                } else if (input.startsWith("set")) {
	                	int target = Integer.parseInt(input.substring(3));
	                	System.out.println("Received value: " + target);
	                	if (target <= MIN_TEMPERATURE) {
	                		temperature.setValue(MIN_TEMPERATURE);
	                	} else if (target >= MAX_TEMPERATURE) {
	                		temperature.setValue(MAX_TEMPERATURE);
	                	} else {
	                		temperature.setValue(target);
	                	}
	                	out.println(temperature.getValue());
	                	initTemperatureStyle();
	                }
				}
			} catch (IOException ioe) {
				System.out.println("Connection terminated.");
			}
		}
	}

	private void initTemperatureStyle() {
		if (temperature.getValue() <= 61) {
			temperatureText.setStyle(Setting.COLD.style());
		} else if (temperature.getValue() <= 66) {
			temperatureText.setStyle(Setting.COOL.style());
		} else if (temperature.getValue() <= 71) {
			temperatureText.setStyle(Setting.NEUTRAL.style());
		} else if (temperature.getValue() <= 77) {
			temperatureText.setStyle(Setting.WARM.style());
		} else if (temperature.getValue() <= 84) {
			temperatureText.setStyle(Setting.HOT.style());
		}
	}
}