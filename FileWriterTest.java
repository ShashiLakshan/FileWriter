
import org.junit.Test;

import static org.junit.Assert.*;

public class FileWriterTest {
    public static final String FILE_NAME = "sample.txt";
    public static final String EXPECTED_ERROR_MSG = "Invalid no of lines you provided";
    public static final int INVALID_NO_OF_LINES = 1073741824;

    FileWriterDemo fileWriterDemo;

    @Test
    public void testInvalidNoOfLines() {
        fileWriterDemo = new FileWriterDemo();
        fileWriterDemo.textsToFile(INVALID_NO_OF_LINES, FILE_NAME);
        assertEquals(EXPECTED_ERROR_MSG, fileWriterDemo.errorMsg);
    }

    //this should go to a seperate class called FileUtilsTest
    @Test
    public void testUniqueStrings() throws InterruptedException {
        FileUtils fileUtils = new FileUtils();
        String uniqueStringOne = fileUtils.generateUniqueString();
        String uniqueStringTwo = fileUtils.generateUniqueString();
        assertFalse(uniqueStringOne.equals(uniqueStringTwo));
    }
}
