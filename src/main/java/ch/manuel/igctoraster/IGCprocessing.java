// Autor: Manuel Schmocker
// Datum: 22.09.2022
package ch.manuel.igctoraster;

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
  private List<Point2D.Double> listLonLat;

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

  // return list
  public List<Point2D.Double> getPointList() {
    return listLonLat;
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
          double y = Integer.parseInt(ma.group(2), 10) + Integer.parseInt(ma.group(3), 10) / 60000.0;
          if (ma.group(4).compareTo("S") == 0) {
            y = -y;
          }
          // E + W
          double x = Integer.parseInt(ma.group(5), 10) + Integer.parseInt(ma.group(6), 10) / 60000.0;
          if (ma.group(7).compareTo("W") == 0) {
            x = -x;
          }
          // store in list 
          // in swiss projection LV95
          Point2D.Double pt = wgs84ToLV95(new Point2D.Double(x, y) );
          listLonLat.add( new Point2D.Double(x, y) );
        }

//      content.append(line);
//      content.append(System.lineSeparator());
      }

//    return content.toString();
    } catch (IOException ex) {
      Logger.getLogger(IGCprocessing.class.getName()).log(Level.SEVERE, null, ex);
    }
  }

  // Transformation of ellipsoidal WGS84 coordinates to Swiss projection coordinates
  // Source: https://www.swisstopo.admin.ch/
  public static Point2D.Double wgs84ToLV95(Point2D.Double pt) {
    // latitudes φ and longitudes λ into arcseconds
    double φ = pt.getY() * 3600;
    double λ = pt.getX() * 3600;

    // calculate the auxiliary values 
    φ = (φ - 169028.66) / 10000;
    λ = (λ - 26782.5) / 10000;

    // Calculate projection coordinates in LV95
    double e_lv95 = 2600072.37
            + +211455.93 * λ
            - 10938.51 * λ * φ
            - 0.36 * λ * Math.pow(φ, 2)
            - 44.54 * Math.pow(λ, 3);

    double n_lv95 = 1200147.07
            + 308807.95 * φ
            + 3745.25 * Math.pow(λ, 2)
            + 76.63 * Math.pow(φ, 2)
            - 194.56 * Math.pow(λ, 2) * φ
            + 119.79 * Math.pow(φ, 3);

    return new Point2D.Double(e_lv95, n_lv95);
  }
}
