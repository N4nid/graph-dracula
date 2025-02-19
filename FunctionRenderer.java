import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import javafx.scene.control.*;
import javafx.event.*;
import javafx.scene.image.*;
import javafx.scene.paint.Color;

/**
 *
 * Beschreibung
 *
 * @version 1.0 vom 21.01.2025
 * @author 
 */

public class FunctionRenderer extends Application {
  // Anfang Attribute
  private Button bRender = new Button();
  int breite = 500;
  int hoehe = 500;
  int posX = 30;
  int posY = 80;
  Pixel[][] render = new Pixel[breite][hoehe];
  WritableImage image = new WritableImage(breite, hoehe);
  private ImageView screen = new ImageView();
  // Ende Attribute
  
  public void start(Stage primaryStage) { 
    Pane root = new Pane();
    Scene scene = new Scene(root, 656, 685);
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
    screen.setFitWidth(596);
    screen.setFitHeight(596);
    root.getChildren().add(screen);    
    
    initialiseRender();
    drawImage(image);
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
    return Math.pow(x,2)+3*Math.pow(y,2)-Math.pow(6,2);
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
    traceFunction();
    writeImage(render);
  } // end of bRender_Action

  // Ende Methoden
} // end of class FunctionRenderer
