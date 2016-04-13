package io.mochilogic.futontext;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * Created by asanchez on 3/29/16.
 */
public class CommentBoxFragment extends Fragment implements KeyEventEditText.EditTextKeyEventCallbackListener {
    private View m_view;
    private KeyEventEditText m_editText;
    private TextView m_comment;
    private TextView m_remove;
    private TextView m_save;
    private CommentFragmentCallbackListener m_listener;

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        Bundle bundle = this.getArguments();
        m_view = inflater.inflate(R.layout.fragment_comment_box, container, false);
        m_editText = (KeyEventEditText) m_view.findViewById(R.id.edit_text);
        m_editText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                m_listener.onFocusChangeCallBack(hasFocus);
            }
        });
        m_editText.setOnKeyEventListener(this);
        m_comment = (TextView)m_view.findViewById(R.id.comment_text);

        if (bundle.getInt("MODE", 0) == FutonTextActivity.VIEW_COMMENT) {
            m_editText.setVisibility(View.GONE);
            m_save = (TextView) m_view.findViewById(R.id.save);
            m_save.setVisibility(View.GONE);
            m_remove = (TextView) m_view.findViewById(R.id.remove);
            m_remove.setVisibility(View.VISIBLE);
            m_comment.setVisibility(View.VISIBLE);
            m_comment.setText(bundle.getString("COMMENT"));
        }

        return m_view;
    }

    public void setCommentBoxCallbackListener(CommentFragmentCallbackListener listener) {
        m_listener = listener;
    }

    public String getComment() {
        if(m_editText.getText().toString().trim().length() > 0){
            return m_editText.getText().toString();
        }
        else{
            return null;
        }
    }

    @Override
    public void onKeyBoardBackPress() {
        m_listener.onKeyBackPress();
    }

    public interface CommentFragmentCallbackListener {
        void onFocusChangeCallBack(boolean hasFocus);

        void onKeyBackPress();
    }
}