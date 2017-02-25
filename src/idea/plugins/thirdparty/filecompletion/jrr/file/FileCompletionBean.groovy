package idea.plugins.thirdparty.filecompletion.jrr.file

import com.intellij.psi.PsiLiteral
import groovy.transform.CompileStatic
import net.sf.jremoterun.utilities.JrrClassUtils
import org.apache.log4j.LogManager
import org.apache.log4j.Logger

@CompileStatic
class FileCompletionBean {
    private static final Logger log = LogManager.getLogger(JrrClassUtils.currentClass);
    PsiLiteral literalElemtnt
    String value
    File parentFilePath
}
