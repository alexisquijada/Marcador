package cl.marcadorpadel.ble;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.location.LocationManager;
import android.view.View;
import android.view.WindowManager;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import org.json.JSONObject;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayDeque;
import java.util.Date;
import java.util.Locale;
import java.util.Queue;
import java.util.UUID;

/**
 * Marcador v2.4 + receptor BLE FitPro LP715(D) para Nexus 10 / Android 10.
 * Sin activar camara ni depender de FitPro. Solo son puntuables los tres
 * controles musicales 0x0A, 0x0B y 0x0C, comprobados en capturas reales.
 */
@SuppressWarnings("deprecation")
public class MainActivity extends Activity {
    private static final int REQ_LOCATION = 41;
    private static final UUID UART_SERVICE = UUID.fromString("6e400001-b5a3-f393-e0a9-e50e24dcca9d");
    private static final UUID UART_WRITE = UUID.fromString("6e400002-b5a3-f393-e0a9-e50e24dcca9d");
    private static final UUID UART_NOTIFY = UUID.fromString("6e400003-b5a3-f393-e0a9-e50e24dcca9d");
    private static final UUID CCCD = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");
    // Comandos ya identificados en los logs originales del usuario.
    private static final byte[] CAMERA_OFF = bytes("CD 00 06 12 01 0C 00 01 00");
    private static final byte[] CAMERA_EVENT = bytes("CD 00 05 1C 01 02 00 00");
    private static final byte[] CAMERA_ACK = bytes("DC 00 05 1C 01 00 08 01");
    private static final String PREFS = "padel_ble";
    private static final String LAST_MAC = "last_mac";
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Queue<byte[]> txQueue = new ArrayDeque<>();
    private final SimpleDateFormat timestamp = new SimpleDateFormat("HH:mm:ss.SSS", Locale.getDefault());

    private BluetoothAdapter bluetooth;
    private BluetoothLeScanner scanner;
    private BluetoothGatt gatt;
    private BluetoothGattCharacteristic writeChar;
    private ScanCallback scanCallback;
    private WebView webView;
    private boolean webReady = false;
    private boolean scanning = false;
    private boolean ready = false;
    private boolean writing = false;
    private boolean manualDisconnect = false;
    private boolean destroyed = false;
    private int session = 0;
    private long lastPrevious = 0;
    private long lastPause = 0;
    private long lastNext = 0;

    @Override protected void onCreate(Bundle state) {
        super.onCreate(state);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        BluetoothManager mgr = (BluetoothManager)getSystemService(Context.BLUETOOTH_SERVICE);
        bluetooth = mgr != null ? mgr.getAdapter() : null;
        webView = new WebView(this);
        webView.setBackgroundColor(Color.BLACK);
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setAllowFileAccess(true);
        webView.addJavascriptInterface(new BleBridge(), "PadelBLE");
        webView.setWebChromeClient(new WebChromeClient());
        webView.setWebViewClient(new WebViewClient() {
            @Override public void onPageFinished(WebView view, String url) {
                if (!url.startsWith("file:///android_asset/")) return;
                webReady = true;
                status(ready ? "ready" : "offline", ready ? "WATCH conectado" : "desconectado");
                // A partir de la segunda sesion se reutiliza la direccion conocida.
                String mac = getPreferences(MODE_PRIVATE).getString(LAST_MAC, null);
                if (mac != null && bluetooth != null && bluetooth.isEnabled()) {
                    log("Reconectando al WATCH conocido " + mac);
                    connectByMac(mac);
                }
            }
        });
        setContentView(webView);
        webView.loadUrl("file:///android_asset/index.html");
        log("Marcador BLE v2.5 iniciado. No activa camara. Music 0A/0B/0C.");
    }

    private final class BleBridge {
        @JavascriptInterface public void findWatch() {
            handler.post(() -> { manualDisconnect = false; scanForWatch(); });
        }
        @JavascriptInterface public void disconnect() {
            handler.post(() -> {
                manualDisconnect = true;
                stopScanning();
                closeConnection();
                status("offline", "desconectado manualmente");
                log("Desconexion manual");
            });
        }
    }

