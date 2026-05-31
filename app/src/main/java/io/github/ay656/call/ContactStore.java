package io.github.ay656.call;

import android.content.Context;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

class ContactStore {
    private final File file;

    ContactStore(Context context) {
        file = new File(context.getFilesDir(), "contacts.json");
    }

    List<Contact> load() {
        if (!file.exists()) {
            List<Contact> defaults = defaults();
            save(defaults);
            return defaults;
        }

        try (FileInputStream input = new FileInputStream(file)) {
            byte[] bytes = new byte[(int) file.length()];
            int read = input.read(bytes);
            if (read <= 0) {
                return defaults();
            }
            JSONArray array = new JSONArray(new String(bytes, StandardCharsets.UTF_8));
            List<Contact> contacts = new ArrayList<>();
            for (int i = 0; i < array.length(); i++) {
                contacts.add(Contact.fromJson(array.getJSONObject(i), i + 1));
            }
            Collections.sort(contacts, Comparator.comparingInt(contact -> contact.sort));
            return contacts;
        } catch (IOException | JSONException exception) {
            return defaults();
        }
    }

    void save(List<Contact> contacts) {
        JSONArray array = new JSONArray();
        for (int i = 0; i < contacts.size(); i++) {
            try {
                array.put(contacts.get(i).toJson(i + 1));
            } catch (JSONException ignored) {
            }
        }

        try (FileOutputStream output = new FileOutputStream(file, false)) {
            output.write(array.toString(2).getBytes(StandardCharsets.UTF_8));
        } catch (IOException | JSONException ignored) {
        }
    }

    private List<Contact> defaults() {
        List<Contact> contacts = new ArrayList<>();
        contacts.add(make("daughter", "\u5973\u513f", "\u5c0f\u96e8", 1, 0xFFE8B4B8));
        contacts.add(make("spouse", "\u8001\u4f34", "", 2, 0xFF90C4D8));
        contacts.add(make("son", "\u513f\u5b50", "\u5c0f\u660e", 3, 0xFF8DBFAC));
        return contacts;
    }

    private Contact make(String id, String name, String displayName, int sort, int color) {
        Contact contact = new Contact();
        contact.id = id;
        contact.name = name;
        contact.displayName = displayName;
        contact.phone = "";
        contact.avatarUri = "";
        contact.avatarColor = color;
        contact.sort = sort;
        return contact;
    }
}
