/*
 * This Java source file was generated by the Gradle 'init' task.
 * template from https://docs.oracle.com/javase/8/javafx/get-started-tutorial/hello_world.htm
 */
package modder;

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.util.List;
import java.util.ArrayList;

// 
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Spec;

@Command(name = "Modder", subcommands = {
        CommandLine.HelpCommand.class }, description = "Utilities for hacking android apk")
class ModderMainCmd {
    @Spec
    CommandSpec spec;

    void ShowAdbShellError(Adb.Output out) {
        System.out.println("can't connect to adb shell:");
        out.strings.forEach(s -> System.out.println(s));

    }

    @Command(name = "listApk", description = "List installed apks")
    void ListApk() {

        Adb adb = new Adb();
        Adb.Output out = adb.ListApk();
        if (out.error != Adb.Error.ok) {
            ShowAdbShellError(out);
            return;
        }
        // output will look like
        // package:com.android.offfice
        // package:com.vivo.appstore
        // "package:" should be trimmed for better view
        for (int i = 0; i < out.strings.size(); i++) {
            // use the caret symbol '^'
            // to match the beggining of the pattern
            String new_str = out.strings.get(i).replaceFirst("^package:", "");
            System.out.printf("%d %s\n", i, new_str);
        }
        System.out.printf("Found %d packages\n", out.strings.size());

    }

    @Command(name = "download", description = "Download an apk from device")
    void Download(

            @Parameters(paramLabel = "packageName", description = "Package to download") String package_name

    ) {

        Adb adb = new Adb();
        // check if [package_name] exists
        Adb.Output out = adb.ListApk();
        if (out.error != Adb.Error.ok) {
            ShowAdbShellError(out);
            return;
        }

        if (!out.strings.contains(package_name)) {
            System.out.printf("package %s doesn't exist in the device\n", package_name);
            System.out.println("use listApk command to list installed packages");
            return;

        }
        out = adb.GetApkPathAtDevice(package_name);
        if (out.error != Adb.Error.ok) {
            ShowAdbShellError(out);
            return;
        }
        // TODO: add print method for Adb.Output
        // we need to loop when downloading the app
        // in case the apk is splitted apks (have multiple paths)
        System.out.println("Downloading apks ...");
        for (int i = 0; i < out.strings.size(); i++) {

            String apkPath = out.strings.get(i);
            System.out.printf("Downloading apk (%d/%d) at %s", i + 1, out.strings.size(), apkPath);
            Adb.Output downloadOut = adb.DownloadApk(apkPath);
            if (downloadOut.error != Adb.Error.ok) {
                ShowAdbShellError(out);
                return;
            }
            downloadOut.strings.forEach(System.out::println);
            System.out.printf("...done\n");
        }

    }

}

//
public class App {
    public String getGreeting() {
        return "Hello World!";
    }

    // workaround for java 11 since app built with
    // "gradle build", will be missing javaFX lib
    // https://stackoverflow.com/questions/59771324/error-javafx-runtime-components-are-missing-and-are-required-to-run-this-appli
    public static class RealApp extends Application {
        final int width = 587;
        final int height = 612;

        @Override
        public void start(Stage primaryStage) {

            if (!Util.DoesCommandExist("adb")) {
                GuiUtil.ShowAlert("Adb Command doesn't exist", "", "Command \"adb\" not found in PATH");
                return;
            }

            // ====================================================
            List<String> output = new ArrayList<String>();

            // test if adb shell can be connected by echoing in
            // the shell and check the output
            output = Util.RunCommand("adb", "shell echo test");
            if (output.size() >= 1) {
                if (!output.get(0).equals("test")) {
                    System.out.println();

                    GuiUtil.ShowAlert("Adb Shell Error",
                            String.format("Error connecting to adb shell: \"%s\" ", output.get(0)),

                            "Have you connected android device to your computer?");
                    return;
                }

            } else {
                GuiUtil.ShowAlert("Adb Shell Error", "", "Error connecting to adb shell: Empty Output");
                return;

            }

            // everything looks okay
            output = Util.RunCommand("adb", "shell pm list packages");
            output.forEach(s -> System.out.println(s));

            // ========================== init gui =================
            //
            StackPane root = new StackPane();
            Scene scene = new Scene(root, width, height);
            // ============== set button ================
            Button btn = new Button();
            btn.setText("Get Size");
            btn.setOnAction(new EventHandler<ActionEvent>() {

                @Override
                public void handle(ActionEvent event) {
                    System.out.println("width: " + scene.getWidth() + " height: " + scene.getHeight());
                }
            });

            root.getChildren().add(btn);
            // ==========================================

            primaryStage.setTitle("Hello World!");
            primaryStage.setScene(scene);
            primaryStage.setMaximized(true);
            // primaryStage.setFullScreen(true);
            primaryStage.show();
        }

    }

    public static void cliInit(String[] args) {
        /* */
        // CommandLine::setSubcommandsCaseInsensitive(true);
        //
        int exitCode = new CommandLine(new ModderMainCmd()).execute(args);
        System.exit(exitCode);

    }

    public static void main(String[] args) {
        // Some testing
        if (Util.DoesCommandExist("adb")) {
            System.out.println("adb exist");
        } else {
            System.out.println("adb doesn't exist");

        }

        // gui
        // Application.launch(RealApp.class);
        // cli app
        cliInit(args);

    }
}
