package com.velora.ui;

import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.net.URL;

import static org.junit.jupiter.api.Assertions.*;

public class VehicleImageAssetsTest {

    @Test
    void motorcycleAndTruckCatalogImagesExistAtFullResolution()
            throws Exception {
        for (int number = 31; number <= 38; number++) {
            String id = "VM-" + String.format("%04d", number);
            String path = "/images/vehicles/" + id + ".jpg";
            URL resource = VehicleImageAssetsTest.class.getResource(path);

            assertNotNull(resource, () -> "Missing vehicle image: " + path);

            BufferedImage image = ImageIO.read(resource);
            assertNotNull(image, () -> "Unreadable vehicle image: " + path);
            assertEquals(1536, image.getWidth(), path);
            assertEquals(1024, image.getHeight(), path);
        }
    }
}
