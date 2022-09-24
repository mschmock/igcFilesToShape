// Autor: Manuel Schmocker
// Datum: 22.09.2022

package ch.manuel.igctoraster;

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
    
    nbElemX = (X_MAX-X_MIN)/CELL_SIZE;
    nbElemY = (Y_MIN-Y_MAX)/CELL_SIZE;
    
    raster = new boolean[nbElemX][nbElemY];
  }
  
  public void processSingleFile() {
    igcProcess = new IGCprocessing(file);
    igcProcess.processIGC();
  }
  
  public void processFiles() {
    
  }
  
          
}
