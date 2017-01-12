package network;

import files.FileManager;

import java.io.*;

public class Response {

    public static final int OK = 200;
    public static final int INTERNAL_ERROR = 500;
    public static final int FILE_NOT_FOUND = 404;
    public static final int UNSUPPORTED_MEDIA_TYPE = 415;
    public static final int METHOD_NOT_ALLOWED = 405;

    private Request request;
    private int status;
    private FileManager.HttpFile httpFile;

    public Response(Request request, FileManager.HttpFile httpFile) {
        this.request = request;
        this.httpFile = httpFile;
        status = OK;
    }

    public Response(Request request, int status) {
        this.request = request;
        this.status = status;
    }

    private void writeHeader(PrintWriter writer, int code) {
        writeHeader(writer, code, httpFile.getMime().getMimeType());
    }

    private void writeHeader(PrintWriter writer, int code, String contentType) {
        writer.printf("HTTP/1.1 %s \r\n", code); // Version & status code
        writer.printf("Content-Type: %s\r\n", contentType); // The type of data
        writer.print("Connection: close\r\n"); // Will close stream
        writer.print("\r\n"); // End of headers
        writer.flush();
    }

    public void send(PrintWriter writer) {
        StringWriter sr = new StringWriter();
        PrintWriter writer1 = new PrintWriter(sr);

        if (status >= 300 || status <= 100) {
            writeHeader(writer, status, Mime.getDefaultMimeType());
        } else {
            if (httpFile != null) {
                File file = httpFile.getFile();
                if (!file.canRead()) {
                    writeHeader(writer, INTERNAL_ERROR, Mime.getDefaultMimeType());
                    return;
                }

                try {
                    if (httpFile.getMime().isBinary()) {
                        byte[] buffer = new byte[256];
                        FileInputStream reader = new FileInputStream(file);

                        int length;
                        while ((length = reader.read(buffer)) != -1) {
//                        writer.write(buffer);
                        }

                        reader.close();
                    } else {
                        char[] buffer = new char[256];
                        FileReader reader = new FileReader(file);

                        while (reader.read(buffer) != -1) {
                            writer1.write(buffer);
                        }
                    }

                    writeHeader(writer, status);

                    writer1.flush();
                    writer.print(sr);
                    writer.flush();
                } catch (FileNotFoundException e) {
                    writeHeader(writer, FILE_NOT_FOUND, Mime.getDefaultMimeType());
                } catch (IOException e) {
                    writeHeader(writer, INTERNAL_ERROR, Mime.getDefaultMimeType());
                }
            } else {
                writeHeader(writer, FILE_NOT_FOUND, Mime.getDefaultMimeType());
            }
        }
    }
}
