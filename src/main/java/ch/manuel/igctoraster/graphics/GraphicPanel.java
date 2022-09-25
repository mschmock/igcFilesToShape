// Autor: Manuel Schmocker
// Datum: 17.09.2022
package ch.manuel.igctoraster.graphics;

import ch.manuel.igctoraster.DataLoader;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.JPanel;

public class GraphicPanel extends JPanel {

  // access to geodata
  private static GeoData geoData;
  // list polygons
  private static List<Polygon> listPoly;
  private static List<Polygon> listPolyLakes;
  private static Path2D.Double igcTrack;
  // transformation
  private final AffineTransform tx;
  private static final int PX_BORDER = 16;                     // border in pixel
  private static Map<Integer, Municipality> mapID;             // map with id of municipalities
  // network
  private static Municipality selectedMunicip;

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
    this.tx = new AffineTransform();

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
    Graphics2D g2 = (Graphics2D) g;

    if (geoData != null) {
      // define transformation
      calcTransformation();
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

  // set igc track
  public static void setIgcTrack(Path2D.Double track) {
    igcTrack = track;
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
    tx.setToIdentity();
    tx.concatenate(trans2);
    tx.concatenate(mirr_y);
    tx.concatenate(scale);
    tx.concatenate(trans);
  }

  // draw Border
  private void drawBorder(Graphics2D g2) {
    // filling if available
    drawFilling(g2);
    // draw Border
    g2.setStroke(new BasicStroke(1));
    g2.setColor(Color.black);
    for (Polygon poly : listPoly) {
      Shape shape = this.tx.createTransformedShape(poly);
      g2.draw(shape);
    }
  }

  // draw filling lakes
  private void drawLakes(Graphics2D g2) {
    g2.setColor(Color.getHSBColor(0.541f, 0.7f, 1.0f));
    for (Polygon lake : listPolyLakes) {
      Shape shape = this.tx.createTransformedShape(lake);
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
      Shape shape = this.tx.createTransformedShape(igcTrack);
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
      Shape shape = this.tx.createTransformedShape(
              new Polygon(listPolyX.get(j),
                      listPolyY.get(j),
                      listPolyX.get(j).length));
      g2.fill(shape);
    }
  }

  // set index / name for element with click
  public void setNameOnClick(Point p) {
    for (int i = 0; i < listPoly.size(); i++) {
      Shape shape = this.tx.createTransformedShape(listPoly.get(i));
      if (shape.contains(p)) {
        GraphicPanel.selectedMunicip = GraphicPanel.mapID.get(i);
        MainFrame.setStatusText("Selected: " + GraphicPanel.mapID.get(i).getName());
        break;
      }
    }
  }

  // vue changed
  public static void viewChanged() {
    //legend.resetMaxVal();
  }
}
