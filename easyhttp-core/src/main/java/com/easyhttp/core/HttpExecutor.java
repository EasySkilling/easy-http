package com.easyhttp.core;

import com.easyhttp.core.entity.ExecuteParams;
import com.easyhttp.core.utils.CheckUtils;
import com.easyhttp.core.utils.LogicUtils;
import com.easyhttp.dep.enums.BodyForm;
import com.easyhttp.dep.utils.GsonParser;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
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

    /**
     * !openDebug就是安全的，不会抛出任何异常，从而保证不会crash，但是可能逻辑会有问题
     */
    private static boolean openDebug = false;

    public static void setOpenDebug(boolean openDebug) {
        HttpExecutor.openDebug = openDebug;
    }

    /**
     * 所有请求方式的工厂
     */
    private HashMap<BodyForm, IRequestFactory> requestFactories = new HashMap<>();

    {
        //do Json
        requestFactories.put(BodyForm.JSON, (params) -> {
            String json = GsonParser.createJson(params);
            if (CheckUtils.isEmpty(json)) {
                if (openDebug) throw new IllegalArgumentException("");
                    //这里最差的情况，让其不crash，即使不相应结果，也不能crash
                else return RequestBody.create("", MediaType.parse("application/json; charset=utf-8"));
            }
            return RequestBody.create(json, MediaType.parse("application/json; charset=utf-8"));
        });

        //do Form
        requestFactories.put(BodyForm.FORM, (params) -> {
            FormBody.Builder formBuilder = new FormBody.Builder();
            for (Map.Entry<String, Object> entry : params.entrySet()) {
                formBuilder.add(entry.getKey(), LogicUtils.string(entry.getValue()));
            }
            return formBuilder.build();
        });
    }

    // 单纯的数据访问
    public Call<T> execute(ExecuteParams executeParams, Map<String, Object> pathMap, Map<String, Object> urlMap, Map<String, Object> bodyMap, Type type) {

        if (executeParams == null) return null;

        BodyForm bodyForm = executeParams.getBodyForm();

        //check whether support the request
        if (!checkSupport(bodyForm)) return null;

        String url = generateHttpUrl(
                executeParams.getBaseUrl(),
                executeParams.getRestUrl(),
                executeParams.isUrlEncode(),
                pathMap,
                urlMap
        );
        System.out.println("execute --------- url = " + url);

        // 将传入的url参数拼接到url上
        HttpUrl.Builder urlBuilder = concatUrl(url, urlMap, executeParams.isUrlEncode());
        // 创建请求构建对象
        Request.Builder builder = new Request.Builder().url(urlBuilder.build());
        // 添加请求体数据
        RequestBody requestBody = requestFactories.get(bodyForm).create(bodyMap);
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

    private HttpUrl.Builder concatUrl(String url, Map<String, Object> urlMap, boolean encode) {
        HttpUrl.Builder urlParamsBuilder = HttpUrl.parse(url).newBuilder();
        if (urlMap != null && urlMap.size() > 0) {
            for (Map.Entry<String, Object> entry : urlMap.entrySet()) {
                String key = entry.getKey();
                Object value = entry.getValue();
                String valStr = value.toString();
                if (encode) {
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
        return urlParamsBuilder;
    }

    // 上传文件的新加方法


    /**
     * 检测是否支持此种请求
     */
    private boolean checkSupport(BodyForm form) {
        if (requestFactories.containsKey(form)) return true;
        if (openDebug) return false;
        throw new RuntimeException("只能支持表单数据form提交或者json对象提交！");
    }

    interface IRequestFactory {
        RequestBody create(Map<String, Object> params);
    }
}
