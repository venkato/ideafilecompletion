package idea.plugins.thirdparty.filecompletion.jrr


import groovy.transform.CompileStatic
import idea.plugins.thirdparty.filecompletion.share.OSIntegrationIdea
import net.sf.jremoterun.utilities.JrrClassUtils
import net.sf.jremoterun.utilities.MBeanFromJavaBean
import net.sf.jremoterun.utilities.log4j.Log4jConfigurator
import org.apache.log4j.Level
import org.apache.log4j.LogManager
import org.apache.log4j.Logger

@CompileStatic
class InitPlugin {
    private static final Logger log = LogManager.getLogger(JrrClassUtils.currentClass);

    public static void init() {
        Logger logger = LogManager.getLogger('idea.plugins.thirdparty.filecompletion')
        Log4jConfigurator.setLevelForLogger1(logger.name, Level.DEBUG)
        CompetionContributerRenew.regDocumentation()
        CompetionContributerRenew.regGotto()
        MBeanFromJavaBean.registerMBean(new OSIntegrationIdea());
    }


}
