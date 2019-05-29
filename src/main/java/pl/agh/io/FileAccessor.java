package pl.agh.io;

import java.io.FileInputStream;

import java.io.*;

public class FileAccessor implements FileAccessable {

    @Override
    public String read(String path)
    {
        return this.getContentFromPath(path);
    }

    /**
     *
     * @param content String
     * @param filename String
     * @return boolean
     */
    @Override
    public boolean save(String content, String filename)
    {
        String defaultResultPath = "src/main/resources/java/";
        return this.save(content, filename, defaultResultPath);
    }

    /**
     *
     * @param content String
     * @param path String
     * @param filename String
     * @return boolean
     */
    private boolean save(String content, String filename, String path)
    {
        boolean saved = false;

        try
        {
            FileWriter fileWriter = new FileWriter(path + File.separatorChar + filename);
            BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
            bufferedWriter.write(content);
            bufferedWriter.close();
            saved = true;
        }
        catch(IOException ex)
        {
            System.out.println("Error writing to file '" + filename + "'");
        }

        return saved;
    }

    /**
     *
     * @param path String
     * @return String result
     */
    private String getContentFromPath(String path)
    {
        String result = "";

        try
        {
            File file = new File(path);
            FileInputStream inputStream = new FileInputStream(path);
            byte[] buffer = new byte[(int)file.length()];

            int total = 0;
            int nRead = 0;

            while((nRead = inputStream.read(buffer)) != -1) {
                result = result.concat(new String(buffer));
                total += nRead;
            }

            inputStream.close();
        }
        catch(FileNotFoundException ex)
        {
            System.out.println("Unable to open file '" + path + "'");
        }
        catch(IOException ex)
        {
            System.out.println("Error reading file '" + path + "'");
        }

        return result;
    }
}