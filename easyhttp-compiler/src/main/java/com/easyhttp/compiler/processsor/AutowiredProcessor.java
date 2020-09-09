package com.easyhttp.compiler.processsor;

import com.easyhttp.core.annotations.Api;
import com.easyhttp.core.annotations.Autowired;
import com.easyhttp.core.manager.ApiProvider;
import com.easyhttp.core.utils.GenerateRules;
import com.google.auto.service.AutoService;
import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.FieldSignature;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Set;

import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;

@AutoService(Processor.class)
@SupportedSourceVersion(SourceVersion.RELEASE_8)
@SupportedAnnotationTypes({
        "com.easyhttp.core.annotations.Autowired"
})
public class AutowiredProcessor extends AbsProcessor {

    @Override
    void handleAnnotations(RoundEnvironment roundEnvironment) {
        printMsg("handleAnnotations--------------");
        Set<? extends Element> autowiredElements = roundEnvironment.getElementsAnnotatedWith(Autowired.class);
        if (!autowiredElements.isEmpty()) {
            for (Element element : autowiredElements) {
                // 从Autowired注解的类型上获取Api注解，如果没有，则无效，不能够使用该框架注入其他类，防止其他问题产生
                TypeMirror typeMirror = element.asType();
                TypeElement clazzElement = (TypeElement) typeUtils.asElement(typeMirror);
                Api api = clazzElement.getAnnotation(Api.class);
                if (api == null) {
                    printError("使用@Autowired注解标注的字段类，必须使用@Api注解标记");
                }
                // 字段当前命名变量名称
                String fieldParamName = element.getSimpleName().toString();
                printMsg("fieldParamName = " + fieldParamName);
                // 字段类完全路径名称
                String fieldClazzName = clazzElement.getQualifiedName().toString();
                printMsg("fieldClazzName = " + fieldClazzName);
                // 字段变量所在的类的完全路径名称
                Element fieldHost = element.getEnclosingElement();
                String fieldHostFullName = fieldHost.asType().toString();
                String generateClassName = GenerateRules.generateEasyAutowiredClazzName(
                        fieldHostFullName, fieldParamName
                );
                printMsg("生成类的名字：" + generateClassName);

                //
                String fieldClazzConstName = "FIELD_CLASS_NAME";
                FieldSpec.Builder fieldClazzConstBuilder = FieldSpec.builder(
                        TypeName.get(String.class),
                        fieldClazzConstName,
                        Modifier.PRIVATE,
                        Modifier.STATIC,
                        Modifier.FINAL
                ).initializer("$S", fieldClazzName);;
                String fieldParamConstName = "FIELD_PARAM_NAME";
                FieldSpec.Builder fieldParamConstBuilder = FieldSpec.builder(
                        TypeName.get(String.class),
                        fieldParamConstName,
                        Modifier.PRIVATE,
                        Modifier.STATIC,
                        Modifier.FINAL
                ).initializer("$S", fieldHostFullName + "." + fieldParamName);

                String pointcutName = "POINTCUT";
                FieldSpec.Builder pointcutConstBuilder = FieldSpec.builder(
                        TypeName.get(String.class),
                        pointcutName,
                        Modifier.PRIVATE,
                        Modifier.STATIC,
                        Modifier.FINAL
                ).initializer("\"get(\" + $L + \" \" + $L + \")\"", fieldClazzConstName, fieldParamConstName);

                // pointcut方法
                String pointcutMethodName = "lazyAssignment";
                MethodSpec.Builder pointcutMethodBuilder =
                        MethodSpec.methodBuilder(pointcutMethodName)
                                .addModifiers(Modifier.PUBLIC)
                                .addAnnotation(
                                        AnnotationSpec.builder(
                                                Pointcut.class
                                        ).addMember("value", "$L", pointcutName).build()
                                );
                // weave方法
                String joinPointParam = "joinPoint";
                MethodSpec.Builder weaveMethodBuilder =
                        MethodSpec.methodBuilder("weave")
                                .addModifiers(Modifier.PUBLIC)
                                .returns(TypeName.get(Object.class))
                                .addParameter(
                                        ParameterSpec.builder(
                                                TypeName.get(ProceedingJoinPoint.class),
                                                joinPointParam
                                        ).build()
                                ).addException(TypeName.get(Throwable.class));

                String fieldValue = "fieldValue";
                String fieldHostObjParam = "fieldHostObj";
                String fieldSignatureParam = "signature";
                String fieldParam = "field";
                String instanceParam = "instance";
                String fieldSimpleName = "simpleName";
                weaveMethodBuilder
                        .addAnnotation(
                                AnnotationSpec.builder(Around.class)
                                        .addMember("value", "\"$L()\"", pointcutMethodName)
                                        .build()
                        )
                        .addStatement(
                                "$T $L = $L.proceed()",
                                Object.class,
                                fieldValue,
                                joinPointParam
                        )
                        .addCode(
                                CodeBlock.builder().beginControlFlow("if ($L != null)", fieldValue)
                                        .addStatement("return $L", fieldValue)
                                        .endControlFlow()
                                        .build()
                        )
                        .addStatement(
                                "$T $L = $L.getTarget()",
                                Object.class,
                                fieldHostObjParam,
                                joinPointParam
                        )
                        .addStatement(
                                "$T $L = ($T) $L.getSignature()",
                                FieldSignature.class,
                                fieldSignatureParam,
                                FieldSignature.class,
                                joinPointParam

                        )
                        .addStatement(
                                "$T $L = $L.getField()",
                                Field.class,
                                fieldParam,
                                fieldSignatureParam
                        )
                        .addStatement(
                                "$T $L = $L.getFieldType().getSimpleName()",
                                String.class,
                                fieldSimpleName,
                                fieldSignatureParam
                        )
                        .addStatement(
                                "$T $L = $T.getGenerateApiClazz($L).newInstance()",
                                Object.class,
                                instanceParam,
                                GenerateRules.class,
                                fieldSimpleName
                        )
                        .addStatement(
                                "$L.setAccessible(true)",
                                fieldParam
                        )

                        .addStatement("$T.add($L,$L)", ApiProvider.class, fieldClazzConstName, instanceParam) //添加到全局服务池

                        .addStatement(
                                "$L.set($L, $L)",
                                fieldParam,
                                fieldHostObjParam,
                                instanceParam
                        )
                        .addStatement(
                                "return $L",
                                instanceParam
                        );
                // 构建类
                TypeSpec.Builder typeBuilder = TypeSpec.classBuilder(generateClassName)
                        .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                        .addAnnotation(Aspect.class)
                        .addField(fieldClazzConstBuilder.build())
                        .addField(fieldParamConstBuilder.build())
                        .addField(pointcutConstBuilder.build())
                        .addMethod(pointcutMethodBuilder.build())
                        .addMethod(weaveMethodBuilder.build());

                JavaFile javaFile = JavaFile.builder(
                        GenerateRules.generateEasyApiClazzNamePkg(),
                        typeBuilder.build()
                ).build();
                try {
                    // 输出文件
                    javaFile.writeTo(filer);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
