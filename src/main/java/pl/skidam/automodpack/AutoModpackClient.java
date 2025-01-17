package pl.skidam.automodpack;

import io.netty.buffer.Unpooled;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientLoginConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientLoginNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientLoginNetworkHandler;
import net.minecraft.network.PacketByteBuf;
import pl.skidam.automodpack.client.StartAndCheck;
import pl.skidam.automodpack.client.modpack.CheckModpack;

import java.io.*;
import java.util.Scanner;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import static pl.skidam.automodpack.AutoModpackMain.*;
import static pl.skidam.automodpack.utils.ValidateURL.ValidateURL;

public class AutoModpackClient implements ClientModInitializer {

    public static boolean isOnServer;
    public static String serverIP;
    public static final File modpack_link = new File("./AutoModpack/modpack-link.txt");

    @Override
    public void onInitializeClient() {

        LOGGER.info("Initializing AutoModpack...");

        isOnServer = false;
        CheckModpack.isCheckUpdatesButtonClicked = false;

        // Load saved link from ./AutoModpack/modpack-link.txt file
        String savedLink = "";
        try {
            FileReader fr = new FileReader(modpack_link);
            Scanner inFile = new Scanner(fr);
            if (inFile.hasNextLine()) {
                savedLink = inFile.nextLine();
            }
            inFile.close();
        } catch (Exception ignored) {
        }

        if (!savedLink.equals("")) {
            if (ValidateURL(savedLink)) {
                link = savedLink;
                LOGGER.info("Loaded saved link to modpack: " + link);
            } else {
                LOGGER.error("Saved link is not valid url or is not end with /modpack");
            }
        }

        // Packets
        ClientLoginNetworking.registerGlobalReceiver(AM_LINK, this::onServerLinkReceived);

        // Register
        ClientLoginConnectionEvents.QUERY_START.register((clientLoginNetworkHandler, minecraftClient) -> {
            serverIP = clientLoginNetworkHandler.getConnection().getAddress().toString();
            isOnServer = true;
        });
        ClientLoginConnectionEvents.DISCONNECT.register((clientLoginNetworkHandler, minecraftClient) -> isOnServer = false);

        new StartAndCheck(true, false);
    }

    private CompletableFuture<PacketByteBuf> onServerLinkReceived(MinecraftClient minecraftClient, ClientLoginNetworkHandler clientLoginNetworkHandler, PacketByteBuf inBuf, Consumer<GenericFutureListener<? extends Future<? super Void>>> consumer) {
        String receivedLink = inBuf.readString(100);
        link = receivedLink;
        try {
            FileWriter fWriter = new FileWriter(modpack_link);
            fWriter.flush();
            fWriter.write(receivedLink);
            fWriter.close();
        } catch (IOException e) { // ignore
        }
        LOGGER.info("Link received from server: {}. Saved to file.", receivedLink);
        new StartAndCheck(false, true);
        PacketByteBuf outBuf = PacketByteBufs.create();
        outBuf.writeString("1");
        return CompletableFuture.completedFuture(outBuf);
    }
}
