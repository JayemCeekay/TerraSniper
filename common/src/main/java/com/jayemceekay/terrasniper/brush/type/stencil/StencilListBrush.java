package com.jayemceekay.terrasniper.brush.type.stencil;

import com.jayemceekay.terrasniper.TerraSniper;
import com.jayemceekay.terrasniper.brush.type.AbstractBrush;
import com.jayemceekay.terrasniper.sniper.snipe.Snipe;
import com.jayemceekay.terrasniper.sniper.snipe.message.SnipeMessenger;
import com.jayemceekay.terrasniper.util.material.Materials;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.extension.input.InputParseException;
import com.sk89q.worldedit.extension.input.ParserContext;
import com.sk89q.worldedit.forge.ForgeAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.world.block.BlockState;
import com.sk89q.worldedit.world.block.BlockTypes;
import net.minecraft.ChatFormatting;
import net.minecraft.server.level.ServerPlayer;
import org.enginehub.piston.converter.SuggestionHelper;

import java.io.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.stream.Stream;

public class StencilListBrush extends AbstractBrush {

    private static final int DEFAULT_PASTE_OPTION = 1;

    private final Map<Integer, String> stencilList = new HashMap<>();
    private String filename = "NoFileLoaded";
    private short x;
    private short z;
    private short y;
    private short xRef;
    private short zRef;
    private short yRef;

    private byte pasteOption; // 0 = full, 1 = fill, 2 = replace

    @Override
    public void loadProperties() {
        this.pasteOption = (byte) 0;

        File dataFolder = new File(TerraSniper.TERRASNIPER_CONFIG_FOLDER.getPath(), "/stencilLists");
        dataFolder.mkdirs();
    }

    @Override
    public void handleCommand(String[] parameters, Snipe snipe) {
        SnipeMessenger messenger = snipe.createMessenger();
        String firstParameter = parameters[0];

        if (firstParameter.equalsIgnoreCase("info")) {
            messenger.sendMessage(ChatFormatting.GOLD + "Stencil List Brush Parameters:");
            messenger.sendMessage(ChatFormatting.AQUA + "/b sl (full|fill|replace) [s] -- Loads the specified stencil list s. " +
                    "Full/fill/replace must come first. Full = paste all blocks, " +
                    "fill = paste only into air blocks, replace = paste full blocks in only, but replace anything in their way.");
        } else {
            byte pasteOption;
            byte pasteParam;
            if (firstParameter.equalsIgnoreCase("full")) {
                pasteOption = 0;
                pasteParam = 1;
            } else if (firstParameter.equalsIgnoreCase("fill")) {
                pasteOption = 1;
                pasteParam = 1;
            } else if (firstParameter.equalsIgnoreCase("replace")) {
                pasteOption = 2;
                pasteParam = 1;
            } else {
                // Reset to [s] parameter expected.
                pasteOption = 1;
                pasteParam = 0;
            }
            if (parameters.length != 1 + pasteParam) {
                messenger.sendMessage(ChatFormatting.RED + "Missing arguments, this command expects more.");
                return;
            }

            this.pasteOption = pasteOption;
            try {
                this.filename = parameters[pasteParam];
                File file = new File(TerraSniper.TERRASNIPER_CONFIG_FOLDER.getPath(), "/stencilLists/" + this.filename + ".txt");
                if (file.exists()) {
                    messenger.sendMessage(ChatFormatting.RED + "Stencil List '" + this.filename + "' exists and was loaded.");
                    readStencilList();
                } else {
                    messenger.sendMessage(ChatFormatting.AQUA + "Stencil List '" + this.filename + "' does not exist.  This brush will not function without a valid stencil list.");
                    this.filename = "NoFileLoaded";
                }
            } catch (RuntimeException exception) {
                messenger.sendMessage(ChatFormatting.RED + "You need to type a stencil list name.");
            }
        }
    }

