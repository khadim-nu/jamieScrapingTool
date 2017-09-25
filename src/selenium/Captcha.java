package selenium;

import java.awt.AWTException;
import java.awt.Robot;
import java.awt.event.KeyEvent;
import java.io.File;
import java.net.MalformedURLException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.openqa.selenium.Cookie;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;

public class Captcha {

    // just look at your cookie's content (e.g. using browser)
// and import these settings from it
//    private static final String key = "89c8ed74db9d544e9ed6f336701cadcd";
    private static final String key = "7f37cbc40690f3921a559b8f5595f8b5";
    private static final String SESSION_COOKIE_NAME = "JSESSIONID";
    private static final String DOMAIN = "http://www.maltapark.com/";
    private static final String COOKIE_PATH = "C://cap";
//    private static final String img_fn = "C://Users/jamie eb/Downloads/captcha.png";
    private static final String img_fn = "C:/Users/jamie/Downloads/captcha.png";
    WebDriver driver;
    
    public Captcha(WebDriver driver) {
        this.driver = driver;
    }
    
    public void setup() throws Exception {
        
        driver.manage().window().maximize();
        driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
        //   driver.get("http://only-testing-blog.blogspot.in/2014/09/selectable.html");
    }
    
    public void deleteImage() {
        try {
            
            File file = new File(img_fn);
            
            if (file.delete()) {
                System.out.println(file.getName() + " is deleted!");
            } else {
                System.out.println("Delete operation is failed.");
            }
            
        } catch (Exception e) {
            e.printStackTrace();
            
        }
    }
    
    public void Save_Image() throws IOException, InterruptedException, AWTException {
        deleteImage();
        //Locate Image
        WebElement Image = driver.findElement(By.id("captchaCtrl_image"));
        //Rihgt click on Image using contextClick() method.
        Actions action = new Actions(driver);
        action.contextClick(Image).build().perform();

        //To perform press Ctrl + v keyboard button action.
        action.sendKeys(Keys.CONTROL, "v").build().perform();
        
        Thread.sleep(3000);
        
        Robot robot = new Robot();
        robot.keyPress(KeyEvent.VK_DOWN);
        robot.keyRelease(KeyEvent.VK_DOWN);
        robot.keyPress(KeyEvent.VK_DOWN);
        robot.keyRelease(KeyEvent.VK_DOWN);
        
        robot.keyPress(KeyEvent.VK_SHIFT);
        robot.keyPress(KeyEvent.VK_SEMICOLON);
        robot.keyRelease(KeyEvent.VK_SHIFT);
        // To press Save button.
        robot.keyPress(KeyEvent.VK_ENTER);
        robot.delay(5000);
        robot.keyPress(KeyEvent.VK_ENTER);
        robot.keyRelease(KeyEvent.VK_ENTER);
        robot=new Robot();
        
    }
    
    protected boolean isResourceAvailableByUrl(String resourceUrl) {
        HttpClient httpClient = new DefaultHttpClient();
        HttpContext localContext = new BasicHttpContext();
        BasicCookieStore cookieStore = new BasicCookieStore();
        // apply jsessionid cookie if it exists
        cookieStore.addCookie(getSessionCookie());
        localContext.setAttribute(ClientContext.COOKIE_STORE, cookieStore);
        // resourceUrl - is url which leads to image
        HttpGet httpGet = new HttpGet(resourceUrl);
        
        try {
            org.apache.http.HttpResponse httpResponse = httpClient.execute(httpGet, localContext);
            System.out.println("code: " + httpResponse.getStatusLine().getStatusCode());
            return true;//httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK;
        } catch (IOException e) {
            return false;
        }
    }
    
    protected BasicClientCookie getSessionCookie() {
        
        Cookie originalCookie = driver.manage().getCookieNamed(SESSION_COOKIE_NAME);
        
        if (originalCookie == null) {
            return null;
        }

        // just build new apache-like cookie based on webDriver's one
        String cookieName = originalCookie.getName();
        String cookieValue = originalCookie.getValue();
        BasicClientCookie resultCookie = new BasicClientCookie(cookieName, cookieValue);
        resultCookie.setDomain(DOMAIN);
        resultCookie.setExpiryDate(originalCookie.getExpiry());
        resultCookie.setPath(COOKIE_PATH);
        return resultCookie;
    }
    
    public String solveCaptcha() throws MalformedURLException, IOException {
        
        try {
            setup();
            Save_Image();
            Thread.sleep(3000);
            ApiResult ret = BypassCaptchaApi.Submit(key, img_fn);
            if (!ret.IsCallOk) {
                System.out.println("Error: " + ret.Error);
                return "";
            }
            
            String value = ret.DecodedValue;
//        System.out.println("Using the decoded value: " + value);
//        System.out.println("Suppose it is correct.");
            ret = BypassCaptchaApi.SendFeedBack(key, ret, true);
            if (!ret.IsCallOk) {
                System.out.println("Error: " + ret.Error);
                return "";
            }
            
            ret = BypassCaptchaApi.GetLeft(key);
            if (!ret.IsCallOk) {
                System.out.println("Error: " + ret.Error);
                return "";
            }

//        System.out.println("There are " + ret.LeftCredits + " credits left on this key");
//        System.out.println("OK");
            return value;
        } catch (Exception e) {
        }
        return "";
    }
    
}
