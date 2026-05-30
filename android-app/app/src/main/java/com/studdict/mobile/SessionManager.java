

package com.studdict.mobile;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {
    private static final String PREF_NAME = "StuddictSession";
    private static final String KEY_CHECKIN_ID = "checkInId";
    private static final String KEY_TABLE_ID = "tableId";

    // Logged-in student profile (captured at login, shown on the profile screen)
    private static final String KEY_STUDENT_ID = "studentId";
    private static final String KEY_FIRST_NAME = "firstName";
    private static final String KEY_LAST_NAME = "lastName";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_UNIVERSITY = "university";
    private static final String KEY_DEPARTMENT = "department";

    private SharedPreferences pref;
    private SharedPreferences.Editor editor;

    public SessionManager(Context context) {
        pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = pref.edit();
    }

    public void saveStudentProfile(String studentId, String firstName, String lastName,
                                   String email, String university, String department) {
        editor.putString(KEY_STUDENT_ID, studentId);
        editor.putString(KEY_FIRST_NAME, firstName);
        editor.putString(KEY_LAST_NAME, lastName);
        editor.putString(KEY_EMAIL, email);
        editor.putString(KEY_UNIVERSITY, university);
        editor.putString(KEY_DEPARTMENT, department);
        editor.apply();
    }

    public String getStudentId() { return pref.getString(KEY_STUDENT_ID, null); }
    public String getFirstName() { return pref.getString(KEY_FIRST_NAME, null); }
    public String getLastName() { return pref.getString(KEY_LAST_NAME, null); }
    public String getEmail() { return pref.getString(KEY_EMAIL, null); }
    public String getUniversity() { return pref.getString(KEY_UNIVERSITY, null); }
    public String getDepartment() { return pref.getString(KEY_DEPARTMENT, null); }

    public void clearProfile() {
        editor.remove(KEY_STUDENT_ID);
        editor.remove(KEY_FIRST_NAME);
        editor.remove(KEY_LAST_NAME);
        editor.remove(KEY_EMAIL);
        editor.remove(KEY_UNIVERSITY);
        editor.remove(KEY_DEPARTMENT);
        editor.apply();
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
