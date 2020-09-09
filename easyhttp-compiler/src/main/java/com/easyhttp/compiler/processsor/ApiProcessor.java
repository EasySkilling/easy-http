package com.easyhttp.compiler.processsor;

import com.easyhttp.compiler.entity.AnnotationSpecWrapper;
import com.easyhttp.compiler.entity.ParamAttrsBox;
import com.easyhttp.compiler.utils.AnnotationUtils;
import com.easyhttp.compiler.utils.ParamsAttrsUtils;
import com.easyhttp.compiler.utils.TipsUtils;
import com.easyhttp.core.Call;
import com.easyhttp.core.HttpExecutor;
import com.easyhttp.core.annotations.Api;
import com.easyhttp.core.annotations.methods.Delete;
import com.easyhttp.core.annotations.methods.Get;
import com.easyhttp.core.annotations.methods.Patch;
import com.easyhttp.core.annotations.methods.Post;
import com.easyhttp.core.annotations.methods.Put;
import com.easyhttp.core.annotations.params.BodyField;
import com.easyhttp.core.annotations.params.BodyMap;
import com.easyhttp.core.annotations.params.UrlField;
import com.easyhttp.core.annotations.params.UrlMap;
import com.easyhttp.core.annotations.paths.PathField;
import com.easyhttp.core.entity.ExecuteParams;
import com.easyhttp.core.enums.BodyForm;
import com.easyhttp.core.enums.SupportAnnotation;
import com.easyhttp.core.utils.ClazzUtils;
import com.easyhttp.core.utils.GenerateRules;
import com.easyhttp.core.utils.GsonParser;
import com.easyhttp.core.utils.RegexUtils;
import com.google.auto.service.AutoService;
import com.google.gson.reflect.TypeToken;
import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;

@AutoService(Processor.class)
@SupportedSourceVersion(SourceVersion.RELEASE_8)
@SupportedAnnotationTypes({
        "com.easyhttp.core.annotations.Api"
})
public class ApiProcessor extends AbsProcessor {

    @Override
    void handleAnnotations(RoundEnvironment roundEnvironment) {
        Set<? extends Element> apiElements = roundEnvironment.getElementsAnnotatedWith(Api.class);
        if (apiElements != null && !apiElements.isEmpty()) {
            for (Element apiElement : apiElements) {
                // 类的完整名称
                String fullClazzName = ((TypeElement)apiElement).getQualifiedName().toString();
                // 判断是否是interface，如果不是interface报错
                boolean anInterface = apiElement.getKind().isInterface();
                if (!anInterface) {
                    printError(fullClazzName + "必须是一个interface！注解@Api只能用于interface之上！");
                }
                Api api = apiElement.getAnnotation(Api.class);
                // 生成类
                generateEasyApiClass((TypeElement)apiElement, api.baseUrl());
            }
        }
    }

