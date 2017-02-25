package idea.plugins.thirdparty.filecompletion.jrr.file

import com.intellij.patterns.ElementPattern
import com.intellij.patterns.ElementPatternCondition
import com.intellij.psi.*
import com.intellij.psi.impl.source.tree.LeafPsiElement
import com.intellij.util.ProcessingContext
import groovy.transform.CompileStatic
import idea.plugins.thirdparty.filecompletion.jrr.IdeaMagic
import idea.plugins.thirdparty.filecompletion.jrr.remoterun.JrrIdeaBean
import net.sf.jremoterun.utilities.JrrClassUtils
import org.apache.log4j.LogManager
import org.apache.log4j.Logger
import org.jetbrains.annotations.Nullable
import org.jetbrains.plugins.groovy.lang.psi.GroovyPsiElement
import org.jetbrains.plugins.groovy.lang.psi.api.statements.GrField
import org.jetbrains.plugins.groovy.lang.psi.api.statements.GrVariable
import org.jetbrains.plugins.groovy.lang.psi.api.statements.arguments.GrArgumentList
import org.jetbrains.plugins.groovy.lang.psi.api.statements.expressions.GrNewExpression
import org.jetbrains.plugins.groovy.lang.psi.api.statements.expressions.GrReferenceExpression
import org.jetbrains.plugins.groovy.lang.psi.api.statements.expressions.GrSafeCastExpression
import org.jetbrains.plugins.groovy.lang.psi.api.statements.typedef.members.GrAccessorMethod
import org.jetbrains.plugins.groovy.lang.psi.impl.GrClassReferenceType

@CompileStatic
public class MyAcceptFileProviderImpl implements ElementPattern<PsiElement> {

    // this is log file
    public static final Logger log = LogManager.getLogger(JrrClassUtils.currentClass);


    @Override
    boolean accepts(@Nullable Object o) {
        log.debug " accept : ${o?.class.name}"
        if (o instanceof com.intellij.psi.impl.source.tree.LeafPsiElement) {
            JrrIdeaBean.bean.psiElement2 = (PsiElement)o;
            boolean accept = isOkJavaAndGroovyPsiElement(o, true) != null;
            return accept;
        }
        return false;
        //
    }

//    static FileCompletionBean isOkPsiElement2(PsiJavaToken leafPsiElement) {
//        File element = isOkJavaPsiElement(leafPsiElement);
//        if (element == null) {
//            return null
//        }
//        log.debug "found file : ${element}"
//        FileCompletionBean completionBean = new FileCompletionBean()
//        completionBean.value = element.name
//        completionBean.parentFilePath = element.parentFile
//        return completionBean;
//    }

    static File isOkJavaPsiElement3(PsiJavaToken leafPsiElement) {
        FileCompletionBean b = isOkJavaPsiElement(leafPsiElement);
        if(b==null){
            return null
        }
        File res
        if(b.parentFilePath==null){
            res= new File(b.value);
        }else{
            res = new File(b.parentFilePath,b.value);
        }
        return res;
    }

    static FileCompletionBean isOkJavaPsiElement(PsiJavaToken leafPsiElement) {
        if (!(leafPsiElement.parent.parent.parent instanceof PsiNewExpression)) {
            log.debug("not new expression")
            return null;
        }
        PsiNewExpression newExpression = (PsiNewExpression) leafPsiElement.parent.parent.parent
        log.debug "${newExpression}"
        FileCompletionBean res= fileViaJavaNewExpression3(newExpression, true);

        log.debug "${res}"
        return res;
    }

    static FileCompletionBean isOkJavaAndGroovyPsiElement(LeafPsiElement leafPsiElement, boolean deep) {
        if (leafPsiElement instanceof com.intellij.psi.impl.source.tree.java.PsiJavaTokenImpl) {
            return isOkJavaPsiElement(leafPsiElement)
        }
        PsiElement parent = leafPsiElement.getParent();
        if (!(parent instanceof PsiLiteral)) {
            return null;
        }
        PsiLiteral literalElemtnt = (PsiLiteral) parent;
        Object value = literalElemtnt.getValue();
        if (!(value instanceof String)) {
            return null;
        }
        FileCompletionBean completionBean = new FileCompletionBean();
        completionBean.value = getStringFromPsiLiteral(literalElemtnt)
        if (completionBean.value == null) {
            return null;
        }
        // log.debug "value = ${completionBean.value}"
        completionBean.literalElemtnt = literalElemtnt;
        PsiElement parent1 = literalElemtnt.parent;
        if (parent1 instanceof GrArgumentList) {
            PsiElement parent2 = parent1.parent
            if (parent2 instanceof GrNewExpression) {
                boolean acceped = fileViaGrNewExpression(parent2 as GrNewExpression, completionBean, deep)
                if (acceped) {
                    return completionBean
                }
                return null
            }
        }
        if (parent1 instanceof GrSafeCastExpression) {
            GrSafeCastExpression castExpression = (GrSafeCastExpression) parent1;
            File file = resolveFileFromSafeCast(castExpression)
            if (file != null) {
                return completionBean
            }

        }
        return null;
    }

