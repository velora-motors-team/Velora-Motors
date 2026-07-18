package com.velora.repository;

import com.velora.vehicle.Vehicle;
import com.velora.vehicle.VehicleStatus;
import com.velora.vehicle.VehicleType;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class VehicleRepositoryTest {

    private Path vehicleFile;
    private boolean originalFileExisted;
    private byte[] originalFileContent;

    @BeforeEach
    public void setUp() throws Exception {
        vehicleFile = getVehicleFile();

        originalFileExisted = Files.exists(vehicleFile);
        originalFileContent = originalFileExisted
                ? Files.readAllBytes(vehicleFile)
                : null;
    }

    @AfterEach
    public void tearDown() throws Exception {
        if (vehicleFile == null) {
            return;
        }

        Files.deleteIfExists(vehicleFile);

        if (originalFileExisted) {
            Files.createDirectories(vehicleFile.getParent());
            Files.write(vehicleFile, originalFileContent);
        }
    }

    @Test
    public void testFindAllReturnsSharedList() {
        VehicleRepository firstRepository = new VehicleRepository();
        VehicleRepository secondRepository = new VehicleRepository();

        List<Vehicle> first = firstRepository.findAll();
        List<Vehicle> second = secondRepository.findAll();

        assertNotNull(first);
        assertSame(first, second);
    }

    @Test
    public void testFindAllContainsVehicles() {
        VehicleRepository repository = new VehicleRepository();

        List<Vehicle> vehicles = repository.findAll();

        assertNotNull(vehicles);
        assertFalse(vehicles.isEmpty());
    }

    @Test
    public void testSaveAllWritesVehiclesFile() throws Exception {
        VehicleRepository repository = new VehicleRepository();

        repository.saveAll();

        assertTrue(Files.exists(vehicleFile));

        List<String> lines = Files.readAllLines(
                vehicleFile,
                StandardCharsets.UTF_8
        );

        assertEquals(repository.findAll().size(), lines.size());

        if (!lines.isEmpty()) {
            assertTrue(lines.get(0).startsWith("VEHICLE\t"));
        }
    }

    @Test
    public void testLoadVehiclesReturnsThirtyEightDefaultVehiclesWhenFileMissing()
            throws Exception {

        Files.deleteIfExists(vehicleFile);

        List<Vehicle> vehicles = invokeLoadVehicles();

        assertEquals(38, vehicles.size());

        Vehicle first = vehicles.get(0);

        assertEquals("VM-0001", first.getId());
        assertEquals("BMW", first.getBrand());
        assertEquals("XM Label", first.getModel());
        assertEquals(VehicleType.SUV, first.getType());
        assertEquals(VehicleStatus.AVAILABLE, first.getStatus());
        assertEquals(520.0, first.getDailyPrice(), 0.0001);
    }

    @Test
    public void testDefaultFleetContainsMotorcyclesTrucksAndElectricBike()
            throws Exception {
        Files.deleteIfExists(vehicleFile);

        List<Vehicle> vehicles = invokeLoadVehicles();

        assertEquals(
                4,
                vehicles.stream()
                        .filter(vehicle -> vehicle.getType() == VehicleType.MOTORCYCLE)
                        .count()
        );
        assertEquals(
                3,
                vehicles.stream()
                        .filter(vehicle -> vehicle.getType() == VehicleType.TRUCK)
                        .count()
        );

        Vehicle ce04 = vehicles.stream()
                .filter(vehicle -> vehicle.getId().equals("VM-0035"))
                .findFirst()
                .orElseThrow();
        assertEquals(VehicleType.ELECTRIC_BIKE, ce04.getType());
        assertEquals(92, ce04.getBatteryLevel());
    }

    @Test
    public void testDefaultVehiclesContainElectricVehicleWithBattery()
            throws Exception {

        Files.deleteIfExists(vehicleFile);

        List<Vehicle> vehicles = invokeLoadVehicles();

        Vehicle electric = vehicles.stream()
                .filter(vehicle -> vehicle.getId().equals("VM-0011"))
                .findFirst()
                .orElseThrow();

        assertEquals(
                VehicleType.ELECTRIC_VEHICLE,
                electric.getType()
        );

        assertEquals(92, electric.getBatteryLevel());
    }

    @Test
    public void testLoadVehiclesUsesSavedVehiclesWhenFileHasData()
            throws Exception {

        writeVehicleFile(
                createLine(
                        "CUSTOM-1",
                        "BMW",
                        "Custom Model",
                        VehicleType.CAR,
                        VehicleStatus.RENTED,
                        333.5,
                        null
                )
        );

        List<Vehicle> vehicles = invokeLoadVehicles();

        assertEquals(1, vehicles.size());
        assertEquals("CUSTOM-1", vehicles.get(0).getId());
        assertEquals("Custom Model", vehicles.get(0).getModel());
    }

    @Test
    public void testLoadSavedVehiclesReturnsEmptyWhenFileMissing()
            throws Exception {

        Files.deleteIfExists(vehicleFile);

        List<Vehicle> vehicles = invokeLoadSavedVehicles();

        assertTrue(vehicles.isEmpty());
    }

    @Test
    public void testLoadSavedVehiclesReadsVehicleWithoutBattery()
            throws Exception {

        writeVehicleFile(
                createLine(
                        "V001",
                        "BMW",
                        "M3",
                        VehicleType.CAR,
                        VehicleStatus.AVAILABLE,
                        350.0,
                        null
                )
        );

        List<Vehicle> vehicles = invokeLoadSavedVehicles();

        assertEquals(1, vehicles.size());

        Vehicle vehicle = vehicles.get(0);

        assertEquals("V001", vehicle.getId());
        assertEquals("BMW", vehicle.getBrand());
        assertEquals("M3", vehicle.getModel());
        assertEquals(VehicleType.CAR, vehicle.getType());
        assertEquals(VehicleStatus.AVAILABLE, vehicle.getStatus());
        assertEquals(350.0, vehicle.getDailyPrice(), 0.0001);
        assertNull(vehicle.getBatteryLevel());
    }

    @Test
    public void testLoadSavedVehiclesReadsVehicleWithBattery()
            throws Exception {

        writeVehicleFile(
                createLine(
                        "EV001",
                        "BMW",
                        "iX",
                        VehicleType.ELECTRIC_VEHICLE,
                        VehicleStatus.AVAILABLE,
                        410.0,
                        92
                )
        );

        List<Vehicle> vehicles = invokeLoadSavedVehicles();

        assertEquals(1, vehicles.size());

        Vehicle vehicle = vehicles.get(0);

        assertEquals(
                VehicleType.ELECTRIC_VEHICLE,
                vehicle.getType()
        );

        assertEquals(92, vehicle.getBatteryLevel());
    }

    @Test
    public void testLoadSavedVehiclesSkipsBlankAndMalformedLines()
            throws Exception {

        String valid = createLine(
                "V001",
                "BMW",
                "M3",
                VehicleType.CAR,
                VehicleStatus.AVAILABLE,
                350.0,
                null
        );

        Files.createDirectories(vehicleFile.getParent());

        Files.writeString(
                vehicleFile,
                System.lineSeparator()
                        + "WRONG\tVALUE"
                        + System.lineSeparator()
                        + "VEHICLE\ttoo\tfew"
                        + System.lineSeparator()
                        + valid
                        + System.lineSeparator(),
                StandardCharsets.UTF_8
        );

        List<Vehicle> vehicles = invokeLoadSavedVehicles();

        assertEquals(1, vehicles.size());
        assertEquals("V001", vehicles.get(0).getId());
    }

    @Test
    public void testLoadSavedVehiclesClearsListOnInvalidEnum()
            throws Exception {

        String valid = createLine(
                "V001",
                "BMW",
                "M3",
                VehicleType.CAR,
                VehicleStatus.AVAILABLE,
                350.0,
                null
        );

        String invalid =
                "VEHICLE\t"
                        + encode("V002") + "\t"
                        + encode("BMW") + "\t"
                        + encode("Broken") + "\t"
                        + "INVALID_TYPE\t"
                        + "AVAILABLE\t"
                        + "200.0\t";

        Files.createDirectories(vehicleFile.getParent());

        Files.writeString(
                vehicleFile,
                valid + System.lineSeparator()
                        + invalid + System.lineSeparator(),
                StandardCharsets.UTF_8
        );

        List<Vehicle> vehicles = invokeLoadSavedVehicles();

        assertTrue(vehicles.isEmpty());
    }

    @Test
    public void testSaveVehiclesAndLoadSavedVehiclesRoundTrip()
            throws Exception {

        Vehicle normalVehicle = Vehicle.create(
                "V001",
                "BMW",
                "M3 Competition",
                VehicleType.CAR,
                VehicleStatus.RENTED,
                385.0
        );

        Vehicle electricVehicle = Vehicle.create(
                "EV001",
                "BMW",
                "i7 M70",
                VehicleType.ELECTRIC_VEHICLE,
                VehicleStatus.AVAILABLE,
                480.0,
                78
        );

        invokeSaveVehicles(
                List.of(normalVehicle, electricVehicle)
        );

        List<Vehicle> loaded = invokeLoadSavedVehicles();

        assertEquals(2, loaded.size());

        assertEquals("V001", loaded.get(0).getId());
        assertNull(loaded.get(0).getBatteryLevel());

        assertEquals("EV001", loaded.get(1).getId());
        assertEquals(78, loaded.get(1).getBatteryLevel());
    }

    @Test
    public void testSaveVehiclesPreservesSpecialCharacters()
            throws Exception {

        Vehicle vehicle = Vehicle.create(
                "V-特殊-1",
                "BMW & Co",
                "M3 / Competition + Special",
                VehicleType.CAR,
                VehicleStatus.AVAILABLE,
                400.0
        );

        invokeSaveVehicles(List.of(vehicle));

        List<Vehicle> loaded = invokeLoadSavedVehicles();

        assertEquals(1, loaded.size());
        assertEquals("V-特殊-1", loaded.get(0).getId());
        assertEquals("BMW & Co", loaded.get(0).getBrand());
        assertEquals(
                "M3 / Competition + Special",
                loaded.get(0).getModel()
        );
    }

    @Test
    public void testPrivateEncodeDecodeHelpers() throws Exception {
        String original = "BMW M3 / Special + عربية";

        String encoded = invokeStringMethod(
                "encode",
                original
        );

        String decoded = invokeStringMethod(
                "decode",
                encoded
        );

        assertNotEquals(original, encoded);
        assertEquals(original, decoded);
    }

    @Test
    public void testPrivateEncodeDecodeNullValues() throws Exception {
        String encoded = invokeStringMethod(
                "encode",
                null
        );

        String decoded = invokeStringMethod(
                "decode",
                null
        );

        assertEquals("", decoded);

        String decodedEncodedEmpty = invokeStringMethod(
                "decode",
                encoded
        );

        assertEquals("", decodedEncodedEmpty);
    }

    @Test
    public void testPrivateParseHelpers() throws Exception {
        assertEquals(78, invokeParseInt("78"));
        assertEquals(
                410.75,
                invokeParseDouble("410.75"),
                0.0001
        );
    }

    @Test
    public void testLoadSavedVehiclesSkipsWrongRecordType()
            throws Exception {
        writeVehicleFile(
                "NOT_A_VEHICLE\t1\t2\t3\t4\t5\t6"
        );

        assertTrue(invokeLoadSavedVehicles().isEmpty());
    }

    @Test
    public void testLoadSavedVehicleWithSevenColumnsHasNoBattery()
            throws Exception {
        writeVehicleFile(String.join(
                "\t",
                "VEHICLE",
                encode("V-7"),
                encode("BMW"),
                encode("M3"),
                VehicleType.CAR.name(),
                VehicleStatus.AVAILABLE.name(),
                "300.0"
        ));

        List<Vehicle> loaded = invokeLoadSavedVehicles();

        assertEquals(1, loaded.size());
        assertNull(loaded.get(0).getBatteryLevel());
    }

    @Test
    public void testSaveVehiclesIgnoresIoFailure() throws Exception {
        Files.deleteIfExists(vehicleFile);
        Files.createDirectories(vehicleFile);

        assertDoesNotThrow(() -> invokeSaveVehicles(List.of(
                Vehicle.create(
                        "V-1", "BMW", "M3", VehicleType.CAR,
                        VehicleStatus.AVAILABLE, 300.0
                )
        )));
    }

    private Path getVehicleFile() throws Exception {
        Field field = VehicleRepository.class.getDeclaredField(
                "VEHICLE_FILE"
        );

        field.setAccessible(true);

        return (Path) field.get(null);
    }

    @SuppressWarnings("unchecked")
    private List<Vehicle> invokeLoadVehicles() throws Exception {
        Method method = VehicleRepository.class.getDeclaredMethod(
                "loadVehicles"
        );

        method.setAccessible(true);

        return (List<Vehicle>) method.invoke(null);
    }

    @SuppressWarnings("unchecked")
    private List<Vehicle> invokeLoadSavedVehicles() throws Exception {
        Method method = VehicleRepository.class.getDeclaredMethod(
                "loadSavedVehicles"
        );

        method.setAccessible(true);

        return (List<Vehicle>) method.invoke(null);
    }

    private void invokeSaveVehicles(List<Vehicle> vehicles)
            throws Exception {

        Method method = VehicleRepository.class.getDeclaredMethod(
                "saveVehicles",
                List.class
        );

        method.setAccessible(true);
        method.invoke(null, vehicles);
    }

    private String invokeStringMethod(
            String methodName,
            String value
    ) throws Exception {

        Method method = VehicleRepository.class.getDeclaredMethod(
                methodName,
                String.class
        );

        method.setAccessible(true);

        return (String) method.invoke(null, value);
    }

    private int invokeParseInt(String value) throws Exception {
        Method method = VehicleRepository.class.getDeclaredMethod(
                "parseInt",
                String.class
        );

        method.setAccessible(true);

        return (int) method.invoke(null, value);
    }

    private double invokeParseDouble(String value) throws Exception {
        Method method = VehicleRepository.class.getDeclaredMethod(
                "parseDouble",
                String.class
        );

        method.setAccessible(true);

        return (double) method.invoke(null, value);
    }

    private void writeVehicleFile(String... lines)
            throws Exception {

        Files.createDirectories(vehicleFile.getParent());

        Files.write(
                vehicleFile,
                List.of(lines),
                StandardCharsets.UTF_8
        );
    }

    private String createLine(
            String id,
            String brand,
            String model,
            VehicleType type,
            VehicleStatus status,
            double price,
            Integer battery
    ) {
        return String.join(
                "\t",
                "VEHICLE",
                encode(id),
                encode(brand),
                encode(model),
                type.name(),
                status.name(),
                String.valueOf(price),
                battery == null ? "" : String.valueOf(battery)
        );
    }

    private String encode(String value) {
        return Base64.getEncoder().encodeToString(
                value.getBytes(StandardCharsets.UTF_8)
        );
    }
}
