

package com.studdict.mobile;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {
    private static final String PREF_NAME = "StuddictSession";
    private static final String KEY_CHECKIN_ID = "checkInId";
    private static final String KEY_TABLE_ID = "tableId";

    private SharedPreferences pref;
    private SharedPreferences.Editor editor;

    public SessionManager(Context context) {
        pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = pref.edit();
    }

    public void setCheckIn(long checkInId, int tableId) {
        editor.putLong(KEY_CHECKIN_ID, checkInId);
        editor.putInt(KEY_TABLE_ID, tableId);
        editor.apply();
    }

    public long getCheckInId() {
        return pref.getLong(KEY_CHECKIN_ID, -1L);
    }

    public int getTableId() {
        return pref.getInt(KEY_TABLE_ID, -1);
    }

    public void clearCheckIn() {
        editor.remove(KEY_CHECKIN_ID);
        editor.remove(KEY_TABLE_ID);
        editor.apply();
    }
}
