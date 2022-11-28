package de.fraunhofer.iais.eis.ids.component.interaction.util;

import de.fraunhofer.iais.eis.ids.component.interaction.validation.ShaclValidator;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.lang.reflect.Field;
import java.net.URL;

public class TestUtil {

    public static String loadResourceAsString(String filename) throws IOException {
        ClassLoader classloader = Thread.currentThread().getContextClassLoader();
        InputStream is = classloader.getResourceAsStream(filename);
        StringWriter writer = new StringWriter();
        IOUtils.copy(is, writer, "UTF-8");
        return writer.toString();
    }

    public static String readContentFromUrl(URL url) throws IOException {
        InputStream inputStream = url.openConnection().getInputStream();
        return streamToString(inputStream);
    }

    public static String streamToString(InputStream inputStream) throws IOException {
        StringWriter writer = new StringWriter();
        IOUtils.copy(inputStream, writer, "UTF-8");
        return writer.toString();
    }

    public static void resetShaclValidatorSingleton() {
        Field instance;
        try {
            instance = ShaclValidator.class.getDeclaredField("validator");
            instance.setAccessible(true);
            instance.set(null, null);
        } catch (Exception e) {
            throw new RuntimeException();
        }
    }

    public static boolean checkIfShaclValidatorSingletonIsNull() {
        Field instance;
        try {
            instance = ShaclValidator.class.getDeclaredField("validator");
            instance.setAccessible(true);
            return instance.get(ShaclValidator.class) == null;
        } catch (Exception e) {
            throw new RuntimeException();
        }
    }
}
