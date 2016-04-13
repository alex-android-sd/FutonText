package io.mochilogic.futontext;

import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.text.Layout;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.view.inputmethod.InputMethodManager;
import android.widget.ScrollView;
import android.widget.TextView;

import java.util.HashMap;
import java.util.Map;


public class FutonTextActivity extends FragmentActivity
        implements CommentBoxFragment.CommentFragmentCallbackListener {
    private int m_offset;
    private int m_line;
    private int m_textX;
    private int m_textY;
    private int m_sentenceY;
    private int m_start;
    private int m_end;
    private String original;
    private Animation slideDown;
    private Animation slideUp;
    private TextView m_foreTextView;
    private TextView m_backTextView;
    private boolean isFutonTextUnFolded = false;
    private boolean isKeyboardOut = false;
    private CommentBoxFragment m_commentBoxFragment;
    private ScrollView m_scrollView;
    public static final int VIEW_COMMENT = 1;
    private HashMap<Integer,String> m_commentsHashMap = new HashMap<Integer,String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_futon_text);

        m_foreTextView = (TextView) findViewById(R.id.foreTextView);
        m_backTextView = (TextView) findViewById(R.id.backTextView);
        m_scrollView = (ScrollView) findViewById(R.id.scrollView);

        Typeface typeFace = Typeface.createFromAsset(getAssets(), "Tinos-Regular.ttf");
        m_foreTextView.setTypeface(typeFace);
        m_backTextView.setTypeface(typeFace);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            m_foreTextView.setBreakStrategy(Layout.BREAK_STRATEGY_SIMPLE);
            m_backTextView.setBreakStrategy(Layout.BREAK_STRATEGY_SIMPLE);
            original = getString(R.string.lorem);
        }

        original = getString(R.string.lorem).replace(" ", "\u00A0");
        m_foreTextView.setText(original);

        setUpAnimations();

        //Determine index of char that was touched in TextView
        m_foreTextView.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                m_textX = (int) event.getX();
                m_textY = (int) event.getY();

                Layout layout = ((TextView) v).getLayout();

                if (layout != null) {
                    m_line = layout.getLineForVertical(m_textY);
                    m_offset = layout.getOffsetForHorizontal(m_line, m_textX);
                    m_sentenceY = layout.getLineTop(m_line);
                }

                return false;
            }
        });

        m_foreTextView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {

                if (!isFutonTextUnFolded) {
                    //find start and end of the long-clicked sentence
                    findSentenceStartEndPositions();

                    //If we did not click in padding...
                    if (m_offset <= m_foreTextView.length()) {
                        foldOutFuton();
                    }
                }
                return true;
            }
        });
    }

    public void onClickSave(View v) {
        String comment = m_commentBoxFragment.getComment();
        if(comment != null){
            m_commentsHashMap.put(m_start,comment);
        }
        foldInFutonText();
    }

    public void onClickDelete(View v){
        m_commentsHashMap.remove(m_start);
        foldInFutonText();
    }

    public void onClickClose(View v) {
        foldInFutonText();
    }

    @Override
    public void onKeyBackPress() {
        isKeyboardOut = false;
        foldInFutonText();
    }

    @Override
    public void onFocusChangeCallBack(boolean hasFocus) {
        if (!hasFocus) {
            foldInFutonText();
        } else {
            isKeyboardOut = true;
        }
    }

    public void foldOutFuton(){
        setUpCommentFragment();
        splitText();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                ft.setCustomAnimations(R.anim.slide_in_left, 0);
                ft.replace(R.id.commentBox_fragment_container, m_commentBoxFragment);
                ft.commit();
            }
        },50);

        isFutonTextUnFolded = true;
    }

    public void foldInFutonText() {
        if (isFutonTextUnFolded) {
            if (isKeyboardOut) {
                closeKeyboard();
                final Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        joinText(null);
                    }
                }, 300);
            }
            else{
                joinText(null);
            }
        }
    }

    public void joinText(View v) {
        isFutonTextUnFolded = false;
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.setCustomAnimations(0, R.anim.slide_out_left);
        ft.remove(m_commentBoxFragment);
        ft.commit();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                m_backTextView.startAnimation(slideUp);
            }
        },50);
    }

    private void splitText() {
        /*I remove the portion of the foreground textview that comes after
         the end of the selected sentence.

         Because my fragment container has the attribute:
         layout_below="@+id/foreTextView"
         the fragment container will reposition itself
         directly below the remaining portion of foretextview.
         */
        if (m_end < m_foreTextView.getText().toString().length() - 1) {
            m_foreTextView.setText(original.substring(0, m_end + 1));
        }

        Spannable foreSpannable = new SpannableString(m_foreTextView.getText()
                .toString());
        foreSpannable.setSpan(new ForegroundColorSpan(Color.parseColor("#B23AEE")), m_start, m_end,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);


        Spannable backSpannable = new SpannableString(original);
        backSpannable.setSpan(new ForegroundColorSpan(Color.parseColor("#00000000")), 0, m_end,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        m_foreTextView.setText(foreSpannable);
        m_backTextView.setText(backSpannable);
        m_backTextView.startAnimation(slideDown);
    }

    //Method to find index of beginning and end of the sentence by looking for '.'
    private void findSentenceStartEndPositions() {

        for (int x = m_offset - 1; x >= 0; x--) {

            if ((original.charAt(x)) == '.') {
                if(original.charAt(x+1)=='\u00A0'){
                    m_start = x + 2;
                }
                else{
                    m_start = x + 1;
                }
                break;
            }
            else if (x == 0) {
                m_start = 0;
            }
        }
        for (int x = m_offset + 1; x < original.length(); x++) {

            if ((original.charAt(x)) == '.') {
                m_end = x + 1;
                break;
            }
        }

    }

    private void setUpAnimations() {
        Resources r = getResources();
        float px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 160, r.getDisplayMetrics());
        slideDown = new TranslateAnimation(0,0,0,px);
        slideDown.setInterpolator(this,R.anim.my_anticipate_interpolator);
        slideDown.setDuration(450);
        slideDown.setFillEnabled(true);
        slideDown.setFillAfter(true);
        slideDown.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                ObjectAnimator.ofInt(m_scrollView, "scrollY", m_sentenceY).setDuration(250).start();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        int[] location = new int[2];
        m_backTextView.getLocationOnScreen(location);
        int y = location[1];

        slideUp = new TranslateAnimation(0,0,y + px,0);
        slideUp.setInterpolator(this,R.anim.my_anticipate_interpolator);
        slideUp.setDuration(450);
        slideUp.setFillEnabled(true);
        slideUp.setFillAfter(true);
        slideUp.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                m_foreTextView.setText(original);
                m_backTextView.setText(original);
                Spannable foreSpannable;
                if(!m_commentsHashMap.isEmpty()){
                    foreSpannable = new SpannableString(m_foreTextView.getText()
                            .toString());
                    int location;
                    for(Map.Entry<Integer, String> entry : m_commentsHashMap.entrySet()) {
                        location = entry.getKey();
                        foreSpannable.setSpan(new ForegroundColorSpan(Color.parseColor("#B23AEE")), location, location + 1,
                                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    }
                    m_foreTextView.setText(foreSpannable);
                }
                ObjectAnimator.ofInt(m_scrollView, "scrollY", m_scrollView.getTop()).setDuration(200).start();
            }
        });
    }

    private void setUpCommentFragment() {
        m_commentBoxFragment = new CommentBoxFragment();
        Bundle bundle_comment = new Bundle();
        String comment = m_commentsHashMap.get(m_start);
        if(comment!=null) {
            bundle_comment.putInt("MODE", FutonTextActivity.VIEW_COMMENT);
            bundle_comment.putString("COMMENT",comment);
        }
        m_commentBoxFragment.setArguments(bundle_comment);
        m_commentBoxFragment.setCommentBoxCallbackListener(this);
    }


    public void closeKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
        isKeyboardOut = false;
    }
}

