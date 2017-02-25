package idea.plugins.thirdparty.filecompletion.jrr.file

import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.codeInsight.completion.CompletionProvider
import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.codeInsight.lookup.AutoCompletionPolicy
import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.openapi.util.IconLoader
import com.intellij.psi.PsiJavaToken
import com.intellij.psi.impl.source.tree.LeafPsiElement
import com.intellij.util.ProcessingContext
import groovy.transform.CompileStatic
import idea.plugins.thirdparty.filecompletion.jrr.IdeaMagic
import idea.plugins.thirdparty.filecompletion.share.OSIntegrationIdea
import net.sf.jremoterun.utilities.JrrClassUtils
import org.apache.log4j.LogManager
import org.apache.log4j.Logger
import org.jetbrains.annotations.NotNull

import javax.swing.*

@CompileStatic
public class FileCompletionProviderImpl extends CompletionProvider<CompletionParameters> {

    private static final Logger log = LogManager.getLogger(JrrClassUtils.currentClass);

    static Icon fileIcon = IconLoader.getIcon('/icons/file.png', OSIntegrationIdea);
    static Icon folderIcon = IconLoader.getIcon('/icons/folder.png', OSIntegrationIdea);


    @Override
    protected void addCompletions(
            @NotNull CompletionParameters parameters, ProcessingContext context, @NotNull CompletionResultSet result) {
        if (result.isStopped()) {
            return;
        }
        assert fileIcon != null
        assert folderIcon != null
        FileCompletionBean completionBean;
        final int startOffset
        // PsiElement psiElement = parameters.position
        boolean javaAss = parameters.position instanceof PsiJavaToken;
        LeafPsiElement psiElement3 = (LeafPsiElement) parameters.position;
        startOffset = psiElement3.startOffset
        completionBean = MyAcceptFileProviderImpl.isOkJavaAndGroovyPsiElement(psiElement3, true);
        if (completionBean == null) {
            return;
        }
        if (result.isStopped()) {
            return;
        }
        String value3 = completionBean.value;
// MyAcceptFileProviderImpl.getStringFromPsiLiteral(completionBean.literalElemtnt);
        if (value3 == null) {
            log.error("value is null")
            return;
        }
        String realValue = value3.replace(IdeaMagic.addedConstant, '');
//        boolean hasWindSlash = realValue.contains('\\');
//        if (hasWindSlash) {
//            realValue = realValue.replace('\\\\', '/')
//            realValue = realValue.replace('\\', '/')
//        }
        // JrrIdeaBean.bean.psiElement3 = psiElement;
        log.debug "parent file :  ${completionBean.parentFilePath}"
        // ;
        int offset23 = parameters.offset//getCursorOffsetInCurrentEditor();

        int offset = offset23 - startOffset - 1
        log.debug("offset cp3 ${offset} , value = ${realValue} , value3 = ${value3} ${startOffset} ${offset23}")
        if (offset < 0) {
            log.error("invalid offset cp2 ${offset} , value = ${realValue} , value3 = ${value3} ${startOffset} ${offset23}")
            return;
        }
        String value5 = value3.replace('\\', '\\\\')
        String calcValue;
        if (true) {
            int correction = value5.substring(0, offset).count('\\\\');
            log.debug "correction 2 : ${correction} ${value3}"
            calcValue = value3.substring(0, offset - correction)
        }
        String calcValue2 = calcValue
        boolean hasWindSlash = calcValue.contains('\\');
        if (hasWindSlash) {
            calcValue2 = calcValue2.replace('\\\\', '/')
            calcValue2 = calcValue2.replace('\\', '/')
            int correction = calcValue.length() - calcValue2.length()
            if (correction == 0) {
                correction = calcValue.count('\\')
            }
            log.info "correction = ${correction} ${calcValue}"
            offset = offset - correction
        }
        if (offset > calcValue2.length()) {
            log.error("offset too big ${offset} , value = ${realValue} , value3 = ${value3} ${startOffset} ${offset23}")
            return;
        }
        log.debug("offset = ${offset} , value = ${realValue}")
        List<File> proposals = FileProposalCalculator.calculateProposals(calcValue2, offset, completionBean.parentFilePath, result)
        proposals = proposals.sort { it.name }
        log.debug("proposals count cp2 = ${proposals.size()}")
        log.debug("hasWindSlash = ${hasWindSlash}")
        if (result.isStopped()) {
            return;
        }
        String relativePath = completionBean.parentFilePath?.absolutePath?.tr('\\', '/')
        proposals.collect {
            if (result.isStopped()) {
                return;
            }
            File aaa = it;
            String insertStr
            if (javaAss) {
                insertStr = aaa.name
                if (insertStr == '\\' || insertStr == '/') {
                    insertStr = aaa.absolutePath
                }
            } else {
                insertStr = aaa.absolutePath;
            }
            insertStr = insertStr.tr('\\', '/');
            if (relativePath != null) {
                if (insertStr.length() < relativePath.length()) {
                    log.debug "bad cal insertStr = ${insertStr} , rel path = ${relativePath} , has slash ${hasWindSlash} , javaAss = ${javaAss}"
                    return
                }
                insertStr = insertStr.substring(relativePath.length())
                if (insertStr.startsWith('/')) {
                    insertStr = insertStr.substring(1)

                }

            }
            if (aaa.isDirectory() && !insertStr.endsWith('/')) {
                insertStr += '/'
            }
            if (hasWindSlash) {
                insertStr = insertStr.replace('/', '\\\\')
            } else {
                insertStr = insertStr.tr('\\', '/')
            }

            // insertStr += ' '+(aaa.isFile()?'f':'d')
            log.debug "insertStr = ${insertStr} , orig file = ${aaa.absolutePath} , rel path = ${relativePath} , has slash ${hasWindSlash}"
            Icon icon = aaa.isFile() ? fileIcon : folderIcon;
            LookupElement element23 = LookupElementBuilder.create(insertStr).withIcon(icon).withCaseSensitivity(false).withAutoCompletionPolicy(AutoCompletionPolicy.GIVE_CHANCE_TO_OVERWRITE);
            FileLookupElement lookupElement = new FileLookupElement(element23, aaa, icon)
            result.addElement(lookupElement);
        };
    }


    private void testNoyUsed() {
        new File('')

    }


}
