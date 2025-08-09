/*
 * SPDX-FileCopyrightText:  © 2022-2023 David Gregory
 * SPDX-License-Identifier: Apache-2.0
 */
package org.mina_lang.common.diagnostics;

import com.opencastsoftware.yvette.LabelledRange;
import com.opencastsoftware.yvette.Severity;
import com.opencastsoftware.yvette.SourceCode;
import com.opencastsoftware.yvette.URISourceCode;
import org.mina_lang.common.Location;

import java.net.URI;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class Diagnostic extends com.opencastsoftware.yvette.Diagnostic {
    private final Location location;
    private final Severity severity;
    private final String message;
    private final List<DiagnosticRelatedInformation> relatedInformation;

    public Diagnostic(Location location, Severity severity, String message,
        List<DiagnosticRelatedInformation> relatedInformation) {
            super(message);
            this.location = location;
            this.severity = severity;
            this.message = message;
            this.relatedInformation = relatedInformation;
    }

    public Diagnostic(Location location, Severity severity, String message, Throwable cause,
        List<DiagnosticRelatedInformation> relatedInformation) {
            super(message, cause);
            this.location = location;
            this.severity = severity;
            this.message = message;
            this.relatedInformation = relatedInformation;
    }

    public Diagnostic(Location location, Severity severity, String message) {
        this(location, severity, message, List.of());
    }

    public Diagnostic(Location location, Severity severity, String message, Throwable cause) {
        this(location, severity, message, cause, List.of());
    }

    public Location location() {
        return location;
    }

    public Severity severity() {
        return severity;
    }

    public String message() {
        return message;
    }

    public List<DiagnosticRelatedInformation> relatedInformation() {
        return relatedInformation;
    }

    @Override
    public String code() {
        return null;
    }

    @Override
    public String help() {
        return null;
    }

    @Override
    public URI url() {
        return null;
    }

    @Override
    public SourceCode sourceCode() {
        return new URISourceCode(location.uri());
    }

    @Override
    public Collection<LabelledRange> labels() {
        return Collections.singletonList(
            new LabelledRange(null, location.range().start(), location.range().end()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((location == null) ? 0 : location.hashCode());
        result = prime * result + ((severity == null) ? 0 : severity.hashCode());
        result = prime * result + ((message == null) ? 0 : message.hashCode());
        result = prime * result + ((relatedInformation == null) ? 0 : relatedInformation.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Diagnostic other = (Diagnostic) obj;
        if (location == null) {
            if (other.location != null)
                return false;
        } else if (!location.equals(other.location))
            return false;
        if (severity != other.severity)
            return false;
        if (message == null) {
            if (other.message != null)
                return false;
        } else if (!message.equals(other.message))
            return false;
        if (relatedInformation == null) {
            if (other.relatedInformation != null)
                return false;
        } else if (!relatedInformation.equals(other.relatedInformation))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "Diagnostic [location=" + location + ", severity=" + severity + ", message=" + message
                + ", relatedInformation=" + relatedInformation + "]";
    }
}
