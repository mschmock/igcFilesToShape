// Autor: Manuel Schmocker
// Datum: 17.09.2022
package ch.manuel.igctoraster.graphics;

import ch.manuel.igctoraster.DataLoader;
import ch.manuel.igctoraster.RasterData;
import ch.manuel.igctoraster.geodata.GeoData;
import ch.manuel.igctoraster.geodata.Municipality;
import ch.manuel.igctoraster.gui.MainFrame;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JPanel;

public class GraphicPanel extends JPanel {

  // access to geodata
  private static GeoData geoData;
  // list polygons
  private static List<Polygon> listPoly;
  private static List<Polygon> listPolyLakes;
  private static Path2D.Double igcTrack;
  // transformation
  private static AffineTransform tx;
  private static float zoom;
  private static Point drag;
  private static final int PX_BORDER = 16;                     // border in pixel
  private static Map<Integer, Municipality> mapID;             // map with id of municipalities
  // network
  private static Municipality selectedMunicip;
  // raster
  private static BufferedImage image;
  private static Point2D.Double ulCorner;
  private static Point2D.Double lrCorner;

  // CONSTRUCTOR
  public GraphicPanel() {
    super();

    // initialisation
    igcTrack = null;
    listPoly = new ArrayList<>();
    listPolyLakes = new ArrayList<>();
    mapID = new HashMap<>();

    // get polygons from geoData
    GraphicPanel.geoData = DataLoader.geoData;
    initPolygons();

    // init transformation
    tx = new AffineTransform();
    zoom = 1.0f;
    drag = new Point(0, 0);

    // raster
    image = null;
    ulCorner = new Point2D.Double(0, 0);
    lrCorner = new Point2D.Double(this.getWidth(), this.getHeight());
  }

  // initalisation of polygons (border municipalites)
  private void initPolygons() {
    if (geoData != null) {
      for (int i = 0; i < GeoData.getNbMunicip(); i++) {
        List<int[]> listPolyX = GeoData.getMunicip(i).getPolyX();
        List<int[]> listPolyY = GeoData.getMunicip(i).getPolyY();
        int nb = listPolyX.size();

        for (int j = 0; j < nb; j++) {
          listPoly.add(new Polygon(listPolyX.get(j),
                  listPolyY.get(j),
                  listPolyX.get(j).length)
          );
          // create map with municipalities
          mapID.put(listPoly.size() - 1, GeoData.getMunicip(i));
        }

        //lakes
        if (GeoData.getMunicip(i).getIsLake()) {
          for (int j = 0; j < nb; j++) {
            listPolyLakes.add(new Polygon(listPolyX.get(j),
                    listPolyY.get(j),
                    listPolyX.get(j).length)
            );
          }
        }

      }
    }
  }

  @Override
  protected void paintComponent(Graphics g) {
    super.paintComponent(g);

    // draw geo data
    Graphics2D g2 = (Graphics2D) g;
    if (geoData != null) {
      // define transformation
      calcTransformation();
      // draw raster on top (with transparancy)
      drawImg(g);

      // draw polygons
      drawLakes(g2);
      drawBorder(g2);
      drawTrack(g2);
    }

  }

  // repaint on click
  public void repaintPanel() {
    this.validate();
    this.repaint();
  }

  // set img background
  public static void setImg(RasterData rData) {
    GraphicPanel.image = rData.createImageFromInt();
    GraphicPanel.ulCorner = rData.getULcornerLV95();
    GraphicPanel.lrCorner = rData.getLRcornerLV95();
  }

  // set igc track
  public static void setIgcTrack(Path2D.Double track) {
    GraphicPanel.igcTrack = track;
  }

  // calculate transformation matrx (translation, scale, mirror...)
  private void calcTransformation() {
    double wd = geoData.getWidth();
    double hg = geoData.getHeight();

    double ratio1 = (this.getWidth() - (2 * PX_BORDER)) / wd;
    double ratio2 = (this.getHeight() - (2 * PX_BORDER)) / hg;
    double scaleFact = (ratio1 < ratio2) ? ratio1 : ratio2;

    AffineTransform trans = AffineTransform.getTranslateInstance(-geoData.getBoundX(), -geoData.getBoundY());
    AffineTransform scale = AffineTransform.getScaleInstance(scaleFact, scaleFact);
    AffineTransform mirr_y = new AffineTransform(1, 0, 0, -1, 0, this.getHeight());
    AffineTransform trans2 = AffineTransform.getTranslateInstance(PX_BORDER, -PX_BORDER);
    // zoom
    AffineTransform trans3a = AffineTransform.getTranslateInstance(-this.getWidth() / 2, -this.getHeight() / 2);
    AffineTransform scale3 = AffineTransform.getScaleInstance(zoom, zoom);
    AffineTransform trans3b = AffineTransform.getTranslateInstance(+this.getWidth() / 2, +this.getHeight() / 2);
    // drag
    AffineTransform trans4 = AffineTransform.getTranslateInstance(drag.getX(), drag.getY());

    tx.setToIdentity();
    // drag
    tx.concatenate(trans4);
    // zoom
    tx.concatenate(trans3b);
    tx.concatenate(scale3);
    tx.concatenate(trans3a);
    // inital transformation
    tx.concatenate(trans2);
    tx.concatenate(mirr_y);
    tx.concatenate(scale);
    tx.concatenate(trans);

  }

