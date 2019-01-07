package screen;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.*;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

import engine.*;
import entity.Bullet;
import entity.BulletPool;
import entity.EnemyShip;
import entity.EnemyShipFormation;
import entity.Entity;
import entity.Ship;

/**
 * Implements the game screen, where the action happens.
 * 
 * @author <a href="mailto:RobertoIA1987@gmail.com">Roberto Izquierdo Amo</a>
 * 
 */
public class GameScreen extends Screen {

	/** Levels between extra life. */
	private static final int EXTRA_LIFE_FRECUENCY = 3;

	/** Milliseconds until the screen accepts user input. */
	private static final int INPUT_DELAY = 6000;
	/** Bonus score for each life remaining at the end of the level. */
	private static final int LIFE_SCORE = 100;
	/** Minimum time between bonus ship's appearances. */
	private static final int BONUS_SHIP_INTERVAL = 20000;
	/** Maximum variance in the time between bonus ship's appearances. */
	private static final int BONUS_SHIP_VARIANCE = 10000;
	/** Time until bonus ship explosion disappears. */
	private static final int BONUS_SHIP_EXPLOSION = 500;
	/** Time from finishing the level to screen change. */
	private static final int SCREEN_CHANGE_INTERVAL = 1500;
	/** Height of the interface separation line. */
	private static final int SEPARATION_LINE_HEIGHT = 40;

	/** Current game difficulty settings. */
	private List<GameSettings> levelSettings;
	/** Formation of enemy ships. */
	private EnemyShipFormation enemyShipFormation;
	/** Player's ship. */
	private Ship ship;
	/** Bonus enemy ship that appears sometimes. */
	private EnemyShip enemyShipSpecial;
	/** Minimum time between bonus ship appearances. */
	private Cooldown enemyShipSpecialCooldown;
	/** Time until bonus ship explosion disappears. */
	private Cooldown enemyShipSpecialExplosionCooldown;
	/** Time from finishing the level to screen change. */
	private Cooldown screenFinishedCooldown;
	/** Set of all bullets fired by on screen ships. */
	private Set<Bullet> bullets;
	/** Game's state. */
	private GameState gameState;
	/** Moment the game starts. */
	private long gameStartTime;
	/** Checks if the level is finished. */
	private boolean levelFinished;
	/** Checks if a bonus life is received. */
	private boolean bonusLife;

	/**
	 * Constructor, establishes the properties of the screen.
	 * 
	 * @param gameState
	 *            Current game state.
	 * @param levelSettings
	 */
	public GameScreen(final GameState gameState, final List<GameSettings> levelSettings) {
		super();

		this.gameState = gameState;
		this.levelSettings = levelSettings;
		this.bonusLife = false;
		this.nextScreenTpe = ScreenType.ScoreScreen;

		// Make sure the game's state is reset
		this.gameState.reset();
	}

	/**
	 * Gets the screen's type
	 * @return The screen's type
	 */
	public ScreenType getScreenType() { return ScreenType.GameScreen; }

	/**
	 * Initializes basic screen properties, and adds necessary elements.
	 */
	public final void initialize() {
		super.initialize();

		enemyShipFormation = new EnemyShipFormation(this.levelSettings.get(this.gameState.getLevel()- 1));
		enemyShipFormation.attach(this);
		this.ship = new Ship(this.getWidth() / 2, this.getHeight() - 30);
		// Appears each 10-30 seconds.
		this.enemyShipSpecialCooldown = Main.getVariableCooldown(
				BONUS_SHIP_INTERVAL, BONUS_SHIP_VARIANCE);
		this.enemyShipSpecialCooldown.reset();
		this.enemyShipSpecialExplosionCooldown = Main
				.getCooldown(BONUS_SHIP_EXPLOSION);
		this.screenFinishedCooldown = Main.getCooldown(SCREEN_CHANGE_INTERVAL);
		this.bullets = new HashSet<Bullet>();

		// Special input delay / countdown.
		this.gameStartTime = System.currentTimeMillis();
		this.inputDelay = Main.getCooldown(INPUT_DELAY);
		this.inputDelay.reset();


		this.levelFinished = false;
		this.screenFinishedCooldown.reset();
	}

