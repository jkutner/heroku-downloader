import com.heroku.api.HerokuAPI;
import com.heroku.api.Slug;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

public class Main {
  public static void main(String[] args) throws Exception {

    String herokuApiKey = System.getenv("HEROKU_API_TOKEN");
    String patchSlugId = System.getenv("PATCH_SLUG_ID");

    if (patchSlugId == null) {
      System.out.println("No PATCH_SLUG_ID found. Skipping.");
      return;
    }

    HerokuAPI api = new HerokuAPI(herokuApiKey);

    Slug slug = api.getSlugInfo("murmuring-sands-88689", patchSlugId);

    System.out.println("Downloading: " + slug.getBlob().getUrl());

    CloseableHttpClient httpclient = HttpClients.createDefault();
    HttpGet get = new HttpGet(slug.getBlob().getUrl());
    CloseableHttpResponse response = httpclient.execute(get);

    InputStream is = response.getEntity().getContent();
    FileOutputStream fos = new FileOutputStream(new File("slug.tgz"));
    int count = 0;
    int inByte;
    while((inByte = is.read()) != -1) {
      fos.write(inByte);
      if (count > 100000) {
        System.out.print(".");
        count = 0;
      } else {
        count++;
      }
    }
    is.close();
    fos.close();

    // TODO unpack the tar ball

    // TODO launch the Java process
  }
}
