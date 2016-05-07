/* Author: Luigi Vincent =
Helper class to read and store temperature
*/

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.Scanner;

public class TemperatureLog {

	public static int getTemp() {
		int temperature = 0;
		try (Scanner scanner = new Scanner(new File("temperature.txt"))) {
			temperature = scanner.nextInt();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		return temperature;
	}

	public static void save(int temperature) {
		try (PrintWriter writer = new PrintWriter("temperature.txt", "UTF-8")) {
			writer.println(temperature);
		} catch (FileNotFoundException | UnsupportedEncodingException e) {
			e.printStackTrace(); 
		}
	}
}