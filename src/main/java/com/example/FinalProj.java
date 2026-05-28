package com.example;

import java.util.HashSet;
import java.util.Set;
import javafx.scene.input.KeyCode;
import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.Pane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.paint.Color;
import javafx.scene.paint.RadialGradient;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.paint.CycleMethod;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Arc;
import javafx.scene.shape.ArcType;
import javafx.scene.shape.Ellipse;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.scene.text.Text;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.application.Platform;
import javafx.animation.PathTransition;
import javafx.animation.PauseTransition;
import javafx.animation.FadeTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.TranslateTransition;
import javafx.animation.SequentialTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.RotateTransition;
import javafx.animation.Interpolator;
import javafx.scene.shape.Path;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.QuadCurveTo;

/**
 * Main JavaFX application for Hoops Showdown.
 *
 * <p>{@code FinalProj} is the controller for the entire game experience. It
 * creates the welcome screen, character-select screens, live court view, shot
 * animations, block timing, score display, and game-over stats screen. It also
 * connects JavaFX input events to the model classes {@link Data} and
 * {@link Player}.</p>
 *
 * <p>The class intentionally keeps the visual layer in JavaFX nodes while
 * delegating score and shot-zone decisions to {@link Player} and shot
 * probability decisions to {@link Data}. This makes it easier to change the
 * artwork or animations without changing the core scoring rules.</p>
 *
 * @see Data
 * @see Player
 */
public class FinalProj extends Application {

    // MediaPlayer plays a short WAV sound when the button is clicked
    //Some background music
    private final MediaPlayer gametracks = new MediaPlayer(
        new Media(getClass().getResource("/gametracks.mp3").toString())
    );

    //Main mechanism for the GUI
    private Scene scene;
    /**
     * Root container for every screen in the JavaFX scene.
     *
     * <p>The application clears and repopulates this pane when moving between
     * the welcome screen, character select, gameplay, and stats screen.</p>
     */
    public Pane rootPane = new Pane();

    //Data and Player ImageViews
    private Data data = new Data();
    /**
     * Image node for player 1's selected character.
     *
     * <p>The node is reused across rounds. Its position is synchronized with
     * {@link Data#getPlayer(int)} so gameplay calculations can use model
     * coordinates.</p>
     */
    public ImageView p1ImageView = new ImageView();
    /**
     * Image node for player 2's selected character.
     *
     * <p>The node is reused across rounds and receives the selected character
     * sprite through {@link #playerNBAImage(int)}.</p>
     */
    public ImageView p2ImageView = new ImageView();

    //The Ball
    private Circle ball;

    // Score and shot-tracking fields.
    private Text scoreboard = new Text();
    private int p1ShotsMade = 0;
    private int badP1Shots = 0;
    private int p2ShotsMade = 0;
    private int badP2Shots = 0;

    //Fields for realtime-based gameplay
    private boolean gameRunning = false;
    private boolean isBallInFlight = false;
    private static final long BLOCK_WINDOW_MILLIS = 50;
    private static final double BLOCK_LANE_WIDTH = 95.0;
    private static final double BLOCK_MAX_DISTANCE = 190.0;
    private final long[] lastBlockAttemptMillis = new long[3];
    private int activeShooterNum = 0;
    private long activeShotStartedMillis = 0;
    private boolean activeShotResolved = false;
    private SequentialTransition activeShotAnimation;

    // Continuous movement: track which keys are currently held + frame timing
    private final Set<KeyCode> heldKeys = new HashSet<>();
    private long lastFrameNanos = 0;
    private static final double MOVE_SPEED = 380.0;   // pixels per second
    private static final double PLAYER_W = 100.0;
    private static final double PLAYER_H = 150.0;
    private static final double COURT_W = 1500.0;
    private static final double COURT_H = 850.0;

    /*private double dx = 150;  // Horizontal speed in pixels/second
    private double dy = 120;  // Vertical speed in pixels/second
    //private ImageView imageView;  // To control the image position with WASD keys*/




    //Responsible for the Game Loop — applies continuous movement based on which keys are held
    AnimationTimer gameEngine = new AnimationTimer(){
        @Override
        public void handle(long now) {
            if (!gameRunning) {
                lastFrameNanos = now;
                return;
            }
            if (lastFrameNanos == 0) {
                lastFrameNanos = now;
                return;
            }
            double dt = (now - lastFrameNanos) / 1_000_000_000.0;
            lastFrameNanos = now;
            if (dt > 0.05) dt = 0.05;       // clamp big stalls so players don't teleport

            if (isBallInFlight) {
                return;                      // freeze movement while the shot is in the air
            }

            double dist = MOVE_SPEED * dt;
            Player p1 = data.getPlayer(1);
            Player p2 = data.getPlayer(2);

            // ===== Player 1 (WASD) =====
            double p1x = p1ImageView.getLayoutX();
            double p1y = p1ImageView.getLayoutY();
            if (heldKeys.contains(KeyCode.W)) p1y -= dist;
            if (heldKeys.contains(KeyCode.S)) p1y += dist;
            if (heldKeys.contains(KeyCode.A)) p1x -= dist;
            if (heldKeys.contains(KeyCode.D)) p1x += dist;
            p1x = Math.max(0, Math.min(COURT_W - PLAYER_W, p1x));
            p1y = Math.max(0, Math.min(COURT_H - PLAYER_H, p1y));
            p1ImageView.setLayoutX(p1x); p1ImageView.setLayoutY(p1y);
            p1.setPlayerX(p1x); p1.setPlayerY(p1y);

            // ===== Player 2 (IJKL) =====
            double p2x = p2ImageView.getLayoutX();
            double p2y = p2ImageView.getLayoutY();
            if (heldKeys.contains(KeyCode.I)) p2y -= dist;
            if (heldKeys.contains(KeyCode.K)) p2y += dist;
            if (heldKeys.contains(KeyCode.J)) p2x -= dist;
            if (heldKeys.contains(KeyCode.L)) p2x += dist;
            p2x = Math.max(0, Math.min(COURT_W - PLAYER_W, p2x));
            p2y = Math.max(0, Math.min(COURT_H - PLAYER_H, p2y));
            p2ImageView.setLayoutX(p2x); p2ImageView.setLayoutY(p2y);
            p2.setPlayerX(p2x); p2.setPlayerY(p2y);

            // Ball follows whoever has possession
            int possession = data.getPossession();
            if (possession == 1) {
                ball.setLayoutX(p1x + 50); ball.setLayoutY(p1y + 50);
            } else if (possession == 2) {
                ball.setLayoutX(p2x + 50); ball.setLayoutY(p2y + 50);
            }
        }
    };




    // ===== UI helpers (visual polish only) =====

    /** Dark court-themed gradient background that fills the whole scene. */
    private Rectangle makeBackdrop() {
        Rectangle bg = new Rectangle(0, 0, 1500, 850);
        bg.setFill(new LinearGradient(
            0, 0, 1, 1, true, CycleMethod.NO_CYCLE,
            new Stop(0.0, Color.web("#0f1430")),
            new Stop(0.55, Color.web("#1b2452")),
            new Stop(1.0, Color.web("#341a4a"))
        ));
        return bg;
    }

