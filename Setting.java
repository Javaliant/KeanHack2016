/* Author: Luigi Vincent 
For temperature settings
*/

public enum Setting {
	COLD("-fx-fill: aqua;"),
	COOL("-fx-fill: linear-gradient(from 0% 0% to 100% 200%, repeat, blue 0%, aqua 50%);"),
	NEUTRAL("-fx-fill: sandybrown;"),
	WARM("-fx-fill: linear-gradient(from 0% 0% to 100% 200%, repeat, sandybrown 0%, red 50%);"),
	HOT("-fx-fill: red;");

	private final String style;

	Setting(String style) {
		this.style = style;
	}

	public String style() {
		return style;
	}
}