    private void generateEasyApiClass(TypeElement apiElement, String baseUrl) {
        // TODO 判断Url是否正确，这里可以使用正则判断，正则表达式怎么拼写？o(╥﹏╥)o，三蛋搞定！！！！
        if (!checkUrl(baseUrl)) {
            printError(TipsUtils.UrlInvalidMsg());
        }
        // // 这里先简单判断是否为空
        // if (baseUrl == null || baseUrl.equals("")) {
        //     printError("@Api注解上请填写正确的url地址");
        // }
        // 使用Api注解节点的包路径名称
        String pkgName = elementUtils.getPackageOf(apiElement).toString();
        // 使用Api注解节点的类名称
        String clazzName = apiElement.getSimpleName().toString();
        // 使用Api注解要生成的类文件名称
        String easyApiClazzName = GenerateRules.generateEasyApiClazzName(clazzName);
        // 控制台打印
        printMsg("使用注解@Api的类：" + clazzName);
        printMsg("新生成的类：" + easyApiClazzName);
        // 获取所有的内部成员
        List<? extends Element> elements = apiElement.getEnclosedElements();
        // 创建方法
        Set<MethodSpec> methodSpecs = parseMethods(apiElement, elements, baseUrl);
        // 创建类
        TypeSpec.Builder clazzBuilder = TypeSpec.classBuilder(easyApiClazzName)
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .addSuperinterface(apiElement.asType());
        if (!methodSpecs.isEmpty()) {
            clazzBuilder.addMethods(methodSpecs);
        }
        // 创建文件
        JavaFile javaFile = JavaFile.builder(GenerateRules.generateEasyApiClazzNamePkg(), clazzBuilder.build()).build();
        try {
            // 输出文件
            javaFile.writeTo(filer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Set<MethodSpec> parseMethods(TypeElement apiElement, List<? extends Element> elements, String baseUrl) {
        Set<MethodSpec> set = new HashSet<>();
        // 获取所有方法
        List<ExecutableElement> executableElements = ElementFilter.methodsIn(elements);
        if (!executableElements.isEmpty()) {
            for (ExecutableElement method : executableElements) {
                MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder(method.getSimpleName().toString());
                // 添加方法注解（因为是重写方法，都有overide暂时不写）
//                builder.addAnnotation(Override.class);
                List<AnnotationSpecWrapper> methodAnnotationSpecList = getAnnotationSpecs(method.getAnnotationMirrors());
                // 定义的方法必须包含EasyHttp的方法注解，且只能有一个，否则是无效的方法，不应该出现在生成的实现类中
                if (!AnnotationUtils.hasOnlyOneEasyHttpAnnotation(methodAnnotationSpecList)) {
                    printError("方法" + method.getSimpleName().toString() + "有且只能有一个EasyHttp中修饰方法的注解！");
                }
                // 添加方法注解
                for (AnnotationSpecWrapper spec : methodAnnotationSpecList) {
                    methodBuilder.addAnnotation(spec.getSpec());
                }
                // 添加modifier
                methodBuilder.addModifiers(Modifier.PUBLIC);
                // 添加返回值（方法的返回值必须是Call或者Call的子类，否则不成立）
                TypeMirror returnType = method.getReturnType();
                printMsg("方法返回类型：" + returnType.toString());
                // 判断returnType是否是Call的子类或者Call
                if (!ClazzUtils.isSubOrSameClazz(elementUtils, typeUtils, messager, returnType, Call.class)) {
                    printError("方法" + method.getSimpleName() + "的返回值必须是" + Call.class.getCanonicalName() + "类型或者子类型！");
                }
                methodBuilder.returns(TypeName.get(returnType));
                // 添加方法参数
                List<? extends VariableElement> methodParameters = method.getParameters();
                // 封装方法参数各个属性的集合对象
                List<ParamAttrsBox> paramAttrsBoxList = new ArrayList<>();
                if (!methodParameters.isEmpty()) {
                    printMsg("*****************方法" + method.getSimpleName() + "参数是否合法判定，starting******************");
                    for (VariableElement param : methodParameters) {
                        // 添加方法参数
                        ParameterSpec.Builder paramBuilder = ParameterSpec.builder(
                                TypeName.get(param.asType()),
                                param.getSimpleName().toString()
                        );

                        // 参数的注解
                        List<AnnotationSpecWrapper> annotationSpecList = getAnnotationSpecs(param.getAnnotationMirrors());
                        if (annotationSpecList.size() > 0) {
                            // 获取参数类型
                            TypeMirror paramType = param.asType();
                            TypeKind kind = paramType.getKind();
                            printMsg("");
                            printMsg("开始解析参数" + param.getSimpleName().toString() + "......");
                            // 如果一个参数使用的EasyHttp的参数注解超过1个，则不符合规则报错
                            if (AnnotationUtils.isMoreThanOneEasyHttpParamAnnotation(annotationSpecList)) {
                                printError("参数" + param.getSimpleName().toString() + "有且只能有一个EasyHttp参数注解");
                            }
                            //
                            printMsg("clazz类型:" + paramType.toString());
                            printMsg("kind类型：" + kind.name());
                            StringBuilder annoSb = new StringBuilder();
                            // 如果是基本数据类型，或者String
                            boolean isString = paramType.toString().equals(String.class.getCanonicalName());
                            boolean isPrimitiveWithStr = kind.isPrimitive() || isString;
                            for (AnnotationSpecWrapper spec : annotationSpecList) {
                                annoSb.append(spec.getSpec().toString()).append(", ");
                                // 参数使用注解规则：
                                // （1）UrlField只能用于基本数据类型和String。
                                // （2）UrlMap只能用于自定义对象和Map对象
                                SupportAnnotation supportAnnotation = spec.getAnnotation();
                                switch (supportAnnotation) {
                                    case P_URL_FIELD:
                                    case P_BODY_FIELD:
                                        if (!isPrimitiveWithStr) {
                                            printError("注解@UrlField只能用于基本数据类型和String类型的参数！");
                                        }
                                        break;
                                    case P_URL_MAP:
                                    case P_BODY_MAP:
                                        // 只支持Map和自定义的JavaBean，因此Kind必须是DECLARED，并且要排除String
                                        if (TypeKind.DECLARED != kind) {
                                            printError("注解@UrlMap只支持Map和自定义JavaBean对象！");
                                        }
                                        if (isPrimitiveWithStr) {
                                            printError("注解@UrlMap只支持Map和自定义JavaBean对象！");
                                        }
                                        // 如果是Iterable子集不满足条件
                                        if (ClazzUtils.isSubOrSameClazz(elementUtils, typeUtils, messager, paramType, Iterable.class)) {
                                            printError("注解@UrlMap只支持Map和自定义JavaBean对象！");
                                        }
                                        // 设置当前参数是否属于Map集合类型，后面会用到
                                        spec.setMapClazz(ClazzUtils.isSubOrSameClazz(elementUtils, typeUtils, messager, paramType, Map.class));
                                        break;
                                    case P_PATH_FIELD:
                                        // url拼接路径，只支持基本数据类型和String
                                        break;
                                    default:
                                }
                                paramBuilder.addAnnotation(spec.getSpec());
                            }
                            printMsg("注解有：" + annoSb.toString());
                        } else {
                            // 如果参数没有框架内部的注解，则无法识别，直接报错
                            printError("方法" + method.getSimpleName() + "的参数" + param.getSimpleName().toString() + "必须包含EasyHttp的注解，否则无法识别！");
                        }
                        // 创建参数属性封装盒子对象，这里由于上一步已经判断了不能多于两个EasyHttp注解，因此这里直接获取索引为0的注解即可
                        AnnotationSpecWrapper annotationSpecWrapper = annotationSpecList.get(0);
                        paramAttrsBoxList.add(new ParamAttrsBox(
                                param.getSimpleName().toString(),
                                annotationSpecWrapper.getAnnotation(),
                                annotationSpecWrapper.getAnnotationAttrs(),
                                annotationSpecWrapper.isMapClazz()
                        ));
                        // 方法添加参数
                        methodBuilder.addParameter(paramBuilder.build());
                    }
                    printMsg("***************方法" + method.getSimpleName() + "参数是否合法判定，ending******************");
                }

                // 添加抛出异常
                List<? extends TypeMirror> thrownTypes = method.getThrownTypes();
                if (thrownTypes != null && thrownTypes.size() > 0) {
                    for (TypeMirror thrownType : thrownTypes) {
                        methodBuilder.addException(TypeName.get(thrownType));
                    }
                }
                // 添加方法内容
                List<ParamAttrsBox> pathFieldBoxes = null;
                List<ParamAttrsBox> urlFieldBoxes = null;
                List<ParamAttrsBox> urlMapBoxes = null;
                List<ParamAttrsBox> bodyFieldBoxes = null;
                List<ParamAttrsBox> bodyMapBoxes = null;
                if (paramAttrsBoxList.size() > 0) {
                    // 如果参数大于0，则需要对参数进行处理
                    // （1）先处理PathField，拼接url，如果有，则需要记录起来，重新后面计算方法注解的url
                    pathFieldBoxes = ParamsAttrsUtils.getPathFields(paramAttrsBoxList, messager);
                    // （2）处理UrlField
                    urlFieldBoxes = ParamsAttrsUtils.getUrlFields(paramAttrsBoxList, messager);
                    // （3）处理UrlMap
                    urlMapBoxes = ParamsAttrsUtils.getUrlMaps(paramAttrsBoxList);
                    // （4）处理BodyField
                    bodyFieldBoxes = ParamsAttrsUtils.getBodyFields(paramAttrsBoxList, messager);
                    // （5）处理BodyMap
                    bodyMapBoxes = ParamsAttrsUtils.getBodyMaps(paramAttrsBoxList);
                }
                // 生成获取url的代码
                boolean hasPathFieldData = pathFieldBoxes != null && pathFieldBoxes.size() > 0;
                String pathMapName = "pathMapDatas";
                if (hasPathFieldData) {
                    methodBuilder.addStatement("$T<String, Object> " + pathMapName + " = new $T<String, Object>()", Map.class, HashMap.class);
                    for (ParamAttrsBox box : pathFieldBoxes) {
                        Object keyValue = box.getAnnotationAttrs().get("key");
                        methodBuilder.addStatement(pathMapName + ".put($S, $L)", keyValue, box.getName());
                    }
                }
                boolean hasUrlFields = urlFieldBoxes != null && urlFieldBoxes.size() > 0;
                boolean hasUrlMaps = urlMapBoxes != null && urlMapBoxes.size() > 0;
                boolean hasUrlMapData = hasUrlFields || hasUrlMaps;
                String urlMapName = "urlMapDatas";
                if (hasUrlMapData) {
                    // 添加一行注释
                    methodBuilder.addComment("拼接到Url部分参数处理");
                    // 将所有的UrlField和urlMapBoxes参数组合成一个Map<String,Object>对象
                    methodBuilder.addStatement("$T<String, Object> " + urlMapName + " = new $T<String, Object>()", Map.class, HashMap.class);
                    if (hasUrlFields) {
                        for (ParamAttrsBox box : urlFieldBoxes) {
                            Object keyValue = box.getAnnotationAttrs().get("key");
                            methodBuilder.addStatement(urlMapName + ".put($S, $L)", keyValue, box.getName());
                        }
                    }
                    // UrlMap注解的参数要通过生成反射代码去获取对象的所有属性
                    if (hasUrlMaps) {
                        for (ParamAttrsBox box : urlMapBoxes) {
                            String name = box.getName();
                            // 拼接反射属性代码
                            // 根据Map和JavaBean进行拼接
                            if (box.isMapClazz()) {
                                // 直接将Map集合添加到urlMapDatas集合中
                                methodBuilder.addStatement("$L.putAll($L)", urlMapName, name);
                            } else {
                                // 通过Gson将avaBean转换成Map<String, Object>
                                methodBuilder.addStatement(
                                        "$T<String, Object> $LToMap = $T.parseBeanToMap($L)",
                                        Map.class,
                                        name,
                                        GsonParser.class,
                                        name
                                );
                                CodeBlock.Builder blockBuilder = CodeBlock.builder();
                                blockBuilder.beginControlFlow("if ($LToMap != null)", name);
                                blockBuilder.add(CodeBlock.of("$L.putAll($LToMap);", urlMapName, name));
                                blockBuilder.endControlFlow();
                                methodBuilder.addCode(blockBuilder.build());
                            }
                        }
                    }
                }
                boolean hasBodyFields = bodyFieldBoxes != null && bodyFieldBoxes.size() > 0;
                boolean hasBodyMaps = bodyMapBoxes != null && bodyMapBoxes.size() > 0;
                boolean hasBodyMapData = hasBodyFields || hasBodyMaps;
                String bodyMapName = "bodyMapDatas";
                if (hasBodyMapData) {
                    // 添加一行注释
                    methodBuilder.addComment("Body部分参数处理");
                    // 将所有的BodyField和BodyMap参数组合成一个Map<String,Object>对象
                    methodBuilder.addStatement("$T<String, Object> " + bodyMapName + " = new $T<String, Object>()", Map.class, HashMap.class);
                    if (hasBodyFields) {
                        for (ParamAttrsBox box : bodyFieldBoxes) {
                            Object keyValue = box.getAnnotationAttrs().get("key");
                            methodBuilder.addStatement(bodyMapName + ".put($S, $L)", keyValue, box.getName());
                        }
                    }
                    // BodyMap注解的参数要通过生成反射代码去获取对象的所有属性
                    if (hasBodyMaps) {
                        for (ParamAttrsBox box : bodyMapBoxes) {
                            String name = box.getName();
                            // 拼接反射属性代码
                            // 根据Map和JavaBean进行拼接
                            if (box.isMapClazz()) {
                                // 直接将Map集合添加到urlMapDatas集合中
                                methodBuilder.addStatement("$L.putAll($L)", bodyMapName, name);
                            } else {
                                // 通过Gson将avaBean转换成Map<String, Object>
                                methodBuilder.addStatement(
                                        "$T<String, Object> $LToMap = $T.parseBeanToMap($L)",
                                        Map.class,
                                        name,
                                        GsonParser.class,
                                        name
                                );
                                CodeBlock.Builder blockBuilder = CodeBlock.builder();
                                blockBuilder.beginControlFlow("if ($LToMap != null)", name);
                                blockBuilder.add(CodeBlock.of("$L.putAll($LToMap);", bodyMapName, name));
                                blockBuilder.endControlFlow();
                                methodBuilder.addCode(blockBuilder.build());
                            }
                        }
                    }
                }
                //
                // 根据返回值类型决定是否需要添加返回对象，由于这里规则必须要返回Call以及Call的子类，因此必须要返回
                // 先获取当前方法的EasyHttp注解
                SupportAnnotation methodAnno = null;
                String restUrl = null;
                boolean urlEncode = false;
                BodyForm bodyForm = BodyForm.FORM;
                for (int i = 0; i < methodAnnotationSpecList.size(); i++) {
                    AnnotationSpecWrapper wrapper = methodAnnotationSpecList.get(i);
                    if (AnnotationUtils.isSupportMethodAnnotation(wrapper.getAnnotation())) {
                        methodAnno = wrapper.getAnnotation();
                        Map<String, Object> annotationAttrs = wrapper.getAnnotationAttrs();
                        if (annotationAttrs.containsKey("url")) {
                            restUrl = (String) annotationAttrs.get("url");
                        }
                        if (annotationAttrs.containsKey("urlEncode")) {
                            urlEncode = (boolean) annotationAttrs.get("urlEncode");
                        }
                        if (annotationAttrs.containsKey("form")) {
                            bodyForm = (BodyForm) annotationAttrs.get("form");
                        }
                        break;
                    }
                }
                // 创建ExecuteParams对象
                String executeParamsName = "executeParams";
                methodBuilder.addStatement(
                        "$T " + executeParamsName + " = new $T($S, $S, $S, $L, $T.$L)",
                        ExecuteParams.class,
                        ExecuteParams.class,
                        baseUrl,
                        restUrl,
                        methodAnno.getPName(),
                        urlEncode,
                        BodyForm.class,
                        bodyForm
                );
                // 获取返回类型的内部类型，也就是泛型集合
                List<String> innerList = ClazzUtils.getInnerGenericClazzNameList(returnType.toString(), messager);
                // 如果innerList集合长度为0，则代表泛型为Object，不处理
                String innerStr = "";
                if (innerList.size() > 0) {
                    for (int i = innerList.size() - 1; i >= 0; i--) {
                        String innerClazzName = innerList.get(i);
                        // 从内往外拼接
                        innerStr = "<" + innerClazzName + innerStr + ">";
                    }
                } else {
                    // 添加为Object
                    innerStr = "<Object>";
                }
                printMsg("innerStr >>> " + innerStr);

                // 获取泛型类具体类型
                String typeName = "type";
                methodBuilder.addStatement(
                        "$T $L = new $T$L() {}.getType()",
                        Type.class,
                        typeName,
                        TypeToken.class,
                        innerStr
                );
//                String callName = "call";
//                methodBuilder.addStatement(
//                        "$T" + innerStr + " $L = new $T" + innerStr + "(){}",
//                        Call.class,
//                        callName,
//                        Call.class
//                );
//                String genericsClassName = "genericsClass";
//                methodBuilder.addStatement(
//                        "$T<Object> $L = $T.getGenericsClass(call, 0)",
//                        Class.class,
//                        genericsClassName,
//                        GenericsUtils.class
//                );
//                methodBuilder.addStatement(
//                        "$T.out.println(\"$L $L = \" + ($L == null ? \"null\" : $L.getCanonicalName()))",
//                        System.class,
//                        method.getSimpleName().toString(),
//                        genericsClassName,
//                        genericsClassName,
//                        genericsClassName
//                );
//                methodBuilder.addStatement(
//                        "$L.setGenericsClass($L)",
//                        callName,
//                        genericsClassName
//                );
                // 添加返回值
                methodBuilder.addStatement(
                        "return new $T" + innerStr + "().execute($L, $L, $L, $L, $L)",
                        HttpExecutor.class,
                        executeParamsName,
                        hasPathFieldData ? pathMapName : null,
                        hasUrlMapData ? urlMapName : null,
                        hasBodyMapData ? bodyMapName : null,
                        typeName
                );
                set.add(methodBuilder.build());
            }
        }
        return set;
    }

    /**
     * 将注解集合解析为javapoet的注解对象
     * @param annotationMirrors
     * @return
     */
    private List<AnnotationSpecWrapper> getAnnotationSpecs(List<? extends AnnotationMirror> annotationMirrors) {
        List<AnnotationSpecWrapper> list = new ArrayList<>();
        if (!annotationMirrors.isEmpty()) {
            for (AnnotationMirror annotation : annotationMirrors) {
                // 封装注解属性的集合
                Map<String, Object> annotationAttrs = new HashMap<>();
                // 创建注解spec
                AnnotationSpec.Builder builder = AnnotationSpec.builder(
                        ClassName.get((TypeElement) annotation.getAnnotationType().asElement())
                );
                // 获取注解的参数
                Map<? extends ExecutableElement, ? extends AnnotationValue> elementValues = annotation.getElementValues();
                if (!elementValues.isEmpty()) {
                    for (Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> entry : elementValues.entrySet()) {
                        ExecutableElement key = entry.getKey();
                        AnnotationValue value = entry.getValue();
                        // 注解的参数只支持基本数据类型、String和枚举
                        // 因此要单独处理String，枚举，还有float
                        TypeKind kind = key.getReturnType().getKind();
                        String format = "$L";
                        if (kind == TypeKind.DECLARED) {
                            if (ClazzUtils.isSubOrSameClazz(elementUtils, typeUtils, messager, key.getReturnType(), String.class)) {
                                format = "$S";
                                builder.addMember(key.getSimpleName().toString(), format, value.getValue());
                            } else {
                                // 其余情况是枚举enum
                                format = "$T.$L";
                                builder.addMember(key.getSimpleName().toString(), format, key.getReturnType(), value.getValue());
                            }
                        } else {
                            if (kind == TypeKind.FLOAT) {
                                // 基本数据类型中float需要添加f后缀
                                format = "$Lf";
                            }
                            builder.addMember(key.getSimpleName().toString(), format, value.getValue());
                        }
                        annotationAttrs.put(key.getSimpleName().toString(), value.getValue());
                    }
                }

                // 获得支持的注解类型枚举
                SupportAnnotation supportAnnotation = null;
                String annotationFullName = annotation.getAnnotationType().toString();
                if (UrlField.class.getCanonicalName().equals(annotationFullName)) {
                    supportAnnotation = SupportAnnotation.P_URL_FIELD;
                } else if (UrlMap.class.getCanonicalName().equals(annotationFullName)) {
                    supportAnnotation = SupportAnnotation.P_URL_MAP;
                } else if (PathField.class.getCanonicalName().equals(annotationFullName)) {
                    supportAnnotation = SupportAnnotation.P_PATH_FIELD;
                } else if (BodyField.class.getCanonicalName().equals(annotationFullName)) {
                    supportAnnotation = SupportAnnotation.P_BODY_FIELD;
                } else if (BodyMap.class.getCanonicalName().equals(annotationFullName)) {
                    supportAnnotation = SupportAnnotation.P_BODY_MAP;
                } else if (Get.class.getCanonicalName().equals(annotationFullName)) {
                    supportAnnotation = SupportAnnotation.M_GET;
                } else if (Post.class.getCanonicalName().equals(annotationFullName)) {
                    supportAnnotation = SupportAnnotation.M_POST;
                } else if (Delete.class.getCanonicalName().equals(annotationFullName)) {
                    supportAnnotation = SupportAnnotation.M_DELETE;
                } else if (Put.class.getCanonicalName().equals(annotationFullName)) {
                    supportAnnotation = SupportAnnotation.M_PUT;
                } else if (Patch.class.getCanonicalName().equals(annotationFullName)) {
                    supportAnnotation = SupportAnnotation.M_PATCH;
                } else {
                    // 不支持的注解，则直接忽略
                    printMsg("非EasyHttp的的注解，生成类会直接忽略：" + annotationFullName);
                }
                // 添加到集合
                // 如果supportAnnotation为null，则代表该注解不是属于框架内部的，直接忽略
                if (supportAnnotation != null) {
                    list.add(new AnnotationSpecWrapper(builder.build(), supportAnnotation, annotationAttrs));
                }
            }
        }
        return list;
    }


    private boolean checkUrl(String url) {
        return RegexUtils.isValidUrl(url);

    }
}