    private void js(String script) {
        if (!destroyed && webReady && webView != null) webView.evaluateJavascript(script, null);
    }
    private void status(String state, String message) {
        js("window.padelBleStatus(" + JSONObject.quote(state) + "," + JSONObject.quote(message) + ");");
    }
    private void command(String action) {
        js("window.padelBleCommand(" + JSONObject.quote(action) + ");");
    }
    private void log(String message) {
        String line = timestamp.format(new Date()) + "  " + message + "\n";
        try (FileOutputStream out = openFileOutput("ble_log.txt", MODE_APPEND)) {
            out.write(line.getBytes(StandardCharsets.UTF_8));
        } catch (Exception ignored) { }
        android.util.Log.d("PadelBLE", message);
    }

    private void scanForWatch() {
        if (bluetooth == null || !bluetooth.isEnabled()) {
            status("offline", "activa Bluetooth");
            log("ERROR: Bluetooth desactivado");
            return;
        }
        if (Build.VERSION.SDK_INT >= 23 &&
                checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQ_LOCATION);
            status("offline", "autoriza Ubicacion y vuelve a Buscar");
            return;
        }
        LocationManager lm = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
        if (lm != null && !lm.isProviderEnabled(LocationManager.GPS_PROVIDER)
                && !lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            log("AVISO: habilita Ubicacion del sistema para escanear BLE");
            status("offline", "activa Ubicacion en Android");
            return;
        }
        stopScanning();
        // Desconectar cualquier sesion antigua antes de empezar otra busqueda.
        closeConnection();
        scanner = bluetooth.getBluetoothLeScanner();
        if (scanner == null) { status("offline", "BLE scanner no disponible"); return; }
        scanCallback = new ScanCallback() {
            @Override public void onScanResult(int type, ScanResult result) {
                if (result == null || result.getDevice() == null) return;
                BluetoothDevice dev = result.getDevice();
                String name = dev.getName();
                String saved = getPreferences(MODE_PRIVATE).getString(LAST_MAC, "");
                if ("WATCH".equalsIgnoreCase(name) || dev.getAddress().equalsIgnoreCase(saved)) {
                    handler.post(() -> {
                        if (!scanning) return;
                        log("Encontrado WATCH: " + dev.getAddress());
                        stopScanning();
                        connect(dev);
                    });
                }
            }
            @Override public void onScanFailed(int errorCode) {
                handler.post(() -> {
                    stopScanning();
                    status("offline", "fallo escaneo: " + errorCode);
                    log("ERROR scan BLE: " + errorCode);
                });
            }
        };
        try {
            scanner.startScan(scanCallback);
            scanning = true;
            status("scanning", "buscando WATCH (12 segundos)");
            log("Buscando reloj WATCH por BLE...");
            final ScanCallback thisScan = scanCallback;
            handler.postDelayed(() -> {
                if (scanning && scanCallback == thisScan) {
                    stopScanning();
                    status("offline", "WATCH no encontrado; vuelve a buscar");
                    log("WATCH no aparecio en 12 s");
                }
            }, 12000);
        } catch (Exception e) {
            status("offline", "error de escaneo"); log("ERROR: " + e.getMessage());
        }
    }

    private void stopScanning() {
        if (scanning && scanner != null && scanCallback != null) {
            try { scanner.stopScan(scanCallback); } catch (Exception ignored) { }
        }
        scanning = false;
        scanCallback = null;
    }

    private void connectByMac(String address) {
        if (bluetooth == null || !bluetooth.isEnabled()) {
            status("offline", "activa Bluetooth"); return;
        }
        try { connect(bluetooth.getRemoteDevice(address)); }
        catch (Exception e) {
            log("ERROR direccion WATCH: " + e.getMessage());
            scanForWatch();
        }
    }

    private void connect(BluetoothDevice dev) {
        if (destroyed) return;
        closeConnection();
        final int owner = session;
        manualDisconnect = false;
        ready = false;
        status("scanning", "conectando a WATCH...");
        log("Conectando " + dev.getAddress());
        try {
            gatt = dev.connectGatt(this, false, new BluetoothGattCallback() {
                private boolean current() { return owner == session && !destroyed; }
                @Override public void onConnectionStateChange(BluetoothGatt bg, int code, int state) {
                    handler.post(() -> {
                        if (!current()) return;
                        if (code == BluetoothGatt.GATT_SUCCESS && state == BluetoothProfile.STATE_CONNECTED) {
                            log("GATT conectado, descubriendo servicios");
                            status("scanning", "descubriendo servicios...");
                            if (!bg.discoverServices()) log("ERROR discoverServices");
                        } else if (state == BluetoothProfile.STATE_DISCONNECTED || code != BluetoothGatt.GATT_SUCCESS) {
                            log("GATT desconectado estado=" + state + " error=" + code);
                            status("offline", "WATCH desconectado (" + code + ")");
                            closeConnection();
                            if (!manualDisconnect) {
                                handler.postDelayed(() -> {
                                    if (!destroyed && !manualDisconnect && !ready && !scanning) scanForWatch();
                                }, 5000);
                            }
                        }
                    });
                }
                @Override public void onServicesDiscovered(BluetoothGatt bg, int code) {
                    handler.post(() -> {
                        if (!current()) return;
                        if (code != BluetoothGatt.GATT_SUCCESS) {
                            status("offline", "error servicios GATT " + code); return;
                        }
                        BluetoothGattService svc = bg.getService(UART_SERVICE);
                        if (svc == null) {
                            status("offline", "WATCH sin servicio UART");
                            log("ERROR: Nordic UART no encontrado"); return;
                        }
                        BluetoothGattCharacteristic rx = svc.getCharacteristic(UART_NOTIFY);
                        writeChar = svc.getCharacteristic(UART_WRITE);
                        if (rx == null || writeChar == null) {
                            status("offline", "faltan caracteristicas UART"); return;
                        }
                        writeChar.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE);
                        BluetoothGattDescriptor desc = rx.getDescriptor(CCCD);
                        if (desc == null || !bg.setCharacteristicNotification(rx, true)) {
                            status("offline", "notificaciones no disponibles"); return;
                        }
                        desc.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                        if (!bg.writeDescriptor(desc)) {
                            status("offline", "error habilitando BLE"); return;
                        }
                        log("UART localizado; habilitando notificaciones");
                    });
                }
                @Override public void onDescriptorWrite(BluetoothGatt bg, BluetoothGattDescriptor desc, int code) {
                    handler.post(() -> {
                        if (!current()) return;
                        if (code != BluetoothGatt.GATT_SUCCESS) {
                            status("offline", "fallo al habilitar notificaciones"); return;
                        }
                        ready = true;
                        getPreferences(MODE_PRIVATE).edit().putString(LAST_MAC, dev.getAddress()).apply();
                        status("ready", "WATCH conectado (modo Musica)");
                        log("BLE listo; SIN camara ON. Solicitando solo CAMARA OFF.");
                        enqueue(CAMERA_OFF); // recuperacion de estado sin entrar a modo camara
                    });
                }
                @Override public void onCharacteristicChanged(BluetoothGatt bg, BluetoothGattCharacteristic c) {
                    if (!current() || !UART_NOTIFY.equals(c.getUuid())) return;
                    byte[] data = c.getValue();
                    if (data != null) {
                        byte[] copy = data.clone();
                        handler.post(() -> { if (current()) received(copy); });
                    }
                }
                @Override public void onCharacteristicWrite(BluetoothGatt bg, BluetoothGattCharacteristic c, int code) {
                    if (code != BluetoothGatt.GATT_SUCCESS && current())
                        handler.post(() -> log("TX error GATT=" + code));
                }
            }, BluetoothDevice.TRANSPORT_LE);
            if (gatt == null) status("offline", "connectGatt devolvio null");
        } catch (Exception e) {
            log("ERROR connectGatt: " + e.getMessage());
            status("offline", "fallo conexion");
        }
    }

    private void received(byte[] data) {
        String hex = asHex(data);
        boolean control = data.length >= 8 && (data[0] & 255) == 0xCD
                && (data[3] & 255) == 0x1C && (data[4] & 255) == 0x01;
        int action = control ? (data[5] & 255) : -1;
        if (control && action == 0x0A) {
            log("RX " + hex + " [MUSIC_PREVIOUS / DESHACER]");
            if (dedupe(0x0A)) command("previous");
        } else if (control && action == 0x0B) {
            log("RX " + hex + " [MUSIC_PLAY_PAUSE / CAMBIAR EQUIPO]");
            if (dedupe(0x0B)) command("pause");
        } else if (control && action == 0x0C) {
            log("RX " + hex + " [MUSIC_NEXT / SUMAR]");
            if (dedupe(0x0C)) command("next");
        } else if (equalsBytes(data, CAMERA_EVENT)) {
            log("RX " + hex + " [CAMERA ignorada; no suma]");
            enqueue(CAMERA_ACK); // Solo la confirmacion de camara verificada empiricamente
        } else if (control) {
            log("RX " + hex + " [CONTROL_1C_" + String.format(Locale.US, "%02X", action) + " sin asignar]");
        } else {
            // Incluye pasos, estado y otras telemetrias; nunca cambian el marcador.
            log("RX " + hex);
        }
    }

    private boolean dedupe(int action) {
        long now = SystemClock.elapsedRealtime();
        long last;
        if (action == 0x0A) last = lastPrevious;
        else if (action == 0x0B) last = lastPause;
        else last = lastNext;
        if (now-last < 350) {
            log("Filtro BLE: duplicado " + action + " a " + (now-last) + " ms");
            return false;
        }
        if (action == 0x0A) lastPrevious = now;
        else if (action == 0x0B) lastPause = now;
        else lastNext = now;
        return true;
    }

    private void enqueue(byte[] data) {
        txQueue.add(data.clone());
        flushWrites();
    }
    private void flushWrites() {
        if (!ready || writing || txQueue.isEmpty() || gatt == null || writeChar == null) return;
        byte[] data = txQueue.remove();
        writing = true;
        writeChar.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE);
        writeChar.setValue(data);
        boolean ok;
        try { ok = gatt.writeCharacteristic(writeChar); } catch (Exception e) { ok = false; }
        log((ok ? "TX " : "TX ERROR ") + asHex(data));
        handler.postDelayed(() -> { writing = false; flushWrites(); }, 125);
    }

    private void closeConnection() {
        session++;
        ready = false;
        writing = false;
        writeChar = null;
        txQueue.clear();
        if (gatt != null) {
            try { gatt.disconnect(); } catch (Exception ignored) { }
            try { gatt.close(); } catch (Exception ignored) { }
            gatt = null;
        }
    }
    private static byte[] bytes(String hex) {
        String[] parts = hex.trim().split(" +");
        byte[] b = new byte[parts.length];
        for (int i = 0; i < parts.length; i++) b[i] = (byte)Integer.parseInt(parts[i], 16);
        return b;
    }
    private static boolean equalsBytes(byte[] a, byte[] b) {
        if (a == null || a.length != b.length) return false;
        for (int i = 0; i < a.length; i++) if (a[i] != b[i]) return false;
        return true;
    }
    private static String asHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            if (sb.length() > 0) sb.append(' ');
            sb.append(String.format(Locale.US, "%02X", b & 255));
        }
        return sb.toString();
    }
    @Override protected void onDestroy() {
        destroyed = true;
        stopScanning();
        closeConnection();
        if (webView != null) {
            webView.removeJavascriptInterface("PadelBLE");
            webView.destroy();
            webView = null;
        }
        super.onDestroy();
    }
}
