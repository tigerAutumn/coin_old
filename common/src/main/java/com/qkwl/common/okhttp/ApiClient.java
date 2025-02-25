package com.qkwl.common.okhttp;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.SerializationFeature;

import com.qkwl.common.huobi.request.*;
import com.qkwl.common.huobi.response.*;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.qkwl.common.okhttp.ApiClient.toQueryString;

/**
 * http client
 */
public class ApiClient {

  static final int CONN_TIMEOUT = 5;
  static final int READ_TIMEOUT = 5;
  static final int WRITE_TIMEOUT = 5;

  static final String API_HOST = "api.huobi.pro";

  static final String API_URL = "https://" + API_HOST;

  static final MediaType JSON = MediaType.parse("application/json");
  static final OkHttpClient client = createOkHttpClient();

  final String accessKeyId;
  final String accessKeySecret;
  final String assetPassword;


  public static void main(String args[]){
    OkHttpClient build = new OkHttpClient.Builder().build();
    ApiSignature apiSignature = new ApiSignature();
    String appKey = "BCAPP_LOGIN_4df5bde009073d3ef60da64d736724d6_A5F1C88A2B8F426BA941E829B9760A24";
    String appSecret = "423E6D4C80FA4C958C5F29D5D888E757";
    Map<String,String> params = new HashMap<>();
    params.put("token",appKey);
    params.put("userName","test我");
    params.put("userGender","test");
    params.put("userAge","test");
    apiSignature.createSignature(appKey,appSecret,"GET","testtest.hotcoin.top","/v1/user/test.html",params);
    try {
      //params.put("Signature",params.get("Signature")+"1");
      Response execute = build.newCall(new Request.Builder().url("https://testtest.hotcoin.top/v1/user/test.html" + "?" + toQueryString(params)).get().build()).execute();
      System.out.printf(execute.body().string());
    } catch (IOException e) {
      e.printStackTrace();
    }

  }


  /**
   * 创建一个ApiClient实例
   * 
   * @param accessKeyId AccessKeyId
   * @param accessKeySecret AccessKeySecret
   */
  public ApiClient(String accessKeyId, String accessKeySecret) {
    this.accessKeyId = accessKeyId;
    this.accessKeySecret = accessKeySecret;
    this.assetPassword = null;
  }

  /**
   * 创建一个ApiClient实例
   * 
   * @param accessKeyId AccessKeyId
   * @param accessKeySecret AccessKeySecret
   * @param assetPassword AssetPassword
   */
  public ApiClient(String accessKeyId, String accessKeySecret, String assetPassword) {
    this.accessKeyId = accessKeyId;
    this.accessKeySecret = accessKeySecret;
    this.assetPassword = assetPassword;
  }

  /**
   * 查询交易对
   * 
   * @return List of symbols.
   */
  public List<Symbol> getSymbols() {
    ApiResponse<List<Symbol>> resp =
        get("/v1/common/symbols", null, new TypeReference<ApiResponse<List<Symbol>>>() {});
    return resp.checkAndReturn();
  }

  /**
   * 查询所有账户信息
   * 
   * @return List of accounts.
   */
  public List<Account> getAccounts() {
    ApiResponse<List<Account>> resp =
        get("/v1/account/accounts", null, new TypeReference<ApiResponse<List<Account>>>() {});
    return resp.checkAndReturn();
  }

  /**
   * 创建订单（未执行)
   * 
   * @param request CreateOrderRequest object.
   * @return Order id.
   */
  public Long createOrder(CreateOrderRequest request) {
    ApiResponse<Long> resp =
        post("/v1/order/orders/place", request, new TypeReference<ApiResponse<Long>>() {});
    return resp.checkAndReturn();
  }

  /**
   * 执行订单
   * 
   * @param orderId The id of created order.
   * @return Order id.
   */
  public String placeOrder(long orderId) {
    ApiResponse<String> resp = post("/v1/order/orders/" + orderId + "/place", null,
        new TypeReference<ApiResponse<String>>() {});
    return resp.checkAndReturn();
  }

  /**
   *
   * 查询订单列表
   *
   * @param request
   * @return
   */
  public List<Order> queryOrderList(QueryOrderRequest request){
    ApiResponse<List<Order>> resp = get("/v1/order/orders", request.getRequestParams(),
            new TypeReference<ApiResponse<List<Order>>>() {});
    return resp.checkAndReturn();
  }

