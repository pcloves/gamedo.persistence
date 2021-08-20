package lombok.javac.handlers;

import com.sun.tools.javac.code.Flags;
import com.sun.tools.javac.code.Type;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.JCTree.JCAnnotation;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.Name;
import com.sun.tools.javac.util.Names;
import lombok.DirtySetter;
import lombok.core.AST;
import lombok.core.AnnotationValues;
import lombok.core.HandlerPriority;
import lombok.javac.JavacAnnotationHandler;
import lombok.javac.JavacNode;
import lombok.javac.JavacTreeMaker;
import org.kohsuke.MetaInfServices;

import java.util.Collection;

import static lombok.javac.Javac.CTC_BOOLEAN;
import static lombok.javac.handlers.JavacHandlerUtil.*;

@MetaInfServices
@HandlerPriority(value = 0, subValue = 1) // we must run AFTER HandleSetter which is at 0 (default value)
public class HandleDirtySetter extends JavacAnnotationHandler<DirtySetter> {
    @Override
    public void handle(AnnotationValues<DirtySetter> annotation, JCAnnotation ast, JavacNode annotationNode) {

        final Collection<JavacNode> fieldNode = annotationNode.upFromAnnotationToFields();
        deleteAnnotationIfNeccessary(annotationNode, DirtySetter.class);
        deleteImportFromCompilationUnit(annotationNode, "lombok.AccessLevel");

        final JavacNode node = annotationNode.up();
        if (node == null) return;

        final AST.Kind kind = node.getKind();
        switch (kind) {
            case TYPE:
                modifySetterMethodForFields(fieldNode);
                break;
            case FIELD:
                modifySetterMethodForField(node);
                break;
            default:
//                node.addError("!!!!!!!!!!!!" + kind);
        }

    }

    private void modifySetterMethodForFields(Collection<JavacNode> fieldNodes) {
        for (JavacNode fieldNode : fieldNodes) {
            modifySetterMethodForField(fieldNode);
        }
    }

    private void modifySetterMethodForField(JavacNode fieldNode) {
        if (fieldNode.getKind() != AST.Kind.FIELD) {
            fieldNode.addError("@" + DirtySetter.class.getSimpleName() + " is only supported on a class or a field.");
            return;
        }

        JCTree.JCVariableDecl fieldDecl = (JCTree.JCVariableDecl) fieldNode.get();
        if ((fieldDecl.mods.flags & Flags.FINAL) != 0) {
            fieldNode.addWarning("stop modify the setter method because of final field.");
            return;
        }

        if ((fieldDecl.mods.flags & Flags.STATIC) != 0) {
            fieldNode.addWarning("stop modify the setter method because of static field.");
            return;
        }

        final JavacTreeMaker treeMaker1 = fieldNode.getTreeMaker();
        JavacNode setterMethod = findSetterForField(fieldNode);
        if (setterMethod == null) {
            fieldNode.addError("setter method is required. Either add it manually or via @Setter annotation");
            return;
        }

        final JavacTreeMaker treeMakerMethod = setterMethod.getTreeMaker();
        JCTree.JCMethodDecl setterMethodDecl = (JCTree.JCMethodDecl) setterMethod.get();
        Type booleanType = treeMaker1.Literal(false).type;

//        fieldNode.addError("#########" + setterMethodDecl.params + "," + setterMethodDecl.recvparam);

        final JCTree.JCModifiers modifiers = treeMakerMethod.Modifiers(Flags.PARAMETER);
        final Name setDirty = setterMethod.toName("setDirty");
        final JCTree.JCPrimitiveTypeTree typeTree = treeMakerMethod.TypeIdent(CTC_BOOLEAN);
        final JCTree.JCVariableDecl param = treeMakerMethod.VarDef(modifiers,
                setDirty,
                typeTree,
                null);

        //这里不行，还得继续实验！
        setterMethodDecl.params = setterMethodDecl.params.append(param);
    }

    private JavacNode findSetterForField(JavacNode fieldNode) {
        String setterName = toSetterName(fieldNode);

        if (setterName != null) {
            for (JavacNode node : fieldNode.up().down()) {
                if (node.getKind() == AST.Kind.METHOD && setterName.equals(node.getName())) {
                    return node;
                }
            }
        }
        return null;
    }
}
