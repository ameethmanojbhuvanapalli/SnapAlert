import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

public class DemoUpload {

	public static void uploadImage(String url, String imageFilePath) throws IOException {
	    // Create an HTTP client
	    HttpClient httpclient = HttpClients.createDefault();

	    // Create an HTTP POST request with the servlet endpoint URL
	    HttpPost httppost = new HttpPost(url);

	    // Create a File object from the image file path
	    File imageFile = new File(imageFilePath);

	    // Create a MultipartEntityBuilder to build the multipart/form-data body of the
	    // request
	    MultipartEntityBuilder builder = MultipartEntityBuilder.create();

	    // Add the image file as a binary body part
	    builder.addBinaryBody("file", new FileInputStream(imageFile), ContentType.APPLICATION_OCTET_STREAM,
	            imageFile.getName());

	    // Build the multipart/form-data entity
	    HttpEntity multipart = builder.build();

	    // Set the entity of the HTTP POST request
	    httppost.setEntity(multipart);

	    // Send the HTTP POST request and get the response
	    HttpResponse response = httpclient.execute(httppost);

	    // Get the response body
	    HttpEntity responseEntity = response.getEntity();
	    String responseBody = EntityUtils.toString(responseEntity);

	    // Print the response body
	    System.out.println("Response body: " + responseBody);

	    // Print a message indicating that the image was uploaded successfully
	    System.out.println("Image uploaded successfully!");
	}


	public static void main(String[] args) {
		// TODO Auto-generated method stub
		String url = "http://localhost:8080/Snap_Alert/UploadImage";
		String imageFilePath = "D:\\Wallpapers\\N5pbw8.jpg";
		try {
			uploadImage(url, imageFilePath);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
