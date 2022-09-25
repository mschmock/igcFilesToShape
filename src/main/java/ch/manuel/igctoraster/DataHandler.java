// Autor: Manuel Schmocker
// Datum: 22.09.2022
package ch.manuel.igctoraster;

import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.File;

public class DataHandler {

  // MEMBERVARIABLES
  private File file;
  private IGCprocessing igcProcess;
  private boolean[][] raster;

  // raster data
  private static final int X_MIN = 2450000;   // LV95: x Min
  private static final int Y_MIN = 1050000;   // LV95: y Min
  private static final int X_MAX = 2850000;   // LV95: x Max
  private static final int Y_MAX = 1310000;   // LV95: y Max
  private static final int CELL_SIZE = 500;   // in Meter
  private static int nbElemX;
  private static int nbElemY;

  // CONSTRUCTOR
  public DataHandler(File file) {
    this.file = file;

    nbElemX = Math.floorDiv(X_MAX - X_MIN, CELL_SIZE);
    nbElemY = Math.floorDiv(Y_MAX - Y_MIN, CELL_SIZE);

    raster = new boolean[nbElemX][nbElemY];
  }

  public void processSingleFile() {
    igcProcess = new IGCprocessing(file);
    igcProcess.processIGC();
    igcProcess.showPolygonOnPanel();
    this.rasterizeTrack();
  }

  public void processFiles() {

  }

  private void rasterizeTrack() {
    int indX;
    int indY;

    for (Point2D.Double p0 : igcProcess.getPointListWGS84()) {
      Point2D.Double pt = IGCprocessing.wgs84ToLV95(p0);

      indX = Math.floorDiv(X_MIN - (int) pt.getX(), CELL_SIZE);
      indY = Math.floorDiv(Y_MIN - (int) pt.getY(), CELL_SIZE);

      if ((indX >= 0 && indX < nbElemX) && (indY >= 0 && indY < nbElemY)) {
        raster[indX][indY] = true;
      }
      
    }
  }

  // create image
  private void createImage() {
    BufferedImage image;
    image = new BufferedImage(nbElemX, nbElemY, BufferedImage.TYPE_INT_ARGB);
    
    
  }
  
}
