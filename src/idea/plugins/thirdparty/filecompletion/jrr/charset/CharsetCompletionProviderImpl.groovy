package idea.plugins.thirdparty.filecompletion.jrr.charset

import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.codeInsight.completion.CompletionProvider
import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.openapi.util.IconLoader
import com.intellij.psi.PsiElement
import com.intellij.psi.impl.source.tree.LeafPsiElement
import com.intellij.util.ProcessingContext
import groovy.transform.CompileStatic
import idea.plugins.thirdparty.filecompletion.jrr.IdeaMagic
import idea.plugins.thirdparty.filecompletion.jrr.file.MyAcceptFileProviderImpl
import idea.plugins.thirdparty.filecompletion.jrr.javassist.JavassistCompletionBean
import idea.plugins.thirdparty.filecompletion.share.OSIntegrationIdea
import net.sf.jremoterun.utilities.JrrClassUtils
import org.apache.log4j.LogManager
import org.apache.log4j.Logger
import org.jetbrains.annotations.NotNull

import javax.swing.*
import java.nio.charset.Charset

@CompileStatic
public class CharsetCompletionProviderImpl extends CompletionProvider<CompletionParameters> {

    private static final Logger log = LogManager.getLogger(JrrClassUtils.currentClass);

    static Icon timeZoneIcon = IconLoader.getIcon('/icons/time_zone.png', OSIntegrationIdea);

    //static Map<String,Charset> timeZones = Charset.availableCharsets();
    static List<String> charsets = (List)Charset.availableCharsets().values().collect {it.aliases()}.flatten();

    @Override
    protected void addCompletions(
            @NotNull CompletionParameters parameters, ProcessingContext context, @NotNull CompletionResultSet result) {
        PsiElement psiElement = parameters.position;
        if (!(psiElement instanceof LeafPsiElement)) {
            return;
        }
        assert timeZoneIcon != null
        JavassistCompletionBean completionBean = MyAcceptCharsetProviderImpl.isOkPsiElement((LeafPsiElement) psiElement);
        String value4 = MyAcceptFileProviderImpl.getStringFromPsiLiteral(completionBean.literalElement);
        String realValue = value4.replace(IdeaMagic.addedConstant, '');
        //int offset = value4.indexOf(IdeaMagic.addedConstant);
//        String valutoClac = value4.substring(0, offset);
//        if (offset < 0) {
//            log.error("invalid offset cp2 ${offset} , value = ${realValue} , value3 = ${value4}")
//            return;
//        }
        log.debug("cp 8 : value = ${realValue}")

        charsets.collect {
            String aaa = it;
            LookupElement element23 = LookupElementBuilder.create(it).withIcon(timeZoneIcon).withCaseSensitivity(true);
            result.addElement(element23);
        };
    }


    private void testNotUsed() {
        Charset.forName("cp860")
        Charset.forName("L9")
        Charset.forName("273")
        Charset.forName('utf-8')
    }

}
