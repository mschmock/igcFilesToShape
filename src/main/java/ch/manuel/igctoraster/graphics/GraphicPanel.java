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
  // transformation
  private final AffineTransform tx;
  private static final int pxBORDER = 16;                     // border in pixel
  private static Map<Integer, Municipality> mapID;             // map with id of municipalities
  // network
  private static Municipality selectedMunicip;

  // Constructor
  public GraphicPanel() {
    super();

    // initialisation
    listPoly = new ArrayList<>();
    mapID = new HashMap<Integer, Municipality>();

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
      drawBorder(g2);
    }
  }

  // repaint on click
  public void repaintPanel() {
    this.validate();
    this.repaint();
  }

  // calculate transformation matrx (translation, scale, mirror...)
  private void calcTransformation() {
    double wd = geoData.getWidth();
    double hg = geoData.getHeight();

    double ratio1 = (this.getWidth() - (2 * pxBORDER)) / wd;
    double ratio2 = (this.getHeight() - (2 * pxBORDER)) / hg;
    double scaleFact = (ratio1 < ratio2) ? ratio1 : ratio2;

    AffineTransform trans = AffineTransform.getTranslateInstance(-geoData.getBoundX(), -geoData.getBoundY());
    AffineTransform scale = AffineTransform.getScaleInstance(scaleFact, scaleFact);
    AffineTransform mirr_y = new AffineTransform(1, 0, 0, -1, 0, this.getHeight());
    AffineTransform trans2 = AffineTransform.getTranslateInstance(pxBORDER, -pxBORDER);
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
    for (int i = 0; i < listPoly.size(); i++) {
      Shape shape = this.tx.createTransformedShape(listPoly.get(i));
      g2.draw(shape);
    }
  }

  // draw on click
  private void drawFilling(Graphics2D g2) {
    if ( GraphicPanel.selectedMunicip != null ) {
      // index of selected municip
      int index = GraphicPanel.selectedMunicip.getIndex();
      Color col = Color.getHSBColor(0.5f, 1.0f, 0.65f);
      // fill polygon
      fillMunicip(g2, index, col);
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
