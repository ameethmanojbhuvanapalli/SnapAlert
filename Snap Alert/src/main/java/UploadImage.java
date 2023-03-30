import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;

@WebServlet("/UploadImage")
@MultipartConfig
public class UploadImage extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private final String UPLOAD_DIR = "C:\\Users\\Work\\eclipse-workspace\\Snap Alert\\src\\main\\webapp\\images";

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // Create a directory for storing uploaded files if it doesn't exist
        Path uploadPath = Paths.get(UPLOAD_DIR);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        // Get all parts of the multipart request
        Collection<Part> parts = request.getParts();

        // Process each part of the multipart request
        for (Part part : parts) {
            String fileName = getFileName(part);

            if (fileName != null) {
                // Extract and save the uploaded file with a timestamp in the name
                String timeStamp = new SimpleDateFormat("yyyyMMddHHmmssSSS").format(new Date());
                String newFileName = "Snap-Alert-" + timeStamp + "." + getFileExtension(fileName);

                InputStream fileContent = part.getInputStream();
                Files.copy(fileContent, uploadPath.resolve(newFileName));
                fileContent.close();
            }
        }

        // Send response back to client
        response.getWriter().println("Image uploaded successfully!");
    }

    // Extracts file name from the Content-Disposition header of the part
    private String getFileName(Part part) {
        String contentDispositionHeader = part.getHeader("content-disposition");
        if (contentDispositionHeader != null && contentDispositionHeader.contains("filename")) {
            String[] parts = contentDispositionHeader.split(";");
            for (String partInfo : parts) {
                if (partInfo.trim().startsWith("filename")) {
                    return partInfo.substring(partInfo.indexOf('=') + 1).trim().replace("\"", "");
                }
            }
        }
        return null;
    }

    // Extracts file extension from the file name
    private String getFileExtension(String fileName) {
        int dotIndex = fileName.lastIndexOf('.');
        return (dotIndex == -1) ? "" : fileName.substring(dotIndex + 1);
    }
}

