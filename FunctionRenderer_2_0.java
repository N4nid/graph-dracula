import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import javafx.scene.control.*;
import javafx.event.*;
import javafx.scene.image.*;
import javafx.scene.paint.Color;
import javafx.animation.AnimationTimer;
import java.util.concurrent.TimeUnit;
import javafx.scene.input.*;
import javafx.util.*;

/**
 *
 * Beschreibung
 *
 * @version 1.0 vom 21.01.2025
 * @author 
 */

public class FunctionRenderer_2_0 extends Application {
  // Anfang Attribute
  Pane root = new Pane();
  Scene scene = new Scene(root, 656, 685);
  private Button bRender = new Button();
  AnimationTimer timer;
  boolean alreadyPushed = true;
  int breite = 600;
  int hoehe = 600;
  int posX = 30;
  int posY = 80;
  Pixel[][] render = new Pixel[breite][hoehe];
  WritableImage image = new WritableImage(breite, hoehe);
  private ImageView screen = new ImageView();
  // Ende Attribute
  
  public void start(Stage primaryStage) { 
    // Anfang Komponenten
    
    bRender.setLayoutX(27);
    bRender.setLayoutY(17);
    bRender.setPrefHeight(25);
    bRender.setPrefWidth(75);
    bRender.setOnAction(
    (event) -> {bRender_Action(event);} 
    );
    bRender.setText("render");
    root.getChildren().add(bRender);
    screen.setX(27);
    screen.setY(60);
    screen.setFitWidth(breite);
    screen.setFitHeight(hoehe);
    root.getChildren().add(screen);    
    
    initialiseRender();
    drawImage(image);
    screen.setOnMouseDragged(
    (event) -> {screen_MouseDragged(event);} 
    );
    // Ende Komponenten
    
    primaryStage.setOnCloseRequest(e -> System.exit(0));
    primaryStage.setTitle("function_renderer");
    primaryStage.setScene(scene);
    primaryStage.show();
  } // end of public FunctionRenderer
  
  // Anfang Methoden
  
  public static void main(String[] args) {
    launch(args);
  } // end of main
  
  public void initialiseRender() {
    for (int x = 0; x < breite; x++) {
      for (int y = 0; y < hoehe; y++) {
        render[x][y] = new Pixel((1.0/20.0),x,y,breite/2,hoehe/2);
        Color color = (x==0||y==0||x==breite-1||y==hoehe-1) ? Color.BLACK : ((render[x][y].y == 0 || render[x][y].x == 0) ? Color.BLACK : Color.WHITE);
        image.getPixelWriter().setColor(x,y,color);
      }
    }
  }
  
  public void drawImage(Image image){
    screen.setX(posX);
    screen.setY(posY);
    screen.setImage(image);
  }
  
  public void writeImage (Pixel[][] pixelarray){
    //schreibt Pixelarray in Image
    Color color;
    for(int x=0; x < breite; x++) {
      for(int y=0; y < hoehe; y++) {
        //Koordinatenachsen:
        color = (x==0||y==0||x==breite-1||y==hoehe-1) ? Color.BLACK : ((render[x][y].y == 0 || render[x][y].x == 0) ? Color.BLACK : Color.WHITE);
        color = (x==0||y==0||x==breite-1||y==hoehe-1) ? Color.BLACK : ((isPartOfFunction(x,y)) ? Color.BLACK : color);
        //color = (x==0||y==0||x==breite-1||y==hoehe-1) ? Color.BLACK : ((render[x][y].sign) ? Color.RED : Color.ORANGE);
        image.getPixelWriter().setColor(x,y,color);
      }
    } 
  }
  
  public double testFunction(double x, double y) {
    //return Math.pow(Math.E,x)-y;
    //return Math.pow(x,2)-y;
    //return Math.pow(x,3)-y;
    return Math.pow(x,2)+Math.pow(y,2)-Math.pow(6,2);
  }
  
  public void traceFunction() {
    for (int x = 0; x < breite; x++) {
      for (int y = 0; y < hoehe; y++) {
        render[x][y].sign = (testFunction(render[x][y].x,render[x][y].y) >= 0) ? true : false;
      }
    }
  }
  
  public boolean isPartOfFunction(int x,int y) {
    return ((render[x][y].sign==render[x+1][y].sign)&&(render[x][y-1].sign==render[x+1][y-1].sign)&&(render[x][y].sign==render[x][y-1].sign)) ? false : true;
  }
  
  public void bRender_Action(Event evt) {
    // TODO hier Quelltext einfügen
    if (alreadyPushed) {
      timer = new AnimationTimer() {
        @Override
        public void handle(long now) {
          traceFunction();
          writeImage(render);
        }
      };
      timer.start();
      System.out.println("start");
      alreadyPushed = false;
    } else {
      timer.stop();
      System.out.println("stop");
      alreadyPushed = true;
    } // end of if-else
  } // end of bRender_Action

  public void screen_MouseDragged(MouseEvent evt) {
    // TODO hier Quelltext einfügen
    System.out.println("X is: " + (evt.getX()));
    System.out.println("Y is: " + (evt.getY()));
  } // end of imageView1_MouseDragged

  // Ende Methoden
} // end of class FunctionRenderer