  // draw image
  private void drawImg(Graphics g) {
    Point2D p1 = GraphicPanel.tx.transform(ulCorner, null);
    Point2D p2 = GraphicPanel.tx.transform(lrCorner, null);

    if (image != null) {
      Logger.getLogger(GraphicPanel.class.getName()).log(Level.INFO, "Set image to position: {0},{1},{2},{3}", new Object[]{p1.getX(), p1.getY(), p2.getX(), p2.getY()});
      g.drawImage(image, (int) p1.getX(), (int) p1.getY(), (int) p2.getX(), (int) p2.getY(), this);
    }
  }

  // draw Border
  private void drawBorder(Graphics2D g2) {
    // filling if available
    drawFilling(g2);
    // draw Border
    g2.setStroke(new BasicStroke(1));
    g2.setColor(Color.black);
    for (Polygon poly : listPoly) {
      Shape shape = GraphicPanel.tx.createTransformedShape(poly);
      g2.draw(shape);
    }
  }

  // draw filling lakes
  private void drawLakes(Graphics2D g2) {
    g2.setColor(Color.getHSBColor(0.541f, 0.7f, 1.0f));
    for (Polygon lake : listPolyLakes) {
      Shape shape = GraphicPanel.tx.createTransformedShape(lake);
      g2.fill(shape);
    }
  }

  // draw on click
  private void drawFilling(Graphics2D g2) {
    if (GraphicPanel.selectedMunicip != null) {
      // index of selected municip
      int index = GraphicPanel.selectedMunicip.getIndex();
      Color col = Color.getHSBColor(0.5f, 1.0f, 0.65f);
      // fill polygon
      fillMunicip(g2, index, col);
    }
  }

  // draw igc track
  private void drawTrack(Graphics2D g2) {
    if (GraphicPanel.igcTrack != null) {
      // draw track
      g2.setStroke(new BasicStroke(1));
      g2.setColor(Color.red);
      Shape shape = GraphicPanel.tx.createTransformedShape(igcTrack);
      g2.draw(shape);
    }
  }

  // fill polygon of municipality i
  private void fillMunicip(Graphics2D g2, int i, Color col) {
    // color
    g2.setColor(col);

    // prepare polygons
    List<int[]> listPolyX = GeoData.getMunicip(i).getPolyX();
    List<int[]> listPolyY = GeoData.getMunicip(i).getPolyY();
    int nb = listPolyX.size();

    // draw polygons
    for (int j = 0; j < nb; j++) {
      Shape shape = GraphicPanel.tx.createTransformedShape(
              new Polygon(listPolyX.get(j),
                      listPolyY.get(j),
                      listPolyX.get(j).length));
      g2.fill(shape);
    }
  }

  // set index / name for element with click
  public void setNameOnClick(Point p) {
    for (int i = 0; i < listPoly.size(); i++) {
      Shape shape = GraphicPanel.tx.createTransformedShape(listPoly.get(i));
      if (shape.contains(p)) {
        GraphicPanel.selectedMunicip = GraphicPanel.mapID.get(i);
        MainFrame.setStatusText("Selected: " + GraphicPanel.mapID.get(i).getName());
        break;
      }
    }
  }

  public void zoomIn(Point p) {
    zoom = zoom * 1.2f;
    repaintPanel();
  }

  public void zoomOut(Point p) {
    zoom = zoom / 1.2f;
    repaintPanel();
  }

  public void dragMap(Point p) {
    drag = new Point((int) (p.getX() + drag.getX()), (int) (p.getY() + drag.getY()));
    repaintPanel();
  }

  public void resetView() {
    zoom = 1.0f;
    drag = new Point(0, 0);
    repaintPanel();
  }

}
