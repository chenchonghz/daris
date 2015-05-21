package daris.client.ui.exmethod.graph;

import arc.gui.gwt.graphics.g2d.Graphics2D;
import arc.gui.gwt.graphics.g2d.scene.GraphicsObject;
import arc.gui.gwt.graphics.g2d.scene.GraphicsObjectReadyHandler;
import arc.gui.gwt.graphics.g2d.scene.HitDistance;
import arc.gui.gwt.graphics.g2d.scene.PrepareRequest;
import arc.gui.gwt.graphics.g2d.scene.Rectangle;

public class Edge implements GraphicsObject<Port[]> {

    public static final double LINE_WIDTH = 0.0016;

    private Port[] _ports;

    public Edge(Port a, Port b) {

        _ports = new Port[] { a, b };
        a.connect(this);
        b.connect(this);
    }

    public Port input() {

        return _ports[0];
    }

    public Port output() {

        return _ports[1];
    }

    @Override
    public void render(Graphics2D gc, boolean pathOnly) {

        // int x1 = (int) (_a.x() * gc.width());
        // int y1 = (int) (_a.y() * gc.height());
        // int x2 = (int) (_b.x() * gc.width());
        // int y2 = (int) (_b.y() * gc.height());
        // gc.beginPath();
        // gc.moveTo(x1, y1);
        // gc.lineTo(x2, y2);
        // gc.closePath();
        // gc.setStrokeWidth(2);
        // gc.setStrokeColour(Colours.BLACK);
        // gc.stroke();

        int lineWidth = (int) (LINE_WIDTH * gc.width());
        gc.setStrokeColour(Colours.BLACK);
        gc.setStrokeWidth(lineWidth);
        gc.setFillColour(Colours.BLACK);
        GraphUtil.strokeArrowLine(gc, input().x(), input().y(), output().x(), output().y(), LineStyle.SOLID, false);
    }

    @Override
    public PrepareRequest prepare(Graphics2D gc, GraphicsObjectReadyHandler rh) {

        rh.ready();
        return null;
    }

    @Override
    public Port[] data() {
        return _ports;
    }

    @Override
    public void setData(Port[] ports) {
        assert ports != null && ports.length == 2;
        _ports = ports;
    }

    @Override
    public boolean fixedBoundingBox() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public Rectangle<?> boundingBox(Graphics2D gc) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public HitDistance hit(Graphics2D gc, int px, int py) {
        // TODO Auto-generated method stub
        return null;
    }
}