    @Override
    public List<String> handleCompletions(String[] parameters, Snipe snipe) {
        if (parameters.length > 0) {
            String parameter = parameters[parameters.length - 1];
            return SuggestionHelper.limitByPrefix(Stream.of("full", "fill", "replace"), parameter);
        } else {
            return SuggestionHelper.limitByPrefix(Stream.of("full", "fill", "replace"), "");
        }

    }

    @Override
    public void handleArrowAction(Snipe snipe) {
        stencilPaste(snipe);
    }

    @Override
    public void handleGunpowderAction(Snipe snipe) {
        stencilPasteRotation(snipe);
    }

    private String readRandomStencil() {
        double rand = Math.random() * (this.stencilList.size());
        int choice = (int) rand;
        return this.stencilList.get(choice);
    }

    private void readStencilList() {
        stencilList.clear();
        File file = new File(TerraSniper.TERRASNIPER_CONFIG_FOLDER.getPath(), "/stencilLists/" + this.filename + ".txt");
        if (file.exists()) {
            try {
                Scanner scanner = new Scanner(file);
                int counter = 0;
                while (scanner.hasNext()) {
                    this.stencilList.put(counter, scanner.nextLine());
                    counter++;
                }
                scanner.close();
            } catch (FileNotFoundException exception) {
                exception.printStackTrace();
            }
        }
    }

