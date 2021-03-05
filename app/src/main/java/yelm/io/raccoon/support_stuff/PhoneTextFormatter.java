package yelm.io.raccoon.support_stuff;

import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.widget.EditText;

public class PhoneTextFormatter implements TextWatcher {

    private EditText editText;

    private String pattern;

    public PhoneTextFormatter(EditText editText, String pattern) {
        this.editText = editText;
        this.pattern = pattern;
        //set max length of string
        int maxLength = pattern.length();
        this.editText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(maxLength)});
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        StringBuilder phone = new StringBuilder(s);
        if (count > 0 && !isValid(phone.toString())) {
            for (int i = 0; i < phone.length(); i++) {
                char c = pattern.charAt(i);
                if ((c != '#') && (c != phone.charAt(i))) {
                    phone.insert(i, c);
                }
            }
            editText.setText(phone);
            editText.setSelection(editText.getText().length());
        }
    }

    @Override
    public void afterTextChanged(Editable s) {
    }

    private boolean isValid(String phone)
    {
        for (int i = 0; i < phone.length(); i++) {
            char c = pattern.charAt(i);
            if (c == '#') continue;
            if (c != phone.charAt(i)) {
                return false;
            }
        }
        return true;
    }
}