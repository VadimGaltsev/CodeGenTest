package ru.home.processor;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.tools.Diagnostic;

@SupportedAnnotationTypes({"ru.home.processor.InitTest"})
@SupportedSourceVersion(SourceVersion.RELEASE_7)
public class AnnotationProcess extends AbstractProcessor {

    private final Map<TypeElement, WorkVisitor> mVisitors = new HashMap<>();

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        if (annotations.isEmpty()) {
            return false;
        }
        final Set<? extends Element> elements = roundEnv.getElementsAnnotatedWith(InitTest.class);
        HashMap<String, Object> map = new HashMap<>();
        for (final Element element : elements) {
            if (element.getAnnotation(InitTest.class) == null) {
                processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, "element " + element.getSimpleName());
                if (element instanceof VariableElement) map.put(element.getSimpleName().toString(), element);
                continue;
            }

            map.put(element.getSimpleName().toString(), element);
            final TypeElement object = (TypeElement) element.getEnclosingElement();
            WorkVisitor visitor = mVisitors.get(object);
            if (visitor == null) {
                visitor = new WorkVisitor(processingEnv, object, map);
                mVisitors.put(object, visitor);
            }
            element.accept(visitor, null);
        }
        for (final WorkVisitor visitor : mVisitors.values()) {
            visitor.makeProxy();
        }
        return true;
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.RELEASE_7;
    }

}
