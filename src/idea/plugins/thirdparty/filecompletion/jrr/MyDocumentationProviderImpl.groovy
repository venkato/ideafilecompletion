package idea.plugins.thirdparty.filecompletion.jrr

import com.intellij.lang.documentation.DocumentationProvider
import com.intellij.lang.java.JavaDocumentationProvider
import com.intellij.openapi.progress.ProcessCanceledException
import com.intellij.psi.*
import com.intellij.psi.impl.source.tree.LeafPsiElement
import com.intellij.psi.impl.source.tree.PsiCommentImpl
import com.intellij.psi.search.ProjectAndLibrariesScope
import groovy.transform.CompileStatic
import idea.plugins.thirdparty.filecompletion.jrr.charset.MyAcceptCharsetProviderImpl
import idea.plugins.thirdparty.filecompletion.jrr.file.FileCompletionBean
import idea.plugins.thirdparty.filecompletion.jrr.file.MyAcceptFileProviderImpl
import idea.plugins.thirdparty.filecompletion.jrr.file.MySyntheticFileSystemItem
import idea.plugins.thirdparty.filecompletion.jrr.javadocredirect.DecimalFormatHelper
import idea.plugins.thirdparty.filecompletion.jrr.javadocredirect.SimpleDateFormatHelper
import idea.plugins.thirdparty.filecompletion.jrr.javassist.JavassistCompletionBean
import idea.plugins.thirdparty.filecompletion.jrr.javassist.MyAcceptJavassistProviderImpl
import idea.plugins.thirdparty.filecompletion.jrr.jrrlib.JrrCompletionBean
import idea.plugins.thirdparty.filecompletion.jrr.jrrlib.MyAcceptJrrProviderImpl
import idea.plugins.thirdparty.filecompletion.jrr.jrrlib.ReflectionElement
import idea.plugins.thirdparty.filecompletion.jrr.maven.MyAcceptMavenProviderImpl
import idea.plugins.thirdparty.filecompletion.jrr.remoterun.JrrIdeaBean
import idea.plugins.thirdparty.filecompletion.jrr.systemprop.MyAcceptSystemPropProviderImpl
import idea.plugins.thirdparty.filecompletion.share.OSIntegrationIdea
import net.sf.jremoterun.utilities.JrrClassUtils
import net.sf.jremoterun.utilities.JrrUtilities
import net.sf.jremoterun.utilities.classpath.MavenCommonUtils
import net.sf.jremoterun.utilities.classpath.MavenId
import org.apache.log4j.LogManager
import org.apache.log4j.Logger
import org.jetbrains.annotations.Nullable
import org.jetbrains.plugins.groovy.lang.psi.api.statements.GrField
import org.jetbrains.plugins.groovy.lang.psi.api.statements.arguments.GrArgumentList

import javax.swing.*
import java.lang.management.ManagementFactory
import java.nio.charset.Charset
import java.text.DecimalFormat
import java.text.SimpleDateFormat

@CompileStatic
class MyDocumentationProviderImpl implements DocumentationProvider {
    private static final Logger log = LogManager.getLogger(JrrClassUtils.currentClass);

    static JavaDocumentationProvider documentationProvider = new JavaDocumentationProvider()

    static String htmlLineSeparator = '<br></br>'
    PsiClass simpleDateFormatClass;
    PsiClass numberFormatClass;
    MavenCommonUtils mavenCommonUtils = new MavenCommonUtils()


    @Override
    String getQuickNavigateInfo(PsiElement element, PsiElement originalElement) {
        return null
    }

    @Override
    List<String> getUrlFor(PsiElement element, PsiElement originalElement) {
        return null
    }

