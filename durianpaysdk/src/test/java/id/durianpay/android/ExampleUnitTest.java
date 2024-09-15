package id.durianpay.android;

import android.content.Context;
import android.content.Intent;

import com.google.gson.JsonObject;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.io.IOException;

import id.durianpay.android.Interfaces.CheckoutResultListener;
import id.durianpay.android.model.DCheckoutOptions;
import id.durianpay.android.utils.Utils;
import id.durianpay.android.utils.WebResourceHelper;

import static id.durianpay.android.CheckoutActivity.MERCHANT_CONFIG_KEY;
import static io.sentry.marshaller.json.JsonMarshaller.ENVIRONMENT;
import static org.junit.Assert.*;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {

    @Mock
    Context mockContext;
    WebResourceHelper webResourceHelper = new WebResourceHelper();

    @Test
    public void loadUrl() throws Exception {
        DCheckoutOptions checkoutOptions = new DCheckoutOptions();
        checkoutOptions.setEnvironment("production");
        checkoutOptions.setLocale("en");
        checkoutOptions.setSiteName("MovieTicket");
        checkoutOptions.setCustomerId("cust001");
        checkoutOptions.setAccessToken("dp_liveXXXXX");
        checkoutOptions.setOrderId("ord_XXXXX");
        checkoutOptions.setAmount("10000");
        checkoutOptions.setCurrency("IDR");
        checkoutOptions.setCustomerEmail("johndoe@gmail.com");

        String options = checkoutOptions.toPostData();
        String matcher = "&access_key=dp_liveXXXXX&environment=production&locale=en&" +
        "site_name=MovieTicket&order_id=ord_XXXXX&amount=10000&currency=IDR&customer_id=cust001&customer_email=johndoe@gmail.com&sdk_source=dpay-android";

        {
            assertEquals(options, matcher);
        }
    }

    @Test
    public void loadUrl1() {
        DCheckoutOptions checkoutOptions = new DCheckoutOptions();
        checkoutOptions.setEnvironment("production");
        checkoutOptions.setLocale("en");
        checkoutOptions.setSiteName("MovieTicket");
        checkoutOptions.setCustomerEmail("johndoe@gmail.com");
        checkoutOptions.setCustomerGivenName("John Doe");
        checkoutOptions.setAccessToken("dp_liveXXXXX");
        checkoutOptions.setOrderId("ord_XXXXX");
        checkoutOptions.setAmount("10000");
        checkoutOptions.setCurrency("IDR");
        checkoutOptions.setCustomerID("cust_001");

        String options = null;
        try {
            options = checkoutOptions.toPostData();
        } catch (Exception e) {
            e.printStackTrace();
        }
        String matcher = "&access_key=dp_liveXXXXX&environment=production&locale=en&" +
                "site_name=MovieTicket&order_id=ord_XXXXX&amount=10000&currency=IDR&customer_id=cust_001&customer_email=johndoe@gmail.com&customer_given_name=John Doe&sdk_source=dpay-android";

        {
            assertEquals(options, matcher);
        }
    }

    @Test (expected = Exception.class)
    public void loadUrlAccessToken() throws Exception {
        DCheckoutOptions checkoutOptions = new DCheckoutOptions();
        checkoutOptions.setEnvironment("production");
        checkoutOptions.setLocale("en");
        checkoutOptions.setSiteName("MovieTicket");
        checkoutOptions.setCustomerId("cust001");
        checkoutOptions.setAccessToken("");
        checkoutOptions.setOrderId("ord_XXXXX");

        String options = checkoutOptions.toPostData();
        String matcher = "&access_key=dp_liveXXXXX&environment=production&locale=en&" +
                "site_name=MovieTicket&order_id=ord_XXXXX&customer_id=cust001";

        {
            assertEquals(options, matcher);
        }
    }

    @Test (expected = Exception.class)
    public void loadUrlOrderID() throws Exception {
        DCheckoutOptions checkoutOptions = new DCheckoutOptions();
        checkoutOptions.setEnvironment("production");
        checkoutOptions.setLocale("en");
        checkoutOptions.setSiteName("MovieTicket");
        checkoutOptions.setCustomerId("cust001");
        checkoutOptions.setAccessToken("dp_liveXXXXX");
        checkoutOptions.setOrderId("");

        String options = checkoutOptions.toPostData();
        String matcher = "&access_key=dp_liveXXXXX&environment=production&locale=en&" +
                "site_name=MovieTicket&order_id=ord_XXXXX&customer_id=cust001";

        {
            assertEquals(options, matcher);
        }
    }

    @Test (expected = Exception.class)
    public void loadUrlCustomerID() throws Exception {
        DCheckoutOptions checkoutOptions = new DCheckoutOptions();
        checkoutOptions.setEnvironment("production");
        checkoutOptions.setLocale("en");
        checkoutOptions.setSiteName("MovieTicket");
        checkoutOptions.setCustomerId("");
        checkoutOptions.setAccessToken("dp_liveXXXXX");
        checkoutOptions.setOrderId("ord_XXXXX");

        String options = checkoutOptions.toPostData();
        String matcher = "&access_key=dp_liveXXXXX&environment=production&locale=en&" +
                "site_name=MovieTicket&order_id=ord_XXXXX&customer_id=cust001";

        {
            assertEquals(options, matcher);
        }
    }

    @Test (expected = Exception.class)
    public void loadUrlCustomerEmailException() throws Exception {
        DCheckoutOptions checkoutOptions = new DCheckoutOptions();
        checkoutOptions.setEnvironment("production");
        checkoutOptions.setLocale("en");
        checkoutOptions.setSiteName("MovieTicket");
        checkoutOptions.setCustomerEmail("");
        checkoutOptions.setCustomerGivenName("John Doe");
        checkoutOptions.setAccessToken("dp_liveXXXXX");
        checkoutOptions.setOrderId("ord_XXXXX");

        String options = checkoutOptions.toPostData();

    }

    @Test (expected = Exception.class)
    public void loadUrlCustomerNameException() throws Exception{
        DCheckoutOptions checkoutOptions = new DCheckoutOptions();
        checkoutOptions.setEnvironment("production");
        checkoutOptions.setLocale("en");
        checkoutOptions.setSiteName("MovieTicket");
        checkoutOptions.setCustomerEmail("johndoe@gmail.com");
        checkoutOptions.setAccessToken("dp_liveXXXXX");
        checkoutOptions.setOrderId("ord_XXXXX");

        String options = checkoutOptions.toPostData();

        String matcher = "&access_key=dp_liveXXXXX&environment=production&locale=en&" +
                "site_name=MovieTicket&order_id=ord_XXXXX&customer_email=johndoe@gmail.com&customer_given_name=John Doe";

        {
            assertEquals(options, matcher);
        }
    }

    @Test
    public void loadUrlCustomerEmailName() {
        DCheckoutOptions checkoutOptions = new DCheckoutOptions();
        checkoutOptions.setEnvironment("production");
        checkoutOptions.setLocale("en");
        checkoutOptions.setSiteName("MovieTicket");
        checkoutOptions.setCustomerEmail("johndoe@gmail.com");
        checkoutOptions.setCustomerGivenName("John Doe");
        checkoutOptions.setAccessToken("dp_liveXXXXX");
        checkoutOptions.setOrderId("ord_XXXXX");
        checkoutOptions.setAmount("10000");
        checkoutOptions.setCurrency("IDR");
        checkoutOptions.setCustomerID("cus_XXX");

        String options = null;
        try {
            options = checkoutOptions.toPostData();
        } catch (Exception e) {
            e.printStackTrace();
        }
        String matcher = "&access_key=dp_liveXXXXX&environment=production&locale=en&" +
                "site_name=MovieTicket&order_id=ord_XXXXX&amount=10000&currency=IDR&customer_id=cus_XXX&" +
                "customer_email=johndoe@gmail.com&customer_given_name=John Doe&sdk_source=dpay-android";

        {
            assertEquals(options, matcher);
        }
    }

    @Test (expected = Exception.class)
    public void loadUrlLocale() throws Exception {
        DCheckoutOptions checkoutOptions = new DCheckoutOptions();
        checkoutOptions.setEnvironment("production");
        checkoutOptions.setLocale("e");
        checkoutOptions.setSiteName("MovieTicket");
        checkoutOptions.setCustomerId("cust001");
        checkoutOptions.setAccessToken("dp_liveXXXXX");
        checkoutOptions.setOrderId("");
        checkoutOptions.setAmount("10000");

        String options = checkoutOptions.toPostData();
        String matcher = "&access_key=dp_liveXXXXX&environment=production&locale=en&amount=10000" +
                "site_name=MovieTicket&order_id=ord_XXXXX&customer_id=cust001&sdk_source=dpay-android";

        {
            assertEquals(options, matcher);
        }
    }

    @Test (expected = Exception.class)
    public void loadUrlLocaleException() throws Exception {
        DCheckoutOptions checkoutOptions = new DCheckoutOptions();
        checkoutOptions.setEnvironment("production");
        checkoutOptions.setLocale("");
        checkoutOptions.setSiteName("MovieTicket");
        checkoutOptions.setCustomerId("cust001");
        checkoutOptions.setAccessToken("dp_liveXXXXX");
        checkoutOptions.setOrderId("");

        String options = checkoutOptions.toPostData();
        String matcher = "&access_key=dp_liveXXXXX&environment=production&locale=en&" +
                "site_name=MovieTicket&order_id=ord_XXXXX&customer_id=cust001&sdk_source=dpay-android";

        {
            assertEquals(options, matcher);
        }
    }


    @Test
    public void loadUrlCustomerIDEmail() {
        DCheckoutOptions checkoutOptions = new DCheckoutOptions();
        checkoutOptions.setEnvironment("production");
        checkoutOptions.setLocale("en");
        checkoutOptions.setSiteName("MovieTicket");
        checkoutOptions.setCustomerGivenName("John Doe");
        checkoutOptions.setAccessToken("dp_liveXXXXX");
        checkoutOptions.setOrderId("ord_XXXXX");
        checkoutOptions.setCustomerEmail("johndoe@gmail.com");
        checkoutOptions.setAmount("10000");
        checkoutOptions.setCurrency("IDR");
        checkoutOptions.setCustomerID("cus_XXX");

        String options = null;
        try {
            options = checkoutOptions.toPostData();
        } catch (Exception e) {
            e.printStackTrace();
        }
        String matcher = "&access_key=dp_liveXXXXX&environment=production&locale=en&" +
                "site_name=MovieTicket&order_id=ord_XXXXX&amount=10000&currency=IDR&customer_id=cus_XXX&" +
                "customer_email=johndoe@gmail.com&customer_given_name=John Doe&sdk_source=dpay-android";

        {
            assertEquals(options, matcher);
        }
    }

    @Test
    public void getChildDirTest() {
        String childDir = webResourceHelper.getChildDir("https://checkout.durianpay.id/static/img_ovo.png");
        String matcher = "image_data";
        {
            assertEquals(childDir, matcher);
        }
    }

    @Test
    public void getlocalFileNameTest() {
        String localFileName = WebResourceHelper.getLocalFileNameForUrl("https://checkout.durianpay.id/static/img_ovo.png");
        String matcher = "img_ovo.png";
        {
            assertEquals(localFileName, matcher);
        }
    }

    @Test
    public void getFileExtensionTest() {
        String fileExt = WebResourceHelper.getFileExt("https://checkout.durianpay.id/static/img_ovo.png");
        String matcher = "png";
        {
            assertEquals(fileExt, matcher);
        }
    }

    @Test
    public void getMimeTypeTest() {
        String fileExt = WebResourceHelper.getFileExt("https://checkout.durianpay.id/static/img_ovo.png");
        String mime = webResourceHelper.getMimeType(fileExt);
        String matcher = "image/png";
        {
            assertEquals(mime, matcher);
        }
    }
}