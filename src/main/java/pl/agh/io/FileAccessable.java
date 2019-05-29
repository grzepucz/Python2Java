package pl.agh.io;

public interface FileAccessable {
    String read(String path);
    boolean save(String content, String path);
}
