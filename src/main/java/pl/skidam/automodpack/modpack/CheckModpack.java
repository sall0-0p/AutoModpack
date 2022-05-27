package pl.skidam.automodpack.modpack;

import pl.skidam.automodpack.AutoModpackClient;
import pl.skidam.automodpack.utils.ToastExecutor;
import pl.skidam.automodpack.utils.WebFileSize;

import java.io.*;

import static pl.skidam.automodpack.AutoModpackClient.*;

public class CheckModpack {

    public CheckModpack() {

        // if latest modpack is not same as current modpack download new mods.
        // Check how big the Modpack file is

        AutoModpackClient.LOGGER.info("Checking if modpack is up-to-date...");

        File Modpack = new File("./AutoModpack/modpack.zip");
        long currentSize = Modpack.length();
        long latestSize = Long.parseLong(WebFileSize.webfileSize(link));

        if (!Modpack.exists() || currentSize != latestSize && latestSize != 0) {
            AutoModpackClient.LOGGER.info("Downloading modpack!");
            new ToastExecutor(1);
            new DownloadModpack();
            return;
        }

        AutoModpackClient.LOGGER.info("Didn't found any updates for modpack!");
        new ToastExecutor(3);
        new UnZip(out, "false");
    }
}
