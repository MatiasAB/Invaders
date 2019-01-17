package screen;

import java.awt.event.KeyEvent;

import engine.Cooldown;
import engine.Main;
import engine.ScreenType;

public class StartScreen extends Screen {

    /** Milliseconds between changes in user selection. */
    private static final int SELECTION_TIME = 200;

    /** Time between changes in user selection. */
    private Cooldown selectionCooldown;

    public StartScreen() {
        super();

        // Defaults to play.
        this.nextScreenTpe = ScreenType.TitleScreen;
        this.selectionCooldown = Main.getCooldown(SELECTION_TIME);
        this.selectionCooldown.reset();
    }

    protected final void update() {
        super.update();

        draw();
        if (this.selectionCooldown.checkFinished()
                && this.inputDelay.checkFinished()) {
            if (inputManager.isKeyDown(KeyEvent.VK_UP)
                    || inputManager.isKeyDown(KeyEvent.VK_W)) {
                this.nextScreenTpe = getPreviousRes(this.nextScreenTpe);
                this.selectionCooldown.reset();
            }
            if (inputManager.isKeyDown(KeyEvent.VK_DOWN)
                    || inputManager.isKeyDown(KeyEvent.VK_S)) {
                this.nextScreenTpe = getNextRes(this.nextScreenTpe);
                this.selectionCooldown.reset();
            }
            if (inputManager.isSpaceKeyDown())
                this.isRunning = false;
        }
    }

    private ScreenType getNextRes(ScreenType selectedRes) {
        if (selectedRes == ScreenType.TitleScreen){
            return ScreenType.TitleScreen2;
        } else {
            return ScreenType.TitleScreen;
        }
    }

    private ScreenType getPreviousRes(ScreenType selectedRes) {
        if (selectedRes == ScreenType.TitleScreen){
            return ScreenType.TitleScreen2;
        } else {
            return ScreenType.TitleScreen;
        }
    }

    @Override
    public ScreenType getScreenType() {
        return ScreenType.StartScreen;
    }

    public final ScreenType run() {
        super.run();

        return this.nextScreenTpe;
    }

    private void draw() {
        drawManager.initDrawing(this);

//        drawManager.drawTitle(this);
//        drawManager.drawMenu(this, this.nextScreenTpe);
        drawManager.drawStart(this, this.nextScreenTpe);

        drawManager.completeDrawing(this);
    }
}
