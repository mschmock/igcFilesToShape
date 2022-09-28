// Autor: Manuel Schmocker
// Datum: 22.09.2022
package ch.manuel.igctoraster;

import ch.manuel.igctoraster.gui.MainFrame;
import java.awt.Color;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.imageio.ImageIO;

public class DataHandler {

  // MEMBERVARIABLES
  private File file;
  private IGCprocessing igcProcess;
  private boolean[][] raster;
  private BufferedImage image;

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
    this.checkRaster();
    this.createImage();
  }

  public void processFiles() {
    List<String> listFiles = null;

    try {
      listFiles = findFiles(file.toPath(), ".igc");
    } catch (IOException ex) {
      Logger.getLogger(DataHandler.class.getName()).log(Level.SEVERE, null, ex);
    }
    Logger.getLogger(DataHandler.class.getName()).log(Level.INFO, listFiles.toString());
  }

  private void rasterizeTrack() {
    int indX;
    int indY;

    for (Point2D.Double p0 : igcProcess.getPointListWGS84()) {
      Point2D.Double pt = IGCprocessing.wgs84ToLV95(p0);

      indX = Math.floorDiv((int) pt.getX() - X_MIN, CELL_SIZE);
      indY = Math.floorDiv((int) pt.getY() - Y_MIN, CELL_SIZE);

      if ((indX >= 0 && indX < nbElemX) && (indY >= 0 && indY < nbElemY)) {
        raster[indX][indY] = true;
//        System.out.println("Set index: " + indX + ", " + indY);
      }
    }

  }

  // save file
  public void saveImage(File file) {
    if (file != null) {
      try {
        Logger.getLogger(DataHandler.class.getName()).log(Level.INFO, "Save to file '" + file + "'");
        ImageIO.write(image, "png", file);
      } catch (IOException ex) {
        Logger.getLogger(DataHandler.class.getName()).log(Level.SEVERE, null, ex);
      }
    }
  }

  // create image
  private void createImage() {
    image = new BufferedImage(nbElemX, nbElemY, BufferedImage.TYPE_BYTE_BINARY);
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
    // image now available
    MainFrame.setMenuSaveActive();
  }

  public static List<String> findFiles(Path path, String fileExtension) throws IOException {

    if (!Files.isDirectory(path)) {
      throw new IllegalArgumentException("Path must be a directory!");
    }

    List<String> result;

    try ( Stream<Path> walk = Files.walk(path)) {
      result = walk
              .filter(p -> !Files.isDirectory(p))
              // this is a path, not string, this only test if path end with a certain path
              //.filter(p -> p.endsWith(fileExtension)) convert path to string first
              .map(p -> p.toString().toLowerCase())
              .filter(f -> f.endsWith(fileExtension))
              .collect(Collectors.toList());
    }

    return result;
  }

  // test
  private void checkRaster() {
    int count = 0;

    for (int i = 0; i < nbElemX; i++) {
      for (int j = 0; j < nbElemY; j++) {
        if (raster[i][j]) {
          count++;
        }
      }
    }
    System.out.println("Nb of activ cells: " + count);
  }

}
