package de.fraunhofer.iais.eis.ids.component.core.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class CalendarUtil {

    final private static Logger logger = LoggerFactory.getLogger(CalendarUtil.class);

    public static XMLGregorianCalendar now() {
        GregorianCalendar c = new GregorianCalendar();
        c.setTime(new Date());
        try {
            return DatatypeFactory.newInstance().newXMLGregorianCalendar(c);
        }
        catch (DatatypeConfigurationException e) {
            logger.error("Unable to create calendar object", e);
            return null;
        }
    }
}
