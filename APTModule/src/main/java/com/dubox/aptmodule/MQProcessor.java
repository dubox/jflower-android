package com.dubox.aptmodule;

import com.dubox.annotationmodule.MyAnnotations;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import com.google.auto.service.AutoService;

import org.checkerframework.checker.units.qual.A;


@AutoService(Processor.class)
@SupportedSourceVersion(SourceVersion.RELEASE_8)
public class MQProcessor extends AbstractProcessor {


    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {

        super.init(processingEnv);

        System.out.println("MQProcessor----------->init");

    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnvironment) {

        System.out.println("MQProcessor----------->process");
        System.out.println(annotations.size());

        for (TypeElement typeElement : annotations) {
            System.out.println("typeElement----------");
            System.out.println(typeElement);
            Set<? extends Element> elements = roundEnvironment.getElementsAnnotatedWith(typeElement);
            for (Element element : elements) {
                System.out.println("element---------");
                // 获取注解镜像
                AnnotationMirror annotationMirror = getAnnotationMirror(element, typeElement);

                // 获取注解传值
                System.out.println(getAnnotationValue(annotationMirror, "value").getValue());
            }
        }

        return true;
    }

    //重写getSupportedAnnotationTypes方法，添加支持处理的注解类型
    @Override
    public Set<String> getSupportedAnnotationTypes() {
        HashSet<String> supportTypes = new LinkedHashSet<>();
        System.out.println("AnnotationTypes-----------");
        System.out.println("AnnotationTypes-----------");

        Class<?>[] innerClasses = MyAnnotations.class.getDeclaredClasses();
        System.out.println(innerClasses.length);
        for (Class<?> innerClass : innerClasses) {
            // 判断内部类是否为注解
            if (innerClass.isAnnotation()) {
                // 将内部类的类型加入到annotations列表中
                supportTypes.add(innerClass.getCanonicalName());
            }
        }
//        supportTypes.add(MyAnnotations.onClick.class.getCanonicalName());
        return supportTypes;
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    private AnnotationMirror getAnnotationMirror(Element element, TypeElement annotation) {
        // 遍历元素的注解镜像
        for (AnnotationMirror mirror : element.getAnnotationMirrors()) {
            // 比较注解的类型和名称
            if (mirror.getAnnotationType().asElement().equals(annotation)) {
                return mirror;
            }
        }
        return null;
    }

    private AnnotationValue getAnnotationValue(AnnotationMirror annotationMirror, String key) {
        // 获取注解的传值
        for (Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> entry : annotationMirror.getElementValues().entrySet()) {
            ExecutableElement method = entry.getKey();
            AnnotationValue value = entry.getValue();

            if (method.getSimpleName().toString().equals(key)) {
                return value;
            }
        }
        return null;
    }

}