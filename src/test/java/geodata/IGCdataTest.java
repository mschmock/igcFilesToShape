// Autor: Manuel Schmocker
// Datum: 22.09.2022
package geodata;

import ch.manuel.igctoraster.IGCprocessing;
import java.awt.geom.Point2D;
import java.io.File;
import org.junit.Assert;
import org.junit.Test;

public class IGCdataTest {

  @Test
  public void testTransformationWGS84toLV95() {
    Point2D.Double pp = new Point2D.Double(8.730497222, 46.04413056);
    pp = IGCprocessing.wgs84ToLV95(pp);
    double xVal = pp.getX();
    double yVal = pp.getY();

    Assert.assertEquals(2699999.76, xVal, 0.01);
    Assert.assertEquals(1099999.97, yVal, 0.01);
  }

  @Test
  public void testInputFromIGCfile() {
    String resourceName = "testfile.igc";

    ClassLoader classLoader = getClass().getClassLoader();
    File file = new File(classLoader.getResource(resourceName).getFile());

    IGCprocessing testProc = new IGCprocessing(file);
    testProc.processIGC();
    
    // test first line in file
    double ptX1 = testProc.getPointListWGS84().get(0).getX();
    double degX1 = 7.0;
    double minX1 = 50.728 / 60.0;
    Assert.assertEquals(degX1 + minX1, ptX1, 0.00001);
    double ptY1 = testProc.getPointListWGS84().get(0).getY();
    double degY1 = 46.0;
    double minY1 = 41.681 / 60.0;
    Assert.assertEquals(degY1 + minY1, ptY1, 0.00001);
  }

}
