import * as child_process from "child_process";
import * as os from "os";
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

import { getApi, FileDownloader } from "@microsoft/vscode-file-downloader-api";
import { findRuntimes } from "jdk-utils";

const COURSIER_DOWNLOAD_TIMEOUT_MS = 120000;
const COURSIER_LAUNCHER_URI = Uri.parse(
  "https://github.com/coursier/launchers/raw/master/coursier.jar"
);

let client: LanguageClient;

async function downloadCoursierJar(
  context: ExtensionContext,
  fileDownloader: FileDownloader
): Promise<Uri> {
  return await window.withProgress(
    {
      location: ProgressLocation.Window,
    },
    (progress, cancellationToken) => {
      return fileDownloader.downloadFile(
        COURSIER_LAUNCHER_URI,
        "coursier.jar",
        context,
        cancellationToken,
        (_downloadedBytes, _totalBytes) =>
          progress.report({ message: "Downloading coursier JAR" }),
        { timeoutInMs: COURSIER_DOWNLOAD_TIMEOUT_MS }
      );
    }
  );
}

async function fetchCoursierJar(
  context: ExtensionContext,
  fileDownloader: FileDownloader
): Promise<Uri> {
  return (
    (await fileDownloader.tryGetItem("coursier.jar", context)) ||
    (await downloadCoursierJar(context, fileDownloader))
  );
}

async function getServerClasspath(
  javaExecutable: string,
  coursierJar: Uri
): Promise<string> {
  return (
    await util.promisify(child_process.execFile)(
      javaExecutable,
      [
        "-jar",
        `"${coursierJar.fsPath}"`,
        "fetch",
        "--classpath",
        "org.mina-lang:mina-lang-server:0.1.0-SNAPSHOT",
      ],
      { shell: true, env: { COURSIER_NO_TERM: "true", ...process.env } }
    )
  ).stdout.trim();
}

function getGcOptions(): string[] {
  // As per [https://access.redhat.com/documentation/en-us/openjdk/11/html/using_shenandoah_garbage_collector_with_openjdk_11/shenandoah-gc-basic-configuration]
  const gcOptions = [
    "-XX:+UseShenandoahGC",
    "-XX:+AlwaysPreTouch",
    "-XX:+UseNUMA",
    "-XX:+DisableExplicitGC",
  ];

  if (os.type() === "Linux") {
    gcOptions.push("-XX:+UseLargePages");
    gcOptions.push("-XX:+UseTransparentHugePages");
  }

  return gcOptions;
}

function getRemoteDebugOptions(): string[] {
  const remoteDebugConfig = workspace.getConfiguration(
    "mina.languageServer.remoteDebug"
  );
  const remoteDebugEnabled = remoteDebugConfig.get<boolean>("enabled");
  const remoteDebugAddress = remoteDebugConfig.get<string>("address");
  const remoteDebugPort = remoteDebugConfig.get<number>("port");
  const remoteDebugSuspend = remoteDebugConfig.get<boolean>("suspend")
    ? "y"
    : "n";

  if (!remoteDebugEnabled) {
    return [];
  }

  const jdwpOptions = [
    "jdwp=transport=dt_socket",
    "server=y",
    `suspend=${remoteDebugSuspend}`,
    `address=${remoteDebugAddress}:${remoteDebugPort}`,
  ].join(",");

  return [`-agentlib:${jdwpOptions}`];
}

async function getProfilingOptions(
  javaExecutable: string,
  coursierJar: Uri
): Promise<string[]> {
  const profilingConfig = workspace.getConfiguration(
    "mina.languageServer.profiling"
  );
  const profilingEnabled = profilingConfig.get<boolean>("enabled");
  const profilingAppName = profilingConfig.get<string>("applicationName");
  const profilingServerAddress = profilingConfig.get<string>("serverAddress");

  if (!profilingEnabled) {
    return [];
  }

  const profilingAgentClasspath = await getProfilingAgentClasspath(
    javaExecutable,
    coursierJar
  );

  return [
    `-javaagent:${profilingAgentClasspath}`,
    `-Dpyroscope.application.name=${profilingAppName}`,
    `-Dpyroscope.server.address=${profilingServerAddress}`,
    "-Dpyroscope.format=jfr",
  ];
}

async function getProfilingAgentClasspath(
  javaExecutable: string,
  coursierJar: Uri
): Promise<string> {
  return (
    await util.promisify(child_process.execFile)(
      javaExecutable,
      [
        "-jar",
        `"${coursierJar.fsPath}"`,
        "fetch",
        "--classpath",
        "io.pyroscope:agent:0.11.5",
      ],
      { shell: true, env: { COURSIER_NO_TERM: "true", ...process.env } }
    )
  ).stdout.trim();
}

export async function activate(context: ExtensionContext) {
  const runtimes = await findRuntimes({ withVersion: true, withTags: true });
  const javaHomeRuntime = runtimes.find((runtime) => runtime.isJavaHomeEnv);
  const serverConfiguration = workspace.getConfiguration("mina.languageServer");

  if (javaHomeRuntime) {
    const fileDownloader: FileDownloader = await getApi();
    const coursierJar = await fetchCoursierJar(context, fileDownloader);
    const javaExecutable = path.join(javaHomeRuntime.homedir, "bin", "java");

    const gcOptions = getGcOptions();
    const remoteDebugOptions = getRemoteDebugOptions();
    const jvmOptions = serverConfiguration.get<string[]>("jvmOptions", []);
    const profilingOptions = await getProfilingOptions(
      javaExecutable,
      coursierJar
    );

    const serverClasspath = await getServerClasspath(
      javaExecutable,
      coursierJar
    );

    const serverOptions: ServerOptions = {
      transport: TransportKind.pipe,
      command: javaExecutable,
      args: [
        `-DSTORAGE_FOLDER=${context.globalStorageUri.fsPath}`,
        `-DLOG_FOLDER=${context.logUri.fsPath}`,
        ...jvmOptions,
        ...remoteDebugOptions,
        ...gcOptions,
        ...profilingOptions,
        "-cp",
        serverClasspath,
        "org.mina_lang.langserver.MinaServerLauncher",
      ],
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

    await client.start();
  } else {
    return await window.showErrorMessage(
      "The JAVA_HOME variable is not set. Unable to determine JDK location."
    );
  }
}

export async function deactivate() {
  if (!client) {
    return;
  } else {
    return await client.stop();
  }
}
