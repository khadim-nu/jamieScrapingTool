/**
 * @autor Khadim Raath
 */
package selenium;

import java.awt.Robot;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.DesiredCapabilities;
import java.awt.datatransfer.StringSelection;
import java.util.*;
import java.io.File;
import java.io.InputStream;

import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Start {

    public static void deleteListings(WebDriver driver) {
        driver.get("http://www.maltapark.com/mylistings.aspx");
        boolean listing = true;
        while (listing) {
            try {
                WebElement del = driver.findElement(By.className("del"));
                del.click();
            } catch (Exception e) {
                System.err.println(e.getMessage());
                listing = false;
            }
        }
    }

    public static void postListings(WebDriver driver, DAL connection) throws IOException, InterruptedException {
        String phone = "99781831";

        String[] params = connection.getConfigs();
        int pricePercentage = Integer.parseInt(params[0]);
        String section = params[1];
        String category = params[2];
        int wanted = 0;//params[3];
        String label = params[4];

        ResultSet items = connection.getItems(label);

        try {
            while (items.next()) {

                String title = items.getString("title");

                String desc = items.getString("description");
                String speci = items.getString("specification");

                title = cleanText(title);
                desc = cleanText(desc);
                speci = cleanText(speci);
                
                desc += "\n Note: Everything we sell is brand new and unopened with warranty, this means no negotiations on prices. Sometimes prices are incorrect due to changes on the market, in this case let us know and we can check for you.  Local pickup is not possible and our items take on average 4 days to reach the client.  You can make orders on the phone and pay cash when our delivery man delivers your item. please note that when you agree on a purchase on the phone you are committing to buying that specific item. We can only replace items that are broken and cannot refund items that you purchased which are in perfect working condition.For more information or invoices send us an Email on   : Jamsonmalta@gmail.com  Or call us on    9978 1831 From Mon - Sun 09:00 - 21:00 \n";

                String tprice = items.getString("price");
//                tprice = tprice.replaceAll("\\D+", "");

                tprice = tprice.replaceAll("EUR", "");
                tprice = tprice.trim();
                tprice = tprice.replace(".", "");
                tprice = tprice.replaceAll(",", ".");
                double price = Double.parseDouble(tprice);
                price = price + (price * pricePercentage / 100);

                driver.get("http://www.maltapark.com/addclassified.aspx?section=" + section + "&category=" + category + "&wanted=" + wanted);
                downloadImage(items.getString("image_url"));
                WebElement element6 = driver.findElement(By.name("ctl00$MainContent$txtItem"));
                element6.sendKeys(title);

                WebElement element7 = driver.findElement(By.name("ctl00$MainContent$txtDescription"));
                element7.sendKeys(desc);
                element7.sendKeys(speci);

                driver.findElement(By.name("ctl00$MainContent$txtItem")).clear();
                driver.findElement(By.name("ctl00$MainContent$txtItem")).sendKeys(title);

                driver.findElement(By.name("ctl00$MainContent$txtContactTel")).sendKeys(phone);
                driver.findElement(By.name("ctl00$MainContent$txtSellingPrice")).sendKeys("" + price);

                try {
                    driver.findElement(By.name("ctl00$MainContent$rbItemCondition")).click();
                } catch (Exception e) {
                }

                //driver.findElement(By.name("ctl00$MainContent$cbIsOnlineSale")).click();
                WebElement prev = driver.findElement(By.name("ctl00$MainContent$btnPreview"));
                prev.click();

                String captchaValue = "";
                Captcha pro = new Captcha(driver);
                captchaValue = pro.solveCaptcha();
                captchaValue = captchaValue.toUpperCase(Locale.ENGLISH);
                System.out.println("Captcha key: " + captchaValue);

                if (!captchaValue.isEmpty()) {
                    JavascriptExecutor js = (JavascriptExecutor) driver;
                    WebElement captcha = driver.findElement(By.id("txtCaptcha"));
                    js.executeScript("arguments[0].setAttribute('value', '" + captchaValue + "')", captcha);

                    driver.findElement(By.name("ctl00$MainContent$cbAgree")).click();
                    WebElement submit = driver.findElement(By.name("ctl00$MainContent$btnSubmit"));
                    submit.click();
                    try {
                        // thread to sleep for 1000 milliseconds
                        Thread.sleep(1000);
                    } catch (Exception e) {
                        System.out.println(e);
                    }
                    uploadPhoto(driver);
                    connection.postedSuccess(items.getInt("id"));
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(Start.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static String getImages(WebDriver driver) {
//        a-button-thumbnail
        String images = "";
        try {
            images = driver.findElement(By.id("miniATF_image")).getAttribute("src");

        } catch (Exception e) {
        }
        return images;
    }

    public static String cleanText(String txt) {
        String cleanedTxt = txt;
        try {
            cleanedTxt = cleanedTxt.replaceAll("amazon", "");
            cleanedTxt = cleanedTxt.replaceAll("Amazon", "");
            cleanedTxt = cleanedTxt.replaceAll("AMAZON", "");
            cleanedTxt = cleanedTxt.replaceAll("uk", "");
            cleanedTxt = cleanedTxt.replaceAll("Uk", "");
            cleanedTxt = cleanedTxt.replaceAll("UK", "");
            cleanedTxt = cleanedTxt.replaceAll("co", "");
            cleanedTxt = cleanedTxt.replaceAll("Co", "");
            cleanedTxt = cleanedTxt.replaceAll("CO", "");
            cleanedTxt = cleanedTxt.replaceAll("united kingdom", "");
            cleanedTxt = cleanedTxt.replaceAll("United Kingdom", "");
            cleanedTxt = cleanedTxt.replaceAll("returns", "");
            cleanedTxt = cleanedTxt.replaceAll("Return", "");
            cleanedTxt = cleanedTxt.replaceAll("policy", "");
            cleanedTxt = cleanedTxt.replaceAll("Policy", "");
            cleanedTxt = cleanedTxt.replaceAll("ASIN", "");
        } catch (Exception e) {
        }
        return cleanedTxt;

    }

    public static String getDesc(WebDriver driver) {
        String desc = "";
        try {
            desc = driver.findElement(By.id("featurebullets_feature_div")).findElement(By.tagName("ul")).getText();

        } catch (Exception e) {
            try {
                desc = driver.findElement(By.id("iframeContent")).getText();
            } catch (Exception e1) {
            }
        }
        return desc;
    }

    public static String getTechDesc(WebDriver driver) {
        String desc = "";
        try {
            desc = driver.findElement(By.className("techD")).getText();
        } catch (Exception e) {
            try {
                desc = driver.findElement(By.id("detail_bullets_id")).getText();
                desc = desc.split("Average Customer")[0];
            } catch (Exception e1) {
            }
        }
        return desc;
    }

    public static void itemDetails(WebDriver driver, DAL connection, String itemUrl, String category, String label) throws IOException, InterruptedException {
        try {

            String itemId = driver.findElement(By.id("ASIN")).getAttribute("value");
            String title = driver.findElement(By.id("productTitle")).getText();
            String images = getImages(driver);
            String desc = getDesc(driver);
            String techDesc = getTechDesc(driver);

            try {
                WebElement cartBtn = driver.findElement(By.id("add-to-cart-button"));
                cartBtn.click();
            } catch (Exception e) {
                WebElement cartBtn = driver.findElement(By.className("pa_mbc_on_amazon_offer")).findElement(By.tagName("a"));
                cartBtn.click();
            }
            try {
                driver.get("https://www.amazon.co.uk/gp/buy/spc/handlers/display.html?hasWorkingJavascript=1");
                Thread.sleep(5000);
                System.out.println("PageLoaded");
                String pricetxt = driver.findElement(By.className("order-summary-tfx-grand-total-stressed")).getText();
                System.out.println("ptext:" + pricetxt);

                pricetxt = pricetxt.replaceAll("Payment Total:", "");
                System.out.println("price:" + pricetxt);
                connection.saveItem(itemId, title, pricetxt, itemUrl, images, desc, techDesc, category, label);
                System.out.println("Item added");
            } catch (Exception e) {
                System.out.println("checkout excepton: " + itemUrl);
            }
        } catch (NoSuchElementException e) {
        }
    }

    public static void scrapeAMazon(WebDriver driver, DAL connection) throws IOException {
        driver.get("https://www.amazon.co.uk");

        WebElement element = driver.findElement(By.id("nav-link-yourAccount"));
        element.click();

        driver.findElement(By.name("email")).sendKeys("jamieellulbonici@gmail.com");
        driver.findElement(By.name("password")).sendKeys("benson71");
        driver.findElement(By.id("signInSubmit")).click();
        try {
            //    Thread.sleep(30000);
            //  driver.findElement(By.name("email")).sendKeys("jamieellulbonici@gmail.com");
            //driver.findElement(By.name("password")).sendKeys("benson71");
            //driver.findElement(By.id("signInSubmit")).click();
        } catch (Exception e) {

        }
        String[] params = connection.getSearchParams();

        driver.get(params[0]);
        //driver.findElement(By.className("nav-input")).click();

        // go to pages
        for (int i = 0; i < 1; i++) {
            List<WebElement> adsList = driver.findElements(By.className("s-access-detail-page"));

            System.out.println("Page " + i + " Size:" + adsList.size());
            ArrayList<String> adsLink = new ArrayList<String>();

            for (WebElement ad : adsList) {
                String adUrl = ad.getAttribute("href");
                //   System.out.println(adUrl);
                adsLink.add(adUrl);
            }
            for (Iterator<String> iterator = adsLink.iterator(); iterator.hasNext();) {
                String link = iterator.next();
                deleteBasketItems(driver);
                driver.get(link);
                try {
                    itemDetails(driver, connection, link, params[0], params[1]);
                } catch (Exception e) {
                }

            }
            //driver.findElement(By.id("pagnNextString")).click();
        }
    }

    private static void deleteBasketItems(WebDriver driver) {
        driver.get("https://www.amazon.co.uk/gp/cart/view.html/ref=nav_cart");
        List<WebElement> cartItems = driver.findElement(By.id("sc-active-cart")).findElements(By.className("sc-action-delete"));
        for (WebElement cartItem : cartItems) {
            cartItem.click();
        }
    }

    public static void downloadImage(String sourceUrl) {
        String imgName = "C:/Users/jamie/Downloads/aaa.jpg";
        try {
            File file = new File(imgName);

            file.delete();
        } catch (Exception e) {

        }

        try (InputStream in = new URL(sourceUrl).openStream()) {
            Files.copy(in, Paths.get(imgName));
        } catch (Exception e) {

        }
    }

    public static void main(String[] args) throws Exception {

        String imagesDest = "/home/raath/NetBeansProjects/jamieTool/images/";

        DAL connection = new DAL();

        boolean scrape = true;
        if (args.length > 0) {
            scrape = false;
        }

        System.setProperty("webdriver.chrome.driver", "chromedriver");

        DesiredCapabilities caps = DesiredCapabilities.chrome();

        ChromeOptions options = new ChromeOptions();
        Map<String, Object> prefs = new HashMap<String, Object>();
        prefs.put("download.default_directory", "/cap");
        options.setExperimentalOption("prefs", prefs);
        options.addArguments(Arrays.asList("--start-maximized", "--test-type",
                "--ignore-certificate-errors", "--disable-popup-blocking",
                "--allow-running-insecure-content", "--disable-translate",
                "--always-authorize-plugins"));
        caps.setCapability(ChromeOptions.CAPABILITY, options);

        WebDriver driver = new ChromeDriver(caps);

        if (scrape) {
            scrapeAMazon(driver, connection);
        } else {

            ////
            driver.get("https://www.maltapark.com/login.aspx");
            WebElement element = driver.findElement(By.name("ctl00$MainContent$txtUsername"));
            element.sendKeys("jamsone");

            WebElement element1 = driver.findElement(By.name("ctl00$MainContent$txtPassword"));
            element1.sendKeys("jamie0497");

            WebElement element2 = driver.findElement(By.name("ctl00$MainContent$btnLogin"));
            element2.click();

//        deleteListings(driver);
            postListings(driver, connection);

//             uploadPhoto(driver);
        }
        System.out.println("Done.");
        // driver.quit();
    }

    public static void uploadPhoto(WebDriver driver) throws InterruptedException {
        try {
            try {
                driver.findElement(By.id("cbDoNotShow")).click();
                driver.findElement(By.id("btnWarningOK")).click();
            } catch (Exception e) {
            }
            //ref: http://learn-automation.com/upload-file-in-selenium-webdriver-using-robot-class/

            //StringSelection sel = new StringSelection("C:/Users/jamie/Downloads/");
            //  Toolkit.getDefaultToolkit().getSystemClipboard().setContents(sel, null);
            //System.out.println("selection" + sel);
            // System.out.println("start upload");
            //  driver.get("http://www.freepdfconvert.com/pdf-word");
            //   DesiredCapabilities caps = DesiredCapabilities.chrome();
            //   StringSelection sel = new StringSelection("aaa.jpg");
            driver.findElement(By.id("plup_browse")).click();
            Robot robot = new Robot();
            try {
                // thread to sleep for 1000 milliseconds
                Thread.sleep(1000);
            } catch (Exception e) {
                System.out.println(e);
            }
            robot.keyPress(KeyEvent.VK_A);
            robot.keyRelease(KeyEvent.VK_A);

            robot.keyPress(KeyEvent.VK_A);
            robot.keyRelease(KeyEvent.VK_A);

            robot.keyPress(KeyEvent.VK_A);
            robot.keyRelease(KeyEvent.VK_A);

            try {
                // thread to sleep for 1000 milliseconds
                Thread.sleep(1000);
            } catch (Exception e) {
                System.out.println(e);
            }

            robot.keyPress(KeyEvent.VK_ENTER);
            robot.keyRelease(KeyEvent.VK_ENTER);
            try {
                Thread.sleep(1000);
            } catch (Exception e) {
                System.out.println(e);
            }
            robot.keyPress(KeyEvent.VK_ENTER);
            robot.keyRelease(KeyEvent.VK_ENTER);
            try {
                Thread.sleep(10000);
            } catch (Exception e) {
                System.out.println(e);
            }
        } catch (Exception er) {
            System.out.println(er);
        }

    }

}