    private void stencilPaste(Snipe snipe) {
        SnipeMessenger messenger = snipe.createMessenger();
        if (this.filename.matches("NoFileLoaded")) {
            messenger.sendMessage(ChatFormatting.RED + "You did not specify a filename for the list.  This is required.");
            return;
        }
        String stencilName = this.readRandomStencil();
        messenger.sendMessage(stencilName);
        File file = new File(TerraSniper.TERRASNIPER_CONFIG_FOLDER.getPath(), "/stencils/" + stencilName + ".vstencil");
        if (file.exists()) {
            try {
                DataInputStream in = new DataInputStream(new BufferedInputStream(new FileInputStream(file)));
                this.x = in.readShort();
                this.z = in.readShort();
                this.y = in.readShort();
                this.xRef = in.readShort();
                this.zRef = in.readShort();
                this.yRef = in.readShort();
                int numRuns = in.readInt();
                // Something here that checks ranks using sanker'world thingie he added to Sniper and boots you out with error message if too big.
                int volume = this.x * this.y * this.z;
                messenger.sendMessage(ChatFormatting.AQUA + this.filename + " pasted.  Volume is " + volume + " blocks.");
                int currX = -this.xRef; // so if your ref point is +5 x, you want to start pasting -5 blocks from the clicked point (the reference) to get the
                // corner, for example.
                int currZ = -this.zRef;
                int currY = -this.yRef;
                BlockState blockData;
                BlockVector3 targetBlock = getTargetBlock();
                if (this.pasteOption == 0) {
                    for (int i = 1; i < numRuns + 1; i++) {
                        if (in.readBoolean()) {
                            int numLoops = in.readByte() + 128;
                            blockData = readBlockData(in, snipe);
                            for (int j = 0; j < numLoops; j++) {
                                setBlockData(
                                        targetBlock.getX() + currX,
                                        clampY(targetBlock.getY() + currY),
                                        targetBlock.getZ() + currZ,
                                        blockData
                                );
                                currX++;
                                if (currX == this.x - this.xRef) {
                                    currX = -this.xRef;
                                    currZ++;
                                    if (currZ == this.z - this.zRef) {
                                        currZ = -this.zRef;
                                        currY++;
                                    }
                                }
                            }
                        } else {
                            setBlockData(
                                    targetBlock.getX() + currX,
                                    clampY(targetBlock.getY() + currY),
                                    targetBlock.getZ() + currZ,
                                    readBlockData(in, snipe)
                            );
                            currX++;
                            if (currX == this.x - this.xRef) {
                                currX = -this.xRef;
                                currZ++;
                                if (currZ == this.z - this.zRef) {
                                    currZ = -this.zRef;
                                    currY++;
                                }
                            }
                        }
                    }
                } else if (this.pasteOption == 1) {
                    for (int i = 1; i < numRuns + 1; i++) {
                        if (in.readBoolean()) {
                            int numLoops = in.readByte() + 128;
                            blockData = readBlockData(in, snipe);
                            for (int j = 0; j < numLoops; j++) {
                                if (!Materials.isEmpty(blockData.getBlockType()) && clampY(
                                        targetBlock.getX() + currX,
                                        targetBlock.getY() + currY,
                                        targetBlock.getZ() + currZ
                                ).getBlockType().getMaterial().isAir()) {
                                    setBlockData(
                                            targetBlock.getX() + currX,
                                            clampY(targetBlock.getY() + currY),
                                            targetBlock.getZ() + currZ,
                                            blockData
                                    );
                                }
                                currX++;
                                if (currX == this.x - this.xRef) {
                                    currX = -this.xRef;
                                    currZ++;
                                    if (currZ == this.z - this.zRef) {
                                        currZ = -this.zRef;
                                        currY++;
                                    }
                                }
                            }
                        } else {
                            blockData = readBlockData(in, snipe);
                            if (!Materials.isEmpty(blockData.getBlockType()) && this.clampY(
                                    targetBlock.getX() + currX,
                                    targetBlock.getY() + currY,
                                    targetBlock.getZ() + currZ
                            ).getBlockType().getMaterial().isAir()) {
                                setBlockData(
                                        targetBlock.getX() + currX,
                                        clampY(targetBlock.getY() + currY),
                                        targetBlock.getZ() + currZ,
                                        blockData
                                );
                            }
                            currX++;
                            if (currX == this.x - this.xRef) {
                                currX = -this.xRef;
                                currZ++;
                                if (currZ == this.z - this.zRef) {
                                    currZ = -this.zRef;
                                    currY++;
                                }
                            }
                        }
                    }
                } else { // replace
                    for (int i = 1; i < numRuns + 1; i++) {
                        if (in.readBoolean()) {
                            int numLoops = in.readByte() + 128;
                            blockData = readBlockData(in, snipe);
                            for (int j = 0; j < (numLoops); j++) {
                                if (!Materials.isEmpty(blockData.getBlockType())) {
                                    setBlockData(
                                            targetBlock.getX() + currX,
                                            clampY(targetBlock.getY() + currY),
                                            targetBlock.getZ() + currZ,
                                            blockData
                                    );
                                }
                                currX++;
                                if (currX == this.x - this.xRef) {
                                    currX = -this.xRef;
                                    currZ++;
                                    if (currZ == this.z - this.zRef) {
                                        currZ = -this.zRef;
                                        currY++;
                                    }
                                }
                            }
                        } else {
                            blockData = readBlockData(in, snipe);
                            if (!Materials.isEmpty(blockData.getBlockType())) {
                                setBlockData(
                                        targetBlock.getX() + currX,
                                        clampY(targetBlock.getY() + currY),
                                        targetBlock.getZ() + currZ,
                                        blockData
                                );
                            }
                            currX++;
                            if (currX == this.x) {
                                currX = 0;
                                currZ++;
                                if (currZ == this.z) {
                                    currZ = 0;
                                    currY++;
                                }
                            }
                        }
                    }
                }
                in.close();
            } catch (IOException | MaxChangedBlocksException exception) {
                messenger.sendMessage(ChatFormatting.RED + "Something went wrong.");
                exception.printStackTrace();
            }
        } else {
            messenger.sendMessage(ChatFormatting.RED + "You need to type a stencil name / your specified stencil does not exist.");
        }
    }

