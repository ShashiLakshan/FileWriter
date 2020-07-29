import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.*;

public class FileWriterDemo {
    String errorMsg = null;
    public static void main(String args[]) {

        //new FileWriterDemo().textsToFile(1073741823, "sample.txt");
        //new FileWriterDemo().textsToFile(1073741824, "sample.txt");
        //new FileWriterDemo().textsToFile(1001, "sample.txt");
        new FileWriterDemo().textsToFile(5, "sample.txt");
    }

    public void textsToFile(int noOfLines, String fileName) {
        if (noOfLines > 1073741823 || noOfLines < 1) {
            errorMsg = "Invalid no of lines you provided";
            System.out.println(errorMsg);
            return;
        }
        final int BUFFER_LIMIT = 1000;
        FileUtils fileUtils = new FileUtils();
        TextLineQueue buffer = new TextLineQueue(BUFFER_LIMIT);

        new ProducerThread(buffer, noOfLines, fileUtils).start();
        new ConsumerThread(buffer, noOfLines, fileName, fileUtils).start();
    }
}

class TextLineQueue {
    BlockingQueue<String> textlinesQueue;
    int limit;

    public TextLineQueue(int limit) {
        textlinesQueue = new ArrayBlockingQueue<>(limit);
        this.limit = limit;
    }

    public boolean isFull() {
        return textlinesQueue.size() == limit;
    }

    public boolean isEmpty() {
        return textlinesQueue.size() == 0;
    }

    public void writeToBuffer(String line) {
        synchronized (textlinesQueue) {
            while (isFull()) {
                try {
                    textlinesQueue.wait();
                } catch (InterruptedException e) {

                }
            }
            textlinesQueue.add(line);
            textlinesQueue.notifyAll();
        }
    }

    public String takeFromBuffer() throws InterruptedException {

        synchronized (textlinesQueue) {
            while (isEmpty()) {
                textlinesQueue.wait();
            }
            textlinesQueue.notifyAll();
            return textlinesQueue.take();
        }
    }
}

/**
 * responsible for generating unique strings to a buffer
 */
class ProducerThread extends Thread {

    TextLineQueue textLineQueue;
    FileUtils fileUtils;
    int noOfLines;

    public ProducerThread(TextLineQueue textLineQueue, int noOfLines, FileUtils fileUtils) {
        this.textLineQueue = textLineQueue;
        this.noOfLines = noOfLines;
        this.fileUtils = fileUtils;
    }

    @Override
    public void run() {
        for(int i=0; i<noOfLines; i++) {
            try {
                textLineQueue.writeToBuffer(fileUtils.generateUniqueString());
            } catch (InterruptedException e) {}
        }
    }
}

/**
 * responsible for writing to a file from a buffer
 */
class ConsumerThread extends Thread {
    TextLineQueue textLineQueue;
    FileUtils fileUtils;
    int noOfLines;
    String fileName;

    public ConsumerThread(TextLineQueue textLineQueue, int noOfLines, String fileName, FileUtils fileUtils) {
        this.textLineQueue = textLineQueue;
        this.noOfLines = noOfLines;
        this.fileUtils = fileUtils;
        this.fileName = fileName;
    }

    @Override
    public void run() {
        File file = new File(fileName);
        try (FileWriter fr = new FileWriter(file)) {
            for(int i=0; i<noOfLines; i++) {
                fileUtils.writeToFile(fr, textLineQueue.takeFromBuffer());
            }
        } catch (IOException e){
            System.out.println("Error in File Creation");
        } catch (InterruptedException e) {
            System.out.println(e.getMessage());
        }
    }
}

class FileUtils {

    public final static int LENGTH_OF_UNIQUE_STRING = 100;

    public void writeToFile(FileWriter fr, String line) {
        try {
            fr.write(line);
            fr.write(System.lineSeparator());
        } catch (IOException e) {
            System.out.println("Something wrong when writing to the file");
        }

    }

    /**
     * generates unique Strings based on time stamp
     *
     * @return - a string with 100 characters long
     */
    public String generateUniqueString() throws InterruptedException {

        LocalDateTime datetime = LocalDateTime.now();

        //length of 23 characters
        DateTimeFormatter format = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss.SSSSSSS");
        String formatDateTime = datetime.format(format);

        StringBuilder sb = new StringBuilder();
        sb.append(formatDateTime);
        sb.append(getRandomString((LENGTH_OF_UNIQUE_STRING - sb.length())));

        Thread.sleep(0, 1);

        return sb.toString();
    }

    /**
     * @param length - length of return String
     * @return
     */
    public String getRandomString(int length) {
        String charset = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789abcdefghijklmnopqrstuvxyz";
        StringBuilder sb = new StringBuilder(length);
        int index = 0;

        for (int i = 0; i < length; i++) {
            index = (int) (charset.length() * Math.random());
            sb.append(charset.charAt(index));
        }
        return sb.toString().toString();
    }
}