  /**
   * k线图
   * @param request
   * @return
   */
  public List<KLine> queryKLineList(QueryKLineRequest request){
    ApiResponse<List<KLine>> resp = get("/market/history/kline", request.getRequestParams(),
            new TypeReference<ApiResponse<List<KLine>>>() {});
    return resp.checkAndReturn();
  }

  /**
   * 获取聚合行情(Ticker)
   *
   * @param request
   * @return
   */
  public MarketDetailMerged queryMarketDetailMerged(QueryMarketDetailMergedRequest request){
    ApiResponseWithTick<MarketDetailMerged> resp = get("/market/detail/merged", request.getRequestParams(),
            new TypeReference<ApiResponseWithTick<MarketDetailMerged>>() {});
    return resp.checkAndReturn();
  }

  /**
   * 获取 Market Depth 数据
   *
   * @param request
   * @return
   */
  public MarketDepth queryMarketDepth(MarketDepthRequest request){
    ApiResponseWithTick<MarketDepth> resp = get("/market/depth", request.getRequestParams(),
            new TypeReference<ApiResponseWithTick<MarketDepth>>(){});
    return resp.checkAndReturn();
  }









  // send a GET request.
  <T> T get(String uri, Map<String, String> params, TypeReference<T> ref) {
    if (params == null) {
      params = new HashMap<>();
    }
    return call("GET", uri, null, params, ref);
  }

  // send a POST request.
  <T> T post(String uri, Object object, TypeReference<T> ref) {
    return call("POST", uri, object, new HashMap<String, String>(), ref);
  }

  // call huobi by endpoint.
  <T> T call(String method, String uri, Object object, Map<String, String> params,
      TypeReference<T> ref) {
    ApiSignature sign = new ApiSignature();
    sign.createSignature(this.accessKeyId, this.accessKeySecret, method, API_HOST, uri, params);
    try {
      Request.Builder builder = null;
      if ("POST".equals(method)) {
        RequestBody body = RequestBody.create(JSON, JsonUtil.writeValue(object));
        builder = new Request.Builder().url(API_URL + uri + "?" + toQueryString(params)).post(body);
      } else {
        builder = new Request.Builder().url(API_URL + uri + "?" + toQueryString(params)).get();
      }
      if (this.assetPassword != null) {
        builder.addHeader("AuthData", authData());
      }
      Request request = builder.build();
      Response response = client.newCall(request).execute();
      String s = response.body().string();
      return JsonUtil.readValue(s, ref);
    } catch (IOException e) {
      throw new ApiException(e);
    }
  }

  String authData() {
    MessageDigest md = null;
    try {
      md = MessageDigest.getInstance("MD5");
    } catch (NoSuchAlgorithmException e) {
      throw new RuntimeException(e);
    }
    md.update(this.assetPassword.getBytes(StandardCharsets.UTF_8));
    md.update("hello, moto".getBytes(StandardCharsets.UTF_8));
    Map<String, String> map = new HashMap<>();
    map.put("assetPwd", DatatypeConverter.printHexBinary(md.digest()).toLowerCase());
    try {
      return ApiSignature.urlEncode(JsonUtil.writeValue(map));
    } catch (IOException e) {
      throw new RuntimeException("Get json failed: " + e.getMessage());
    }
  }

  // Encode as "a=1&b=%20&c=&d=AAA"
  static String toQueryString(Map<String, String> params) {
    return String.join("&", params.entrySet().stream().map((entry) -> {
      return entry.getKey() + "=" + ApiSignature.urlEncode(entry.getValue());
    }).collect(Collectors.toList()));
  }

  // create OkHttpClient:
  static OkHttpClient createOkHttpClient() {
    return new OkHttpClient.Builder()
            .connectTimeout(CONN_TIMEOUT, TimeUnit.SECONDS)
        .readTimeout(READ_TIMEOUT, TimeUnit.SECONDS)
            .writeTimeout(WRITE_TIMEOUT, TimeUnit.SECONDS)
        .build();
  }

}


/**
 * API签名，签名规范：
 * 
 * http://docs.aws.amazon.com/zh_cn/general/latest/gr/signature-version-2.html
 * 
 * @author liaoxuefeng
 */
class ApiSignature {

  final Logger log = LoggerFactory.getLogger(getClass());

