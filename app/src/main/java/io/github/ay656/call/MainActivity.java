package io.github.ay656.call;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.speech.tts.TextToSpeech;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
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
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class MainActivity extends Activity {
    private static final int REQUEST_CALL_PERMISSION = 10;
    private static final int REQUEST_PICK_AVATAR = 20;
    private static final long CALL_DELAY_MS = 1000L;

    private static final String APP_TITLE = "\u7b80\u547c";
    private static final String FOOTER_IDLE = "\u8f7b\u70b9\u5bb6\u4eba\u5373\u53ef\u62e8\u6253";
    private static final String TAP_TO_CALL = "\u8f7b\u70b9\u5373\u53ef\u547c\u53eb";
    private static final String NO_NUMBER_SPEAK = "\u53f7\u7801\u8fd8\u6ca1\u6709\u8bbe\u7f6e\uff0c\u8bf7\u8ba9\u5bb6\u4eba\u5e2e\u5fd9\u8bbe\u7f6e\u3002";
    private static final int[] AVATAR_COLORS = {
            0xFFE8B4B8, 0xFF90C4D8, 0xFF8DBFAC, 0xFFC4B0D4, 0xFFECC4A0, 0xFFB0C4CC
    };

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
    private boolean inSettings = false;
    private boolean settingsDirty = false;
    private boolean bindingEditors = false;

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
        inSettings = false;
        settingsDirty = false;
        root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackground(new MistBackgroundDrawable());
        root.setPadding(dp(18), dp(12), dp(18), dp(10));
        setContentView(root);

        root.setAlpha(0f);
        root.setTranslationY(dp(8));
        root.animate().alpha(1f).translationY(0f)
                .setDuration(220)
                .setInterpolator(new android.view.animation.DecelerateInterpolator())
                .start();

        TextView title = new TextView(this);
        title.setText(APP_TITLE);
        title.setTextSize(32);
        title.setTextColor(Color.rgb(20, 24, 24));
        title.setGravity(Gravity.CENTER);
        title.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);
        root.addView(title, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                dp(74)));

        title.setOnClickListener(view -> showSettings());

        ScrollView scrollView = new ScrollView(this);
        scrollView.setOverScrollMode(View.OVER_SCROLL_ALWAYS);
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
            LinearLayout emptyBox = new LinearLayout(this);
            emptyBox.setOrientation(LinearLayout.VERTICAL);
            emptyBox.setGravity(Gravity.CENTER);
            emptyBox.setPadding(dp(22), dp(28), dp(22), dp(28));
            emptyBox.setBackground(new GlassDrawable(dp(28)));

            TextView peopleIcon = new TextView(this);
            peopleIcon.setText("\uD83D\uDC65");
            peopleIcon.setTextSize(44);
            peopleIcon.setGravity(Gravity.CENTER);
            emptyBox.addView(peopleIcon, new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    dp(62)));

            TextView emptyTitle = text("\u8fd8\u6ca1\u6709\u8054\u7cfb\u4eba", 26, true);
            emptyTitle.setGravity(Gravity.CENTER);
            TextView emptyHint = text("\u70b9\u51fb\u9876\u90e8\u300c\u7b80\u547c\u300d\u8fdb\u5165\u8bbe\u7f6e", 17, false);
            emptyHint.setGravity(Gravity.CENTER);
            emptyHint.setTextColor(Color.argb(180, 52, 58, 58));
            emptyBox.addView(emptyTitle, new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    dp(52)));
            emptyBox.addView(emptyHint, new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    dp(42)));

            LinearLayout.LayoutParams emptyParams = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    dp(210));
            emptyParams.setMargins(0, dp(18), 0, 0);
            list.addView(emptyBox, emptyParams);
        } else {
            for (Contact contact : contacts) {
                list.addView(card(contact), cardLayoutParams());
            }
        }

        footer = text(FOOTER_IDLE, 16, false);
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
        GlassDrawable cardBg = new GlassDrawable(dp(28));
        card.setBackground(cardBg);
        card.setClickable(true);
        card.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        cardBg.setPressed(true);
                        card.setScaleX(0.97f);
                        card.setScaleY(0.97f);
                        return true;
                    case MotionEvent.ACTION_UP:
                        cardBg.setPressed(false);
                        card.animate().scaleX(1f).scaleY(1f).setDuration(120).start();
                        card.performClick();
                        return true;
                    case MotionEvent.ACTION_CANCEL:
                        cardBg.setPressed(false);
                        card.animate().scaleX(1f).scaleY(1f).setDuration(120).start();
                        return true;
                }
                return false;
            }
        });
        card.setOnClickListener(view -> startCall(contact));

        ImageView avatar = avatarView(contact, dp(76));
        card.addView(avatar, new LinearLayout.LayoutParams(dp(88), dp(112)));

        LinearLayout textBox = new LinearLayout(this);
        textBox.setOrientation(LinearLayout.VERTICAL);
        textBox.setGravity(Gravity.CENTER_VERTICAL);

        TextView name = text(contact.primaryName(), 34, true);
        name.setSingleLine(true);
        TextView detail = text(contact.displayName == null || contact.displayName.isEmpty()
                ? TAP_TO_CALL
                : contact.displayName, 16, false);
        detail.setTextColor(Color.argb(180, 52, 58, 58));
        detail.setSingleLine(true);
        textBox.addView(name);
        textBox.addView(detail);
        card.addView(textBox, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1));

        TextView phone = new TextView(this);
        phone.setText("\u260e");
        phone.setTextSize(28);
        phone.setTextColor(Color.rgb(26, 31, 31));
        phone.setGravity(Gravity.CENTER);
        phone.setBackground(new GlassDrawable(dp(30)));
        card.addView(phone, new LinearLayout.LayoutParams(dp(58), dp(58)));
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
        loadAvatarOrFallback(avatar, contact);
        avatar.setLayoutParams(new LinearLayout.LayoutParams(size, size));
        return avatar;
    }

    private void startCall(Contact contact) {
        if (callLocked) {
            return;
        }
        String phone = normalizePhone(contact.phone);
        if (!isValidPhone(phone)) {
            speak(NO_NUMBER_SPEAK);
            Toast.makeText(this,
                    contact.primaryName() + " \u7684\u53f7\u7801\u8fd8\u6ca1\u6709\u8bbe\u7f6e",
                    Toast.LENGTH_LONG).show();
            return;
        }

        callLocked = true;
        footer.setText("\u6b63\u5728\u547c\u53eb " + contact.primaryName() + "...");
        speak("\u6b63\u5728\u547c\u53eb " + contact.primaryName());
        showCallCountdown(contact, phone);
    }

    private void showCallCountdown(Contact contact, String phone) {
        final boolean[] shouldDial = {true};
        final int[] countdown = {3};

        android.app.Dialog dialog = new android.app.Dialog(this);
        dialog.requestWindowFeature(android.view.Window.FEATURE_NO_TITLE);
        dialog.setCancelable(true);
        dialog.setCanceledOnTouchOutside(true);

        LinearLayout panel = new LinearLayout(this);
        panel.setOrientation(LinearLayout.VERTICAL);
        panel.setGravity(Gravity.CENTER);
        panel.setPadding(dp(32), dp(28), dp(32), dp(28));
        panel.setBackground(new GlassDrawable(dp(32)));
        dialog.setContentView(panel);

        TextView countText = new TextView(this);
        countText.setText("3");
        countText.setTextSize(52);
        countText.setTextColor(Color.rgb(30, 38, 38));
        countText.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);
        countText.setGravity(Gravity.CENTER);
        panel.addView(countText, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, dp(70)));

        TextView nameText = new TextView(this);
        nameText.setText(contact.primaryName());
        nameText.setTextSize(28);
        nameText.setTextColor(Color.rgb(26, 33, 33));
        nameText.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);
        nameText.setGravity(Gravity.CENTER);
        panel.addView(nameText, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(44)));

        TextView hintText = new TextView(this);
        hintText.setText("\u5373\u5c06\u62e8\u53f7...");
        hintText.setTextSize(16);
        hintText.setTextColor(Color.argb(160, 55, 63, 63));
        hintText.setGravity(Gravity.CENTER);
        panel.addView(hintText, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(36)));

        Button cancelBtn = new Button(this);
        cancelBtn.setText("\u53d6\u6d88");
        cancelBtn.setTextSize(17);
        cancelBtn.setAllCaps(false);
        cancelBtn.setTextColor(Color.rgb(180, 55, 55));
        cancelBtn.setBackground(new GlassDrawable(dp(22)));
        cancelBtn.setPadding(dp(28), dp(12), dp(28), dp(12));
        LinearLayout.LayoutParams btnParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, dp(48));
        btnParams.setMargins(0, dp(16), 0, 0);
        panel.addView(cancelBtn, btnParams);

        dialog.getWindow().setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(
                Color.argb(80, 0, 0, 0)));
        dialog.getWindow().setLayout(dp(280), ViewGroup.LayoutParams.WRAP_CONTENT);

        dialog.setOnCancelListener(d -> {
            shouldDial[0] = false;
            resetCallState();
        });
        cancelBtn.setOnClickListener(v -> {
            shouldDial[0] = false;
            dialog.dismiss();
            resetCallState();
        });
        dialog.show();

        Runnable tick = new Runnable() {
            @Override
            public void run() {
                if (!shouldDial[0]) return;
                countdown[0]--;
                if (countdown[0] <= 0) {
                    dialog.dismiss();
                    dial(contact, phone);
                    handler.postDelayed(MainActivity.this::resetCallState, 1200L);
                    return;
                }
                countText.setText(String.valueOf(countdown[0]));
                countText.animate().scaleX(1.3f).scaleY(1.3f).setDuration(100)
                        .withEndAction(() -> countText.animate().scaleX(1f).scaleY(1f).setDuration(150).start())
                        .start();
                handler.postDelayed(this, 1000);
            }
        };
        handler.postDelayed(tick, 1000);
    }

    private void resetCallState() {
        callLocked = false;
        if (footer != null) {
            footer.setText(FOOTER_IDLE);
        }
    }

    private void dial(Contact contact, String phone) {
        pendingCall = contact;
        Intent callIntent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + phone));
        if (checkSelfPermission(Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED) {
            startActivity(callIntent);
        } else {
            Toast.makeText(this,
                    "\u9700\u8981\u62e8\u53f7\u6743\u9650\uff0c\u7528\u4e8e\u76f4\u63a5\u547c\u53eb\u5bb6\u4eba",
                    Toast.LENGTH_LONG).show();
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
                Toast.makeText(this,
                        "\u672a\u83b7\u5f97\u76f4\u63a5\u62e8\u53f7\u6743\u9650\uff0c\u5df2\u6253\u5f00\u7cfb\u7edf\u62e8\u53f7\u76d8",
                        Toast.LENGTH_LONG).show();
                startActivity(new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + phone)));
            }
        }
    }

    private void showSettings() {
        inSettings = true;
        settingsDirty = false;
        root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackground(new MistBackgroundDrawable());
        root.setPadding(dp(14), dp(12), dp(14), dp(12));
        setContentView(root);

        root.setAlpha(0f);
        root.setTranslationY(dp(8));
        root.animate().alpha(1f).translationY(0f)
                .setDuration(220)
                .setInterpolator(new android.view.animation.DecelerateInterpolator())
                .start();

        LinearLayout top = new LinearLayout(this);
        top.setOrientation(LinearLayout.HORIZONTAL);
        top.setGravity(Gravity.CENTER_VERTICAL);
        top.setPadding(0, 0, 0, dp(8));
        top.setBackground(new GlassDrawable(dp(18)));
        root.addView(top, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                dp(72)));

        Button back = button("\u2190 \u8fd4\u56de");
        Button save = button("\u4fdd\u5b58");
        TextView title = text("\u5bb6\u5c5e\u8bbe\u7f6e", 24, true);
        title.setGravity(Gravity.CENTER);
        top.addView(back, new LinearLayout.LayoutParams(dp(100), dp(46)));
        top.addView(title, new LinearLayout.LayoutParams(0, dp(46), 1));
        top.addView(save, new LinearLayout.LayoutParams(dp(100), dp(46)));

        ScrollView scroll = new ScrollView(this);
        scroll.setOverScrollMode(View.OVER_SCROLL_ALWAYS);
        settingsList = new LinearLayout(this);
        settingsList.setOrientation(LinearLayout.VERTICAL);
        settingsList.setPadding(0, dp(10), 0, dp(8));
        scroll.addView(settingsList);
        root.addView(scroll, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                0,
                1));

        Button add = button("\u6dfb\u52a0\u8054\u7cfb\u4eba");
        root.addView(add, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                dp(54)));

        back.setOnClickListener(view -> leaveSettings());
        save.setOnClickListener(view -> saveSettings());
        add.setOnClickListener(view -> {
            syncEditorInputs(false);
            Contact contact = new Contact();
            contact.id = String.valueOf(System.currentTimeMillis());
            contact.name = "";
            contact.displayName = "";
            contact.phone = "";
            contact.avatarUri = "";
            contact.avatarColor = AVATAR_COLORS[contacts.size() % AVATAR_COLORS.length];
            contacts.add(contact);
            settingsDirty = true;
            renderSettingsList();
        });
        renderSettingsList();
    }

    private void renderSettingsList() {
        bindingEditors = true;
        settingsList.removeAllViews();
        for (Contact contact : contacts) {
            settingsList.addView(editor(contact), editorLayoutParams());
        }
        bindingEditors = false;
    }

    private View editor(Contact contact) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(16), dp(14), dp(16), dp(14));
        card.setBackground(new GlassDrawable(dp(24)));

        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        card.addView(row, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                dp(156)));

        LinearLayout avatarBox = new LinearLayout(this);
        avatarBox.setOrientation(LinearLayout.VERTICAL);
        avatarBox.setGravity(Gravity.CENTER);
        row.addView(avatarBox, new LinearLayout.LayoutParams(dp(106), dp(156)));

        ImageButton avatar = new ImageButton(this);
        avatar.setBackgroundColor(Color.TRANSPARENT);
        avatar.setBackground(new GlassDrawable(dp(48)));
        loadAvatarOrFallback(avatar, contact);
        avatar.setScaleType(ImageView.ScaleType.CENTER_CROP);
        avatarBox.addView(avatar, new LinearLayout.LayoutParams(dp(92), dp(92)));

        TextView avatarHint = new TextView(this);
        avatarHint.setText("\u70b9\u51fb\u66f4\u6362");
        avatarHint.setTextSize(12);
        avatarHint.setTextColor(Color.argb(140, 60, 68, 68));
        avatarHint.setGravity(Gravity.CENTER);
        avatarBox.addView(avatarHint, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                dp(22)));

        LinearLayout fields = new LinearLayout(this);
        fields.setOrientation(LinearLayout.VERTICAL);
        fields.setPadding(dp(14), 0, 0, 0);
        row.addView(fields, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));

        TextView labelName = new TextView(this);
        labelName.setText("\u79f0\u8c13");
        labelName.setTextSize(13);
        labelName.setTextColor(Color.argb(160, 48, 56, 56));
        labelName.setPadding(0, 0, 0, dp(4));
        fields.addView(labelName);

        EditText name = input("\u4f8b\u5982\uff1a\u5973\u513f", contact.name, InputType.TYPE_CLASS_TEXT);

        TextView labelDisp = new TextView(this);
        labelDisp.setText("\u59d3\u540d\uff08\u53ef\u9009\uff09");
        labelDisp.setTextSize(13);
        labelDisp.setTextColor(Color.argb(160, 48, 56, 56));
        labelDisp.setPadding(0, dp(8), 0, dp(4));
        fields.addView(labelDisp);

        EditText displayName = input("\u771f\u5b9e\u59d3\u540d", contact.displayName, InputType.TYPE_CLASS_TEXT);

        TextView labelPhone = new TextView(this);
        labelPhone.setText("\u7535\u8bdd");
        labelPhone.setTextSize(13);
        labelPhone.setTextColor(Color.argb(160, 48, 56, 56));
        labelPhone.setPadding(0, dp(8), 0, dp(4));
        fields.addView(labelPhone);

        EditText phone = input("\u7535\u8bdd\u53f7\u7801", contact.phone, InputType.TYPE_CLASS_PHONE);
        attachDirtyWatcher(name);
        attachDirtyWatcher(displayName);
        attachDirtyWatcher(phone);
        fields.addView(name);
        fields.addView(displayName);
        fields.addView(phone);

        LinearLayout actions = new LinearLayout(this);
        actions.setOrientation(LinearLayout.HORIZONTAL);
        actions.setGravity(Gravity.CENTER_VERTICAL);
        actions.setPadding(0, dp(2), 0, 0);
        card.addView(actions, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                dp(52)));

        Button up = button("\u4e0a\u79fb");
        Button down = button("\u4e0b\u79fb");
        Button remove = button("\u5220\u9664");
        actions.addView(up, new LinearLayout.LayoutParams(0, dp(44), 1));
        actions.addView(down, new LinearLayout.LayoutParams(0, dp(44), 1));
        actions.addView(remove, new LinearLayout.LayoutParams(0, dp(44), 1));

        name.setOnFocusChangeListener((view, hasFocus) -> {
            if (!hasFocus) {
                contact.name = name.getText().toString();
                settingsDirty = true;
            }
        });
        displayName.setOnFocusChangeListener((view, hasFocus) -> {
            if (!hasFocus) {
                contact.displayName = displayName.getText().toString();
                settingsDirty = true;
            }
        });
        phone.setOnFocusChangeListener((view, hasFocus) -> {
            if (!hasFocus) {
                contact.phone = normalizePhone(phone.getText().toString());
                settingsDirty = true;
            }
        });

        avatar.setOnClickListener(view -> {
            syncEditorInputs(false);
            pendingAvatarContact = contact;
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("image/*");
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
            startActivityForResult(intent, REQUEST_PICK_AVATAR);
        });

        up.setOnClickListener(view -> moveContact(contact, -1));
        down.setOnClickListener(view -> moveContact(contact, 1));
        remove.setOnClickListener(view -> new AlertDialog.Builder(this)
                .setTitle("\u5220\u9664\u8054\u7cfb\u4eba")
                .setMessage("\u786e\u5b9a\u5220\u9664\u8fd9\u4e2a\u8054\u7cfb\u4eba\u5417\uff1f")
                .setNegativeButton("\u53d6\u6d88", null)
                .setPositiveButton("\u5220\u9664", (dialog, which) -> {
                    syncEditorInputs(false);
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
                dp(258));
        params.setMargins(0, 0, 0, dp(14));
        return params;
    }

    private void moveContact(Contact contact, int delta) {
        syncEditorInputs(false);
        int index = contacts.indexOf(contact);
        int target = index + delta;
        if (index < 0 || target < 0 || target >= contacts.size()) {
            return;
        }
        Collections.swap(contacts, index, target);
        settingsDirty = true;
        renderSettingsList();
    }

    private void saveSettings() {
        if (!syncEditorInputs(true)) {
            return;
        }
        store.save(contacts);
        settingsDirty = false;
        Toast.makeText(this, "\u5df2\u4fdd\u5b58", Toast.LENGTH_SHORT).show();
        showMain();
    }

    private boolean syncEditorInputs(boolean requireName) {
        if (settingsList == null) {
            return true;
        }
        for (int i = 0; i < settingsList.getChildCount(); i++) {
            View child = settingsList.getChildAt(i);
            Object tag = child.getTag();
            if (tag instanceof EditorRefs) {
                EditorRefs refs = (EditorRefs) tag;
                refs.contact.name = refs.name.getText().toString().trim();
                refs.contact.displayName = refs.displayName.getText().toString().trim();
                refs.contact.phone = normalizePhone(refs.phone.getText().toString());
                if (requireName && refs.contact.name.isEmpty()) {
                    Toast.makeText(this,
                            "\u8bf7\u586b\u5199\u6bcf\u4e2a\u8054\u7cfb\u4eba\u7684\u79f0\u8c13",
                            Toast.LENGTH_LONG).show();
                    return false;
                }
                if (requireName && !refs.contact.phone.isEmpty() && !isValidPhone(refs.contact.phone)) {
                    Toast.makeText(this,
                            refs.contact.primaryName() + " \u7684\u53f7\u7801\u683c\u5f0f\u4e0d\u6b63\u786e",
                            Toast.LENGTH_LONG).show();
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_PICK_AVATAR && resultCode == RESULT_OK && data != null && pendingAvatarContact != null) {
            Uri uri = data.getData();
            if (uri != null) {
                int flags = data.getFlags()
                        & (Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                int readFlag = flags & Intent.FLAG_GRANT_READ_URI_PERMISSION;
                if (readFlag != 0) {
                    try {
                        getContentResolver().takePersistableUriPermission(uri, readFlag);
                    } catch (SecurityException ignored) {
                    }
                }
                pendingAvatarContact.avatarUri = uri.toString();
                settingsDirty = true;
                renderSettingsList();
            }
        }
    }

    private void speak(String message) {
        if (tts != null) {
            tts.speak(message, TextToSpeech.QUEUE_FLUSH, null, "call");
        }
    }

    private void loadAvatarOrFallback(ImageView avatar, Contact contact) {
        if (contact.avatarUri != null && !contact.avatarUri.isEmpty()) {
            try {
                Drawable drawable = Drawable.createFromStream(
                        getContentResolver().openInputStream(Uri.parse(contact.avatarUri)),
                        "avatar");
                if (drawable != null) {
                    avatar.setImageDrawable(drawable);
                    return;
                }
            } catch (Exception ignored) {
                contact.avatarUri = "";
            }
        }
        avatar.setImageDrawable(new InitialDrawable(contact.primaryName(), contact.avatarColor));
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
        input.setHintTextColor(Color.argb(120, 60, 68, 68));
        input.setText(value == null ? "" : value);
        input.setTextSize(17);
        input.setSingleLine(true);
        input.setInputType(inputType);
        input.setBackground(new GlassDrawable(dp(14)));
        input.setPadding(dp(14), dp(12), dp(14), dp(12));
        input.setTextColor(Color.rgb(22, 28, 28));
        return input;
    }

    private void attachDirtyWatcher(EditText input) {
        input.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!bindingEditors) {
                    settingsDirty = true;
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    private Button button(String text) {
        Button button = new Button(this);
        button.setText(text);
        button.setTextSize(16);
        button.setAllCaps(false);
        button.setTextColor(Color.rgb(22, 28, 28));
        button.setBackground(new GlassDrawable(dp(18)));
        button.setPadding(dp(8), dp(10), dp(8), dp(10));
        button.setGravity(Gravity.CENTER);
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

    private boolean isValidPhone(String phone) {
        if (phone == null) {
            return false;
        }
        String digits = phone.replace("+", "");
        return digits.length() >= 5 && digits.length() <= 20;
    }

    private void leaveSettings() {
        syncEditorInputs(false);
        if (!settingsDirty) {
            showMain();
            return;
        }
        new AlertDialog.Builder(this)
                .setTitle("\u653e\u5f03\u4fee\u6539\uff1f")
                .setMessage("\u8bbe\u7f6e\u8fd8\u6ca1\u6709\u4fdd\u5b58\uff0c\u786e\u5b9a\u8fd4\u56de\u4e3b\u754c\u9762\u5417\uff1f")
                .setNegativeButton("\u7ee7\u7eed\u7f16\u8f91", null)
                .setPositiveButton("\u653e\u5f03", (dialog, which) -> showMain())
                .show();
    }

    @Override
    public void onBackPressed() {
        if (inSettings) {
            leaveSettings();
        } else {
            new AlertDialog.Builder(this)
                    .setTitle("\u786e\u5b9a\u9000\u51fa\uff1f")
                    .setMessage("\u9000\u51fa\u540e\u9700\u8981\u91cd\u65b0\u6253\u5f00\u7b80\u547c\u3002")
                    .setNegativeButton("\u7559\u5728\u7b80\u547c", null)
                    .setPositiveButton("\u9000\u51fa", (dialog, which) -> finish())
                    .show();
        }
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
