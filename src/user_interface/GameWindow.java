// javac -d compiled -sourcepath src src/user_interface/GameWindow.java
// java -cp compiled user_interface.GameWindow
package user_interface;

import javax.swing.JFrame;

@SuppressWarnings(value = { "serial" })
public class GameWindow extends JFrame {

	public static final int SCREEN_WIDTH = 1200;
	public static final int SCREEN_HEIGHT = 400;

	private GameScreen gameScreen;

	public GameWindow() {
		super("Dino");
		setSize(SCREEN_WIDTH, SCREEN_HEIGHT);
		setResizable(false);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setUndecorated(true);
		setLocationRelativeTo(null);

		gameScreen = new GameScreen();
		add(gameScreen);
	}

	private void startGame() {
		gameScreen.startGame();
		gameScreen.startThread();
	}

	public static void main(String[] args) {
		GameWindow gameWindow = new GameWindow();
		gameWindow.startGame();
		gameWindow.setVisible(true);
	}

}
