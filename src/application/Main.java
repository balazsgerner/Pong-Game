package application;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ThreadLocalRandom;

import javafx.animation.Animation;
import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.CornerRadii;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Paint;
import javafx.scene.paint.Stop;
import javafx.stage.Stage;
import javafx.util.Duration;

public class Main extends Application {

  // Enable splash screen loading
  private static final boolean DEMO_FEATURE_ENABLED = false;

  private static final boolean DEBUG_MODE_ENABLED = true;

  private static final double CANVAS_WIDTH = 800;

  private static final double CANVAS_HEIGHT = 800;

  private static final double CENTERX = CANVAS_WIDTH / 2;

  private static final double CENTERY = CANVAS_HEIGHT / 2;

  private static final double PADDLE_HEIGHT = 200;

  private static final double PADDLE_WIDTH = 15;

  private static final double BALL_RADIUS = 15;

  private static final Color INITIAL_PADDLE1_COLOR = Color.GREEN;

  private static final Color LIGHT_PADDLE1_COLOR = Color.GREENYELLOW;

  private static final Color INITIAL_PADDLE2_COLOR = Color.BLUE;

  private static final Color LIGHT_PADDLE2_COLOR = Color.DEEPSKYBLUE;

  private double ballPosX = CENTERX;

  private double ballPosY = CENTERY;

  // default 2.5
  private double ballSpeedX = 5;

  // default 2
  private double ballSpeedY = 0;

  private double paddle1Y = CENTERY - PADDLE_HEIGHT / 2;

  private double paddle2Y = CENTERY - PADDLE_HEIGHT / 2;

  private Paint currentPaddle1Color = INITIAL_PADDLE1_COLOR;

  private Paint currentPaddle2Color = INITIAL_PADDLE2_COLOR;

  private Label clickToPlayLbl;

  private AnchorPane root;

  private Scene scene;

  private Canvas canvas;

  private Timeline timeline;

  private Timer moveBallTimer;

  private BorderPane gamePanel;

