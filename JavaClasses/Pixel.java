public class Pixel {
  boolean sign = true;     
  //true = auf oder ueber d. Funktion; false = unter d. Funktion
  boolean partOfFunction;  //false = Teil keiner Funktion, true = Funktionsnummerierung
  double x,y;
  //Koordinaten im gerenderten Koord.-system
  //Konstruktoren:
  public Pixel () {
  }
  public Pixel (double scaling, int realX, int realY, int X0, int Y0) {    //X0 und Y0 sind die Koordinaten des Ursprungs im Pixelarray
    this.x = (realX-X0)*scaling;
    this.y = -(realY-Y0)*scaling;
    //System.out.println("("+realX+"/ "+realY+") - ("+x+"/ "+y+")");
  }
}