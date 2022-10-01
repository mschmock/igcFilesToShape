// Autor: Manuel Schmocker
// Datum: 22.09.2022
package ch.manuel.igctoraster;

import ch.manuel.igctoraster.graphics.GraphicPanel;
import ch.manuel.igctoraster.gui.MainFrame;
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
  private RasterData rData;
  private BufferedImage imageRaster;

  // CONSTRUCTOR
  public DataHandler(File file) {
    this.file = file;
    rData = new RasterData(500);    // create raster with cell size of 500 m
  }

  // process a single igc file
  public void processSingleFile() {
    igcProcess = new IGCprocessing(file);
    igcProcess.processIGC();
    igcProcess.showPolygonOnPanel();
    // get pointList from igc data
    rData.rasterizeTrack(igcProcess.getPointListWGS84());
    imageRaster = rData.createImageFromBool();
    // image now available
    MainFrame.setMenuSaveActive();
  }

  // process a list with files
  public void processFiles() {
    List<String> listFiles = null;

    try {
      listFiles = findFiles(file.toPath(), ".igc");
    } catch (IOException ex) {
      Logger.getLogger(DataHandler.class.getName()).log(Level.SEVERE, null, ex);
    }
    Logger.getLogger(DataHandler.class.getName()).log(Level.INFO, "Opening list of files: {0}", listFiles.toString());

    for (String f : listFiles) {
      File ff = new File(f);
      igcProcess = new IGCprocessing(ff);
      igcProcess.processIGC();
      // get pointList from igc data
      rData.rasterizeTrack(igcProcess.getPointListWGS84());
      rData.addToList();
    }
    // analyse all rasters
    rData.sumRaster();
    showImgOnPanel();
    // image now available
    MainFrame.setMenuSaveActive();
  }

  // save image to file
  public void saveImage(File file) {
    if (file != null) {
      try {
        Logger.getLogger(DataHandler.class.getName()).log(Level.INFO, "Save image to file: '{0}'", file);
        ImageIO.write(imageRaster, "png", file);
      } catch (IOException ex) {
        Logger.getLogger(DataHandler.class.getName()).log(Level.SEVERE, null, ex);
      }
    }
  }

  // create a list with all files in a directory
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

  // add image to GraphicPanel
  private void showImgOnPanel() {
    GraphicPanel.setImg(rData);
    MainFrame.updateGraphicPanel();
  }

}
