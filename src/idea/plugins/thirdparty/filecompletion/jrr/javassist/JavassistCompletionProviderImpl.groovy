package idea.plugins.thirdparty.filecompletion.jrr.javassist

import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.codeInsight.completion.CompletionProvider
import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.codeInsight.completion.JavaMethodCallElement
import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiMethod
import com.intellij.psi.impl.source.tree.LeafPsiElement
import com.intellij.util.ProcessingContext
import groovy.transform.CompileStatic
import idea.plugins.thirdparty.filecompletion.jrr.IdeaMagic
import idea.plugins.thirdparty.filecompletion.jrr.file.MyAcceptFileProviderImpl
import idea.plugins.thirdparty.filecompletion.jrr.jrrlib.ReflectionElement
import net.sf.jremoterun.utilities.JrrClassUtils
import org.apache.log4j.LogManager
import org.apache.log4j.Logger
import org.jetbrains.annotations.NotNull

import javax.swing.*

@CompileStatic
public class JavassistCompletionProviderImpl extends CompletionProvider<CompletionParameters> {

    private static final Logger log = LogManager.getLogger(JrrClassUtils.currentClass);

    public static EnumSet<ReflectionElement> reFields = EnumSet.of(ReflectionElement.setFieldValue,
            ReflectionElement.getFieldValue, ReflectionElement.findField);

    public static EnumSet<ReflectionElement> reMethods = EnumSet.of(ReflectionElement.invokeMethod,
            ReflectionElement.findMethod);

    @Override
    protected void addCompletions(
            @NotNull CompletionParameters parameters, ProcessingContext context, @NotNull CompletionResultSet result) {
        PsiElement psiElement = parameters.position;
        if (!(psiElement instanceof LeafPsiElement)) {
            return;
        }
        JavassistCompletionBean completionBean = MyAcceptJavassistProviderImpl.isOkPsiElement((LeafPsiElement) psiElement);
        String value4 = MyAcceptFileProviderImpl.getStringFromPsiLiteral(completionBean.literalElement);
        String realValue = value4.replace(IdeaMagic.addedConstant, '');
        // int offset = value4.indexOf(IdeaMagic.addedConstant);
        // String valutoClac = value4.substring(0, offset);
        log.debug("cp 8 : value = ${realValue}")
        completionBean.onObjectClass.allMethods.sort { "${it.name} ${it.parameterList.parametersCount}" }.each {
            PsiMethod psiMethod222 = (PsiMethod) it;
            LookupElement element = new JavaMethodCallElement(psiMethod222)
            result.addElement(element);
        }
    }

    private void testNotUsed() {
        JButton testVar = null;
        JrrClassUtils.getFieldValue(testVar, "vetoableChangeSupport")
        JrrClassUtils.invokeMethod(testVar, "requestFocus")
        JrrClassUtils.getFieldValue(testVar, "verifyInputWhenFocusTarget")
        JrrClassUtils.invokeMethod(testVar, "focu")
    }
}
