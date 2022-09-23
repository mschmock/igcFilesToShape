// Autor: Manuel Schmocker
// Datum: 22.09.2022
package ch.manuel.igctoraster;

import java.awt.Point;
import java.awt.geom.Point2D;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class IGCprocessing {

  // MEMBERVARIABLES
  private BufferedReader reader;
  private List<Point2D.Float> listLonLat;    

  // Regex
  private static final Pattern B_RECORD_RE = Pattern.compile("^B(\\d{6})(\\d{2})(\\d{5})([NS])(\\d{3})(\\d{5})([EW])([AV])(\\d{5})(\\d{5})");

  /*  group 1: time
      group 2+3: latitude
      group 5+6: longitude
      group 9+10: altitude
   */

  public IGCprocessing(File file) {
    listLonLat = new ArrayList<>();

    try {
      reader = new BufferedReader(new FileReader(file));
    } catch (FileNotFoundException ex) {
      Logger.getLogger(IGCprocessing.class.getName()).log(Level.SEVERE, null, ex);
    }
  }

  public void processIGC() {

    StringBuilder content = new StringBuilder();
    String line;

    try {
      // read lines
      while ((line = reader.readLine()) != null) {
        // search regex
        Matcher ma = B_RECORD_RE.matcher(line);

        if (ma.find()) {

          // S + N         
          float y = Integer.parseInt(ma.group(2), 10) + Integer.parseInt(ma.group(3), 10) / 60000f;
          if (ma.group(4).compareTo("S") == 0) {
            y = -y;
          }
          // E + W
          float x = Integer.parseInt(ma.group(5), 10) + Integer.parseInt(ma.group(6), 10) / 60000f;
          if (ma.group(7).compareTo("W") == 0) {
            x = -x;
          }
          // store in list 
          listLonLat.add(new Point2D.Float(x, y));
        }

//      content.append(line);
//      content.append(System.lineSeparator());
      }

      
//    return content.toString();
    } catch (IOException ex) {
      Logger.getLogger(IGCprocessing.class.getName()).log(Level.SEVERE, null, ex);
    }
  }

}
