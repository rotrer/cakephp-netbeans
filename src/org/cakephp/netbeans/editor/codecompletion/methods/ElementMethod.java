/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */
package org.cakephp.netbeans.editor.codecompletion.methods;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.cakephp.netbeans.module.CakePhpModule;
import org.cakephp.netbeans.util.CakePhpUtils;
import org.cakephp.netbeans.util.CakeVersion;
import org.netbeans.modules.php.api.phpmodule.PhpModule;
import org.netbeans.modules.php.api.util.StringUtils;
import org.openide.filesystems.FileObject;

/**
 *
 * @author junichi11
 */
public class ElementMethod extends AssetMethod {

    private static final String ELEMENTS = "Elements"; // NOI18N
    private final FileObject currentFile;

    public ElementMethod(PhpModule phpModule) {
        super(phpModule);
        this.currentFile = null;
    }

    public ElementMethod(PhpModule phpModule, FileObject currentFile) {
        super(phpModule);
        this.currentFile = currentFile;
    }

    @Override
    public List<String> getElements(int argCount, String inputValue) {
        CakePhpModule cakeModule = CakePhpModule.forPhpModule(phpModule);
        if (cakeModule == null || !CakePhpUtils.isCtpFile(currentFile)) {
            return Collections.emptyList();
        }

        List<String> elements = new ArrayList<String>();
        int cakeVersion = CakeVersion.getInstance(phpModule).getMajor();

        if (argCount == 1) {
            // get plugin name
            boolean isPlugin = false;
            String pluginName = ""; // NOI18N
            String basePath;
            if (cakeVersion >= 2) {
                String[] pluginSplit = CakePhpUtils.pluginSplit(inputValue);
                if (pluginSplit != null && pluginSplit.length == 2) {
                    pluginName = pluginSplit[0];
                    basePath = pluginSplit[1];
                    isPlugin = true;
                } else {
                    basePath = inputValue;
                }
            } else {
                basePath = inputValue;
            }

            // check subdirectory
            String filter = setSubDirectoryPath(basePath);

            // plugin elements
            // for CakePHP 2.1+
            if (isPlugin && cakeVersion >= 2) {
                for (CakePhpModule.DIR_TYPE dirType : PLUGINS) {
                    FileObject viewDirectory = cakeModule.getViewDirectory(dirType, pluginName);
                    if (viewDirectory != null) {
                        FileObject elementsDirectory = viewDirectory.getFileObject(getRelativePath());
                        if (elementsDirectory != null) {
                            for (FileObject element : elementsDirectory.getChildren()) {
                                addElement(element, filter, elements, pluginName);
                            }
                        }
                        break;
                    }
                }
                return elements;
            }

            // get directory type
            CakePhpModule.DIR_TYPE currentDirType = cakeModule.getCurrentDirectoryType(currentFile);
            String currentPluginName = cakeModule.getCurrentPluginName(currentFile);
            if (StringUtils.isEmpty(currentPluginName)) {
                currentPluginName = null;
            }

            // get view directory
            FileObject viewDirectory = cakeModule.getViewDirectory(currentDirType, currentPluginName);
            if (viewDirectory != null) {
                FileObject elementDirectory = viewDirectory.getFileObject(getRelativePath());
                if (elementDirectory != null) {
                    for (FileObject child : elementDirectory.getChildren()) {
                        addElement(child, filter, elements);
                    }
                }
            }

            if (!subDirectoryPath.isEmpty()) {
                return elements;
            }

            // plugin names
            // for CakePHP 2.1+
            if (cakeVersion >= 2) {
                for (CakePhpModule.DIR_TYPE dirType : PLUGINS) {
                    FileObject pluginDirectory = cakeModule.getDirectory(dirType);
                    if (pluginDirectory != null) {
                        for (FileObject child : pluginDirectory.getChildren()) {
                            if (child.isFolder()) {
                                String name = child.getNameExt();
                                viewDirectory = cakeModule.getViewDirectory(dirType, name);
                                if (viewDirectory != null && viewDirectory.getFileObject(ELEMENTS) != null) {
                                    if (name.startsWith(filter)) {
                                        name = name + DOT;
                                        elements.add(name);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return elements;
    }

    /**
     * Add element starting with filter value to list.
     *
     * @param fo FileObject for adding
     * @param filter filtering with this value
     * @param elements List for adding (add to this)
     * @return true if add, otherwise false
     */
    private boolean addElement(FileObject fo, String filter, List<String> elements) {
        return addElement(fo, filter, elements, null);
    }

    private String getRelativePath() {
        String subPath = ELEMENTS;
        if (!subDirectoryPath.isEmpty()) {
            subPath = subPath + SLASH + subDirectoryPath;
        }
        return subPath;
    }
}
