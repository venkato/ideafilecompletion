package idea.plugins.thirdparty.filecompletion.jrr

import com.intellij.codeInsight.navigation.actions.GotoDeclarationHandler
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.progress.ProcessCanceledException
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiJavaToken
import com.intellij.psi.PsiLiteral
import com.intellij.psi.PsiMethod
import com.intellij.psi.impl.source.tree.LeafPsiElement
import groovy.transform.CompileStatic
import idea.plugins.thirdparty.filecompletion.jrr.file.FileCompletionBean
import idea.plugins.thirdparty.filecompletion.jrr.file.MyAcceptFileProviderImpl
import idea.plugins.thirdparty.filecompletion.jrr.file.MySyntheticFileSystemItem
import idea.plugins.thirdparty.filecompletion.jrr.javassist.JavassistCompletionBean
import idea.plugins.thirdparty.filecompletion.jrr.javassist.MyAcceptJavassistProviderImpl
import idea.plugins.thirdparty.filecompletion.jrr.jrrlib.JrrCompletionBean
import idea.plugins.thirdparty.filecompletion.jrr.jrrlib.MyAcceptJrrProviderImpl
import idea.plugins.thirdparty.filecompletion.jrr.jrrlib.ReflectionElement
import net.sf.jremoterun.utilities.JrrClassUtils
import net.sf.jremoterun.utilities.JrrUtilities
import org.apache.log4j.LogManager
import org.apache.log4j.Logger
import org.jetbrains.annotations.Nullable
import org.jetbrains.plugins.groovy.lang.psi.api.statements.arguments.GrArgumentList

import javax.swing.JOptionPane
import javax.swing.SwingUtilities

@CompileStatic
class GotoDeclarationHandlerImpl implements GotoDeclarationHandler {
    private static final Logger log = LogManager.getLogger(JrrClassUtils.currentClass);



    @Override
    PsiElement[] getGotoDeclarationTargets(@Nullable PsiElement sourceElement, int offset, Editor editor) {
        try{
            return getGotoDeclarationTargetsImpl(sourceElement,offset,editor)
        }catch (ProcessCanceledException e){
            log.debug(e)
            throw e;
        }catch (Throwable e){
            JrrUtilities.showException("Failed on calc goto delcarion",e)
            return new PsiElement[0];
        }
    }


    PsiElement[] getGotoDeclarationTargetsImpl(@Nullable PsiElement sourceElement, int offset, Editor editor) {
        PsiElement el = getGotoDeclarationTargetsImpl2(sourceElement,offset,editor);
        if(el == null){
            return new PsiElement[0];
        }
        PsiElement[] result = new PsiElement[1]
        result[0] = el
        return result;
    }

    PsiElement getGotoDeclarationTargetsImpl2(@Nullable PsiElement sourceElement, int offset, Editor editor) {
        // log.debug "getGoTo ${sourceElement?.class.name} ${sourceElement}"
        if (sourceElement instanceof PsiJavaToken) {
            PsiJavaToken to = (PsiJavaToken) sourceElement
            File file = MyAcceptFileProviderImpl.isOkJavaPsiElement3(to);
            if (file == null) {

            } else {
                return  createFileELlemnt(file)
            }
        }
        if (sourceElement instanceof LeafPsiElement) {
            LeafPsiElement element = (LeafPsiElement) sourceElement;
            return  gotoImpl(element);
        }
        return null
    }

    public static PsiElement createFileELlemnt(File file) {
        if (file.exists()) {
            if (file.isDirectory()) {
                log.info "file is dir : ${file}"
//                SwingUtilities.invokeLater {
//                    JOptionPane.showMessageDialog(null,"File is dir : ${file.name}")
//                }
                return null;
            }
            if (file.length() > 1000 * 500) {
                log.debug "file size too big : ${file} ${file.length()}"
//                SwingUtilities.invokeLater {
//                    JOptionPane.showMessageDialog(null,"file size too big : ${file.name} ${file.length()}")
//                }
                return null;
            }
            return new MySyntheticFileSystemItem(file);
        } else {
            log.debug "file not exists ${file}"
//            SwingUtilities.invokeLater {
//                JOptionPane.showMessageDialog(null,"file not exists ${file.absolutePath}")
//            }
        }
        return null;
    }

    PsiElement gotoImpl(LeafPsiElement sourceElement) {
        FileCompletionBean element = MyAcceptFileProviderImpl.isOkJavaAndGroovyPsiElement(sourceElement, true);
        if (element != null) {
            File file;
            if (element.parentFilePath == null) {
                file = new File(element.value)
            } else {
                file = new File(element.parentFilePath, element.value);

            }
            return createFileELlemnt(file)
        }
        JavassistCompletionBean element1 = MyAcceptJavassistProviderImpl.isOkPsiElement(sourceElement)
        if (element1 != null) {
            Object value = element1.literalElement.getValue();
            if (!(value instanceof String)) {
                return null;
            }
            String value4 = (String) value;
            // ;
            //int offset = value3.indexOf(addedConstant);
            String realValue = value4.replace(IdeaMagic.addedConstant, '');
            //.collect{LookupElementBuilder.create(it.name)}
            if (!(element1.args instanceof GrArgumentList)) {
                log.debug("no gr ")
            }
            GrArgumentList argumentList = (GrArgumentList) element1.args;
            PsiLiteral grLiteral3 = (PsiLiteral) argumentList.allArguments[3]
            Integer paramCount = (Integer) grLiteral3.value;
            log.debug "paramCount = ${paramCount}"
            PsiMethod find = element1.onObjectClass.allMethods.find {
                it.name == realValue && it.parameterList.parametersCount == paramCount
            };
            return find;
        }
        JrrCompletionBean element2 = MyAcceptJrrProviderImpl.isOkPsiElement(sourceElement)
        if (element2 != null) {
            Object value = element2.literalElement.getValue();
            if (!(value instanceof String)) {
                return null;
            }
            String value4 = (String) value;
            String realValue = value4.replace(IdeaMagic.addedConstant, '');
            if (ReflectionElement.reFields.contains(element2.methodName)) {
                return element2.onObjectClass.allFields.find { it.name == realValue }
            } else {
                int paramCount = -1
                switch (element2.methodName) {
                    case ReflectionElement.findMethod:
                        if (element2.args.allArguments[3] instanceof PsiLiteral) {
                            if (!(element1.args instanceof GrArgumentList)) {
                                log.debug("no gr ")
                            }
                            GrArgumentList argumentList = (GrArgumentList) element1.args;
                            PsiLiteral literal333 = (PsiLiteral) argumentList.allArguments[3];
                            if (literal333.value instanceof Integer) {
                                paramCount = (Integer) literal333.value;
                            }
                        }
                        break;
                }
                if (paramCount == -1) {
                    paramCount = element2.args.allArguments.length - 2
                }
                log.debug "paramCount = ${paramCount}"
                return element2.onObjectClass.allMethods.find {
                    it.name == realValue && it.parameterList.parametersCount == paramCount
                };
            }
        }
        return null
    }

    @Override
    String getActionText(DataContext context) {
        return "Open file in Idea"
    }

}
