package com.velora.ui;

import org.junit.jupiter.api.Test;

import javax.swing.SwingUtilities;
import java.awt.image.BufferedImage;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

public class CustomerFooterTest {

    @Test
    void buildsAndPaintsFooter() throws Exception {
        AtomicReference<CustomerFooter> result = new AtomicReference<>();

        SwingUtilities.invokeAndWait(() -> {
            CustomerFooter footer = new CustomerFooter();
            footer.setSize(1200, 58);
            footer.paint(new BufferedImage(
                    1200, 58, BufferedImage.TYPE_INT_ARGB
            ).getGraphics());
            result.set(footer);
        });

        assertNotNull(result.get());
        assertEquals(3, result.get().getComponentCount());
        assertEquals(58, result.get().getPreferredSize().height);
    }
}
