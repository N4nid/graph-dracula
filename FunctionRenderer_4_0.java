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
import java.util.ArrayList;
import javafx.geometry.Insets;
import javafx.scene.layout.*;
import javafx.scene.control.ScrollPane;

/**
 *
 * Beschreibung
 *
 * @version 1.0 vom 21.01.2025
 * @author 
 */

public class FunctionRenderer_4_0 extends Application {
  // Anfang Attribute
  Pane root = new Pane();
  private Button bRender = new Button();
  //Interactiveness
  int lastMouseX,lastMouseY;
  boolean ready;
  //GUI
  Color background = Color.rgb(40,40,40);
  Color axes = Color.WHITE;
  int breite = 1800;
  int hoehe = 900;
  int posX = 30;
  int posY = 80;
  Color render[][] = new Color[breite*2][hoehe*2];
  WritableImage image = new WritableImage(breite*2, hoehe*2);
  private ImageView screen = new ImageView();
  //The Math stuff
  double scaling = 0.01;
  int X0 = breite;     //x = 0 des Funktions-Koord.-systems in Abh. von render
  int Y0 = hoehe;      //y = 0 des Funktions-Koord.-systems in Abh. von render
  ArrayList<EquationTree> functions = new ArrayList<EquationTree>();
  private ScrollPane view = new ScrollPane();
  // Ende Attribute
  
  public void start(Stage primaryStage) { 
    root.setBackground(new Background(new BackgroundFill(Color.rgb(40,40,40), CornerRadii.EMPTY, Insets.EMPTY)));
    Scene scene = new Scene(root, 1139, 743);
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
    screen.setX(posX - breite/2);
    screen.setY(posY - hoehe/2);
    screen.setFitWidth(breite*2);
    screen.setFitHeight(hoehe*2);
    screen.setX(posX);
    screen.setY(posY);
    screen.setImage(image);
    root.getChildren().add(screen);   
    view.setLayoutX(posX);
    view.setLayoutY(posY);
    view.setPrefHeight(hoehe);
    view.setPrefWidth(breite);
    view.setContent(screen);
    view.pannableProperty().setValue(true);
    view.pannableProperty().setValue(true);
    view.hbarPolicyProperty().setValue(ScrollPane.ScrollBarPolicy.NEVER);
    view.vbarPolicyProperty().setValue(ScrollPane.ScrollBarPolicy.NEVER);
    view.setStyle("-fx-background-color:#CFA36B;-fx-border-color:#654321;-fx-border-width: 5px;");
    root.getChildren().add(view);
    
    initialiseImage();
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
  
  public class MyThread extends Thread {
    boolean[][][] storage;
    int i;
    public MyThread(boolean[][][] storage, int index) {
      this.storage = storage;
      this.i = index;
    }
    public void run() {
      for (int x = 0; x < breite*2; x++) {                //laeuft ueber das Pixelarray (breite*2/hoehe*2)
        for (int y = 0; y < hoehe*2; y++) {
          storage[i][x][y] = (functions.get(i).calculate((x-X0)*scaling, -(y-Y0)*scaling,new Variable[0]) >= 0) ? true : false;
        }
      }
    }
  }
  
  public void exampleFunctionInitialiser() {
    Main functionTestStation = new Main();
    
    functions.add(functionTestStation.buildTestEquation());
    functions.get(functions.size()-1).GraphColor = Color.LIGHTGRAY;
    
    functions.add(functionTestStation.buildTestFunction());
    functions.get(functions.size()-1).GraphColor = Color.RED;
    
    functions.add(functionTestStation.buildComplicatedTestFunction());
    functions.get(functions.size()-1).GraphColor = Color.LIME;
    
    functions.add(functionTestStation.buildTestKreis());
    functions.get(functions.size()-1).GraphColor = Color.BLUE;
    
    functions.add(functionTestStation.buildTestSinusFunction());
    functions.get(functions.size()-1).GraphColor = Color.GREEN;
    
    long startTime = System.nanoTime();
    
    boolean[][][] negPosMaps = new boolean[functions.size()][breite*2][hoehe*2];
    for (int i = 0; i < functions.size(); i++) {
      MyThread thread = new MyThread(negPosMaps,i);
      thread.start();
      if (i == functions.size()-1) {
        try {
          thread.join();
        } catch(Exception e) {
          System.out.println("Alarrrrm");
        }
      } // end of if
    }
    
    long endTime = System.nanoTime();
    
    long duration = (endTime - startTime)/1000000;  //divide by 1000000 to get milliseconds.
    System.out.println(""+duration);
    drawNewFunctions(negPosMaps);
  }
  
  public void initialiseImage() {
    for (int x = 0; x < breite*2; x++) {
      for (int y = 0; y < hoehe*2; y++) {
        Color color = (x==0||y==0||x==breite-1||y==hoehe-1||(x-X0)*scaling == 0||-(y-Y0)*scaling == 0) ? axes : background;
        image.getPixelWriter().setColor(x,y,color);
      }
    }
  }
  
  public void drawNewFunctions(boolean[][][] negPosMaps){
    //zeichnet neue Funktionen aus negPosMaps in render & auf's image
    for(int x=0; x < breite*2; x++) {
      for(int y=0; y < hoehe*2; y++) {
        Color color = background;
        if (x==0||y==0||x==breite*2-1||y==hoehe*2-1||(x-X0)*scaling == 0 || -(y-Y0)*scaling == 0) {
          color = axes;
        } else {
          for (int i = 0; i < functions.size(); i++) {
            if (isPartOfFunction(x,y,negPosMaps,i)) {
              color = functions.get(i).GraphColor;
            } // end of if
          }
        } // end of if-else
        //color = (x==0||y==0||x==breite-1||y==hoehe-1) ? Color.BLACK : ((negPosMaps.get(0)[x][y]) ? Color.RED : Color.ORANGE);
        render[x][y] = color;
        image.getPixelWriter().setColor(x,y,color);
      }
    } 
  }
  
  public boolean isPartOfFunction(int x,int y,boolean[][][] negPosMap, int i) {
    return ((negPosMap[i][x][y]==negPosMap[i][x+1][y])&&(negPosMap[i][x][y-1]==negPosMap[i][x+1][y-1])&&(negPosMap[i][x][y]==negPosMap[i][x][y-1])) ? false : true;
  }
  
  public void bRender_Action(Event evt) {
    // TODO hier Quelltext einfügen
    exampleFunctionInitialiser();
  } // end of bRender_Action

  // Ende Methoden
} // end of class FunctionRenderer
