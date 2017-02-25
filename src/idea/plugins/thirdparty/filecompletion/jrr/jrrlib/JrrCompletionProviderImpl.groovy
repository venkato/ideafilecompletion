package idea.plugins.thirdparty.filecompletion.jrr.jrrlib

import com.intellij.codeInsight.completion.*
import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiField
import com.intellij.psi.PsiMethod
import com.intellij.psi.impl.source.tree.LeafPsiElement
import com.intellij.util.ProcessingContext
import groovy.transform.CompileStatic
import idea.plugins.thirdparty.filecompletion.jrr.file.MyAcceptFileProviderImpl
import net.sf.jremoterun.utilities.JrrClassUtils
import org.apache.log4j.LogManager
import org.apache.log4j.Logger
import org.jetbrains.annotations.NotNull

import javax.swing.*

@CompileStatic
public class JrrCompletionProviderImpl extends CompletionProvider<CompletionParameters> {

    private static final Logger log = LogManager.getLogger(JrrClassUtils.currentClass);

    @Override
    protected void addCompletions(
            @NotNull CompletionParameters parameters, ProcessingContext context, @NotNull CompletionResultSet result) {
        PsiElement psiElement = parameters.position;
        if (!(psiElement instanceof LeafPsiElement)) {
            return;
        }
        JrrCompletionBean completionBean = MyAcceptJrrProviderImpl.isOkPsiElement((LeafPsiElement) psiElement);
        String value4 = MyAcceptFileProviderImpl.getStringFromPsiLiteral(completionBean.literalElement);
//        int offset = value4.indexOf(IdeaMagic.addedConstant);
//        if(value4.length()==0){
//            offset = 0
//        }
        //String realValue = value4.replace(IdeaMagic.addedConstant, '');

//        if(offset==-1){
//            log.debug  "incorrect : ${value4}"
//            return
//        }
//        String valutoClac = value4.substring(0, offset);
//        if (offset < 0) {
//            log.error("invalid offset cp2 ${offset} , value = ${realValue} , value3 = ${value4}")
//            return;
//        }
//        log.debug("cp 9 : offset = ${offset} , value = ${realValue} , valutoClac = ${valutoClac}")
        if (ReflectionElement.reFields.contains(completionBean.methodName)) {
            completionBean.onObjectClass.allFields
                    .findAll { it.hasModifierProperty('static') == completionBean.onObjectStatic }
                    .sort { it.name }.each {
                PsiField field = it;
                JavaGlobalMemberLookupElement globalMemberLookupElement = new JavaGlobalMemberLookupElement(field, field.containingClass, null, null, false) {
                    public void handleInsert(InsertionContext insertionContext) {

                    }
                };
                LookupElement element = globalMemberLookupElement;
                result.addElement(element);
            }
        } else {
            completionBean.onObjectClass.allMethods
                    .findAll { it.hasModifierProperty('static') == completionBean.onObjectStatic }
                    .unique { "${it.name} ${it.parameterList.parametersCount}" }.sort {
                "${it.name} ${it.parameterList.parametersCount}"
            }.each {
                PsiMethod psiMethod222 = it;
                LookupElement element = new JavaGlobalMemberLookupElement(psiMethod222, psiMethod222.containingClass, null, null, false)
                result.addElement(element);
            }
        }
    }

    private void testNotUsed() {
        JButton testVar = null;
        JrrClassUtils.getFieldValue(JButton, "uiClassID")
        JrrClassUtils.getFieldValue(testVar, "ui")
        JrrClassUtils.getFieldValue(testVar, "uiClassID")
        JrrClassUtils.getFieldValue(testVar, "vetoableChangeSupport")
        JrrClassUtils.getFieldValue(testVar, "verifyInputWhenFocusTarget")
        JrrClassUtils.getFieldValue(testVar, "WHEN_IN_FOCUSED_WINDOW")
        JrrClassUtils.invokeMethod(testVar, "requestFocus")
        JrrClassUtils.invokeMethod(testVar, "")
    }
}
