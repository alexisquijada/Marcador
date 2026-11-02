package cl.marcadorpadel.sn30;

import android.app.Activity;
import android.graphics.Color;
import android.hardware.input.InputManager;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.InputDevice;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import org.json.JSONObject;
import java.util.Locale;

/**
 * Marcador Pádel v3.1 — mandos Android HID genéricos.
 * El emparejamiento Bluetooth lo realiza Android desde Ajustes.
 * No hace escaneo BLE, no necesita permisos de Bluetooth ni root.
 */
@SuppressWarnings("deprecation")
public class MainActivity extends Activity {
    private static final String TAG = "PadelSN30";
    private final Handler handler = new Handler(Looper.getMainLooper());
    private WebView webView;
    private InputManager inputManager;
    private boolean webReady = false;
    private boolean destroyed = false;

    // Además del listener, comprobar cada 2 segundos: algunos mandos Bluetooth
    // retrasan la notificacion de alta/baja cuando Android entra en reposo.
    // No considerar falta de pulsaciones como desconexion (pueden pasar minutos).
    private final Runnable connectionCheck = new Runnable() {
        @Override public void run() {
            if (destroyed) return;
            refreshControllerStatus();
            handler.postDelayed(this, 2000L);
        }
    };

    private final InputManager.InputDeviceListener deviceListener = new InputManager.InputDeviceListener() {
        @Override public void onInputDeviceAdded(int deviceId) { handler.post(() -> refreshControllerStatus()); }
        @Override public void onInputDeviceRemoved(int deviceId) { handler.post(() -> refreshControllerStatus()); }
        @Override public void onInputDeviceChanged(int deviceId) { handler.post(() -> refreshControllerStatus()); }
    };

    @Override protected void onCreate(Bundle state) {
        super.onCreate(state);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);

        inputManager = (InputManager)getSystemService(Context.INPUT_SERVICE);
        if (inputManager != null) inputManager.registerInputDeviceListener(deviceListener, handler);
        handler.postDelayed(connectionCheck, 2000L);

        webView = new WebView(this);
        webView.setBackgroundColor(Color.BLACK);
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setAllowFileAccess(true);
        webView.setWebChromeClient(new WebChromeClient());
        webView.setWebViewClient(new WebViewClient() {
            @Override public void onPageFinished(WebView view, String url) {
                if (!url.startsWith("file:///android_asset/")) return;
                webReady = true;
                refreshControllerStatus();
            }
        });
        setContentView(webView);
        webView.loadUrl("file:///android_asset/index.html");
        Log.i(TAG, "Marcador v3.1 iniciado. Entrada por mandos Android HID.");
    }

    /**
     * Linux reporta BTN_GAMEPAD/BTN_EAST/BTN_NORTH/BTN_WEST.
     * Android traduce los botones a KeyEvent; nunca suponemos que el orden
     * físico sea exacto, porque se pueden aprender desde el menú.
     */
    @Override public boolean dispatchKeyEvent(KeyEvent event) {
        InputDevice device = event.getDevice();
        if (device == null) device = InputDevice.getDevice(event.getDeviceId());
        if (isGameController(device) && isControllerKey(event.getKeyCode())) {
            if (event.getAction() == KeyEvent.ACTION_DOWN && event.getRepeatCount() == 0) {
                int code = event.getKeyCode();
                String name = KeyEvent.keyCodeToString(code);
                Log.d(TAG, "Control pulsado " + name + " (" + code + ") dispositivo=" + event.getDeviceId());
                runJs("window.padelGamepadEvent(" + code + ","
                        + JSONObject.quote(name) + "," + event.getDeviceId() + ");");
            }
            // Consumir UP y repeticiones; nunca emitir múltiples puntos por una pulsación.
            return true;
        }
        return super.dispatchKeyEvent(event);
    }

    private static boolean isGameController(InputDevice device) {
        if (device == null) return false;
        int sources = device.getSources();
        return ((sources & InputDevice.SOURCE_GAMEPAD) == InputDevice.SOURCE_GAMEPAD)
                || ((sources & InputDevice.SOURCE_JOYSTICK) == InputDevice.SOURCE_JOYSTICK);
    }

    private static boolean isControllerKey(int code) {
        // Android: botones A/B/C/X/Y/Z, L/R, select/start, etc.
        if (code >= KeyEvent.KEYCODE_BUTTON_A && code <= KeyEvent.KEYCODE_BUTTON_MODE) return true;
        return code == KeyEvent.KEYCODE_DPAD_UP || code == KeyEvent.KEYCODE_DPAD_DOWN
                || code == KeyEvent.KEYCODE_DPAD_LEFT || code == KeyEvent.KEYCODE_DPAD_RIGHT
                || code == KeyEvent.KEYCODE_DPAD_CENTER;
    }

    private void refreshControllerStatus() {
        int count = 0;
        String name = "";
        for (int id : InputDevice.getDeviceIds()) {
            InputDevice device = InputDevice.getDevice(id);
            if (isGameController(device)) {
                count++;
                name = device.getName();
            }
        }
        Log.d(TAG, "Mandos HID conectados: " + count);
        runJs("window.padelGamepadStatus(" + count + "," + JSONObject.quote(name) + ");");
    }

    private void runJs(String source) {
        if (!destroyed && webReady && webView != null)
            webView.evaluateJavascript(source, null);
    }

    @Override protected void onResume() {
        super.onResume();
        handler.post(() -> refreshControllerStatus());
    }

    @Override protected void onDestroy() {
        destroyed = true;
        webReady = false;
        if (inputManager != null) inputManager.unregisterInputDeviceListener(deviceListener);
        handler.removeCallbacksAndMessages(null);
        if (webView != null) {
            webView.loadUrl("about:blank");
            webView.destroy();
            webView = null;
        }
        super.onDestroy();
    }
}
