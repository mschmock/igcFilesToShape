//Autor: Manuel Schmocker
//Datum: 13.04.2020
package ch.manuel.igctoraster;

import ch.manuel.utilities.MyUtilities;
import ch.manuel.igctoraster.geodata.GeoData;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

// Class: Load geodatas from resources
public class DataLoader {

  // Membervariablen
  public static GeoData geoData;
  private static boolean loadOK;
  private static final Charset utf8 = StandardCharsets.UTF_8;

  // files in resources
  private static final String dataXML = "/data/appData.xml";
  private static final String geoDataJSON = "/data/geodata.json";
  private static final String lakesJSON = "/data/geodata_lakes.json";

  // xml
  private static Document appDataXML;

  //Konstruktor
  public DataLoader() {
    // Create container for geodata
    DataLoader.geoData = new GeoData();
    DataLoader.loadOK = false;
  }

  public void loadData() {
    boolean loadOK_1 = false;
    boolean loadOK_2 = false;

    // load xml
    loadXML();

    // load JSON from resouces
    // file 1
    Startup.dialog.addText("loading data from file: '" + geoDataJSON + "'\n");
    loadOK_1 = this.loadJSON(geoDataJSON, false);            // Grenzen -> geodata.json
    Startup.dialog.addText("loading data from file: '" + lakesJSON + "'\n");
    loadOK_2 = this.loadJSON(lakesJSON, true);               // Lakes -> geodata.json
    if (loadOK_1 && loadOK_2) {
      Startup.dialog.addText("\t-> OK\n");
    } else {
      Startup.dialog.addText("\t-> failed\n");
    }

    // preparing data
    if (loadOK_1 && loadOK_2) {
      DataLoader.loadOK = true;

      // calculate bounds + distances
      DataLoader.geoData.calculateBounds();
      DataLoader.geoData.calculateDistances();
    } else {
      DataLoader.loadOK = false;
    }
  }

  // load xml
  private boolean loadXML() {
    boolean hasErr = false;
    String errMsg = "All OK";

    try {
      // get File: appData.xml
      InputStream in = getClass().getResourceAsStream(dataXML);

      DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
      DocumentBuilder db = dbf.newDocumentBuilder();

      DataLoader.appDataXML = db.parse(in);
      DataLoader.appDataXML.normalizeDocument();

    } catch (ParserConfigurationException ex) {
      hasErr = true;
      errMsg = "Error loading xml: " + ex.getMessage();
    } catch (SAXException ex) {
      hasErr = true;
      errMsg = "Error loading xml: " + ex.getMessage();
    } catch (IOException ex) {
      hasErr = true;
      errMsg = "Error loading xml: " + ex.getMessage();
    }
    // print error-message
    if (hasErr) {
      MyUtilities.getErrorMsg("Error", errMsg);
      return false;
    } else {
      // no errors loading data
      return true;
    }
  }

