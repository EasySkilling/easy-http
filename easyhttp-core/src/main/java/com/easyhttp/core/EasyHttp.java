package com.easyhttp.core;

/**
 * 我未完成功能：
 * 自定义上传注解@UploadFile、@DownloadFile，与@Get，@Post方法区别使用，用于上传文件，这个稍后做。
 * 下面的请三蛋大佬来完成：
 * （1）优化okhttp部分模板代码，目前我只是实现了基本功能，没有考虑任何东西，麻烦三蛋大佬把okhttp文件上传的功能封装一下，我这只菜鸡好调用。
 * （2）EasyHttp全局可统一配置https，@Api注解参数可以单独配置自定义接口类的访问方式（http或https）。
 * （3）配置https时，可配置证书，如果没有配置证书，则使用默认的忽略证书的代码配置。
 * （4）可通过配置切换底层访问框架，默认使用okhttp，比如volly，httputils，apache-httpclient等，当前暂时不做，请三蛋大佬列入计划。
 * （5）其他的后面再拓展。
 */
public class EasyHttp {

    public static final boolean DEBUG = true;

}
