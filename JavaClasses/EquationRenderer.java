import javafx.scene.*;
import java.util.ArrayList;
import javafx.concurrent.Task;
import javafx.application.Platform;

public class EquationRenderer {
  RenderValues renderValues;
  ArrayList<EquationTree> equations = new ArrayList<EquationTree>();
  ArrayList<Thread> threads = new ArrayList<Thread>();
  ArrayList<CalcInBackground> tasks = new ArrayList<CalcInBackground>();
  int counter = 0;
  Renderer renderer;
  
  private TwoDVec<Double> lastZoom;
  private TwoDVec<Double> lastPos;
  private TwoDVec<Double> lastMove;
  ArrayList<ArrayList<TwoDVec<TwoDVec<Double>>>> equationLineCache = new ArrayList<ArrayList<TwoDVec<TwoDVec<Double>>>>();
  
  Variable[] customVars;
  EquationTree[] existingFunctions;
  
  public EquationRenderer(RenderValues renderValues, Renderer renderer) {
    this.renderValues = renderValues;
    this.renderer = renderer;
    this.lastZoom = new TwoDVec<Double>((double)renderValues.zoom.x, (double)renderValues.zoom.y);
    this.lastPos = new TwoDVec<Double>((double)renderValues.midpoint.x, (double)renderValues.midpoint.y);
    this.lastMove = new TwoDVec<Double>((double)renderValues.midpoint.x, (double)renderValues.midpoint.y);
  }
  
  public class CalcInBackground extends Task<Void> {
    int i;
    TwoDVec<Double> zoom;
    TwoDVec<Double> midpoint;
    TwoDVec<Double> res;
    boolean[][] negPosMap;
    GraphPixel graphPixel[][];
    boolean newEquation;
    
    public CalcInBackground(int index, boolean newEquation) {
      this.i = index;
      this.negPosMap = new boolean[renderValues.resolution.x*2][renderValues.resolution.y*2];
      this.newEquation = newEquation;
      zoom = new TwoDVec<Double>((double)renderValues.zoom.x, (double)renderValues.zoom.y);
      midpoint = new TwoDVec<Double>((double)renderValues.midpoint.x, (double)renderValues.midpoint.y);
      res = new TwoDVec<Double>((double)renderValues.resolution.x, (double)renderValues.resolution.y);
      graphPixel = new GraphPixel[renderValues.resolution.x * 2][renderValues.resolution.y * 2];
      for (int x = 0; x < graphPixel.length; x++) {
        for (int y = 0; y < graphPixel[0].length; y++) {
          graphPixel[x][y] = new GraphPixel();
        }
      }          
    }
    
    protected Void call() throws Exception {
      //NegPosMap berechnen
      for (int x = 0; x < res.x * 2; x++) {                
        for (int y = 0; y < res.y * 2; y++) {
          if (isCancelled()) {
            System.out.println("...");
            return null;
          } 
          negPosMap[x][y] = (equations.get(i).calculate(new TwoDVec<Double>((x-2*midpoint.x)*zoom.x,
          (-y+2*midpoint.y)*zoom.y),customVars,existingFunctions) >= 0) ? true : false;
        }
      }
      //LinePoints berechnen
      ArrayList<TwoDVec<TwoDVec<Double>>> linePoints = new ArrayList<TwoDVec<TwoDVec<Double>>>();
      for (int x = 1; x < res.x * 2 - 1; x++) {
        for (int y = 1; y < res.y * 2 - 1; y++) {
          if (isCancelled()) {
            System.out.println("...");
            return null;
          }
          if (isPartOfFunction(x, y, negPosMap)) {
            graphPixel[x][y].graph = true;                                                       
          } // end of if
        }                                                                       
      }
      //select line points
      for (int x = 0; x < graphPixel.length; x++) {
        for (int y = 0; y < graphPixel[0].length; y++) {
          if (isCancelled()) {
            System.out.println("...");
            return null;
          }
          if (graphPixel[x][y].graph && !graphPixel[x][y].visited) {
            connectNeighbours(x,y,linePoints, graphPixel, midpoint);
          } // end of if
        }
      }
      if (isCancelled()) {
        System.out.println("...");
        return null;
      }
      if (newEquation) {
        equationLineCache.add(linePoints);
      } else {                                      
        equationLineCache.set(i,linePoints);
      } // end of if-else
      moveGraphs(((double)renderValues.midpoint.x - (double)midpoint.x),((double)renderValues.midpoint.y - (double)midpoint.y));
      //Rueckmeldung, dass Thread fertig ist                                                     
      Platform.runLater(new Runnable() {
        @Override 
        public void run() {
          renderer.rerender();
        }
      });
      return null;
    }
  }
  
