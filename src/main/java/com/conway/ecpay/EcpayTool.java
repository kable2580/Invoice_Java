package com.conway.ecpay;

import lombok.extern.slf4j.Slf4j;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.UUID;

/** Created by Conway on 2019-12-24 */
@Slf4j
public class EcpayTool {

  private static final String HASH_KEY = "ejCk326UnaZWKisg";
  private static final String HASH_IV = "q9jcZX8Ib9LM8wYk";
  private static final String INVOICE_ISSUE_URL =
      "https://einvoice-stage.ecpay.com.tw/Invoice/Issue";

  private static final char[] HEX_ARRAY = "0123456789ABCDEF".toCharArray();

  // 合作特店編號
  private static final String MERCHANT_ID = "2000132";

  private static OkHttpClient okHttpClient = new OkHttpClient();

  public static void main(String[] args) {
    EcpayTool ecpayTool = new EcpayTool();

    EcpayInvoiceIssueObj ecpayInvoiceIssueObj = new EcpayInvoiceIssueObj();
    ecpayInvoiceIssueObj.setCustomerName("王曉明");
    ecpayInvoiceIssueObj.setCustomerAddr("台灣 ");
    ecpayInvoiceIssueObj.setCustomerEmail("conway@test.com");
    ecpayInvoiceIssueObj.setPrint("1");
    ecpayInvoiceIssueObj.setDonation("0");
    ecpayInvoiceIssueObj.setTaxType("1");
    ecpayInvoiceIssueObj.setSalesAmount("145");
    ecpayInvoiceIssueObj.setInvoiceRemark("測試");
    ecpayInvoiceIssueObj.setItemName("物品一|物品二");
    ecpayInvoiceIssueObj.setItemCount("2|1");
    ecpayInvoiceIssueObj.setItemWord("單位1|單位二");
    ecpayInvoiceIssueObj.setItemPrice("45|55");
    ecpayInvoiceIssueObj.setItemTaxType("|");
    ecpayInvoiceIssueObj.setItemAmount("90|55");
    ecpayInvoiceIssueObj.setItemRemark("備註1|備註2");
    ecpayInvoiceIssueObj.setInvType("07");
    ecpayInvoiceIssueObj.setVat("1");

    ecpayTool.issue(ecpayInvoiceIssueObj);
  }

