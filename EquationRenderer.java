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
  Color render[][] = new Color[breite*2][hoehe*2];
  public WritableImage image = new WritableImage(breite*2, hoehe*2);
  private RenderImage renderImage;
  double scaling;
  int X0 = breite;     //x = 0 des Funktions-Koord.-systems in Abh. von render
  int Y0 = hoehe;      //y = 0 des Funktions-Koord.-systems in Abh. von render
  ArrayList<EquationTree> functions = new ArrayList<EquationTree>();
  private ArrayList<Image> frameBuffer = new ArrayList<Image>();

  public TwoDVec<Double> lastPos = new TwoDVec<Double>(-1.0,-1.0);
  public TwoDVec<Double> lastZoom = new TwoDVec<Double>(-1.0,-1.0);

  public EquationRenderer(int breite, int hoehe, double scaling) {
    this.breite = breite;
    this.hoehe = hoehe;
    this.scaling = scaling;
    render = new Color[breite*2][hoehe*2];
    image = new WritableImage(breite * 2, hoehe * 2);
    X0 = breite;     //x = 0 des Funktions-Koord.-systems in Abh. von render
    Y0 = hoehe;
    frameBuffer.add(new WritableImage(breite * 2, hoehe * 2));
    frameBuffer.add(new WritableImage(breite * 2, hoehe * 2));
  }

  
  public class LayerRenderThread extends Thread {
    boolean[][][] storage;
    int i;
    public LayerRenderThread(boolean[][][] storage, int index) {
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

  public class UpdateRenderThread extends Thread {
    ArrayList<Image> frameBuffer;
    ApplicationController controller;
    public UpdateRenderThread(ArrayList<Image> frameBuffer, ApplicationController controller) {
      this.frameBuffer = frameBuffer;
      this.controller = controller;
    }
    public void run() {

    }
  }


  private boolean[][][] getNegPosMaps(ArrayList<EquationTree> functions){
    boolean[][][] negPosMaps = new boolean[functions.size()][breite*2][hoehe*2];
    for (int i = 0; i < functions.size(); i++) {
    LayerRenderThread thread = new LayerRenderThread(negPosMaps,i);
    thread.start();
    if (i == functions.size()-1) {
      try {
        thread.join();
      } catch(Exception e) {
        System.out.println("Alarrrrm");
      }
    } // end of if
  }
    return negPosMaps;
  }

  public void initialiseImage() {
    for (int x = 0; x < breite*2; x++) {
      for (int y = 0; y < hoehe*2; y++) {
        Color color = Color.web("#242424");
        image.getPixelWriter().setColor(x,y,color);
      }
    }
  }
  
  private Image drawNewFunctions(boolean[][][] negPosMaps){
    //zeichnet neue Funktionen aus negPosMaps in render & auf's image
    for(int x=1; x < breite*2 - 1; x++) {
      for(int y=1; y < hoehe*2 - 1; y++) {
          for (int i = 0; i < functions.size(); i++) {
            if (isPartOfFunction(x,y,negPosMaps,i)) {
              image.getPixelWriter().setColor(x,y,functions.get(i).graphColor);
            } // end of if
          }
      }
    }
    return image;
  }

  public RenderImage getRenderedEquations(ArrayList<EquationTree> equations, TwoDVec<Double> zoom, TwoDVec<Double> midpoint,TwoDVec<Double> frameSize) {
    if (lastZoom.x == -1 || renderImage == null) {
      scaling = zoom.x;
      X0 = (int)(frameSize.x + midpoint.x);
      Y0 = (int)(frameSize.y + midpoint.y);
      renderImage = new RenderImage(renderEquations(equations),new TwoDVec<Double> (midpoint.x, midpoint.y),new TwoDVec<Double>(scaling,scaling));
      return renderImage;
    }
    else {
      System.out.println(midpoint.y-lastPos.y + frameSize.y);
      return renderImage;
    }
  }

  private Image renderEquations(ArrayList<EquationTree> equations) {
      initialiseImage();
      this.functions = equations;
      this.lastZoom = new TwoDVec<Double>(scaling,scaling);
      this.lastPos = new TwoDVec<Double>((double)X0,(double)Y0);
      return drawNewFunctions(getNegPosMaps(equations));
  }
  
  public boolean isPartOfFunction(int x,int y,boolean[][][] negPosMap, int i) {
    return ((negPosMap[i][x][y]==negPosMap[i][x+1][y])&&(negPosMap[i][x][y-1]==negPosMap[i][x+1][y-1])&&(negPosMap[i][x][y]==negPosMap[i][x][y-1])) ? false : true;
  }


  // Ende Methoden
} // end of class FunctionRenderer
