//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.example.bernabe.psic;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import mf.javax.xml.parsers.DocumentBuilder;
import mf.javax.xml.parsers.DocumentBuilderFactory;
import mf.javax.xml.parsers.ParserConfigurationException;
import org.inra.qualscape.io.FileTools;
import org.inra.qualscape.io.FormatTools;
import org.inra.qualscape.io.XmlTools;
import mf.org.w3c.dom.Document;
import mf.org.w3c.dom.Element;

import mf.org.apache.xml.serialize.OutputFormat;
import mf.org.apache.xml.serialize.XMLSerializer;
import mf.org.apache.xerces.jaxp.DocumentBuilderFactoryImpl;


public class WekaToXML {
    private File textfile = null;
    private File xmlfile = null;
    private int textIndentLength = 5;
    private String textIndentString = " ";
    private boolean indentWithTab = true;
    private boolean replaceTextByOperator = false;

    public WekaToXML(File aTextfile, File anXmlfile, boolean withTabIndentation, boolean withComparativeOperator) {
        this.textfile = aTextfile;
        this.xmlfile = anXmlfile;
        this.indentWithTab = withTabIndentation;
        this.replaceTextByOperator = withComparativeOperator;
        this.textIndentString = FormatTools.replicateString(this.textIndentString, this.textIndentLength);
    }

    private void writeXmlFile(Document document) {
        document.normalizeDocument();

        try {
            OutputFormat e = new OutputFormat(document);
            e.setIndenting(true);
            e.setIndent(this.textIndentLength);
            BufferedWriter output = new BufferedWriter(new FileWriter(this.xmlfile));
            XMLSerializer serializer = new XMLSerializer(output, e);
            serializer.serialize(document);
        } catch (Exception var5) {
            System.out.println("Error while writing XML file: " + this.xmlfile.getAbsolutePath());
            var5.printStackTrace();
        }

    }

    public boolean writeXmlFromWekaText() {
        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance("mf.org.apache.xerces.jaxp.DocumentBuilderFactoryImpl", null);
        DocumentBuilder docBuilder = null;

        try {
            docBuilder = docFactory.newDocumentBuilder();
            Document e1 = docBuilder.newDocument();
            e1.setXmlVersion("1.0");
            e1.setXmlStandalone(true);
            Element root = e1.createElement("DecisionTree");
            root.setAttribute("type", this.textfile.getName().substring(0, this.textfile.getName().indexOf(".")));
            BufferedReader input = new BufferedReader(new FileReader(this.textfile));
            String line = null;
            boolean lineRank = false;
            int myNodeRank = 0;
            Element myNode = root;

            while(true) {
                int var15;
                do {
                    if((line = input.readLine()) == null) {
                        e1.appendChild(root);
                        input.close();
                        this.writeXmlFile(e1);
                        this.cleanXmlFile(this.xmlfile);
                        return true;
                    }

                    var15 = FormatTools.countSequenceInString("|", line) + 1;
                    line = FormatTools.cleansWekaTreeLine(line);
                } while(line.length() <= 0);

                Element newNode = e1.createElement("Test");
                newNode.setAttribute("attribute", this.getTestVariable(line));
                newNode.setAttribute("operator", this.getTestOperator(line));
                newNode.setAttribute("value", this.getTestVariableValue(line));
                if(this.hasLeaf(line)) {
                    Element leafNode = e1.createElement("Output");
                    leafNode.setAttribute("decision", this.getLeafDecision(line));
                    leafNode.setAttribute("info", this.getLeafInfo(line));
                    newNode.appendChild(leafNode);
                }

                while(myNodeRank >= var15) {
                    myNode = (Element)myNode.getParentNode();
                    --myNodeRank;
                }

                myNode.appendChild(newNode);
                myNode = newNode;
                myNodeRank = var15;
            }
        } catch (FileNotFoundException var12) {
            System.out.println("File " + this.textfile.getAbsolutePath() + " does not exist.");
            var12.printStackTrace();
            return false;
        } catch (IOException var13) {
            System.out.println("Error while parsing " + this.textfile.getAbsolutePath());
            var13.printStackTrace();
            return false;
        } catch (ParserConfigurationException var14) {
            System.out.println("Error while parsing newDocumentBuilder");
            var14.printStackTrace();
            return false;
        }
    }

    private void cleanXmlFile(File anXmlFile) {
        String xmlFileString = FileTools.textfileContentToString(anXmlFile);
        if(this.replaceTextByOperator) {
            xmlFileString = xmlFileString.replaceAll("&lt;", "<");
            xmlFileString = xmlFileString.replaceAll("&gt;", ">");
        }

        if(this.indentWithTab) {
            xmlFileString = xmlFileString.replaceAll(this.textIndentString, "\t");
        } else {
            xmlFileString = xmlFileString.replaceAll(this.textIndentString, "");
        }

        FileTools.writeStringToFile(xmlFileString, anXmlFile);
    }

    private String removeLeafInfo(String line) {
        return line.contains(": ")?line.substring(0, line.indexOf(": ")):line;
    }

    private String getTestVariable(String line) {
        return line.substring(0, line.indexOf(" "));
    }

    private String getTestOperator(String line) {
        line = this.removeLeafInfo(line);
        String operator = line.substring(line.indexOf(" ") + 1, line.lastIndexOf(" "));
        return operator;
    }

    private String getTestVariableValue(String line) {
        line = this.removeLeafInfo(line);
        return line.substring(line.lastIndexOf(" ") + 1);
    }

    private boolean hasLeaf(String line) {
        return line.contains(": ");
    }

    private String getLeafDecision(String line) {
        int locate = line.lastIndexOf(" (");
        return locate == -1?line.substring(line.lastIndexOf(": ") + 2):line.substring(line.lastIndexOf(": ") + 2, locate);
    }

    private String getLeafInfo(String line) {
        int locate = line.lastIndexOf("(");
        return locate == -1?"":line.substring(locate);
    }

    public static void main(String[] args) {
        int argsLength = args.length;
        if(argsLength >= 1) {
            String textfileString = args[0];
            String xmlfileString;
            if(argsLength >= 2) {
                xmlfileString = args[1];
            } else {
                xmlfileString = textfileString + ".xml";
            }

            boolean withTabIndent;
            if(argsLength >= 3) {
                withTabIndent = Boolean.parseBoolean(args[2]);
            } else {
                withTabIndent = true;
            }

            boolean withComparativeOperator;
            if(argsLength >= 4) {
                withComparativeOperator = Boolean.parseBoolean(args[3]);
            } else {
                withComparativeOperator = false;
            }

            String mindmapfileString;
            if(argsLength >= 5) {
                mindmapfileString = args[4];
            } else {
                mindmapfileString = null;
            }

            File textfile = new File(textfileString);
            File xmlfile = new File(xmlfileString);
            if(textfile.exists()) {
                WekaToXML aWekaToXML = new WekaToXML(textfile, xmlfile, withTabIndent, withComparativeOperator);
                aWekaToXML.writeXmlFromWekaText();
                if(mindmapfileString != null) {
                    XmlTools.exportXmlDecisionTreeToFreemindFile(xmlfile, new File(mindmapfileString), withTabIndent);
                }
            } else {
                System.out.println("File " + textfile.getAbsolutePath() + " does not exist.");
            }

        } else {
            System.out.println("the path of the weka text file to read is not specified");
        }
    }
}