  static final DateTimeFormatter DT_FORMAT = DateTimeFormatter.ofPattern("uuuu-MM-dd'T'HH:mm:ss");
  static final ZoneId ZONE_GMT = ZoneId.of("Z");

  /**
   * 创建一个有效的签名。该方法为客户端调用，将在传入的params中添加AccessKeyId、Timestamp、SignatureVersion、SignatureMethod、Signature参数。
   * 
   * @param appKey AppKeyId.
   * @param appSecretKey AppKeySecret.
   * @param method 请求方法，"GET"或"POST"
   * @param host 请求域名，例如"be.huobi.com"
   * @param uri 请求路径，注意不含?以及后的参数，例如"/v1/huobi/info"
   * @param params 原始请求参数，以Key-Value存储，注意Value不要编码
   */
  public void createSignature(String appKey, String appSecretKey, String method, String host, String uri, Map<String, String> params) {
    StringBuilder sb = new StringBuilder(1024);
    sb.append(method.toUpperCase()).append('\n') // GET
        .append(host.toLowerCase()).append('\n') // Host
        .append(uri).append('\n'); // /path
    params.remove("Signature");
    params.put("AccessKeyId", appKey);
    params.put("SignatureVersion", "2");
    params.put("SignatureMethod", "HmacSHA256");
    params.put("Timestamp", String.valueOf(System.currentTimeMillis()/1000));
    // build signature:
    SortedMap<String, String> map = new TreeMap<>(params);
    for (Map.Entry<String, String> entry : map.entrySet()) {
      if (entry.getKey().equals("token"))continue;
      String key = entry.getKey();
      String value = entry.getValue();
      //不用进行urlencode
      //sb.append(key).append('=').append(urlEncode(value)).append('&');
      System.out.println("value = "+value);
      System.out.println("encode = "+urlEncode(value));
      sb.append(key).append('=').append(urlEncode(value)).append('&');
    }
    // remove last '&':
    sb.deleteCharAt(sb.length() - 1);
    // sign:
    Mac hmacSha256 = null;
    try {
      hmacSha256 = Mac.getInstance("HmacSHA256");
      SecretKeySpec secKey =
          new SecretKeySpec(appSecretKey.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
      hmacSha256.init(secKey);
    } catch (NoSuchAlgorithmException e) {
      throw new RuntimeException("No such algorithm: " + e.getMessage());
    } catch (InvalidKeyException e) {
      throw new RuntimeException("Invalid key: " + e.getMessage());
    }
    String payload = sb.toString();
    byte[] hash = hmacSha256.doFinal(payload.getBytes(StandardCharsets.UTF_8));
    String actualSign = Base64.getEncoder().encodeToString(hash);
    params.put("Signature", actualSign);
    if (log.isDebugEnabled()) {
      log.debug("Dump parameters:");
      for (Map.Entry<String, String> entry : params.entrySet()) {
        log.debug("  key: " + entry.getKey() + ", value: " + entry.getValue());
      }
    }
  }

  /**
   * 使用标准URL Encode编码。注意和JDK默认的不同，空格被编码为%20而不是+。
   * 
   * @param s String字符串
   * @return URL编码后的字符串
   */
  public static String urlEncode(String s) {
    try {
      return URLEncoder.encode(s, "UTF-8").replaceAll("\\+", "%20");
    } catch (UnsupportedEncodingException e) {
      throw new IllegalArgumentException("UTF-8 encoding not supported!");
    }
  }

  /**
   * Return epoch seconds
   */
  long epochNow() {
    return Instant.now().getEpochSecond();
  }

  String gmtNow() {
    return Instant.ofEpochSecond(epochNow()).atZone(ZONE_GMT).format(DT_FORMAT);
  }
}


class JsonUtil {

  public static String writeValue(Object obj) throws IOException {
    return objectMapper.writeValueAsString(obj);
  }

  public static <T> T readValue(String s, TypeReference<T> ref) throws IOException {
    return objectMapper.readValue(s, ref);
  }

  static final ObjectMapper objectMapper = createObjectMapper();

  static ObjectMapper createObjectMapper() {
    final ObjectMapper mapper = new ObjectMapper();
    mapper.setPropertyNamingStrategy(PropertyNamingStrategy.KEBAB_CASE);
    mapper.setSerializationInclusion(JsonInclude.Include.ALWAYS);
    // disabled features:
    mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
    mapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
    mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    return mapper;
  }



}
