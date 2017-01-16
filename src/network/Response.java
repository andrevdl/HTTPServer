package network;

import files.FileManager;
import files.rules.result.AltHeader;

import java.io.*;

/**
 *
 */
public class Response {

    /**
     *
     */
    public static final int OK = 200;

    /**
     *
     */
    public static final int INTERNAL_ERROR = 500;

    /**
     *
     */
    public static final int FILE_NOT_FOUND = 404;

    /**
     *
     */
    public static final int UNSUPPORTED_MEDIA_TYPE = 415;

    /**
     *
     */
    public static final int METHOD_NOT_ALLOWED = 405;

    /**
     *
     */
    private int status;

    /**
     *
     */
    private FileManager.HttpFile httpFile;

    /**
     *
     * @param httpFile
     */
    public Response(FileManager.HttpFile httpFile) {
        this.httpFile = httpFile;
        status = OK;
    }

    /**
     *
     * @param status
     */
    public Response(int status) {
        this.status = status;
    }

    /**
     *
     * @param writer
     * @param code
     * @param size
     */
    private void writeHeader(PrintWriter writer, int code, long size) {
        writeHeader(writer, code, httpFile.getMime().getMime(), size);
    }

    /**
     *
     * @param writer
     * @param code
     * @param contentType
     */
    private void writeHeader(PrintWriter writer, int code, String contentType) {
        writeHeader(writer, code, contentType, 0);
    }

    /**
     *
     * @param writer
     * @param code
     * @param contentType
     * @param size
     */
    private void writeHeader(PrintWriter writer, int code, String contentType, long size) {
        writer.printf("HTTP/1.1 %s \r\n", code); // Version & status code
        writer.printf("Content-Type: %s\r\n", contentType); // The type of data
        writer.printf("Content-Length: %d\r\n", size); // The size of data
        writer.print("Connection: close\r\n"); // Will close stream
        writer.print("\r\n"); // End of headers
        writer.flush();
    }

    /**
     *
     * @param writer
     * @param header
     */
    public static void writeHeader(PrintWriter writer, AltHeader header) {
        writer.printf("HTTP/1.1 %s \r\n", header.getHttpCode()); // Version & status code
        writer.printf("Content-Type: %s\r\n", "text/plain"); // The type of data

        header.getHeader(writer);

        writer.print("Connection: close\r\n"); // Will close stream
        writer.print("\r\n"); // End of headers
        writer.flush();
    }

    /**
     *
     * @param stream
     */
    public void send(OutputStream stream) {
        PrintWriter writer = new PrintWriter(stream);
        if (status >= 300 || status <= 100) {
            writeHeader(writer, status, Mime.getDefault());

            if (status >= 500) {
                System.err.println(status + " error raised");
            }
        } else {
            if (httpFile != null) {
                File file = httpFile.getFile();
                if (!file.canRead()) {
                    writeHeader(writer, INTERNAL_ERROR, Mime.getDefault());
                    return;
                }

                try {
                    if (httpFile.getMime().isBinary()) {
                        DataOutputStream out = new DataOutputStream(stream);

                        byte[] buffer = new byte[256];
                        FileInputStream reader = new FileInputStream(file);

                        int length;
                        while ((length = reader.read(buffer)) != -1) {
                            out.write(buffer, 0, length);
                        }

                        reader.close();

                        writeHeader(writer, status, out.size());

                        writer.flush();
                        out.flush();
                    } else {
                        long size = 0;
                        PrintWriter writer1 = new PrintWriter(stream);

                        char[] buffer = new char[256];
                        FileReader reader = new FileReader(file);

                        long _size;
                        while ((_size = reader.read(buffer)) != -1) {
                            size += _size;
                            writer1.write(buffer);
                        }

                        writeHeader(writer, status, size);

                        writer.flush();
                        writer1.flush();
                    }
                } catch (FileNotFoundException e) {
                    writeHeader(writer, FILE_NOT_FOUND, Mime.getDefault());
                } catch (IOException e) {
                    writeHeader(writer, INTERNAL_ERROR, Mime.getDefault());
                }
            } else {
                writeHeader(writer, FILE_NOT_FOUND, Mime.getDefault());
            }
        }
    }
}
