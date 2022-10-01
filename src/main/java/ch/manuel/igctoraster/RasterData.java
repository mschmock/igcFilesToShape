// Autor: Manuel Schmocker
// Datum: 30.10.2022
package ch.manuel.igctoraster;

import java.awt.Color;
import java.awt.Point;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class RasterData {

  // MEMBERVARIABLES
  private boolean[][] raster;
  private List<boolean[][]> listRaster;
  private int[][] sumRaster;
  private int maxVal;

  // raster size data
  private static final int X_MIN = 2450000;   // LV95: x Min
  private static final int Y_MIN = 1050000;   // LV95: y Min
  private static final int X_MAX = 2850000;   // LV95: x Max
  private static final int Y_MAX = 1310000;   // LV95: y Max
  private static int cellSize;                // element size in Meter
  private static int nbElemX;
  private static int nbElemY;

  // CONSTRUCTOR
  public RasterData(int cellSize) {
    RasterData.cellSize = cellSize;

    // nb of elements in raster
    nbElemX = Math.floorDiv(X_MAX - X_MIN, cellSize);
    nbElemY = Math.floorDiv(Y_MAX - Y_MIN, cellSize);

    // init elements
    raster = new boolean[nbElemX][nbElemY];
    listRaster = new ArrayList<>();
    sumRaster = new int[nbElemX][nbElemY];
  }

  // reset raster (all false)
  private void resetRaster() {
    raster = new boolean[nbElemX][nbElemY];
  }

  // crate raster from track
  public void rasterizeTrack(List<Point2D.Double> trackWGS84) {
    // reset raster to false
    resetRaster();

    int indX;
    int indY;

    for (Point2D.Double p0 : trackWGS84) {
      Point2D.Double pt = IGCprocessing.wgs84ToLV95(p0);
      indX = Math.floorDiv((int) pt.getX() - X_MIN, cellSize);
      indY = Math.floorDiv((int) pt.getY() - Y_MIN, cellSize);
      // limit point to extentions
      if ((indX >= 0 && indX < nbElemX) && (indY >= 0 && indY < nbElemY)) {
        raster[indX][indY] = true;
      }
    }
  }

  // add raster to list
  public void addToList() {
    listRaster.add(raster);
  }

  // sum raster
  public void sumRaster() {
    for (int i = 0; i < nbElemX; i++) {
      for (int j = 0; j < nbElemY; j++) {
        int count = 0;
        // count occurence per pixel
        for (boolean[][] r : listRaster) {
          if (r[i][j]) {
            count++;
          }
        }
        sumRaster[i][j] = count;
        // keep max value
        if (count > maxVal) {
          maxVal = count;
        }
      }
    }
    Logger.getLogger(RasterData.class.getName()).log(Level.INFO, "Job finished: All raster combined");
    Logger.getLogger(RasterData.class.getName()).log(Level.INFO, "With max. occurence: {0}", maxVal);
  }

  // RETURN IMAGE
  // return rgb image from sumRaster
  public BufferedImage createImageFromInt() {
    BufferedImage image = new BufferedImage(nbElemX, nbElemY, BufferedImage.TYPE_INT_RGB);
    for (int i = 0; i < nbElemX; i++) {
      for (int j = 0; j < nbElemY; j++) {
        float c = 1.0f * sumRaster[i][j] / maxVal;
        Color col;
        if (sumRaster[i][j] > 0) {
          col = Color.getHSBColor(c * 80.0f / 360.0f, 1.0f, 1.0f);
        } else {
          col = new Color(255, 255, 255, 255);
        }
        image.setRGB(i, j, col.getRGB());
      }
    }
    return image;
  }

  // return binary image form raster
  public BufferedImage createImageFromBool() {
    BufferedImage image = new BufferedImage(nbElemX, nbElemY, BufferedImage.TYPE_BYTE_BINARY);
    int col;

    for (int i = 0; i < nbElemX; i++) {
      for (int j = 0; j < nbElemY; j++) {
        if (raster[i][nbElemY - j - 1]) {
          col = Color.black.getRGB();
        } else {
          col = Color.white.getRGB();
        }
        image.setRGB(i, j, col);
      }
    }
    return image;
  }
  
  // get boundry
  public Point2D.Double getULcornerLV95() {
    return new Point2D.Double(X_MIN, Y_MAX);
  }
  public Point2D.Double getLRcornerLV95() {
    return new Point2D.Double(X_MAX, Y_MIN);
  }

  // test raster
  private void checkRaster() {
    int count = 0;

    for (int i = 0; i < nbElemX; i++) {
      for (int j = 0; j < nbElemY; j++) {
        if (raster[i][j]) {
          count++;
        }
      }
    }
    Logger.getLogger(RasterData.class.getName()).log(Level.INFO, "Nb of activ cells: {0}", count);
  }

}