    String buildFilePath(File file) {
        String fullPath = file.absolutePath.tr('\\', '/')
        log.debug "fullPath = ${fullPath}"
        List<String> list = Arrays.asList(fullPath.split('/'))
        // List<String> list2 = new ArrayList<>(list)
        List<String> res = []
        //List<String> tmp =[]
        boolean first = true
        String path = ''
        list.eachWithIndex { String entry, int i ->
            if (first) {
                first = false
            } else {
                path += '/'
            }
            path += entry
            if (path.length() > 70) {
                res.add(path)
                path = '';
            }
        }
        if (path.length() != 0) {
            res.add(path)
        }
        if (fullPath.startsWith('/')) {
            res[0] = '/' + res[0]
        }
        if (fullPath.endsWith('/') || file.isDirectory()) {
            res[res.size() - 1] = res[res.size() - 1] + '/'
        }
        log.debug("res tmp = ${res}")
        String result = res.join(htmlLineSeparator)
        log.debug "res = ${result}"
        return result


    }

    String generateInfoForFile(File file) {
        String res = "<html> <body> "
        String filePath = buildFilePath(file)
        if (!file.exists()) {
            res += " File not exists ${htmlLineSeparator} ${filePath} "
        } else {
            res += filePath
        }
        res += " </body> </html>"
        // log.debug("res = ${res}")
        return res
    }


    @Override
    String generateDoc(PsiElement element23, @Nullable PsiElement originalElement) {
        try {
            return generateDocImpl(element23, originalElement)
        } catch (ProcessCanceledException e) {
            log.debug(e)
            throw e;
        } catch (Throwable e) {
            JrrUtilities.showException("Failed calc doc", e);
            return null
        }
    }

    String generateDocImpl(PsiElement element23, @Nullable PsiElement originalElement) {
        log.debug "cp 1 : ${element23} ${originalElement}"
        // log.debug "4"
        if (originalElement instanceof PsiJavaToken) {
            PsiJavaToken to = (PsiJavaToken) originalElement
            File file = MyAcceptFileProviderImpl.isOkJavaPsiElement3(to);
            if (file == null) {
                log.debug "not a file : ${to}"
                return null
            }
            return generateInfoForFile(file)
        }
        if (originalElement instanceof LeafPsiElement) {
            LeafPsiElement element = (LeafPsiElement) originalElement;
            JrrCompletionBean element2 = MyAcceptJrrProviderImpl.isOkPsiElement(element)
            if (element2 != null) {
                return generateDorForJrr(element2, originalElement)
            }
            if (MyAcceptSystemPropProviderImpl.isOkPsiElement(element)) {
                String value4 = MyAcceptFileProviderImpl.getStringFromPsiLiteral(element.parent);
                String propertyValue = System.getProperty(value4);
                if (propertyValue == null) {
                    return "property not found : ${value4}"
                }
                return "value = ${propertyValue}"
            }
            if (MyAcceptCharsetProviderImpl.isOkPsiElement(element) != null) {
                try {
                    String value4 = MyAcceptFileProviderImpl.getStringFromPsiLiteral(element.parent);
                    Charset charset = Charset.forName(value4)
                    if (charset == null) {
                        return "Charset not found : ${charset.displayName()}"
                    }
                    return "Charset : ${charset.displayName()}"
                } catch (Exception e) {
                    log.debug "${e}"
                }
            }
            if (MyAcceptMavenProviderImpl.isOkPsiElement(element)) {
                String value4 = MyAcceptFileProviderImpl.getStringFromPsiLiteral(element.parent);
                List<String> ids = value4.tokenize(':')
                if (value4.count(':') != 2 || ids.size() != 3) {
                    return "Not a maven token ${value4}"
                }
                MavenId mavenId = new MavenId(value4)
                File file = mavenCommonUtils.findMavenOrGradle(mavenId)
                if (file == null) {
                    return "Maven token not found : ${value4}"
                }
                return generateInfoForFile(file);
            }
            PsiElement impl = gotoImpl(element);
            //log.debug "ref : ${impl}"
            if (impl != null) {
                if (impl instanceof MySyntheticFileSystemItem) {
                    // return
                    MySyntheticFileSystemItem iii = (MySyntheticFileSystemItem) impl;
                    return generateInfoForFile(iii.file)
                } else {
                    String doc = documentationProvider.generateDoc(impl, originalElement)
                    log.debug "doc found ? ${doc != null}"
                    //log.debug "doc = ${doc}"
                    return doc
                }
            }
        }
        return null
    }

