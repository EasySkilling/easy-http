package com.easyhttp.core;

import com.easyhttp.core.entity.ExecuteParams;
import com.easyhttp.core.enums.BodyForm;
import com.easyhttp.core.utils.GsonParser;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.FormBody;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

public class HttpExecutor<T> {

    // 单纯的数据访问
    public Call<T> execute(ExecuteParams executeParams, Map<String, Object> pathMap, Map<String, Object> urlMap, Map<String, Object> bodyMap, Type type) {
        String url = generateHttpUrl(
                executeParams.getBaseUrl(),
                executeParams.getRestUrl(),
                executeParams.isUrlEncode(),
                pathMap,
                urlMap
        );
        // 将传入的url参数拼接到url上
        HttpUrl.Builder urlParamsBuilder = HttpUrl.parse(url).newBuilder();
        if (urlMap != null && urlMap.size() > 0) {
            for (Map.Entry<String, Object> entry : urlMap.entrySet()) {
                String key = entry.getKey();
                Object value = entry.getValue();
                String valStr = value.toString();
                if (executeParams.isUrlEncode()) {
                    try {
                        valStr = URLEncoder.encode(valStr, "UTF-8");
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                        throw new RuntimeException("参数UrlEncode失败:" + e.getLocalizedMessage());
                    }
                }
                urlParamsBuilder.addQueryParameter(key, valStr);
            }
        }
        // 创建请求构建对象
        Request.Builder builder = new Request.Builder()
                .url(urlParamsBuilder.build());
        // 添加请求体数据
        RequestBody requestBody = null;
        if (bodyMap != null && bodyMap.size() > 0) {
            // 数据提交方式
            BodyForm bodyForm = executeParams.getBodyForm();
            if (BodyForm.FORM == bodyForm) {
                FormBody.Builder formBuilder = new FormBody.Builder();
                for (Map.Entry<String, Object> entry : bodyMap.entrySet()) {
                    formBuilder.add(entry.getKey(), entry.getValue().toString());
                }
                requestBody = formBuilder.build();
            } else if (BodyForm.JSON == bodyForm) {
                String json = GsonParser.createJson(bodyMap);
                if (json != null) {
                    requestBody = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), json);
                }
            } else {
                throw new RuntimeException("只能支持表单数据form提交或者json对象提交！");
            }
        }
        String httpMethod = executeParams.getHttpMethod();
        builder.method(httpMethod, requestBody);

        return new Call<T>().setType(type).newOkHttpCall(new OkHttpClient().newCall(builder.build()));
    }

    private String generateHttpUrl(String baseUrl, String restUrl, boolean urlEncode,
                                   Map<String, Object> pathMap, Map<String, Object> urlMap){
        // 最终要返回的url值
        String url;
        if (restUrl == null || restUrl.trim().equals("")) {
            url = baseUrl;
        } else {
            String newRestUrl = parseRestUrl(restUrl, pathMap);
            // 严格解析，并按照规则拼接url
            // (1)如果baseUrl是否以/结尾
            boolean baseUrlHasEndSeparator = baseUrl.endsWith("/");
            // (2)restUrl是否以/开头
            boolean restUrlHasStartSeparator = newRestUrl.startsWith("/");
            if (baseUrlHasEndSeparator) {
                if (restUrlHasStartSeparator) {
                    url = baseUrl.substring(0, baseUrl.length() - 1) + newRestUrl;
                } else {
                    url = baseUrl + newRestUrl;
                }
            } else {
                if (restUrlHasStartSeparator) {
                    url = baseUrl + newRestUrl;
                } else {
                    url = baseUrl + "/" + newRestUrl;
                }
            }
        }
        // 在url后面拼接参数，如果有参数，url以/结尾则需要先将/去掉
//        if (urlMap != null && urlMap.size() > 0) {
//            if (url.endsWith("/")) {
//                url = url.substring(0, url.length() - 1);
//            }
//            StringBuilder urlBuilder = new StringBuilder(url + "?");
//            for (Map.Entry<String, Object> entry : urlMap.entrySet()) {
//                String key = entry.getKey();
//                Object value = entry.getValue();
//                //
//                String valStr = value.toString();
//                if (urlEncode) {
//                    valStr = URLEncoder.encode(value.toString(), "UTF-8");
//                }
//                urlBuilder.append(key).append("=").append(valStr).append("&");
//            }
//            urlBuilder.deleteCharAt(urlBuilder.length() - 1);
//            return urlBuilder.toString();
//        }
        return url;
    }

    private static String parseRestUrl(String restUrl, Map<String, Object> map) {
        // {xxx}的正则表达式
        String pattern = "\\{[^\\}]+\\}";
        StringBuilder matchUrl = new StringBuilder(restUrl);
        Matcher matcher = Pattern.compile(pattern).matcher(matchUrl);
        // 用于保存url中{}中的变量名称，也就是map中对应数据的key值
        List<String> args = new ArrayList<>();
        while (matcher.find()) {
            String matchStr = matcher.group();
            int start = matcher.start();
            int end = matcher.end();
            String argName = matchStr.replace("{", "").replace("}", "").trim();
            args.add(argName);
            matchUrl.replace(start, end, "$");
            matcher = Pattern.compile(pattern).matcher(matchUrl);
        }
        int argsSize = args.size();
        if (argsSize == 0) {
            return matchUrl.toString();
        }
        if (map == null) {
            throw new RuntimeException("url中对应参数变量没有定义，无法解析！");
        }
        String[] split = matchUrl.toString().split("\\$");
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < split.length; i++) {
            String str = split[i];
            result.append(str);
            if (i < argsSize) {
                String key = args.get(i);
                if (!map.containsKey(key)) {
                    throw new RuntimeException("url中对应的参数" + key + "没有定义，无法解析！");
                }
                Object value = map.get(key);
                if (value != null) {
                    result.append(value.toString());
                }
            }
        }
        return result.toString();
    }


    // 上传文件的新加方法

}
