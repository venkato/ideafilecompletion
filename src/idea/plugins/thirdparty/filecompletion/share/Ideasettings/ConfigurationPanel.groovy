/*
    IDEA Plugin
    Copyright (C) 2002 Andrew J. Armstrong

    This program is free software; you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation; either version 2 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program; if not, write to the Free Software
    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA

	Author:
	Andrew J. Armstrong <andrew_armstrong@bigpond.com>
*/

package idea.plugins.thirdparty.filecompletion.share.Ideasettings

import com.intellij.openapi.options.ShowSettingsUtil
import com.intellij.tools.Tool
import groovy.transform.CompileStatic
import idea.plugins.thirdparty.filecompletion.share.OSIntegrationIdea
import net.sf.jremoterun.utilities.JrrClassUtils
import org.apache.log4j.LogManager
import org.apache.log4j.Logger

import javax.swing.*
import java.awt.*

@CompileStatic
public class ConfigurationPanel extends JPanel {
    private static final Logger log = LogManager.getLogger(JrrClassUtils.currentClass);
    private JList _list;


    JButton openExternalTools = new JButton("Open external Tools")

    public ConfigurationPanel() {
        buildGUI();
    }

    private void buildGUI() {
        java.util.List<Tool> tools = OpenFileActionSettings.findAllEnabledTools();
        OpenFileActionSettings instance = OpenFileActionSettings.getInstance()
        java.util.List<String> toolsS = tools.collect { it.name };
        _list = new JList(toolsS.toArray(new String[0]));
        String tool_name = instance.TOOL_NAME
        if (tools.contains(tool_name)) {
            _list.setSelectedValue(toolName, true)
        } else {

        }
        setLayout(new FlowLayout());
        add(new JLabel('Open file with tool : '))
        add(_list);

        openExternalTools.addActionListener {
            ShowSettingsUtil.getInstance().showSettingsDialog(OSIntegrationIdea.openedProject, "External Tools")
        }
        add(openExternalTools)
    }

    public String getToolName() {
        return (String) _list.getSelectedValue();
    }

    public void setToolName(String toolName) {
        _list.setSelectedValue(toolName, true);
    }

}