    PsiElement gotoImpl(LeafPsiElement sourceElement) {

        if (SimpleDateFormatHelper.isOkPsiElement(sourceElement)) {
            if (simpleDateFormatClass == null) {
                ProjectAndLibrariesScope scope = new ProjectAndLibrariesScope(OSIntegrationIdea.openedProject);
                PsiClass psiClass = JavaPsiFacade.getInstance(OSIntegrationIdea.openedProject).findClass(SimpleDateFormat.name, scope);
                simpleDateFormatClass = psiClass
            }
            return simpleDateFormatClass
        }
        if (DecimalFormatHelper.isOkPsiElement(sourceElement)) {
            if (numberFormatClass == null) {
                ProjectAndLibrariesScope scope = new ProjectAndLibrariesScope(OSIntegrationIdea.openedProject);
                PsiClass psiClass = JavaPsiFacade.getInstance(OSIntegrationIdea.openedProject).findClass(DecimalFormat.name, scope);
                numberFormatClass = psiClass
            }
            return numberFormatClass
        }
        FileCompletionBean element = MyAcceptFileProviderImpl.isOkJavaAndGroovyPsiElement(sourceElement, true);
        if (element != null) {
            File file;
            if (element.parentFilePath == null) {
                file = new File(element.value)
            } else {
                file = new File(element.parentFilePath, element.value);

            }
            return new MySyntheticFileSystemItem(file);
        }
        JavassistCompletionBean element1 = MyAcceptJavassistProviderImpl.isOkPsiElement(sourceElement)
        if (element1 != null) {
            Object value = element1.literalElement.getValue();
            if (!(value instanceof String)) {
                return null;
            }
            String value4 = (String) value;
            // ;
            String realValue = value4.replace(IdeaMagic.addedConstant, '');
            if (!(element1.args instanceof GrArgumentList)) {
                log.debug("no gr ")
            }
            GrArgumentList argumentList = (GrArgumentList) element1.args;
            PsiLiteral grLiteral3 = (PsiLiteral) argumentList.allArguments[3];
            Integer paramCount = (Integer) grLiteral3.value;
            log.debug "paramCount = ${paramCount}"
            PsiMethod find = element1.onObjectClass.allMethods.find {
                it.name == realValue && it.parameterList.parametersCount == paramCount
            };
            return find;
        }
        return null
    }

    String endSuffix = ' = </b></PRE></body></html>';

    private String getInitilizerForField(PsiField psiField, String docAlready) {
        log.debug "docAlready : ${docAlready}"
        String text;
        if (docAlready.contains(endSuffix)) {

            switch (psiField) {
                case { psiField instanceof GrField }:
                    // to calc comment need use parent , which is GrVariableDeclarationImpl
                    GrField grField = (GrField) psiField;
                    if (grField.initializerGroovy != null) {
                        text = grField.initializerGroovy.text

                    }
                    break;
                case { psiField.initializer != null }:
                    text = psiField.initializer.text
                    break;
                default:
                    log.debug "not gr field ${psiField?.class.name} ${psiField}"
            }
        }

        List<PsiCommentImpl> comments = (List) psiField.children.findAll { it.class == PsiCommentImpl }
        comments = comments.findAll { it.text != null }
        List<String> comments2 = comments.collect { it.text.replace('//', '').trim() }
        comments2 = comments2.findAll { it.length() > 1 }
        String commetsAsS = comments2.join(htmlLineSeparator);
        String doc = null
        if (text != null && !docAlready.contains(text)) {
            doc = docAlready.replace(endSuffix, '') + ' = ' + text
        } else {

        }
        if (commetsAsS != null && commetsAsS.length() > 1) {
            if (doc == null) {
                doc = docAlready.replace(htmlEnd, '') + htmlLineSeparator + commetsAsS;
            } else {
                doc += htmlLineSeparator + commetsAsS;
            }

        }
        if (doc == null) {
            return docAlready
        }
        log.debug "final doc : ${doc}"
        return doc + htmlEnd;
    }