    static File resolveFileFromSafeCast(GrSafeCastExpression castExpression) {
        String text = castExpression.type.presentableText
        if (text != null && text.contains('File')) {
            log.debug("accpted")
            String literal = getStringFromPsiLiteral(castExpression.operand)
            if (literal == null) {
                log.debug "not a tsring"
                return null;
            }
            return new File(literal);
        }
        return null
    }

    /**
     * Internal method
     */
    private
    static boolean fileViaGrNewExpression(GrNewExpression grExpression, FileCompletionBean completionBean, boolean deep) {
        PsiType type = grExpression.type;
        if (!(type instanceof GrClassReferenceType)) {
            return false;
        }
        GrClassReferenceType grClassReferenceType = (GrClassReferenceType) type;
        PsiClass resolve = grClassReferenceType.resolve()

        if (resolve == null || !(resolve.name.contains('File'))) {
            log.debug("accpted")
            return false;
        }
        if (grExpression.argumentList.allArguments.length == 1) {
            String literal = getStringFromPsiLiteral(grExpression.argumentList.allArguments[0]);
            if (literal == null) {
                log.debug "not a string"
                return false
            }
            return true;
        }
        if (!deep) {
            return false;
        }
        if (grExpression.argumentList.allArguments.length == 2) {
            completionBean.parentFilePath = fileViaGroovyNewExpression3(grExpression, false)
            return completionBean.parentFilePath != null;
        }
        return false
    }

    static String getStringFromPsiLiteral(PsiElement psiElement) {
        if (!(psiElement instanceof PsiLiteral)) {
            return null;
        }
        PsiLiteral literalElemtnt = (PsiLiteral) psiElement;
        Object value = literalElemtnt.getValue();
        if (!(value instanceof String)) {
            return null;
        }
        String value2 = (String) value;
        return value2.replace(IdeaMagic.addedConstant, '');
    }

    /**
     * Resolve java construction new File('path')
     * java construction not supported : new File(parent,'child')
     * but for groovy supported
     * @param grExpression
     * @param addSuffix
     * @return
     */
    static FileCompletionBean fileViaJavaNewExpression3(PsiNewExpression grExpression, boolean addSuffix) {
        PsiType type = grExpression.type;
        if (!(type instanceof PsiClassType)) {
            log.debug "not a type"
            return null;
        }
        PsiClassType grClassReferenceType = (PsiClassType) type;
        PsiClass psiClass = grClassReferenceType.resolve();
        if (psiClass == null || psiClass.name == null || !(psiClass.name.contains('File'))) {
            log.debug "not a file"
            return null;
        }
        if (grExpression.argumentList.expressions.length == 1) {
            PsiExpression element = grExpression.argumentList.expressions[0]
            String value2 = getStringFromPsiLiteral(element);
            if (value2 == null) {
                log.debug "not a string"
                return null;
            }
            FileCompletionBean fileCompletionBean =new FileCompletionBean()
            fileCompletionBean.value = value2
            return fileCompletionBean;
        }
        if (grExpression.argumentList.expressions.length == 2) {
            log.debug "too many args"
            return null;
        }
        log.debug "not implemented"
        return null
    }

    static File fileViaJavaNewExpression2(PsiNewExpression grExpression) {
        PsiType type = grExpression.type;
        if (!(type instanceof PsiClassType)) {
            log.debug "not a type"
            return null;
        }
        PsiClassType grClassReferenceType = (PsiClassType) type;
        PsiClass psiClass = grClassReferenceType.resolve();
        if (psiClass == null || psiClass.name == null || !(psiClass.name.contains('File'))) {
            log.debug "not a file"
            return null;
        }
        if (grExpression.argumentList.expressions.length == 1) {
            PsiExpression element = grExpression.argumentList.expressions[0]
            String value2 = getStringFromPsiLiteral(element);
            if (value2 == null) {
                log.debug "not a string"
                return null;
            }
            return new File(value2);
        }
        if (grExpression.argumentList.expressions.length != 2) {
            log.debug "too many args"
            return null;
        }
        log.debug "not implemented"
        return null
    }

