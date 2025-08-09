/*
 * SPDX-FileCopyrightText:  © 2023 David Gregory
 * SPDX-License-Identifier: Apache-2.0
 */
package org.mina_lang.langserver.util;

import ch.epfl.scala.bsp4j.ShowMessageParams;
import com.opencastsoftware.yvette.Severity;
import org.eclipse.lsp4j.*;

public class Conversions {
    public static DiagnosticSeverity toLspSeverity(ch.epfl.scala.bsp4j.DiagnosticSeverity bspSeverity) {
        return DiagnosticSeverity.forValue(bspSeverity.getValue());
    }

    public static DiagnosticSeverity toLspSeverity(Severity minaSeverity) {
        return DiagnosticSeverity.forValue(minaSeverity.code());
    }

    public static Position toLspPosition(ch.epfl.scala.bsp4j.Position bspPosition) {
        return new Position(bspPosition.getLine(), bspPosition.getCharacter());
    }

    public static Position toLspPosition(com.opencastsoftware.yvette.Position minaPosition) {
        return new Position(minaPosition.line(), minaPosition.character());
    }

    public static Range toLspRange(ch.epfl.scala.bsp4j.Range bspRange) {
        return new Range(toLspPosition(bspRange.getStart()), toLspPosition(bspRange.getEnd()));
    }

    public static Range toLspRange(com.opencastsoftware.yvette.Range minaRange) {
        return new Range(toLspPosition(minaRange.start()), toLspPosition(minaRange.start()));
    }

    public static Location toLspLocation(ch.epfl.scala.bsp4j.Location bspLocation) {
        return new Location(bspLocation.getUri(), toLspRange(bspLocation.getRange()));
    }

    public static Location toLspLocation(org.mina_lang.common.Location bspLocation) {
        return new Location(bspLocation.uri().toString(), toLspRange(bspLocation.range()));
    }

    public static DiagnosticRelatedInformation toLspRelatedInformation(ch.epfl.scala.bsp4j.DiagnosticRelatedInformation bspRelatedInfo) {
        return new DiagnosticRelatedInformation(toLspLocation(bspRelatedInfo.getLocation()), bspRelatedInfo.getMessage());
    }


    public static DiagnosticRelatedInformation toLspRelatedInformation(org.mina_lang.common.diagnostics.DiagnosticRelatedInformation minaRelatedInfo) {
        return new DiagnosticRelatedInformation(toLspLocation(minaRelatedInfo.location()), minaRelatedInfo.message());
    }

    public static Diagnostic toLspDiagnostic(ch.epfl.scala.bsp4j.Diagnostic bspDiagnostic) {
        Diagnostic diagnostic = new Diagnostic();
        diagnostic.setRange(toLspRange(bspDiagnostic.getRange()));
        diagnostic.setMessage(bspDiagnostic.getMessage());
        if (bspDiagnostic.getCode() != null) {
            diagnostic.setCode(bspDiagnostic.getCode());
        }
        if (bspDiagnostic.getSource() != null) {
            diagnostic.setSource(bspDiagnostic.getSource());
        }
        if (bspDiagnostic.getSeverity() != null) {
            diagnostic.setSeverity(toLspSeverity(bspDiagnostic.getSeverity()));
        }
        if (bspDiagnostic.getRelatedInformation() != null) {
            diagnostic.setRelatedInformation(
                bspDiagnostic.getRelatedInformation()
                    .stream()
                    .map(Conversions::toLspRelatedInformation)
                    .toList());
        }
        return diagnostic;
    }

    public static Diagnostic toLspDiagnostic(org.mina_lang.common.diagnostics.Diagnostic minaDiagnostic) {
        Diagnostic diagnostic = new Diagnostic();
        diagnostic.setCode(minaDiagnostic.code());
        diagnostic.setSource("minac");
        diagnostic.setRange(toLspRange(minaDiagnostic.location().range()));
        diagnostic.setSeverity(toLspSeverity(minaDiagnostic.severity()));
        diagnostic.setMessage(minaDiagnostic.getMessage());
        diagnostic.setRelatedInformation(
            minaDiagnostic.relatedInformation()
                .stream()
                .map(Conversions::toLspRelatedInformation)
                .toList());
        return diagnostic;
    }

    public static PublishDiagnosticsParams toLspPublishDiagnostics(ch.epfl.scala.bsp4j.PublishDiagnosticsParams bspPublishDiagnostics) {
        return new PublishDiagnosticsParams(
            bspPublishDiagnostics.getTextDocument().getUri(),
            bspPublishDiagnostics.getDiagnostics().stream().map(Conversions::toLspDiagnostic).toList()
        );
    }

    public static MessageType toLspMessageType(ch.epfl.scala.bsp4j.MessageType bspMessageType) {
        return MessageType.forValue(bspMessageType.getValue());
    }

    public static MessageParams toLspMessageParams(ShowMessageParams bspShowMessage) {
        return new MessageParams(
            toLspMessageType(bspShowMessage.getType()),
            bspShowMessage.getMessage()
        );
    }
}
