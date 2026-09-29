/*
 * Decompiled with CFR 0.152.
 */
package night.utils.system;

import java.awt.Image;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.File;
import java.util.Collections;

public class ImageTransferable
implements Transferable {
    private final Image image;
    private final File file;
    private final boolean includeFile;

    public ImageTransferable(Image image, File file, boolean includeFile) {
        this.image = image;
        this.file = file;
        this.includeFile = includeFile;
    }

    @Override
    public DataFlavor[] getTransferDataFlavors() {
        if (this.includeFile && this.file != null) {
            return new DataFlavor[]{DataFlavor.imageFlavor, DataFlavor.javaFileListFlavor};
        }
        return new DataFlavor[]{DataFlavor.imageFlavor};
    }

    @Override
    public boolean isDataFlavorSupported(DataFlavor flavor) {
        if (DataFlavor.imageFlavor.equals(flavor)) {
            return this.image != null;
        }
        return this.includeFile && this.file != null && DataFlavor.javaFileListFlavor.equals(flavor);
    }

    @Override
    public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException {
        if (DataFlavor.imageFlavor.equals(flavor) && this.image != null) {
            return this.image;
        }
        if (this.includeFile && this.file != null && DataFlavor.javaFileListFlavor.equals(flavor)) {
            return Collections.singletonList(this.file);
        }
        throw new UnsupportedFlavorException(flavor);
    }
}