    /**
     * Resolve groovy construction new File('path') and new File(parent,'path')
     * @param grExpression
     * @param addSuffix indecatios if need resolve child path for 2 args constructor
     * @return
     */
    static File fileViaGroovyNewExpression3(GrNewExpression grExpression, boolean addSuffix) {
        PsiType type = grExpression.type;
        if (!(type instanceof GrClassReferenceType)) {
            log.debug "not a type"
            return null;
        }
        PsiClassType grClassReferenceType = (PsiClassType) type;
        PsiClass resolve = grClassReferenceType.resolve()

        if (resolve == null || !(resolve.name.contains('File'))) {
            log.debug "not a file"
            return null;
        }
        if (grExpression.argumentList.allArguments.length == 1) {
            GroovyPsiElement element = grExpression.argumentList.allArguments[0]
            String value2 = getStringFromPsiLiteral(element);
            if (value2 == null) {
                log.debug "not a string"
                return null;
            }
            return new File(value2);
        }
        if (grExpression.argumentList.allArguments.length != 2) {
            log.debug "too many args"
            return null;
        }
        GroovyPsiElement arg = grExpression.argumentList.allArguments[0]
        if (!(arg instanceof GrReferenceExpression)) {
            log.debug "not a refernce"
            return null
        }
        GrReferenceExpression arg1 = (GrReferenceExpression) arg;
        if (!(arg1.sameNameVariants?.length == 1)) {
            log.debug "args not 1"
            return null;
        }
        File fileParent = findFileFromVarGeneric(arg1.sameNameVariants[0].element);
        if (fileParent == null) {
            return null
        }
        if (!addSuffix) {
            return fileParent
        }
        String suffixFile = getStringFromPsiLiteral(grExpression.argumentList.allArguments[1]);
        if (suffixFile == null) {
            return null
        }
        return new File(fileParent, suffixFile)
    }

    /**
     * Calculate path for field.
     * Example : File parent = new File('/opt');
     * use parent somewhere. This method calc path for parent field
     */
    static File findFileFromField(PsiField psiField) {
        if (psiField.navigationElement != null && psiField.navigationElement instanceof PsiField) {
            log.debug "use navigation el"
            psiField = psiField.navigationElement as PsiField
        }
        PsiExpression initializer = psiField.initializer
        if (initializer == null) {
            if (psiField instanceof PsiCompiledElement) {
                PsiElement mirror = psiField.getMirror();
                if (mirror instanceof PsiField) {
                    psiField = (PsiField) mirror;
                } else {
                    log.debug "not a field"
                    return null;
                }
            } else {
                log.debug "not a psi compile"
                return null;
            }
        }
        if (initializer instanceof PsiNewExpression) {
            PsiNewExpression grNewExpressionRef = (PsiNewExpression) initializer
            File fileResolved = fileViaJavaNewExpression2(grNewExpressionRef);
            return fileResolved;
        } else {
            if (psiField instanceof GrField) {
                GrField grField = (GrField) psiField;
                switch (grField) {
                    case { grField.initializerGroovy instanceof GrNewExpression }:
                        GrNewExpression grNewExpressionRef = (GrNewExpression) grField.initializerGroovy
                        File fileResolved = fileViaGroovyNewExpression3(grNewExpressionRef, true);
                        return fileResolved
                    case { grField.initializerGroovy instanceof GrSafeCastExpression }:
                        GrSafeCastExpression sc = grField.initializerGroovy as GrSafeCastExpression
                        return resolveFileFromSafeCast(sc)
                    default:
                        JrrIdeaBean.bean.psiElement3 = psiField
                        log.debug "not PsiNewExpression : ${initializer} ${initializer?.class}"
                        return null
                }
            } else {
                log.debug "not gr field ${psiField?.class.name} ${psiField}"
                return null
            }
        }
        log.debug "why here ${psiField}"
        return null
    }

    /**
     * Calculate path for groovy var.
     * Example : File parent = new File('/opt');
     * use parent somewhere. This method calc path for parent var
     */
    static File findFileFromGrVar(GrVariable grVariable) {
        switch (grVariable) {
            case { grVariable.initializerGroovy instanceof GrSafeCastExpression }:
                GrSafeCastExpression castExpression = (GrSafeCastExpression) grVariable.initializerGroovy;
                return resolveFileFromSafeCast(castExpression)
            case { grVariable.initializerGroovy instanceof GrNewExpression }:
                GrNewExpression grNewExpressionRef = (GrNewExpression) grVariable.initializerGroovy
                return fileViaGroovyNewExpression3(grNewExpressionRef, true);

        }
        log.debug "not a new expression"
        return null;
//        log.debug "varRef no ${varRef.class.name} ${varRef}"
        return null

    }

    /**
     * Calculate path for var.
     * Example : File parent = new File('/opt');
     * use parent somewhere. This method calc path for parent var
     */
    static File findFileFromVarGeneric(PsiElement varRef) {
        if (varRef instanceof PsiField) {
            PsiField psiField = (PsiField) varRef
            return findFileFromField(psiField)
        }
        if (varRef instanceof GrVariable) {
            return findFileFromGrVar(varRef as GrVariable)
        }
        if (varRef instanceof GrAccessorMethod) {
            PsiElement navigationElement = varRef.navigationElement
            if(navigationElement!=varRef){
                return findFileFromVarGeneric(navigationElement)
            }
        }
        log.debug "not a GrVar : ${varRef.class} ${varRef}"
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
