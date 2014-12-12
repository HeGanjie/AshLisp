package ash.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;

/**
 * Created by Bruce on 2014/4/19.
 */
public final class JavaUtils {
    public static final String DEFAULT_CHARSET = "utf-8";
    public static final boolean DEBUGGING = false;

    public static void delay(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            throwRuntimeExceptionAndPrint(e);
        }
    }

    public static String displayArray(final Object[] arrObjects, final String joinString) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < arrObjects.length; i++) {
            sb.append(arrObjects[i]);
            if (i + 1 < arrObjects.length) sb.append(joinString);
        }
        return sb.toString();
    }

    public static void throwRuntimeExceptionAndPrint(Throwable e) {
        if (DEBUGGING) e.printStackTrace();
        throw new RuntimeException(e);
    }

    public static String buildString(final Object ... args) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < args.length; i++)
            sb.append(args[i]);
        return sb.toString();
    }

    public static boolean isStringNullOrEmpty(final String mValue) {
        return mValue == null || mValue.isEmpty();
    }

    public static boolean isStringNullOrWriteSpace(final String mValue) {
        return mValue == null || mValue.trim().isEmpty();
    }

    public static String readTextFileForDefaultEncoding(final String resPath) {
        return readResourceTextFile(resPath, DEFAULT_CHARSET);
    }

    public static String readResourceTextFile(final String resPath, final String encodingName) {
        InputStream xmlResourceInputStream = JavaUtils.class.getClassLoader()
                .getResourceAsStream(resPath);
        BufferedReader xmlFileReader = new BufferedReader(
                new InputStreamReader(xmlResourceInputStream, Charset.forName(encodingName)));

        return readTextFromReader(xmlFileReader);
    }

    private static String readTextFromReader(final BufferedReader xmlFileReader) {
        StringBuffer sb = new StringBuffer();
        String temString = null;

        try {
            while (null != (temString = xmlFileReader.readLine())) {
                sb.append(temString);
                sb.append('\n');
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (null != xmlFileReader) {
                try {
                    xmlFileReader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return sb.toString();
    }

    public static boolean isClassExist(String string) {
    	try {
    		Class.forName(string);
    		return true;
    	} catch(ClassNotFoundException e) {
    		return false;
    	}
    }
}
