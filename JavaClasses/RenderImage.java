import javafx.scene.image.Image;

public class RenderImage {
  public Image image;
  public TwoDVec<Double> renderPos;
  public TwoDVec<Double> renderZoom;

  public RenderImage(Image image, TwoDVec<Double> renderPos, TwoDVec<Double> renderZoom) {
    this.image = image;
    this.renderPos = renderPos;
    this.renderZoom = renderZoom;
  }

  public RenderImage convertToDifferentCoord(TwoDVec<Double> midpoint, TwoDVec<Double> zoom, TwoDVec<Double> viewSize) {
    TwoDVec<Double> imagePos = new TwoDVec<Double>( midpoint.x - renderPos.x,midpoint.y - renderPos.y);
    TwoDVec<Double> zoomFacs = new TwoDVec<Double>(1/renderZoom.x,1/renderZoom.y);
    TwoDVec<Double> imageSize = new TwoDVec<Double>(image.getWidth() / (zoom.x * zoomFacs.x) ,image.getHeight() /(zoom.y * zoomFacs.y));
    TwoDVec<Double> imageOffset = new TwoDVec<Double>(-(imageSize.x / 2 - viewSize.x),-(imageSize.y / 2- viewSize.y));
    imagePos.setPos(imagePos.x + imageOffset.x,imagePos.y + imageOffset.y);
    return new RenderImage(image,imagePos,imageSize);
  }
}
