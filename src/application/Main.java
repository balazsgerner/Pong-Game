package application;

import java.util.Timer;
import java.util.TimerTask;

import javafx.animation.Animation;
import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.PathTransition;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.stage.Stage;
import javafx.util.Duration;

public class Main extends Application {

  private static final double CANVAS_WIDTH = 800;

  private static final double CANVAS_HEIGHT = 800;

  private static final double CENTERX = CANVAS_WIDTH / 2;

  private static final double CENTERY = CANVAS_HEIGHT / 2;

  private static final double PADDLE_HEIGHT = 200;

  private static final double PADDLE_WIDTH = 15;

  private static final double BALL_RADIUS = 15;

  private static final double PADDLE_SPEED = 30;

  private double ballPosX = 20;

  private double ballPosy = 20;

  private double ballSpeedX = 5;

  private double ballSpeedY = 4;

  private double paddleY = CENTERY - PADDLE_HEIGHT / 2;

  private Label clickToPlayLbl;

  private AnchorPane root;

  private Scene scene;

  @Override
  public void start(Stage primaryStage) {
    try {
      root = new AnchorPane();
      scene = new Scene(root, CANVAS_WIDTH, CANVAS_HEIGHT);
      scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());

      Label label = new Label("Pong Game");
      label.getStyleClass().add("title");
      Label demoLabel = new Label("DEMO");
      root.getChildren().add(demoLabel);
      demoLabel.setOpacity(0);
      centerLabel(demoLabel);
      demoLabel.setLayoutY(250);

      clickToPlayLbl = new Label("Click to Play");
      clickToPlayLbl.setOpacity(0);
      root.getChildren().add(clickToPlayLbl);
      centerLabel(clickToPlayLbl);
      clickToPlayLbl.setLayoutY(350);

      showTitleLabel(label, demoLabel);
      root.getChildren().add(label);
      primaryStage.setScene(scene);

      root.setCursor(Cursor.NONE);
      primaryStage.setResizable(false);
      primaryStage.sizeToScene();
      Image icon = new Image(Main.class.getResourceAsStream("/resources/icon/8bit_icon.png"));
      primaryStage.getIcons().add(icon);
      primaryStage.setTitle("Pong Game");
      primaryStage.show();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private void showTitleLabel(Label label, Label demoLabel) {
    Path path = new Path();
    path.getElements().add(new MoveTo(CANVAS_WIDTH / 2, 10));
    path.getElements().add(new LineTo(CANVAS_WIDTH / 2, 200));
    PathTransition pathTransition = new PathTransition(Duration.millis(2000), path, label);
    pathTransition.setAutoReverse(false);
    pathTransition.setOnFinished(e -> showDemoMessage(demoLabel));
    pathTransition.play();
  }

  private void showDemoMessage(Label demoLabel) {
    FadeTransition ft = new FadeTransition(Duration.millis(1250), demoLabel);
    ft.setFromValue(0.0);
    ft.setToValue(1);
    ft.setAutoReverse(false);
    ft.setCycleCount(1);
    ft.play();

    ft.setOnFinished(e3 -> showClickToPlayMessage());
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
    Canvas canvas = new Canvas(CANVAS_WIDTH, CANVAS_HEIGHT);

    rePaintCanvas(canvas);
    Timeline timeline = new Timeline(new KeyFrame(Duration.millis(15), e -> rePaintCanvas(canvas)));
    timeline.setCycleCount(Animation.INDEFINITE);
    timeline.play();

    Timer moveBallTimer = new Timer(true);
    moveBallTimer.scheduleAtFixedRate(new TimerTask() {

      @Override
      public void run() {
        moveBall();
      }

      private void moveBall() {
        if (ballPosX + ballSpeedX <= 0 || ballPosX + ballSpeedX + BALL_RADIUS >= CANVAS_WIDTH) {
          ballSpeedX = -ballSpeedX;
        } else {
          ballPosX = ballPosX + ballSpeedX;
        }

        if (ballPosy + ballSpeedY <= 0 || ballPosy + ballSpeedY + BALL_RADIUS >= CANVAS_HEIGHT) {
          ballSpeedY = -ballSpeedY;
        } else {
          ballPosy = ballPosy + ballSpeedY;
        }
      }
    }, 0, 20);

    scene.setOnKeyPressed(e -> movePaddle(e));
    BorderPane gamePanel = new BorderPane(canvas);
    gamePanel.setCursor(Cursor.NONE);
    scene.setRoot(gamePanel);
  }

  private void movePaddle(KeyEvent e) {
    KeyCode code = e.getCode();
    switch (code) {
    case UP:
      if (paddleY >= PADDLE_SPEED) {
        paddleY -= PADDLE_SPEED;
      }
      break;
    case DOWN:
      if (paddleY + PADDLE_HEIGHT <= CANVAS_HEIGHT - PADDLE_SPEED) {
        paddleY += PADDLE_SPEED;
      }
      break;
    default:
      break;
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
    ctx.setLineWidth(PADDLE_WIDTH);
    ctx.strokeLine(PADDLE_WIDTH / 2, paddleY, PADDLE_WIDTH / 2, paddleY + PADDLE_HEIGHT);

    ctx.setFill(Color.WHITE);
    ctx.fillOval(ballPosX, ballPosy, BALL_RADIUS, BALL_RADIUS);
  }

  public static void main(String[] args) {
    launch(args);
  }
}
