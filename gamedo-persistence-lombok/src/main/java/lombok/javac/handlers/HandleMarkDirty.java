package lombok.javac.handlers;

import com.sun.tools.javac.code.Flags;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.JCTree.JCAnnotation;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.ListBuffer;
import com.sun.tools.javac.util.Name;
import lombok.AccessLevel;
import org.gamedo.annotation.MarkDirty;
import lombok.core.AST;
import lombok.core.AnnotationValues;
import lombok.core.HandlerPriority;
import lombok.core.handlers.HandlerUtil;
import lombok.javac.Javac;
import lombok.javac.JavacAnnotationHandler;
import lombok.javac.JavacNode;
import lombok.javac.JavacTreeMaker;
import org.kohsuke.MetaInfServices;

import static lombok.javac.Javac.CTC_VOID;
import static lombok.javac.handlers.JavacHandlerUtil.*;

@MetaInfServices
@HandlerPriority(value = 0, subValue = 1) // we must run AFTER HandleSetter which is at 0 (default value)
public class HandleMarkDirty extends JavacAnnotationHandler<MarkDirty> {

    @Override
    public void handle(AnnotationValues<MarkDirty> annotation, JCAnnotation ast, JavacNode annotationNode) {

        deleteAnnotationIfNeccessary(annotationNode, MarkDirty.class);
        deleteImportFromCompilationUnit(annotationNode, "lombok.AccessLevel");

        final JavacNode node = annotationNode.up();
        AccessLevel accessLevel = annotation.getInstance().value();
        if (node == null) return;

        final AST.Kind kind = node.getKind();
        switch (kind) {
            case TYPE:
                createMarkDirtyMethodForFields(annotationNode, node.down(), accessLevel);
                break;
            case FIELD:
                createMarkDirtyMethodForFields(annotationNode, annotationNode.upFromAnnotationToFields(), accessLevel);
                break;
            default:
                node.addError("@" + MarkDirty.class.getSimpleName() + " is only supported on a class or a field.");
        }

    }

    private void createMarkDirtyMethodForFields(JavacNode source, Iterable<JavacNode> fieldNodes, AccessLevel accessLevel) {

        for (JavacNode fieldNode : fieldNodes) {
            if (fieldQualifies(fieldNode)) {
                createMarkDirtyMethodForField(source, fieldNode, accessLevel);
            }
        }
    }

    private void createMarkDirtyMethodForField(JavacNode source, JavacNode fieldNode, AccessLevel accessLevel) {
        if (fieldNode.getKind() != AST.Kind.FIELD) {
            fieldNode.addError("@" + MarkDirty.class.getSimpleName() + " is only supported on a class or a field." + fieldNode);
            return;
        }

        final JCTree.JCVariableDecl fieldDecl = (JCTree.JCVariableDecl) fieldNode.get();
        if ((fieldDecl.mods.flags & Flags.FINAL) != 0) {
            fieldNode.addWarning("stop create markDirty method because of the final flag.");
            return;
        }

        if ((fieldDecl.mods.flags & Flags.STATIC) != 0) {
            fieldNode.addWarning("stop create markDirty method because of the static flag.");
            return;
        }

        if (markDirtyMethodAlreadyPresent(fieldNode)) {
            fieldNode.addWarning(HandlerUtil.buildAccessorName("markDirty", fieldNode.getName()) + " is present, ignore it.");
            return;
        }

        if (JavacHandlerUtil.hasAnnotation(MarkDirty.Exclude.class, fieldNode)) {
            fieldNode.addWarning(MarkDirty.Exclude.class.getSimpleName() + " is present, ignore it.");
            return;
        }

        deleteAnnotationIfNeccessary(fieldNode, MarkDirty.Exclude.class);

        final JavacNode typeNode = fieldNode.up();
        final JavacTreeMaker typeMarker = typeNode.getTreeMaker();
        final long access = toJavacModifier(accessLevel);
        final JCTree.JCExpression methodType = typeMarker.TypeIdent(CTC_VOID);
        final Name methodName = fieldNode.toName(HandlerUtil.buildAccessorName("markDirty", fieldNode.getName()));

        final ListBuffer<JCTree.JCStatement> statements = new ListBuffer<>();
        final JCTree.JCExpressionStatement setDirty = typeMarker.Exec(typeMarker.Apply(
                //参数类型(传入方法的参数的类型) 如果是无参的不能设置为null 使用 List.nil()
                List.of(memberAccess(typeNode, typeMarker, "java.lang.String"),
                        memberAccess(typeNode, typeMarker, "java.lang.Object")),
                memberAccess(fieldNode, fieldNode.getTreeMaker(), "setDirty"),
                //两个参数
                List.of(typeMarker.Literal(fieldNode.getName()),
                        typeMarker.Ident(fieldDecl.name)
                )
                )
        );

        statements.append(setDirty);

        final JCTree.JCBlock methodBody = typeMarker.Block(0, statements.toList());
        final JCTree.JCMethodDecl methodDef = typeMarker.MethodDef(typeMarker.Modifiers(access),
                //函数名
                methodName,
                //返回值
                methodType,
                //泛型参数列表
                List.nil(),
                //入参列表
                List.nil(),
                //异常列表
                List.nil(),
                methodBody,
                null
        );

        JavacHandlerUtil.injectMethod(typeNode,
                recursiveSetGeneratedBy(methodDef, source),
                List.nil(),
                Javac.createVoidType(fieldNode.getSymbolTable(), CTC_VOID));
    }

    private JCTree.JCExpression memberAccess(JavacNode javacNode, JavacTreeMaker treeMaker, String components) {
        String[] componentArray = components.split("\\.");
        JCTree.JCExpression expr = treeMaker.Ident(javacNode.toName(componentArray[0]));
        for (int i = 1; i < componentArray.length; i++) {
            expr = treeMaker.Select(expr, javacNode.toName(componentArray[i]));
        }
        return expr;
    }

    private boolean markDirtyMethodAlreadyPresent(JavacNode fieldNode) {
        String markDirtyMethodName = HandlerUtil.buildAccessorName("markDirty", fieldNode.getName());

        if (markDirtyMethodName != null) {
            for (JavacNode node : fieldNode.up().down()) {
                if (node.getKind() == AST.Kind.METHOD && markDirtyMethodName.equals(node.getName())) {
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean fieldQualifies(JavacNode field) {
        if (field.getKind() != AST.Kind.FIELD) return false;
        JCTree.JCVariableDecl fieldDecl = (JCTree.JCVariableDecl) field.get();
        //Skip fields that start with $
        if (fieldDecl.name.toString().startsWith("$")) return false;
        //Skip static fields.
        return (fieldDecl.mods.flags & Flags.STATIC) == 0;
    }
}
