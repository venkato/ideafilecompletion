package idea.plugins.thirdparty.filecompletion.jrr.jrrlib

import com.intellij.psi.PsiClass
import com.intellij.psi.PsiLiteral
import groovy.transform.CompileStatic
import net.sf.jremoterun.utilities.JrrClassUtils
import org.apache.log4j.LogManager
import org.apache.log4j.Logger
import org.jetbrains.plugins.groovy.lang.psi.api.statements.arguments.GrArgumentList

@CompileStatic
class JrrCompletionBean {
    private static final Logger log = LogManager.getLogger(JrrClassUtils.currentClass);


    ReflectionElement methodName
    PsiClass onObjectClass
    boolean onObjectStatic
    PsiLiteral literalElement
    GrArgumentList args
}
