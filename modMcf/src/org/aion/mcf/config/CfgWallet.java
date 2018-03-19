package org.aion.mcf.config;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

public class CfgWallet extends AbstractXMLCfg {

    private static final String CFG_NAME = "wallet";
    private final static String UI_ENABLED = "ui_enabled";

    private boolean uiEnabled;

    @Override
    protected void readConfigElement(final XMLStreamReader sr, final String elementName) throws XMLStreamException {
        switch (elementName) {
            case UI_ENABLED:
                uiEnabled = Boolean.valueOf(Cfg.readValue(sr));
                break;
            default:
                Cfg.skipElement(sr);
                break;
        }
    }

    @Override
    protected void writeConfigElements(final XMLStreamWriter xmlWriter) throws XMLStreamException {
        xmlWriter.writeCharacters("\r\n\t");
        xmlWriter.writeStartElement(CFG_NAME);

        xmlWriter.writeCharacters("\r\n\t\t");
        xmlWriter.writeStartElement(UI_ENABLED);
        xmlWriter.writeCharacters(String.valueOf(uiEnabled));
        xmlWriter.writeEndElement();

        xmlWriter.writeCharacters("\r\n\t");
        xmlWriter.writeEndElement();
    }

    public boolean isUiEnabled() {
        return uiEnabled;
    }
}
