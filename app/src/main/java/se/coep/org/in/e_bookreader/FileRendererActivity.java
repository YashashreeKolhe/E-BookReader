package se.coep.org.in.e_bookreader;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.media.Image;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.ActionMode;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.webkit.ValueCallback;
import android.webkit.WebView;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;


import org.w3c.dom.Text;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URI;
import java.util.List;

public class FileRendererActivity extends AppCompatActivity {
    private String fileName;
    private boolean immersiveVisibilityFlag = true;
    private DrawerLayout mDrawerLayout;
    private boolean isDrawerPressed = false;
    private EpubFile file;
    public static String currentFont = "Default";
    private String pasteData;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file_renderer_pdf);
        ActionBar actionbar = getSupportActionBar();
        Drawable mDrawable = ContextCompat.getDrawable(this, R.drawable.ic_menu);;
        actionbar.setHomeAsUpIndicator(mDrawable);
        actionbar.setDisplayHomeAsUpEnabled(true);
        mDrawerLayout = findViewById(R.id.drawer_layout);

        fileName = getIntent().getStringExtra(FileChooserActivity.FILE_NAME);

        if (fileName.endsWith("epub")) {
            file = new EpubFile(fileName, this, this.getWindow().getDecorView());
            String ncxFilePath = file.getNcxFilePath();
            file.parse(ncxFilePath);
            file.open(this, this.getWindow().getDecorView(), false);
            file.initializeForBookmark(ncxFilePath);
            ContentNavigation nav = new ContentNavigation("epub", this, this.getWindow().getDecorView());
            nav.addContent(file.getContentFile(), file);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                if(!isDrawerPressed) {
                    mDrawerLayout.openDrawer(GravityCompat.START);
                    isDrawerPressed = true;

                }
                else {
                    mDrawerLayout.closeDrawers();
                    isDrawerPressed = false;
                }
                return true;

            case R.id.optionsButton:
                final Dialog optionsDialog = new Dialog(this);
                optionsDialog.setContentView(R.layout.options_dialog);
                optionsDialog.getWindow().setGravity(Gravity.RIGHT | Gravity.TOP);
                optionsDialog.show();
                final TextView fontSize = (TextView) optionsDialog.findViewById(R.id.font_size_options_dialog);
                fontSize.setText(Integer.toString(file.getFontSize()));
                ImageButton fontPlus = (ImageButton) optionsDialog.findViewById(R.id.plus_button_options_dialog);
                ImageButton fontMinus = (ImageButton) optionsDialog.findViewById(R.id.minus_button_options_dialog);
                Spinner fontFamily = (Spinner) optionsDialog.findViewById(R.id.font_family_spinner_options_dialog);
                fontPlus.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        file.changeFontSize(true);
                        fontSize.setText(Integer.toString(file.getFontSize()));
                    }
                });
                fontMinus.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        file.changeFontSize(false);
                        fontSize.setText(Integer.toString(file.getFontSize()));
                    }
                });
                fontFamily.setOnItemSelectedListener(new MyItemSelectedListener());
                return true;
        }
        return super.onOptionsItemSelected(item);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.options_menu, menu);
        return true;
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(this, FileChooserActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    public void onActionModeStarted(final ActionMode mode) {
        super.onActionModeStarted(mode);

        MenuInflater menuInflater = mode.getMenuInflater();
        final Menu menu = mode.getMenu();

        //menu.clear();
        //menuInflater.inflate(R.menu.context_menu, menu);

        menu.add(0, 5, 0, "Highlight");

        //menu.findItem(R.id.copy).setOnMenuItemClickListener(new ToastMenuItemListener(this, mode, "One!", this.getWindow().getDecorView()));
        //menu.findItem(R.id.select_all).setOnMenuItemClickListener(new ToastMenuItemListener(this, mode, "Two!", this.getWindow().getDecorView()));
        Log.v("text1", String.valueOf(menu.getItem(0).getTitle()));
        Log.v("text1", String.valueOf(menu.getItem(1).getTitle()));
        Log.v("text1", String.valueOf(menu.getItem(2).getTitle()));

        menu.getItem(0).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                Toast.makeText(FileRendererActivity.this, "Text Highlighted!", Toast.LENGTH_SHORT).show();
                final WebView webview = (WebView) findViewById(R.id.webview);
                webview.getSettings().setJavaScriptEnabled(true);
                webview.evaluateJavascript("(function() {return window.getSelection().toString()})()",
                        new ValueCallback<String>() {
                            @Override
                            public void onReceiveValue(String s) {
                                Log.v("select", s);
                                pasteData = s;
                                String data = pasteData.substring(1, pasteData.length()-1);
                                Log.v("select", data);
                                webview.findAllAsync(data);
                                Method m = null;
                                try {
                                    m = WebView.class.getMethod("setFindIsUp", Boolean.TYPE);
                                    m.invoke(webview, true);
                                } catch (NoSuchMethodException e) {
                                    e.printStackTrace();
                                } catch (IllegalAccessException e) {
                                    e.printStackTrace();
                                } catch (InvocationTargetException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                );
                mode.finish();
                return true;
            }
        });
        //new ToastMenuItemListener(this, mode, "Text Highlighted!", this.getWindow().getDecorView()));
    }

    private  class ToastMenuItemListener implements MenuItem.OnMenuItemClickListener {

        private final Context context;
        private final ActionMode actionMode;
        private final String text;
        private View view;

        private ToastMenuItemListener(Context context, ActionMode actionMode, String text, View view) {
            this.context = context;
            this.actionMode = actionMode;
            this.text = text;
            this.view = view;
        }

        @Override
        public boolean onMenuItemClick(MenuItem item) {
            Toast.makeText(context, text, Toast.LENGTH_SHORT).show();
            final WebView webview = (WebView) view.findViewById(R.id.webview);
            webview.getSettings().setJavaScriptEnabled(true);
            webview.evaluateJavascript("(function() {return window.getSelection().toString()})()",
                    new ValueCallback<String>() {
                        @Override
                        public void onReceiveValue(String s) {
                            Log.v("select", s);
                        }
                    }
            );
            Menu menu = actionMode.getMenu();
            menu.findItem(0).setChecked(true);
            ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            String pasteData = "";
            ClipData.Item textItem = clipboard.getPrimaryClip().getItemAt(0);
            pasteData = textItem.getText().toString();
            Log.v("text", pasteData);
            menu.findItem(0).setChecked(false);
            webview.findAllAsync(pasteData);
            try
            {
                Method m = WebView.class.getMethod("setFindIsUp", Boolean.TYPE);
                m.invoke(webview, true);
            }
            catch (Throwable ignored){}
            actionMode.finish();
            return true;
        }
    }
}
