package idea.plugins.thirdparty.filecompletion.share.Ideasettings

import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.ServiceManager
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.tools.Tool
import com.intellij.tools.ToolManager
import com.intellij.util.xmlb.XmlSerializerUtil
import groovy.transform.CompileStatic
import net.sf.jremoterun.utilities.JrrClassUtils
import org.apache.log4j.LogManager
import org.apache.log4j.Logger
import org.jetbrains.annotations.Nullable

@CompileStatic
@State(name = 'OpenFileFromSourceSettings', storages = @Storage("other.xml"))
public class OpenFileActionSettings implements PersistentStateComponent<OpenFileActionSettings> {
    private static final Logger log = LogManager.getLogger(JrrClassUtils.currentClass);
    public String TOOL_NAME;

    public OpenFileActionSettings() {
        List<Tool> tools = OpenFileActionSettings.findAllEnabledTools();
        if (tools.size() == 0) {
            TOOL_NAME = ''
        } else {
            TOOL_NAME = tools[0].name
        }
    }

    Tool findSelectedTool() {
        List<Tool> tools = OpenFileActionSettings.findAllEnabledTools();
        return tools.find { it.name == TOOL_NAME }
    }

    public static List<Tool> findAllEnabledTools() {
        List<Tool> tools = ToolManager.instance.tools
        tools = tools.findAll { it.enabled }
        return tools;
    }

    public static OpenFileActionSettings getInstance() {
        return ServiceManager.getService(OpenFileActionSettings.class);
    }

    @Nullable
    @Override
    public OpenFileActionSettings getState() {
        return this;
    }

    @Override
    public void loadState(OpenFileActionSettings state) {
        log.debug "load state"
        XmlSerializerUtil.copyBean(state, this);
    }

}