  @Override
  public void start(Stage primaryStage) {
    try {
      root = new AnchorPane();
      root.setBackground(new Background(new BackgroundFill(Color.WHITESMOKE, new CornerRadii(0), new Insets(0))));
      scene = new Scene(root, CANVAS_WIDTH, CANVAS_HEIGHT);
      scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());

      Label label = new Label("Pong Game");
      label.setLayoutY(200);
      label.getStyleClass().add("title");
      Label demoLabel = new Label("DEMO");
      root.getChildren().add(demoLabel);
      demoLabel.setOpacity(0);
      demoLabel.setLayoutY(250);

      clickToPlayLbl = new Label("Click to Play");
      clickToPlayLbl.setOpacity(0);
      root.getChildren().add(clickToPlayLbl);
      clickToPlayLbl.setLayoutY(350);

      root.getChildren().add(label);
      primaryStage.setScene(scene);
      primaryStage.setFullScreen(true);

      centerLabel(demoLabel);
      centerLabel(clickToPlayLbl);
      centerLabel(label);

      canvas = new Canvas(CANVAS_WIDTH, CANVAS_HEIGHT);
      scene.setOnKeyPressed(e -> {
        KeyCode code = e.getCode();
        if (code.equals(KeyCode.Q)) {
          System.out.println(ballPosX + " " + ballPosY + " " + ballSpeedX + " " + ballSpeedY);
        } else if (code.equals(KeyCode.R)) {
          stopGame();
          startGame();
        }
      });

      if (DEMO_FEATURE_ENABLED) {
        showTitleLabel(label, demoLabel);
      } else {
        initCanvas();
      }

      root.setCursor(Cursor.NONE);

      Image icon = new Image(Main.class.getResourceAsStream("/resources/icon/8bit_icon.png"));
      primaryStage.getIcons().add(icon);
      primaryStage.setTitle("Pong Game");
      primaryStage.show();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private void startGame() {
    ballPosX = CENTERX;
    ballPosY = CENTERY;
    int xAxis = ThreadLocalRandom.current().nextInt(1);
    int yAxis = ThreadLocalRandom.current().nextInt(1);
    ballSpeedX = xAxis == 1 ? -2.5 : 2.5;
    ballSpeedY = yAxis == 1 ? 2 : -2;
    initCanvas();
  }

  private void stopGame() {
    timeline.stop();
    moveBallTimer.cancel();
  }

  private void showTitleLabel(Label label, Label demoLabel) {
    FadeTransition ft = new FadeTransition(Duration.millis(1500), label);
    ft.setFromValue(0.0);
    ft.setToValue(1.0);
    ft.setAutoReverse(false);
    ft.setOnFinished(e -> showDemoMessage(demoLabel));
    ft.play();
  }

  private void showDemoMessage(Label demoLabel) {
    FadeTransition ft = new FadeTransition(Duration.millis(1500), demoLabel);
    ft.setFromValue(0.0);
    ft.setToValue(1);
    ft.setAutoReverse(false);
    ft.setCycleCount(1);
    ft.play();

    ft.setOnFinished(e -> showClickToPlayMessage());
  }

  private void centerLabel(Label label) {
    AnchorPane.setLeftAnchor(label, 0.0);
    AnchorPane.setRightAnchor(label, 0.0);
    label.setAlignment(Pos.CENTER);
  }

  private void showClickToPlayMessage() {
    FadeTransition ft = new FadeTransition(Duration.millis(1250), clickToPlayLbl);
    ft.setFromValue(0.0);
    ft.setToValue(0.5);
    ft.setCycleCount(Animation.INDEFINITE);
    ft.setAutoReverse(true);
    ft.play();
    root.setOnMousePressed(e -> initCanvas());
  }

  private void initCanvas() {
    rePaintCanvas(canvas);
    timeline = new Timeline(new KeyFrame(Duration.millis(15), e -> rePaintCanvas(canvas)));
    timeline.setCycleCount(Animation.INDEFINITE);
    timeline.play();

    moveBallTimer = new Timer(true);
    moveBallTimer.scheduleAtFixedRate(new TimerTask() {

      private Timer colorChangeTimer;

      @Override
      public void run() {
        moveBall();
      }

      @Override
      public boolean cancel() {
        colorChangeTimer.cancel();
        return super.cancel();
      }

      private void moveBall() {
        if (ballPosX == PADDLE_WIDTH && ballSpeedX < 0) {
          if (paddle1Y <= ballPosY && paddle1Y + PADDLE_HEIGHT - BALL_RADIUS >= ballPosY) {
            ballSpeedX = -ballSpeedX;

            // Change color if paddle is hit
            startColorChangeTimer(1);
          }
        } else if (ballPosX + BALL_RADIUS == CANVAS_WIDTH && ballSpeedX > 0) {
          ballSpeedX = -ballSpeedX;

          // Change color if paddle is hit
          startColorChangeTimer(2);
        } else {
          ballPosX += ballSpeedX;
        }
        if (ballPosY == 0 && ballSpeedY < 0) {
          ballSpeedY = -ballSpeedY;
        } else if (ballPosY + BALL_RADIUS >= CANVAS_HEIGHT && ballSpeedY > 0) {
          ballSpeedY = -ballSpeedY;
        } else {
          ballPosY += ballSpeedY;
        }
      }

      private void startColorChangeTimer(int paddleNum) {
        colorChangeTimer = new Timer(true);
        colorChangeTimer.schedule(new TimerTask() {

          int cycle = 0;

          List<Stop> stops = new ArrayList<>();

          {
            if (paddleNum == 1) {
              stops.add(new Stop(0.0, INITIAL_PADDLE1_COLOR));
              stops.add(new Stop((ballPosY - paddle1Y) / PADDLE_HEIGHT, LIGHT_PADDLE1_COLOR));
              stops.add(new Stop(1.0, INITIAL_PADDLE1_COLOR));
            } else {
              stops.add(new Stop(0.0, INITIAL_PADDLE2_COLOR));
              stops.add(new Stop((ballPosY - paddle2Y) / PADDLE_HEIGHT, LIGHT_PADDLE2_COLOR));
              stops.add(new Stop(1.0, INITIAL_PADDLE2_COLOR));
            }
          }

          @Override
          public void run() {
            if (paddleNum == 1) {
              if (cycle++ < 40) {
                currentPaddle1Color = new LinearGradient(0, paddle1Y, 0, paddle1Y + PADDLE_HEIGHT, false, CycleMethod.NO_CYCLE, stops);
              } else {
                currentPaddle1Color = INITIAL_PADDLE1_COLOR;
                colorChangeTimer.cancel();
              }
            } else {
              if (cycle++ < 40) {
                currentPaddle2Color = new LinearGradient(CANVAS_WIDTH - PADDLE_WIDTH, paddle2Y, CANVAS_WIDTH, paddle2Y + PADDLE_HEIGHT, false,
                    CycleMethod.NO_CYCLE, stops);
              } else {
                currentPaddle2Color = INITIAL_PADDLE2_COLOR;
                colorChangeTimer.cancel();
              }

            }

          }
        }, 0, 10);
      }
    }, 0, 5);

    scene.setOnMouseMoved(e -> movePaddle(e));
    gamePanel = new BorderPane(canvas);
    gamePanel.setCursor(Cursor.NONE);
    scene.setRoot(gamePanel);
  }

  private void movePaddle(MouseEvent e) {
    double y = e.getSceneY();
    if (y <= CANVAS_HEIGHT - PADDLE_HEIGHT) {
      paddle1Y = y;
    }
  }

  private void rePaintCanvas(Canvas canvas) {
    GraphicsContext ctx = canvas.getGraphicsContext2D();
    ctx.setFill(Color.BLACK);
    ctx.fillRect(0, 0, CANVAS_WIDTH, CANVAS_HEIGHT);

    ctx.setStroke(Color.WHITE);
    ctx.setLineWidth(5);
    ctx.setLineDashes(10);
    ctx.strokeLine(CENTERX, 0, CENTERX, CANVAS_HEIGHT);

    ctx.setLineDashes(null);
    ctx.setStroke(currentPaddle1Color);

    // draw paddles
    ctx.setLineWidth(PADDLE_WIDTH);
    ctx.strokeLine(PADDLE_WIDTH / 2, paddle1Y, PADDLE_WIDTH / 2, paddle1Y + PADDLE_HEIGHT);
    ctx.setStroke(currentPaddle2Color);
    ctx.strokeLine(CANVAS_WIDTH - PADDLE_WIDTH / 2, paddle2Y, CANVAS_WIDTH - PADDLE_WIDTH / 2, paddle2Y + PADDLE_HEIGHT);

    ctx.setFill(Color.WHITE);
    ctx.fillOval(ballPosX, ballPosY, BALL_RADIUS, BALL_RADIUS);

    ctx.setStroke(Color.WHITE);
    ctx.setLineWidth(2.5);
    ctx.strokeRect(0, 0, CANVAS_HEIGHT, CANVAS_HEIGHT);

    if (DEBUG_MODE_ENABLED) {
      ctx.setStroke(Color.MAGENTA);
      ctx.setLineWidth(1);
      ctx.setLineDashes(5);

      ctx.strokeLine(0, CENTERY, CANVAS_HEIGHT, CENTERY);
      ctx.strokeLine(PADDLE_WIDTH, 0, PADDLE_WIDTH, CANVAS_HEIGHT);
      ctx.strokeLine(CANVAS_WIDTH - PADDLE_WIDTH, 0, CANVAS_WIDTH - PADDLE_WIDTH, CANVAS_HEIGHT);

    }
  }

  public static void main(String[] args) {
    launch(args);
  }
}
