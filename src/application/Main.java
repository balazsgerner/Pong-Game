package application;

import java.util.Timer;
import java.util.TimerTask;

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
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.CornerRadii;
import javafx.scene.paint.Color;
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

  private static final Color INITIAL_PADDLE_COLOR = Color.GREEN;

  private static final Color LIGHT_PADDLE_COLOR = Color.GREENYELLOW;

  private double ballPosX = 20;

  private double ballPosY = 20;

  private double ballSpeedX = 2.5;

  private double ballSpeedY = 2;

  private double paddleY = CENTERY - PADDLE_HEIGHT / 2;

  private Color currentPaddleColor = INITIAL_PADDLE_COLOR;

  private Label clickToPlayLbl;

  private AnchorPane root;

  private Scene scene;

  private Canvas canvas;

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

      showTitleLabel(label, demoLabel);

      root.setCursor(Cursor.NONE);

      Image icon = new Image(Main.class.getResourceAsStream("/resources/icon/8bit_icon.png"));
      primaryStage.getIcons().add(icon);
      primaryStage.setTitle("Pong Game");
      primaryStage.show();
    } catch (Exception e) {
      e.printStackTrace();
    }
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
    canvas = new Canvas(CANVAS_WIDTH, CANVAS_HEIGHT);

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
          if (ballPosX + ballSpeedX <= 0 && ballPosY >= paddleY && ballPosY <= paddleY + PADDLE_HEIGHT) {
            Timer timer = new Timer();
            timer.scheduleAtFixedRate(new TimerTask() {

              int cycle = 0;

              @Override
              public void run() {
                changeColor(timer);
              }

              private void changeColor(Timer timer) {
                switch (cycle++) {
                case 0:
                  currentPaddleColor = LIGHT_PADDLE_COLOR;
                  break;
                case 1:
                  currentPaddleColor = INITIAL_PADDLE_COLOR;
                default:
                  timer.cancel();
                  break;
                }
              }
            }, 0, 150);
          }
          ballSpeedX = -ballSpeedX;
        } else {
          ballPosX += ballSpeedX;
        }
        if (ballPosY + ballSpeedY <= 0 || ballPosY + BALL_RADIUS + ballSpeedY >= CANVAS_HEIGHT) {
          ballSpeedY = -ballSpeedY;
        } else {
          ballPosY += ballSpeedY;
        }
      }
    }, 0, 5);

    scene.setOnMouseMoved(e -> movePaddle(e));
    BorderPane gamePanel = new BorderPane(canvas);
    gamePanel.setCursor(Cursor.NONE);
    scene.setRoot(gamePanel);
  }

  private void movePaddle(MouseEvent e) {
    double y = e.getSceneY();
    if (y <= CANVAS_HEIGHT - PADDLE_HEIGHT) {
      paddleY = y;
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
    ctx.setStroke(currentPaddleColor);
    ctx.setLineWidth(PADDLE_WIDTH);
    ctx.strokeLine(PADDLE_WIDTH / 2, paddleY, PADDLE_WIDTH / 2, paddleY + PADDLE_HEIGHT);

    ctx.setFill(Color.WHITE);
    ctx.fillOval(ballPosX, ballPosY, BALL_RADIUS, BALL_RADIUS);

    ctx.setStroke(Color.WHITE);
    ctx.setLineWidth(2.5);
    ctx.strokeRect(0, 0, CANVAS_HEIGHT, CANVAS_HEIGHT);
  }

  public static void main(String[] args) {
    launch(args);
  }
}
