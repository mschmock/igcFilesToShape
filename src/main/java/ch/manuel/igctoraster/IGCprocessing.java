// Autor: Manuel Schmocker
// Datum: 22.09.2022

package ch.manuel.igctoraster;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;


public class IGCprocessing {
  
  // MEMBERVARIABLES
  private File file;
  
  private BufferedReader reader;
  
  public IGCprocessing(File file) {
    this.file = file;
    
    try {
      reader = new BufferedReader(new FileReader(file));
    } catch (FileNotFoundException ex) {
      Logger.getLogger(IGCprocessing.class.getName()).log(Level.SEVERE, null, ex);
    }
  }
  
  
  
  public String readAllLines(BufferedReader reader) throws IOException {
    StringBuilder content = new StringBuilder();
    String line;
    
    while ((line = reader.readLine()) != null) {
        content.append(line);
        content.append(System.lineSeparator());
    }

    return content.toString();
}
  
  
}
