package org.mina_lang.langserver;

import ch.epfl.scala.bsp4j.BspConnectionDetails;
import ch.epfl.scala.bsp4j.BuildServer;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import dev.dirs.BaseDirectories;
import org.eclipse.lsp4j.WorkspaceFolder;
import org.eclipse.lsp4j.jsonrpc.Launcher;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;
import java.util.function.BiPredicate;
import java.util.stream.Stream;

public class MinaBuildServer {
    private static final Gson gson = new Gson();
    private static final BiPredicate<Path, BasicFileAttributes> isJsonFile = (path, attrs) -> {
        var fileName = path.getFileName().toString();
        return fileName.endsWith(".json") && attrs.isRegularFile();
    };

    private final MinaLanguageServer languageServer;

    public MinaBuildServer(MinaLanguageServer languageServer) {
        this.languageServer = languageServer;
    }

    public static List<BspConnectionDetails> discover(WorkspaceFolder folder) throws URISyntaxException, IOException {
        Path workspacePath = Paths.get(new URI(folder.getUri()));
        Path workspaceBspFolder = workspacePath.resolve(".bsp");

        List<BspConnectionDetails> workspaceConnectionFiles = discover(workspaceBspFolder).toList();

        if (workspaceConnectionFiles.isEmpty()) {
            Path dataLocalDirPath = Paths.get(BaseDirectories.get().dataLocalDir);
            Path dataLocalDirBspFolder = dataLocalDirPath.resolve("bsp");

            Path dataDirPath = Paths.get(BaseDirectories.get().dataDir);
            Path dataDirBspFolder = dataDirPath.resolve("bsp");

            List<BspConnectionDetails> dataDirConnectionFiles = Stream.concat(
                discover(dataLocalDirBspFolder),
                discover(dataDirBspFolder)
            ).distinct().toList();

            if (dataDirConnectionFiles.isEmpty()) {
                // TODO: Try system-level directories to follow the BSP spec
                return List.of();
            } else {
                return dataDirConnectionFiles;
            }
        } else {
            return workspaceConnectionFiles;
        }
    }

    public static Stream<BspConnectionDetails> discover(Path bspFolder) throws IOException {
        try (var connectionFiles = Files.find(bspFolder, 1, isJsonFile)) {
            return connectionFiles.flatMap(connectionFile -> {
                try {
                    return Stream.of(gson.fromJson(Files.readString(connectionFile), BspConnectionDetails.class));
                } catch (IOException | JsonSyntaxException e) {
                    return Stream.empty();
                }
            });
        }
    }

    public MinaBuildClient connect(WorkspaceFolder workspaceFolder, BspConnectionDetails details) throws URISyntaxException, IOException {
        var processBuilder = new ProcessBuilder(details.getArgv())
            .directory(Paths.get(new URI(workspaceFolder.getUri())).toFile());

        processBuilder.environment().putAll(System.getenv());

        var buildServerProcess = processBuilder.start();

        var buildClient = new MinaBuildClient(languageServer);

        var launcher = new Launcher.Builder<BuildServer>()
            .setLocalService(buildClient)
            .setRemoteInterface(BuildServer.class)
            .setInput(buildServerProcess.getInputStream())
            .setOutput(buildServerProcess.getOutputStream())
            .setExecutorService(languageServer.getExecutor())
            .create();

        launcher.startListening();

        buildClient.onConnectWithServer(launcher.getRemoteProxy());

        return buildClient;
    }
}
