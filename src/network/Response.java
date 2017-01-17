package network;

import files.FileManager;
import files.rules.result.AltHeader;

import java.io.*;

/**
 * HTTP Response
 */
public class Response {

    /**
     * HTTP OK
     */
    public static final int OK = 200;

    /**
     * HTTP INTERNAL ERROR
     */
    public static final int INTERNAL_ERROR = 500;

    /**
     * HTTP FILE NOT FOUND
     */
    public static final int FILE_NOT_FOUND = 404;

    /**
     * HTTP UNSUPPORTED MEDIA TYPE
     */
    public static final int UNSUPPORTED_MEDIA_TYPE = 415;

    /**
     * HTTP METHOD NOT ALLOWED
     */
    public static final int METHOD_NOT_ALLOWED = 405;

    /**
     * Status of the response.
     */
    private int status;

    /**
     * Requested file.
     */
    private FileManager.HttpFile httpFile;

    /**
     * Constructor.
     * @param httpFile Requested file.
     */
    public Response(FileManager.HttpFile httpFile) {
        this.httpFile = httpFile;
        status = OK;
    }

    /**
     * Constructor.
     * @param status Status of the response.
     */
    public Response(int status) {
        this.status = status;
    }

    /**
     * Create HTTP response header.
     * @param writer Writer to write to.
     * @param code Status of the response.
     * @param size Size of the body of the request.
     */
    private void writeHeader(PrintWriter writer, int code, long size) {
        writeHeader(writer, code, httpFile.getMime().getMime(), size);
    }

    /**
     * Create HTTP response header.
     * @param writer Writer to write to.
     * @param code Status of the response.
     * @param contentType Content type of the request.
     */
    private void writeHeader(PrintWriter writer, int code, String contentType) {
        writeHeader(writer, code, contentType, 0);
    }

    /**
     * Create HTTP response header.
     * @param writer Writer to write to.
     * @param code Status of the response.
     * @param contentType Content type of the request.
     * @param size Size of the body of the request.
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
     * Create HTTP response header.
     * @param writer Writer to write to.
     * @param header Header data.
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
     * Send the requested data to the client as a HTTP response.
     * @param stream Output stream of the {@link Client#socket}.
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

                        // buffer each time 256 bytes
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

                        // buffer each time 256 characters (256 bytes)
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
