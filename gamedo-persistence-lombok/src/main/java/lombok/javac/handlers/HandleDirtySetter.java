package lombok.javac.handlers;

import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.JCTree.JCAnnotation;
import lombok.AccessLevel;
import lombok.DirtySetter;
import lombok.core.AnnotationValues;
import lombok.javac.JavacAnnotationHandler;
import lombok.javac.JavacNode;

import java.util.Collection;

import static lombok.javac.handlers.JavacHandlerUtil.*;

public class HandleDirtySetter extends JavacAnnotationHandler<DirtySetter> {
    @Override
    public void handle(AnnotationValues<DirtySetter> annotation, JCAnnotation ast, JavacNode annotationNode) {

        final Collection<JavacNode> fields = annotationNode.upFromAnnotationToFields();
        deleteAnnotationIfNeccessary(annotationNode, DirtySetter.class);
        deleteImportFromCompilationUnit(annotationNode, "lombok.AccessLevel");

        final JavacNode node = annotationNode.up();
        AccessLevel level = annotation.getInstance().value();

        JCTree.JCClassDecl classDecl = (JCTree.JCClassDecl) node.get();

        if (level == AccessLevel.NONE || node == null) return;


    }
}
