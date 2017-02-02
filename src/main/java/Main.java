import com.heroku.api.HerokuAPI;
import com.heroku.api.Release;
import com.heroku.api.Slug;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.List;

public class Main {
  public static void main(String[] args) throws Exception {

    String herokuApiKey = System.getenv("HEROKU_API_TOKEN");
    String appName = System.getenv("APP_NAME");

    if (appName == null) {
      throw new IllegalArgumentException("No APP_NAME found!");
    }

    HerokuAPI api = new HerokuAPI(herokuApiKey);

    List<Release> releases = api.listReleases(appName);

    Release latestRelease = null;
    Integer i = releases.size();
    while (latestRelease == null) {
      i--;
      if (i < 0) throw new RuntimeException("No Slug found for " + appName + "!");
      Release r = releases.get(i);
      if (r.getSlug() != null) {
        latestRelease = r;
      }
    }

    System.out.println("Release: " + latestRelease.getVersion());

    Slug slug = api.getSlugInfo(appName, latestRelease.getSlug().getId());

    System.out.println("Downloading: " + slug.getBlob().getUrl());

    CloseableHttpClient httpclient = HttpClients.custom()
        .setSSLHostnameVerifier((s, sslSession) -> s.matches("^.*.s3.amazonaws.com$")).build();

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