  public void issue(EcpayInvoiceIssueObj ecpayInvoiceIssueObj) {
    try {

      // using tree map because we need values to be sorted
      TreeMap<String, String> requestParams = new TreeMap<>();

      // generate timestamp
      requestParams.put("TimeStamp", Long.toString(Instant.now().getEpochSecond()));
      requestParams.put("MerchantID", MERCHANT_ID);

      // generate relate number
      // todo: change this generator
      String relateNumber = UUID.randomUUID().toString().replace("-", "").substring(0, 30);
      requestParams.put("RelateNumber", relateNumber);

      requestParams.put("CustomerID", ecpayInvoiceIssueObj.getCustomerId());
      requestParams.put("CustomerIdentifier", ecpayInvoiceIssueObj.getCustomerIdentifier());
      requestParams.put("CustomerName", dotNetUrlEncode(ecpayInvoiceIssueObj.getCustomerName()));
      requestParams.put("CustomerAddr", dotNetUrlEncode(ecpayInvoiceIssueObj.getCustomerAddr()));
      requestParams.put("CustomerPhone", ecpayInvoiceIssueObj.getCustomerPhone());
      requestParams.put("CustomerEmail", dotNetUrlEncode(ecpayInvoiceIssueObj.getCustomerEmail()));
      requestParams.put("ClearanceMark", ecpayInvoiceIssueObj.getClearanceMark());
      requestParams.put("Print", ecpayInvoiceIssueObj.getPrint());
      requestParams.put("Donation", ecpayInvoiceIssueObj.getDonation());
      requestParams.put("LoveCode", ecpayInvoiceIssueObj.getLoveCode());
      requestParams.put("CarruerType", ecpayInvoiceIssueObj.getCarruerType());
      requestParams.put("CarruerNum", ecpayInvoiceIssueObj.getCarruerNum());
      requestParams.put("TaxType", ecpayInvoiceIssueObj.getTaxType());
      requestParams.put("SalesAmount", ecpayInvoiceIssueObj.getSalesAmount());
      requestParams.put("InvoiceRemark", dotNetUrlEncode(ecpayInvoiceIssueObj.getInvoiceRemark()));
      requestParams.put("ItemName", dotNetUrlEncode(ecpayInvoiceIssueObj.getItemName()));
      requestParams.put("ItemCount", ecpayInvoiceIssueObj.getItemCount());
      requestParams.put("ItemWord", dotNetUrlEncode(ecpayInvoiceIssueObj.getItemWord()));
      requestParams.put("ItemPrice", ecpayInvoiceIssueObj.getItemPrice());
      requestParams.put("ItemTaxType", ecpayInvoiceIssueObj.getItemTaxType());
      requestParams.put("ItemAmount", ecpayInvoiceIssueObj.getItemAmount());
      requestParams.put("ItemRemark", dotNetUrlEncode(ecpayInvoiceIssueObj.getItemRemark()));
      requestParams.put("InvType", ecpayInvoiceIssueObj.getInvType());
      requestParams.put("vat", ecpayInvoiceIssueObj.getVat());

      // compute checkMacValue
      String checkMacValue = generateCheckMacValue(HASH_KEY, HASH_IV, requestParams);
      requestParams.put("CheckMacValue", checkMacValue);

      // post
      FormBody.Builder formBuilder = new FormBody.Builder();
      requestParams.forEach(formBuilder::addEncoded);
      FormBody formBody = formBuilder.build();

      Request request =
          new Request.Builder()
              .url(INVOICE_ISSUE_URL)
              .addHeader("Content-Type", "application/x-www-form-urlencoded")
              .post(formBody)
              .build();

      Response response = okHttpClient.newCall(request).execute();

      log.info(response.body().string());

    } catch (IOException | NoSuchAlgorithmException e) {
      log.error(e.toString());
    }
  }

  public static String generateCheckMacValue(
      String hashKey, String hashIV, SortedMap<String, String> params)
      throws NoSuchAlgorithmException, UnsupportedEncodingException {

    // create query string, ignoring unneeded elements
    StringBuilder stringBuilder = new StringBuilder();
    for (Map.Entry<String, String> entry : params.entrySet()) {
      String key = entry.getKey();

      // checkMacValue should not be inside params, but still check to avoid errors
      if (key.equals("InvoiceRemark")
          || key.equals("ItemName")
          || key.equals("ItemWord")
          || key.equals("ItemRemark")
          || key.equals("CheckMacValue")) continue;
      stringBuilder.append(key).append("=").append(entry.getValue());
      if (!key.equals(params.lastKey())) stringBuilder.append("&");
    }

    // add HashKey and HashIV
    String queryString =
        "HashKey=" + hashKey + "&" + stringBuilder.toString() + "&HashIV=" + hashIV;

    String encoded = dotNetUrlEncode(queryString);

    // convert to lower case
    encoded = encoded.toLowerCase();
    return computeMD5Hash(encoded).toUpperCase();
  }

  public static String computeMD5Hash(String plainText) throws NoSuchAlgorithmException {
    MessageDigest md = MessageDigest.getInstance("MD5");
    byte[] digest = md.digest(plainText.getBytes());
    return byteArrayToHexString(digest);
  }

  private static String byteArrayToHexString(byte[] bytes) {
    char[] hexChars = new char[bytes.length * 2];
    for (int j = 0; j < bytes.length; j++) {
      int v = bytes[j] & 0xFF;
      hexChars[j * 2] = HEX_ARRAY[v >>> 4];
      hexChars[j * 2 + 1] = HEX_ARRAY[v & 0x0F];
    }
    return new String(hexChars);
  }

  /** .NET url encoding */
  public static String dotNetUrlEncode(String text) throws UnsupportedEncodingException {
    return URLEncoder.encode(text, StandardCharsets.UTF_8.toString())
        .replace("%2d", "-")
        .replace("%5f", "_")
        .replace("%2e", ".")
        .replace("%21", "!")
        .replace("%2a", "*")
        .replace("%28", "(")
        .replace("%29", ")")
        .replace("%20", "+");
  }
}
