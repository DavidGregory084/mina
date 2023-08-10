import * as child_process from "child_process";
import * as os from "os";
import * as path from "path";
import * as util from "util";

import {
  ExtensionContext,
  OutputChannel,
  ProgressLocation,
  Uri,
  WorkspaceConfiguration,
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
  coursierJar: Uri,
  serverConfiguration: WorkspaceConfiguration
): Promise<string> {
  const serverVersion = serverConfiguration.get<number>("version");

  return (
    await util.promisify(child_process.execFile)(
      javaExecutable,
      [
        "-jar",
        `"${coursierJar.fsPath}"`,
        "fetch",
        "--classpath",
        `org.mina-lang:mina-lang-server:${serverVersion}`,
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
    "transport=dt_socket",
    "server=y",
    `suspend=${remoteDebugSuspend}`,
    `address=${remoteDebugAddress}:${remoteDebugPort}`,
  ].join(",");

  return [`-agentlib:jdwp=${jdwpOptions}`];
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
  const profilingAgentVersion = profilingConfig.get<string>("agentVersion");

  if (!profilingEnabled || !profilingAgentVersion) {
    return [];
  }

  const profilingAgentClasspath = await getProfilingAgentClasspath(
    javaExecutable,
    coursierJar,
    profilingAgentVersion
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
  coursierJar: Uri,
  agentVersion: string
): Promise<string> {
  return (
    await util.promisify(child_process.execFile)(
      javaExecutable,
      [
        "-jar",
        `"${coursierJar.fsPath}"`,
        "fetch",
        "--classpath",
        `io.pyroscope:agent:${agentVersion}`,
      ],
      { shell: true, env: { COURSIER_NO_TERM: "true", ...process.env } }
    )
  ).stdout.trim();
}

async function start(context: ExtensionContext, outputChannel: OutputChannel) {
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
      coursierJar,
      serverConfiguration
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
        "org.mina_lang.langserver.MinaLanguageServerLauncher",
      ],
    };

    const clientOptions: LanguageClientOptions = {
      outputChannel,
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

async function stop() {
  if (!client) {
    return;
  } else {
    return await client.stop();
  }
}

async function restart(
  context: ExtensionContext,
  outputChannel: OutputChannel
) {
  await stop();
  await start(context, outputChannel);
}

export async function activate(context: ExtensionContext) {
  const outputChannel = window.createOutputChannel("Mina Language Server");

  await start(context, outputChannel);

  context.subscriptions.push(
    workspace.onDidChangeConfiguration(async (event) => {
      // Restart when configuration that affects server artifact or process args is changed
      if (
        event.affectsConfiguration("mina.languageServer.version") ||
        event.affectsConfiguration("mina.languageServer.remoteDebug") ||
        event.affectsConfiguration("mina.languageServer.profiling")
      ) {
        await restart(context, outputChannel);
      }
    })
  );
}

export async function deactivate() {
  await stop();
}
