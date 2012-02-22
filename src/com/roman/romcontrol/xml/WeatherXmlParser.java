
package com.roman.romcontrol.xml;

import android.content.Context;
import android.util.Log;

import java.io.StringReader;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import com.roman.romcontrol.WeatherInfo;

public class WeatherXmlParser {

    protected static final String TAG = "WeatherXmlParser";

    /** Yahoo attributes */
    private static final String PARAM_YAHOO_LOCATION = "yweather:location";
    private static final String PARAM_YAHOO_UNIT = "yweather:units";
    private static final String PARAM_YAHOO_ATMOSPHERE = "yweather:atmosphere";
    private static final String PARAM_YAHOO_CONDITION = "yweather:condition";
    private static final String PARAM_YAHOO_WIND = "yweather:wind";

    private static final String ATT_YAHOO_CITY = "city";
    private static final String ATT_YAHOO_TEMP = "temp";
    private static final String ATT_YAHOO_HUMIDITY = "humidity";
    private static final String ATT_YAHOO_TEXT = "text";
    private static final String ATT_YAHOO_DATE = "date";
    private static final String ATT_YAHOO_SPEED = "speed";

    private Context context;

    public WeatherXmlParser() {
    }

    public WeatherInfo parseWeatherResponse(Document docWeather) {
        if (docWeather == null) {
            Log.e(TAG, "Invalid doc weather");
            return null;
        }

        String strCity = null;
        String strDate = null;
        String strCondition = null;
        String strTempC = null;
        String strHumidity = null;
        String strWindSpeed = null;

        try {
            Element root = docWeather.getDocumentElement();
            root.normalize();
            
            NamedNodeMap locationNode = root.getElementsByTagName(PARAM_YAHOO_LOCATION).item(0)
                    .getAttributes();

            if (locationNode != null) {
                strCity = locationNode.getNamedItem(ATT_YAHOO_CITY).getNodeValue();
            }

            NamedNodeMap atmosNode = root.getElementsByTagName(PARAM_YAHOO_ATMOSPHERE).item(0)
                    .getAttributes();
            if (atmosNode != null) {
                strHumidity = atmosNode.getNamedItem(ATT_YAHOO_HUMIDITY).getNodeValue();
            }

            NamedNodeMap conditionNode = root.getElementsByTagName(PARAM_YAHOO_CONDITION).item(0)
                    .getAttributes();
            if (conditionNode != null) {
                strCondition = conditionNode.getNamedItem(ATT_YAHOO_TEXT).getNodeValue();
                strTempC = conditionNode.getNamedItem(ATT_YAHOO_TEMP).getNodeValue();
                strDate = conditionNode.getNamedItem(ATT_YAHOO_DATE).getNodeValue();
            }

            NamedNodeMap temNode = root.getElementsByTagName(PARAM_YAHOO_WIND).item(0)
                    .getAttributes();
            if (temNode != null) {
                strWindSpeed = temNode.getNamedItem(ATT_YAHOO_SPEED).getNodeValue();
            }
        } catch (Exception e) {
            Log.e(TAG, "Something wrong with parser data: " + e.toString());
            return null;
        }

        /* Weather info */
        WeatherInfo yahooWeatherInfo = new WeatherInfo(strCity, strDate,
                strCondition, strTempC, strHumidity, strWindSpeed);

        return yahooWeatherInfo;
    }

    public String parsePlaceFinderResponse(String response) {
        try {

            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(new InputSource(new StringReader(response)));

            NodeList resultNodes = doc.getElementsByTagName("Result");

            Node resultNode = resultNodes.item(0);
            NodeList attrsList = resultNode.getChildNodes();

            for (int i = 0; i < attrsList.getLength(); i++) {
                
                Node node = attrsList.item(i);
                Node firstChild = node.getFirstChild();

                if ("woeid".equalsIgnoreCase(node.getNodeName()) && firstChild != null) {
                    return firstChild.getNodeValue();
                }
            }
        } catch (Exception e) {
            Log.e(TAG, e.toString());
        }
        return null;
    }
}