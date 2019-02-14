/******************************************************************************* 
 * Copyright (c) 2014-2015 Red Hat, Inc. 
 * Distributed under license by Red Hat, Inc. All rights reserved. 
 * This program is made available under the terms of the 
 * Eclipse Public License v1.0 which accompanies this distribution, 
 * and is available at http://www.eclipse.org/legal/epl-v10.html 
 *
 * Contributors: 
 * Red Hat, Inc. - initial API and implementation 
 ******************************************************************************/

package sh.wheel.gitops.agent.testutil;

import org.apache.commons.io.IOUtils;

import java.io.InputStream;

public enum Samples {


    V1_BUILDCONFIG_PIPELINE("openshift3/v1_buildconfig_pipeline.json");

    private static final String SAMPLES_FOLDER = "/samples/";
    public static final String TESTREPO1_PATH = SAMPLES_FOLDER + "testrepo1/";

    private String filePath;

    Samples(String fileName) {
        this.filePath = SAMPLES_FOLDER + fileName;
    }

    Samples(String root, String filename) {
        this.filePath = root + filename;
    }

    public String getContentAsString() {
        String content = null;
        try {
            final InputStream contentStream = Samples.class.getResourceAsStream(filePath);
            content = IOUtils.toString(contentStream, "UTF-8");
        } catch (Throwable e) {
            e.printStackTrace();
            throw new RuntimeException("Could not read file " + filePath + ": " + e.getMessage());
        }
        return content;
    }
}