  private void calculateNewEquations(ArrayList<EquationTree> equations) {
    for (int i = 0; i < equations.size(); i++) {
      if (equations.size() > equationLineCache.size() && i >= equationLineCache.size()) {
        tasks.add(new CalcInBackground(i, true));
        threads.add(new Thread(tasks.get(tasks.size()-1)));
        threads.get(tasks.size()-1).setDaemon(true);
        threads.get(threads.size()-1).start();
      } else {
        tasks.get(i).cancel();
        tasks.set(i,new CalcInBackground(i, false));
        threads.set(i,new Thread(tasks.get(i)));
        threads.get(i).setDaemon(true);  
        threads.get(i).start();
      } // end of if-else                                                    
    }
  }          
  
  public void connectNeighbours(int x, int y, ArrayList<TwoDVec<TwoDVec<Double>>> linePoints, GraphPixel[][] graphPixel, TwoDVec<Double> midpoint) {   //checks the eight neighbouring pixels for if they're part of the graph
    if (graphPixel[x+1][y+1].graph && !graphPixel[x+1][y+1].visited) {
      TwoDVec<TwoDVec<Double>> line = new TwoDVec<TwoDVec<Double>>(new TwoDVec<Double>((double)x-midpoint.x,(double)y-midpoint.y),new TwoDVec<Double>((double)x+1-midpoint.x,(double)y+1-midpoint.y));
      graphPixel[x+1][y].visited = true;
      graphPixel[x][y+1].visited = true;
      linePoints.add(line);                                
    } // end of if
    if (graphPixel[x-1][y+1].graph && !graphPixel[x-1][y+1].visited) {
      TwoDVec<TwoDVec<Double>> line = new TwoDVec<TwoDVec<Double>>(new TwoDVec<Double>((double)x-midpoint.x,(double)y-midpoint.y),new TwoDVec<Double>((double)x-1-midpoint.x,(double)y+1-midpoint.y));
      graphPixel[x][y+1].visited = true;
      graphPixel[x-1][y].visited = true;
      linePoints.add(line);
    } // end of if
    if (graphPixel[x-1][y-1].graph && !graphPixel[x-1][y-1].visited) {
      TwoDVec<TwoDVec<Double>> line = new TwoDVec<TwoDVec<Double>>(new TwoDVec<Double>((double)x-midpoint.x,(double)y-midpoint.y),new TwoDVec<Double>((double)x-1-midpoint.x,(double)y-1-midpoint.y));
      graphPixel[x-1][y].visited = true;
      graphPixel[x][y-1].visited = true;
      linePoints.add(line);
    } // end of if
    if (graphPixel[x+1][y-1].graph && !graphPixel[x+1][y-1].visited) {
      TwoDVec<TwoDVec<Double>> line = new TwoDVec<TwoDVec<Double>>(new TwoDVec<Double>((double)x-midpoint.x,(double)y-midpoint.y),new TwoDVec<Double>((double)x+1-midpoint.x,(double)y-1-midpoint.y));
      graphPixel[x+1][y].visited = true;
      graphPixel[x][y-1].visited = true;
      linePoints.add(line);
    } // end of if
    if (graphPixel[x+1][y].graph && !graphPixel[x+1][y].visited) {
      TwoDVec<TwoDVec<Double>> line = new TwoDVec<TwoDVec<Double>>(new TwoDVec<Double>((double)x-midpoint.x,(double)y-midpoint.y),new TwoDVec<Double>((double)x+1-midpoint.x,(double)y-midpoint.y));
      linePoints.add(line);
    } // end of if
    if (graphPixel[x][y+1].graph && !graphPixel[x][y+1].visited) {
      TwoDVec<TwoDVec<Double>> line = new TwoDVec<TwoDVec<Double>>(new TwoDVec<Double>((double)x-midpoint.x,(double)y-midpoint.y),new TwoDVec<Double>((double)x-midpoint.x,(double)y+1-midpoint.y));
      linePoints.add(line);
    } // end of if
    if (graphPixel[x-1][y].graph && !graphPixel[x-1][y].visited) {
      TwoDVec<TwoDVec<Double>> line = new TwoDVec<TwoDVec<Double>>(new TwoDVec<Double>((double)x-midpoint.x,(double)y-midpoint.y),new TwoDVec<Double>((double)x-1-midpoint.x,(double)y-midpoint.y));
      linePoints.add(line);
    } // end of if
    if (graphPixel[x][y-1].graph && !graphPixel[x][y-1].visited) {
      TwoDVec<TwoDVec<Double>> line = new TwoDVec<TwoDVec<Double>>(new TwoDVec<Double>((double)x-midpoint.x,(double)y-midpoint.y),new TwoDVec<Double>((double)x-midpoint.x,(double)y-1-midpoint.y));
      linePoints.add(line);
    } // end of if
    graphPixel[x][y].visited = true;
  }
    
