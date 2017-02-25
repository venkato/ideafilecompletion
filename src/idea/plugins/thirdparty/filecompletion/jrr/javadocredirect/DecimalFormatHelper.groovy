package idea.plugins.thirdparty.filecompletion.jrr.javadocredirect

import com.intellij.psi.*
import com.intellij.psi.impl.source.tree.LeafPsiElement
import groovy.transform.CompileStatic
import idea.plugins.thirdparty.filecompletion.jrr.javassist.JavassistCompletionBean
import net.sf.jremoterun.utilities.JrrClassUtils
import org.apache.log4j.LogManager
import org.apache.log4j.Logger
import org.jetbrains.plugins.groovy.lang.psi.api.statements.expressions.GrNewExpression

import java.text.DecimalFormat

@CompileStatic
public class DecimalFormatHelper {
    private static final Logger log = LogManager.getLogger(JrrClassUtils.currentClass);

    private void testNotUsed() {
        new DecimalFormat("00")
    }

    public static boolean isOkPsiElement(LeafPsiElement leafPsiElement) {
        log.debug "is ok ? ${leafPsiElement}"
        PsiElement parent = leafPsiElement.getParent();
        if (!(parent instanceof PsiLiteral)) {
            log.debug "not psi"
            return null;
        }
        PsiLiteral literalElemtnt = parent as PsiLiteral;
        Object value = literalElemtnt.getValue();
        if (value instanceof String) {
        } else {
            log.debug "not a string"
            return null;
        }
        JavassistCompletionBean completionBean = new JavassistCompletionBean();
        completionBean.literalElement = literalElemtnt;
        PsiElement parent1 = literalElemtnt.parent;
        if (!(parent1 instanceof PsiExpressionList)) {
            log.debug "not gr args"
            return null;
        }
        PsiExpressionList args = (PsiExpressionList) parent1;
        completionBean.args = args
//        if (args.allArguments.length > 2) {
//            log.debug "too small args"
//            return null
//        }
        PsiElement parent2 = args.parent
        if (parent2 instanceof PsiNewExpression) {
            PsiNewExpression grNewExpression = (PsiNewExpression) parent2;
            PsiType type = grNewExpression.type;
            if (!(type instanceof PsiClassType)) {
                log.debug "not a type"
                return null;
            }
            PsiClassType grClassReferenceType = (PsiClassType) type;
            PsiClass psiClass = grClassReferenceType.resolve();
            if (psiClass == null || !(psiClass.name.contains('DecimalFormat'))) {
                log.debug "not a file"
                return null;
            }
            return true
        } else if (parent2 instanceof GrNewExpression) {
            GrNewExpression grNewExpression = (GrNewExpression) parent2;
            PsiType type = grNewExpression.type;
            if (!(type instanceof PsiClassType)) {
                log.debug "not a type"
                return null;
            }
            PsiClassType grClassReferenceType = (PsiClassType) type;
            PsiClass psiClass = grClassReferenceType.resolve();
            if (psiClass == null || !(psiClass.name.contains('DecimalFormat'))) {
                log.debug "not a file"
                return null;
            }
            return true
        }

        log.debug "not not"
        return false;

    }
}
