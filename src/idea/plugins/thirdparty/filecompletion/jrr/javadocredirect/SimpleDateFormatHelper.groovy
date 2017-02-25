package idea.plugins.thirdparty.filecompletion.jrr.javadocredirect

import com.intellij.psi.*
import com.intellij.psi.impl.source.tree.LeafPsiElement
import groovy.transform.CompileStatic
import idea.plugins.thirdparty.filecompletion.jrr.javassist.JavassistCompletionBean
import net.sf.jremoterun.utilities.JrrClassUtils
import org.apache.log4j.LogManager
import org.apache.log4j.Logger
import org.jetbrains.plugins.groovy.lang.psi.api.statements.expressions.GrExpression
import org.jetbrains.plugins.groovy.lang.psi.api.statements.expressions.GrMethodCall
import org.jetbrains.plugins.groovy.lang.psi.api.statements.expressions.GrNewExpression
import org.jetbrains.plugins.groovy.lang.psi.api.statements.expressions.GrReferenceExpression
import org.jetbrains.plugins.groovy.lang.psi.api.statements.typedef.members.GrGdkMethod

import java.text.SimpleDateFormat
import java.util.regex.Pattern

@CompileStatic
public class SimpleDateFormatHelper {
    private static final Logger log = LogManager.getLogger(JrrClassUtils.currentClass);

    private void testNotUsed() {
        Date date = null
        Calendar calendar = null
        new SimpleDateFormat('yyyy-MM')
        calendar.format('yyyy-MM')
        new Date().format('yyyy-MM')
        new Date().format('yyyy-MM', TimeZone.getTimeZone('Etc/GMT+3'))
        date.format('yyyy-MM')
        Pattern p = ~"dsfsdfsd"
    }

    public static boolean isOkPsiElement(LeafPsiElement leafPsiElement) {
        PsiElement parent = leafPsiElement.getParent();
        if (!(parent instanceof PsiLiteral)) {
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
        if (true) {
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
                if (psiClass == null || !(psiClass.name.contains('SimpleDateFormat'))) {
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
                if (psiClass == null || !(psiClass.name.contains('SimpleDateFormat'))) {
                    log.debug "not a file"
                    return null;
                }
                return true
            }

            if (!(parent2 instanceof GrMethodCall)) {
                log.debug "not method"
                return null;
            }
            GrMethodCall methodCall = (GrMethodCall) parent2;
            GrExpression expression = methodCall.invokedExpression
            if (expression instanceof GrReferenceExpression) {
                GrReferenceExpression grReferenceExpression = (GrReferenceExpression) expression;
                if (grReferenceExpression.sameNameVariants.length != 1) {
                    log.debug "bad lenth"
                    return false
                }
                PsiElement element623 = grReferenceExpression.sameNameVariants[0].element
                if (element623 instanceof GrGdkMethod) {
                    GrGdkMethod gdkMethod = (GrGdkMethod) element623;
                    if (gdkMethod.staticMethod == null) {
                        log.debug "bad static method"
                        return false
                    }
                    if (gdkMethod.staticMethod.name != 'format') {
                        log.debug "not format"
                        return
                    }
                    return gdkMethod.staticMethod.containingClass.name == 'DateGroovyMethods'
                }
            }

            if (false) {
                Date date = null
                new SimpleDateFormat('yyyy-MM')
                new Date().format('yyyy-MM')
                date.format('yyyy-MM')
            }
        }
        return false;

    }

}
