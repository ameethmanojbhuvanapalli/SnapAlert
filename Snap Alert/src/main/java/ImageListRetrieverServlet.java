

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;

@WebServlet("/ImageListRetrieverServlet")
public class ImageListRetrieverServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String folderPath = "C:\\Users\\Work\\eclipse-workspace\\Snap Alert\\src\\main\\webapp\\images"; // Change this to the path of your image directory
        List<String> imageList = ImageListRetriever.getImageList(folderPath);

        Gson gson = new Gson();
        String json = gson.toJson(imageList);

        response.setContentType("application/json");
        PrintWriter out = response.getWriter();
        out.print(json);
        out.flush();
    }
}