	/**
	 * Starts the action.
	 * 
	 * @return Next screen code.
	 */
	public final ScreenType run() {
		super.run();
		return this.nextScreenTpe;
	}

	/**
	 * Updates the elements on screen and checks for events.
	 */
	protected final void update() {
		super.update();

		if (this.inputDelay.checkFinished() && !this.levelFinished) {

			if (!this.ship.isDestroyed()) {

				this.ship.checkMovement(this);
				if (inputManager.isSpaceKeyDown()) {
					if (inputManager.is2ndKeyDown() && ship.checkRapid(System.currentTimeMillis())) {
						if (this.ship.shootFast(this.bullets)) {
							this.gameState.setBulletsShot(this.gameState.getBulletsShot() + 1);
						}
					} else if (!ship.checkRapid(System.currentTimeMillis()) && inputManager.is2ndKeyDown()) {
						if (this.ship.shootThree(this.bullets)) {
							this.gameState.setBulletsShot(this.gameState.getBulletsShot() + 1);
						}
					} else {
						if (this.ship.shoot(this.bullets)){
							this.gameState.setBulletsShot(this.gameState.getBulletsShot() + 1);
						}
					}
				}
			}

			if (this.enemyShipSpecial != null) {
				if (!this.enemyShipSpecial.isDestroyed()) {
					this.enemyShipSpecial.move(2, 0);
				} else if (this.enemyShipSpecialExplosionCooldown.checkFinished()) {
					this.enemyShipSpecial = null;
					this.ship.toggleRapid(false, System.currentTimeMillis());
				}
			}
			if (this.enemyShipSpecial == null
					&& this.enemyShipSpecialCooldown.checkFinished()) {
				this.enemyShipSpecial = new EnemyShip();
				this.enemyShipSpecialCooldown.reset();
				this.logger.info("A special ship appears");
			}
			if (this.enemyShipSpecial != null
					&& this.enemyShipSpecial.getPositionX() > this.getWidth()) {
				this.enemyShipSpecial = null;
				this.logger.info("The special ship has escaped");
			}

			this.ship.update();
			this.enemyShipFormation.update();
			this.enemyShipFormation.shoot(this.bullets);
		}

		manageCollisions();
		cleanBullets();
		draw();

		if ((this.enemyShipFormation.isEmpty() || this.gameState.getLivesRemaining() == 0)
				&& !this.levelFinished) {
			this.levelFinished = true;
			this.screenFinishedCooldown.reset();
		}

		// If this level is over start the next level
		if (this.levelFinished && this.screenFinishedCooldown.checkFinished()) {
			// If the player doenst have anymore lives or they beat the last level, stop running the game screen
			if(this.gameState.getLivesRemaining() <= 0 || this.gameState.getLevel()>= this.levelSettings.size())
				this.isRunning = false;
			// Move on to the next level
			else {
				// One extra life every few levels.
				this.bonusLife = this.gameState.getLevel()
						% EXTRA_LIFE_FRECUENCY == 0
						&& this.gameState.getLivesRemaining() < Constants.MAX_LIVES;
				if(bonusLife)
					this.gameState.setLivesRemaining(this.gameState.getLivesRemaining() + 1);

				// Move on to the next level
				this.gameState.setLevel(this.gameState.getLevel() + 1);

				// Reset this screen
				this.initialize();

				// Update the score
				int newScore = this.gameState.getScore() + LIFE_SCORE * (this.gameState.getLivesRemaining() - 1);
				this.gameState.setScore(newScore);
			}

			this.logger.info("Screen cleared with a score of " + this.gameState.getScore());
		}
	}

