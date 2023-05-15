import * as cp from "child_process";
import * as path from "path";

import {
  downloadAndUnzipVSCode,
  resolveCliArgsFromVSCodeExecutablePath,
  runTests,
} from "@vscode/test-electron";
import { TestOptions } from "@vscode/test-electron/out/runTest";

async function main() {
  try {
    // The folder containing the Extension Manifest package.json
    // Passed to `--extensionDevelopmentPath`
    const extensionDevelopmentPath = path.resolve(__dirname, "../../");

    // The path to the extension test script
    // Passed to --extensionTestsPath
    const extensionTestsPath = path.resolve(__dirname, "./suite/index");

    const testOptions: TestOptions = {
      extensionDevelopmentPath,
      extensionTestsPath,
    };

    const vscodeExecutablePath = await downloadAndUnzipVSCode(testOptions);

    const [cli, ...args] =
      resolveCliArgsFromVSCodeExecutablePath(vscodeExecutablePath);

    cp.spawnSync(
      cli,
      [...args, "--install-extension", "mindaro-dev.file-downloader"],
      {
        encoding: "utf-8",
        stdio: "inherit",
      }
    );

    // Download VS Code, unzip it and run the integration test
    await runTests(testOptions);
  } catch (err) {
    console.error("Failed to run tests");
    process.exit(1);
  }
}

main();