    /** Subtle decorative court arcs / glow that sit on top of the backdrop. */
    private Pane makeAmbientGlow() {
        Pane p = new Pane();
        Circle glow1 = new Circle(220, 220, 280, Color.web("#ff8a3d", 0.18));
        glow1.setEffect(new javafx.scene.effect.GaussianBlur(60));
        Circle glow2 = new Circle(1300, 700, 320, Color.web("#3a8dff", 0.22));
        glow2.setEffect(new javafx.scene.effect.GaussianBlur(80));
        p.getChildren().addAll(glow1, glow2);
        p.setMouseTransparent(true);
        return p;
    }

    /** Returns the display name for an NBA pick (1-5). */
    private String nbaName(int nba) {
        switch (nba) {
            case 1: return "LeBron James";
            case 2: return "Stephen Curry";
            case 3: return "Kevin Durant";
            case 4: return "Giannis Antetokounmpo";
            case 5: return "Luka Dončić";
            default: return "—";
        }
    }

    /** Returns the resource path for an NBA pick portrait. */
    private String nbaImagePath(int nba) {
        switch (nba) {
            case 1: return "/Lebron.png";
            case 2: return "/Curry.png";
            case 3: return "/Durant.png";
            case 4: return "/Antetokounmpo.png";
            case 5: return "/Doncic.png";
            default: return "/Empty.png";
        }
    }

    /** Compact label for in-game player nameplates. */
    private String shortNbaName(int nba) {
        switch (nba) {
            case 1: return "LEBRON";
            case 2: return "CURRY";
            case 3: return "DURANT";
            case 4: return "GIANNIS";
            case 5: return "LUKA";
            default: return "PLAYER";
        }
    }

    /** Style a button using one of the CSS classes in styles.css. */
    private Button styledButton(String text, String variant) {
        Button b = new Button(text);
        b.getStyleClass().addAll("btn", variant);
        return b;
    }

    /** Build a fancy basketball: radial-gradient fill, seams, drop shadow. */
    private void buildBasketball() {
        ball = new Circle(28);
        ball.setFill(new RadialGradient(
            0, 0, 0.35, 0.35, 0.75, true, CycleMethod.NO_CYCLE,
            new Stop(0.0, Color.web("#ffb070")),
            new Stop(0.55, Color.web("#ff6a1a")),
            new Stop(1.0, Color.web("#992f00"))
        ));
        ball.setStroke(Color.web("#5a1900"));
        ball.setStrokeWidth(1.5);
        ball.setEffect(new DropShadow(12, Color.rgb(0, 0, 0, 0.6)));
    }

    /** Draws the scoring boundary used by Player.shotType(). */
    private Pane makeThreePointLine() {
        Pane linePane = new Pane();
        double startAngle = 200.0;
        double endAngle = 340.0;
        double startRad = Math.toRadians(startAngle);
        double endRad = Math.toRadians(endAngle);
        double leftX = Player.HOOP_X + Player.THREE_POINT_RADIUS * Math.cos(startRad);
        double lineY = Player.HOOP_Y - Player.THREE_POINT_RADIUS * Math.sin(startRad);
        double rightX = Player.HOOP_X + Player.THREE_POINT_RADIUS * Math.cos(endRad);

        Arc arc = new Arc(
            Player.HOOP_X,
            Player.HOOP_Y,
            Player.THREE_POINT_RADIUS,
            Player.THREE_POINT_RADIUS,
            startAngle,
            endAngle - startAngle
        );
        arc.setType(ArcType.OPEN);

        Line leftSide = new Line(leftX, 0, leftX, lineY);
        Line rightSide = new Line(rightX, 0, rightX, lineY);

        for (javafx.scene.shape.Shape segment : new javafx.scene.shape.Shape[] { leftSide, arc, rightSide }) {
            segment.setFill(Color.TRANSPARENT);
            segment.setStroke(Color.web("#ff9800"));
            segment.setStrokeWidth(10);
            segment.setEffect(new DropShadow(10, Color.web("#ff9800", 0.45)));
        }

        linePane.getChildren().addAll(leftSide, arc, rightSide);
        linePane.setMouseTransparent(true);
        return linePane;
    }

    /** Extra court lighting and markings layered above the court image. */
    private Pane makeCourtPolish() {
        Pane courtPolish = new Pane();
        courtPolish.setMouseTransparent(true);

        Rectangle warmWash = new Rectangle(0, 0, COURT_W, COURT_H);
        warmWash.setFill(new LinearGradient(
            0, 0, 0, 1, true, CycleMethod.NO_CYCLE,
            new Stop(0.0, Color.web("#fff1b6", 0.14)),
            new Stop(0.45, Color.web("#ffffff", 0.02)),
            new Stop(1.0, Color.web("#5f2b00", 0.12))
        ));

        Rectangle vignette = new Rectangle(0, 0, COURT_W, COURT_H);
        vignette.setFill(new RadialGradient(
            0, 0, 0.50, 0.36, 0.86, true, CycleMethod.NO_CYCLE,
            new Stop(0.0, Color.TRANSPARENT),
            new Stop(0.68, Color.web("#000000", 0.04)),
            new Stop(1.0, Color.web("#000000", 0.28))
        ));

        Rectangle paintGlow = new Rectangle(Player.HOOP_X - 210, 0, 420, 390);
        paintGlow.setFill(Color.web("#2b72ff", 0.08));
        paintGlow.setStroke(Color.web("#ffffff", 0.22));
        paintGlow.setStrokeWidth(3);
        paintGlow.setArcWidth(22);
        paintGlow.setArcHeight(22);

        Arc restricted = new Arc(Player.HOOP_X, Player.HOOP_Y + 190, 130, 72, 0, -180);
        restricted.setType(ArcType.OPEN);
        restricted.setFill(Color.TRANSPARENT);
        restricted.setStroke(Color.web("#ffffff", 0.42));
        restricted.setStrokeWidth(5);

        Circle rimGlow = new Circle(Player.HOOP_X, Player.HOOP_Y, 64, Color.web("#ff4f1d", 0.16));
        rimGlow.setEffect(new javafx.scene.effect.GaussianBlur(18));

        for (int x = 150; x < COURT_W; x += 150) {
            Line grain = new Line(x, 0, x - 70, COURT_H);
            grain.setStroke(Color.web("#ffffff", 0.07));
            grain.setStrokeWidth(2);
            courtPolish.getChildren().add(grain);
        }

        Line centerLane = new Line(Player.HOOP_X, 0, Player.HOOP_X, COURT_H);
        centerLane.setStroke(Color.web("#ffffff", 0.10));
        centerLane.setStrokeWidth(3);
        centerLane.getStrokeDashArray().addAll(18.0, 24.0);

        courtPolish.getChildren().addAll(warmWash, paintGlow, restricted, rimGlow, centerLane, vignette);
        return courtPolish;
    }

