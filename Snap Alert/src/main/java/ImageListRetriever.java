import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public class ImageListRetriever {
    public static List<String> getImageList(String folderPath) {
        List<String> imageList = new ArrayList<>();
        File folder = new File(folderPath);
        File[] files = folder.listFiles();

        // Sort files by last modified timestamp
        Arrays.sort(files, Comparator.comparingLong(File::lastModified).reversed());

        for (File file : files) {
            if (file.isFile()) {
                String fileName = file.getName();
                if (fileName.endsWith(".jpg") || fileName.endsWith(".jpeg") || fileName.endsWith(".png")) {
                    imageList.add(fileName);
                }
            }
        }

        return imageList;
    }
}
