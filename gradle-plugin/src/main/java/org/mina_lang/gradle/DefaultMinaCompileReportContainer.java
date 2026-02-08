/*
 * SPDX-FileCopyrightText:  © 2026 David Gregory
 * SPDX-License-Identifier: Apache-2.0
 */
package org.mina_lang.gradle;

import org.gradle.api.Describable;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.reporting.DirectoryReport;
import org.gradle.api.reporting.Report;
import org.gradle.api.reporting.internal.DefaultReportContainer;
import org.gradle.api.reporting.internal.DelegatingReportContainer;
import org.gradle.api.reporting.internal.SingleDirectoryReport;

import javax.inject.Inject;
import java.util.List;

public class DefaultMinaCompileReportContainer extends DelegatingReportContainer<Report> implements MinaCompileReportContainer {
    @Inject
    public DefaultMinaCompileReportContainer(Describable owner, ObjectFactory objectFactory) {
        super(DefaultReportContainer.create(objectFactory, Report.class, factory -> List.of(
            factory.instantiateReport(SingleDirectoryReport.class, "html", owner, "index.html")
        )));
    }

    @Override
    public DirectoryReport getHtml() {
        return (DirectoryReport) getByName("html");
    }
}