    /** Ground detail that makes each character feel planted on the court. */
    private Pane makePlayerGroundDetail(ImageView playerView, Color accent) {
        Pane detail = new Pane();
        detail.setMouseTransparent(true);
        detail.layoutXProperty().bind(playerView.layoutXProperty());
        detail.layoutYProperty().bind(playerView.layoutYProperty());

        Ellipse shadow = new Ellipse(50, 142, 54, 16);
        shadow.setFill(Color.web("#000000", 0.34));
        shadow.setEffect(new javafx.scene.effect.GaussianBlur(7));

        Circle aura = new Circle(50, 82, 58, Color.TRANSPARENT);
        aura.setStroke(accent.deriveColor(0, 1.0, 1.2, 0.55));
        aura.setStrokeWidth(4);
        aura.setEffect(new DropShadow(18, accent.deriveColor(0, 1.0, 1.0, 0.55)));

        Ellipse reflection = new Ellipse(50, 145, 34, 6);
        reflection.setFill(accent.deriveColor(0, 1.0, 1.2, 0.18));
        reflection.setEffect(new javafx.scene.effect.GaussianBlur(5));

        detail.getChildren().addAll(shadow, aura, reflection);
        return detail;
    }

    /** Floating identity detail for each player figure. */
    private Pane makePlayerTopDetail(int pNum, ImageView playerView, Color accent) {
        Pane detail = new Pane();
        detail.setMouseTransparent(true);
        detail.layoutXProperty().bind(playerView.layoutXProperty());
        detail.layoutYProperty().bind(playerView.layoutYProperty());

        Rectangle plate = new Rectangle(-8, 4, 116, 32);
        plate.setFill(Color.web("#080d1f", 0.82));
        plate.setStroke(accent.deriveColor(0, 1.0, 1.15, 0.95));
        plate.setStrokeWidth(2);
        plate.setArcWidth(18);
        plate.setArcHeight(18);
        plate.setEffect(new DropShadow(10, Color.web("#000000", 0.55)));

        Circle chip = new Circle(12, 20, 18);
        chip.setFill(accent);
        chip.setStroke(Color.WHITE);
        chip.setStrokeWidth(2);

        Text playerNumber = new Text("P" + pNum);
        playerNumber.setFill(Color.web("#07101f"));
        playerNumber.setFont(Font.font("Helvetica Neue", FontWeight.BLACK, 14));
        playerNumber.setLayoutX(1);
        playerNumber.setLayoutY(25);

        Text name = new Text(shortNbaName(data.getPlayer(pNum).getNBA()));
        name.setFill(Color.WHITE);
        name.setFont(Font.font("Helvetica Neue", FontWeight.BLACK, 15));
        name.setLayoutX(34);
        name.setLayoutY(25);
        name.setEffect(new DropShadow(5, Color.web("#000000", 0.7)));

        Line topAccent = new Line(38, 9, 96, 9);
        topAccent.setStroke(accent.deriveColor(0, 1.0, 1.25, 0.85));
        topAccent.setStrokeWidth(2);

        detail.getChildren().addAll(plate, chip, playerNumber, name, topAccent);
        return detail;
    }

    /**
     * Initializes the JavaFX stage and shows the welcome screen.
     *
     * <p>This method is called by the JavaFX runtime after
     * {@link #main(String[])} invokes {@link #launch(String...)}. It creates the
     * reusable basketball node, applies CSS, sizes the scene to the court, and
     * then delegates the first visible screen to the welcome-screen builder.</p>
     *
     * @param stage primary JavaFX stage supplied by the JavaFX runtime
     * @implNote The root pane is made focus traversable so keyboard controls can
     * be attached directly to the game surface.
     */
    @Override
    public void start(Stage stage) {
        //1. Window icon + title
        Image icon = new Image(getClass().getResource("/Basketball.png").toString());
        stage.getIcons().add(icon);
        stage.setTitle("Hoops Showdown");

        // Build the ball once (re-used across matches)
        buildBasketball();

        // Create scene
        scene = new Scene(rootPane, 1500, 850);
        scene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());
        stage.setScene(scene);
        stage.show();
        rootPane.requestFocus();
        rootPane.setFocusTraversable(true);

