package idea.plugins.thirdparty.filecompletion.jrr.charset

import com.intellij.patterns.ElementPattern
import com.intellij.patterns.ElementPatternCondition
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiLiteral
import com.intellij.psi.impl.compiled.ClsMethodImpl
import com.intellij.psi.impl.source.tree.LeafPsiElement
import com.intellij.util.ProcessingContext
import groovy.transform.CompileStatic
import idea.plugins.thirdparty.filecompletion.jrr.javassist.JavassistCompletionBean
import idea.plugins.thirdparty.filecompletion.jrr.remoterun.JrrIdeaBean
import net.sf.jremoterun.utilities.JrrClassUtils
import org.apache.log4j.LogManager
import org.apache.log4j.Logger
import org.jetbrains.annotations.Nullable
import org.jetbrains.plugins.groovy.lang.psi.api.statements.arguments.GrArgumentList
import org.jetbrains.plugins.groovy.lang.psi.api.statements.expressions.GrExpression
import org.jetbrains.plugins.groovy.lang.psi.api.statements.expressions.GrMethodCall
import org.jetbrains.plugins.groovy.lang.psi.api.statements.expressions.GrReferenceExpression

import java.nio.charset.Charset

@CompileStatic
public class MyAcceptCharsetProviderImpl implements ElementPattern<PsiElement> {
    private static final Logger log = LogManager.getLogger(JrrClassUtils.currentClass);

    private void testNotUsed() {
        Charset.forName('')
    }

    @Override
    public boolean accepts(@Nullable Object o) {
        //log.debug "timezone ${o}"
        if (o instanceof com.intellij.psi.impl.source.tree.LeafPsiElement) {
            JrrIdeaBean.bean.psiElement2 = (PsiElement)o;
            JavassistCompletionBean element = isOkPsiElement(o);
            log.debug "cp3 ${element}"
            return element != null
        }
        return false;
    }

    public static JavassistCompletionBean isOkPsiElement(LeafPsiElement leafPsiElement) {
        PsiElement parent = leafPsiElement.getParent();
        if (!(parent instanceof PsiLiteral)) {
            return null;
        }
        PsiLiteral literalElemtnt = parent as PsiLiteral;
        Object value = literalElemtnt.getValue();
        if (value instanceof String) {
        } else {
            return null;
        }
        JavassistCompletionBean completionBean = new JavassistCompletionBean();
        completionBean.literalElement = literalElemtnt;
        PsiElement parent1 = literalElemtnt.parent;
        if (!(parent1 instanceof GrArgumentList)) {
            return null;
        }

        GrArgumentList args = (GrArgumentList) parent1;
        completionBean.args = args
        if (args.allArguments.length != 1) {
            return null;
        }
        PsiElement parent2 = args.parent
        if (!(parent2 instanceof GrMethodCall)) {
            return null;
        }
        GrMethodCall methodCall = (GrMethodCall) parent2;
        PsiElement child = methodCall.children[0];
        if (!(child instanceof GrReferenceExpression)) {
            return null;
        }
        GrExpression expression = methodCall.invokedExpression
        if (expression instanceof GrReferenceExpression) {
            GrReferenceExpression grReferenceExpression = (GrReferenceExpression) expression;
            if (grReferenceExpression.sameNameVariants.length != 1) {
                log.debug "bad lenth"
                return null
            }
            PsiElement element623 = grReferenceExpression.sameNameVariants[0].element
            if (element623 instanceof ClsMethodImpl) {
                ClsMethodImpl gdkMethod = (ClsMethodImpl) element623;
                if (gdkMethod.mirror == null) {
                    log.debug "bad static method"
                    return null
                }
                if (gdkMethod.name != 'forName') {
                    log.debug "not format"
                    return null
                }
                if (gdkMethod.containingClass.name == 'Charset') {
                    return completionBean;
                }
            }
        }

        return null;

    }


    @Override
    public boolean accepts(@Nullable Object o, ProcessingContext context) {
        return accepts(o);
    }

    @Override
    public ElementPatternCondition<PsiElement> getCondition() {
        log.debug(1)
        return new ElementPatternCondition(null);
    }

}
