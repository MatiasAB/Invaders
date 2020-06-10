package engine;

import screen.*;

import java.util.List;
import java.util.Scanner;

/**
 * Created by Ryan on 7/28/2015.
 */
public class Invaders {

    /**
     * Start running the game
     */
    public static void run() {

        // Get a list of levels to play
        List<GameSettings> levelSettings = Levels.getLevels();
        // Hold on to all of the game's information
        GameState gameState = new GameState(1, 0, Constants.MAX_LIVES, 0, 0);
        
        // Show Title Screen below this line
        Screen start = new StartScreen();
        start.show();
        Screen screen;
        if (start.getNextScreenType() == ScreenType.TitleScreen){
            screen = new TitleScreen();
        } else {
            screen = new TitleScreen2();
        }
        screen.show();
        ScreenType nextScreenType = screen.getNextScreenType();
        while (nextScreenType != ScreenType.EndGame){
            if (nextScreenType == ScreenType.TitleScreen){
                Screen s = new TitleScreen();
                s.show();
                nextScreenType = s.getNextScreenType();
            } else if (nextScreenType == ScreenType.GameScreen){
                Screen g = new GameScreen(gameState, levelSettings);
                g.show();
                nextScreenType = g.getNextScreenType();

            } else if (nextScreenType == ScreenType.ScoreScreen){
                Screen sc = new ScoreScreen(gameState);
                sc.show();
                nextScreenType = sc.getNextScreenType();
            } else if (nextScreenType == ScreenType.HighScoreScreen){
                Screen hsc = new HighScoreScreen();
                hsc.show();
                nextScreenType = hsc.getNextScreenType();
            }
        }


    }
}