    private void stencilPaste180(Snipe snipe) {
        SnipeMessenger messenger = snipe.createMessenger();
        if (this.filename.matches("NoFileLoaded")) {
            messenger.sendMessage(ChatFormatting.RED + "You did not specify a filename for the list.  This is required.");
            return;
        }
        String stencilName = this.readRandomStencil();
        File file = new File(TerraSniper.TERRASNIPER_CONFIG_FOLDER.getPath(), "/stencils/" + stencilName + ".vstencil");
        if (file.exists()) {
            try {
                DataInputStream in = new DataInputStream(new BufferedInputStream(new FileInputStream(file)));
                this.x = in.readShort();
                this.z = in.readShort();
                this.y = in.readShort();
                this.xRef = in.readShort();
                this.zRef = in.readShort();
                this.yRef = in.readShort();
                int numRuns = in.readInt();
                // Something here that checks ranks using sanker'world thingie he added to Sniper and boots you out with error message if too big.
                int volume = this.x * this.y * this.z;
                messenger.sendMessage(ChatFormatting.AQUA + this.filename + " pasted.  Volume is " + volume + " blocks.");
                int currX = this.xRef; // so if your ref point is +5 x, you want to start pasting -5 blocks from the clicked point (the reference) to get the
                // corner, for example.
                int currZ = this.zRef;
                int currY = -this.yRef;
                BlockState blockData;
                BlockVector3 targetBlock = this.getTargetBlock();
                if (this.pasteOption == 0) {
                    for (int i = 1; i < numRuns + 1; i++) {
                        if (in.readBoolean()) {
                            int numLoops = in.readByte() + 128;
                            blockData = readBlockData(in, snipe);
                            for (int j = 0; j < numLoops; j++) {
                                setBlockData(
                                        targetBlock.getX() + currX,
                                        clampY(targetBlock.getY() + currY),
                                        targetBlock.getZ() + currZ,
                                        blockData
                                );
                                currX--;
                                if (currX == -this.x + this.xRef) {
                                    currX = this.xRef;
                                    currZ--;
                                    if (currZ == -this.z + this.zRef) {
                                        currZ = this.zRef;
                                        currY++;
                                    }
                                }
                            }
                        } else {
                            setBlockData(
                                    targetBlock.getX() + currX,
                                    clampY(targetBlock.getY() + currY),
                                    targetBlock.getZ() + currZ,
                                    readBlockData(in, snipe)
                            );
                            currX--;
                            if (currX == -this.x + this.xRef) {
                                currX = this.xRef;
                                currZ--;
                                if (currZ == -this.z + this.zRef) {
                                    currZ = this.zRef;
                                    currY++;
                                }
                            }
                        }
                    }
                } else if (this.pasteOption == 1) {
                    for (int i = 1; i < numRuns + 1; i++) {
                        if (in.readBoolean()) {
                            int numLoops = in.readByte() + 128;
                            blockData = readBlockData(in, snipe);
                            for (int j = 0; j < numLoops; j++) {
                                if (!Materials.isEmpty(blockData.getBlockType()) && this.clampY(
                                        targetBlock.getX() + currX,
                                        targetBlock.getY() + currY,
                                        targetBlock.getZ() + currZ
                                ).getBlockType().getMaterial().isAir()) {
                                    setBlockData(
                                            targetBlock.getX() + currX,
                                            clampY(targetBlock.getY() + currY),
                                            targetBlock.getZ() + currZ,
                                            blockData
                                    );
                                }
                                currX--;
                                if (currX == -this.x + this.xRef) {
                                    currX = this.xRef;
                                    currZ--;
                                    if (currZ == -this.z + this.zRef) {
                                        currZ = this.zRef;
                                        currY++;
                                    }
                                }
                            }
                        } else {
                            blockData = readBlockData(in, snipe);
                            if (!Materials.isEmpty(blockData.getBlockType()) && this.clampY(
                                    targetBlock.getX() + currX,
                                    targetBlock.getY() + currY,
                                    targetBlock.getZ() + currZ
                            ).getBlockType().getMaterial().isAir()) {
                                setBlockData(
                                        targetBlock.getX() + currX,
                                        clampY(targetBlock.getY() + currY),
                                        targetBlock.getZ() + currZ,
                                        blockData
                                );
                            }
                            currX--;
                            if (currX == -this.x + this.xRef) {
                                currX = this.xRef;
                                currZ--;
                                if (currZ == -this.z + this.zRef) {
                                    currZ = this.zRef;
                                    currY++;
                                }
                            }
                        }
                    }
                } else { // replace
                    for (int i = 1; i < numRuns + 1; i++) {
                        if (in.readBoolean()) {
                            int numLoops = in.readByte() + 128;
                            blockData = readBlockData(in, snipe);
                            for (int j = 0; j < (numLoops); j++) {
                                if (!Materials.isEmpty(blockData.getBlockType())) {
                                    setBlockData(
                                            targetBlock.getX() + currX,
                                            clampY(targetBlock.getY() + currY),
                                            targetBlock.getZ() + currZ,
                                            blockData
                                    );
                                }
                                currX--;
                                if (currX == -this.x + this.xRef) {
                                    currX = this.xRef;
                                    currZ--;
                                    if (currZ == -this.z + this.zRef) {
                                        currZ = this.zRef;
                                        currY++;
                                    }
                                }
                            }
                        } else {
                            blockData = readBlockData(in, snipe);
                            if (!Materials.isEmpty(blockData.getBlockType())) {
                                setBlockData(
                                        targetBlock.getX() + currX,
                                        clampY(targetBlock.getY() + currY),
                                        targetBlock.getZ() + currZ,
                                        blockData
                                );
                            }
                            currX--;
                            if (currX == -this.x + this.xRef) {
                                currX = this.xRef;
                                currZ--;
                                if (currZ == -this.z + this.zRef) {
                                    currZ = this.zRef;
                                    currY++;
                                }
                            }
                        }
                    }
                }
                in.close();
            } catch (IOException | MaxChangedBlocksException exception) {
                messenger.sendMessage(ChatFormatting.RED + "Something went wrong.");
                exception.printStackTrace();
            }
        } else {
            messenger.sendMessage(ChatFormatting.RED + "You need to type a stencil name / your specified stencil does not exist.");
        }
    }