  // load JSON: geodata (Grenzen)
  private boolean loadJSON(String pathJSON, boolean isLake) {

    // get File: geodata.json
    InputStream in = getClass().getResourceAsStream(pathJSON);

    // JSON parser object to parse read file
    JSONParser jsonParser = new JSONParser();

    // Error message
    String idName = "";
    String errMsg = "All OK";
    boolean hasErr = false;

    try ( BufferedReader reader = new BufferedReader(new InputStreamReader(in, utf8))) {
      //Read JSON file
      Object obj = jsonParser.parse(reader);
      JSONArray objList = (JSONArray) obj;

      //Iterate over objects array
      Iterator<JSONObject> iterator = objList.iterator();
      while (iterator.hasNext()) {
        JSONObject tmpObj = iterator.next();
        JSONObject attriObj = (JSONObject) tmpObj.get("attributes");

        idName = tmpObj.get("id").toString();

        // INSERT DATA
        String input;

        // name AND create a new Municipality object
        geoData.addMunicip(attriObj.get("GMDNAME").toString());       // Gemeindename
        // is a lake
        GeoData.getLastElement().setIsLake(isLake);
        // ID -> in map 
        input = attriObj.get("GMDNR").toString();
        if (MyUtilities.isInteger(input)) {
          // setID --> map with [id, object]
          geoData.setID(Integer.parseInt(input), GeoData.getLastElement());
        } else {
          errMsg = pathJSON + "\nFehler im Objekt " + idName + ", element: 'GMDNR'";
          hasErr = true;
          break;
        }
        // Zentrum: Koordinate LV 95 E
        input = attriObj.get("E_CNTR").toString();
        if (MyUtilities.isInteger(input)) {
          GeoData.getLastElement().setCenterE(Integer.parseInt(input));
        } else {
          errMsg = pathJSON + "\nFehler im Objekt " + idName + ", element: 'E_CNTR'";
          hasErr = true;
          break;
        }
        // Zentrum: Koordinate LV 95 N
        input = attriObj.get("N_CNTR").toString();
        if (MyUtilities.isInteger(input)) {
          GeoData.getLastElement().setCenterN(Integer.parseInt(input));
        } else {
          errMsg = pathJSON + "\nFehler im Objekt " + idName + ", element: 'N_CNTR'";
          hasErr = true;
          break;
        }
        // Umgrenzung: min Koordinate LV 95 N
        input = attriObj.get("N_MIN").toString();
        if (MyUtilities.isInteger(input)) {
          GeoData.getLastElement().setMinN(Integer.parseInt(input));
        } else {
          errMsg = pathJSON + "\nFehler im Objekt " + idName + ", element: 'N_MIN'";
          hasErr = true;
          break;
        }
        // Umgrenzung: max Koordinate LV 95 N
        input = attriObj.get("N_MAX").toString();
        if (MyUtilities.isInteger(input)) {
          GeoData.getLastElement().setMaxN(Integer.parseInt(input));
        } else {
          errMsg = pathJSON + "\nFehler im Objekt " + idName + ", element: 'N_MAX'";
          hasErr = true;
          break;
        }
        // Umgrenzung: min Koordinate LV 95 E
        input = attriObj.get("E_MIN").toString();
        if (MyUtilities.isInteger(input)) {
          GeoData.getLastElement().setMinE(Integer.parseInt(input));
        } else {
          errMsg = pathJSON + "\nFehler im Objekt " + idName + ", element: 'E_MIN'";
          hasErr = true;
          break;
        }
        // Umgrenzung: max Koordinate LV 95 E
        input = attriObj.get("E_MAX").toString();
        if (MyUtilities.isInteger(input)) {
          GeoData.getLastElement().setMaxE(Integer.parseInt(input));
        } else {
          errMsg = pathJSON + "\nFehler im Objekt " + idName + ", element: 'E_MAX'";
          hasErr = true;
          break;
        }
        // Polygon Gemeindegrenze
        if (!GeoData.getLastElement().setPolygon(attriObj.get("POLYGON").toString())) {
          errMsg = pathJSON + "\nFehler im Objekt " + idName + ", element: 'POLYGON'";
          hasErr = true;
          break;
        }

      }

    } catch (FileNotFoundException e) {
      hasErr = true;
      errMsg = "Datei nicht gefunden!";
    } catch (IOException e) {
      hasErr = true;
      errMsg = e.getMessage();
    } catch (org.json.simple.parser.ParseException e) {
      hasErr = true;
      errMsg = "Fehlerhafte Formatierung JSON in Pos: " + e.getPosition();
    }

    // print error-message
    if (hasErr) {
      MyUtilities.getErrorMsg("Error", errMsg);
      return false;
    } else {
      // no errors loading data
      return true;
    }
  }

  // getter
  // return boolean if loading is ok
  public static boolean isLoadingOK() {
    return DataLoader.loadOK;
  }

  public static String getXMLdata(String tagName) {
    return DataLoader.appDataXML.getElementsByTagName(tagName).item(0).getTextContent();
  }

  // set status text in Dialog
  void setStatusText() {
    Startup.dialog.addText("Application Version: " + DataLoader.getXMLdata("version") + "\n");
    Startup.dialog.addText("Loaded Municipalities: " + GeoData.getNbMunicip() + "\n");
    Startup.dialog.addText("Bounds xMin " + geoData.getBoundX()
            + ", yMin " + geoData.getBoundY() + "\n");
    Startup.dialog.addText("\tWidth " + geoData.getWidth()
            + ", Height " + geoData.getHeight() + "\n");
  }

}
