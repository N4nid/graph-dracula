public class TwoDVec<E> {
    public E x;
    public E y;
    public TwoDVec(E x, E y) {
        this.x = x;
        this.y = y;
    }

    public TwoDVec() {
    }

    public void setPos(E x, E y) {
        this.x = x;
        this.y = y;
    }
}
