package ru.home.processor;


import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;
import com.sun.source.util.Trees;
import com.sun.tools.javac.code.Flags;
import com.sun.tools.javac.processing.JavacProcessingEnvironment;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.tree.TreeTranslator;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.Pair;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;

import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.util.ElementScanner7;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;


public class WorkVisitor extends ElementScanner7<Void, Void> {

    private final CodeBlock.Builder newData = CodeBlock.builder();

    private final Trees mTrees;

    private final Messager mLogger;

    private final Filer mFiler;

    private final TypeElement mOriginElement;

    private final TreeMaker mTreeMaker;

    private final ProcessingEnvironment environment;

    private VariableElement variableElement;

    private final com.sun.tools.javac.util.Names mNames;

    private final HashMap<String, Object> elementConcurrentHashMap = new HashMap<>();

    public WorkVisitor(ProcessingEnvironment env, TypeElement element, HashMap<String, Object> elementsMap) {
        super();
        environment = env;
        mTrees = Trees.instance(env);
        mLogger = env.getMessager();
        mFiler = env.getFiler();
        mOriginElement = element;
        final JavacProcessingEnvironment javacEnv = (JavacProcessingEnvironment) env;
        mTreeMaker = TreeMaker.instance(javacEnv.getContext());
        mNames = com.sun.tools.javac.util.Names.instance(javacEnv.getContext());
    }

    @Override
    public Void visitVariable(VariableElement e, Void aVoid) {
        ((JCTree) mTrees.getTree(e)).accept(new TreeTranslator() {
            @Override
            public void visitVarDef(JCTree.JCVariableDecl jcVariableDecl) {
                super.visitVarDef(jcVariableDecl);
                jcVariableDecl.mods.flags &= ~Flags.PRIVATE;
            }
        });
        variableElement = e;
        return super.visitVariable(e, aVoid);
    }
    @Override
    public Void visitExecutable(ExecutableElement executableElement, Void aVoid) {
//        final BlockTree blockTree = mTrees.getTree(executableElement).getBody();
//        if (executableElement.getSimpleName().toString().equals("onCreate")) {
//            ((JCTree) mTrees.getTree(executableElement)).accept(new TreeTranslator() {
//                @Override
//                public void visitBlock(JCTree.JCBlock jcBlock) {
//                    mLogger.printMessage(Diagnostic.Kind.NOTE, "body " + blockTree.getStatements());
//                    StatementTree statementTree = blockTree.getStatements().get(0);
//                    JCTree.JCExpression jcExpression = mTreeMaker.Ident(mNames.fromString(blockTree.getStatements().get(0).toString()));
//                    JCTree.JCExpression printExpression = mTreeMaker.Ident(mNames._super);
//                    mLogger.printMessage(Diagnostic.Kind.NOTE, "jcExpression " + printExpression);
//                    printExpression = mTreeMaker.Select(printExpression, mNames.fromString("onCreate"));
//                    mLogger.printMessage(Diagnostic.Kind.NOTE, "jcExpression " + printExpression);
//                    JCTree.JCVariableDecl variableDecl = ((JCTree.JCMethodDecl)mTrees.getTree(executableElement)).params.get(0);
//                    //  printExpression = mTreeMaker.Select(printExpression, mNames.fromString("println"));
//                    mLogger.printMessage(Diagnostic.Kind.NOTE, "jcExpression " + printExpression);
//                    List<JCTree.JCExpression> printArgs = List.from(new JCTree.JCExpression[] {mTreeMaker.Ident(variableDecl)});
//                    printExpression = mTreeMaker.Apply(List.<JCTree.JCExpression>nil(), printExpression, printArgs);
//                    JCTree.JCStatement call = mTreeMaker.Exec(printExpression);
//                    mLogger.printMessage(Diagnostic.Kind.NOTE, "jcExpression " + jcExpression);
//                    JCTree.JCStatement jcStatement = mTreeMaker.Exec(jcExpression);
//                    mLogger.printMessage(Diagnostic.Kind.NOTE, "jcStatement " + jcStatement.pos);
//                    JCTree.JCBlock selector = mTreeMaker.Block(0L, List.from(new JCTree.JCStatement[]{call}));
//                    mLogger.printMessage(Diagnostic.Kind.NOTE, "new body " + selector);
//                    mLogger.printMessage(Diagnostic.Kind.NOTE, "old Body  " + jcBlock);
//                    ((JCTree.JCMethodDecl)mTrees.getTree(executableElement)).body = selector;
//                //    mLogger.printMessage(Diagnostic.Kind.NOTE, "arg body " + ((JCTree.JCMethodDecl)mTrees.getTree(executableElement)).recvparam);
//                    mLogger.printMessage(Diagnostic.Kind.NOTE, "arg tail body " + ((JCTree.JCMethodDecl)mTrees.getTree(executableElement)).params.get(0));
//                    mLogger.printMessage(Diagnostic.Kind.NOTE, "new " + ((JCTree.JCMethodDecl)mTrees.getTree(executableElement)).body);
//                    super.visitBlock(selector);
//                }
//            });
//        }
        return super.visitExecutable(executableElement, aVoid);
    }