	/**
	 * Draws the elements associated with the screen.
	 */
	private void draw() {
		drawManager.initDrawing(this);

		drawManager.drawEntity(this.ship);
		if (this.enemyShipSpecial != null)
			drawManager.drawEntity(this.enemyShipSpecial);

		enemyShipFormation.draw();

		for (Bullet bullet : this.bullets)
			drawManager.drawEntity(bullet);

		// Interface.
		drawManager.drawScore(this, this.gameState.getScore());
		drawManager.drawLives(this, this.gameState.getLivesRemaining());
		drawManager.drawHorizontalLine(this, SEPARATION_LINE_HEIGHT - 1);

		// Countdown to game start.
		if (!this.inputDelay.checkFinished()) {
			int countdown = (int) ((INPUT_DELAY
					- (System.currentTimeMillis()
							- this.gameStartTime)) / 1000);
			drawManager.drawCountDown(this, this.gameState.getLevel(), countdown,
					this.bonusLife);
			drawManager.drawHorizontalLine(this, this.getHeight() / 2 - this.getHeight()
					/ 12);
			drawManager.drawHorizontalLine(this, this.getHeight() / 2 + this.getHeight()
					/ 12);
		}

		drawManager.completeDrawing(this);
	}

	/**
	 * Cleans bullets that go off screen.
	 */
	private void cleanBullets() {
		Set<Bullet> recyclable = new HashSet<Bullet>();
		for (Bullet bullet : this.bullets) {
			bullet.update();
			if (bullet.getPositionY() < SEPARATION_LINE_HEIGHT
					|| bullet.getPositionY() > this.getHeight())
				recyclable.add(bullet);
		}
		this.bullets.removeAll(recyclable);
		BulletPool.recycle(recyclable);
	}

	/**
	 * Manages collisions between bullets and ships.
	 */
	private void manageCollisions() {
		Set<Bullet> recyclable = new HashSet<Bullet>();
		for (Bullet bullet : this.bullets)
			if (bullet.getSpeed() > 0) {
				if (checkCollision(bullet, this.ship) && !this.levelFinished) {
					recyclable.add(bullet);
					if (!this.ship.isDestroyed()) {
						this.ship.destroy();
						this.gameState.setLivesRemaining(this.gameState.getLivesRemaining() - 1);
						this.logger.info("Hit on player ship, " + this.gameState.getLivesRemaining()
								+ " lives remaining.");
					}
				}
			} else {
				for (EnemyShip enemyShip : this.enemyShipFormation)
					if (!enemyShip.isDestroyed()
							&& checkCollision(bullet, enemyShip)) {
						this.gameState.setScore(this.gameState.getScore() + enemyShip.getPointValue());
						this.gameState.setShipsDestroyed(this.gameState.getShipsDestroyed() + 1);
						this.enemyShipFormation.destroy(enemyShip);
						recyclable.add(bullet);
					}
				if (this.enemyShipSpecial != null
						&& !this.enemyShipSpecial.isDestroyed()
						&& checkCollision(bullet, this.enemyShipSpecial)) {
					this.gameState.setScore(this.gameState.getScore() + this.enemyShipSpecial.getPointValue());
					this.gameState.setShipsDestroyed(this.gameState.getShipsDestroyed() + 1);
					this.enemyShipSpecial.destroy();
					this.enemyShipSpecialExplosionCooldown.reset();
					recyclable.add(bullet);
				}
			}
		this.bullets.removeAll(recyclable);
		BulletPool.recycle(recyclable);
	}

	/**
	 * Checks if two entities are colliding.
	 * 
	 * @param a
	 *            First entity, the bullet.
	 * @param b
	 *            Second entity, the ship.
	 * @return Result of the collision test.
	 */
	private boolean checkCollision(final Entity a, final Entity b) {
		// Calculate center point of the entities in both axis.
		int centerAX = a.getPositionX() + a.getWidth() / 2;
		int centerAY = a.getPositionY() + a.getHeight() / 2;
		int centerBX = b.getPositionX() + b.getWidth() / 2;
		int centerBY = b.getPositionY() + b.getHeight() / 2;
		// Calculate maximum distance without collision.
		int maxDistanceX = a.getWidth() / 2 + b.getWidth() / 2;
		int maxDistanceY = a.getHeight() / 2 + b.getHeight() / 2;
		// Calculates distance.
		int distanceX = Math.abs(centerAX - centerBX);
		int distanceY = Math.abs(centerAY - centerBY);

		return distanceX < maxDistanceX && distanceY < maxDistanceY;
	}
}