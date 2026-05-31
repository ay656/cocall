package io.github.ay656.call;

import org.json.JSONException;
import org.json.JSONObject;

class Contact {
    String id;
    String name;
    String displayName;
    String phone;
    String avatarUri;
    int avatarColor;
    int sort;

    static Contact fromJson(JSONObject object, int fallbackSort) throws JSONException {
        Contact contact = new Contact();
        contact.id = object.optString("id", String.valueOf(System.currentTimeMillis()));
        contact.name = object.optString("name", "");
        contact.displayName = object.optString("displayName", "");
        contact.phone = object.optString("phone", "");
        contact.avatarUri = object.optString("avatarUri", "");
        contact.avatarColor = object.optInt("avatarColor", 0);
        contact.sort = object.optInt("sort", fallbackSort);
        return contact;
    }

    JSONObject toJson(int index) throws JSONException {
        JSONObject object = new JSONObject();
        object.put("id", id == null || id.isEmpty() ? String.valueOf(System.currentTimeMillis()) : id);
        object.put("name", safe(name));
        object.put("displayName", safe(displayName));
        object.put("phone", safe(phone));
        object.put("avatarUri", safe(avatarUri));
        object.put("avatarColor", avatarColor);
        object.put("sort", index);
        return object;
    }

    String primaryName() {
        if (name != null && !name.trim().isEmpty()) {
            return name.trim();
        }
        if (displayName != null && !displayName.trim().isEmpty()) {
            return displayName.trim();
        }
        return "\u5bb6\u4eba";
    }

    private static String safe(String value) {
        return value == null ? "" : value.trim();
    }
}