  public boolean isPartOfFunction(int x, int y, boolean[][] negPosMap) {   //returns true if function runs through pixel
    return ((negPosMap[x][y] == negPosMap[x + 1][y]) && (negPosMap[x][y - 1] == negPosMap[x + 1][y - 1]) && (negPosMap[x][y] == negPosMap[x][y - 1])) ? false : true;
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
    
  public ArrayList<ArrayList<TwoDVec<TwoDVec<Double>>>> calculateEquationsLinePoints(ArrayList<EquationTree> equations,Variable[] existingVariables, EquationTree[] existingFunctions) {
    this.customVars = existingVariables;
    this.existingFunctions = existingFunctions;
    if ((double)lastZoom.x != (double)renderValues.zoom.x || (double)lastZoom.y != (double)renderValues.zoom.y) {
      this.equations = equations;                                                      //Vergroessern/ Verkleinern
      this.lastZoom = new TwoDVec<Double>((double)renderValues.zoom.x, (double)renderValues.zoom.y);
      this.lastPos = new TwoDVec<Double>((double)renderValues.midpoint.x, (double)renderValues.midpoint.y);
      calculateNewEquations(equations);
    } else if((double)lastPos.x != (double)renderValues.midpoint.x || (double)lastPos.y != (double)renderValues.midpoint.y) {
      if ((Math.abs(((double)renderValues.midpoint.x-(double)lastMove.x)) < (double)renderValues.resolution.x/2) && (Math.abs(((double)renderValues.midpoint.y-(double)lastMove.y)) < (double)renderValues.resolution.y/2)) {
        double DeltaX = (renderValues.midpoint.x - lastPos.x);                          //Verschieben
        double DeltaY = (renderValues.midpoint.y - lastPos.y);
        this.lastZoom = new TwoDVec<Double>((double)renderValues.zoom.x, (double)renderValues.zoom.y);
        this.lastPos = new TwoDVec<Double>((double)renderValues.midpoint.x, (double)renderValues.midpoint.y);
        moveGraphs(DeltaX,DeltaY);
      } else {
        this.equations = equations;                                                     //Verschieben und neu rendern
        this.lastZoom = new TwoDVec<Double>((double)renderValues.zoom.x, (double)renderValues.zoom.y);
        this.lastPos = new TwoDVec<Double>((double)renderValues.midpoint.x, (double)renderValues.midpoint.y);
        this.lastMove = new TwoDVec<Double>((double)renderValues.midpoint.x, (double)renderValues.midpoint.y);
        calculateNewEquations(equations);
      } // end of if-else
    } else {
      this.equations = equations;                                                       //Preview Equation oder neue Equation
      calculateNewEquations(equations);                                                    
    }
    return equationLineCache;
    //Farbwechsel sollte im Renderer geregelt werden!!!
  }
}