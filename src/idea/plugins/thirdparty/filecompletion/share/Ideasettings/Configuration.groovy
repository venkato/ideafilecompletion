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

import com.intellij.openapi.options.BaseConfigurable
import com.intellij.openapi.options.ConfigurationException
import groovy.transform.CompileStatic
import net.sf.jremoterun.utilities.JrrClassUtils
import org.apache.log4j.LogManager
import org.apache.log4j.Logger

import javax.swing.*

@CompileStatic
public class Configuration extends BaseConfigurable {
    private static final Logger log = LogManager.getLogger(JrrClassUtils.currentClass);
    private ConfigurationPanel _panel;

    private final OpenFileActionSettings settings;

    public Configuration() {
        settings = OpenFileActionSettings.getInstance();
    }

    @Override
    public String getDisplayName() {
        return Constants.pluginName;
    }

    @Override
    public String getHelpTopic() {
        return null;
    }

    @Override
    public JComponent createComponent() {
        _panel = new ConfigurationPanel();
        return _panel;
    }

    @Override
    public boolean isModified() {
        boolean modfi = _panel.toolName != settings.TOOL_NAME
        log.debug "modfied 3 :  ${modfi} : ${_panel.toolName} ${settings.TOOL_NAME}"
        return modfi
    }

    /**
     * Save the settings from the configuration panel
     */
    @Override
    public void apply() throws ConfigurationException {
        settings.TOOL_NAME = _panel.toolName;
        log.debug "apply : ${settings.TOOL_NAME}"
//        settings.getTextAttributes().setBackgroundColor(getHighlightColor());
    }

    /**
     * Load current settings into the configuration panel
     */
    @Override
    public void reset() {
        _panel.toolName = toolName;
        log.debug " reset ${toolName}"
    }

    @Override
    public void disposeUIResources() {
        _panel = null;
    }


    public String getToolName() {
        return settings.TOOL_NAME;
    }
}
