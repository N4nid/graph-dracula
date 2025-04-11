public class RenderValues {
  public TwoDVec<Integer> resolution;
  public TwoDVec<Double> zoom;
  public TwoDVec<Double> midpoint;
  public RenderValues(TwoDVec<Integer> resolution,TwoDVec<Double> zoom,TwoDVec<Double> midpoint) {
    this.resolution = resolution;
    this.zoom = zoom;
    this.midpoint = midpoint;
  }
  
  public TwoDVec<Double> realCoordToScreenCoord(TwoDVec<Double> realCoord) {
    TwoDVec<Double> screenCoord = new TwoDVec<Double>(realCoord.x / zoom.x +  midpoint.x, (realCoord.y) / -zoom.y + midpoint.y);
    return screenCoord;
  }

  public TwoDVec<Double> screenCoordToRealCoord(TwoDVec<Integer> screenCoord) {
    TwoDVec<Double> realCoord = new TwoDVec<Double>((screenCoord.x -midpoint.x)* zoom.x, (-screenCoord.y+ midpoint.y) * zoom.y);
    return realCoord;
  }

  public TwoDVec<Double> screenCoordDoubleToRealCoord(TwoDVec<Double> screenCoord) {
    TwoDVec<Double> realCoord = new TwoDVec<Double>((screenCoord.x -midpoint.x)* zoom.x, (-screenCoord.y+ midpoint.y) * zoom.y);
    return realCoord;
  }
  
  public TwoDVec<Double> screenCoordDoubleToRealCoord(TwoDVec<Double> screenCoord, TwoDVec<Double> midpoint, TwoDVec<Double> zoom) {
    TwoDVec<Double> realCoord = new TwoDVec<Double>((screenCoord.x -midpoint.x)* zoom.x, (-screenCoord.y+ midpoint.y) * zoom.y);
    return realCoord;
  }
}