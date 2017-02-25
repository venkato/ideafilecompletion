package idea.plugins.thirdparty.filecompletion.jrr.javassist

import com.intellij.psi.PsiClass
import com.intellij.psi.PsiExpressionList
import com.intellij.psi.PsiLiteral
import groovy.transform.CompileStatic
import net.sf.jremoterun.utilities.JrrClassUtils
import org.apache.log4j.LogManager
import org.apache.log4j.Logger

@CompileStatic
class JavassistCompletionBean {
    private static final Logger log = LogManager.getLogger(JrrClassUtils.currentClass);


    PsiClass onObjectClass
    PsiLiteral literalElement
    PsiExpressionList args


}
