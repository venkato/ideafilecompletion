package idea.plugins.thirdparty.filecompletion.jrr.maven

import com.intellij.patterns.ElementPattern
import com.intellij.patterns.ElementPatternCondition
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiClassType
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiLiteral
import com.intellij.psi.PsiType
import com.intellij.psi.impl.compiled.ClsMethodImpl
import com.intellij.psi.impl.source.tree.LeafPsiElement
import com.intellij.util.ProcessingContext
import groovy.transform.CompileStatic
import idea.plugins.thirdparty.filecompletion.jrr.javassist.JavassistCompletionBean
import idea.plugins.thirdparty.filecompletion.jrr.remoterun.JrrIdeaBean
import net.sf.jremoterun.utilities.JrrClassUtils
import net.sf.jremoterun.utilities.classpath.MavenId
import org.apache.log4j.LogManager
import org.apache.log4j.Logger
import org.jetbrains.annotations.Nullable
import org.jetbrains.plugins.groovy.lang.psi.api.statements.arguments.GrArgumentList
import org.jetbrains.plugins.groovy.lang.psi.api.statements.expressions.GrExpression
import org.jetbrains.plugins.groovy.lang.psi.api.statements.expressions.GrMethodCall
import org.jetbrains.plugins.groovy.lang.psi.api.statements.expressions.GrNewExpression
import org.jetbrains.plugins.groovy.lang.psi.api.statements.expressions.GrReferenceExpression

@CompileStatic
public class MyAcceptMavenProviderImpl implements ElementPattern<PsiElement> {
    private static final Logger log = LogManager.getLogger(JrrClassUtils.currentClass);

    private void testNotUsed() {
        new MavenId('cglib:cglib-nodep:3.2.0');
    }

    @Override
    public boolean accepts(@Nullable Object o) {
        //log.debug "timezone ${o}"
        if (o instanceof com.intellij.psi.impl.source.tree.LeafPsiElement) {
            JrrIdeaBean.bean.psiElement2 = (PsiElement)o;
            boolean element = isOkPsiElement(o);
            log.debug "cp3 ${element}"
            return element
        }
        return false;
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
        if (!(parent2 instanceof GrNewExpression)) {
            return null;
        }
        GrNewExpression grNewExpression = (GrNewExpression) parent2;
        PsiType type = grNewExpression.type;
        if (!(type instanceof PsiClassType)) {
            log.debug "not a type"
            return null;
        }
        PsiClassType grClassReferenceType = (PsiClassType) type;
        PsiClass psiClass = grClassReferenceType.resolve();
        if (psiClass == null || !(psiClass.name.contains(MavenId.simpleName))) {
            log.debug "not a maven id"
            return null;
        }

        return true;

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