    public void addStatements(ExecutableElement executableElement) {
        JCTree.JCBlock block = parseMethod(executableElement);
        String oldBlock = block.toString();
        if (oldBlock.contains("super")) {
            String line = oldBlock.subSequence(oldBlock.indexOf("super"), oldBlock.indexOf(";") + 1).toString();
            oldBlock = oldBlock.replace(line, "");
        }
        CodeBlock.Builder code = CodeBlock.builder();
        if (oldBlock.contains("}") || oldBlock.contains("{")) {
            oldBlock = oldBlock.replace("}", "");
            oldBlock = oldBlock.replace("{", "");
        }
        oldBlock = oldBlock.replaceAll("\n", "");
        StringBuilder line = new StringBuilder();
        SortedMap<Integer, Object> sortedMap = new TreeMap<>();
        int index = 0;
        for (String s : oldBlock.split(";")) {
            for (Element element : mOriginElement.getEnclosedElements()) {
                    if (s.contains(element.getSimpleName().toString()) && element.getKind().isField()) {
                        String elementName = element.getSimpleName().toString();
                        if (s.matches("(.)*(\\W)*(" + elementName + ")(\\W)+(.)*")) {
                            while (s.contains(elementName)) {
                                if (!sortedMap.containsKey(s.indexOf(elementName))) {
                                    index = s.indexOf(elementName);
                                    sortedMap.put(index, element.getSimpleName());
                                    mLogger.printMessage(Diagnostic.Kind.NOTE, "var putted element " + elementName + " " + (index));
                                }
                                s = s.replaceFirst("(" + elementName + ")",
                                        Matcher.quoteReplacement("(($T) MainActivityShadow.this).$L"));
                                mLogger.printMessage(Diagnostic.Kind.NOTE, "var new str " + s);
                                if (index != 0) {
                                    sortedMap.put(index - 1, mOriginElement);
                                    mLogger.printMessage(Diagnostic.Kind.NOTE, "var putted origin " + (index - 1));
                                    mLogger.printMessage(Diagnostic.Kind.NOTE, "var origin index " + index);
                                } else {
                                    mLogger.printMessage(Diagnostic.Kind.NOTE, "var putted origin " + index);
                                    sortedMap.put(0, mOriginElement);
                                }
                            }
                            //  mLogger.printMessage(Diagnostic.Kind.NOTE, "New line var " + s);
//                        for (Object o : sortedMap.values()) {
//                            mLogger.printMessage(Diagnostic.Kind.NOTE, "Element var in map " + o.toString());
//                        }
                        }
                    }
            }
            line.append(s).append(";").append("\n");
            mLogger.printMessage(Diagnostic.Kind.NOTE, "Element var " + line.toString());
            for (Object o : sortedMap.values()) {
                mLogger.printMessage(Diagnostic.Kind.NOTE, "Element var in map " + o.toString());
            }
            code.add(line.toString(), sortedMap.values().toArray());
            mLogger.printMessage(Diagnostic.Kind.NOTE, "Code var " + code.build().toString());
            line.delete(0, line.capacity());
            sortedMap.clear();
            index = 0;
//            if (s.contains(elementT.getSimpleName().toString())) {
//                code.add(s, ClassName.get(mOriginElement), elementT.getSimpleName());
//            }
        }
        mLogger.printMessage(Diagnostic.Kind.NOTE, "Body line var " + line.toString());

        mLogger.printMessage(Diagnostic.Kind.NOTE, "OldBlock " + oldBlock);
        newData.addStatement("(($T) this).$L = new $T() \n", /*($T)this*/
                        ClassName.get(mOriginElement), variableElement.getSimpleName(), ClassName.get(variableElement.asType()),
                        ClassName.get(mOriginElement))
                .add("(($T) this).$L.initTest(new $T() { \n", ClassName.get(mOriginElement), variableElement.getSimpleName(),
                        ClassName.get("ru.home.processor", "ITest"))
                .add("@Override \n" +
                                "public void onInit() { \n ")
                .add(code.build())
                .add("}});");
        mLogger.printMessage(Diagnostic.Kind.NOTE, newData.build().toString());
//                            ClassName.get(mOriginElement), e.getSimpleName());
    }

