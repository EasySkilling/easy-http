package com.easyhttp.core.utils;

public class GenerateRules {

    private static final String AUTOWIRED_PREFIX = "EasyAutowired$$";
    private static final String API_PREFIX = "EasyApi$$";
    private static final String CLASS_PKG = "com.easyhttp.generate";

    // 生成类的名称
    public static String generateEasyApiClazzName(String originApiName) {
        return API_PREFIX + originApiName;
    }

    // 生成类的包路径
    public static String generateEasyApiClazzNamePkg() {
        return CLASS_PKG;
    }

    // 根据原始类，获取生成类的Class对象
    public static Class<?> getGenerateApiClazz(Class<?> api) throws ClassNotFoundException {
        return getGenerateApiClazz(api.getSimpleName());
    }

    public static Class<?> getGenerateApiClazz(String originApiName) throws ClassNotFoundException {
        String easyApiName = generateEasyApiClazzNamePkg() + "." + generateEasyApiClazzName(originApiName);
        return Class.forName(easyApiName);
    }

    public static String generateEasyAutowiredClazzName(String fieldHostFullName, String fieldParamName) {
        return AUTOWIRED_PREFIX + fieldHostFullName.replace(".", "$") + "$$" + fieldParamName;
//        return AUTOWIRED_PREFIX + fieldHostFullName.replace(".", "") + fieldParamName;
    }
}
