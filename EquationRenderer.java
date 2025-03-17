import javafx.scene.image.*;
import javafx.scene.paint.Color;

import java.util.ArrayList;

/**
 *
 * Beschreibung
 *
 * @version 1.0 vom 21.01.2025
 * @author 
 */

public class EquationRenderer {

  int breite = 1800;
  int hoehe = 900;
  Color render[][] = new Color[breite * 2][hoehe * 2];
  public WritableImage image = new WritableImage(breite * 2, hoehe * 2);
  double scaling;
  int X0 = breite;     //x = 0 des Funktions-Koord.-systems in Abh. von render
  int Y0 = hoehe;      //y = 0 des Funktions-Koord.-systems in Abh. von render
  ArrayList<EquationTree> functions = new ArrayList<EquationTree>();
  public TwoDVec<Double> lastPos = new TwoDVec<Double>(-1.0, -1.0);
  public TwoDVec<Double> lastZoom = new TwoDVec<Double>(-1.0, -1.0);
  public boolean doImageWriting = true;

  public EquationRenderer(int breite, int hoehe, double scaling) {
    this.breite = breite;
    this.hoehe = hoehe;
    this.scaling = scaling;
    render = new Color[breite * 2][hoehe * 2];
    image = new WritableImage(breite * 2, hoehe * 2);
    X0 = breite;     //x = 0 des Funktions-Koord.-systems in Abh. von render
    Y0 = hoehe;
  }


  public class MyThread extends Thread {
    boolean[][][] storage;
    int i;

    public MyThread(boolean[][][] storage, int index) {
      this.storage = storage;
      this.i = index;
    }

    public void run() {
      for (int x = 0; x < breite * 2; x++) {                //laeuft ueber das Pixelarray (breite*2/hoehe*2)
        for (int y = 0; y < hoehe * 2; y++) {
          storage[i][x][y] = (functions.get(i).calculate((x - X0) * scaling, -(y - Y0) * scaling, new Variable[0]) >= 0) ? true : false;
        }
      }
    }
  }


  private boolean[][][] getNegPosMaps(ArrayList<EquationTree> functions) {
    boolean[][][] negPosMaps = new boolean[functions.size()][breite * 2][hoehe * 2];
    for (int i = 0; i < functions.size(); i++) {
      MyThread thread = new MyThread(negPosMaps, i);
      thread.start();
      if (i == functions.size() - 1) {
        try {
          thread.join();
        } catch (Exception e) {
          System.out.println("Alarrrrm");
        }
      } // end of if
    }
    return negPosMaps;
  }

  public void initialiseImage() {
    for (int x = 0; x < breite * 2; x++) {
      for (int y = 0; y < hoehe * 2; y++) {
        Color color = Color.web("#242424");
        image.getPixelWriter().setColor(x, y, color);
      }
    }
  }

  private Image drawNewFunctions(boolean[][][] negPosMaps) {
    //zeichnet neue Funktionen aus negPosMaps in render & auf's image
    for (int x = 1; x < breite * 2 - 1; x++) {
      for (int y = 1; y < hoehe * 2 - 1; y++) {
        for (int i = 0; i < functions.size(); i++) {
          if (isPartOfFunction(x, y, negPosMaps, i)) {
            if (doImageWriting) {
              image.getPixelWriter().setColor(x, y, functions.get(i).graphColor);
            }
          } // end of if
        }
      }
    }
    return image;
  }

  public Image drawEquations(ArrayList<EquationTree> equations) {
    long startTime = System.nanoTime();
    if (lastZoom.x != scaling) {
      if (doImageWriting) {
        initialiseImage();
      }
      this.functions = equations;
      this.lastZoom = new TwoDVec<Double>(scaling, scaling);
      this.lastPos = new TwoDVec<Double>((double) X0, (double) Y0);
      long endTime = System.nanoTime();
      System.out.println("RenderTIme: " + (endTime - startTime));
      return drawNewFunctions(getNegPosMaps(equations));
    } else {
      return image;
    }
  }

  public boolean isPartOfFunction(int x, int y, boolean[][][] negPosMap, int i) {
    return ((negPosMap[i][x][y] == negPosMap[i][x + 1][y]) && (negPosMap[i][x][y - 1] == negPosMap[i][x + 1][y - 1]) && (negPosMap[i][x][y] == negPosMap[i][x][y - 1])) ? false : true;
  }
}