    private void stencilPaste270(Snipe snipe) {
        SnipeMessenger messenger = snipe.createMessenger();
        if (this.filename.matches("NoFileLoaded")) {
            messenger.sendMessage(ChatFormatting.RED + "You did not specify a filename for the list.  This is required.");
            return;
        }
        String stencilName = this.readRandomStencil();
        File file = new File(TerraSniper.TERRASNIPER_CONFIG_FOLDER.getPath(), "/stencils/" + stencilName + ".vstencil");
        if (file.exists()) {
            try {
                DataInputStream in = new DataInputStream(new BufferedInputStream(new FileInputStream(file)));
                this.x = in.readShort();
                this.z = in.readShort();
                this.y = in.readShort();
                this.xRef = in.readShort();
                this.zRef = in.readShort();
                this.yRef = in.readShort();
                int numRuns = in.readInt();
                // Something here that checks ranks using sanker'world thingie he added to Sniper and boots you out with error message if too big.
                int volume = this.x * this.y * this.z;
                messenger.sendMessage(ChatFormatting.AQUA + this.filename + " pasted.  Volume is " + volume + " blocks.");
                int currX = this.zRef; // so if your ref point is +5 x, you want to start pasting -5 blocks from the clicked point (the reference) to get the
                // corner, for example.
                int currZ = -this.xRef;
                int currY = -this.yRef;
                BlockState blockData;
                BlockVector3 targetBlock = this.getTargetBlock();
                if (this.pasteOption == 0) {
                    for (int i = 1; i < numRuns + 1; i++) {
                        if (in.readBoolean()) {
                            int numLoops = in.readByte() + 128;
                            blockData = readBlockData(in, snipe);
                            for (int j = 0; j < numLoops; j++) {
                                setBlockData(
                                        targetBlock.getX() + currX,
                                        clampY(targetBlock.getY() + currY),
                                        targetBlock.getZ() + currZ,
                                        blockData
                                );
                                currZ++;
                                if (currZ == this.x - this.xRef) {
                                    currZ = -this.xRef;
                                    currX--;
                                    if (currX == -this.z + this.zRef) {
                                        currX = this.zRef;
                                        currY++;
                                    }
                                }
                            }
                        } else {
                            setBlockData(
                                    targetBlock.getX() + currX,
                                    clampY(targetBlock.getY() + currY),
                                    targetBlock.getZ() + currZ,
                                    readBlockData(in, snipe)
                            );
                            currZ++;
                            currZ++;
                            if (currZ == this.x - this.xRef) {
                                currZ = -this.xRef;
                                currX--;
                                if (currX == -this.z + this.zRef) {
                                    currX = this.zRef;
                                    currY++;
                                }
                            }
                        }
                    }
                } else if (this.pasteOption == 1) {
                    for (int i = 1; i < numRuns + 1; i++) {
                        if (in.readBoolean()) {
                            int numLoops = in.readByte() + 128;
                            blockData = readBlockData(in, snipe);
                            for (int j = 0; j < numLoops; j++) {
                                if (!Materials.isEmpty(blockData.getBlockType()) && this.clampY(
                                        targetBlock.getX() + currX,
                                        targetBlock.getY() + currY,
                                        targetBlock.getZ() + currZ
                                ).getBlockType().getMaterial().isAir()) { // no reason to paste air over
                                    // air, and it prevents us
                                    // most of the time from
                                    // having to even check the
                                    // block.
                                    setBlockData(
                                            targetBlock.getX() + currX,
                                            clampY(targetBlock.getY() + currY),
                                            targetBlock.getZ() + currZ,
                                            blockData
                                    );
                                }
                                currZ++;
                                if (currZ == this.x - this.xRef) {
                                    currZ = -this.xRef;
                                    currX--;
                                    if (currX == -this.z + this.zRef) {
                                        currX = this.zRef;
                                        currY++;
                                    }
                                }
                            }
                        } else {
                            blockData = readBlockData(in, snipe);
                            if (!Materials.isEmpty(blockData.getBlockType()) && this.clampY(
                                    targetBlock.getX() + currX,
                                    targetBlock.getY() + currY,
                                    targetBlock.getZ() + currZ
                            ).getBlockType().getMaterial().isAir()) { // no reason to paste air over
                                // air, and it prevents us most of
                                // the time from having to even
                                // check the block.
                                setBlockData(
                                        targetBlock.getX() + currX,
                                        clampY(targetBlock.getY() + currY),
                                        targetBlock.getZ() + currZ,
                                        blockData
                                );
                            }
                            currZ++;
                            if (currZ == this.x - this.xRef) {
                                currZ = -this.xRef;
                                currX--;
                                if (currX == -this.z + this.zRef) {
                                    currX = this.zRef;
                                    currY++;
                                }
                            }
                        }
                    }
                } else { // replace
                    for (int i = 1; i < numRuns + 1; i++) {
                        if (in.readBoolean()) {
                            int numLoops = in.readByte() + 128;
                            blockData = readBlockData(in, snipe);
                            for (int j = 0; j < (numLoops); j++) {
                                if (!Materials.isEmpty(blockData.getBlockType())) {
                                    setBlockData(
                                            targetBlock.getX() + currX,
                                            clampY(targetBlock.getY() + currY),
                                            targetBlock.getZ() + currZ,
                                            blockData
                                    );
                                }
                                currZ++;
                                if (currZ == this.x - this.xRef) {
                                    currZ = -this.xRef;
                                    currX--;
                                    if (currX == -this.z + this.zRef) {
                                        currX = this.zRef;
                                        currY++;
                                    }
                                }
                            }
                        } else {
                            blockData = readBlockData(in, snipe);
                            if (!Materials.isEmpty(blockData.getBlockType())) {
                                setBlockData(
                                        targetBlock.getX() + currX,
                                        clampY(targetBlock.getY() + currY),
                                        targetBlock.getZ() + currZ,
                                        blockData
                                );
                            }
                            currZ++;
                            if (currZ == this.x - this.xRef) {
                                currZ = -this.xRef;
                                currX--;
                                if (currX == -this.z + this.zRef) {
                                    currX = this.zRef;
                                    currY++;
                                }
                            }
                        }
                    }
                }
                in.close();
            } catch (IOException | MaxChangedBlocksException exception) {
                messenger.sendMessage(ChatFormatting.RED + "Something went wrong.");
                exception.printStackTrace();
            }
        } else {
            messenger.sendMessage(ChatFormatting.RED + "You need to type a stencil name / your specified stencil does not exist.");
        }
    }

