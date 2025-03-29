import javafx.scene.*;
import java.util.ArrayList;

public class EquationRenderer {
  RenderValues renderValues;
  GraphPixel graphPixel[][];
  ArrayList<EquationTree> equations = new ArrayList<EquationTree>();
  
  public TwoDVec<Double> lastZoom;
  public TwoDVec<Double> lastPos;
  public TwoDVec<Double> lastMove;
  ArrayList<ArrayList<TwoDVec<TwoDVec<Double>>>> equationLineCache = new ArrayList<ArrayList<TwoDVec<TwoDVec<Double>>>>();
  
  public EquationRenderer(RenderValues renderValues) {
    this.renderValues = renderValues;
    this.lastZoom = new TwoDVec<Double>(renderValues.zoom.x, renderValues.zoom.y);
    this.lastPos = new TwoDVec<Double>(renderValues.midpoint.x, renderValues.midpoint.y);
    this.lastMove = new TwoDVec<Double>(renderValues.midpoint.x, renderValues.midpoint.y);
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
          //storage[i][x][y] = (equations.get(i).calculate(renderValues.screenCoordToRealCoord(new TwoDVec<Integer>(x,y)),
          //new Variable[0]) >= 0) ? true : false;
          storage[i][x][y] = (equations.get(i).calculate(new TwoDVec<Double>((x-2*renderValues.midpoint.x)*renderValues.zoom.x,
          (-y+2*renderValues.midpoint.y)*renderValues.zoom.y),new Variable[0]) >= 0) ? true : false;
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


  private ArrayList<ArrayList<TwoDVec<TwoDVec<Double>>>> calculateNewEquations(boolean[][][] negPosMaps, boolean overrideCache) {
    //zeichnet neue Funktionen aus negPosMaps in eine Pixelmap
    ArrayList<ArrayList<TwoDVec<TwoDVec<Double>>>> returnEquationLines = new ArrayList<ArrayList<TwoDVec<TwoDVec<Double>>>>();
    for (int i = 0; i < equations.size(); i++) {
      ArrayList<TwoDVec<TwoDVec<Double>>> linePoints = new ArrayList<TwoDVec<TwoDVec<Double>>>();
      for (int x = 1; x < renderValues.resolution.x * 2 - 1; x++) {
        for (int y = 1; y < renderValues.resolution.y * 2 - 1; y++) {
          if (isPartOfFunction(x, y, negPosMaps, i)) {
            graphPixel[x][y].graph = true;
          } // end of if
        }
      }
      selectLinePoints(linePoints);
      returnEquationLines.add(linePoints);
    }
    if (overrideCache) {
      equationLineCache = returnEquationLines;
    }
    return returnEquationLines;
  }
  
  public void selectLinePoints(ArrayList<TwoDVec<TwoDVec<Double>>> linePoints) {
    for (int x = 0; x < graphPixel.length; x++) {
      for (int y = 0; y < graphPixel[0].length; y++) {
        if (graphPixel[x][y].graph && !graphPixel[x][y].visited) {
          connectNeighbours(x,y,linePoints);
        } // end of if
      }
    }
  }
  
  public void connectNeighbours(int x, int y, ArrayList<TwoDVec<TwoDVec<Double>>> linePoints) {   //checks the eight neighbouring pixels for if they're part of the graph
    if (graphPixel[x+1][y+1].graph && !graphPixel[x+1][y+1].visited) {
      TwoDVec<TwoDVec<Double>> line = new TwoDVec<TwoDVec<Double>>(new TwoDVec<Double>((double)x-renderValues.midpoint.x,(double)y-renderValues.midpoint.y),new TwoDVec<Double>((double)x+1-renderValues.midpoint.x,(double)y+1-renderValues.midpoint.y));
      graphPixel[x+1][y].visited = true;
      graphPixel[x][y+1].visited = true;
      linePoints.add(line);
    } // end of if
    if (graphPixel[x-1][y+1].graph && !graphPixel[x-1][y+1].visited) {
      TwoDVec<TwoDVec<Double>> line = new TwoDVec<TwoDVec<Double>>(new TwoDVec<Double>((double)x-renderValues.midpoint.x,(double)y-renderValues.midpoint.y),new TwoDVec<Double>((double)x-1-renderValues.midpoint.x,(double)y+1-renderValues.midpoint.y));
      graphPixel[x][y+1].visited = true;
      graphPixel[x-1][y].visited = true;
      linePoints.add(line);
    } // end of if
    if (graphPixel[x-1][y-1].graph && !graphPixel[x-1][y-1].visited) {
      TwoDVec<TwoDVec<Double>> line = new TwoDVec<TwoDVec<Double>>(new TwoDVec<Double>((double)x-renderValues.midpoint.x,(double)y-renderValues.midpoint.y),new TwoDVec<Double>((double)x-1-renderValues.midpoint.x,(double)y-1-renderValues.midpoint.y));
      graphPixel[x-1][y].visited = true;
      graphPixel[x][y-1].visited = true;
      linePoints.add(line);
    } // end of if
    if (graphPixel[x+1][y-1].graph && !graphPixel[x+1][y-1].visited) {
      TwoDVec<TwoDVec<Double>> line = new TwoDVec<TwoDVec<Double>>(new TwoDVec<Double>((double)x-renderValues.midpoint.x,(double)y-renderValues.midpoint.y),new TwoDVec<Double>((double)x+1-renderValues.midpoint.x,(double)y-1-renderValues.midpoint.y));
      graphPixel[x+1][y].visited = true;
      graphPixel[x][y-1].visited = true;
      linePoints.add(line);
    } // end of if
    if (graphPixel[x+1][y].graph && !graphPixel[x+1][y].visited) {
      TwoDVec<TwoDVec<Double>> line = new TwoDVec<TwoDVec<Double>>(new TwoDVec<Double>((double)x-renderValues.midpoint.x,(double)y-renderValues.midpoint.y),new TwoDVec<Double>((double)x+1-renderValues.midpoint.x,(double)y-renderValues.midpoint.y));
      linePoints.add(line);
    } // end of if
    if (graphPixel[x][y+1].graph && !graphPixel[x][y+1].visited) {
      TwoDVec<TwoDVec<Double>> line = new TwoDVec<TwoDVec<Double>>(new TwoDVec<Double>((double)x-renderValues.midpoint.x,(double)y-renderValues.midpoint.y),new TwoDVec<Double>((double)x-renderValues.midpoint.x,(double)y+1-renderValues.midpoint.y));
      linePoints.add(line);
    } // end of if
    if (graphPixel[x-1][y].graph && !graphPixel[x-1][y].visited) {
      TwoDVec<TwoDVec<Double>> line = new TwoDVec<TwoDVec<Double>>(new TwoDVec<Double>((double)x-renderValues.midpoint.x,(double)y-renderValues.midpoint.y),new TwoDVec<Double>((double)x-1-renderValues.midpoint.x,(double)y-renderValues.midpoint.y));
      linePoints.add(line);
    } // end of if
    if (graphPixel[x][y-1].graph && !graphPixel[x][y-1].visited) {
      TwoDVec<TwoDVec<Double>> line = new TwoDVec<TwoDVec<Double>>(new TwoDVec<Double>((double)x-renderValues.midpoint.x,(double)y-renderValues.midpoint.y),new TwoDVec<Double>((double)x-renderValues.midpoint.x,(double)y-1-renderValues.midpoint.y));
      linePoints.add(line);
    } // end of if
    graphPixel[x][y].visited = true;
  }
  
  public boolean isPartOfFunction(int x, int y, boolean[][][] negPosMap, int i) {   //returns true if function runs through pixel
    return ((negPosMap[i][x][y] == negPosMap[i][x + 1][y]) && (negPosMap[i][x][y - 1] == negPosMap[i][x + 1][y - 1]) && (negPosMap[i][x][y] == negPosMap[i][x][y - 1])) ? false : true;
  }
  
  public void moveGraphs(double DeltaX, double DeltaY) {
    for (int i = 0; i < equationLineCache.size(); i++) {
      for (int j = 0; j < equationLineCache.get(i).size(); j++) {
        equationLineCache.get(i).get(j).x.x += DeltaX;
        equationLineCache.get(i).get(j).x.y += DeltaY;
        equationLineCache.get(i).get(j).y.x += DeltaX;
        equationLineCache.get(i).get(j).y.y += DeltaY;
      }
    }
  }

  public ArrayList<ArrayList<TwoDVec<TwoDVec<Double>>>> calculateEquationsLinePoints(ArrayList<EquationTree> equations) {
    if (lastZoom.x != renderValues.zoom.x || lastZoom.y != renderValues.zoom.y) {
      this.equations = equations;                                                      //Vergroessern/ Verkleinern
      this.lastZoom = new TwoDVec<Double>(renderValues.zoom.x, renderValues.zoom.y);
      this.lastPos = new TwoDVec<Double>(renderValues.midpoint.x, renderValues.midpoint.y);
      return calculateNewEquations(getNegPosMaps(equations),true);
    }
    if(lastPos.x != renderValues.midpoint.x || lastPos.y != renderValues.midpoint.y) {
      if ((Math.abs(((double)renderValues.midpoint.x-(double)lastMove.x)) < (double)renderValues.resolution.x/2) && (Math.abs(((double)renderValues.midpoint.y-(double)lastMove.y)) < (double)renderValues.resolution.y/2)) {
        double DeltaX = (renderValues.midpoint.x - lastPos.x);                          //Verschieben
        double DeltaY = (renderValues.midpoint.y - lastPos.y);
        this.lastZoom = new TwoDVec<Double>(renderValues.zoom.x, renderValues.zoom.y);
        this.lastPos = new TwoDVec<Double>(renderValues.midpoint.x, renderValues.midpoint.y);
        moveGraphs(DeltaX,DeltaY);
        return equationLineCache;
      } else {
        this.equations = equations;                                                     //Verschieben und neu rendern
        this.lastZoom = new TwoDVec<Double>(renderValues.zoom.x, renderValues.zoom.y);
        this.lastPos = new TwoDVec<Double>(renderValues.midpoint.x, renderValues.midpoint.y);
        this.lastMove = new TwoDVec<Double>(renderValues.midpoint.x, renderValues.midpoint.y);
        return calculateNewEquations(getNegPosMaps(equations),true);
      } // end of if-else
    }
    if (equations.size() == 1) {
      this.equations = equations;                                                      //Preview Equation oder neue Equation
      return calculateNewEquations(getNegPosMaps(equations),true);
    }
    return equationLineCache;           //Farbwechsel (sollte im Renderer geregelt werden)
  }
  
  public ArrayList<TwoDVec<TwoDVec<Double>>> calculateEquationLinePoints(EquationTree equation) {
    return null;
  }
}