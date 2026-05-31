package io.github.ay656.call;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.speech.tts.TextToSpeech;
import android.text.InputType;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends Activity {
    private static final int REQUEST_CALL_PERMISSION = 10;
    private static final int REQUEST_PICK_AVATAR = 20;
    private static final long HOLD_TO_SETTINGS_MS = 3000L;
    private static final long CALL_DELAY_MS = 900L;

    private final Handler handler = new Handler(Looper.getMainLooper());
    private ContactStore store;
    private List<Contact> contacts = new ArrayList<>();
    private LinearLayout root;
    private TextView footer;
    private TextToSpeech tts;
    private Contact pendingCall;
    private Contact pendingAvatarContact;
    private LinearLayout settingsList;
    private boolean callLocked = false;

    private final Runnable openSettingsRunnable = this::showSettingsGate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        store = new ContactStore(this);
        contacts = store.load();
        tts = new TextToSpeech(this, status -> {
            if (status == TextToSpeech.SUCCESS) {
                tts.setLanguage(Locale.CHINESE);
            }
        });
        showMain();
    }

    private void showMain() {
        root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackground(new MistBackgroundDrawable());
        root.setPadding(dp(18), dp(12), dp(18), dp(10));
        setContentView(root);

        TextView title = new TextView(this);
        title.setText("简呼");
        title.setTextSize(32);
        title.setTextColor(Color.rgb(20, 24, 24));
        title.setGravity(Gravity.CENTER);
        title.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);
        root.addView(title, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                dp(74)));

        title.setOnTouchListener((view, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                title.setText("继续长按");
                handler.postDelayed(openSettingsRunnable, HOLD_TO_SETTINGS_MS);
                return true;
            }
            if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {
                title.setText("简呼");
                handler.removeCallbacks(openSettingsRunnable);
                return true;
            }
            return true;
        });

        ScrollView scrollView = new ScrollView(this);
        LinearLayout list = new LinearLayout(this);
        list.setOrientation(LinearLayout.VERTICAL);
        list.setPadding(0, dp(8), 0, dp(8));
        scrollView.addView(list);
        root.addView(scrollView, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                0,
                1));

        contacts = store.load();
        if (contacts.isEmpty()) {
            TextView empty = text("请家人先设置联系人", 26, true);
            empty.setGravity(Gravity.CENTER);
            list.addView(empty, new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    dp(160)));
        } else {
            for (Contact contact : contacts) {
                list.addView(card(contact), cardLayoutParams());
            }
        }

        footer = text("轻点家人即可拨打", 16, false);
        footer.setGravity(Gravity.CENTER);
        footer.setTextColor(Color.argb(180, 38, 44, 44));
        root.addView(footer, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                dp(42)));
    }

    private View card(Contact contact) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.HORIZONTAL);
        card.setGravity(Gravity.CENTER_VERTICAL);
        card.setPadding(dp(18), 0, dp(16), 0);
        card.setBackground(new GlassDrawable(dp(28)));
        card.setClickable(true);
        card.setOnClickListener(view -> startCall(contact));

        ImageView avatar = avatarView(contact, dp(76));
        card.addView(avatar, new LinearLayout.LayoutParams(dp(88), dp(112)));

        LinearLayout textBox = new LinearLayout(this);
        textBox.setOrientation(LinearLayout.VERTICAL);
        textBox.setGravity(Gravity.CENTER_VERTICAL);

        TextView name = text(contact.primaryName(), 34, true);
        name.setSingleLine(true);
        TextView detail = text(contact.displayName == null || contact.displayName.isEmpty()
                ? "轻点即可呼叫"
                : contact.displayName, 16, false);
        detail.setTextColor(Color.argb(180, 52, 58, 58));
        detail.setSingleLine(true);
        textBox.addView(name);
        textBox.addView(detail);
        card.addView(textBox, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1));

        TextView phone = new TextView(this);
        phone.setText("☎");
        phone.setTextSize(28);
        phone.setTextColor(Color.rgb(26, 31, 31));
        phone.setGravity(Gravity.CENTER);
        phone.setBackground(new GlassDrawable(dp(30)));
        LinearLayout.LayoutParams phoneParams = new LinearLayout.LayoutParams(dp(58), dp(58));
        card.addView(phone, phoneParams);
        return card;
    }

    private LinearLayout.LayoutParams cardLayoutParams() {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                dp(118));
        params.setMargins(0, 0, 0, dp(16));
        return params;
    }

    private ImageView avatarView(Contact contact, int size) {
        ImageView avatar = new ImageView(this);
        avatar.setScaleType(ImageView.ScaleType.CENTER_CROP);
        avatar.setPadding(dp(8), dp(8), dp(8), dp(8));
        if (contact.avatarUri != null && !contact.avatarUri.isEmpty()) {
            avatar.setImageURI(Uri.parse(contact.avatarUri));
        } else {
            avatar.setImageDrawable(new InitialDrawable(contact.primaryName()));
        }
        avatar.setLayoutParams(new LinearLayout.LayoutParams(size, size));
        return avatar;
    }

    private void startCall(Contact contact) {
        if (callLocked) {
            return;
        }
        String phone = normalizePhone(contact.phone);
        if (phone.length() < 5) {
            speak("号码还没有设置，请让家人帮忙设置。");
            Toast.makeText(this, contact.primaryName() + " 的号码还没有设置", Toast.LENGTH_LONG).show();
            return;
        }
        callLocked = true;
        footer.setText("正在呼叫 " + contact.primaryName() + "...");
        speak("正在呼叫 " + contact.primaryName());
        handler.postDelayed(() -> dial(contact, phone), CALL_DELAY_MS);
        handler.postDelayed(() -> {
            callLocked = false;
            if (footer != null) {
                footer.setText("轻点家人即可拨打");
            }
        }, 2000L);
    }

    private void dial(Contact contact, String phone) {
        pendingCall = contact;
        Intent callIntent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + phone));
        if (checkSelfPermission(Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED) {
            startActivity(callIntent);
        } else {
            requestPermissions(new String[]{Manifest.permission.CALL_PHONE}, REQUEST_CALL_PERMISSION);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CALL_PERMISSION && pendingCall != null) {
            String phone = normalizePhone(pendingCall.phone);
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startActivity(new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + phone)));
            } else {
                startActivity(new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + phone)));
            }
        }
    }

    private void showSettingsGate() {
        EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        input.setGravity(Gravity.CENTER);
        input.setTextSize(22);
        new AlertDialog.Builder(this)
                .setTitle("家属验证")
                .setMessage("请输入 3 + 5 的结果")
                .setView(input)
                .setNegativeButton("取消", null)
                .setPositiveButton("进入", (dialog, which) -> {
                    if ("8".equals(input.getText().toString().trim())) {
                        showSettings();
                    } else {
                        Toast.makeText(this, "答案不正确", Toast.LENGTH_SHORT).show();
                    }
                })
                .show();
    }

    private void showSettings() {
        root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackground(new MistBackgroundDrawable());
        root.setPadding(dp(14), dp(12), dp(14), dp(12));
        setContentView(root);

        LinearLayout top = new LinearLayout(this);
        top.setOrientation(LinearLayout.HORIZONTAL);
        top.setGravity(Gravity.CENTER_VERTICAL);
        top.setPadding(0, 0, 0, dp(8));
        root.addView(top, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                dp(68)));

        Button back = button("返回");
        Button save = button("保存");
        TextView title = text("家属设置", 26, true);
        title.setGravity(Gravity.CENTER);
        top.addView(back, new LinearLayout.LayoutParams(dp(88), dp(48)));
        top.addView(title, new LinearLayout.LayoutParams(0, dp(48), 1));
        top.addView(save, new LinearLayout.LayoutParams(dp(88), dp(48)));

        ScrollView scroll = new ScrollView(this);
        settingsList = new LinearLayout(this);
        settingsList.setOrientation(LinearLayout.VERTICAL);
        scroll.addView(settingsList);
        root.addView(scroll, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                0,
                1));

        Button add = button("添加联系人");
        root.addView(add, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                dp(54)));

        back.setOnClickListener(view -> showMain());
        save.setOnClickListener(view -> saveSettings());
        add.setOnClickListener(view -> {
            Contact contact = new Contact();
            contact.id = String.valueOf(System.currentTimeMillis());
            contact.name = "";
            contact.displayName = "";
            contact.phone = "";
            contact.avatarUri = "";
            contacts.add(contact);
            renderSettingsList();
        });
        renderSettingsList();
    }

    private void renderSettingsList() {
        settingsList.removeAllViews();
        for (Contact contact : contacts) {
            settingsList.addView(editor(contact), editorLayoutParams());
        }
    }

    private View editor(Contact contact) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(14), dp(12), dp(14), dp(12));
        card.setBackground(new GlassDrawable(dp(22)));

        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        card.addView(row, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                dp(146)));

        ImageButton avatar = new ImageButton(this);
        avatar.setBackgroundColor(Color.TRANSPARENT);
        if (contact.avatarUri != null && !contact.avatarUri.isEmpty()) {
            avatar.setImageURI(Uri.parse(contact.avatarUri));
        } else {
            avatar.setImageDrawable(new InitialDrawable(contact.primaryName()));
        }
        avatar.setScaleType(ImageView.ScaleType.CENTER_CROP);
        row.addView(avatar, new LinearLayout.LayoutParams(dp(96), dp(96)));

        LinearLayout fields = new LinearLayout(this);
        fields.setOrientation(LinearLayout.VERTICAL);
        fields.setPadding(dp(12), 0, 0, 0);
        row.addView(fields, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1));

        EditText name = input("称谓，例如 女儿", contact.name, InputType.TYPE_CLASS_TEXT);
        EditText displayName = input("真实姓名，可选", contact.displayName, InputType.TYPE_CLASS_TEXT);
        EditText phone = input("电话号码", contact.phone, InputType.TYPE_CLASS_PHONE);
        fields.addView(name);
        fields.addView(displayName);
        fields.addView(phone);

        LinearLayout actions = new LinearLayout(this);
        actions.setOrientation(LinearLayout.HORIZONTAL);
        actions.setGravity(Gravity.CENTER_VERTICAL);
        card.addView(actions, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                dp(48)));

        Button remove = button("删除");
        actions.addView(remove, new LinearLayout.LayoutParams(0, dp(44), 1));

        name.setOnFocusChangeListener((view, hasFocus) -> {
            if (!hasFocus) contact.name = name.getText().toString();
        });
        displayName.setOnFocusChangeListener((view, hasFocus) -> {
            if (!hasFocus) contact.displayName = displayName.getText().toString();
        });
        phone.setOnFocusChangeListener((view, hasFocus) -> {
            if (!hasFocus) contact.phone = normalizePhone(phone.getText().toString());
        });

        avatar.setOnClickListener(view -> {
            contact.name = name.getText().toString();
            contact.displayName = displayName.getText().toString();
            contact.phone = normalizePhone(phone.getText().toString());
            pendingAvatarContact = contact;
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("image/*");
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
            startActivityForResult(intent, REQUEST_PICK_AVATAR);
        });

        remove.setOnClickListener(view -> new AlertDialog.Builder(this)
                .setTitle("删除联系人")
                .setMessage("确定删除这个联系人吗？")
                .setNegativeButton("取消", null)
                .setPositiveButton("删除", (dialog, which) -> {
                    contacts.remove(contact);
                    renderSettingsList();
                })
                .show());

        card.setTag(new EditorRefs(contact, name, displayName, phone));
        return card;
    }

    private LinearLayout.LayoutParams editorLayoutParams() {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                dp(214));
        params.setMargins(0, 0, 0, dp(14));
        return params;
    }

    private void saveSettings() {
        for (int i = 0; i < settingsList.getChildCount(); i++) {
            View child = settingsList.getChildAt(i);
            Object tag = child.getTag();
            if (tag instanceof EditorRefs) {
                EditorRefs refs = (EditorRefs) tag;
                refs.contact.name = refs.name.getText().toString().trim();
                refs.contact.displayName = refs.displayName.getText().toString().trim();
                refs.contact.phone = normalizePhone(refs.phone.getText().toString());
                if (refs.contact.name.isEmpty()) {
                    Toast.makeText(this, "请填写每个联系人的称谓", Toast.LENGTH_LONG).show();
                    return;
                }
            }
        }
        store.save(contacts);
        Toast.makeText(this, "已保存", Toast.LENGTH_SHORT).show();
        showMain();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_PICK_AVATAR && resultCode == RESULT_OK && data != null && pendingAvatarContact != null) {
            Uri uri = data.getData();
            if (uri != null) {
                int flags = data.getFlags() & Intent.FLAG_GRANT_READ_URI_PERMISSION;
                try {
                    getContentResolver().takePersistableUriPermission(uri, flags);
                } catch (SecurityException ignored) {
                }
                pendingAvatarContact.avatarUri = uri.toString();
                renderSettingsList();
            }
        }
    }

    private void speak(String message) {
        if (tts != null) {
            tts.speak(message, TextToSpeech.QUEUE_FLUSH, null, "call");
        }
    }

    private TextView text(String value, int size, boolean bold) {
        TextView view = new TextView(this);
        view.setText(value);
        view.setTextSize(size);
        view.setTextColor(Color.rgb(18, 22, 22));
        view.setGravity(Gravity.CENTER_VERTICAL);
        if (bold) {
            view.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);
        }
        return view;
    }

    private EditText input(String hint, String value, int inputType) {
        EditText input = new EditText(this);
        input.setHint(hint);
        input.setText(value == null ? "" : value);
        input.setTextSize(17);
        input.setSingleLine(true);
        input.setInputType(inputType);
        input.setBackground(new GlassDrawable(dp(12)));
        input.setPadding(dp(10), 0, dp(10), 0);
        return input;
    }

    private Button button(String text) {
        Button button = new Button(this);
        button.setText(text);
        button.setTextSize(16);
        button.setAllCaps(false);
        button.setTextColor(Color.rgb(22, 28, 28));
        button.setBackground(new GlassDrawable(dp(18)));
        return button;
    }

    private String normalizePhone(String value) {
        if (value == null) {
            return "";
        }
        String trimmed = value.trim();
        String prefix = trimmed.startsWith("+") ? "+" : "";
        return prefix + trimmed.replaceAll("\\D", "");
    }

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density + 0.5f);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
    }

    private static class EditorRefs {
        final Contact contact;
        final EditText name;
        final EditText displayName;
        final EditText phone;

        EditorRefs(Contact contact, EditText name, EditText displayName, EditText phone) {
            this.contact = contact;
            this.name = name;
            this.displayName = displayName;
            this.phone = phone;
        }
    }
}
