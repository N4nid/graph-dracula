import javafx.scene.Node;
import javafx.scene.layout.Region;

import java.util.ArrayList;

public class Anchor {
    private Region baseObject;
    private Region relateToObject;
    public TwoDVec<Double> offsetVec;
    private boolean keepX = false;
    private boolean keepY = false;
    private String type;
    private TwoDVec<Double> maxSize = new TwoDVec<Double>(10000.0,100000.0);

    public Anchor(Region baseObject, Region relateToObject, TwoDVec<Double> offsetVec, String type) {
        this.baseObject = baseObject;
        this.relateToObject = relateToObject;
        this.offsetVec = offsetVec;
        this.type = type;
    }

    public Anchor(Region baseObject, Region relateToObject, TwoDVec<Double> offsetVec, String type, boolean keepX, boolean keepY) {
        this.baseObject = baseObject;
        this.relateToObject = relateToObject;
        this.offsetVec = offsetVec;
        this.keepX = keepX;
        this.keepY = keepY;
        this.type = type;
    }

    public Anchor(Region baseObject, Region relateToObject, TwoDVec<Double> offsetVec, String type, boolean keepX, boolean keepY, TwoDVec<Double> maxSize) {
        this.baseObject = baseObject;
        this.relateToObject = relateToObject;
        this.offsetVec = offsetVec;
        this.keepX = keepX;
        this.keepY = keepY;
        this.type = type;
        this.maxSize = maxSize;
    }


    public void applyAnchor() {
        if (type.equals("scale")) {
            TwoDVec<Double> targetSize = new TwoDVec<Double>(relateToObject.getPrefWidth() + offsetVec.x,relateToObject.getPrefHeight() + offsetVec.y);
            if (!keepX) {
                if ((targetSize.x <= maxSize.x)) {
                    baseObject.setPrefWidth(targetSize.x);
                } else if(baseObject.getPrefWidth() < maxSize.x) {
                    baseObject.setPrefWidth(maxSize.x);
                }
            }
            if (!keepY) {
                if (targetSize.y < maxSize.y) {
                    baseObject.setPrefHeight(targetSize.y);
                } else if(baseObject.getPrefHeight() < maxSize.y) {
                    baseObject.setPrefHeight(maxSize.y);
                }
            }
        }
        if (type.equals("pos")) {
            if (!keepX) {
                baseObject.setLayoutX(relateToObject.getLayoutX() + offsetVec.x);
            }
            if (!keepY) {
                baseObject.setLayoutY(relateToObject.getLayoutY() + offsetVec.y);
            }
        }
        if (type.equals("scale->pos")) {
            if (!keepX) {
                baseObject.setLayoutX(relateToObject.getPrefWidth() + offsetVec.x);
            }
            if (!keepY) {
                baseObject.setLayoutY(relateToObject.getPrefHeight() + offsetVec.y);
            }
        }
        if (type.equals("pos->scale")) {
            TwoDVec<Double> targetSize = new TwoDVec<Double>(relateToObject.getLayoutX() + offsetVec.x,relateToObject.getLayoutY() + offsetVec.y);
            if (!keepX) {
                if (targetSize.x <= maxSize.x) {
                    baseObject.setPrefWidth(targetSize.x + offsetVec.x);
                } else if(baseObject.getPrefWidth() < maxSize.x) {
                    baseObject.setPrefWidth(maxSize.x);
                }
            }
            if (!keepY) {
                if (targetSize.y <= maxSize.y) {
                    baseObject.setPrefHeight(targetSize.y);
                } else if(baseObject.getPrefHeight() < maxSize.y) {
                    baseObject.setPrefWidth(maxSize.y);
                }
            }
        }
    }

    public static void applyAnchors(ArrayList<Anchor> anchors) {
        for (int i = 0; i < anchors.size(); i++) {
            anchors.get(i).applyAnchor();
        }
    }

    public static Anchor findAnchorOfObject(Node object, String type, ArrayList<Anchor> anchors) {
        for (int i = 0; i < anchors.size(); i++) {
            if (anchors.get(i).baseObject == object && anchors.get(i).type.equals(type)) {
                return anchors.get(i);
            }
        }
        return null;
    }
}