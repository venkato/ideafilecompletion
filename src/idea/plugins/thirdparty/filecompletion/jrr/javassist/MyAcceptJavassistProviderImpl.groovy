package idea.plugins.thirdparty.filecompletion.jrr.javassist

import com.intellij.patterns.ElementPattern
import com.intellij.patterns.ElementPatternCondition
import com.intellij.psi.*
import com.intellij.psi.impl.source.tree.LeafPsiElement
import com.intellij.util.ProcessingContext
import groovy.transform.CompileStatic
import idea.plugins.thirdparty.filecompletion.jrr.remoterun.JrrIdeaBean
import javassist.CtBehavior
import net.sf.jremoterun.utilities.JrrClassUtils
import net.sf.jremoterun.utilities.javassist.JrrJavassistUtils
import org.apache.log4j.LogManager
import org.apache.log4j.Logger
import org.jetbrains.annotations.Nullable
import org.jetbrains.plugins.groovy.lang.psi.GroovyPsiElement
import org.jetbrains.plugins.groovy.lang.psi.api.statements.GrVariable
import org.jetbrains.plugins.groovy.lang.psi.api.statements.arguments.GrArgumentList
import org.jetbrains.plugins.groovy.lang.psi.api.statements.expressions.GrMethodCall
import org.jetbrains.plugins.groovy.lang.psi.api.statements.expressions.GrReferenceExpression

@CompileStatic
public class MyAcceptJavassistProviderImpl implements ElementPattern<PsiElement> {
    private static final Logger log = LogManager.getLogger(JrrClassUtils.currentClass);


    @Override
    public boolean accepts(@Nullable Object o) {
        if (o instanceof com.intellij.psi.impl.source.tree.LeafPsiElement) {
            JrrIdeaBean.bean.psiElement2 = (PsiElement)o;

            boolean accepted = isOkPsiElement((LeafPsiElement)o) != null;
            log.debug "${accepted} ${o}"
            return accepted
        }
        return false;
    }

    private void testNotUsed() {
        Class clazz = ArrayList
        JrrJavassistUtils.findMethod(clazz, null, "", 1);

        Class class1 = com.intellij.idea.IdeaLogger.class;
        CtBehavior invokeMethod = JrrJavassistUtils.findMethod(class1, null, "logErrorHeader", 0);
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
//        if(args.allArguments.length!=4){
//            log.debug "too small args"
//            return null
//        }
        if (true) {
            PsiElement parent2 = args.parent
            if ((parent2 instanceof PsiMethodCallExpression)) {
                PsiMethodCallExpression expression = (PsiMethodCallExpression) parent2

            } else if (!(parent2 instanceof GrMethodCall)) {
                log.debug "not method"
                return null;
            }
            //GrMethodCall methodCall = (GrMethodCall) parent2;
            PsiElement child = parent2.children[0];
            if ((child instanceof PsiReferenceExpression)) {

            } else if (!(child instanceof GrReferenceExpression)) {
                log.debug "not gr ref"
                return null;
            }

            //GrReferenceExpression referenceExpression = (GrReferenceExpression) child;

            if (!(child.text?.contains('JrrJavassistUtils.'))) {
                log.debug "no jrrJavassistUtil method"
                return null;
            }


        }

//        if (args.allArguments.length < 3) {
//
//            return null;
//        }
//        if (args.allArguments[1] != literalElemtnt) {
//            return null;
//        }
        if (args != null || args.expressionTypes == null || args.expressionTypes.length == 0) {
//log.debug "wrong args ${args.expressionTypes}"

        } else {
            PsiType type2 = args.expressionTypes[0]
            if (type2 instanceof PsiClassType) {
                PsiClassType typeee = (PsiClassType) type2;
                PsiClass resolve = typeee.resolve()
                if (resolve == 'java.lang.Class') {
                    if (typeee.parameters == null || typeee.parameters.length != 1) {
                        log.debug "bad generic ${typeee}"
                        return null
                    }
                    if (typeee.parameters[0] instanceof PsiClassType) {
                        PsiClassType sss = (PsiClassType) typeee.parameters[0]
                        completionBean.onObjectClass = sss.resolve()
                        return completionBean
                    } else {
                        log.debug "bad generic ${typeee.parameters[0]}"
                        return null
                    }

                } else {
                    completionBean.onObjectClass = resolve
                }
                return completionBean;
            }
        }

        if (!(parent1 instanceof GrArgumentList)) {
            log.debug "not gr args"
            return null;
        }

        GrArgumentList args3 = (GrArgumentList) parent1;
        if (!(args3.allArguments[0] instanceof GrReferenceExpression)) {
            log.debug "not ger ref"
            return null;
        }
        GrReferenceExpression onObject = (GrReferenceExpression) args3.allArguments[0];
        GroovyPsiElement arg = args3.allArguments[0]
        if (!(arg instanceof GrReferenceExpression)) {
            log.debug "not a refernce"
            return null
        }
        GrReferenceExpression grReferenceExpression = (GrReferenceExpression) arg
        PsiElement varRef = grReferenceExpression.sameNameVariants[0].element;
        if (!(varRef instanceof GrVariable)) {
            log.debug "not a GrVar : ${varRef.class} ${varRef}"
            return null;
        }
        GrVariable grVariable = varRef as GrVariable
        if (grVariable.initializerGroovy == null) {
            log.debug "init is null"
            return null
        }
        if (!(grVariable.initializerGroovy instanceof GrReferenceExpression)) {
            log.debug "init not gr"
            return null
        }
        GrReferenceExpression grReferenceExpression1 = (GrReferenceExpression) grVariable.initializerGroovy
        if (grReferenceExpression1.sameNameVariants.length != 1) {
            log.debug "no same vars"
            PsiType type = grReferenceExpression1.type;
            if (!(type instanceof PsiClassType)) {
                return null
            }
            PsiClassType psiClassType = (PsiClassType) type;
            if (psiClassType.parameters.length != 1) {
                return null;
            }
            PsiType type1 = psiClassType.parameters[0]
            if (!(type1 instanceof PsiClassType)) {
                return null
            }
            PsiClassType psiClassType1 = (PsiClassType) type1;
            completionBean.onObjectClass = psiClassType1.resolve();

        } else {
            PsiElement element = grReferenceExpression1.sameNameVariants[0].element;
            if (!(element instanceof PsiClass)) {
                log.debug "not psi class"
                return null;
            }
            completionBean.onObjectClass = (PsiClass) element;
        }


        return completionBean;

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
