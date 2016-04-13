package io.mochilogic.futontext;

import android.content.Context;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.widget.EditText;

/**
 * Created by asanchez on 3/31/16.
 */
public class KeyEventEditText extends EditText {

    Context context;
    EditTextKeyEventCallbackListener m_listener;

    public KeyEventEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
    }

    @Override
    public boolean onKeyPreIme(int keyCode, KeyEvent event) {
        super.onKeyPreIme(keyCode, event);
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            m_listener.onKeyBoardBackPress();
        }
        return false;
    }

    public void setOnKeyEventListener(EditTextKeyEventCallbackListener listener) {
        m_listener = listener;
    }

    public interface EditTextKeyEventCallbackListener {
        void onKeyBoardBackPress();
    }
}