package cl.padel.fitprodiag;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.graphics.Color;
import android.graphics.Typeface;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Queue;
import java.util.UUID;

/**
 * Diagnóstico BLE para el smartwatch FitPro en Nexus 10 / Android 10.
 * Sin dependencias ni interacción con el marcador de pádel.
 * El número filtrado es un conteo técnico; NO equivale a pulsaciones físicas verificadas.
 */
@SuppressWarnings("deprecation")
public class MainActivity extends Activity {
    private static final int PERMISSION_LOCATION = 101;
    private static final UUID UART_SERVICE = UUID.fromString("6e400001-b5a3-f393-e0a9-e50e24dcca9d");
    private static final UUID UART_WRITE = UUID.fromString("6e400002-b5a3-f393-e0a9-e50e24dcca9d");
    private static final UUID UART_NOTIFY = UUID.fromString("6e400003-b5a3-f393-e0a9-e50e24dcca9d");
    private static final UUID CCCD = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");

    private static final byte[] CAMERA_ON = hex("CD 00 06 12 01 0C 00 01 01");
    private static final byte[] CAMERA_OFF = hex("CD 00 06 12 01 0C 00 01 00");
    private static final byte[] CAMERA_EVENT = hex("CD 00 05 1C 01 02 00 00");
    private static final byte[] CAMERA_ACK = hex("DC 00 05 1C 01 00 08 01");
    private static final int[] DEBOUNCE_MS = {500, 1000, 2000, 3500, 5000};

    private final Handler ui = new Handler(Looper.getMainLooper());
    private final Map<String, BluetoothDevice> found = new LinkedHashMap<>();
    private final Queue<byte[]> outgoing = new ArrayDeque<>();
    private final StringBuilder fullLog = new StringBuilder();
    private final SimpleDateFormat clock = new SimpleDateFormat("HH:mm:ss.SSS", Locale.getDefault());

    private BluetoothAdapter adapter;
    private BluetoothLeScanner scanner;
    private BluetoothGatt gatt;
    private BluetoothGattCharacteristic writeCharacteristic;
    private boolean scanning = false;
    private boolean ready = false;
    private boolean cameraRequested = false;
    private boolean busy = false;
    private int sessionId = 0;
    private long lastAccepted = Long.MIN_VALUE / 2;
    private int notifications = 0, cameraEvents = 0, accepted = 0, others = 0, beforeMode = 0;

