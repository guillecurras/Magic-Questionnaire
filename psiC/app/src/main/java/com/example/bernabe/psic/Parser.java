package com.example.bernabe.psic;

import java.io.File;

/**
 * Created by guill on 18/11/2015.
 */
public class Parser {

    File arbolXML;

    private int fase = 0;

    public Parser() {
    }

    public Parser(File arbolXML) {
        this.arbolXML = arbolXML;
    }


    public void setArbolXML(File arbolXML) {
        this.arbolXML = arbolXML;
    }

    public String getSiguientePregunta(String respuesta)  //TODO: Implementar
    {
        String[] preguntas = {"¿Es peludo?", "¿Es electronico?", "¿Se puede transportar?", "#Ordenador"};  // La respuesta se envía con un #.
        fase++;

        return preguntas[fase];
    }
}
