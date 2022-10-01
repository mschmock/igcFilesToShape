/**
 * Autor: Manuel Schmocker
 * Datum: 03.09.2022
 */
package igctoraster;

import ch.manuel.igctoraster.DataLoader;
import ch.manuel.igctoraster.Startup;
import ch.manuel.igctoraster.gui.InfoDialog;
import org.junit.Test;

public class MainTests {

  // test startup and data loader
  @Test
  public void testDataLoadOnStartup() {
    Startup.dialog = new InfoDialog(new javax.swing.JFrame(), true);
    DataLoader dataLoader = new DataLoader();
    dataLoader.loadData();

    DataLoader.geoData.testprint();
  }

}
