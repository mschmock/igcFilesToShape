// Autor: Manuel Schmocker
// Datum: 22.09.2022

package ch.manuel.igctoraster;

import java.io.File;



public class DataHandler {
  
  // MEMBERVARIABLES
  private File file;
  private IGCprocessing igcProcess;
  
  public DataHandler(File file) {
    this.file = file;
    
  }
  
  public void processSingleFile() {
    igcProcess = new IGCprocessing(file);
    igcProcess.processIGC();
  }
  
  public void processFiles() {
    
  }
  
          
}
