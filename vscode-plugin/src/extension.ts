import * as child_process from "child_process";
import * as path from "path";
import * as util from "util";

import {
  ExtensionContext,
  ProgressLocation,
  Uri,
  window,
  workspace,
} from "vscode";

import {
  LanguageClient,
  LanguageClientOptions,
  ServerOptions,
  TransportKind,
} from "vscode-languageclient/node";

import { findRuntimes } from "jdk-utils";
import { getApi } from "@microsoft/vscode-file-downloader-api";

let client: LanguageClient;

export async function activate(context: ExtensionContext) {
  const runtimes = await findRuntimes({ withVersion: true, withTags: true });
  const javaHomeRuntime = runtimes.find((runtime) => runtime.isJavaHomeEnv);

  if (javaHomeRuntime) {
    const fileDownloader = await getApi();

    const coursierLaunchers = Uri.parse(
      "https://github.com/coursier/launchers/raw/master/coursier.jar"
    );

    const coursierJar =
      (await fileDownloader.tryGetItem("coursier.jar", context)) ||
      (await window.withProgress(
        {
          location: ProgressLocation.Window,
        },
        (progress, cancellationToken) => {
          return fileDownloader.downloadFile(
            coursierLaunchers,
            "coursier.jar",
            context,
            cancellationToken,
            (downloadedBytes, totalBytes) =>
              progress.report({
                message: "Downloading coursier JAR",
                increment: Math.round((downloadedBytes / totalBytes) * 100),
              })
          );
        }
      ));

    const javaExecutable = path.join(javaHomeRuntime.homedir, "bin", "java");

    const getServerClasspath = await util.promisify(child_process.execFile)(
      javaExecutable,
      [
        "-jar",
        `"${coursierJar.fsPath}"`,
        "fetch",
        "--classpath",
        "org.mina-lang:mina-server:0.1.0-SNAPSHOT",
      ],
      { shell: true, env: { COURSIER_NO_TERM: "true", ...process.env } }
    );

    const serverOptions: ServerOptions = {
      command: javaExecutable,
      args: [
        `-DLOG_FOLDER=${context.logUri.fsPath}`,
        "-cp",
        getServerClasspath.stdout,
        "org.mina_lang.main.Server",
      ],
      transport: TransportKind.pipe
    };

    const clientOptions: LanguageClientOptions = {
      documentSelector: [
        { scheme: "file", language: "mina" },
        { scheme: "jar", language: "mina" },
      ],
      synchronize: {
        fileEvents: workspace.createFileSystemWatcher("**/*.mina"),
      },
    };

    client = new LanguageClient(
      "mina-lang-server",
      "Mina Language Server",
      serverOptions,
      clientOptions
    );

    client.start();
  } else {
    return await window.showErrorMessage(
      "The JAVA_HOME variable is not set. Unable to determine JDK location."
    );
  }
}

export function deactivate() {
  if (!client) {
    return;
  } else {
    return client.stop();
  }
}
