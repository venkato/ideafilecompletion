package idea.plugins.thirdparty.filecompletion.jrr

import com.intellij.codeInsight.completion.CompletionContributor
import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.codeInsight.completion.CompletionResultSet
import groovy.transform.CompileStatic
import net.sf.jremoterun.utilities.JrrClassUtils
import org.apache.log4j.LogManager
import org.apache.log4j.Logger
import org.jetbrains.annotations.NotNull

/**
 * Reserved for future flexibility
 */
@CompileStatic
class MyCompletionContributorImpl extends CompletionContributor {

    private static final Logger log = LogManager.getLogger(JrrClassUtils.currentClass);

    @Override
    public void fillCompletionVariants(@NotNull CompletionParameters parameters, @NotNull CompletionResultSet result) {
    }
}