    private TextView status, counts, logText;
    private Spinner debounceChoice;
    private ScrollView logScroll;
    private Button scanButton, connectButton, enableButton, disableButton, disconnectButton;

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        BluetoothManager manager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        adapter = manager == null ? null : manager.getAdapter();
        buildUi();
        log("Aplicación iniciada. Solo diagnóstico, no suma puntos.");
        if (adapter == null) log("ERROR: no hay adaptador Bluetooth LE.");
        else if (!adapter.isEnabled()) log("Activa Bluetooth en Ajustes y vuelve a Buscar.");
        requestLocationIfNeeded();
        updateUi();
    }

    private void buildUi() {
        int white = Color.WHITE, muted = Color.rgb(172, 190, 205);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(14), dp(16), dp(14), dp(12));
        root.setBackgroundColor(Color.rgb(11, 17, 26));

        TextView heading = text("FitPro · Diagnóstico BLE", 23, white, true);
        root.addView(heading);
        TextView warn = text("Receptor independiente. Los eventos filtrados NO son puntos comprobados.", 13, muted, false);
        warn.setPadding(0, dp(7), 0, dp(10));
        root.addView(warn);

        status = text("Estado: desconectado", 17, Color.rgb(103, 222, 163), true);
        root.addView(status);
        counts = text("", 17, white, true);
        counts.setPadding(0, dp(10), 0, dp(8));
        root.addView(counts);

        LinearLayout controlRow = row();
        scanButton = button("Buscar relojes", controlRow, () -> startSearch());
        connectButton = button("Elegir reloj", controlRow, () -> selectDevice());
        root.addView(controlRow);

        LinearLayout cameraRow = row();
        enableButton = button("Activar cámara", cameraRow, () -> sendCamera(true));
        disableButton = button("Desactivar", cameraRow, () -> sendCamera(false));
        disconnectButton = button("Salir", cameraRow, () -> disconnect());
        root.addView(cameraRow);

        LinearLayout options = row();
        TextView debounceLabel = text("Filtro (ms):", 14, white, false);
        options.addView(debounceLabel);
        debounceChoice = new Spinner(this);
        String[] opts = {"500", "1000", "2000", "3500", "5000"};
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, opts);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        debounceChoice.setAdapter(spinnerAdapter);
        debounceChoice.setSelection(3);
        options.addView(debounceChoice, new LinearLayout.LayoutParams(dp(110), dp(48)));
        root.addView(options);

        LinearLayout utils = row();
        button("Reiniciar", utils, () -> resetCounters());
        button("Copiar registro", utils, () -> copyLog());
        root.addView(utils);

        TextView eventsLabel = text("Registro (RX/TX, horas y datos HEX):", 14, muted, true);
        eventsLabel.setPadding(0, dp(12), 0, dp(5));
        root.addView(eventsLabel);
        logScroll = new ScrollView(this);
        logScroll.setFillViewport(true);
        logText = text("", 12, Color.rgb(208, 225, 235), false);
        logText.setTypeface(Typeface.MONOSPACE);
        logText.setTextIsSelectable(true);
        logText.setPadding(dp(7), dp(6), dp(7), dp(6));
        logScroll.addView(logText);
        root.addView(logScroll, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f));
        setContentView(root);
    }

    private LinearLayout row() {
        LinearLayout l = new LinearLayout(this);
        l.setOrientation(LinearLayout.HORIZONTAL);
        l.setGravity(Gravity.CENTER_VERTICAL);
        return l;
    }

    private Button button(String text, LinearLayout parent, Runnable click) {
        Button b = new Button(this);
        b.setText(text);
        b.setTextSize(11);
        b.setAllCaps(false);
        b.setOnClickListener(v -> click.run());
        parent.addView(b, new LinearLayout.LayoutParams(0, dp(48), 1f));
        return b;
    }

    private TextView text(String content, int sp, int color, boolean bold) {
        TextView t = new TextView(this);
        t.setText(content);
        t.setTextSize(sp);
        t.setTextColor(color);
        if (bold) t.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        return t;
    }

    private int dp(int pixels) {
        return (int) (getResources().getDisplayMetrics().density * pixels + 0.5f);
    }

    private void requestLocationIfNeeded() {
        if (Build.VERSION.SDK_INT >= 23 &&
                checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_LOCATION);
        }
    }

    @Override public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] results) {
        super.onRequestPermissionsResult(requestCode, permissions, results);
        if (requestCode == PERMISSION_LOCATION && (results.length == 0 ||
                results[0] != PackageManager.PERMISSION_GRANTED)) {
            log("Necesitas autorizar Ubicación para buscar relojes BLE en Android 10.");
        }
    }

    private void startSearch() {
        if (adapter == null || !adapter.isEnabled()) {
            log("ERROR: Bluetooth desactivado.");
            return;
        }
        if (Build.VERSION.SDK_INT >= 23 &&
                checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {
            requestLocationIfNeeded();
            return;
        }
        LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (lm != null && !lm.isProviderEnabled(LocationManager.GPS_PROVIDER)
                && !lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            log("AVISO: activa Ubicación en Ajustes para que Android 10 permita escanear BLE.");
        }
        if (scanning) stopSearch();
        found.clear();
        try {
            for (BluetoothDevice d : adapter.getBondedDevices()) {
                found.put(d.getAddress(), d);
            }
            scanner = adapter.getBluetoothLeScanner();
            if (scanner == null) { log("ERROR: BLE Scanner no disponible."); return; }
            scanner.startScan(callback);
            scanning = true;
            log("Escaneando 12 segundos. Dispositivos conocidos: " + found.size());
            updateUi();
            ui.postDelayed(() -> {
                if (scanning) { stopSearch(); selectDevice(); }
            }, 12000);
        } catch (SecurityException | IllegalStateException e) {
            log("ERROR en escaneo: " + e.getMessage());
        }
    }

    private final ScanCallback callback = new ScanCallback() {
        @Override public void onScanResult(int callbackType, ScanResult result) {
            if (result == null || result.getDevice() == null) return;
            ui.post(() -> {
                BluetoothDevice d = result.getDevice();
                if (!found.containsKey(d.getAddress())) {
                    found.put(d.getAddress(), d);
                    log("Encontrado: " + deviceLabel(d));
                }
            });
        }
        @Override public void onScanFailed(int errorCode) {
            ui.post(() -> { scanning = false; log("Error de búsqueda BLE: " + errorCode); updateUi(); });
        }
    };

    private void stopSearch() {
        if (scanning && scanner != null) {
            try { scanner.stopScan(callback); } catch (Exception ignored) { }
        }
        scanning = false;
        updateUi();
    }

    private String deviceLabel(BluetoothDevice d) {
        String name = d.getName();
        return (name == null || name.isEmpty() ? "Sin nombre" : name) + "  [" + d.getAddress() + "]";
    }

    private void selectDevice() {
        if (found.isEmpty()) {
            log("No hay dispositivos. Pulsa Buscar relojes primero.");
            return;
        }
        List<BluetoothDevice> devs = new ArrayList<>(found.values());
        String[] titles = new String[devs.size()];
        for (int i = 0; i < devs.size(); i++) titles[i] = deviceLabel(devs.get(i));
        new AlertDialog.Builder(this).setTitle("Conectar reloj BLE")
                .setItems(titles, (dlg, index) -> connect(devs.get(index)))
                .setNegativeButton("Cancelar", null).show();
    }

    private void connect(BluetoothDevice device) {
        stopSearch();
        disconnect();
        int currentSession = ++sessionId;
        log("Conectando: " + deviceLabel(device));
        status.setText("Estado: conectando...");
        try {
            gatt = device.connectGatt(this, false, new Callbacks(currentSession), BluetoothDevice.TRANSPORT_LE);
            if (gatt == null) log("ERROR: connectGatt devolvió null.");
        } catch (Exception e) {
            log("ERROR de conexión: " + e.getMessage());
        }
    }

    private final class Callbacks extends BluetoothGattCallback {
        private final int owner;
        Callbacks(int session) { owner = session; }
        private boolean current() { return owner == sessionId; }

        @Override public void onConnectionStateChange(BluetoothGatt bg, int code, int newState) {
            if (!current()) return;
            ui.post(() -> {
                if (newState == BluetoothGatt.STATE_CONNECTED && code == BluetoothGatt.GATT_SUCCESS) {
                    status.setText("Estado: conectado, descubriendo servicios...");
                    log("Conectado. Descubriendo GATT...");
                    if (!bg.discoverServices()) log("ERROR: discoverServices falló.");
                } else {
                    ready = false;
                    writeCharacteristic = null;
                    status.setText("Estado: desconectado (código " + code + ")");
                    log("Desconexión GATT, estado=" + newState + ", código=" + code);
                    updateUi();
                }
            });
        }

        @Override public void onServicesDiscovered(BluetoothGatt bg, int code) {
            if (!current()) return;
            ui.post(() -> {
                if (code != BluetoothGatt.GATT_SUCCESS) {
                    log("ERROR descubriendo servicios: " + code);
                    return;
                }
                BluetoothGattService service = bg.getService(UART_SERVICE);
                if (service == null) {
                    log("No aparece el servicio Nordic UART " + UART_SERVICE);
                    for (BluetoothGattService s : bg.getServices()) log("Servicio: " + s.getUuid());
                    return;
                }
                BluetoothGattCharacteristic send = service.getCharacteristic(UART_WRITE);
                BluetoothGattCharacteristic receive = service.getCharacteristic(UART_NOTIFY);
                if (send == null || receive == null) {
                    log("ERROR: falta una característica de escritura/notificaciones UART.");
                    return;
                }
                writeCharacteristic = send;
                writeCharacteristic.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE);
                if (!bg.setCharacteristicNotification(receive, true)) {
                    log("ERROR: setCharacteristicNotification devolvió false.");
                    return;
                }
                BluetoothGattDescriptor desc = receive.getDescriptor(CCCD);
                if (desc == null) {
                    log("ERROR: CCCD no disponible, no se recibirán notificaciones.");
                    return;
                }
                desc.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                if (!bg.writeDescriptor(desc)) {
                    log("ERROR: no se pudo habilitar CCCD.");
                    return;
                }
                log("UART localizado, habilitando notificaciones...");
            });
        }

        @Override public void onDescriptorWrite(BluetoothGatt bg, BluetoothGattDescriptor desc, int code) {
            if (!current()) return;
            ui.post(() -> {
                if (code != BluetoothGatt.GATT_SUCCESS) {
                    log("ERROR habilitando notificaciones: " + code);
                    return;
                }
                ready = true;
                status.setText("Estado: conectado. Solicitando modo cámara...");
                log("Notificaciones habilitadas. Solicitando cámara FitPro...");
                sendCamera(true);
                updateUi();
            });
        }

        @Override public void onCharacteristicChanged(BluetoothGatt bg, BluetoothGattCharacteristic c) {
            if (!current() || !UART_NOTIFY.equals(c.getUuid())) return;
            byte[] input = c.getValue();
            if (input == null) return;
            byte[] copy = input.clone();
            ui.post(() -> processNotification(copy));
        }

        @Override public void onCharacteristicWrite(BluetoothGatt bg, BluetoothGattCharacteristic c, int code) {
            if (!current()) return;
            if (code != BluetoothGatt.GATT_SUCCESS)
                ui.post(() -> log("ERROR escribiendo GATT: " + code));
        }
    }

    private void processNotification(byte[] message) {
        notifications++;
        boolean camera = same(message, CAMERA_EVENT);
        log("RX " + asHex(message) + (camera ? "  [CAMERA]" : ""));
        if (camera) {
            cameraEvents++;
            enqueueWrite(CAMERA_ACK); // Confirmar TODOS los eventos, incluso los descartados.
            long now = SystemClock.elapsedRealtime();
            int debounce = DEBOUNCE_MS[debounceChoice.getSelectedItemPosition()];
            if (!cameraRequested) {
                beforeMode++;
                log("FILTRO: evento fuera del modo cámara (no contable)");
            } else if (now - lastAccepted >= debounce) {
                lastAccepted = now;
                accepted++;
                log("FILTRO: evento aceptado #" + accepted + " (no es un punto confirmado)");
            } else {
                log("FILTRO: repetido, ignorado (" + (now - lastAccepted) + " ms)");
            }
        } else {
            others++;
        }
        updateUi();
    }

    private void sendCamera(boolean enable) {
        if (!ready) { log("Conecta el reloj antes de activar la cámara."); return; }
        cameraRequested = enable;
        if (enable) lastAccepted = Long.MIN_VALUE / 2;
        enqueueWrite(enable ? CAMERA_ON : CAMERA_OFF);
        status.setText("Estado: conectado, modo cámara " + (enable ? "solicitado" : "desactivado"));
    }

    private void enqueueWrite(byte[] message) {
        outgoing.add(message.clone());
        flushWrites();
    }

    private void flushWrites() {
        if (busy || outgoing.isEmpty() || gatt == null || writeCharacteristic == null || !ready) return;
        byte[] data = outgoing.poll();
        busy = true;
        writeCharacteristic.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE);
        writeCharacteristic.setValue(data);
        boolean success;
        try { success = gatt.writeCharacteristic(writeCharacteristic); }
        catch (Exception e) { success = false; }
        log((success ? "TX " : "TX ERROR ") + asHex(data));
        // Las escrituras GATT sin respuesta no necesitan esperar una confirmación ATT.
        // Intervalo breve para serializar llamadas al BluetoothGatt en Android 10.
        ui.postDelayed(() -> { busy = false; flushWrites(); }, 125);
    }

    private void disconnect() {
        stopSearch();
        if (gatt != null) {
            // Al desconectar, no dependemos de que CAMERA_OFF llegue al reloj.
            // La desconexión termina la sesión de cámara de nuestra app.
            try { gatt.disconnect(); } catch (Exception ignored) { }
            try { gatt.close(); } catch (Exception ignored) { }
        }
        gatt = null;
        ++sessionId;
        ready = false;
        cameraRequested = false;
        busy = false;
        writeCharacteristic = null;
        outgoing.clear();
        if (status != null) status.setText("Estado: desconectado");
        updateUi();
    }

    private void resetCounters() {
        notifications = 0;
        cameraEvents = 0;
        accepted = 0;
        others = 0;
        beforeMode = 0;
        lastAccepted = Long.MIN_VALUE / 2;
        log("--- CONTADORES REINICIADOS ---");
        updateUi();
    }

    private void updateUi() {
        if (counts == null) return;
        counts.setText("Notificaciones: " + notifications
                + "    Cámara: " + cameraEvents
                + "\nAceptados (filtro): " + accepted
                + "    Otros: " + others
                + "    Pre-modo: " + beforeMode);
        if (enableButton != null) enableButton.setEnabled(ready);
        if (disableButton != null) disableButton.setEnabled(ready);
        if (connectButton != null) connectButton.setEnabled(!found.isEmpty());
        if (scanButton != null) scanButton.setText(scanning ? "Buscando..." : "Buscar relojes");
    }

    private void log(String line) {
        if (Looper.myLooper() != Looper.getMainLooper()) {
            ui.post(() -> log(line));
            return;
        }
        String entry = clock.format(new Date()) + "  " + line + "\n";
        fullLog.append(entry);
        if (fullLog.length() > 200000) fullLog.delete(0, 30000);
        if (logText != null) {
            logText.append(entry);
            if (logText.getText().length() > 32000) {
                String raw = logText.getText().toString();
                logText.setText(raw.substring(Math.min(8000, raw.length())));
            }
            logScroll.post(() -> logScroll.fullScroll(View.FOCUS_DOWN));
        }
        try (FileOutputStream file = new FileOutputStream(new File(getFilesDir(), "ble_log.txt"), true)) {
            file.write(entry.getBytes(StandardCharsets.UTF_8));
        } catch (Exception ignored) { }
    }

    private void copyLog() {
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        if (clipboard != null) {
            clipboard.setPrimaryClip(ClipData.newPlainText("FitPro BLE", fullLog.toString()));
            log("Registro copiado al portapapeles.");
        }
    }

    private static boolean same(byte[] x, byte[] y) {
        if (x == null || x.length != y.length) return false;
        for (int i = 0; i < x.length; i++) if (x[i] != y[i]) return false;
        return true;
    }

    private static byte[] hex(String input) {
        String s = input.replace(" ", "");
        byte[] a = new byte[s.length() / 2];
        for (int i = 0; i < a.length; i++)
            a[i] = (byte) Integer.parseInt(s.substring(i * 2, i * 2 + 2), 16);
        return a;
    }

    private static String asHex(byte[] data) {
        StringBuilder sb = new StringBuilder();
        for (byte d : data) sb.append(String.format(Locale.US, "%02X ", d & 0xFF));
        return sb.toString().trim();
    }

    @Override protected void onDestroy() {
        disconnect();
        super.onDestroy();
    }
}