    JCTree.JCBlock parseMethod(final ExecutableElement executableElement) {
        final JCTree.JCBlock blocks[] = new JCTree.JCBlock[1];
        ((JCTree) mTrees.getTree(executableElement)).accept(new TreeTranslator() {
            @Override
            public void visitBlock(JCTree.JCBlock jcBlock) {
                JCTree.JCExpression expression = mTreeMaker.Ident(mNames._super);
                expression = mTreeMaker.Select(expression, mNames.fromString("onCreate"));
                JCTree.JCVariableDecl variableDecl = ((JCTree.JCMethodDecl) mTrees.getTree(executableElement)).params.get(0);
                List<JCTree.JCExpression> printArgs = List.from(new JCTree.JCExpression[]{mTreeMaker.Ident(variableDecl)});
                expression = mTreeMaker.Apply(List.<JCTree.JCExpression>nil(), expression, printArgs);
                JCTree.JCStatement statement = mTreeMaker.Exec(expression);
                JCTree.JCBlock block = mTreeMaker.Block(0L, List.from(new JCTree.JCStatement[]{statement}));
                ((JCTree.JCMethodDecl) mTrees.getTree(executableElement)).body = block;
                blocks[0] = jcBlock;
                super.visitBlock(block);
            }
        });
        mLogger.printMessage(Diagnostic.Kind.NOTE, blocks[0].toString());
        return blocks[0];
    }

    public void makeProxy() {
        for (Element element : mOriginElement.getEnclosedElements()) {
            if (element.getSimpleName().toString().equals("onCreate")) {
                addStatements((ExecutableElement) element);
                final TypeSpec typeSpec = TypeSpec.classBuilder(mOriginElement.getSimpleName() + "Shadow")
                        .addModifiers(Modifier.ABSTRACT)
                        .superclass(ClassName.get(mOriginElement.getSuperclass()))
                        .addOriginatingElement(mOriginElement)
                        .addMethod(MethodSpec.overriding((ExecutableElement)element).addStatement("super.onCreate(savedInstanceState)")
                        .addCode(newData.build()).
                                build())
                        .build();
                final JavaFile javaFile = JavaFile.builder(mOriginElement.getEnclosingElement().toString(), typeSpec)
                        .addFileComment("Generated by Vadik;")
                        .build();
                try {
                    final JavaFileObject sourceFile = mFiler.createSourceFile(
                            javaFile.packageName + "." + typeSpec.name, mOriginElement);
                    try (final Writer writer = new BufferedWriter(sourceFile.openWriter())) {
                        javaFile.writeTo(writer);
                    }
                    JCTree.JCExpression selector = mTreeMaker.Ident(mNames.fromString(javaFile.packageName));
                    selector = mTreeMaker.Select(selector, mNames.fromString(typeSpec.name));
                    ((JCTree.JCClassDecl) mTrees.getTree(mOriginElement)).extending = selector;
                } catch (IOException e) {
                    mLogger.printMessage(Diagnostic.Kind.ERROR, e.getMessage(), mOriginElement);
                }
            }
        }

    }

}