    private void stencilPaste90(Snipe snipe) {
        SnipeMessenger messenger = snipe.createMessenger();
        if (this.filename.matches("NoFileLoaded")) {
            messenger.sendMessage(ChatFormatting.RED + "You did not specify a filename for the list.  This is required.");
            return;
        }
        String stencilName = this.readRandomStencil();
        File file = new File(TerraSniper.TERRASNIPER_CONFIG_FOLDER.getPath(), "/stencils/" + stencilName + ".vstencil");
        if (file.exists()) {
            try {
                DataInputStream in = new DataInputStream(new BufferedInputStream(new FileInputStream(file)));
                this.x = in.readShort();
                this.z = in.readShort();
                this.y = in.readShort();
                this.xRef = in.readShort();
                this.zRef = in.readShort();
                this.yRef = in.readShort();
                int numRuns = in.readInt();
                // Something here that checks ranks using sanker'world thingie he added to Sniper and boots you out with error message if too big.
                int volume = this.x * this.y * this.z;
                messenger.sendMessage(ChatFormatting.AQUA + this.filename + " pasted.  Volume is " + volume + " blocks.");
                int currX = -this.zRef; // so if your ref point is +5 x, you want to start pasting -5 blocks from the clicked point (the reference) to get the
                // corner, for example.
                int currZ = this.xRef;
                int currY = -this.yRef;
                BlockState blockData;
                BlockVector3 targetBlock = this.getTargetBlock();
                if (this.pasteOption == 0) {
                    for (int i = 1; i < numRuns + 1; i++) {
                        if (in.readBoolean()) {
                            int numLoops = in.readByte() + 128;
                            blockData = readBlockData(in, snipe);
                            for (int j = 0; j < numLoops; j++) {
                                setBlockData(
                                        targetBlock.getX() + currX,
                                        clampY(targetBlock.getY() + currY),
                                        targetBlock.getZ() + currZ,
                                        blockData
                                );
                                currZ--;
                                if (currZ == -this.x + this.xRef) {
                                    currZ = this.xRef;
                                    currX++;
                                    if (currX == this.z - this.zRef) {
                                        currX = -this.zRef;
                                        currY++;
                                    }
                                }
                            }
                        } else {
                            setBlockData(
                                    targetBlock.getX() + currX,
                                    clampY(targetBlock.getY() + currY),
                                    targetBlock.getZ() + currZ,
                                    readBlockData(in, snipe)
                            );
                            currZ--;
                            if (currZ == -this.x + this.xRef) {
                                currZ = this.xRef;
                                currX++;
                                if (currX == this.z - this.zRef) {
                                    currX = -this.zRef;
                                    currY++;
                                }
                            }
                        }
                    }
                } else if (this.pasteOption == 1) {
                    for (int i = 1; i < numRuns + 1; i++) {
                        if (in.readBoolean()) {
                            int numLoops = in.readByte() + 128;
                            blockData = readBlockData(in, snipe);
                            for (int j = 0; j < numLoops; j++) {
                                if (!Materials.isEmpty(blockData.getBlockType()) && this.clampY(
                                        targetBlock.getX() + currX,
                                        targetBlock.getY() + currY,
                                        targetBlock.getZ() + currZ
                                ).getBlockType().getMaterial().isAir()) {
                                    setBlockData(
                                            targetBlock.getX() + currX,
                                            clampY(targetBlock.getY() + currY),
                                            targetBlock.getZ() + currZ,
                                            blockData
                                    );
                                }
                                currZ--;
                                if (currZ == -this.x + this.xRef) {
                                    currZ = this.xRef;
                                    currX++;
                                    if (currX == this.z - this.zRef) {
                                        currX = -this.zRef;
                                        currY++;
                                    }
                                }
                            }
                        } else {
                            blockData = readBlockData(in, snipe);
                            if (!Materials.isEmpty(blockData.getBlockType()) && this.clampY(
                                    targetBlock.getX() + currX,
                                    targetBlock.getY() + currY,
                                    targetBlock.getZ() + currZ
                            ).getBlockType().getMaterial().isAir()) {
                                setBlockData(
                                        targetBlock.getX() + currX,
                                        clampY(targetBlock.getY() + currY),
                                        targetBlock.getZ() + currZ,
                                        blockData
                                );
                            }
                            currZ--;
                            if (currZ == -this.x + this.xRef) {
                                currZ = this.xRef;
                                currX++;
                                if (currX == this.z - this.zRef) {
                                    currX = -this.zRef;
                                    currY++;
                                }
                            }
                        }
                    }
                } else { // replace
                    for (int i = 1; i < numRuns + 1; i++) {
                        if (in.readBoolean()) {
                            int numLoops = in.readByte() + 128;
                            blockData = readBlockData(in, snipe);
                            for (int j = 0; j < (numLoops); j++) {
                                if (!Materials.isEmpty(blockData.getBlockType())) {
                                    setBlockData(
                                            targetBlock.getX() + currX,
                                            clampY(targetBlock.getY() + currY),
                                            targetBlock.getZ() + currZ,
                                            blockData
                                    );
                                }
                                currZ--;
                                if (currZ == -this.x + this.xRef) {
                                    currZ = this.xRef;
                                    currX++;
                                    if (currX == this.z - this.zRef) {
                                        currX = -this.zRef;
                                        currY++;
                                    }
                                }
                            }
                        } else {
                            blockData = readBlockData(in, snipe);
                            if (!Materials.isEmpty(blockData.getBlockType())) {
                                setBlockData(
                                        targetBlock.getX() + currX,
                                        clampY(targetBlock.getY() + currY),
                                        targetBlock.getZ() + currZ,
                                        blockData
                                );
                            }
                            currZ--;
                            if (currZ == -this.x + this.xRef) {
                                currZ = this.xRef;
                                currX++;
                                if (currX == this.z - this.zRef) {
                                    currX = -this.zRef;
                                    currY++;
                                }
                            }
                        }
                    }
                }
                in.close();
            } catch (IOException | MaxChangedBlocksException exception) {
                messenger.sendMessage(ChatFormatting.RED + "Something went wrong.");
                exception.printStackTrace();
            }
        } else {
            messenger.sendMessage(ChatFormatting.RED + "You need to type a stencil name / your specified stencil does not exist.");
        }
    }