        // Show the welcome screen (also used by PLAY AGAIN on the stats screen)
        showWelcome();
    }

    /**
     * Builds the welcome screen and wires up the
     * START → DONE → BEGIN MATCH chain. Called on launch and from PLAY AGAIN.
     */
    private void showWelcome() {
        rootPane.getChildren().clear();
        rootPane.setTranslateX(0); rootPane.setTranslateY(0);

        // Reset players (clean slate)
        data.makePlayer(1, 0, 0);
        data.makePlayer(2, 0, 0);

        //1.b) Welcome screen content
        Text title = new Text("HOOPS SHOWDOWN");
        title.getStyleClass().add("title");
        title.setLayoutX(280); title.setLayoutY(280);

        Text subtitle = new Text("A one-on-one NBA street battle");
        subtitle.getStyleClass().add("subtitle");
        subtitle.setLayoutX(430); subtitle.setLayoutY(340);

        Text byline = new Text("by Evan and Kris  •  first to 11 wins");
        byline.getStyleClass().add("byline");
        byline.setLayoutX(560); byline.setLayoutY(380);

        //1.c) Start button
        Button startButton = styledButton("PRESS TO START", "btn");
        startButton.setLayoutX(620); startButton.setLayoutY(500);

        //1.d) Decorative bobbing basketball
        Image icon = new Image(getClass().getResource("/Basketball.png").toString());
        ImageView welcomeBall = new ImageView(icon);
        welcomeBall.setFitWidth(140);
        welcomeBall.setPreserveRatio(true);
        welcomeBall.setLayoutX(680); welcomeBall.setLayoutY(610);
        welcomeBall.setEffect(new DropShadow(20, Color.web("#ff8a3d", 0.7)));

        rootPane.getChildren().addAll(
            makeBackdrop(), makeAmbientGlow(),
            title, subtitle, byline, welcomeBall, startButton
        );

        FadeTransition fade = new FadeTransition(Duration.millis(900), rootPane);
        fade.setFromValue(0); fade.setToValue(1);
        fade.play();

        TranslateTransition bob = new TranslateTransition(Duration.seconds(1.6), welcomeBall);
        bob.setByY(-18); bob.setAutoReverse(true); bob.setCycleCount(TranslateTransition.INDEFINITE);
        bob.play();

        rootPane.requestFocus();

        //2.a) Player 1 select -> DONE
        Button doneButton = styledButton("DONE", "btn-secondary");
        doneButton.setLayoutX(670); doneButton.setLayoutY(720);

        startButton.setOnAction(event -> {
            select1Char();
            rootPane.getChildren().add(doneButton);
        });

        Button beginButton = styledButton("BEGIN MATCH", "btn-success");
        beginButton.setLayoutX(630); beginButton.setLayoutY(720);

        //2.b) Player 2 select -> BEGIN
        doneButton.setOnAction(event -> {
            select2Char();
            rootPane.getChildren().add(beginButton);
            rootPane.getChildren().remove(doneButton);
        });

        //3.a) Start the match
        beginButton.setOnAction(event -> {
            rootPane.getChildren().clear();
            setupState(2);
            playerMoves();
            rootPane.requestFocus();
            gameRunning = true;
            gameEngine.start();
        });
    }

    /** Reset all match state and return to the welcome screen. */
    private void restartGame() {
        // Reset stat trackers
        p1ShotsMade = 0; badP1Shots = 0;
        p2ShotsMade = 0; badP2Shots = 0;
        gameRunning = false;
        isBallInFlight = false;
        clearActiveShot();
        heldKeys.clear();
        lastFrameNanos = 0;
        gameEngine.stop();

        // Reset scoreboard text node (it's a reused field)
        scoreboard.setText("0  :  0");

        // Stop any lingering music
        if (gametracks.getStatus() == MediaPlayer.Status.PLAYING) {
            gametracks.stop();
        }

        showWelcome();
    }




    /**
     * Shows the character-select screen for player 1.
     *
     * <p>Player 1 has no locked character because they choose first. The actual
     * UI construction is handled by the shared character-select builder.</p>
     *
     * @see #select2Char()
     */
    public void select1Char(){
        buildSelectScreen(1, -1);
    }




    /**
     * Shows the character-select screen for player 2.
     *
     * <p>Player 2 cannot choose the same character as player 1. The selected
     * player 1 character is passed as the locked choice.</p>
     *
     * @see #select1Char()
     */
    public void select2Char(){
        buildSelectScreen(2, data.getPlayer(1).getNBA());
    }

    /**
     * Shared character-select screen with live feedback.
     * @param pNum         which player is choosing (1 or 2)
     * @param lockedNba    NBA already picked by the other player (-1 if none)
     */
    private void buildSelectScreen(int pNum, int lockedNba) {
        rootPane.getChildren().clear();

        boolean isP1 = (pNum == 1);
        Color accent = isP1 ? Color.web("#34d27a") : Color.web("#ff5577");
        String accentHex = isP1 ? "#34d27a" : "#ff5577";

        // Header
        String header = isP1
            ? "PLAYER 1 — pick your fighter (1–5), then press DONE"
            : "PLAYER 2 — pick an unselected fighter, then BEGIN MATCH";
        Text instruction = new Text(header);
        instruction.getStyleClass().add("instruction");
        instruction.setLayoutX(isP1 ? 150 : 60);
        instruction.setLayoutY(120);

        // Big translucent P1 / P2 watermark
        Text tag = new Text(isP1 ? "P1" : "P2");
        tag.setFill(accent);
        tag.setFont(Font.font("Helvetica Neue", FontWeight.BLACK, 120));
        tag.setOpacity(0.18);
        tag.setLayoutX(isP1 ? 60 : 1340);
        tag.setLayoutY(180);

        // Character roster image (shrunk a bit to make room for the pick card)
        Image roster = new Image(getClass().getResource("/Characters.png").toString());
        ImageView rosterView = new ImageView(roster);
        rosterView.setFitWidth(820);
        rosterView.setPreserveRatio(true);
        rosterView.setLayoutX(80);
        rosterView.setY(200);
        rosterView.setEffect(new DropShadow(20, Color.web("#000000", 0.6)));

        // ===== Selection card on the right =====
        Rectangle pickCard = new Rectangle(960, 200, 470, 560);
        pickCard.getStyleClass().add("scoreboard-card");
        pickCard.setStroke(accent);

        Text cardHeading = new Text((isP1 ? "PLAYER 1" : "PLAYER 2") + " PICK");
        cardHeading.setFill(accent);
        cardHeading.setFont(Font.font("Helvetica Neue", FontWeight.BLACK, 32));
        cardHeading.setLayoutX(990);
        cardHeading.setLayoutY(250);

        // Currently-selected portrait (defaults to Empty)
        ImageView portrait = new ImageView(new Image(getClass().getResource("/Empty.png").toString()));
        portrait.setFitHeight(390);
        portrait.setPreserveRatio(true);
        portrait.setLayoutX(1035);
        portrait.setLayoutY(280);
        portrait.setEffect(new DropShadow(18, Color.web(accentHex, 0.6)));

        Text pickName = new Text("— press 1–5 —");
        pickName.setFill(Color.web("#f4f4f8"));
        pickName.setFont(Font.font("Helvetica Neue", FontWeight.BOLD, 28));
        pickName.setLayoutX(1000);
        pickName.setLayoutY(710);

        Text hint = new Text(lockedNba > 0
            ? "P1 picked " + nbaName(lockedNba) + " — locked"
            : "Choose carefully — different stars, different stats");
        hint.setFill(Color.web("#c9d6ff"));
        hint.setFont(Font.font("Helvetica Neue", 16));
        hint.setLayoutX(1000);
        hint.setLayoutY(740);

        rootPane.getChildren().addAll(
            makeBackdrop(), makeAmbientGlow(),
            tag, instruction, rosterView,
            pickCard, cardHeading, portrait, pickName, hint
        );

        // ===== Key handling =====
        rootPane.setOnKeyPressed(event -> {
            int choice = -1;
            switch (event.getCode()) {
                case DIGIT1: choice = 1; break;
                case DIGIT2: choice = 2; break;
                case DIGIT3: choice = 3; break;
                case DIGIT4: choice = 4; break;
                case DIGIT5: choice = 5; break;
                default: return;
            }

            // Player 2 can't pick the locked character
            if (choice == lockedNba) {
                pickName.setText("Taken! — try another");
                pickName.setFill(Color.web("#ff5577"));
                ScaleTransition shake = new ScaleTransition(Duration.millis(80), pickName);
                shake.setFromX(1.0); shake.setToX(1.08);
                shake.setAutoReverse(true); shake.setCycleCount(4);
                shake.play();
                return;
            }

            // Commit the pick
            data.makePlayer(pNum, choice, 0);

            // Update the card
            portrait.setImage(new Image(getClass().getResource(nbaImagePath(choice)).toString()));
            pickName.setText(nbaName(choice));
            pickName.setFill(accent);

            // Pop-in animation on the portrait so the user *feels* the selection
            ScaleTransition pop = new ScaleTransition(Duration.millis(220), portrait);
            pop.setFromX(0.85); pop.setFromY(0.85);
            pop.setToX(1.0); pop.setToY(1.0);
            pop.setInterpolator(Interpolator.EASE_OUT);
            pop.play();
        });
    }




    /**
     * Moves possession and the ball to a player.
     *
     * <p>This method updates both the visible ball position and the possession
     * value stored in {@link Data}. It is called during round setup and by older
     * prototype steal code. The current defensive mechanic uses timed blocks,
     * but the banner path is retained for compatibility with the previous
     * behavior.</p>
     *
     * @param pNum player number that should receive the ball
     * @param steal whether to show the legacy steal banner
     * @see Data#swapPossession(int)
     */
    public void switchPossession(int pNum, boolean steal){
        //Shows a temporary message indicating that a STEAL occurred
        if (steal){
            Text stealText = new Text("STEAL!!");
            stealText.setLayoutX(1400); stealText.setLayoutY(20);
            stealText.setFill(Color.RED);
            rootPane.getChildren().add(stealText);
            PauseTransition delay = new PauseTransition(Duration.seconds(3));
            delay.setOnFinished(event -> rootPane.getChildren().remove(stealText));
            delay.play();
        }

        Player p = data.getPlayer(pNum);
        ball.setLayoutX(p.getPlayerX()); ball.setLayoutY(p.getPlayerY());       //Moves the Ball to the Player who has Possession now
        data.swapPossession(pNum);
    }




    /**
     * Loads the selected character image into the correct player ImageView.
     *
     * <p>The game keeps one {@link ImageView} per player and swaps the image
     * resource based on the selected character number stored in {@link Player}.
     * This is called during every round reset so the sprites stay correct after
     * replaying or returning from character selection.</p>
     *
     * @param pNum player number whose image should be refreshed
     * @see Player#getNBA()
     */
    public void playerNBAImage(int pNum){       //Depending on the key press, maps the image of the chosen NBA Character
        Player p = data.getPlayer(pNum);
        int pNBA = p.getNBA();
        Image pImage = new Image(getClass().getResource("/Empty.png").toString());
        if (pNBA == 1){
            pImage = new Image(getClass().getResource("/Lebron.png").toString());
            if (pNum == 1){
                p1ImageView.setImage(pImage);
            }else{
                p2ImageView.setImage(pImage);
            }
        } else if (pNBA == 2){
            pImage = new Image(getClass().getResource("/Curry.png").toString());
            if (pNum == 1){
                p1ImageView.setImage(pImage);
            }else{
                p2ImageView.setImage(pImage);
            }
        } else if (pNBA == 3){
            pImage = new Image(getClass().getResource("/Durant.png").toString());
            if (pNum == 1){
                p1ImageView.setImage(pImage);
            }else{
                p2ImageView.setImage(pImage);
            }
        } else if (pNBA == 4){
            pImage = new Image(getClass().getResource("/Antetokounmpo.png").toString());
            if (pNum == 1){
                p1ImageView.setImage(pImage);
            }else{
                p2ImageView.setImage(pImage);
            }
        } else if (pNBA == 5){
            pImage = new Image(getClass().getResource("/Doncic.png").toString());
            if (pNum == 1){
                p1ImageView.setImage(pImage);
            }else{
                p2ImageView.setImage(pImage);
            }
        }
    }




    /**
     * Resets both players, the ball, and possession for a new round.
     *
     * <p>The player with possession starts closer to the bottom baseline. The
     * defender starts above them, creating a predictable one-on-one setup after
     * every make, miss, or block. This method also restarts the background music
     * and clears any translation left by ball path animations.</p>
     *
     * @param pNum player number that starts the next round with possession
     * @implNote Player model coordinates are updated immediately after moving
     * the ImageViews so shot and block calculations are correct before the next
     * frame.
     */
    public void resetPos(int pNum){     //Resets the Position of everything, which is useful after each 'round' or each shot made
        Player p1 = data.getPlayer(1);
        playerNBAImage(1);
        Player p2 = data.getPlayer(2);
        playerNBAImage(2);

        //Create the Ball and designate the Possession
        ball.setTranslateX(0); ball.setTranslateY(0);       //Resetting translation cuz of PathTransition
        ball.setLayoutX(700); ball.setLayoutY(375);
        switchPossession(pNum, false);

        //Set coordinates of Attacker and the Defender to the Setup Positions. Need to remember to update the Player objects' x and y Attributes as well.
        p1ImageView.setFitWidth(100);
        p1ImageView.setFitHeight(0);
        p1ImageView.setPreserveRatio(true);
        p2ImageView.setFitWidth(100);
        p2ImageView.setFitHeight(0);
        p2ImageView.setPreserveRatio(true);
        if (pNum == 1){
            p1ImageView.setLayoutX(700); p1ImageView.setLayoutY(700);
            p1.setPlayerX(p1ImageView.getLayoutX()); p1.setPlayerY(p1ImageView.getLayoutY());
            p2ImageView.setLayoutX(700); p2ImageView.setLayoutY(600);
            p2.setPlayerX(p2ImageView.getLayoutX()); p2.setPlayerY(p2ImageView.getLayoutY());
        } else{
            p1ImageView.setLayoutX(700); p1ImageView.setLayoutY(600);
            p1.setPlayerX(p1ImageView.getLayoutX()); p1.setPlayerY(p1ImageView.getLayoutY());
            p2ImageView.setLayoutX(700); p2ImageView.setLayoutY(700);
            p2.setPlayerX(p2ImageView.getLayoutX()); p2.setPlayerY(p2ImageView.getLayoutY());
        }

        //Starts playing a music
        if (gametracks.getStatus() == MediaPlayer.Status.PLAYING) {
            gametracks.stop();  // Stop if it's already playing
        }
        gametracks.seek(Duration.ZERO);  // Reset to beginning
        gametracks.play();
    }




    /**
     * Builds the in-game court screen for a new or resumed round.
     *
     * <p>The live screen includes the court image, drawn three-point line, court
     * lighting overlays, player ground effects, player sprites, basketball,
     * scoreboard, and player tags. The center of the playfield stays clear so
     * players can track movement and shots.</p>
     *
     * @param pNum player number that starts with possession
     * @see #resetPos(int)
     */
    public void setupState(int pNum){
        //Clearing the screen and adding the new images
        rootPane.getChildren().clear();
        Player p1 = data.getPlayer(1);
        Player p2 = data.getPlayer(2);
        resetPos(pNum);

        //Add the image of the basketball Court
        Image bg = new Image(getClass().getResource("/BasketballBackground.png").toString());
        ImageView courtView = new ImageView(bg);
        courtView.setFitWidth(1500);
        courtView.setFitHeight(850);
        courtView.setLayoutX(0); courtView.setLayoutY(0);
        Pane courtPolish = makeCourtPolish();
        Pane threePointLine = makeThreePointLine();

        // Scoreboard card (top-left)
        Rectangle scoreCard = new Rectangle(20, 20, 320, 110);
        scoreCard.getStyleClass().add("scoreboard-card");

        Text scoreLabel = new Text("SCORE");
        scoreLabel.getStyleClass().add("scoreboard-label");
        scoreLabel.setLayoutX(40); scoreLabel.setLayoutY(48);

        Text p1Tag = new Text("P1");
        p1Tag.setFill(Color.web("#34d27a"));
        p1Tag.setFont(Font.font("Helvetica Neue", FontWeight.BOLD, 16));
        p1Tag.setLayoutX(60); p1Tag.setLayoutY(118);

        Text p2Tag = new Text("P2");
        p2Tag.setFill(Color.web("#ff5577"));
        p2Tag.setFont(Font.font("Helvetica Neue", FontWeight.BOLD, 16));
        p2Tag.setLayoutX(280); p2Tag.setLayoutY(118);

        scoreboard.setText(p1.getScore() + "  :  " + p2.getScore());
        scoreboard.getStyleClass().add("scoreboard-value");
        scoreboard.setLayoutX(110); scoreboard.setLayoutY(110);

        Color p1Accent = Color.web("#34d27a");
        Color p2Accent = Color.web("#ff5577");
        Pane p1Ground = makePlayerGroundDetail(p1ImageView, p1Accent);
        Pane p2Ground = makePlayerGroundDetail(p2ImageView, p2Accent);
        Pane p1Top = makePlayerTopDetail(1, p1ImageView, p1Accent);
        Pane p2Top = makePlayerTopDetail(2, p2ImageView, p2Accent);

        // Player drop shadows
        p1ImageView.setEffect(new DropShadow(18, Color.rgb(0,0,0,0.72)));
        p2ImageView.setEffect(new DropShadow(18, Color.rgb(0,0,0,0.72)));

        //Adds all the stuff to the screen
        rootPane.getChildren().addAll(
            courtView,
            courtPolish,
            threePointLine,
            p1Ground, p2Ground,
            p2ImageView, p1ImageView, ball,
            p2Top, p1Top,
            scoreCard, scoreLabel, scoreboard, p1Tag, p2Tag
        );
    }




    /**
     * Registers keyboard controls for movement, shooting, and timed blocks.
     *
     * <p>Movement keys are stored in {@code heldKeys} and applied by the
     * animation timer for smooth continuous movement. Shoot/block keys are
     * handled only on their initial press so holding a key does not spam shots.
     * The same key is context-sensitive: it shoots when the player has
     * possession and attempts a block when the opponent has possession.</p>
     *
     * <p>Player 1 uses {@code W/A/S/D} plus {@code E}; player 2 uses
     * {@code I/J/K/L} plus {@code O}.</p>
     *
     * @implNote Block attempts are timestamped so the defender can press just
     * before or just after the shooter within the configured timing window.
     */
    public void playerMoves(){
        // Clear any stale state from previous matches
        heldKeys.clear();
        lastBlockAttemptMillis[1] = 0;
        lastBlockAttemptMillis[2] = 0;

        rootPane.setOnKeyPressed(event -> {
            KeyCode code = event.getCode();

            // One-shot actions: SHOOT / BLOCK — only fire on the initial press, not repeats
            if (code == KeyCode.E && !heldKeys.contains(KeyCode.E)) {
                int possessionState = data.getPossession();
                if (possessionState == 1) {
                    shoot(1);
                } else {
                    attemptBlock(1);
                }
            } else if (code == KeyCode.O && !heldKeys.contains(KeyCode.O)) {
                int possessionState = data.getPossession();
                if (possessionState == 2) {
                    shoot(2);
                } else {
                    attemptBlock(2);
                }
            }

            heldKeys.add(code);
        });

        rootPane.setOnKeyReleased(event -> heldKeys.remove(event.getCode()));
    }

    private int otherPlayer(int pNum) {
        return (pNum == 1) ? 2 : 1;
    }

    private void clearActiveShot() {
        activeShooterNum = 0;
        activeShotStartedMillis = 0;
        activeShotResolved = false;
        activeShotAnimation = null;
    }

    private void recordShotAttempt(int pNum) {
        if (pNum == 1) { p1ShotsMade++; } else { p2ShotsMade++; }
    }

    private void recordBlockedShot(int shooterNum) {
        if (shooterNum == 1) { badP1Shots++; } else { badP2Shots++; }
    }

    private boolean isBlockTimingGood(int defenderNum, long shotStartedMillis) {
        long blockAttemptMillis = lastBlockAttemptMillis[defenderNum];
        return blockAttemptMillis > 0
            && Math.abs(blockAttemptMillis - shotStartedMillis) <= BLOCK_WINDOW_MILLIS;
    }

    private boolean isDefenderInFrontOfShooter(int shooterNum, int defenderNum) {
        Player shooter = data.getPlayer(shooterNum);
        Player defender = data.getPlayer(defenderNum);

        double shooterX = shooter.getPlayerX();
        double shooterY = shooter.getPlayerY();
        double defenderX = defender.getPlayerX();
        double defenderY = defender.getPlayerY();

        double hoopVectorX = Player.HOOP_X - shooterX;
        double hoopVectorY = Player.HOOP_Y - shooterY;
        double hoopDistance = Math.sqrt((hoopVectorX * hoopVectorX) + (hoopVectorY * hoopVectorY));
        if (hoopDistance == 0) {
            return false;
        }

        double defenderVectorX = defenderX - shooterX;
        double defenderVectorY = defenderY - shooterY;
        double projection = ((defenderVectorX * hoopVectorX) + (defenderVectorY * hoopVectorY)) / hoopDistance;
        double perpendicular = Math.abs((defenderVectorX * hoopVectorY) - (defenderVectorY * hoopVectorX)) / hoopDistance;

        return projection > 0
            && projection <= BLOCK_MAX_DISTANCE
            && perpendicular <= BLOCK_LANE_WIDTH;
    }

    private void attemptBlock(int defenderNum) {
        long now = System.currentTimeMillis();
        lastBlockAttemptMillis[defenderNum] = now;

        if (isBallInFlight
                && activeShooterNum != 0
                && activeShooterNum != defenderNum
                && Math.abs(now - activeShotStartedMillis) <= BLOCK_WINDOW_MILLIS
                && isDefenderInFrontOfShooter(activeShooterNum, defenderNum)) {
            onBlock(defenderNum);
        }
    }

    private void onBlock(int defenderNum) {
        if (activeShotResolved) {
            return;
        }

        activeShotResolved = true;
        int shooterNum = activeShooterNum;
        recordBlockedShot(shooterNum);

        if (activeShotAnimation != null) {
            activeShotAnimation.stop();
        }

        ImageView shooterView = (shooterNum == 1) ? p1ImageView : p2ImageView;
        shooterView.setTranslateX(0);
        shooterView.setTranslateY(0);
        shooterView.setScaleX(1.0);
        shooterView.setScaleY(1.0);
        ball.setTranslateX(0);
        ball.setTranslateY(0);
        Player defender = data.getPlayer(defenderNum);
        ball.setLayoutX(defender.getPlayerX());
        ball.setLayoutY(defender.getPlayerY());

        showShotFeedback("BLOCK!", Color.web("#3a8dff"));
        screenShake(10);

        PauseTransition delay = new PauseTransition(Duration.millis(500));
        delay.setOnFinished(event -> finishRound(shooterNum, false));
        delay.play();
    }




    /**
     * Starts a shot attempt for the player with possession.
     *
     * <p>The method guards against duplicate shots while the ball is already in
     * flight, records shot timing for block checks, runs the make/miss
     * probability through {@link Player#makeOrMiss(Data)}, and starts the
     * shooter and ball animations. If the defender already pressed block within
     * the valid timing window and is in the shooting lane, the shot is blocked
     * before the animation begins.</p>
     *
     * @param pNum shooting player number
     * @see #playerMoves()
     * @see Player#makeOrMiss(Data)
     */
    public void shoot(int pNum){
        if (isBallInFlight){
            return;     //Can't SHOOT when the Ball is currently in the air
        }

        isBallInFlight = true;
        activeShooterNum = pNum;
        activeShotStartedMillis = System.currentTimeMillis();
        activeShotResolved = false;

        int defenderNum = otherPlayer(pNum);
        recordShotAttempt(pNum);
        if (isBlockTimingGood(defenderNum, activeShotStartedMillis)
                && isDefenderInFrontOfShooter(pNum, defenderNum)) {
            onBlock(defenderNum);
            return;
        }

        Player p = data.getPlayer(pNum);
        boolean make = p.makeOrMiss(data);

        ImageView shooterView = (pNum == 1) ? p1ImageView : p2ImageView;

        // === 1. Shooter motion: small dip, then big leap up, then back down ===
        TranslateTransition dip = new TranslateTransition(Duration.millis(180), shooterView);
        dip.setByY(15);
        dip.setInterpolator(Interpolator.EASE_OUT);

        TranslateTransition leap = new TranslateTransition(Duration.millis(320), shooterView);
        leap.setByY(-95);                         // jumps up
        leap.setInterpolator(Interpolator.EASE_OUT);

        ScaleTransition stretch = new ScaleTransition(Duration.millis(320), shooterView);
        stretch.setToY(1.08); stretch.setToX(0.96); // stretches as they release
        stretch.setInterpolator(Interpolator.EASE_OUT);

        ParallelTransition rise = new ParallelTransition(leap, stretch);

        TranslateTransition land = new TranslateTransition(Duration.millis(280), shooterView);
        land.setByY(80);
        land.setInterpolator(Interpolator.EASE_IN);

        ScaleTransition unstretch = new ScaleTransition(Duration.millis(280), shooterView);
        unstretch.setToY(1.0); unstretch.setToX(1.0);

        ParallelTransition fall = new ParallelTransition(land, unstretch);

        // === 2. Ball arc: parabolic path from shooter's hand to the rim ===
        double startX = ball.getLayoutX();
        double startY = ball.getLayoutY();
        double rimX = 750;
        double rimY = 90;
        double dx = rimX - startX;
        double dy = rimY - startY;
        // Apex high above the midpoint for a nice rainbow arc
        double midX = dx / 2;
        double midY = dy - 320;

        Path arc = new Path();
        arc.getElements().add(new MoveTo(0, 0));
        arc.getElements().add(new QuadCurveTo(midX, midY, dx, dy));

        PathTransition shotTransition = new PathTransition(Duration.millis(900), arc, ball);
        shotTransition.setInterpolator(Interpolator.EASE_OUT);

        // Ball spins while in flight
        RotateTransition spin = new RotateTransition(Duration.millis(900), ball);
        spin.setByAngle(540);

        ParallelTransition flight = new ParallelTransition(shotTransition, spin);

        // === 3. Wait briefly for the leap to peak before the ball is "released" ===
        PauseTransition release = new PauseTransition(Duration.millis(200));

        // === 4. After ball reaches rim: SWISH or BRICK ===
        shotTransition.setOnFinished(event -> {
            if (activeShotResolved) {
                return;
            }
            activeShotResolved = true;
            if (make) {
                onMake(pNum);
            } else {
                onMiss(pNum, dx, dy);
            }
        });

        // Chain it all together: dip → (rise + ball flight after small release delay) → fall
        SequentialTransition fullShot = new SequentialTransition(
            dip,
            new ParallelTransition(rise, new SequentialTransition(release, flight)),
            fall
        );
        activeShotAnimation = fullShot;

        System.out.println("Player 1 Score: " + data.getPlayer(1).getScore()
            + " Player 2 Score: " + data.getPlayer(2).getScore()
            + " DistanceProb: " + data.distanceProb(pNum, 1) + ',' + data.distanceProb(pNum, 2) + ',' + data.distanceProb(pNum, 3));

        fullShot.play();
    }

    /** Ball goes in: SWISH popup, score update, light celebration, then next round. */
    private void onMake(int pNum) {
        int pts = data.getPlayer(pNum).pointValueForCurrentPosition();
        scoreboard.setText(data.getPlayer(1).getScore() + "  :  " + data.getPlayer(2).getScore());
        String banner = (pts == 3) ? "SWISH! +3" : "BUCKET! +2";
        showShotFeedback(banner, Color.web("#34d27a"));
        screenShake(pts == 3 ? 9 : 6);

        // Ball drops through the rim
        TranslateTransition drop = new TranslateTransition(Duration.millis(450), ball);
        drop.setByY(120);
        drop.setInterpolator(Interpolator.EASE_IN);
        drop.setOnFinished(ev -> finishRound(pNum, true));
        drop.play();
    }

    /** Ball misses: bounces off, BRICK popup, then next round. */
    private void onMiss(int pNum, double dx, double dy) {
        if (pNum == 1) { badP1Shots++; } else { badP2Shots++; }
        showShotFeedback("BRICK!", Color.web("#ff5577"));

        // Ball ricochets: a small bounce up-and-out, then falls
        Path bounce = new Path();
        bounce.getElements().add(new MoveTo(dx, dy));
        bounce.getElements().add(new QuadCurveTo(dx + 60, dy - 40, dx + 110, dy + 90));

        PathTransition bounceT = new PathTransition(Duration.millis(700), bounce, ball);
        bounceT.setInterpolator(Interpolator.EASE_OUT);

        RotateTransition bounceSpin = new RotateTransition(Duration.millis(700), ball);
        bounceSpin.setByAngle(-360);

        ParallelTransition miss = new ParallelTransition(bounceT, bounceSpin);
        miss.setOnFinished(ev -> finishRound(pNum, false));
        miss.play();
    }

    /** Centered popup text that pops in, then fades out. */
    private void showShotFeedback(String message, Color color) {
        Text txt = new Text(message);
        txt.setFill(color);
        txt.setFont(Font.font("Helvetica Neue", FontWeight.BLACK, 110));
        txt.setEffect(new DropShadow(20, Color.rgb(0, 0, 0, 0.8)));
        // Rough centering for an unknown text width
        txt.setLayoutX(560);
        txt.setLayoutY(380);
        txt.setScaleX(0.3); txt.setScaleY(0.3);
        txt.setOpacity(0);
        rootPane.getChildren().add(txt);

        ScaleTransition pop = new ScaleTransition(Duration.millis(260), txt);
        pop.setToX(1.0); pop.setToY(1.0);
        pop.setInterpolator(Interpolator.EASE_OUT);

        FadeTransition fadeIn = new FadeTransition(Duration.millis(180), txt);
        fadeIn.setFromValue(0); fadeIn.setToValue(1);

        PauseTransition hold = new PauseTransition(Duration.millis(700));

        FadeTransition fadeOut = new FadeTransition(Duration.millis(350), txt);
        fadeOut.setFromValue(1); fadeOut.setToValue(0);

        SequentialTransition seq = new SequentialTransition(
            new ParallelTransition(pop, fadeIn), hold, fadeOut
        );
        seq.setOnFinished(e -> rootPane.getChildren().remove(txt));
        seq.play();
    }

    /** Quick screen shake for celebrations. */
    private void screenShake(int intensity) {
        TranslateTransition t = new TranslateTransition(Duration.millis(50), rootPane);
        t.setFromX(0); t.setByX(intensity);
        t.setAutoReverse(true); t.setCycleCount(6);
        t.setOnFinished(e -> { rootPane.setTranslateX(0); rootPane.setTranslateY(0); });
        t.play();
    }




    /**
     * Completes a shot sequence and starts the next round or stats screen.
     *
     * <p>Made shots let the shooter keep possession. Missed or blocked shots
     * give possession to the other player. If either player has reached the
     * winning score, the game loop stops and the stats screen is shown instead
     * of resetting player positions.</p>
     *
     * @param shooterNum player number that attempted the shot
     * @param made whether the shot was made
     * @implNote This method always clears active shot state at the end so the
     * next round can accept movement, shots, and block attempts normally.
     */
    public void finishRound(int shooterNum, boolean made){
        int nextPossession = 0;
        Player p1 = data.getPlayer(1);
        Player p2 = data.getPlayer(2);

        if (p1.getScore() >= 11 || p2.getScore() >= 11){
            gameRunning = false;
            gameEngine.stop();
            statsScreen();
        }else{      //Winner's Ball and Loser's Loss
            if (made){
                if (shooterNum == 1){
                nextPossession = 1;
                }else{
                nextPossession = 2;
                }
            }else{
                if (shooterNum == 1){
                    nextPossession = 2;
                }else{
                    nextPossession = 1;
                }
            }
            resetPos(nextPossession);
        }
        
        isBallInFlight = false;     //Indicates that the Ball is no longer in the midst of its shot path
        clearActiveShot();
    }




    /**
     * Shows the post-game score and shooting statistics.
     *
     * <p>The screen displays the winner, final score, shots made, shots missed,
     * and total attempts for both players. It also provides actions to play
     * again or quit the application.</p>
     */
    public void statsScreen(){
        rootPane.getChildren().clear();

        int p1Score = data.getPlayer(1).getScore();
        int p2Score = data.getPlayer(2).getScore();
        int winner = p1Score > p2Score ? 1 : 2;
        int goodP1Shots = p1ShotsMade - badP1Shots;
        int goodP2Shots = p2ShotsMade - badP2Shots;

        // Banner
        Text bye = new Text("GAME OVER");
        bye.getStyleClass().add("final-banner");
        bye.setLayoutX(560); bye.setLayoutY(140);

        Text winnerText = new Text("Player " + winner + " wins!");
        winnerText.getStyleClass().add("subtitle");
        winnerText.setLayoutX(640); winnerText.setLayoutY(195);

        Text finalScore = new Text(p1Score + "   :   " + p2Score);
        finalScore.setFill(Color.WHITE);
        finalScore.setFont(Font.font("Helvetica Neue", FontWeight.BLACK, 96));
        finalScore.setEffect(new DropShadow(20, Color.web("#ff8a3d", 0.7)));
        finalScore.setLayoutX(600); finalScore.setLayoutY(320);

        // P1 card
        Rectangle p1Card = new Rectangle(220, 400, 460, 320);
        p1Card.getStyleClass().add("stats-card");
        Text p1Head = new Text("PLAYER 1");
        p1Head.setFill(Color.web("#34d27a"));
        p1Head.setFont(Font.font("Helvetica Neue", FontWeight.BLACK, 36));
        p1Head.setLayoutX(260); p1Head.setLayoutY(465);

        Text p1Lines = new Text(
            "Final Score:        " + p1Score +
            "\nShots Made:        " + goodP1Shots +
            "\nShots Missed:      " + badP1Shots +
            "\nTotal Attempts:    " + p1ShotsMade
        );
        p1Lines.getStyleClass().add("stats-line");
        p1Lines.setFont(Font.font("Menlo", 22));
        p1Lines.setLayoutX(260); p1Lines.setLayoutY(520);

        // P2 card
        Rectangle p2Card = new Rectangle(820, 400, 460, 320);
        p2Card.getStyleClass().add("stats-card");
        Text p2Head = new Text("PLAYER 2");
        p2Head.setFill(Color.web("#ff5577"));
        p2Head.setFont(Font.font("Helvetica Neue", FontWeight.BLACK, 36));
        p2Head.setLayoutX(860); p2Head.setLayoutY(465);

        Text p2Lines = new Text(
            "Final Score:        " + p2Score +
            "\nShots Made:        " + goodP2Shots +
            "\nShots Missed:      " + badP2Shots +
            "\nTotal Attempts:    " + p2ShotsMade
        );
        p2Lines.getStyleClass().add("stats-line");
        p2Lines.setFont(Font.font("Menlo", 22));
        p2Lines.setLayoutX(860); p2Lines.setLayoutY(520);

        Text thanks = new Text("Thanks for playing  •  GG");
        thanks.getStyleClass().add("byline");
        thanks.setLayoutX(640); thanks.setLayoutY(745);

        // Action buttons
        Button playAgainBtn = styledButton("PLAY AGAIN", "btn-success");
        playAgainBtn.setLayoutX(530); playAgainBtn.setLayoutY(775);
        playAgainBtn.setOnAction(e -> restartGame());

        Button quitBtn = styledButton("QUIT", "btn-secondary");
        quitBtn.setLayoutX(830); quitBtn.setLayoutY(775);
        quitBtn.setOnAction(e -> Platform.exit());

        rootPane.getChildren().addAll(
            makeBackdrop(), makeAmbientGlow(),
            bye, winnerText, finalScore,
            p1Card, p1Head, p1Lines,
            p2Card, p2Head, p2Lines,
            thanks,
            playAgainBtn, quitBtn
        );

        // Banner pop-in
        ScaleTransition pop = new ScaleTransition(Duration.millis(500), bye);
        pop.setFromX(0.6); pop.setFromY(0.6); pop.setToX(1); pop.setToY(1);
        pop.play();
    }




    /*// Method to move the ball based on how much time has passed
    private void moveBall(double elapsedSeconds) {
        // Update position based on speed and elapsed time
        double x = ball.getLayoutX() + dx * elapsedSeconds;
        double y = ball.getLayoutY() + dy * elapsedSeconds;

        // Check for wall collisions and reverse direction if needed
        if (x < ball.getRadius() || x >= scene.getWidth() - ball.getRadius()) {
            dx = -dx;
        }
        if (y < ball.getRadius() || y >= scene.getHeight() - ball.getRadius()) {
            dy = -dy;
        }

        // Apply new position
        ball.setLayoutX(x);
        ball.setLayoutY(y);
    }




    // Inner class that handles frame-by-frame animation using system time
    private class BallAnimationTimer extends AnimationTimer {
        private long lastUpdate = 0;  // Timestamp of the last frame

        @Override
        public void handle(long now) {
            if (lastUpdate > 0) {
                // Convert nanoseconds to seconds for speed calculation
                double elapsedSeconds = (now - lastUpdate) / 1_000_000_000.0;
                moveBall(elapsedSeconds);
            }
            lastUpdate = now;  // Update the timestamp for next frame
        }
    }*/




    /*// Play the sound from the beginning
    private void playSound() {
        if (c_major.getStatus() == MediaPlayer.Status.PLAYING) {
            c_major.stop();  // Stop if it's already playing
        }
        c_major.seek(Duration.ZERO);  // Reset to beginning
        c_major.play();
    }*/



    
    /**
     * Application entry point.
     *
     * <p>The JavaFX Maven plugin launches this class. The method prints the
     * JavaFX runtime version for quick troubleshooting and then delegates to the
     * JavaFX lifecycle.</p>
     *
     * @param args command-line arguments passed by JavaFX
     */
    public static void main(String[] args) {
        System.out.println("JavaFX version: " + System.getProperty("javafx.runtime.version"));
        launch(args);
    }
}
