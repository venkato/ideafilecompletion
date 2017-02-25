package idea.plugins.thirdparty.filecompletion.share

import com.intellij.codeInsight.completion.CompletionContributor
import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.codeInsight.completion.CompletionResultSet
import idea.plugins.thirdparty.filecompletion.jrr.CompetionContributerRenew
import idea.plugins.thirdparty.filecompletion.jrr.MyCompletionContributorImpl
import net.sf.jremoterun.utilities.JrrClassUtils
import org.apache.log4j.LogManager
import org.apache.log4j.Logger
import org.jetbrains.annotations.NotNull

public class MyJavaCompletionContributor extends CompletionContributor {

    private static final Logger log = LogManager.getLogger(JrrClassUtils.currentClass);

    public static CompletionContributor completionContributor = new MyCompletionContributorImpl();

    public static MyJavaCompletionContributor myGroovyCompletionContributor;

    public static Exception callStack;

    public MyJavaCompletionContributor() {
        if (myGroovyCompletionContributor != null) {
            log.error("myGroovyCompletionContributor was defined before, see next exception when");
            log.error("myGroovyCompletionContributor was defined before, here : ", callStack);
        }
        myGroovyCompletionContributor = this;
        callStack = new Exception();
        CompetionContributerRenew.renewJavaContextAssist()
    }


    @Override
    public void fillCompletionVariants(@NotNull CompletionParameters parameters, @NotNull CompletionResultSet result) {
        completionContributor.fillCompletionVariants(parameters, result);
        //log.debug("parameters " + parameters.getCompletionType());
        super.fillCompletionVariants(parameters, result);
    }

}