    private void testNotUsed() {
        Socket testVar = null;
        JrrClassUtils.getFieldValue(testVar, "")
        JrrClassUtils.getFieldValue(testVar.getChannel().keyFor(null), "attachment")
        JrrClassUtils.getFieldValue(ManagementFactory.properties as HashMap, "size")
        JrrClassUtils.getFieldValue(Class, "useCaches")
        JrrClassUtils.getFieldValue(Class, "vsf cachessdf")
        JrrClassUtils.getFieldValue(testVar, "bound")
        JrrClassUtils.getFieldValue(testVar, "closeLock")
        JrrClassUtils.getFieldValue(JButton, "coalesceMap")
        MyAcceptFileProviderImpl.log.toString()
        JrrClassUtils.getFieldValue(MyAcceptFileProviderImpl, "log")
        //JrrClassUtils.findMethod(testVar, "connect",2)
        JrrClassUtils.invokeMethod(testVar, "close")
        JrrClassUtils.invokeMethod(testVar, "connect")
    }

    String htmlEnd = '</body></html>'

    private String generateDoCForField(PsiField psiField, JrrCompletionBean element2, PsiElement originalElement) {
        if (psiField.navigationElement != null && psiField.navigationElement instanceof PsiField) {
            log.debug "use navigation el"
            psiField = psiField.navigationElement as PsiField
        }
        String doc = documentationProvider.generateDoc(psiField, originalElement)
        JrrIdeaBean.bean.psiField = psiField
        return getInitilizerForField(psiField, doc)
//        log.debug "additional for field 4 : ${additional}"
//        if (additional != null && additional.length() > 0) {
//            log.debug  "doc 1 = ${doc}"
//            doc = doc.replace(htmlEnd, '')
//            doc = doc + additional + htmlEnd
//            log.debug  "doc 2 = ${doc}"
//        } else {
//
//        }

        return doc;
    }

    private String generateDorForJrr(JrrCompletionBean element2, PsiElement originalElement) {
        log.debug "calc field value2"
        Object value = element2.literalElement.getValue();
        if (!(value instanceof String)) {
            return null;
        }
        String value4 = (String) value;
        String realValue = value4.replace(IdeaMagic.addedConstant, '');
        if (ReflectionElement.reFields.contains(element2.methodName)) {
            PsiField psiField = element2.onObjectClass.allFields.find { it.name == realValue }
            if (psiField == null) {
                log.debug "field not found 3 :  ${element2.onObjectClass} ${realValue} "
                JrrIdeaBean.bean.psiField = psiField
                JrrIdeaBean.bean.psiElement3 = psiField
                return null;
            }
            return generateDoCForField(psiField, element2, originalElement);
        }
        int paramCount = -1
        switch (element2.methodName) {
            case ReflectionElement.findMethod:
                if (element2.args.allArguments[3] instanceof PsiLiteral) {
                    if (!(element2.args instanceof GrArgumentList)) {
                        log.debug("no gr ")
                    }
                    GrArgumentList argumentList = (GrArgumentList) element2.args;
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
        PsiElement psiElement = element2.onObjectClass.allMethods.find {
            it.name == realValue && it.parameterList.parametersCount == paramCount
        };
        if (psiElement == null) {
            return null
        }
        String doc = documentationProvider.generateDoc(psiElement, originalElement)
        return doc;

    }


    @Override
    PsiElement getDocumentationElementForLookupItem(PsiManager psiManager, Object object, PsiElement element) {
        return null
    }

    @Override
    PsiElement getDocumentationElementForLink(PsiManager psiManager, String link, PsiElement context) {
        return null
    }


}
