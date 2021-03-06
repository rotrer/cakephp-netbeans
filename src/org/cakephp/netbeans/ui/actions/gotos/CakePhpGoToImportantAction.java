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
package org.cakephp.netbeans.ui.actions.gotos;

import java.util.List;
import org.cakephp.netbeans.ui.GoToItem;
import org.cakephp.netbeans.ui.actions.gotos.statuses.CakePhpGoToStatus;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle;

/**
 * Go to Important file (files in config directory). e.g. database.php, core.php
 *
 * @author junichi11
 */
@ActionID(
        category = "PHP",
        id = "org.cakephp.netbeans.ui.actions.CakePhpGoToImportantAction")
@ActionRegistration(
        displayName = "#CakePhpGoToImportantAction_Name")
@ActionReferences({
    @ActionReference(path = "Shortcuts", name = "DS-G D-I"),
    @ActionReference(path = "Shortcuts", name = "D-J D-I")
})
@NbBundle.Messages("CakePhpGoToImportantAction_Name=CakePHP Go To Important")
public class CakePhpGoToImportantAction extends CakePhpGoToAction {

    private static final CakePhpGoToImportantAction INSTANCE = new CakePhpGoToImportantAction();
    private static final long serialVersionUID = -1898908042463103380L;

    private CakePhpGoToImportantAction() {
    }

    public static CakePhpGoToImportantAction getInstance() {
        return INSTANCE;
    }

    @Override
    protected List<GoToItem> getGoToItems(CakePhpGoToStatus status) {
        return status.getImportants();
    }

    @Override
    protected String getFullName() {
        return getPureName();
    }

    @Override
    protected String getPureName() {
        return Bundle.CakePhpGoToImportantAction_Name();
    }
}