    private BlockState readBlockData(DataInputStream in, Snipe snipe) throws IOException {
        String blockDataString = in.readUTF();
        try {
            ParserContext parserContext = new ParserContext();
            parserContext.setActor(ForgeAdapter.adaptPlayer((ServerPlayer) snipe.getSniper().getPlayer()));
            parserContext.setExtent(getEditSession());
            return WorldEdit.getInstance().getBlockFactory().parseFromInput(blockDataString, parserContext).toImmutableState();
        } catch (InputParseException e) {
            e.printStackTrace();
        }
        return BlockTypes.AIR.getDefaultState();
    }

    private void stencilPasteRotation(Snipe snipe) {
        // just randomly chooses a rotation and then calls stencilPaste.
        this.readStencilList();
        double random = Math.random();
        if (random < 0.26) {
            this.stencilPaste(snipe);
        } else if (random < 0.51) {
            this.stencilPaste90(snipe);
        } else if (random < 0.76) {
            this.stencilPaste180(snipe);
        } else {
            this.stencilPaste270(snipe);
        }
    }

    @Override
    public void sendInfo(Snipe snipe) {
        SnipeMessenger messenger = snipe.createMessenger();
        messenger.sendBrushNameMessage();
        messenger.sendMessage("File loaded: " + this.filename);
    }

}
