/*
 * All rights Reserved, Designed By baowei
 *
 * 注意：本内容仅限于内部传阅，禁止外泄以及用于其他的商业目的
 */

/**
 * @author yan.zhang
 * @date 2019/10/12 11:07
 */


import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.zip.GZIPInputStream;


public class TopUtil {
    public static String signTopRequest(Map<String, String> params, String secret, String signMethod) throws IOException {
        //检查参数是否已经排序
        String[] keys = params.keySet().toArray(new String[0]);
        Arrays.sort(keys);
        //把所有参数名和参数值串在一起
        StringBuilder query = new StringBuilder();
        if ("md5".equals(signMethod)) {
            query.append(secret);
        }
        for (String key : keys) {
            String value = params.get(key);
            if (isNotEmpty(key) && isNotEmpty(value)) {
                query.append(key).append(value);
            }
        }
        //使用MD5/HMAC加密
        byte[] bytes;
        if ("hmac".equals(signMethod)) {
            bytes = encryptHMAC(query.toString(), secret);
        } else {
            query.append(secret);
            bytes = encryptMD5(query.toString());
        }
        //把二进制转化为大写的十六进制
        return byte2hex(bytes);
    }

    private static byte[] encryptHMAC(String data, String secret) throws IOException {
        byte[] bytes = null;
        try {
            SecretKey secretKey = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacMD5");
            Mac mac = Mac.getInstance(secretKey.getAlgorithm());
            mac.init(secretKey);
            bytes = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
        } catch (GeneralSecurityException gse) {
            throw new IOException(gse.toString());
        }
        return bytes;
    }

    /**
     * 对字符串采用UTF-8编码后，用MD5进行摘要。
     */
    private static byte[] encryptMD5(String data) throws IOException {
        return encryptMD5(data.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 对字节流进行MD5摘要。
     */
    private static byte[] encryptMD5(byte[] data) throws IOException {
        byte[] bytes = null;
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            bytes = md.digest(data);
        } catch (GeneralSecurityException gse) {
            throw new IOException(gse.toString());
        }
        return bytes;
    }


    /**
     * 把字节流转换为十六进制表示方式。
     */
    private static String byte2hex(byte[] bytes) {
        StringBuilder sign = new StringBuilder();
        for (int i = 0; i < bytes.length; i++) {
            String hex = Integer.toHexString(bytes[i] & 0xFF);
            if (hex.length() == 1) {
                sign.append("0");
            }
            sign.append(hex.toUpperCase());
        }
        return sign.toString();
    }

    private static boolean isNotEmpty(String value) {
        int strLen;
        if (value == null || (strLen = value.length()) == 0) {
            return false;
        }
        for (int i = 0; i < strLen; i++) {
            if ((!Character.isWhitespace(value.charAt(i)))) {
                return true;
            }
        }
        return false;
    }

    public static String callApi(URL url, Map<String, String> params) throws IOException {
        String query = buildQuery(params);
        byte[] content = {};
        if (query != null) {
            content = query.getBytes(StandardCharsets.UTF_8);
        }
        HttpURLConnection conn = null;
        OutputStream out = null;
        String rsp = null;
        try {
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setDoInput(true);
            conn.setDoOutput(true);
            conn.setRequestProperty("Host", url.getHost());
            conn.setRequestProperty("Accept", "text/xml,text/javascript");
            conn.setRequestProperty("User-Agent", "top-sdk-java");
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded;charset=" + StandardCharsets.UTF_8);
            out = conn.getOutputStream();
            out.write(content);
            rsp = getResponseAsString(conn);
        } finally {
            if (out != null) {
                out.close();
            }
            if (conn != null) {
                conn.disconnect();
            }
        }
        return rsp;
    }

    private static String buildQuery(Map<String, String> params) throws IOException {
        if (params == null || params.isEmpty()) {
            return null;
        }
        StringBuilder query = new StringBuilder();
        Set<Map.Entry<String, String>> entries = params.entrySet();
        boolean hasParam = false;
        for (Map.Entry<String, String> entry : entries) {
            String name = entry.getKey();
            String value = entry.getValue();
            // 忽略参数名或参数值为空的参数
            if (isNotEmpty(name) && isNotEmpty(value)) {
                if (hasParam) {
                    query.append("&");
                } else {
                    hasParam = true;
                }
                query.append(name).append("=").append(URLEncoder.encode(value, "utf-8"));
            }
        }
        return query.toString();
    }

    private static String getResponseAsString(HttpURLConnection conn) throws IOException {
        String charset = getResponseCharset(conn.getContentType());
        if (conn.getResponseCode() < 400) {
            String contentEncoding = conn.getContentEncoding();
            if ("gzip".equalsIgnoreCase(contentEncoding)) {
                return getStreamAsString(new GZIPInputStream(conn.getInputStream()), charset);
            } else {
                return getStreamAsString(conn.getInputStream(), charset);
            }
        } else {// Client Error 4xx and Server Error 5xx
            throw new IOException(conn.getResponseCode() + " " + conn.getResponseMessage());
        }
    }

    private static String getStreamAsString(InputStream stream, String charset) throws IOException {
        try {
            Reader reader = new InputStreamReader(stream, charset);
            StringBuilder response = new StringBuilder();
            final char[] buff = new char[1024];
            int read = 0;
            while ((read = reader.read(buff)) > 0) {
                response.append(buff, 0, read);
            }

            return response.toString();
        } finally {
            if (stream != null) {
                stream.close();
            }
        }
    }

    private static String getResponseCharset(String ctype) {
        String charset = "utf-8";

        if (isNotEmpty(ctype)) {
            String[] params = ctype.split(";");
            for (String param : params) {
                param = param.trim();
                if (param.startsWith("charset")) {
                    String[] pair = param.split("=", 2);
                    if (pair.length == 2) {
                        if (isNotEmpty(pair[1])) {
                            charset = pair[1].trim();
                        }
                    }
                    break;
                }
            }
        }
        return charset;
    }


    public static void main(String[] args) throws UnsupportedEncodingException {
        System.out.println(URLEncoder.encode("测试", "utf-8"));
    }
}
