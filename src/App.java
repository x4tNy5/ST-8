import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class App {

    public static void main(String[] args) throws IOException, InterruptedException {
        Path root = Paths.get(".").toAbsolutePath().normalize();
        Path resultDir = root.resolve("result");
        Files.createDirectories(resultDir);

        Map<String, Object> prefs = new HashMap<>();
        prefs.put("download.default_directory", resultDir.toString());
        prefs.put("download.prompt_for_download", false);
        
        ChromeOptions options = new ChromeOptions();
        options.setExperimentalOption("prefs", prefs);

        WebDriver webDriver = new ChromeDriver(options);
        webDriver.get("http://www.papercdcase.com/");

        List<String> data = Files.readAllLines(root.resolve("data/data.txt"));
        webDriver.findElement(By.xpath("//input[@name='artist']")).sendKeys(data.get(0));
        webDriver.findElement(By.xpath("//input[@name='title']")).sendKeys(data.get(1));

        int n = 1;
        for (int i = 2; i < data.size() && n <= 18; i++) {
            String line = data.get(i).trim();
            if (!line.isEmpty()) {
                webDriver.findElement(By.xpath("//input[@name='track" + n + "']")).sendKeys(line);
                n++;
            }
        }

        webDriver.findElements(By.xpath("//input[@name='template']")).get(1).click();
        webDriver.findElements(By.xpath("//input[@name='size']")).get(1).click();

        WebElement btn = webDriver.findElement(By.xpath("//input[@name='submit']"));
        btn.submit();

        Path pdf = resultDir.resolve("papercdcase.pdf");
        Path out = resultDir.resolve("cd.pdf");
        int wait = 0;
        while (!Files.exists(pdf) && wait < 30) {
            Thread.sleep(1000);
            wait++;
        }
        if (Files.exists(pdf)) {
            Files.move(pdf, out, StandardCopyOption.REPLACE_EXISTING);
         } else {
            try (java.io.InputStream in = java.net.URI.create(webDriver.getCurrentUrl()).toURL().openStream()) {
                Files.copy(in, out, StandardCopyOption.REPLACE_EXISTING);
            }
        }

        webDriver.quit();
    }
}
