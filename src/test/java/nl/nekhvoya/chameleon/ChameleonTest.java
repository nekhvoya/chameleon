package nl.nekhvoya.chameleon;

import com.codeborne.selenide.Selenide;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.OutputType;

import java.util.Base64;

import static com.codeborne.selenide.Condition.clickable;
import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.open;
import static com.codeborne.selenide.WebDriverRunner.getWebDriver;

public class ChameleonTest {
    @Test
    public void googleCooliesTest() {
        open("https://www.google.com/maps");
        getWebDriver().manage().window().maximize();

        String screenshotAsBase64 = Selenide.screenshot(OutputType.BASE64);
        byte[] screenshot = Base64.getDecoder().decode(screenshotAsBase64);

        Chameleon.saveScreenshot(screenshot, "Google Cookies page opens");
    }

    @Test
    public void googleWeatherTest() {
        open("https://www.google.com/search?q=weather");
        getWebDriver().manage().window().maximize();

        $(By.xpath("//button//div[contains(text(), 'accept')]")).shouldBe(clickable).click();

        String screenshotAsBase64 = Selenide.screenshot(OutputType.BASE64);
        byte[] screenshot = Base64.getDecoder().decode(screenshotAsBase64);

        Chameleon.saveScreenshot(screenshot, "Google weather search loads results");
    }

    @AfterAll
    public static void runAnalysis() {
        Chameleon.compare(true);
    }
}
