package idea.plugins.thirdparty.filecompletion.jrr.maven

import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.codeInsight.completion.CompletionProvider
import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.openapi.util.IconLoader
import com.intellij.psi.PsiElement
import com.intellij.psi.impl.source.tree.LeafPsiElement
import com.intellij.util.ProcessingContext
import groovy.io.FileType
import groovy.transform.CompileStatic
import idea.plugins.thirdparty.filecompletion.jrr.IdeaMagic
import idea.plugins.thirdparty.filecompletion.jrr.file.MyAcceptFileProviderImpl
import idea.plugins.thirdparty.filecompletion.share.OSIntegrationIdea
import net.sf.jremoterun.utilities.JrrClassUtils
import net.sf.jremoterun.utilities.classpath.AddFilesToClassLoaderGroovy
import net.sf.jremoterun.utilities.classpath.ClassPathCalculatorAbstract
import net.sf.jremoterun.utilities.classpath.MavenCommonUtils
import net.sf.jremoterun.utilities.classpath.MavenId
import org.apache.log4j.LogManager
import org.apache.log4j.Logger
import org.jetbrains.annotations.NotNull

import javax.swing.*

@CompileStatic
public class MavenCompletionProviderImpl extends CompletionProvider<CompletionParameters> {

    private static final Logger log = LogManager.getLogger(JrrClassUtils.currentClass);

    static Icon timeZoneIcon = IconLoader.getIcon('/icons/open_file.png', OSIntegrationIdea);

    private Date lastCheckDateGroupsIds;
    private HashSet cachedGroupsIds

    AddFilesToClassLoaderGroovy addFilesToClassLoader = new AddFilesToClassLoaderGroovy() {
        @Override
        void addFileImpl(File file) throws Exception {

        }
    }


    private void testNotUsed2() {
        new MavenId('org.javassist:javassist:');
    }

    public static List<String> findMavenGroupIds (MavenCommonUtils mavenCommonUtils){
        HashSet<File> groupsIds = []
        mavenCommonUtils.mavenLocalDir.eachFileRecurse(FileType.FILES) {
            if (it.name.endsWith('.jar')) {
                groupsIds.add(it.parentFile.parentFile)
            }
        }
        return groupsIds.collect {
            ClassPathCalculatorAbstract.getPathToParent(mavenCommonUtils.mavenLocalDir, it).tr('/','.')
        }
    }

    List<String> getProposals(String realValue) {
        int tokenCount = realValue.count(':')
        log.debug "tokenCount = ${tokenCount}"
        List<String> tokenize = realValue.tokenize(':')
        final List<String> res = []
        MavenCommonUtils mavenCommonUtils = addFilesToClassLoader.mavenCommonUtils;
        switch (tokenCount) {
            case 0:
                log.debug "cp22"
                if (lastCheckDateGroupsIds != null && System.currentTimeMillis() - lastCheckDateGroupsIds.getTime() < 60000) {
                    // 1 min ago
                    res.addAll(cachedGroupsIds);
                } else {
                    if (mavenCommonUtils.gradleLocalDir.exists()) {
                        res = mavenCommonUtils.gradleLocalDir.listFiles().findAll {
                            it.isDirectory()
                        }.collect { it.name }
                        log.debug "res size = ${res.size()}"
                    }
                    if (mavenCommonUtils.mavenLocalDir.exists()) {
                        res.addAll(findMavenGroupIds(mavenCommonUtils));
                    }
                    cachedGroupsIds = new HashSet(res)
                    lastCheckDateGroupsIds = new Date()
                }
                break
            case 1:
                String groupId = tokenize[0]
                if (mavenCommonUtils.gradleLocalDir.exists()) {
                    File groupDir = new File(mavenCommonUtils.gradleLocalDir, groupId);
                    if (groupDir.exists()) {
                        res.addAll(groupDir.listFiles().findAll {
                            it.isDirectory()
                        }.collect { it.name }
                        )
                    }
                }
                if (mavenCommonUtils.mavenLocalDir.exists()) {
                    File groupDir = new File(mavenCommonUtils.mavenLocalDir, groupId.tr('.', '/'));
                    if (groupDir.exists()) {
                        res.addAll(groupDir.listFiles().findAll {
                            it.isDirectory()
                        }.collect { it.name })
                    }
                }
                break;
            case 2:
                String groupId = tokenize[0]
                String artifact = tokenize[1]
                if (mavenCommonUtils.gradleLocalDir.exists()) {
                    File groupDir = new File(mavenCommonUtils.gradleLocalDir, groupId + '/' + artifact);
                    if (groupDir.exists()) {
                        res.addAll(groupDir.listFiles().findAll {
                            it.isDirectory()
                        }.collect { it.name })
                    }
                }
                if (mavenCommonUtils.mavenLocalDir.exists()) {
                    File groupDir = new File(mavenCommonUtils.mavenLocalDir, groupId.tr('.', '/') + '/' + artifact);
                    if (groupDir.exists()) {
                        res.addAll(groupDir.listFiles().findAll {
                            it.isDirectory()
                        }.collect { it.name })
                    }
                }
                break;
            default:
                break
        }
        return res.unique().sort()
    }


    @Override
    protected void addCompletions(
            @NotNull CompletionParameters parameters, ProcessingContext context, @NotNull CompletionResultSet result) {
        PsiElement psiElement = parameters.position;
        if (!(psiElement instanceof LeafPsiElement)) {
            return;
        }
        LeafPsiElement psiElement3 = (LeafPsiElement) parameters.position;
        int startOffset = psiElement3.startOffset
        assert timeZoneIcon != null
        //boolean accept = MyAcceptMavenProviderImpl.isOkPsiElement((LeafPsiElement) psiElement);
        String value4 = MyAcceptFileProviderImpl.getStringFromPsiLiteral(psiElement.parent);
        String realValue = value4.replace(IdeaMagic.addedConstant, '');
        log.debug("cp 9 : value = ${realValue}")
        int offset23 = parameters.offset//getCursorOffsetInCurrentEditor();
        int offset = offset23 - startOffset - 1
        log.debug("offset cp3 ${offset} , value = ${realValue} , value3 = ${realValue} ${startOffset} ${offset23}")
        if (offset < 0) {
            log.error("invalid offset cp2 ${offset} , value = ${realValue} , value3 = ${realValue} ${startOffset} ${offset23}")
            return;
        }
        String calcValue = realValue.substring(0, offset)

        List<String> proposals = getProposals(calcValue)
        if (proposals == null) {
            log.debug "propsals are null"
        } else {
            int count = calcValue.count(':')
            List<String> tokenize = calcValue.tokenize(':')
            String prefix
            switch (count) {
                case 1:
                    prefix = tokenize[0] + ':'
                    break
                case 2:
                    prefix = tokenize[0] + ':' + tokenize[1] + ':'
                    break;
                default:
                    prefix = ''
            }
            log.debug " proposals count : ${proposals.size()}"
            proposals.collect {
                String insertSt = prefix + it
                if (count != 2) {
                    insertSt += ':'
                }
                LookupElement element23 = LookupElementBuilder.create(insertSt).withIcon(timeZoneIcon)
                MavenLookupElement lookupElement = new MavenLookupElement(element23, it, timeZoneIcon);
                result.addElement(lookupElement);
            };
        }
    }


    private void testNotUsed() {
        new MavenId('org.springframework.javaconfig:spring-javaconfig:1.0.0.m3');
        new MavenId('org.javassist:javassist:3.18.1-GA');
        new MavenId("org.javassist:javassist:3.18.1-GA");

    }

}
