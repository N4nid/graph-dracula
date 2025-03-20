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
  RenderValues renderValues;
  GraphPixel graphPixel[][];
  ArrayList<EquationTree> equations = new ArrayList<EquationTree>();
  
  public TwoDVec<Double> lastPos = new TwoDVec<Double>(-1.0, -1.0);
  public TwoDVec<Double> lastZoom = new TwoDVec<Double>(-1.0, -1.0);
  ArrayList<TwoDVec<TwoDVec<Double>>> linePoints = new ArrayList<TwoDVec<TwoDVec<Double>>>();
  ArrayList<ArrayList<TwoDVec<TwoDVec<Double>>>> equationLineCache = new ArrayList<ArrayList<TwoDVec<TwoDVec<Double>>>>();

  public EquationRenderer(RenderValues renderValues) {
    this.renderValues = renderValues;
    graphPixel = new GraphPixel[renderValues.resolution.x * 2][renderValues.resolution.y * 2];
    for (int x = 0; x < graphPixel.length; x++) {
      for (int y = 0; y < graphPixel[0].length; y++) {
        graphPixel[x][y] = new GraphPixel();
    }
    }
  }

  public class MyThread extends Thread {
    boolean[][][] storage;
    int i;

    public MyThread(boolean[][][] storage, int index) {
      this.storage = storage;
      this.i = index;
    }

    public void run() {
      for (int x = 0; x < renderValues.resolution.x * 2; x++) {                
        for (int y = 0; y < renderValues.resolution.y * 2; y++) {
          storage[i][x][y] = (equations.get(i).calculate(renderValues.screenCoordToRealCoord(new TwoDVec<Integer>(x,y)),
          new Variable[0]) >= 0) ? true : false;
        }
      }
    }
  }


  private boolean[][][] getNegPosMaps(ArrayList<EquationTree> functions) {
    boolean[][][] negPosMaps = new boolean[functions.size()][renderValues.resolution.x * 2][renderValues.resolution.y * 2];
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

  private ArrayList<ArrayList<TwoDVec<TwoDVec<Double>>>> calculateNewEquations(boolean[][][] negPosMaps) {
    //zeichnet neue Funktionen aus negPosMaps in eine Pixelmap
    ArrayList<ArrayList<TwoDVec<TwoDVec<Double>>>> returnEquationLines = new ArrayList<ArrayList<TwoDVec<TwoDVec<Double>>>>();
    for (int i = 0; i < equations.size(); i++) {
      for (int x = 1; x < renderValues.resolution.x * 2 - 1; x++) {
        for (int y = 1; y < renderValues.resolution.y * 2 - 1; y++) {
          if (isPartOfFunction(x, y, negPosMaps, i)) {
            graphPixel[x][y].graph = true;
          } // end of if
        }
      }
      selectLinePoints();
      returnEquationLines.add(linePoints);
      linePoints.clear();
    }
    equationLineCache = returnEquationLines;
    return returnEquationLines;
  }
  
  public void selectLinePoints() {
    for (int x = 0; x < graphPixel.length; x++) {
      for (int y = 0; y < graphPixel[0].length; y++) {
        if (graphPixel[x][y].graph && !graphPixel[x][y].visited) {
          connectNeighbours(x,y);
        } // end of if
      }
    }
  }
  
  public void connectNeighbours(int x, int y) {   //checks the eight neighbouring pixels for if they're part of the graph
    if (graphPixel[x+1][y].graph && !graphPixel[x+1][y].visited) {
      TwoDVec<TwoDVec> line = new TwoDVec<TwoDVec>(new TwoDVec(x,y),new TwoDVec(x+1,y));
    } // end of if
    if (graphPixel[x+1][y+1].graph && !graphPixel[x+1][y+1].visited) {
      TwoDVec<TwoDVec> line = new TwoDVec<TwoDVec>(new TwoDVec(x,y),new TwoDVec(x+1,y));
    } // end of if
    if (graphPixel[x][y+1].graph && !graphPixel[x][y+1].visited) {
      TwoDVec<TwoDVec> line = new TwoDVec<TwoDVec>(new TwoDVec(x,y),new TwoDVec(x+1,y));
    } // end of if
    if (graphPixel[x-1][y+1].graph && !graphPixel[x-1][y+1].visited) {
      TwoDVec<TwoDVec> line = new TwoDVec<TwoDVec>(new TwoDVec(x,y),new TwoDVec(x+1,y));
    } // end of if
    if (graphPixel[x-1][y].graph && !graphPixel[x-1][y+1].visited) {
      TwoDVec<TwoDVec> line = new TwoDVec<TwoDVec>(new TwoDVec(x,y),new TwoDVec(x+1,y));
    } // end of if
    if (graphPixel[x-1][y-1].graph && !graphPixel[x-1][y-1].visited) {
      TwoDVec<TwoDVec> line = new TwoDVec<TwoDVec>(new TwoDVec(x,y),new TwoDVec(x+1,y));
    } // end of if
    if (graphPixel[x][y-1].graph && !graphPixel[x][y-1].visited) {
      TwoDVec<TwoDVec> line = new TwoDVec<TwoDVec>(new TwoDVec(x,y),new TwoDVec(x+1,y));
    } // end of if
    if (graphPixel[x+1][y-1].graph && !graphPixel[x+1][y-1].visited) {
      TwoDVec<TwoDVec> line = new TwoDVec<TwoDVec>(new TwoDVec(x,y),new TwoDVec(x+1,y));
    } // end of if
    graphPixel[x][y].visited = true;
  }

  public ArrayList<ArrayList<TwoDVec<TwoDVec<Double>>>> calculateLinePoints(ArrayList<EquationTree> equations) {
    if (lastZoom.x != renderValues.zoom.x) {
      this.equations = equations;
      this.lastZoom = new TwoDVec<Double>(renderValues.zoom.x, renderValues.zoom.y);
      this.lastPos = new TwoDVec<Double>(renderValues.midpoint.x, (double) renderValues.midpoint.y);
      return calculateNewEquations(getNegPosMaps(equations));
    }
    return equationLineCache;
  }

  public boolean isPartOfFunction(int x, int y, boolean[][][] negPosMap, int i) {   //returns true if function runs through pixel
    return ((negPosMap[i][x][y] == negPosMap[i][x + 1][y]) && (negPosMap[i][x][y - 1] == negPosMap[i][x + 1][y - 1]) && (negPosMap[i][x][y] == negPosMap[i][x][y - 1])) ? false : true;
  }
}