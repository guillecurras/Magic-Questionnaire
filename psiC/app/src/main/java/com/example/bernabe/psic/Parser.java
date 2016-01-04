package com.example.bernabe.psic;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.File;
import java.util.TreeMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

/**
 * Created by guill on 18/11/2015.
 */
public class Parser {

    File arbolXML;
    private static TreeMap<String, String> respuestas = new TreeMap<String, String>();
    private static Document doc;
    private NamedNodeMap atts = null;
    private NodeList nodos;


    private int fase = -1;

    public Parser() {
    }

    public Parser(File arbolXML) {
        this.arbolXML = arbolXML;
    }


    public void setArbolXML(File arbolXML) {
        this.arbolXML = arbolXML;
    }

    public String getPrimeraPregunta()
    {
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            dbf.setValidating(false);
            DocumentBuilder db = dbf.newDocumentBuilder();
            doc = db.parse(arbolXML);

        } catch (Exception e) {
            e.printStackTrace(System.err);

        }

        Node tree = doc.getDocumentElement();
        nodos = tree.getChildNodes();

        for (int i = 0; i < nodos.getLength(); i++) {
            if (nodos.item(i).getNodeName().trim().equals("Test")) {
                atts = nodos.item(1).getAttributes();

            }
        }
        return atts.getNamedItem("attribute").getNodeValue();
    }

    public String getSiguientePregunta(double respuesta)
    {
        if (nodos == null)
        {
            return getPrimeraPregunta();
        }
        for (int i = 0; i < nodos.getLength(); i++) {
            if (nodos.item(i).getNodeName().equals("Test")) {
                atts = nodos.item(i).getAttributes();
                if (atts.getNamedItem("operator").getTextContent().trim().equals("<=")) {
                    if (respuesta <= Double.parseDouble(atts.getNamedItem("value").getTextContent().trim())){
                        NodeList nextlist = nodos.item(i).getChildNodes();
                        for (int j = 0; j < nextlist.getLength(); j++) {
                            if (nextlist.item(j).getNodeName().equals("Output")) {
                                return "#" + nextlist.item(j).getAttributes().getNamedItem("decision").getNodeValue();
                            } else if (nextlist.item(j).getNodeName().equals("Test")) {
                                nodos = nextlist;
                                return nextlist.item(j).getAttributes().getNamedItem("attribute").getNodeValue();
                            }
                        }
                    }
                }
                else if (atts.getNamedItem("operator").getTextContent().trim().equals(">")) {
                    if (respuesta > Double.parseDouble(atts.getNamedItem("value").getTextContent().trim())){
                        NodeList nextlist = nodos.item(i).getChildNodes();
                        for (int j = 0; j < nextlist.getLength(); j++) {
                            if (nextlist.item(j).getNodeName().equals("Output")) {
                                return "#" + nextlist.item(j).getAttributes().getNamedItem("decision").getNodeValue();
                            } else if (nextlist.item(j).getNodeName().equals("Test")) {
                                nodos = nextlist;
                                return nextlist.item(j).getAttributes().getNamedItem("attribute").getNodeValue();
                            }
                        }
                    }
                }
            }
        }
        return null;
    }
}
