package daris.client.ui.exmethod.graph;

import java.util.List;
import java.util.Vector;

import arc.gui.gwt.colour.Colour;
import arc.gui.gwt.graphics.g2d.Graphics2D;
import arc.gui.gwt.graphics.g2d.scene.GraphicsObject;
import arc.gui.gwt.graphics.g2d.scene.GraphicsObjectReadyHandler;
import arc.gui.gwt.graphics.g2d.scene.HitDistance;
import arc.gui.gwt.graphics.g2d.scene.PrepareRequest;
import arc.gui.gwt.graphics.g2d.scene.Rectangle;

public class Port implements GraphicsObject<Node> {

    public static final double WIDTH = 0.005;
    public static final double HEIGHT = 0.008;
    public static final Colour PORT_COLOUR = Colours.DARK_GRAY;

    private double _x;
    private double _y;
    private double _w;
    private double _h;

    private Node _node;
    private List<Edge> _edges;

    public Port(Node node) {

        _x = 0;
        _y = 0;
        _w = WIDTH;
        _h = HEIGHT;
        _node = node;
        _edges = null;
    }

    public double x() {

        return _x;
    }

    public double y() {

        return _y;
    }

    public double width() {

        return _w;
    }

    public double height() {

        return _h;
    }

    protected void setOwnerNode(Node n) {

        _node = n;
    }

    public Node node() {

        return _node;
    }

    public List<Edge> edges() {

        return _edges;
    }

    public List<Node> connectedNodes() {

        if (_edges == null || _edges.isEmpty()) {
            return null;
        }
        List<Node> nodes = new Vector<Node>(_edges.size());
        for (Edge edge : _edges) {
            nodes.add(edge.output().node());
        }
        return nodes;
    }

    public void connect(Edge edge) {

        if (_edges == null) {
            _edges = new Vector<Edge>(2);
        }
        _edges.add(edge);
    }

    protected void setPosition(double x, double y) {

        _x = x;
        _y = y;
    }

    protected void setSize(double w, double h) {

        _w = w;
        _h = h;
    }

    @Override
    public void render(Graphics2D gc, boolean pathOnly) {

        int x = (int) ((_x - _w / 2) * gc.width());
        int y = (int) ((_y - _h / 2) * gc.height());
        int w = (int) (_w * gc.width());
        int h = (int) (_h * gc.height());
        gc.setFillColour(PORT_COLOUR);
        gc.fillRect(x, y, w, h);
    }

    @Override
    public PrepareRequest prepare(Graphics2D gc, GraphicsObjectReadyHandler rh) {

        rh.ready();
        return null;
    }

    @Override
    public Node data() {
        return _node;
    }

    @Override
    public void setData(Node node) {
        _node = node;
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
