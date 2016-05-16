package be.ordina.msdashboard;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

/**
 * Helper class for JSON assertions
 *
 * @author Andreas Evers
 */
public class JsonHelper {

    public static String load(final String fileName)
            throws FileNotFoundException {
        try (Scanner scanner = new Scanner(new File(fileName))) {
            final StringBuilder builder = new StringBuilder();
            while (scanner.hasNextLine())
                builder.append(scanner.nextLine()).append("\n");
            return builder.toString().trim();
        }
    }

    public static String removeBlankNodes(String string) {
        return string.replaceAll("_:t\\d", "");
    }
}
