package com.example.mynoteapp.Support;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.Objects;

public class InputValidator {

    private static final String REQUIRED = "Field is Required!";
    private static final String INVALID_DATE = "Date NOT Valid: valid(DDMMYYYY)";
    private static final String INVALID_EXP_DATE = "Date NOT Valid: valid(YY/MM)";
    private static final InputValidator ourInstance = new InputValidator();

    public static InputValidator getInstance() {
        return ourInstance;
    }

    private InputValidator() {
    }


    public boolean validateRequired(@Nullable TextInputLayout layout, TextInputEditText text) {
        if (fieldNotBlank(text)) {
            return true;
        } else {
            if (layout != null) {
                showError(layout, REQUIRED);
                setErrorWatcher(text, layout);
            } else {
                showError(text, REQUIRED);
                setErrorWatcher(text, text);
            }
            return false;
        }
    }


    private boolean fieldNotBlank(@NonNull TextInputEditText text) {
        return text.getText() != null && !text.getText().toString().isEmpty();
    }


    public boolean validatePassword(TextInputLayout layout, TextInputEditText text) {
        if (validateRequired(layout, text)) {
            if (Objects.requireNonNull(text.getText()).toString().trim().length() < 4) {
                layout.setError("Password should be 4 digits");
                text.setError("");
                return false;
            } else {
                return true;
            }
        }
        return validateRequired(layout, text);
    }

    public boolean validateConfirmPassword(TextInputLayout layout, TextInputEditText password, TextInputEditText confirmPassword) {
        if (validatePassword(layout, confirmPassword)) {
            if (passwordMatch(password, confirmPassword)) {
                return true;
            } else {
                layout.setError("Passwords Don't Match");
                password.setError("");
                confirmPassword.setError("");
            }
        }
        return false;
    }

    public boolean validateConfirmPin(TextInputLayout layout, TextInputEditText password, TextInputEditText confirmPassword) {
        if (validatePassword(layout, confirmPassword)) {
            if (passwordMatch(password, confirmPassword)) {
                return true;
            } else {
                layout.setError("Pin Don't Match");
                password.setError("");
                confirmPassword.setError("");
            }
        }
        return false;
    }


    private boolean passwordMatch(TextInputEditText password, TextInputEditText confirmPassword) {
        return Objects.requireNonNull(password.getText()).toString().trim().equals(Objects.requireNonNull(confirmPassword.getText()).toString().trim());
    }


    private void clearError(@Nullable View view) {
        if (view != null) {
            if (view instanceof TextInputLayout) {
                ((TextInputLayout) view).setError(null);
            } else if (view instanceof TextInputEditText) {
                ((TextInputEditText) view).setError(null);

            } else if (view instanceof TextView) {
                ((TextView) view).setError(null);
                view.setVisibility(View.GONE);

            }
        }
    }

    private void showError(@NonNull View view, String error) {
        view.requestFocus();
        if (view instanceof TextInputLayout) {
            ((TextInputLayout) view).setError(error);
        } else if (view instanceof TextInputEditText) {
            ((TextInputEditText) view).setError(error);

        } else if (view instanceof TextView) {
            view.setVisibility(View.VISIBLE);
            ((TextView) view).setError("");
            ((TextView) view).setText(error);

        }

    }


    private void setErrorWatcher(@NonNull TextInputEditText input, @Nullable View error) {
        input.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                clearError(error);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }
}

